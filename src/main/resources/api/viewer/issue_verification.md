# issueVerification (POST /api/viewers/verification)
- If the uuid is not registered, then a new verification code will be created.
- Otherwise, the uuid is registered, then return the verification code for the uuid.

## Request

### Request Body

| Name     | Type   | Required | Constraint                       |
|----------|--------|----------|----------------------------------|
| `uuid`   | string | true     | @NotBlank                        |

## Reply

### Response Body

| Name               | Type    | Description                                         |
|--------------------|---------|-----------------------------------------------------|
| `verificationCode` | integer | Random number generation for UUID limited to 0-9999 |

### 200 OK

```json
{
  "timestamp": "2025-04-27T16:00:59.14832+09:00",
  "result": {
    "verificationCode": 4117
  }
}
```

### 400 Bad Request

- the uuid is blank

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
```json
{
  "timestamp": "2025-04-27T16:06:04.153891+09:00",
  "status": 400,
  "message": "등록되지 않은 Viewer는 성별을 입력해야 합니다."
}
```

### 409 Conflict

- the gender is not match with existing profile

```json
{
  "timestamp": "2025-04-27T16:04:52.613191+09:00",
  "status": 409,
  "message": "성별이 일치하지 않습니다."
}
```
