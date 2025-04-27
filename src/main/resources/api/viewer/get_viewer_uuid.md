# getViewerUuid (GET /api/viewer/uuid)

## Request

### Query Parameters

| Name   | Type   | Required | Constraint |
|--------|--------|----------|------------|
| `uuid` | string | true     | @NotBlank  |

## Reply

### Response Body

| Name                | Type      | Description                                        |
|---------------------|-----------|----------------------------------------------------|
| `id`                | integer   | The unique identifier of the created viewer        |
| `uuid`              | string    | The Unique identifier string of the created viewer |
| `gender`            | string    | Gender of the viewer owner                         |
| `ticket`            | integer   | The number of total tickets                        |
| `usedTicket`        | integer   | The number of used tickets                         |
| `updatedTime`       | string    | latest issued tickets                              |
| `purchasedProfiles` | integer[] | Ids of purchased Profiles                          |

### 200 OK

```json
{
  "timestamp": "2025-04-27T16:25:05.072975+09:00",
  "result": {
    "id": 1,
    "uuid": "ec5ea893-8540-42c5-958a-ce69ed7f9f63",
    "ticket": 3,
    "gender": "MALE",
    "usedTicket": 0,
    "updatedTime": "2025-04-27T16:25:01.767483+09:00",
    "purchasedProfiles": []
  }
}
```

### 404 Not Found

- the uuid does not exist in all viewer

```json
{
  "timestamp": "2025-04-27T14:19:15.75724+09:00",
  "status": 404,
  "message": "해당하는 Viewer를 찾을 수 없습니다."
}
```
