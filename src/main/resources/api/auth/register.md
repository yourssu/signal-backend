# register (POST /api/auth/register)

## Description
Creates a new user account and issues JWT tokens for authentication. This endpoint generates a unique UUID for the user and returns both access and refresh tokens.

## Request

### Request Body
This endpoint does not require a request body. A new user is automatically created upon calling.

## Reply

### Response Body

| Name                    | Type    | Description                                          |
|-------------------------|---------|------------------------------------------------------|
| `accessToken`           | string  | JWT access token for API authentication             |
| `refreshToken`          | string  | JWT refresh token for obtaining new access tokens   |
| `tokenType`             | string  | Token type (always "Bearer")                        |
| `accessTokenExpiresIn`  | integer | Access token expiration time in milliseconds        |
| `refreshTokenExpiresIn` | integer | Refresh token expiration time in milliseconds       |

### 201 Created

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

### 500 Internal Server Error
When there is an issue creating a new user or generating tokens.

```json
{
  "timestamp": "2025-01-24T15:30:45.123456+09:00",
  "status": 500,
  "message": "Internal server error occurred while processing the request"
}
```
