#!/usr/bin/env bash
set -euo pipefail

repo_root="$(git rev-parse --show-toplevel)"
guard="$repo_root/.agents/hooks/guard-command.sh"
verify="$repo_root/.agents/hooks/verify-before-pr.sh"
secret_scan="$repo_root/.agents/hooks/scan-secrets.sh"

expect_blocked() {
  local command_text="$1"
  if bash "$guard" "$command_text" >/dev/null 2>&1; then
    printf 'expected blocked command: %s\n' "$command_text" >&2
    exit 1
  fi
}

expect_allowed() {
  local command_text="$1"
  bash "$guard" "$command_text" >/dev/null
}

expect_blocked 'gh pr merge 165'
expect_blocked 'gh pr merge; echo bypass'
expect_blocked 'gh --repo yourssu/signal-backend pr merge 165'
expect_blocked 'command gh pr merge 165'
expect_blocked "bash -c 'git push origin feature'"
expect_blocked '/usr/bin/git push origin feature'
expect_blocked "bash -lc 'git push origin feature'"
expect_blocked "sh -xc 'gh pr merge 165'"
expect_blocked 'gh pr --repo yourssu/signal-backend merge 165'
expect_blocked 'gh pr --repo yourssu/signal-backend create --base main'
expect_blocked 'git -c alias.publish=push publish origin HEAD'
expect_blocked 'gh api -X PUT repos/yourssu/signal-backend/pulls/165/merge'
expect_blocked "gh api graphql -f 'query=mutation { enablePullRequestAutoMerge(input: {}) { clientMutationId } }'"
expect_blocked 'gh api -X POST repos/yourssu/signal-backend/pulls'
expect_blocked 'gh api -X POST repos/yourssu/signal-backend/merges -f base=main -f head=feature'
expect_blocked 'gh api repos/yourssu/signal-backend/pulls -f title=test -f head=feature -f base=main'
expect_blocked 'gh api repos/yourssu/signal-backend/merges -f base=main -f head=feature'
expect_blocked 'X=1 git push origin feature'
expect_blocked 'X=1 gh pr merge 165'
expect_blocked 'nohup git push origin feature'
expect_blocked 'git push origin feature'
expect_blocked 'gh pr create --base main'
expect_blocked 'cat .env'
expect_blocked 'cat .env.local'
expect_blocked 'printenv'
expect_blocked 'env'
expect_blocked 'env -0'
expect_blocked 'env FOO=bar'
expect_blocked 'git switch main && git commit -m bypass'
main_repo="$(mktemp -d)"
git -C "$main_repo" init -qb main
expect_blocked "git -C $main_repo commit -m bypass"
expect_blocked "HARNESS_BRANCH=feature git -C $main_repo commit -m bypass"
expect_allowed './gradlew test'

secret_repo="$(mktemp -d)"
git -C "$secret_repo" init -q
git -C "$secret_repo" config user.email harness@example.com
git -C "$secret_repo" config user.name harness
printf 'safe\n' > "$secret_repo/safe.txt"
git -C "$secret_repo" add safe.txt
git -C "$secret_repo" commit -qm init
printf '%s%s\n' 'sk-' 'proj-abcdefghijklmnopqrstuvwxyz' > "$secret_repo/leak.txt"
git -C "$secret_repo" add leak.txt
if (cd "$secret_repo" && bash "$secret_scan") >/dev/null 2>&1; then
  printf 'expected staged secret to be blocked\n' >&2
  exit 1
fi
git -C "$secret_repo" commit -qm leak
if (cd "$secret_repo" && SECRET_SCAN_BASE_REF=HEAD~1 bash "$secret_scan") >/dev/null 2>&1; then
  printf 'expected committed secret to be blocked\n' >&2
  exit 1
fi

verify_output="$(VERIFY_DRY_RUN=1 HARNESS_BASE_REF=origin/main bash "$verify")"
printf '%s\n' "$verify_output" | grep -q '하네스 테스트'
printf '%s\n' "$verify_output" | grep -q 'Shell 문법'

adapter_root="$(mktemp -d)"
HARNESS_PROJECT_ROOT="$adapter_root" python3 "$repo_root/.agents/hooks/install-local-adapters.py" >/dev/null
python3 - "$adapter_root" <<'PY'
import json
import sys
from pathlib import Path

root = Path(sys.argv[1])
for relative in (Path('.claude/settings.json'), Path('.codex/hooks.json')):
    data = json.loads((root / relative).read_text())
    command = data['hooks']['PreToolUse'][0]['hooks'][0]['command']
    assert '.agents/hooks/guard-command.sh' in command
    assert '/Users/' not in command
PY

printf 'harness tests passed\n'
