# addBlacklist (POST /api/profiles/blacklists)

- add blacklist to the profile
- requires secretKey

## Request

### Request Body

| Name        | Type    | Required | Constraint |
|-------------|---------|----------|------------|
| `secretKey` | string  | true     | @NotBlank  |
| `profileId` | integer | true     | @NotNull   |

## Reply

### 200 OK

### Response Body

| Name        | Type    | Description                          |
|-------------|---------|--------------------------------------|
| `id`        | integer | Blacklist's Id                       |
| `profileId` | integer | The unique identifier of the profile |
