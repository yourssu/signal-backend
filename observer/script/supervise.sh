#!/bin/bash
set -u

source "$(dirname "$0")/.env"

PROJECT_NAME=${PROJECT_NAME:?}
STATE_DIR="$(pwd)/logs/state"
mkdir -p "$STATE_DIR"

alert() {
  component=$1
  state=$2
  detail=$3
  text="[${ENVIRONMENT^^}] ${component^^} ${state}: ${detail}"
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
  restarts=$(cat "$budget" 2>/dev/null || echo 0)
  healthy=$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}unknown{{end}}' "$container" 2>/dev/null || echo missing)
  if [ "$healthy" = healthy ]; then
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
    alert "$component" UNHEALTHY "3 consecutive failures; automatic restart 1/1"
    echo 1 > "$budget"
    docker restart "$container" >/dev/null
  else
    marker="$STATE_DIR/${component}-manual-alerted"
    if [ ! -e "$marker" ]; then
      alert "$component" "MANUAL ACTION REQUIRED" "automatic restart exhausted"
      touch "$marker"
    fi
  fi
}

while true; do
  check_component spring
  check_component observer
  sleep 30
done
