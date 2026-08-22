#!/bin/bash
set -u

source "${SUPERVISOR_ENV_FILE:-$(dirname "$0")/.env}"

PROJECT_NAME=${PROJECT_NAME:?}
ENVIRONMENT=${ENVIRONMENT:?}
ENVIRONMENT_LABEL=$(printf '%s' "$ENVIRONMENT" | tr '[:lower:]' '[:upper:]')
STATE_DIR="$(pwd)/logs/state"
ACTIVE_INCIDENT="$STATE_DIR/runtime-active-incident"
INCIDENT_TS="$STATE_DIR/runtime-incident-ts"
INCIDENT_STARTED="$STATE_DIR/runtime-incident-started"
RESTART_ATTEMPTED="$STATE_DIR/runtime-restart-attempted"
MANUAL_ALERTED="$STATE_DIR/runtime-manual-alerted"
RESOURCE_INTERVAL_COUNT="${RESOURCE_INTERVAL_COUNT:-10}"
mkdir -p "$STATE_DIR"

slack_post() {
  text=$1
  thread_ts=${2:-}
  payload=$(python3 -c 'import json,sys; p={"channel":sys.argv[1],"text":sys.argv[2]}; ts=sys.argv[3]; p.update({"thread_ts":ts}) if ts else None; print(json.dumps(p))' \
    "$SLACK_LOG_CHANNEL" "$text" "$thread_ts")
  response=$(curl -fsS --max-time 10 \
    -H @<(printf 'Authorization: Bearer %s\n' "$SLACK_TOKEN") \
    -H 'Content-Type: application/json' --data "$payload" \
    https://slack.com/api/chat.postMessage 2>/dev/null || true)
  case "$response" in
    *'"ok":true'*) printf '%s' "$response" | python3 -c 'import json,sys; print(json.load(sys.stdin).get("ts", ""))'; return 0 ;;
  esac
  logger -t signal-supervisor "SLACK DELIVERY FAILED" 2>/dev/null || true
  return 1
}

status_of() {
  docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' \
    "${PROJECT_NAME}-$1" 2>/dev/null || echo missing
}

incident_thread() {
  cat "$INCIDENT_TS" 2>/dev/null || true
}

clear_runtime_incident() {
  unlink "$ACTIVE_INCIDENT" "$INCIDENT_TS" "$INCIDENT_STARTED" "$RESTART_ATTEMPTED" "$MANUAL_ALERTED" 2>/dev/null || true
}

start_incident() {
  key=$1
  spring_status=$2
  observer_status=$3
  admin_status=$4
  started_at=$(date +%s)
  occurred_at=$(date '+%Y-%m-%d %H:%M:%S %Z')
  incident_id="${key}-$(date '+%Y%m%d-%H%M%S')"

  if [ "$key" = stack ]; then
    title="🚨 [${ENVIRONMENT_LABEL}] Signal 전체 서비스 장애"
    impact="API 요청·이벤트 감지·관리자 명령 중 일부 또는 전체 사용 불가"
  else
    component_label=$(printf '%s' "$key" | tr '[:lower:]' '[:upper:]')
    title="🔴 [${ENVIRONMENT_LABEL}] ${component_label} 장애"
    case "$key" in
      spring) impact="API 요청 처리 불가" ;;
      observer) impact="이벤트 감지 및 Slack 알림 지연" ;;
      admin) impact="Slack 관리자 명령 처리 불가" ;;
    esac
  fi

  if [ "$spring_status" = missing ] && [ "$observer_status" = missing ] && [ "$admin_status" = missing ]; then
    automatic_action="재시작 불가 (컨테이너 없음); 배포 상태 확인"
  else
    automatic_action="비정상 컨테이너 재시작 1회"
  fi

  text="${title}
