#!/usr/bin/env bash
set -euo pipefail

if [[ "$#" -eq 0 ]]; then
  printf 'usage: %s <issue-number> [issue-number...]\n' "$0" >&2
  exit 2
fi

repo_root="$(git rev-parse --show-toplevel)"
issue_repo="${HARNESS_ISSUE_REPOSITORY:-yourssu/signal-backend}"

for issue_number in "$@"; do
  if [[ ! "$issue_number" =~ ^[0-9]+$ ]]; then
    printf 'invalid issue number: %s\n' "$issue_number" >&2
    exit 2
  fi
  gh issue view "$issue_number" --repo "$issue_repo" --json number --jq .number >/dev/null
done
mkdir -p "$repo_root/.agents/local"
printf '%s\n' "$@" > "$repo_root/.agents/local/current-issues"
printf '[harness] Issue 연결 완료: %s\n' "$*"
