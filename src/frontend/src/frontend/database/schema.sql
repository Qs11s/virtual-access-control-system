-- database/schema.sql
-- Minimal schema for Virtual Access & Attendance System (draft)

CREATE TABLE users (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  username    VARCHAR(50)  NOT NULL,
  full_name   VARCHAR(100) NOT NULL,
  role        ENUM('admin', 'member') DEFAULT 'member',
  dept        VARCHAR(100),
  status      ENUM('active', 'disabled') DEFAULT 'active'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE doors (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  door_code   VARCHAR(50)  NOT NULL UNIQUE,
  door_name   VARCHAR(100) NOT NULL,
  location    VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE events (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  user_id     INT NOT NULL,
  door_id     INT NOT NULL,
  method      ENUM('qr', 'otp', 'password') DEFAULT 'qr',
  result      ENUM('success', 'denied', 'error') DEFAULT 'success',
  reason      VARCHAR(255),
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (door_id) REFERENCES doors(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

