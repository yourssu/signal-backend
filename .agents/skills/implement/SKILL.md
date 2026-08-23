---
name: implement
description: 승인된 Issue와 실행 스펙을 기준으로 signal-backend 기능·버그를 최소 수정하고 테스트 우선으로 구현한다.
---

# 구현

## 시작 조건

- Git 공유 변경이면 연결된 Issue가 있어야 한다.
- `.agents/workflows/development.md`에 따라 작업 규모를 분류한다.
- Medium·Large는 승인된 `.agents/local/specs/issue-<번호>`를 먼저 읽는다.
- 별도 브랜치 또는 worktree에서만 수정한다.

## 원칙

- 요청 범위에 필요한 최소 코드만 수정한다.
- 기존 패턴을 우선하며 요청 없는 리팩토링·포맷·주석 변경을 하지 않는다.
- 테스트를 먼저 추가하거나 실패를 재현한 뒤 최소 구현한다.
- 구현자는 PR을 merge하지 않는다.

## Kotlin DDD

- `api`: Controller와 API DTO만 둔다.
- `business`: 애플리케이션 서비스와 command·response를 둔다.
- `implement`: 도메인 모델, reader·writer·validator와 예외를 둔다.
- `storage`: JPA Entity와 repository 구현을 둔다.
- 생성자 주입과 `@ConfigurationProperties`를 사용한다.
- 민감 정보는 암호화하고 로그에 평문을 남기지 않는다.

## 완료

`.agents/skills/verify/SKILL.md`로 실제 검증한 뒤 diff에 범위 밖 변경이 없는지 확인한다. 검증 실패나 미실행을 숨기지 않는다.
