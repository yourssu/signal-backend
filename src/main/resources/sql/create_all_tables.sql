-- ==========================================
-- Database Creation
-- ==========================================
CREATE DATABASE IF NOT EXISTS `signal_dev`
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE `signal_dev`;

-- ==========================================
-- Drop existing tables
-- ==========================================
DROP TABLE IF EXISTS referral_order;
DROP TABLE IF EXISTS referral;
DROP TABLE IF EXISTS purchased_profile;
DROP TABLE IF EXISTS intro_sentence;
DROP TABLE IF EXISTS blacklist;
DROP TABLE IF EXISTS verification;
DROP TABLE IF EXISTS kakaopay_order;
DROP TABLE IF EXISTS order_history;
DROP TABLE IF EXISTS profile;
DROP TABLE IF EXISTS viewer;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS google_user;

-- ==========================================
-- Create Tables
-- ==========================================

-- 1. Users table
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    uuid VARCHAR(36) NOT NULL,
    created_time DATETIME(6) NOT NULL,
    updated_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_uuid (uuid),
    INDEX idx_users_uuid (uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Profile table
CREATE TABLE profile (
    id BIGINT NOT NULL AUTO_INCREMENT,
    uuid VARCHAR(36) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    department VARCHAR(255) NOT NULL,
    birth_year INT NOT NULL,
    animal VARCHAR(50) NOT NULL,
    contact VARCHAR(255) NOT NULL,
    mbti VARCHAR(4) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    created_time DATETIME(6) NOT NULL,
    updated_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_profile_uuid (uuid),
    INDEX idx_profile_uuid (uuid),
    INDEX idx_profile_gender (gender),
    INDEX idx_profile_department (department)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Intro Sentence table
CREATE TABLE intro_sentence (
    id BIGINT NOT NULL AUTO_INCREMENT,
    intro_sentence TEXT NOT NULL,
    uuid VARCHAR(36) NOT NULL,
    created_time DATETIME(6) NOT NULL,
    updated_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_intro_sentence_uuid (uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Viewer table
CREATE TABLE viewer (
    id BIGINT NOT NULL AUTO_INCREMENT,
    uuid VARCHAR(36) NOT NULL,
    ticket INT NOT NULL DEFAULT 0,
    used_ticket INT NOT NULL DEFAULT 0,
    version BIGINT DEFAULT NULL,
    created_time DATETIME(6) NOT NULL,
    updated_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_viewer_uuid (uuid),
    INDEX idx_viewer_uuid (uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Purchased Profile table
CREATE TABLE purchased_profile (
    id BIGINT NOT NULL AUTO_INCREMENT,
    purchase_id BIGINT NOT NULL,
    viewer_id BIGINT NOT NULL,
    created_time DATETIME(6) NOT NULL,
    updated_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_purchased_profile_purchase_id (purchase_id),
    INDEX idx_purchased_profile_viewer_id (viewer_id),
    UNIQUE KEY uk_purchased_profile (purchase_id, viewer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Blacklist table
CREATE TABLE blacklist (
    id BIGINT NOT NULL AUTO_INCREMENT,
    profile_id BIGINT NOT NULL,
    created_by_admin BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_blacklist_profile_id (profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Verification table
CREATE TABLE verification (
    id BIGINT NOT NULL AUTO_INCREMENT,
    uuid VARCHAR(36) NOT NULL,
    verification_code INT NOT NULL,
    created_time DATETIME(6) NOT NULL,
    updated_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_verification_uuid (uuid),
    UNIQUE KEY uk_verification_code (verification_code),
    INDEX idx_verification_uuid (uuid),
    INDEX idx_verification_code (verification_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. KakaoPay Order table
CREATE TABLE kakaopay_order (
    id BIGINT NOT NULL AUTO_INCREMENT,
    uuid VARCHAR(36) NOT NULL,
    tid VARCHAR(255) NOT NULL,
    order_id VARCHAR(255) NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    amount INT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    aid VARCHAR(255) DEFAULT NULL,
    approved_time DATETIME(6) DEFAULT NULL,
    canceled_time DATETIME(6) DEFAULT NULL,
    failed_time DATETIME(6) DEFAULT NULL,
    created_time DATETIME(6) NOT NULL,
    updated_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_kakaopay_order_tid (tid),
    INDEX idx_kakaopay_order_uuid (uuid),
    INDEX idx_kakaopay_order_order_id (order_id),
    INDEX idx_kakaopay_order_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Order History table
CREATE TABLE order_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id VARCHAR(255) NOT NULL,
    viewer_uuid VARCHAR(36) NOT NULL,
    amount INT NOT NULL,
    quantity INT NOT NULL,
    order_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_time DATETIME(6) NOT NULL,
    updated_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_history_order_id (order_id),
    INDEX idx_order_history_viewer_uuid (viewer_uuid),
    INDEX idx_order_history_order_id (order_id),
    INDEX idx_order_history_status (status),
    INDEX idx_order_history_order_type (order_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Google User table
CREATE TABLE google_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    uuid VARCHAR(36) NOT NULL,
    identifier VARCHAR(255) NOT NULL,
    created_time DATETIME(6) NOT NULL,
    updated_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_google_user_identifier (identifier),
    INDEX idx_google_user_uuid (uuid),
    INDEX idx_google_user_identifier (identifier)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Referral table
CREATE TABLE referral (
    id BIGINT NOT NULL AUTO_INCREMENT,
    origin VARCHAR(36) NOT NULL,
    referral_code VARCHAR(255) NOT NULL,
    created_time DATETIME(6) NOT NULL,
    updated_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_referral_referral_code (referral_code),
    INDEX idx_referral_origin (origin),
    INDEX idx_referral_referral_code (referral_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Referral Order table
CREATE TABLE referral_order (
    id BIGINT NOT NULL AUTO_INCREMENT,
    referral_code VARCHAR(255) NOT NULL,
    viewer_uuid VARCHAR(36) NOT NULL,
    created_time DATETIME(6) NOT NULL,
    updated_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_referral_order_referral_code (referral_code),
    INDEX idx_referral_order_viewer_uuid (viewer_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- Create Views (Optional)
-- ==========================================

-- View for profile with intro sentences count
CREATE OR REPLACE VIEW v_profile_summary AS
SELECT 
    p.id,
    p.uuid,
    p.nickname,
    p.gender,
    p.department,
    p.mbti,
    COUNT(i.id) as intro_count
FROM profile p
LEFT JOIN intro_sentence i ON p.uuid = i.uuid
GROUP BY p.id, p.uuid, p.nickname, p.gender, p.department, p.mbti;

-- View for viewer statistics
CREATE OR REPLACE VIEW v_viewer_statistics AS
SELECT 
    v.id,
    v.uuid,
    v.ticket,
    v.used_ticket,
    COUNT(pp.id) as purchased_profiles_count
FROM viewer v
LEFT JOIN purchased_profile pp ON v.id = pp.viewer_id
GROUP BY v.id, v.uuid, v.ticket, v.used_ticket;

-- ==========================================
-- Grant permissions (adjust as needed)
-- ==========================================
-- GRANT ALL PRIVILEGES ON signal_db.* TO 'signal_user'@'localhost' IDENTIFIED BY 'your_password';
-- FLUSH PRIVILEGES;

-- ==========================================
-- Initial Data (Optional)
-- ==========================================

-- Insert sample users
-- INSERT INTO users (uuid, created_time, updated_time) VALUES 
-- ('550e8400-e29b-41d4-a716-446655440000', NOW(), NOW()),
-- ('550e8400-e29b-41d4-a716-446655440001', NOW(), NOW());

-- ==========================================
-- Table Information
-- ==========================================
SELECT 
    'Database created successfully!' AS message,
    DATABASE() AS current_database;

-- Show all tables
SHOW TABLES;

-- Show table count
SELECT COUNT(*) AS total_tables FROM information_schema.tables 
WHERE table_schema = 'signal' AND table_type = 'BASE TABLE';
