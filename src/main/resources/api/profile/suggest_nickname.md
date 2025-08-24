# suggestNickname (POST /api/profiles/nickname)

- Responses are cached
- Use OpenAI APIs in development and production environments.
- Local environments should be isolated.

## Authentication
Requires Bearer token authentication.

## Request

### Request Body

| Name             | Type     | Required | Constraint |
|------------------|----------|----------|------------|
| `introSentences` | string[] | true     | @NotNull   |

## Reply

### Response Body

| Name       | Type   | Description        |
|------------|--------|--------------------|
| `nickname` | string | Suggested Nickname |

### 201 Created

```json
{
  "timestamp": "2025-04-27T17:18:00.268908419+09:00",
  "result": {
    "nickname": "딸기포도치킨"
  }
}
```

### 400 Bad Request

- the introSentences is null

```json
{
  "timestamp": "2025-04-27T17:20:18.395153555+09:00",
  "status": 400,
  "message": "잘못된 RequestBody입니다."
}
```
