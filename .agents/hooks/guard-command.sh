#!/usr/bin/env bash
set -euo pipefail

repo_root="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
exec python3 "$repo_root/.agents/hooks/guard_command.py" "${*:-}"