\`\`\`
발생 시각  ${occurred_at}
Spring     ${spring_status}
Observer   ${observer_status}
Admin      ${admin_status}
영향       ${impact}
자동 조치  ${automatic_action}
사건 ID    ${incident_id}
\`\`\`"
  ts=$(slack_post "$text" || true)
  printf '%s' "$key" > "$ACTIVE_INCIDENT"
  printf '%s' "$started_at" > "$INCIDENT_STARTED"
  [ -n "$ts" ] && printf '%s' "$ts" > "$INCIDENT_TS"
}

recover_incident() {
  key=$(cat "$ACTIVE_INCIDENT")
  started_at=$(cat "$INCIDENT_STARTED" 2>/dev/null || date +%s)
  duration=$(( $(date +%s) - started_at ))
  case "$key" in
    stack) key_label="Signal 전체 서비스" ;;
    spring) key_label="Spring API" ;;
    observer) key_label="Observer" ;;
    admin) key_label="Admin" ;;
  esac
  text="🟢 [${ENVIRONMENT_LABEL}] ${key_label} 복구 완료
\`\`\`
복구 시각  $(date '+%Y-%m-%d %H:%M:%S %Z')
장애 시간  ${duration}초
조치       컨테이너 재시작 후 healthcheck 정상
\`\`\`"
  slack_post "$text" "$(incident_thread)" >/dev/null || true
  clear_runtime_incident
}

manual_alert() {
  [ -e "$MANUAL_ALERTED" ] && return
  mention=""
  [ "$(printf '%s' "$ENVIRONMENT" | tr '[:upper:]' '[:lower:]')" = prod ] && mention="<!channel> "
  text="${mention}🚨 [${ENVIRONMENT_LABEL}] 자동 복구 실패 · 수동 조치 필요
\`\`\`
확인 시각  $(date '+%Y-%m-%d %H:%M:%S %Z')
Spring     $1
Observer   $2
Admin      $3
필요 조치  EC2에서 docker ps 및 docker logs --since 15m 확인
\`\`\`"
  if slack_post "$text" "$(incident_thread)" >/dev/null; then
    touch "$MANUAL_ALERTED"
  fi
}

check_runtime() {
  spring_status=$(status_of spring)
  observer_status=$(status_of observer)
  admin_status=$(status_of admin)
  bad_components=""
  bad_count=0
  starting_count=0
  for pair in "spring:$spring_status" "observer:$observer_status" "admin:$admin_status"; do
    component=${pair%%:*}
    status=${pair#*:}
    [ "$status" = starting ] && starting_count=$((starting_count + 1)) && continue
    [ "$status" = healthy ] && continue
    bad_components="$bad_components $component"
    bad_count=$((bad_count + 1))
  done

  if [ "$bad_count" -eq 0 ]; then
    [ "$starting_count" -gt 0 ] && return
    if [ -e "$ACTIVE_INCIDENT" ]; then
      recover_incident
    fi
    return
  fi

  [ "$bad_count" -ge 2 ] && key=stack || key=$(printf '%s' "$bad_components" | awk '{print $1}')
  if [ ! -e "$ACTIVE_INCIDENT" ]; then
    start_incident "$key" "$spring_status" "$observer_status" "$admin_status"
  fi

  if [ ! -e "$RESTART_ATTEMPTED" ]; then
    touch "$RESTART_ATTEMPTED"
    for component in $bad_components; do
      status=$(status_of "$component")
      [ "$status" = missing ] && continue
      docker restart "${PROJECT_NAME}-${component}" >/dev/null 2>&1 || true
    done
    return
  fi
  manual_alert "$spring_status" "$observer_status" "$admin_status"
}

resource_value_kb() {
  awk -v key="$1" '$1 == key ":" {print $2}' /proc/meminfo 2>/dev/null
}

check_resource_threshold() {
  name=$1
  breached=$2
  title=$3
  detail=$4
  counter="$STATE_DIR/resource-${name}-count"
  alerted="$STATE_DIR/resource-${name}-alerted"
  if [ "$breached" -eq 1 ]; then
    count=$(cat "$counter" 2>/dev/null || echo 0)
    count=$((count + 1))
    echo "$count" > "$counter"
    if [ "$count" -ge "$RESOURCE_INTERVAL_COUNT" ] && [ ! -e "$alerted" ]; then
      if slack_post "🟠 [${ENVIRONMENT_LABEL}] ${title}\n\`\`\`\n감지 시각  $(date '+%Y-%m-%d %H:%M:%S %Z')\n지속 시간  $((RESOURCE_INTERVAL_COUNT * 30))초\n${detail}\n\`\`\`" >/dev/null; then
        touch "$alerted"
      fi
    fi
  else
    echo 0 > "$counter"
    if [ -e "$alerted" ]; then
      slack_post "🟢 [${ENVIRONMENT_LABEL}] ${title} 해소\n\`\`\`\n복구 시각  $(date '+%Y-%m-%d %H:%M:%S %Z')\n\`\`\`" >/dev/null || true
      unlink "$alerted" 2>/dev/null || true
    fi
  fi
}

check_resources() {
  available_kb=$(resource_value_kb MemAvailable)
  swap_total_kb=$(resource_value_kb SwapTotal)
  swap_free_kb=$(resource_value_kb SwapFree)
  available_kb=${available_kb:-0}
  swap_total_kb=${swap_total_kb:-0}
  swap_free_kb=${swap_free_kb:-0}
  swap_used_kb=$((swap_total_kb - swap_free_kb))
  memory_breached=0
  if [ "$available_kb" -gt 0 ] && [ "$available_kb" -lt 204800 ] && [ "$swap_used_kb" -gt $((swap_total_kb / 2)) ]; then
    memory_breached=1
  fi
  check_resource_threshold memory "$memory_breached" "EC2 메모리 부족" \
    "가용 메모리 $((available_kb / 1024)) MiB\nSwap 사용   $((swap_used_kb / 1024)) MiB"

  disk_values=$(df -Pk "$(pwd)" 2>/dev/null | awk 'NR==2 {print $4, $5}')
  disk_available_kb=$(printf '%s' "$disk_values" | awk '{print $1}')
  disk_percent=$(printf '%s' "$disk_values" | awk '{gsub(/%/, "", $2); print $2}')
  disk_available_kb=${disk_available_kb:-99999999}
  disk_percent=${disk_percent:-0}
  disk_breached=0
  if [ "$disk_available_kb" -lt 5242880 ] || [ "$disk_percent" -ge 85 ]; then disk_breached=1; fi
  check_resource_threshold disk "$disk_breached" "EC2 디스크 용량 부족" \
    "남은 용량   $((disk_available_kb / 1024)) MiB\n디스크 사용 ${disk_percent}%"
}

while true; do
  check_runtime
  check_resources
  if [ "${SUPERVISOR_ONCE:-0}" = 1 ]; then break; fi
  sleep 30
done
