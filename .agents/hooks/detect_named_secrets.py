#!/usr/bin/env python3
import re
import sys


assignment = re.compile(
    r'''(?ix)
    ["']?
    [a-z0-9_]*(?:password|passwd|secret|token|private_key|access_key)[a-z0-9_]*
    ["']?
    \s*[:=]\s*
    (?:"(?P<double>[^"\r\n]*)"|'(?P<single>[^'\r\n]*)'|(?P<plain>[^\s,}\r\n]+))
    ''',
)
placeholder_prefixes = (
    "changeme",
    "dummy",
    "example",
    "not-a-real",
    "replace-me",
    "test",
    "todo",
    "your-",
)
placeholder_values = {"password", "passwordpassword", "secret", "token", "xxx"}


def is_secret(value: str) -> bool:
    normalized = value.strip()
    lowered = normalized.lower()
    if len(normalized) < 12:
        return False
    if any(marker in normalized for marker in ("${", "{{", "<", ">")):
        return False
    if lowered in placeholder_values or lowered.startswith(placeholder_prefixes):
        return False
    return True


for line in sys.stdin:
    for match in assignment.finditer(line):
        value = next((item for item in match.group("double", "single", "plain") if item is not None), "")
        if is_secret(value):
            raise SystemExit(0)

raise SystemExit(1)
