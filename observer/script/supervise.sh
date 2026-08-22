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
MEMORY_ALERT_INTERVAL_COUNT="${MEMORY_ALERT_INTERVAL_COUNT:-10}"
MEMORY_RECOVERY_INTERVAL_COUNT="${MEMORY_RECOVERY_INTERVAL_COUNT:-10}"
MEMORY_AVAILABLE_THRESHOLD_KB="${MEMORY_AVAILABLE_THRESHOLD_KB:-102400}"
MEMORY_PSI_FULL_THRESHOLD="${MEMORY_PSI_FULL_THRESHOLD:-1.00}"
MEMINFO_FILE="${MEMINFO_FILE:-/proc/meminfo}"
MEMORY_PRESSURE_FILE="${MEMORY_PRESSURE_FILE:-/proc/pressure/memory}"
VMSTAT_FILE="${VMSTAT_FILE:-/proc/vmstat}"
mkdir -p "$STATE_DIR"

slack_post() {
  text=$(printf '%b' "$1")
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
  awk -v key="$1" '$1 == key ":" {print $2}' "$MEMINFO_FILE" 2>/dev/null
}

memory_psi_full_avg60() {
  awk '$1 == "full" {for (i=2; i<=NF; i++) if ($i ~ /^avg60=/) {sub(/^avg60=/, "", $i); print $i}}' \
    "$MEMORY_PRESSURE_FILE" 2>/dev/null
}

kernel_oom_count() {
  awk '$1 == "oom_kill" {print $2}' "$VMSTAT_FILE" 2>/dev/null
}

top_memory_containers() {
  docker stats --no-stream --format '{{.MemPerc}}\t{{.Name}}\t{{.MemUsage}}' 2>/dev/null |
    sort -rn | head -3 | awk -F '\t' '{printf "%s%s %s (%s)", separator, $2, $3, $1; separator=", "}'
}

