---
name: verify
description: 변경 범위에 맞는 자동 테스트와 실제 API·브라우저 검증을 실행하고 증거가 있을 때만 완료를 판정한다.
---

# 검증

## 자동 검증

PR 전 `.agents/hooks/verify-before-pr.sh`를 실행한다. 스크립트가 선택한 Gradle, Python, Shell 검증 중 하나라도 실패하면 완료·push·PR을 중단한다.

## 실제 동작

- API 계약·Controller 변경: 애플리케이션을 기동하고 정상·실패 HTTP 요청과 응답을 확인한다.
- UI 흐름: Chrome DevTools MCP로 콘솔·네트워크·화면을 진단하고, 반복 회귀는 Playwright 테스트로 저장해 실행한다.
- 외부 서비스: mock 검증과 DEV 실제 검증을 구분한다. 운영 쓰기는 별도 승인을 받는다.
- 배포·Docker: 전환, 기동, healthcheck와 실패 시 이전 서비스 유지 여부를 확인한다.

## 증거

명령, 종료 코드, 테스트 수와 실제 요청·응답의 핵심 결과를 기록한다. 실행할 수 없는 필수 검증은 통과로 표현하지 않고 PR을 만들기 전에 사용자에게 알린다.
