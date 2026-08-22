#!/bin/bash
set -euo pipefail

source .env

PROJECT_NAME=${PROJECT_NAME:?}
ECR_REGISTRY=${ECR_REGISTRY:?}
SERVER_PORT=${SERVER_PORT:?}
ENVIRONMENT=${ENVIRONMENT:?}
ENVIRONMENT_LABEL=$(printf '%s' "$ENVIRONMENT" | tr '[:lower:]' '[:upper:]')
SPRING_CONTAINER="${PROJECT_NAME}-spring"
OBSERVER_CONTAINER="${PROJECT_NAME}-observer"
ADMIN_CONTAINER="${PROJECT_NAME}-admin"
CONTAINERS="$SPRING_CONTAINER $OBSERVER_CONTAINER $ADMIN_CONTAINER"
NETWORK_NAME="${PROJECT_NAME}-network"
SUPERVISOR_SERVICE="${PROJECT_NAME}-supervisor.service"
IMAGE_REPOSITORY="$ECR_REGISTRY/yourssu/${PROJECT_NAME}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
IMAGE_NAME="$IMAGE_REPOSITORY:$IMAGE_TAG"
HEALTH_TIMEOUT="${DEPLOY_HEALTH_TIMEOUT:-180}"
MIN_FREE_DISK_KB="${DEPLOY_MIN_FREE_DISK_KB:-2097152}"
DEPLOY_STARTED_AT=$(date +%s)
SUPERVISOR_STOPPED=0

exec 9>"/tmp/${PROJECT_NAME}-deploy.lock"
if ! flock -n 9; then
  echo "Another deployment is already running."
  exit 75
fi

slack_post() {
  text=$1
  [ -n "${SLACK_TOKEN:-}" ] || return 0
  [ -n "${SLACK_LOG_CHANNEL:-}" ] || return 0
  response=$(curl -fsS --max-time 10 \
    -H @<(printf 'Authorization: Bearer %s\n' "$SLACK_TOKEN") \
    -H 'Content-Type: application/json' \
    --data "$(python3 -c 'import json,sys; print(json.dumps({"channel":sys.argv[1],"text":sys.argv[2]}))' "$SLACK_LOG_CHANNEL" "$text")" \
    https://slack.com/api/chat.postMessage 2>/dev/null || true)
  case "$response" in *'"ok":true'*) return 0 ;; esac
  logger -t signal-deploy "SLACK DELIVERY FAILED" 2>/dev/null || true
  return 0
}

component_status() {
  docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$1" 2>/dev/null || echo missing
}

wait_healthy() {
  container=$1
  deadline=$(( $(date +%s) + HEALTH_TIMEOUT ))
  while [ "$(date +%s)" -le "$deadline" ]; do
    status=$(component_status "$container")
    [ "$status" = healthy ] && return 0
    case "$status" in exited|dead|missing) return 1 ;; esac
    sleep 5
  done
  return 1
}

remove_containers() {
  for container in $CONTAINERS; do
    if [ -n "$(docker ps -aq -f name=^/${container}$)" ]; then
      docker rm -f "$container" >/dev/null
    fi
  done
}

run_spring() {
  docker run -d --name "$SPRING_CONTAINER" --network "$NETWORK_NAME" --restart no \
    -p "$SERVER_PORT:$SERVER_PORT" -v "$(pwd)/logs:/app/logs" --env-file .env \
    -e COMPONENT=spring \
    --health-cmd "/app/venv/bin/python /app/script/healthcheck.py spring" \
    --health-interval 30s --health-retries 3 --health-timeout 10s --health-start-period 60s \
    "$1" >/dev/null
}

run_observer() {
  docker run -d --name "$OBSERVER_CONTAINER" --network "$NETWORK_NAME" --restart no \
    -v "$(pwd)/logs:/app/logs" --env-file .env -e COMPONENT=observer \
    --health-cmd "/app/venv/bin/python /app/script/healthcheck.py observer" \
    --health-interval 30s --health-retries 3 --health-timeout 10s --health-start-period 30s \
    "$1" >/dev/null
}

run_admin() {
  docker run -d --name "$ADMIN_CONTAINER" --network "$NETWORK_NAME" --restart no \
    -p 127.0.0.1:3005:3005 --env-file .env -e COMPONENT=admin \
    --health-cmd "/app/venv/bin/python /app/script/healthcheck.py admin" \
    --health-interval 30s --health-retries 3 --health-timeout 10s --health-start-period 30s \
    "$1" >/dev/null
}

start_stack() {
  image=$1
  run_spring "$image" || return 1
  wait_healthy "$SPRING_CONTAINER" || return 1
  run_observer "$image" || return 1
  run_admin "$image" || return 1
  wait_healthy "$OBSERVER_CONTAINER" || return 1
  wait_healthy "$ADMIN_CONTAINER" || return 1
}

install_supervisor() {
  sudo tee "/etc/systemd/system/$SUPERVISOR_SERVICE" >/dev/null <<EOF
[Unit]
Description=Signal component health supervisor
Requires=docker.service
After=docker.service network-online.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=$(pwd)
Environment=SUPERVISOR_ENV_FILE=$(pwd)/.env
ExecStart=/bin/bash $(pwd)/supervise.sh
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF
  sudo systemctl daemon-reload
  sudo systemctl enable "$SUPERVISOR_SERVICE" >/dev/null
  sudo systemctl restart "$SUPERVISOR_SERVICE"
  SUPERVISOR_STOPPED=0
}