check_oom() {
  current=$(kernel_oom_count)
  [ -z "$current" ] && return
  counter="$STATE_DIR/resource-memory-oom-count"
  previous=$(cat "$counter" 2>/dev/null || true)
  printf '%s' "$current" > "$counter"
  [ -z "$previous" ] && return
  [ "$current" -le "$previous" ] && return

  targets=""
  for component in spring observer admin; do
    oom_killed=$(docker inspect --format '{{.State.OOMKilled}}' "${PROJECT_NAME}-${component}" 2>/dev/null || true)
    [ "$oom_killed" = true ] && targets="${targets}${targets:+, }${PROJECT_NAME}-${component}"
  done
  [ -z "$targets" ] && targets="커널 OOM 발생 (대상은 journal/docker inspect 확인)"
  slack_post "🔴 [${ENVIRONMENT_LABEL}] EC2 OOM 발생
\`\`\`
발생 시각  $(date '+%Y-%m-%d %H:%M:%S %Z')
대상       ${targets}
증가 횟수  $((current - previous))회
자동 조치  메모리 감시에서는 재시작하지 않음
확인       journalctl -k --since '15 min ago' | grep -i oom
\`\`\`" >/dev/null || true
  touch "$STATE_DIR/resource-memory-oom-occurred"
}

check_memory_pressure() {
  available_kb=$(resource_value_kb MemAvailable)
  psi=$(memory_psi_full_avg60)
  available_kb=${available_kb:-0}
  psi=${psi:-0}
  if [ "$available_kb" -gt 0 ] && [ "$available_kb" -lt "$MEMORY_AVAILABLE_THRESHOLD_KB" ]; then
    logger -t signal-supervisor "low memory available_kb=$available_kb psi_full_avg60=$psi" 2>/dev/null || true
  fi
  breached=0
  if [ "$available_kb" -gt 0 ] && [ "$available_kb" -lt "$MEMORY_AVAILABLE_THRESHOLD_KB" ] &&
      awk -v value="$psi" -v threshold="$MEMORY_PSI_FULL_THRESHOLD" 'BEGIN {exit !(value >= threshold)}'; then
    breached=1
  fi

  count_file="$STATE_DIR/resource-memory-count"
  recovery_file="$STATE_DIR/resource-memory-recovery-count"
  alerted="$STATE_DIR/resource-memory-alerted"
  started="$STATE_DIR/resource-memory-started"
  minimum="$STATE_DIR/resource-memory-minimum-kb"
  maximum_psi="$STATE_DIR/resource-memory-maximum-psi"

  if [ "$breached" -eq 1 ]; then
    echo 0 > "$recovery_file"
    count=$(cat "$count_file" 2>/dev/null || echo 0)
    count=$((count + 1))
    echo "$count" > "$count_file"
    [ -e "$started" ] || date +%s > "$started"
    old_min=$(cat "$minimum" 2>/dev/null || echo "$available_kb")
    [ "$available_kb" -lt "$old_min" ] && old_min=$available_kb
    echo "$old_min" > "$minimum"
    old_max=$(cat "$maximum_psi" 2>/dev/null || echo "$psi")
    if awk -v current="$psi" -v old="$old_max" 'BEGIN {exit !(current > old)}'; then old_max=$psi; fi
    echo "$old_max" > "$maximum_psi"

    if [ "$count" -ge "$MEMORY_ALERT_INTERVAL_COUNT" ] && [ ! -e "$alerted" ]; then
      containers=$(top_memory_containers)
      [ -z "$containers" ] && containers="확인 불가"
      [ -e "$STATE_DIR/resource-memory-oom-occurred" ] && warning_oom_status="발생" || warning_oom_status="발생 없음"
      if slack_post "🟠 [${ENVIRONMENT_LABEL}] EC2 메모리 압력 지속
\`\`\`
감지 시각  $(date '+%Y-%m-%d %H:%M:%S %Z')
지속 시간  $((MEMORY_ALERT_INTERVAL_COUNT * 30))초
가용 메모리 $((available_kb / 1024)) MiB
PSI full    ${psi}% (avg60)
OOM        ${warning_oom_status}
상위 사용   ${containers}
자동 조치   없음
\`\`\`" >/dev/null; then
        touch "$alerted"
      fi
    fi
    return
  fi

  if [ ! -e "$alerted" ]; then
    echo 0 > "$count_file"
    echo 0 > "$recovery_file"
    unlink "$started" "$minimum" "$maximum_psi" "$STATE_DIR/resource-memory-oom-occurred" 2>/dev/null || true
    return
  fi

  recovery_count=$(cat "$recovery_file" 2>/dev/null || echo 0)
  recovery_count=$((recovery_count + 1))
  echo "$recovery_count" > "$recovery_file"
  [ "$recovery_count" -lt "$MEMORY_RECOVERY_INTERVAL_COUNT" ] && return

  started_at=$(cat "$started" 2>/dev/null || date +%s)
  minimum_kb=$(cat "$minimum" 2>/dev/null || echo 0)
  max_psi=$(cat "$maximum_psi" 2>/dev/null || echo 0)
  [ -e "$STATE_DIR/resource-memory-oom-occurred" ] && oom_status="발생" || oom_status="발생 없음"
  if slack_post "🟢 [${ENVIRONMENT_LABEL}] EC2 메모리 압력 해소
\`\`\`
복구 시각  $(date '+%Y-%m-%d %H:%M:%S %Z')
장애 시간  $(( $(date +%s) - started_at ))초
최저 가용   $((minimum_kb / 1024)) MiB
최대 PSI    ${max_psi}% (avg60)
OOM        ${oom_status}
복구 기준   $((MEMORY_RECOVERY_INTERVAL_COUNT * 30))초 연속 정상
\`\`\`" >/dev/null; then
    unlink "$alerted" "$started" "$minimum" "$maximum_psi" "$STATE_DIR/resource-memory-oom-occurred" 2>/dev/null || true
    echo 0 > "$count_file"
    echo 0 > "$recovery_file"
  fi
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
  check_oom
  check_memory_pressure

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
