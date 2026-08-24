#!/usr/bin/env python3
import json
import os
import shutil
from pathlib import Path


def load_json(path: Path) -> dict:
    if not path.exists():
        return {}
    with path.open(encoding="utf-8") as file:
        return json.load(file)


LEGACY_MARKERS = ("CLAUDE_PROJECT_DIR/app", "커밋 전 gradlew test 실행", "./gradlew test")
MANAGED_MARKER = "managed-by: signal-agents-adapter"


def install_hook(path: Path) -> None:
    config = load_json(path)
    hooks = config.setdefault("hooks", {}).setdefault("PreToolUse", [])
    def is_legacy_hook(command: str) -> bool:
        legacy_script = command.strip().endswith((".codex/hooks/pre-commit-test.sh'", '.codex/hooks/pre-commit-test.sh"'))
        return all(marker in command for marker in LEGACY_MARKERS) or legacy_script

    cleaned = []
    for entry in hooks:
        entry_hooks = [
            hook
            for hook in entry.get("hooks", [])
            if not is_legacy_hook(hook.get("command", ""))
        ]
        if entry_hooks:
            entry["hooks"] = entry_hooks
            cleaned.append(entry)
    hooks[:] = cleaned
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


def skill_metadata(path: Path) -> tuple[str, str]:
    lines = path.read_text(encoding="utf-8").splitlines()
    name = path.parent.name
    description = f"프로젝트 .agents 원본의 {name} 스킬을 사용한다."
    for line in lines[1:]:
        if line == "---":
            break
        if line.startswith("name:"):
            name = line.split(":", 1)[1].strip()
        if line.startswith("description:"):
            description = line.split(":", 1)[1].strip()
    return name, description


def install_skill_adapters(project_root: Path) -> None:
    source_root = project_root / ".agents" / "skills"
    claude_root = project_root / ".claude" / "skills"
    codex_root = project_root / ".codex" / "skills"
    for source in sorted(source_root.glob("*/SKILL.md")):
        name, description = skill_metadata(source)
        relative_source = f".agents/skills/{source.parent.name}/SKILL.md"
        claude_adapter = claude_root / source.parent.name
        codex_adapter = codex_root / source.parent.name
        claude_skill = claude_adapter / "SKILL.md"
        codex_skill = codex_adapter / "SKILL.md"

        def managed(path: Path) -> bool:
            if not path.is_file():
                return False
            content = path.read_text(encoding="utf-8")
            return MANAGED_MARKER in content

        if codex_adapter.exists() and managed(codex_skill):
            shutil.rmtree(codex_adapter)
        if claude_adapter.exists() and not managed(claude_skill):
            print(f"[harness] 기존 Claude 스킬 보존: {claude_skill}")
            continue
        shutil.rmtree(claude_adapter, ignore_errors=True)
        claude_adapter.mkdir(parents=True, exist_ok=True)
        claude_skill.write_text(
            "---\n"
            f"name: {name}\n"
            f"description: {description}\n"
            "---\n\n"
            f"<!-- {MANAGED_MARKER} -->\n"
            f"이 스킬을 실행하기 전에 프로젝트 루트의 `{relative_source}`를 전체 읽고 그대로 따른다.\n",
            encoding="utf-8",
        )


project_root = Path(os.environ.get("HARNESS_PROJECT_ROOT", Path.cwd())).resolve()
install_hook(project_root / ".claude" / "settings.json")
install_hook(project_root / ".codex" / "hooks.json")
install_skill_adapters(project_root)
print("[harness] Claude·Codex 로컬 안전 훅 설치 완료")
