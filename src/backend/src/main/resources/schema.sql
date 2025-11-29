CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255),
    password VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(255) UNIQUE,
    name VARCHAR(255),
    qr_token VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS access_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    location_id BIGINT,
    access_time TIMESTAMP,
    method VARCHAR(255),
    allowed BOOLEAN
);
