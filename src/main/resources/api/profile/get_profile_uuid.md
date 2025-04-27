# getProfileUuid (GET /api/profiles/uuid)

## Request

### Request Parameters

| Name   | Type   | Required | Constraint |
|--------|--------|----------|------------|
| `uuid` | string | true     | @NotBlank  |

## Reply

### Response Body

| Name             | Type     | Description                                       |
|------------------|----------|---------------------------------------------------|
| `profileId`      | integer  | The unique identifier of the profile              |
| `uuid`           | string   | Generated Unique identifier string of the profile |
| `gender`         | string   | Gender of the profile owner                       |
| `animal`         | string   | Animal representing the profile                   |
| `contact`        | string   | Contact information                               |
| `mbti`           | string   | MBTI personality type                             |
| `nickname`       | string   | Profile's nickname                                |
| `introSentences` | string[] | List of introduction sentences                    |

### 200 OK

```json
{
  "timestamp": "2025-04-27T14:18:14.994596+09:00",
  "result": {
    "profileId": 2,
    "uuid": "ec5ea893-8540-42c5-958a-ce69ed7f9f63",
    "gender": "FEMALE",
    "animal": "DOG",
    "contact": "@leo",
    "mbti": "ISTJ",
    "nickname": "leopold",
    "introSentences": [
      "나는 딸기를 좋아해",
      "나는 포도도 좋아해",
      "제일 좋아하는 음식은 치킨이야"
    ]
  }
}
```

### 404 Not Found

- the uuid does not exist in all profiles
```json
{
  "timestamp": "2025-04-27T14:19:15.75724+09:00",
  "status": 404,
  "message": "해당하는 프로필을 찾을 수 없습니다."
}
```
