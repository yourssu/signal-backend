# Claude Code adapter

이 저장소의 공용 에이전트 규칙은 `AGENTS.md`가 원본이다. 작업 전에 다음 순서로 읽는다.

1. `AGENTS.md`
2. `.agents/workflows/development.md`
3. 현재 요청에 해당하는 `.agents/skills/<name>/SKILL.md`
4. Medium·Large 작업이면 `.agents/local/specs/issue-<번호>/`

`.claude/`에 공용 규칙이나 skill을 복제하지 않는다. `.claude/`는 개인 로컬 설정과 세션 상태에만 사용하며 Git에 올리지 않는다.
