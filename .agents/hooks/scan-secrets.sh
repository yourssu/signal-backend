#!/usr/bin/env bash
set -euo pipefail

repo_root="$(git rev-parse --show-toplevel)"
cd "$repo_root"

tracked_local_files="$({ git ls-files; git diff --cached --name-only --diff-filter=ACMR; } | sort -u | grep -E '(^|/)(\.env($|\.)|[^/]+\.(pem|p12|pfx|key)$)|^\.(claude|codex)/|^\.agents/local/' || true)"
tracked_local_files="$(printf '%s\n' "$tracked_local_files" | grep -Ev '(^|/)\.env\.example$' || true)"
if [[ -n "$tracked_local_files" ]]; then
  printf '[secret-scan] 로컬·시크릿 파일이 Git 추적 대상입니다:\n%s\n' "$tracked_local_files" >&2
  exit 4
fi

base_ref="${SECRET_SCAN_BASE_REF:-origin/main}"
if ! git rev-parse --verify "$base_ref" >/dev/null 2>&1; then
  base_ref="main"
fi
added_lines="$({
  git diff "$base_ref"...HEAD --unified=0 --no-color --diff-filter=ACMR
  git diff --cached --unified=0 --no-color --diff-filter=ACMR
} | sed -n '/^+++ /d; /^+/s/^+//p')"
if printf '%s\n' "$added_lines" | grep -Eiq '(-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----|sk-(proj-)?[A-Za-z0-9_-]{20,}|xox[baprs]-[A-Za-z0-9-]{10,}|gh[pousr]_[A-Za-z0-9]{20,}|AKIA[0-9A-Z]{16})'; then
  printf '[secret-scan] staged diff에서 시크릿 형태 문자열을 발견했습니다.\n' >&2
  exit 4
fi

printf '[secret-scan] 통과\n'
