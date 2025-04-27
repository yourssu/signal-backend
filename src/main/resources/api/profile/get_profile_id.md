# getPurchasedProfile (GET /api/profiles/{profileId})

## Request

### Path Variables

| Name        | Type    | Required | Constraint |
|-------------|---------|----------|------------|
| `profileId` | integer | true     | @NotNull   |

### Query Parameters

| Name   | Type   | Required | Constraint |
|--------|--------|----------|------------|
| `uuid` | string | true     | @NotBlank  |

## Reply

### Response Body

| Name             | Type     | Description                                       |
|------------------|----------|---------------------------------------------------|
| `profileId`      | integer  | The unique identifier of the profile              |
| `gender`         | string   | Gender of the profile owner                       |
| `animal`         | string   | Animal representing the profile                   |
| `contact`        | string   | Contact information                               |
| `mbti`           | string   | MBTI personality type                             |
| `nickname`       | string   | Profile's nickname                                |
| `introSentences` | string[] | List of introduction sentences                    |

### 200 OK

```json
{
  "timestamp": "2025-04-27T14:47:59.730735+09:00",
  "result": {
    "profileId": 1,
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

### 403 Forbidden

- the viewer does not buy the profile for a profileId

```json
{
"timestamp": "2025-04-27T14:49:59.579037+09:00",
"status": 403,
"message": "No purchased profile was found"
}
```

### 404 Not Found

- the profileId does not exist in all profiles

```json
{
  "timestamp": "2025-04-27T14:19:15.75724+09:00",
  "status": 404,
  "message": "해당하는 프로필을 찾을 수 없습니다."
}
```

- the viewer's uuid does not exist

```json
{
  "timestamp": "2025-04-27T14:48:50.917092+09:00",
  "status": 404,
  "message": "Viewer not found"
}
