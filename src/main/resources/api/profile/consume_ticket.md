# consumeTicket (POST /api/profiles/contact)
- If the viewer bought a profile, then tickets don't be consumed

## Authentication
Requires Bearer token authentication.

## Request

### Request Body

| Name        | Type    | Required | Constraint |
|-------------|---------|----------|------------|
| `profileId` | integer | true     | @NotNull   |

## Reply

### Response Body

| Name             | Type     | Description                                               |
|------------------|----------|-----------------------------------------------------------|
| `profileId`      | integer  | The unique identifier of the created profile              |
| `gender`         | string   | Gender of the profile owner                               |
| `department`     | string   | Department of the profile owner                           |
| `birthYear`      | integer  | Birth year of the profile owner                           |
| `animal`         | string   | Animal representing the profile                           |
| `contact`        | string   | Contact information                                       |
| `mbti`           | string   | MBTI personality type                                     |
| `nickname`       | string   | Profile's nickname                                        |
| `introSentences` | string[] | List of introduction sentences                            |

### 200 OK

```json
{
  "timestamp": "2025-04-27T15:21:07.318735+09:00",
  "result": {
    "profileId": 1,
    "gender": "MALE",
    "department": "학과",
    "birthYear": 2000,
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

### 400 Bad Request

- the profileId is blank

```json
{
  "timestamp": "2025-04-27T15:26:17.368726+09:00",
  "status": 400,
  "message": "Invalid Input: [must not be blank]"
}
```

- tickets are insufficient

```json
{
  "timestamp": "2025-04-27T15:24:49.04025+09:00",
  "status": 400,
  "message": "사용할 수 있는 티켓을 초과했습니다."
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
  "message": "해당하는 Viewer를 찾을 수 없습니다."
}
```
