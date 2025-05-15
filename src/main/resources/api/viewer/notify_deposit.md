# NotifyDeposit (POST /api/viewers/deposit)

## Request

### Request Body

| Name               | Type    | Required | Constraint                   |
|--------------------|---------|----------|------------------------------|
| `message`          | string  | true     | @NotBlank && @Size(max = 20) |
| `verificationCode` | integer | true     | @NumberFormat                |

## Reply

### 200 OK
