# issueTicketByDepositName (POST /api/viewers/deposit)

## Request

### Request Body

| Name               | Type    | Required | Constraint                   |
|--------------------|---------|----------|------------------------------|
| `message`          | string  | true     | @NotBlank && @Size(max = 20) |
| `verificationCode` | integer | true     | @NumberFormat                |

## Reply

### Response Body

| Name          | Type    | Description                                        |
|---------------|---------|----------------------------------------------------|
| `id`          | integer | The unique identifier of the created viewer        |
| `uuid`        | string  | The Unique identifier string of the created viewer |
| `ticket`      | integer | The number of total tickets                        |
| `usedTicket`  | integer | The number of used tickets                         |
| `updatedTime` | String  | latest issued tickets                              |

### 200 OK

```json
{
  "timestamp": "2025-04-27T16:17:07.483828+09:00",
  "result": {
    "id": 1,
    "uuid": "ec5ea893-8540-42c5-958a-ce69ed7f9f63",
    "ticket": 3,
    "usedTicket": 0,
    "updatedTime": "2025-04-27T16:17:07.473236+09:00[Asia/Seoul]"
  }
}
```
