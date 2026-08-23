#!/usr/bin/env python3
import json
import os
from pathlib import Path


def load_json(path: Path) -> dict:
    if not path.exists():
        return {}
    with path.open(encoding="utf-8") as file:
        return json.load(file)


def install(path: Path) -> None:
    config = load_json(path)
    hooks = config.setdefault("hooks", {}).setdefault("PreToolUse", [])
    command = (
        "ROOT=$(git rev-parse --show-toplevel) && "
        "CMD=$(jq -r '.tool_input.command // \"\"') && "
        "bash \"$ROOT/.agents/hooks/guard-command.sh\" \"$CMD\""
    )
    entry = {
        "matcher": "Bash",
        "hooks": [{"type": "command", "command": command, "timeout": 10}],
    }
    if not any(item.get("hooks") == entry["hooks"] for item in hooks):
        hooks.append(entry)
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as file:
        json.dump(config, file, ensure_ascii=False, indent=2)
        file.write("\n")


project_root = Path(os.environ.get("HARNESS_PROJECT_ROOT", Path.cwd())).resolve()
install(project_root / ".claude" / "settings.json")
install(project_root / ".codex" / "hooks.json")
print("[harness] Claude·Codex 로컬 안전 훅 설치 완료")
