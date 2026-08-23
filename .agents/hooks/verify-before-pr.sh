#!/usr/bin/env bash
set -euo pipefail

repo_root="$(git rev-parse --show-toplevel)"
cd "$repo_root"

base_ref="${HARNESS_BASE_REF:-origin/main}"
if ! git rev-parse --verify "$base_ref" >/dev/null 2>&1; then
  base_ref="main"
fi

changed_file_list="$(
  {
    git diff --name-only "$base_ref"...HEAD
    git diff --name-only
    git diff --name-only --cached
    git ls-files --others --exclude-standard
  } | awk 'NF' | sort -u
)"

if [[ -z "$changed_file_list" ]]; then
  printf '[verify] 변경 파일 없음\n'
  exit 0
fi

run_secret_scan() {
  printf '[verify] staged secret 검사\n'
  bash .agents/hooks/scan-secrets.sh
}

has_path() {
  printf '%s\n' "$changed_file_list" | grep -Eq "$1"
}

run_step() {
  local label="$1"
  shift
  printf '[verify] %s\n' "$label"
  if [[ "${VERIFY_DRY_RUN:-0}" == "1" ]]; then
    printf '[verify] dry-run: %q' "$1"
    shift
    printf ' %q' "$@"
    printf '\n'
    return 0
  fi
  "$@"
}

run_secret_scan

if has_path '^\.agents/'; then
  run_step '하네스 테스트' bash .agents/tests/harness_test.sh
fi

if has_path '^app/'; then
  run_step 'Gradle 전체 테스트' bash -c 'cd app && ./gradlew test'
fi

if has_path '^observer/'; then
  run_step 'Python 전체 테스트' python3 -m unittest discover -s observer/tests -p 'test_*.py'
fi

shell_files="$(printf '%s\n' "$changed_file_list" | grep -E '\.sh$' || true)"
if [[ -n "$shell_files" ]]; then
  while IFS= read -r shell_file; do
    [[ -f "$shell_file" ]] || continue
    run_step "Shell 문법: $shell_file" bash -n "$shell_file"
  done <<< "$shell_files"
fi

if has_path '^app/src/main/kotlin/com/yourssu/signal/api/'; then
  api_evidence="$repo_root/.agents/local/api-evidence.md"
  current_head="$(git rev-parse HEAD)"
  if [[ ! -s "$api_evidence" ]] || ! grep -Fqx "commit: $current_head" "$api_evidence" || ! grep -Eq '^command: .+' "$api_evidence" || ! grep -Fqx 'result: success' "$api_evidence"; then
    printf '[verify] 실패: 실제 HTTP 정상·실패 응답을 %s에 기록해야 합니다.\n' "$api_evidence" >&2
    exit 3
  fi
  printf '[verify] API 실검증 증거 확인: %s\n' "$api_evidence"
fi

if has_path '^\.github/workflows/'; then
  workflow_files="$(printf '%s\n' "$changed_file_list" | grep -E '^\.github/workflows/.*\.ya?ml$' || true)"
  if [[ -n "$workflow_files" ]]; then
    if command -v actionlint >/dev/null 2>&1; then
      while IFS= read -r workflow_file; do
        run_step "GitHub Actions 검증: $workflow_file" actionlint "$workflow_file"
      done <<< "$workflow_files"
    else
      run_step 'GitHub Actions YAML 문법' ruby -e 'require "yaml"; ARGV.each { |path| YAML.safe_load_file(path, aliases: true) }' $workflow_files
    fi
  fi

  deployment_evidence="$repo_root/.agents/local/deployment-evidence.md"
  current_head="$(git rev-parse HEAD)"
  if [[ ! -s "$deployment_evidence" ]] || ! grep -Fqx "commit: $current_head" "$deployment_evidence" || ! grep -Eq '^command: .+' "$deployment_evidence" || ! grep -Fqx 'result: success' "$deployment_evidence"; then
    printf '[verify] 실패: 배포·롤백 검토 결과를 %s에 기록해야 합니다.\n' "$deployment_evidence" >&2
    exit 3
  fi
  printf '[verify] 배포 검증 증거 확인: %s\n' "$deployment_evidence"
fi

printf '[verify] 자동 검증 완료\n'
