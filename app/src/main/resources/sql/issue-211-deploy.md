# Issue 211 DB 배포 절차

이 변경은 롤링 배포하지 않는다. 모든 writer를 중지하고 DB 쓰기가 없는 것을 확인한 뒤 진행한다.

## 배포

1. 애플리케이션 writer를 모두 중지하고 활성 writer가 0개인지 확인한다.
2. `issue-211-deploy.sql`을 문장별로 실행하고 단계별로 확인한다. 중간 실패 시 확인 쿼리로 이미 완료된 단계를 판별해 그 다음 문장부터 재개한다.

컬럼 추가 후 컬럼이 nullable로 존재하는지 확인한다.

```sql
SHOW COLUMNS FROM meeting_room LIKE 'creation_limit_date';
```

백필 후 아래 결과가 0인지 확인한다.

```sql
SELECT COUNT(*) AS invalid_rows
FROM meeting_room
WHERE status <> 'EXPIRED' AND creation_limit_date IS NULL;
```

인덱스 교체 후 `Seq_in_index` 순서가 `creator_uuid`, `creation_limit_date`인지 확인한다.

```sql
SHOW INDEX FROM meeting_room WHERE Key_name = 'uk_meeting_room_creator_date';
```

3. 신버전 애플리케이션의 `ddl-auto=validate` 기동과 헬스체크가 성공한 뒤 쓰기를 재개한다.

## 롤백

신버전에서 쓰기를 한 번이라도 재개했다면 구버전으로 롤백하지 않고 forward fix한다. 쓰기 재개 전 신버전 기동에 실패한 경우에만 아래 절차로 DB를 먼저 되돌린 뒤 구버전을 기동한다.

```sql
SELECT creator_uuid, creation_date, COUNT(*) AS duplicate_count
FROM meeting_room
GROUP BY creator_uuid, creation_date
HAVING COUNT(*) > 1;
```

결과가 0건일 때만 실행한다.

```sql
ALTER TABLE meeting_room
    DROP INDEX uk_meeting_room_creator_date,
    ADD UNIQUE KEY uk_meeting_room_creator_date (creator_uuid, creation_date);

ALTER TABLE meeting_room
    DROP COLUMN creation_limit_date;
```
