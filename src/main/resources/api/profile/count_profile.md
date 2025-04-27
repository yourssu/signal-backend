# countAllProfiles (GET /api/profiles/count)

## Request

None

## Reply

### Response Body

| Name    | Type    | Description                |
|---------|---------|----------------------------|
| `count` | integer | the number of all profiles |

### 200 OK

```json
{
  "timestamp": "2025-04-27T14:25:36.546553+09:00",
  "result": {
    "count": 0
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
