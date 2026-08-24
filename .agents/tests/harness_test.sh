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

git -C "$secret_repo" reset -q --hard HEAD~1
printf '%s=%s\n' 'JWT_SECRET' 'abcdefghijklmnopqrstuvwxyz123456' > "$secret_repo/generic-secret.txt"
git -C "$secret_repo" add generic-secret.txt
if (cd "$secret_repo" && bash "$secret_scan") >/dev/null 2>&1; then
  printf 'expected named generic secret to be blocked\n' >&2
  exit 1
fi

git -C "$secret_repo" reset -q --hard HEAD
printf '"%s": "%s"\n' 'JWT_SECRET' 'abcdefghijklmnopqrstuvwxyz123456' > "$secret_repo/json-secret.txt"
git -C "$secret_repo" add json-secret.txt
if (cd "$secret_repo" && bash "$secret_scan") >/dev/null 2>&1; then
  printf 'expected JSON secret to be blocked\n' >&2
  exit 1
fi

git -C "$secret_repo" reset -q --hard HEAD
printf '%s=%s\n' 'DB_PASSWORD' 'abc!def@ghi#jklmnopqrst' > "$secret_repo/special-secret.txt"
git -C "$secret_repo" add special-secret.txt
if (cd "$secret_repo" && bash "$secret_scan") >/dev/null 2>&1; then
  printf 'expected special-character secret to be blocked\n' >&2
  exit 1
fi

git -C "$secret_repo" reset -q --hard HEAD
{
  printf '%s=%s\n' 'JWT_SECRET' 'your-jwt-secret-minimum-256-bits-long-string-here'
  printf '%s=%s\n' 'DB_PASSWORD' 'test-password-for-in-memory-database'
  printf '%s=%s\n' 'API_TOKEN' '${API_TOKEN}'
} > "$secret_repo/placeholders.txt"
git -C "$secret_repo" add placeholders.txt
if ! (cd "$secret_repo" && bash "$secret_scan") >/dev/null 2>&1; then
  printf 'expected placeholders to be allowed\n' >&2
  exit 1
fi

verify_output="$(VERIFY_DRY_RUN=1 HARNESS_CHANGED_FILES='.agents/tests/harness_test.sh' bash "$verify")"
printf '%s\n' "$verify_output" | grep -q '하네스 테스트'
printf '%s\n' "$verify_output" | grep -q 'Shell 문법'

adapter_root="$(mktemp -d)"
mkdir -p "$adapter_root/.agents" "$adapter_root/.claude" "$adapter_root/.codex"
cp -R "$repo_root/.agents/skills" "$adapter_root/.agents/skills"
cat > "$adapter_root/.claude/settings.json" <<'JSON'
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": "CMD=$(jq -r '.tool_input.command // \\\"\\\"'); if echo \\\"$CMD\\\" | grep -q 'git commit'; then echo '[hook] 커밋 전 gradlew test 실행...' >&2; cd \\\"$CLAUDE_PROJECT_DIR/app\\\" && ./gradlew test; fi",
            "timeout": 600
          }
        ]
      },
      {
        "matcher": "CustomTool",
        "hooks": [{"type": "command", "command": "printf custom", "timeout": 3}]
      },
      {
        "matcher": "UserHook",
        "hooks": [{"type": "command", "command": "printf '$CLAUDE_PROJECT_DIR/app'", "timeout": 3}]
      }
    ]
  }
}
JSON
cp "$adapter_root/.claude/settings.json" "$adapter_root/.codex/hooks.json"
mkdir -p "$adapter_root/.claude/skills/implement" "$adapter_root/.codex/skills/implement"
printf '%s\n' 'user-owned-skill' > "$adapter_root/.claude/skills/implement/SKILL.md"
printf '%s\n' 'user-owned-skill' > "$adapter_root/.codex/skills/implement/SKILL.md"
printf '%s\n' 'keep-me' > "$adapter_root/.claude/skills/implement/notes.txt"
HARNESS_PROJECT_ROOT="$adapter_root" python3 "$repo_root/.agents/hooks/install-local-adapters.py" >/dev/null
HARNESS_PROJECT_ROOT="$adapter_root" python3 "$repo_root/.agents/hooks/install-local-adapters.py" >/dev/null
python3 - "$adapter_root" <<'PY'
import json
import sys
from pathlib import Path

root = Path(sys.argv[1])
for relative in (Path('.claude/settings.json'), Path('.codex/hooks.json')):
    data = json.loads((root / relative).read_text())
    commands = [hook['command'] for entry in data['hooks']['PreToolUse'] for hook in entry['hooks']]
    assert sum('.agents/hooks/guard-command.sh' in command for command in commands) == 1
    assert all('커밋 전 gradlew test 실행' not in command for command in commands)
    assert all("grep -q 'git commit'" not in command for command in commands)
    assert 'printf custom' in commands
    assert "printf '$CLAUDE_PROJECT_DIR/app'" in commands

claude_skills = root / '.claude' / 'skills'
for name in ('issue', 'plan', 'pr', 'review', 'specify', 'verify'):
    adapter = (claude_skills / name / 'SKILL.md').read_text()
    assert f'.agents/skills/{name}/SKILL.md' in adapter
    assert 'managed-by: signal-agents-adapter' in adapter

assert (claude_skills / 'implement' / 'SKILL.md').read_text() == 'user-owned-skill\n'
assert (claude_skills / 'implement' / 'notes.txt').read_text() == 'keep-me\n'
assert (root / '.codex' / 'skills' / 'implement' / 'SKILL.md').read_text() == 'user-owned-skill\n'
PY

printf 'harness tests passed\n'
