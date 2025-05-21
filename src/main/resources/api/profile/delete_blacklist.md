# addBlacklist (DELETE /api/profiles/blacklists/{profileId})

- delete a blacklist from the profileId
- requires secretKey
- 
## Request

### Path Variable

| Name        | Type    | Required | Constraint |
|-------------|---------|----------|------------|
| `profileId` | integer | true     | @NotNull   |


### Query Parameters
| Name        | Type    | Required | Constraint |
|-------------|---------|----------|------------|
| `secretKey` | string  | true     | @NotBlank  |

## Reply

### 204 No Content
