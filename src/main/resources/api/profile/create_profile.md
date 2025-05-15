# createProfile (POST /api/profiles)

## Request

### Request Body

| Name             | Type     | Required | Constraint                                                                       |
|------------------|----------|----------|----------------------------------------------------------------------------------|
| `gender`         | string   | true     | only "MALE" or "FEMALE"                                                          |
| `department`     | string   | Department of the profile owner                           |
| `birthYear`      | integer  | Birth year of the profile owner                           |
| `animal`         | string   | true     | @NotBlank                                                                        |
| `contact`        | string   | true     | only "^010\\d{8}\$" or "^@[a-zA-Z0-9._]{1,30}\$"                                 |
| `mbti`           | string   | true     | @NotBlank                                                                        |
| `nickname`       | string   | true     | @Size(min = 1, max = 15)                                                         |
| `introSentences` | string[] | true     | @Size(min = 0, max = 3)List<@Size(min = 0, max = 20) String>                     |
| `uuid`           | string   | false    | validate the gender of a viewer. if null, then generate a new uuid for a profile |

## Reply

### Response Body

| Name             | Type     | Description                                               |
|------------------|----------|-----------------------------------------------------------|
| `profileId`      | integer  | The unique identifier of the created profile              |
| `uuid`           | string   | Generated Unique identifier string of the created profile |
| `gender`         | string   | Gender of the profile owner                               |
| `department`     | string   | Department of the profile owner                           |
| `birthYear`      | integer  | Birth year of the profile owner                           |
| `animal`         | string   | Animal representing the profile                           |
| `contact`        | string   | Contact information                                       |
| `mbti`           | string   | MBTI personality type                                     |
| `nickname`       | string   | Profile's nickname                                        |
| `introSentences` | string[] | List of introduction sentences                            |

### 201 Created

```json
{
  "timestamp": "2025-04-27T12:16:55.043588+09:00",
  "result": {
    "profileId": 1,
    "uuid": "26eaadba-a2a0-4de3-ae7c-ea98cd14a014",
    "gender": "FEMALE",
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

- the gender is blank
- the animal is blank
- the MBTI value is blank

```json
{
  "timestamp": "2025-04-27T12:17:31.904463+09:00",
  "status": 400,
  "message": "Invalid Input: [must not be blank]"
}
```

- the gender is an invalid format

```json
{
  "timestamp": "2025-04-27T12:23:21.976081+09:00",
  "status": 400,
  "message": "해당하는 성별을 찾을 수 없습니다."
}
```

- the contact is an invalid format

```json
{
  "timestamp": "2025-04-27T12:18:09.180091+09:00",
  "status": 400,
  "message": "Invalid Input: [연락처는 인스타그램 아이디 또는 010xxxxxxxx 형식이어야 합니다.]"
}
```

- the nickname exceeds the maximum length

```json
{
  "timestamp": "2025-04-27T12:25:02.94318+09:00",
  "status": 400,
  "message": "Invalid Input: [size must be between 1 and 15]"
}
```

- any introSentences exceed the maximum length

```json
{
  "timestamp": "2025-04-27T12:27:22.975561+09:00",
  "status": 400,
  "message": "잘못된 요청입니다."
}
```

### 409 Conflict

- the gender is not match with existing viewer

```json
{
  "timestamp": "2025-04-27T16:04:52.613191+09:00",
  "status": 409,
  "message": "성별이 일치하지 않습니다."
}
```
