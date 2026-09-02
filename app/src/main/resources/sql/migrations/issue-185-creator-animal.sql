SET @creator_animal_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'meeting_room'
      AND column_name = 'creator_animal'
);

SET @creator_animal_column_ddl = IF(
    @creator_animal_column_exists = 0,
    'ALTER TABLE meeting_room ADD COLUMN creator_animal VARCHAR(50) NULL AFTER creator_uuid',
    'SELECT 1'
);

PREPARE creator_animal_column_statement FROM @creator_animal_column_ddl;
EXECUTE creator_animal_column_statement;
DEALLOCATE PREPARE creator_animal_column_statement;

UPDATE meeting_room AS room
INNER JOIN profile AS creator_profile
    ON creator_profile.uuid = room.creator_uuid
SET room.creator_animal = creator_profile.animal
WHERE room.creator_animal IS NULL;
