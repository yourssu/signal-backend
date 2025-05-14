# issueTicketByBankDepositSms (POST /api/viewers/sms)

- Provide functionality to issue viewer tickets through bank deposit SMS. 
- Once a ticket has been issued, the same verification code cannot be reused.

## Request

### Request Body

| Name        | Type   | Required | Constraint |
|-------------|--------|----------|------------|
| `secretKey` | string | true     | @NotBlank  |
| `message`   | string | true     | @NotBlank  |
| `type`      | string | true     | @NotBlank  |

```json
{
  "message": "[Web발신]\n[카카오뱅크]\n홍길동(3873)\n05\/14 11:30\n입금 {{ticket}}원\n{{verificationCode}}\n잔액 6원",
  "secretKey": "{{secretKey}}",
  "type": "kakao"
}
```

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

### 400 Bad Request

- the ticket is negative or zero

```json
{
  "timestamp": "2025-04-27T16:19:40.677098+09:00",
  "status": 400,
  "message": "Invalid Input: [must be greater than 0]"
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

### 404 Not Found

- the verification code used only once.

```json
{
  "timestamp": "2025-04-27T16:17:48.903316+09:00",
  "status": 404,
  "message": "A verification code not found for uuid"
}
```
