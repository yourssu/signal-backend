# getViewerUuid (GET /api/viewers/uuid)

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
| `usedTicket`        | integer   | The number of used tickets                         |
| `updatedTime`       | string    | latest issued tickets                              |
| `purchasedProfiles` | integer[] | Ids of purchased Profiles                          |

### 200 OK

```json
{
  "timestamp": "2025-04-30T13:57:23.439067+09:00",
  "result": {
    "id": 1,
    "uuid": "ec5ea893-8540-42c5-958a-ce69ed7f9f63",
    "ticket": 3,
    "usedTicket": 2,
    "updatedTime": "2025-04-30T13:57:16.343937+09:00",
    "purchasedProfiles": [
      {
        "profileId": 1,
        "createdTime": "2025-04-30T13:57:16.342400+09:00[Asia/Seoul]"
      }
    ]
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
