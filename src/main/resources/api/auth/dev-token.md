# generateDevToken (POST /api/auth/dev/token)

## Description
Development-only endpoint that generates JWT tokens for a specific UUID. If the provided UUID doesn't exist in the system, a new user will be automatically created with that UUID. This endpoint is only available in non-production environments and is useful for testing and development purposes. Requires admin access key for authentication.

**Note: This endpoint is disabled in production environments.**

## Behavior
- If the UUID exists: Returns tokens for the existing user
- If the UUID doesn't exist: Creates a new user with the provided UUID and returns tokens

## Request

### Request Body

| Name       | Type   | Required | Constraint                        |
|------------|--------|----------|-----------------------------------|
| `uuid`     | string | true     | @NotBlank - User UUID             |
| `accessKey` | string | true     | @NotBlank - Admin access key      |

### Request Example
```json
{
  "uuid": "7f36a89d-1234-4567-8901-234567890abc",
  "accessKey": "DonaEmilyEatSteakLeopold"
}
```

## Reply

### Response Body

| Name                    | Type    | Description                                          |
|-------------------------|---------|------------------------------------------------------|
| `accessToken`           | string  | JWT access token for API authentication             |
| `refreshToken`          | string  | JWT refresh token for obtaining new access tokens   |
| `tokenType`             | string  | Token type (always "Bearer")                        |
| `accessTokenExpiresIn`  | integer | Access token expiration time in milliseconds        |
| `refreshTokenExpiresIn` | integer | Refresh token expiration time in milliseconds       |

### 200 OK

```json
{
  "timestamp": "2025-01-24T15:30:45.123456+09:00",
  "result": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI3ZjM2YTg5ZC0xMjM0LTQ1NjctODkwMS0yMzQ1Njc4OTBhYmMiLCJpYXQiOjE3MDU5OTgwNDUsImV4cCI6MTcwNTk5OTg0NX0.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI3ZjM2YTg5ZC0xMjM0LTQ1NjctODkwMS0yMzQ1Njc4OTBhYmMiLCJpYXQiOjE3MDU5OTgwNDUsImV4cCI6MTcwNjYwMjg0NX0.yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy",
    "tokenType": "Bearer",
    "accessTokenExpiresIn": 1800000,
    "refreshTokenExpiresIn": 2592000000
  }
}
```

## Error Cases

### 400 Bad Request
When the UUID or access key field is empty or missing.

```json
{
  "timestamp": "2025-01-24T15:30:45.123456+09:00",
  "status": 400,
  "message": "Invalid Input: [UUID is required]"
}
```

### 401 Unauthorized
When the access key is invalid.

```json
{
  "timestamp": "2025-01-24T15:30:45.123456+09:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid access key"
}
```

### 404 Not Found (Production Environment)
This endpoint returns 404 in production environments as it is not registered.

```json
{
  "timestamp": "2025-01-24T15:30:45.123456+09:00",
  "status": 404,
  "error": "Not Found",
  "message": "No static resource api/auth/dev/token."
}
```
