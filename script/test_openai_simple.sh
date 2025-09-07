#!/bin/bash

# Load from .env file
source ../.env

API_KEY="$OPENAI_API_KEY"
URL="$OPENAI_URL"
MODEL="$OPENAI_MODEL"

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
        "content": "'"$OPENAI_POLICY_PROMPT"'"
      },
      {
        "role": "user",
        "content": "학과: 컴퓨터학부\n연락처: @test_user\n닉네임: 테스트유저\n자기소개: 안녕하세요 반갑습니다 sex"
      }
    ]
  }'
