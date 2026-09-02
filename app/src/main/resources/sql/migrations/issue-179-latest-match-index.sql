SET @latest_match_index_exists = (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'meeting_room'
      AND index_name = 'idx_meeting_room_latest_match'
);

SET @latest_match_index_ddl = IF(
    @latest_match_index_exists = 0,
    'CREATE INDEX idx_meeting_room_latest_match ON meeting_room (status, matched_at, id)',
    'SELECT 1'
);

PREPARE latest_match_index_statement FROM @latest_match_index_ddl;
EXECUTE latest_match_index_statement;
DEALLOCATE PREPARE latest_match_index_statement;
