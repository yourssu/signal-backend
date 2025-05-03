# countProfilesByGender (GET /api/profiles/genders/{gender}/count)

## Request

### Path Parameters

| Name     | Type   | Required | Constraint                                                               |
|----------|--------|----------|--------------------------------------------------------------------------|
| `gender` | string | true     | The gender to filter profiles by (eg. male or female). Case-insensitive. |

## Reply

### Response Body

| Name    | Type    | Description                        |
|---------|---------|------------------------------------|
| `count` | integer | the number of profiles by a gender |

### 200 OK

```json
{
  "timestamp": "2025-04-27T14:25:36.546553+09:00",
  "result": {
    "count": 0
  }
}
```
