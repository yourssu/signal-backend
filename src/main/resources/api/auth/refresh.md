# refreshToken (POST /api/auth/refresh)

## Description
Refreshes JWT tokens using a valid refresh token. This endpoint validates the provided refresh token and issues new access and refresh tokens.

## Request

### Request Body

| Name           | Type   | Required | Constraint                                    |
|----------------|--------|----------|-----------------------------------------------|
| `refreshToken` | string | true     | @NotBlank - Valid JWT refresh token issued by the system |

### Request Example
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI3ZjM2YTg5ZC0xMjM0LTQ1NjctODkwMS0yMzQ1Njc4OTBhYmMiLCJpYXQiOjE3MDU5OTgwNDUsImV4cCI6MTcwNjYwMjg0NX0.yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy"
}
```

## Reply

### Response Body

| Name                    | Type    | Description                                          |
|-------------------------|---------|------------------------------------------------------|
| `accessToken`           | string  | New JWT access token for API authentication         |
| `refreshToken`          | string  | New JWT refresh token for obtaining new access tokens|
| `tokenType`             | string  | Token type (always "Bearer")                        |
| `accessTokenExpiresIn`  | integer | Access token expiration time in milliseconds        |
| `refreshTokenExpiresIn` | integer | Refresh token expiration time in milliseconds       |

### 200 OK

```json
{
  "timestamp": "2025-01-24T15:35:12.456789+09:00",
  "result": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI3ZjM2YTg5ZC0xMjM0LTQ1NjctODkwMS0yMzQ1Njc4OTBhYmMiLCJpYXQiOjE3MDU5OTgzMTIsImV4cCI6MTcwNjAwMDExMn0.zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI3ZjM2YTg5ZC0xMjM0LTQ1NjctODkwMS0yMzQ1Njc4OTBhYmMiLCJpYXQiOjE3MDU5OTgzMTIsImV4cCI6MTcwNjYwMzExMn0.aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
    "tokenType": "Bearer",
    "accessTokenExpiresIn": 1800000,
    "refreshTokenExpiresIn": 2592000000
  }
}
```

## Error Cases

### 400 Bad Request
When the request body is malformed or missing required fields.

```json
{
  "timestamp": "2025-01-24T15:35:12.456789+09:00",
  "status": 400,
  "message": "Invalid request body"
}
```

### 401 Unauthorized
When the refresh token is invalid, expired, or malformed.

```json
{
  "timestamp": "2025-01-24T15:35:12.456789+09:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid JWT token"
}
```

### Token Format Error Cases

#### Expired Token
```json
{
  "timestamp": "2025-01-24T15:35:12.456789+09:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token has expired"
}
```

#### Malformed Token
```json
{
  "timestamp": "2025-01-24T15:35:12.456789+09:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid token format"
}
```
