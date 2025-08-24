# approvePayment (POST /api/viewers/payment/kakaopay/approve)

## Description

Completes the KakaoPay payment process after user approval. Issues tickets upon successful payment.

## Authentication

Requires Bearer token authentication. User's UUID is extracted from the authentication token.

## Request

### Request Body

| Name      | Type   | Required | Constraint | Description                            |
|-----------|--------|----------|------------|----------------------------------------|
| `tid`     | string | true     | @NotBlank  | Transaction ID from payment initiation |
| `pgToken` | string | true     | @NotBlank  | Payment approval token from KakaoPay   |

## Response

### Response Body

| Name            | Type    | Description                               |
|-----------------|---------|-------------------------------------------|
| `tid`           | string  | Transaction ID                            |
| `uuid`          | string  | User's UUID                               |
| `ticketCount`   | integer | Number of tickets purchased               |
| `totalAmount`   | integer | Total payment amount                      |
| `approvedAt`    | string  | Timestamp when payment was approved       |
| `ticketsIssued` | integer | Number of tickets issued to the user      |
| `totalTickets`  | integer | Total tickets the user has after purchase |

### 200 OK

```json
{
  "timestamp": "2025-04-27T12:16:55.043588+09:00",
  "result": {
    "tid": "T5e0b3b0f461d47a6a8c2",
    "uuid": "ec5ea893-8540-42c5-958a-ce69ed7f9f63",
    "ticketCount": 3,
    "totalAmount": 4500,
    "approvedAt": "2025-04-27T12:17:30",
    "ticketsIssued": 3,
    "totalTickets": 6
  }
}
```

### 400 Bad Request

- Invalid or missing parameters

```json
{
  "timestamp": "2025-04-27T12:17:31.904463+09:00",
  "status": 400,
  "message": "Invalid Input: [must not be blank]"
}
```

- Payment already processed

```json
{
  "timestamp": "2025-04-27T12:17:31.904463+09:00",
  "status": 400,
  "message": "이미 처리된 결제입니다."
}
```

### 404 Not Found

- Transaction not found

```json
{
  "timestamp": "2025-04-27T14:19:15.75724+09:00",
  "status": 404,
  "message": "해당하는 결제 정보를 찾을 수 없습니다."
}
```

- User not found

```json
{
  "timestamp": "2025-04-27T14:19:15.75724+09:00",
  "status": 404,
  "message": "해당하는 Viewer를 찾을 수 없습니다."
}
```

### 500 Internal Server Error

- KakaoPay API error

```json
{
  "timestamp": "2025-04-27T14:19:15.75724+09:00",
  "status": 500,
  "message": "결제 승인 처리 중 오류가 발생했습니다."
}
```
