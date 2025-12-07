INSERT INTO users (username, password)
VALUES ('test2', '\$2a\$10\$KAg6hJk1mEHKSqGHqDywuOXYBGHuvCzruB9BKksxDGYc7NZVXMpy6');

-- 2. 位置信息
INSERT INTO locations (code, name, qr_token)
VALUES ('MAIN01', 'Main Gate', 'initial-token');

-- 3. 课程（Test Course）
INSERT INTO courses (name, code)
VALUES ('Test Course', 'C001');

-- 4. 基础场次（兼容H2时间格式，同时新增有效时间场次）
-- 4.1 原有历史场次（保留）
INSERT INTO sessions (course_id, location_id, start_time, end_time)
VALUES (1, 1, '2025-01-01T10:00:00', '2025-01-01T12:00:00');

-- 4.2 新增当前有效场次（每次启动生成±20分钟，避免过期）
INSERT INTO sessions (course_id, location_id, start_time, end_time)
VALUES (1, 1, DATEADD('MINUTE', -20, NOW()), DATEADD('MINUTE', 20, NOW()));

-- 5. 学生-课程关联（student005=ID39绑定Test Course）
MERGE INTO STUDENT_COURSES (STUDENT_ID, COURSE_ID) 
KEY (STUDENT_ID, COURSE_ID) 
VALUES (39, 1);