---
name: pr
description: signal-backend 변경을 검증하고 사용자 컨벤션에 맞춰 커밋·push·PR 생성까지만 수행한다. PR merge에는 사용하지 않는다.
---

# PR 생성

사용자가 커밋·push·PR을 요청했을 때만 사용한다.

## 불변 규칙

- 연결된 GitHub Issue가 있어야 한다.
- base는 `main`이며 `main`에서 직접 작업하지 않는다.
- `.agents/hooks/set-issue.sh <번호> [번호...]`로 현재 Issue를 모두 연결한다.
- `.agents/hooks/verify-before-pr.sh`와 필요한 실제 API·브라우저 검증을 통과해야 한다.
- 시크릿과 범위 밖 변경이 diff에 없어야 한다.
- PR 생성 후 멈춘다. `gh pr merge`, auto-merge 설정, merge 버튼 조작을 절대 수행하지 않는다.
- raw `git push`와 `gh pr create` 대신 `.agents/hooks/create-pr.sh`를 사용한다.
- PR 본문에 연결된 모든 Issue의 `Closes #번호`를 한 줄씩 넣는다.

## 명명

- 브랜치: `{type}/{kebab-요약}`
- 커밋: `type: 한글 요약`
- PR: `[#이슈번호]; type: 제목`

## 본문

```markdown
Closes #이슈번호

## 요약

## 특이사항
```

`## 요약`에는 변경된 기능과 운영 동작을 영역별 `###`와 중첩 bullet로 빠짐없이 정리한다. 파일 목록, 구현 과정, 테스트 상세는 넣지 않는다.

`## 특이사항`은 사용자가 직접 작성한다. 에이전트는 제목 아래에 문장, 불릿, 공백용 `-`를 넣지 않고 항상 비워둔다.
