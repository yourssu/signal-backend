CREATE TABLE IF NOT EXISTS meeting_room (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slot VARCHAR(20) NOT NULL,
    active_slot VARCHAR(20) DEFAULT NULL,
    creator_uuid VARCHAR(36) NOT NULL,
    party_size INT NOT NULL,
    invitation VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    creation_date DATE NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    matched_at DATETIME(6) DEFAULT NULL,
    cancelled_at DATETIME(6) DEFAULT NULL,
    expired_at DATETIME(6) DEFAULT NULL,
    created_time DATETIME(6) NOT NULL,
    updated_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_meeting_room_active_slot (active_slot),
    UNIQUE KEY uk_meeting_room_creator_date (creator_uuid, creation_date),
    INDEX idx_meeting_room_board (status, expires_at),
    INDEX idx_meeting_room_creator_uuid (creator_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS meeting_member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    team_side VARCHAR(20) NOT NULL,
    member_order INT NOT NULL,
    user_uuid VARCHAR(36) DEFAULT NULL,
    gender VARCHAR(10) NOT NULL,
    birth_year INT NOT NULL,
    department VARCHAR(255) NOT NULL,
    created_time DATETIME(6) NOT NULL,
    updated_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_meeting_member_room_side_order (room_id, team_side, member_order),
    INDEX idx_meeting_member_room_id (room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS meeting_match (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    applicant_uuid VARCHAR(36) NOT NULL,
    creator_contact VARCHAR(1024) NOT NULL,
    applicant_contact VARCHAR(1024) NOT NULL,
    matched_at DATETIME(6) NOT NULL,
    created_time DATETIME(6) NOT NULL,
    updated_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_meeting_match_room_id (room_id),
    INDEX idx_meeting_match_applicant_uuid (applicant_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
