#!/bin/bash
set -u

source "${SUPERVISOR_ENV_FILE:-$(dirname "$0")/.env}"

PROJECT_NAME=${PROJECT_NAME:?}
STATE_DIR="$(pwd)/logs/state"
mkdir -p "$STATE_DIR"

alert() {
  component=$1
  state=$2
  detail=$3
  environment_label=$(printf '%s' "$ENVIRONMENT" | tr '[:lower:]' '[:upper:]')
  component_label=$(printf '%s' "$component" | tr '[:lower:]' '[:upper:]')
  detected_at=$(date '+%Y-%m-%d %H:%M:%S %Z')
  text="${state} [${environment_label}] ${component_label}
\`\`\`
• 시간: ${detected_at}
• 컨테이너: ${PROJECT_NAME}-${component}
• 상세: ${detail}
\`\`\`"
  response=$(curl -fsS --max-time 10 -H @<(printf 'Authorization: Bearer %s\n' "$SLACK_TOKEN") -H 'Content-Type: application/json' \
    --data "$(python3 -c 'import json,sys; print(json.dumps({"channel":sys.argv[1],"text":sys.argv[2]}))' "$SLACK_LOG_CHANNEL" "$text")" \
    https://slack.com/api/chat.postMessage 2>/dev/null || true)
  case "$response" in *'"ok":true'*) return 0 ;; esac
  logger -t signal-supervisor "SLACK DELIVERY FAILED component=$component state=$state"
  return 1
}

check_component() {
  component=$1
  container="${PROJECT_NAME}-${component}"
  budget="$STATE_DIR/${component}-restart-budget"
  healthy_count="$STATE_DIR/${component}-healthy-count"
  recovery_pending="$STATE_DIR/${component}-recovery-pending"
  restarts=$(cat "$budget" 2>/dev/null || echo 0)
  healthy=$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}unknown{{end}}' "$container" 2>/dev/null || echo missing)
  if [ "$healthy" = healthy ]; then
    if [ -e "$recovery_pending" ]; then
      alert "$component" "🟢 자동 복구 완료" "자동 재시작 후 healthcheck 정상"
      unlink "$recovery_pending" 2>/dev/null || true
    fi
    count=$(cat "$healthy_count" 2>/dev/null || echo 0)
    count=$((count + 1))
    echo "$count" > "$healthy_count"
    if [ "$count" -ge 10 ]; then
      echo 0 > "$budget"
      unlink "$STATE_DIR/${component}-manual-alerted" 2>/dev/null || true
    fi
    return
  fi
  [ "$healthy" = starting ] && return
  echo 0 > "$healthy_count"
  if [ "$restarts" -eq 0 ]; then
    alert "$component" "🔴 장애 감지" "healthcheck 3회 연속 실패; 자동 재시작 1/1 진행"
    echo 1 > "$budget"
    touch "$recovery_pending"
    docker restart "$container" >/dev/null
  else
    marker="$STATE_DIR/${component}-manual-alerted"
    if [ ! -e "$marker" ]; then
      alert "$component" "🚨 수동 조치 필요" "자동 재시작 후에도 비정상; EC2에서 docker logs --since 15m ${container} 확인 필요"
      touch "$marker"
    fi
  fi
}

while true; do
  check_component spring
  check_component observer
  check_component admin
  if [ "${SUPERVISOR_ONCE:-0}" = 1 ]; then break; fi
  sleep 30
done
