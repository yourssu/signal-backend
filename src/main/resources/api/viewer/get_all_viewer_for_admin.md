# getAllViewersForAdmin (GET /api/viewers)

## Request

### Query Parameters

| Name        | Type   | Required | Constraint |
|-------------|--------|----------|------------|
| `secretKey` | string | true     | @NotBlank  |

## Reply

### Response Body

| Name          | Type    | Description                                |
|---------------|---------|--------------------------------------------|
| `id`          | integer | The unique identifier of the viewer        |
| `uuid`        | string  | The Unique identifier string of the viewer |
| `gender`      | string  | Gender of the viewer owner                 |
| `ticket`      | integer | The number of total tickets                |
| `usedTicket`  | integer | The number of used tickets                 |
| `updatedTime` | String  | latest issued tickets                      |

### 200 OK

```json
{
  "timestamp": "2025-04-27T16:39:11.616139+09:00",
  "result": [
    {
      "id": 1,
      "uuid": "ec5ea893-8540-42c5-958a-ce69ed7f9f63",
      "gender": "MALE",
      "ticket": 6,
      "usedTicket": 0,
      "updatedTime": "2025-04-27T16:38:42.432065+09:00"
    },
    {
      "id": 2,
      "uuid": "ec5ea893-8540-42c5-958a-ab69ed7f9f63",
      "gender": "MALE",
      "ticket": 6,
      "usedTicket": 0,
      "updatedTime": "2025-04-27T16:38:42.432065+09:00"
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
