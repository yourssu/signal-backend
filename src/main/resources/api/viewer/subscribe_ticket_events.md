# subscribeToTicketEvents (GET /api/viewers/tickets/events)

## Request

### Query Parameters

| Name   | Type   | Required | Constraint |
|--------|--------|----------|------------|
| `uuid` | string | true     | @NotBlank  |

## Reply

Content-Type: `text/event-stream`

### 200 OK

### 503 Service Unavailable(timeout = 300s)
