#!/usr/bin/env python3
import re
import shlex
import subprocess
import sys
from pathlib import Path


def deny(message: str) -> None:
    print(f"[harness] blocked: {message}", file=sys.stderr)
    raise SystemExit(2)


def segments(command: str) -> list[list[str]]:
    lexer = shlex.shlex(command, posix=True, punctuation_chars=";&|")
    lexer.whitespace_split = True
    lexer.commenters = ""
    result, current = [], []
    for token in lexer:
        if token and all(char in ";&|" for char in token):
            if current:
                result.append(current)
                current = []
        else:
            current.append(token)
    if current:
        result.append(current)
    return result


def normalize(tokens: list[str]) -> list[str]:
    assignment = re.compile(r"^[A-Za-z_][A-Za-z0-9_]*=")
    while tokens:
        while tokens and assignment.match(tokens[0]):
            tokens = tokens[1:]
        if not tokens:
            break
        wrapper = Path(tokens[0]).name
        if wrapper in {"command", "builtin", "nohup"}:
            tokens = tokens[1:]
            continue
        if wrapper in {"sudo", "env", "nice", "time"}:
            tokens = tokens[1:]
            while tokens and (tokens[0].startswith("-") or assignment.match(tokens[0])):
                option = tokens[0]
                tokens = tokens[1:]
                if option in {"-u", "-g", "-n"} and tokens:
                    tokens = tokens[1:]
            continue
        break
    return tokens


def git_context(tokens: list[str]) -> tuple[Path, str]:
    target = Path.cwd()
    index = 1
    while index < len(tokens):
        token = tokens[index]
        if token == "-C" and index + 1 < len(tokens):
            candidate = Path(tokens[index + 1])
            target = candidate if candidate.is_absolute() else target / candidate
            index += 2
            continue
        if token in {"-c", "--config-env"} and index + 1 < len(tokens):
            index += 2
            continue
        if token.startswith(("--git-dir", "--work-tree")):
            return target, "unsupported-context"
        if token.startswith("-"):
            index += 1
            continue
        return target, token
    return target, ""


def inspect(command: str) -> None:
    for raw in segments(command):
        for index, token in enumerate(raw):
            if Path(token).name != "env":
                continue
            prefix = raw[:index]
            if any(Path(item).name not in {"command", "builtin", "sudo"} and not re.match(r"^[A-Za-z_][A-Za-z0-9_]*=", item) for item in prefix):
                continue
            remaining = raw[index + 1:]
            cursor = 0
            while cursor < len(remaining):
                item = remaining[cursor]
                if re.match(r"^[A-Za-z_][A-Za-z0-9_]*=", item) or item in {"-0", "--null", "-i", "--ignore-environment"}:
                    cursor += 1
                    continue
                if item in {"-u", "--unset", "-C", "--chdir", "-S", "--split-string"}:
                    cursor += 2
                    continue
                break
            if cursor >= len(remaining):
                deny("전체 환경변수 출력은 허용하지 않습니다.")
        tokens = normalize(raw)
        if not tokens:
            continue
        executable = Path(tokens[0]).name
        if executable in {"bash", "sh", "zsh"}:
            command_index = next((index for index, token in enumerate(tokens[1:], 1) if token.startswith("-") and "c" in token[1:]), None)
            if command_index is not None and command_index + 1 < len(tokens):
                inspect(tokens[command_index + 1])
            continue
        if executable == "gh" and "pr" in tokens:
            pr_index = tokens.index("pr")
            pr_tokens = tokens[pr_index + 1:]
            if "merge" in pr_tokens:
                deny("PR merge는 사용자만 수행합니다.")
            if "create" in pr_tokens:
                deny("PR 생성은 .agents/hooks/create-pr.sh를 사용합니다.")
        if executable == "gh" and "api" in tokens:
            api_text = " ".join(tokens[tokens.index("api") + 1:])
            lowered = api_text.lower()
            forbidden_operations = ("mergepullrequest", "enablepullrequestautomerge", "createpullrequest")
            if any(operation in lowered for operation in forbidden_operations):
                deny("PR 생성·merge·auto-merge API는 사용할 수 없습니다.")
            explicit_get = any(token.upper() == "GET" for token in tokens)
            implicit_post = any(token in {"-f", "--raw-field", "-F", "--field", "--input"} or token.startswith(("--raw-field=", "--field=", "--input=")) for token in tokens)
            mutation_method = any(token.upper() == "POST" for token in tokens) or implicit_post and not explicit_get
            if "/pulls/" in lowered and "/merge" in lowered or mutation_method and ("/pulls" in lowered or "/merges" in lowered):
                deny("PR 생성·merge API는 사용할 수 없습니다.")
        if executable == "git":
            target, subcommand = git_context(tokens)
            if any(token.startswith("alias.") for token in tokens[1:]):
                deny("Git alias를 명령에서 정의할 수 없습니다.")
            if subcommand and subcommand not in {"push", "add", "commit", "switch", "checkout", "unsupported-context"}:
                alias = subprocess.run(
                    ["git", "-C", str(target), "config", "--get", f"alias.{subcommand}"],
                    capture_output=True, text=True, check=False,
                ).stdout.strip()
                if alias:
                    inspect(f"git {alias} {' '.join(tokens[tokens.index(subcommand) + 1:])}")
            if subcommand == "push" or "push" in tokens[1:]:
                deny("push는 .agents/hooks/create-pr.sh에서 검증 후 수행합니다.")
            if subcommand in {"switch", "checkout"} and "main" in tokens[1:]:
                deny("에이전트는 main으로 전환하지 않습니다.")
            writes = subcommand in {"add", "commit"}
            if subcommand == "unsupported-context" and any(item in tokens[1:] for item in {"add", "commit", "push"}):
                deny("명시적 git-dir/work-tree 쓰기는 허용하지 않습니다.")
            branch = subprocess.run(
                ["git", "-C", str(target), "branch", "--show-current"], capture_output=True, text=True, check=False
            ).stdout.strip()
            if branch == "main" and writes:
                deny("main 브랜치에서 Git 공유 변경을 기록할 수 없습니다.")
            if writes:
                root = subprocess.run(["git", "-C", str(target), "rev-parse", "--show-toplevel"], capture_output=True, text=True, check=False).stdout.strip()
                issue_file = Path(root) / ".agents/local/current-issues"
                lines = issue_file.read_text().splitlines() if issue_file.is_file() else []
                if not lines or not all(line.isdigit() for line in lines):
                    deny("연결 Issue가 없습니다. .agents/hooks/set-issue.sh를 먼저 실행하세요.")
        readers = {"cat", "sed", "head", "tail", "less", "more", "awk", "python", "python3"}
        if executable in readers and any(Path(token).name == ".env" or Path(token).name.startswith(".env.") for token in tokens[1:]):
            deny(".env 원문 출력은 허용하지 않습니다.")
        if executable in {"env", "printenv"}:
            deny("전체 환경변수 출력은 허용하지 않습니다.")


if len(sys.argv) > 1 and sys.argv[1]:
    try:
        inspect(sys.argv[1])
    except ValueError as error:
        deny(f"명령을 안전하게 해석할 수 없습니다: {error}")
