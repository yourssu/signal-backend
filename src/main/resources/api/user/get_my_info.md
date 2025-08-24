# getMyInfo (GET /api/users/me)

## Description
Retrieves the authenticated user's UUID from the JWT token.

## Authentication
Requires Bearer token authentication.

## Request
No parameters required. User's UUID is extracted from the authentication token.

## Response

### Response Body

| Name   | Type   | Description                                |
|--------|--------|-------------------------------------------|
| `uuid` | string | The authenticated user's UUID             |

### 200 OK

```json
{
  "timestamp": "2025-04-27T12:16:55.043588+09:00",
  "result": {
    "uuid": "ec5ea893-8540-42c5-958a-ce69ed7f9f63"
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

### 403 Forbidden

When the token is invalid or expired.

```json
{
  "timestamp": "2025-04-27T16:04:52.613191+09:00",
  "status": 403,
  "message": "Invalid or expired token"
}
```