# issueVerification (POST /api/viewers/verification)
- If the uuid is not registered, then a new verification code will be created.
- Otherwise, the uuid is registered, then return the verification code for the uuid.

## Authentication
Requires Bearer token authentication.

## Request

No request body required. User's UUID is extracted from the authentication token.

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

### 401 Unauthorized

When no valid authentication token is provided.

```json
{
  "timestamp": "2025-04-27T16:04:52.613191+09:00",
  "status": 401,
  "message": "Unauthorized"
}
```
