# getProfileRanking (GET /api/profiles/ranking)

## Request

### Query Parameters

| Name   | Type   | Required | Constraint | Description                  |
|--------|--------|----------|------------|------------------------------|
| `uuid` | string | true     | @NotBlank  | UUID of the profile to rank |

## Reply

### Response Body

| Name             | Type    | Description                          |
|------------------|---------|--------------------------------------|
| `uuid`           | string  | The unique identifier of the profile |
| `ranking`        | integer | The ranking of the profile           |
| `totalProfile`   | integer | The total number of profiles         |
| `purchaseCount`  | integer | The number of purchases              |

### 200 OK

```json
{
  "timestamp": "2025-08-31T12:00:00.000000+09:00",
  "result": {
    "uuid": "ec5ea893-8540-42c5-958a-ce69ed7f9f63",
    "ranking": 1,
    "totalProfile": 100,
    "purchaseCount": 10
  }
}
```

### 404 Not Found

- The profile with the given UUID does not exist.

```json
{
  "timestamp": "2025-08-31T12:00:00.000000+09:00",
  "status": 404,
  "message": "해당하는 프로필을 찾을 수 없습니다."
}
```
