# getRandomProfile (GET /api/profiles/random)

## Request

### Query Parameters

| Name              | Type      | Required | Constraint | Description                  |
|-------------------|-----------|----------|------------|------------------------------|
| `uuid`            | string    | true     | @NotBlank  | Viewer's uuid                |
| `gender`          | string    | true     | @NotBlank  | Gender of the random profile | 
| `excludeProfiles` | integer[] | false    |            | excluded Ids                 |

## Reply

### Response Body

| Name             | Type     | Description                                 |
|------------------|----------|---------------------------------------------|
| `profileId`      | integer  | The unique identifier of the random profile |
| `gender`         | string   | Gender of the random profile                |
| `animal`         | string   | Animal representing the profile             |
| `mbti`           | string   | MBTI personality type                       |
| `nickname`       | string   | Profile's nickname                          |
| `introSentences` | string[] | List of introduction sentences              |

### 200 OK

```json
{
  "timestamp": "2025-04-27T14:59:57.30334+09:00",
  "result": {
    "profileId": 1,
    "gender": "FEMALE",
    "animal": "DOG",
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

- the viewer does not exist

```json
{
  "timestamp": "2025-04-27T14:48:50.917092+09:00",
  "status": 404,
  "message": "해당하는 Viewer를 찾을 수 없습니다."
}
```

- the viewer could not find a random profile

```json
{
  "timestamp": "2025-04-27T14:59:57.30334+09:00",
  "status": 404,
  "message": "조회할 수 있는 랜덤 프로필이 없습니다."
}
```
