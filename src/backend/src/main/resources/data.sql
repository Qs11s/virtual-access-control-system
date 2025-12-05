-- Admin
INSERT INTO users (username, password)
VALUES ('test2', '$2a$10$KAg6hJk1mEHKSqGHqDywuOXYBGHuvCzruB9BKksxDGYc7NZVXMpy6');

INSERT INTO locations (code, name, qr_token)
VALUES ('MAIN01', 'Main Gate', 'initial-token');

INSERT INTO courses (name, code)
VALUES ('Test Course', 'C001');

INSERT INTO sessions (course_id, location_id, start_time, end_time)
VALUES (1, 1, '2025-01-01T10:00:00', '2025-01-01T12:00:00');