restore_supervisor_on_exit() {
  if [ "$SUPERVISOR_STOPPED" -eq 1 ]; then
    sudo systemctl restart "$SUPERVISOR_SERVICE" >/dev/null 2>&1 || true
  fi
}
trap restore_supervisor_on_exit EXIT

cleanup_old_images() {
  current_id=$1
  previous_id=${2:-}
  docker images "$IMAGE_REPOSITORY" --format '{{.ID}}' | awk '!seen[$0]++' | while read -r image_id; do
    [ -n "$image_id" ] || continue
    [ "$image_id" = "$current_id" ] && continue
    [ -n "$previous_id" ] && [ "$image_id" = "$previous_id" ] && continue
    docker images "$IMAGE_REPOSITORY" --format '{{.Repository}}:{{.Tag}} {{.ID}}' | \
      awk -v id="$image_id" '$2 == id {print $1}' | while read -r image_ref; do
        docker image rm "$image_ref" >/dev/null 2>&1 || true
      done
  done
}

format_policy() {
  python3 -c 'import sys
raw=sys.argv[1]
try:
    formatted=[]
    for item in raw.split("."):
        name, values=item.split("@", 1)
        price, count=values.rsplit("n", 1)
        formatted.append(f"{name}@{price}원/{count}장")
    print(" · ".join(formatted))
except ValueError:
    print(raw)' "$1"
}

validate_preflight() {
  for name in DB_URL DB_USERNAME DB_PASSWORD ADMIN_ACCESS_KEY CONTACT_SECRET_KEY JWT_SECRET \
    OPENAI_URL OPENAI_API_KEY OPENAI_MODEL SLACK_TOKEN SLACK_ADMIN_CHANNEL SLACK_LOG_CHANNEL; do
    value=${!name-}
    if [ -z "$value" ]; then
      echo "Required environment variable is empty: $name" >&2
      return 1
    fi
  done
  available_kb=$(df -Pk "$(pwd)" | awk 'NR==2 {print $4}')
  if [ "${available_kb:-0}" -lt "$MIN_FREE_DISK_KB" ]; then
    echo "Not enough disk space to pull a deployment image." >&2
    return 1
  fi
}

echo "Starting deployment process..."
echo "Image name: $IMAGE_NAME"
validate_preflight
previous_image_id=$(docker inspect --format '{{.Image}}' "$SPRING_CONTAINER" 2>/dev/null || true)

echo "Logging in to Amazon ECR Public..."
aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin public.ecr.aws
echo "Pulling deployment image..."
docker pull "$IMAGE_NAME"
target_image_id=$(docker image inspect --format '{{.Id}}' "$IMAGE_NAME")
[ -n "$target_image_id" ]

docker network inspect "$NETWORK_NAME" >/dev/null 2>&1 || docker network create "$NETWORK_NAME" >/dev/null
mkdir -p "$(pwd)/logs/state"
sudo systemctl stop "$SUPERVISOR_SERVICE" >/dev/null 2>&1 || true
SUPERVISOR_STOPPED=1
remove_containers

echo "Starting deployment image: $target_image_id"
if ! start_stack "$target_image_id"; then
  failed_at=$(date '+%Y-%m-%d %H:%M:%S %Z')
  for container in $CONTAINERS; do
    docker logs --since 15m "$container" >"logs/${container}-deploy-failure.log" 2>&1 || true
  done
  remove_containers

  if [ -n "$previous_image_id" ] && start_stack "$previous_image_id"; then
    install_supervisor
    slack_post "🔴 [${ENVIRONMENT_LABEL}] Signal 배포 실패\n\`\`\`\n발생 시각  ${failed_at}\n대상 버전  ${IMAGE_TAG}\n실패 단계  새 버전 healthcheck\n서비스 상태 이전 이미지로 자동 복구 완료\n\`\`\`"
    echo "Deployment failed; previous image restored: $previous_image_id" >&2
    exit 1
  fi

  SUPERVISOR_STOPPED=0
  mention=""
  [ "$(printf '%s' "$ENVIRONMENT" | tr '[:upper:]' '[:lower:]')" = prod ] && mention="<!channel> "
  slack_post "${mention}🚨 [${ENVIRONMENT_LABEL}] Signal 배포 및 자동 복구 실패\n\`\`\`\n발생 시각  ${failed_at}\n대상 버전  ${IMAGE_TAG}\nSpring     $(component_status "$SPRING_CONTAINER")\nObserver   $(component_status "$OBSERVER_CONTAINER")\nAdmin      $(component_status "$ADMIN_CONTAINER")\nSupervisor stopped (중복 재시작·알림 방지)\n필요 조치  EC2 배포 로그와 컨테이너 로그 확인\n\`\`\`"
  echo "Deployment and rollback failed." >&2
  exit 1
fi

install_supervisor
cleanup_old_images "$target_image_id" "$previous_image_id" || true

duration=$(( $(date +%s) - DEPLOY_STARTED_AT ))
slack_post "🟢 [${ENVIRONMENT_LABEL}] Signal 배포 완료\n\`\`\`\n배포 시각  $(date '+%Y-%m-%d %H:%M:%S %Z')\n버전       ${IMAGE_TAG}\n소요 시간  ${duration}초\nSpring     healthy\nObserver   healthy\nAdmin      healthy\n티켓 정책  $(format_policy "${TICKET_PRICE_POLICY:-미설정}")\n가입 할인  $(format_policy "${TICKET_PRICE_REGISTERED_POLICY:-미설정}")\n\`\`\`"

echo "Deployment completed successfully!"
docker ps -f name="$PROJECT_NAME"
