# initiatePayment (POST /api/viewers/payment/kakaopay/initiate)

## Description

Initiates a KakaoPay payment process for purchasing tickets. Returns URLs for payment approval.

## Authentication

Requires Bearer token authentication. User's UUID is extracted from the authentication token.

## Request

### Request Body

| Name       | Type    | Required | Constraint | Description                   |
|------------|---------|----------|------------|-------------------------------|
| `quantity` | integer | true     | @Positive  | Number of tickets to purchase |
| `price`    | integer | true     | @Positive  | Price per ticket              |

## Response

### Response Body

| Name                    | Type   | Description                              |
|-------------------------|--------|------------------------------------------|
| `tid`                   | string | Transaction ID from KakaoPay             |
| `nextRedirectAppUrl`    | string | URL for mobile app payment approval      |
| `nextRedirectMobileUrl` | string | URL for mobile web payment approval      |
| `nextRedirectPcUrl`     | string | URL for PC web payment approval          |
| `createdAt`             | string | Timestamp when the payment was initiated |

### 201 Created

```json
{
  "timestamp": "2025-04-27T12:16:55.043588+09:00",
  "result": {
    "tid": "T5e0b3b0f461d47a6a8c2",
    "nextRedirectAppUrl": "https://online-pay.kakao.com/mockup/v1/...",
    "nextRedirectMobileUrl": "https://online-pay.kakao.com/mockup/v1/...",
    "nextRedirectPcUrl": "https://online-pay.kakao.com/mockup/v1/...",
    "createdAt": "2025-04-27T12:16:55"
  }
}
```

### 404 Not Found

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
  "message": "결제 요청 처리 중 오류가 발생했습니다."
}
```
