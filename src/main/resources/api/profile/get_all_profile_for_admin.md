# getAllProfilesForAdmin (GET /api/profiles)

## Request

### Request Parameters

| Name        | Type   | Required | Constraint |
|-------------|--------|----------|------------|
| `secretKey` | string | true     | @NotBlank  |

## Reply

### Response Body

| Name             | Type     | Description                          |
|------------------|----------|--------------------------------------|
| `profileId`      | integer  | The unique identifier of the profile |
| `gender`         | string   | Gender of the profile owner          |
| `animal`         | string   | Animal representing the profile      |
| `mbti`           | string   | MBTI personality type                |
| `nickname`       | string   | Profile's nickname                   |
| `introSentences` | string[] | Empty List                           |

### 200 OK

```json
{
  "timestamp": "2025-04-27T14:40:54.486393+09:00",
  "result": [
    {
      "profileId": 1,
      "gender": "FEMALE",
      "animal": "DOG",
      "mbti": "ISTJ",
      "nickname": "leopold",
      "introSentences": []
    },
    {
      "profileId": 2,
      "gender": "FEMALE",
      "animal": "DOG",
      "mbti": "ISTJ",
      "nickname": "leopold",
      "introSentences": []
    },
    {
      "profileId": 3,
      "gender": "FEMALE",
      "animal": "DOG",
      "mbti": "ISTJ",
      "nickname": "leopold",
      "introSentences": []
    }
  ]
}
```

### 403 Forbidden

- invalid secretKey

````json
{
  "timestamp": "2025-04-27T14:41:27.810708+09:00",
  "status": 403,
  "message": "올바르지 않은 관리자 기능 접근 토큰입니다."
}
````
