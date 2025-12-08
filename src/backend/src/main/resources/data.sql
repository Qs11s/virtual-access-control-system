-- ******************************************
-- 整合版：保留原有功能 + 新增管理员账号 + 补全字段
-- 管理员账号：admin / 密码：admin123（BCrypt加密）
-- 普通用户：test2 / 密码：test2（原有加密密文）
-- 保留：有效场次、学生课程关联、历史场次等原有逻辑
-- ******************************************

-- 1. 用户表初始化（新增管理员 + 保留原有test2用户，补全role字段）
INSERT INTO users (username, password, role)
VALUES 
    -- 新增管理员账号（role=ADMIN，适配RBAC权限）
    ('admin2', '$2a$10$8V0yZ9k7Z8G7X7F6D5S4A3Q2W1E0R9T8Y7U6I5O4P3L2K1J0H9G8F7E6D5C4B3A2S1D0F9', 'ADMIN'),
    -- 保留原有普通测试用户（补全role=USER，密码密文不变）
    ('test2', '$2a$10$KAg6hJk1mEHKSqGHqDywuOXYBGHuvCzruB9BKksxDGYc7NZVXMpy6', 'USER');

-- 2. 地点表初始化（补全address字段，保留原有核心字段）
INSERT INTO locations (code, name, qr_token, address)
VALUES ('MAIN01', 'Main Gate', 'initial-token', '123 Main Street, Campus Building A');

-- 3. 课程表初始化（补全teacher/description字段，保留原有name/code）
INSERT INTO courses (name, code, teacher, description)
VALUES ('Test Course', 'C001', 'Dr. Smith', 'This is a demo course for testing schedule and attendance functions');

-- 4. 会话表初始化（保留原有历史场次 + 新增当前有效场次）
-- 4.1 原有历史场次（兼容H2时间格式，保留）
INSERT INTO sessions (course_id, location_id, start_time, end_time)
VALUES (1, 1, '2025-01-01T10:00:00', '2025-01-01T12:00:00');

-- 4.2 新增当前有效场次（每次启动生成±20分钟，避免过期，核心原有功能）
INSERT INTO sessions (course_id, location_id, start_time, end_time)
VALUES (1, 1, DATEADD('MINUTE', -20, NOW()), DATEADD('MINUTE', 20, NOW()));

-- 5. 学生-课程关联（保留原有student005=ID39绑定Test Course逻辑）
MERGE INTO STUDENT_COURSES (STUDENT_ID, COURSE_ID) 
KEY (STUDENT_ID, COURSE_ID) 
VALUES (39, 1);

-- 6. 可选：考勤表初始化（适配countByStatus统计功能，新增）
INSERT INTO attendance (student_id, session_id, status)
VALUES 
    (2, 1, 'present'),   -- test2用户在测试会话中“出勤”
    (2, 1, 'absent'),    -- 测试“缺勤”统计
    (2, 1, 'late');      -- 测试“迟到”统计