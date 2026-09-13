-- app/src/main/resources/sql/issue-211-deploy.md의 절차에 따라 애플리케이션 쓰기를 중지한 상태에서 실행한다.
ALTER TABLE meeting_room
    ADD COLUMN creation_limit_date DATE DEFAULT NULL AFTER creation_date;

UPDATE meeting_room
SET creation_limit_date = CASE
    WHEN status = 'EXPIRED' THEN NULL
    ELSE creation_date
END;

ALTER TABLE meeting_room
    DROP INDEX uk_meeting_room_creator_date,
    ADD UNIQUE KEY uk_meeting_room_creator_date (creator_uuid, creation_limit_date);
