---
name: specify
description: GitHub Issue를 에이전트가 실행 가능한 간결한 spec.md 계약으로 정제한다. Medium·Large 작업에 사용한다.
---

# 스펙 작성

GitHub Issue를 공용 요구사항 원본으로 유지하고 `.agents/local/specs/issue-<번호>/spec.md`에는 구현 판단에 필요한 확정 내용만 쓴다. 로컬 spec은 Git에 커밋하지 않는다.

필수 항목:

- Issue URL, 규모, 상태
- 확정 요구사항
- 제외 범위
- 관찰 가능한 수용 조건
- 회귀·보안·운영 위험
- 미해결 결정이 있으면 승인 전 명시

Issue 본문, 회의 기록, 긴 배경 설명을 복제하지 않는다. 구현 방법은 `plan.md`에 둔다. 스펙을 작성한 뒤 사용자 승인을 받기 전 구현하지 않는다.
