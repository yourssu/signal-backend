#!/usr/bin/env bash
set -euo pipefail

repo_root="$(git rev-parse --show-toplevel)"
issue_file="$repo_root/.agents/local/current-issues"

if [[ ! -s "$issue_file" ]] || grep -Evq '^[0-9]+$' "$issue_file"; then
  printf '[harness] 연결 Issue가 없습니다. set-issue.sh를 먼저 실행하세요.\n' >&2
  exit 2
fi

issue_repo="${HARNESS_ISSUE_REPOSITORY:-yourssu/signal-backend}"
while IFS= read -r issue_number; do
  gh issue view "$issue_number" --repo "$issue_repo" --json number --jq .number >/dev/null
done < "$issue_file"

current_branch="$(git branch --show-current)"
if [[ -z "$current_branch" || "$current_branch" == "main" ]]; then
  printf '[harness] main 또는 detached HEAD에서는 PR을 만들 수 없습니다.\n' >&2
  exit 2
fi

if [[ -n "$(git status --porcelain)" ]]; then
  printf '[harness] 커밋하지 않은 변경이 남아 있습니다.\n' >&2
  exit 2
fi

bash "$repo_root/.agents/hooks/verify-before-pr.sh"

body_file=""
args=("$@")
for ((index=0; index<${#args[@]}; index++)); do
  if [[ "${args[$index]}" == "--body-file" ]] && (( index + 1 < ${#args[@]} )); then
    body_file="${args[$((index + 1))]}"
  fi
done
if [[ -z "$body_file" || ! -s "$body_file" ]]; then
  printf '[harness] --body-file로 PR 본문을 제공해야 합니다.\n' >&2
  exit 2
fi
while IFS= read -r issue_number; do
  if ! grep -Fqx "Closes #$issue_number" "$body_file"; then
    printf '[harness] PR 본문에 Closes #%s가 없습니다.\n' "$issue_number" >&2
    exit 2
  fi
done < "$issue_file"

git push -u origin "$current_branch"
gh pr create --repo "$issue_repo" --base main --head "$current_branch" "$@"
