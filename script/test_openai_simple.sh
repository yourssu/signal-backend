#!/bin/bash

# 간단한 OpenAI API 테스트 (환경변수 직접 입력)

# 여기에 실제 값들을 입력하세요
API_KEY="secret-key"
URL="https://api.openai.com/v1/responses"
MODEL="gpt-5-mini"

# 테스트 요청
curl -X POST "$URL" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $API_KEY" \
  -d '{
    "model": "'$MODEL'",
    "reasoning": {
      "effort": "low"
    },
    "input": [
      {
        "role": "developer",
        "content": "다음 프로필 정보가 부적절한 내용을 포함하는지 검사해주세요. 응답은 반드시 다음 형식으로만 답변하세요: VIOLATION: true/false REASON: (위반시에만) 위반 사유를 간단히 설명"
      },
      {
        "role": "user",
        "content": "학과: 컴퓨터학부\n연락처: @test_user\n닉네임: 테스트유저\n자기소개: 안녕하세요 반갑습니다 sex"
      }
    ]
  }'
