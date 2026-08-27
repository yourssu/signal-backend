# SIGNAL 에이전트 규칙

## 작업 원칙

- 한국어로 응답한다.
- 요청 범위에 직접 관련된 코드만 수정한다. 범위 밖 리팩토링·포맷·주석 변경은 금지한다.
- 기존 파일은 필요한 부분만 제자리에서 수정한다. 삭제 후 재생성, 파일 전체 재작성, 불필요한 import·코드 재배열로 diff를 키우지 않는다.
- Git에 공유되는 파일은 GitHub Issue에서 시작해 별도 브랜치 또는 worktree에서 수정하고 PR로 전달한다.
- `main`에서 직접 수정하거나 커밋하지 않는다.
- 에이전트는 PR 생성까지만 수행한다. PR merge, auto-merge 설정, merge 버튼 조작은 절대 금지한다.
- 시크릿, 개인정보, 토큰, 비밀번호를 코드·로그·응답에 노출하지 않는다.
- 완료 선언 전에 실제 검증 명령의 성공 증거를 확보한다. 추론과 컴파일만으로 동작을 판정하지 않는다.

## 경량 SDD

작업 시작 시 `.agents/workflows/development.md`를 따라 Small·Medium·Large로 분류한다.

- Small: Issue → Plan → Implement → Verify
- Medium: `.agents/local/specs/issue-<번호>/spec.md`, `plan.md` 작성 → 독립 리뷰 1회
- Large: Medium 산출물 + `tasks.md` 작성 → 독립 구현 병렬화 → 코드·운영 리뷰

스펙은 에이전트가 빠르게 읽는 실행 계약이다. 확정 요구사항, 제외 범위, 수용 조건, 위험, 관련 코드와 미해결 결정만 기록한다.

## 프로젝트 구조

- `app/`: Kotlin 1.9.25, Spring Boot 3.4.1, JPA, Kotest
- `observer/`: Python observer·admin·배포 스크립트
- Kotlin 도메인은 `api → business → implement → storage` 책임을 지키고 생성자 주입을 사용한다.
- 연락처 등 민감 정보는 저장 시 암호화하고 로그에 평문을 남기지 않는다.
- Signal 프론트엔드 저장소는 읽기 전용이다. 명시적 요청 없이 수정하지 않는다.

## 검증

`.agents/project.yml`과 `.agents/skills/verify/SKILL.md`를 기준으로 변경 범위에 맞는 검증을 실행한다.

- Kotlin 변경: `cd app && ./gradlew test`
- Python 변경: `python3 -m unittest discover -s observer/tests -p 'test_*.py'`
- Shell 변경: 변경한 `.sh`마다 `bash -n`
- API 변경: 애플리케이션을 실제로 기동하고 HTTP 요청·응답 확인
- UI 흐름 변경: 실제 브라우저 또는 저장된 Playwright 테스트 실행

실행할 수 없는 검증은 통과로 간주하지 않고 PR 전에 사용자에게 명시한다.

## 하네스 원본

- `.agents/`: Git에 공유되는 유일한 하네스 원본
- `.claude/`, `.codex/`: 개인 환경의 로컬 어댑터이며 Git에 올리지 않는다.
- `.agents/local/`: handoff, resume, 실행 로그 등 로컬 상태이며 Git에 올리지 않는다.

외부 도구의 읽기·쓰기 권한과 선택 기준은 `.agents/workflows/tools.md`를 따른다.
