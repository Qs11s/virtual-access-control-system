-- 禁用外键约束（插入数据时避免顺序问题）
SET REFERENTIAL_INTEGRITY FALSE;

-- ===================== 基础数据插入（按依赖顺序） =====================
-- 1. 用户数据（适配测试用例：admin/teacher/student，{noop}标记明文密码）
INSERT INTO users (username, password, role)
VALUES 
('admin', '{noop}admin123', 'ROLE_ADMIN'),       -- 管理员（明文密码：admin123）
('teacher_zhang', '{noop}teacher123', 'ROLE_TEACHER'), -- 教师（明文密码：teacher123）
('student_wang', '{noop}student123', 'ROLE_STUDENT'),  -- 学生1（明文密码：student123）
('student_li', '{noop}student123', 'ROLE_STUDENT');    -- 学生2（备用）

-- 2. 地点数据（适配临时码验证：locationId=1）
INSERT INTO locations (code, name, qr_token, address)
VALUES 
('LOC_MAIN', '主教学楼门禁', 'qr_main_123', '主教学楼1楼'),  -- id=1
('LOC_LAB', '实验室门禁', 'qr_lab_456', '实验楼2楼');        -- id=2

-- 3. 课程数据（关联教师：teacher_zhang的id=2）
INSERT INTO courses (code, name, teacher_id, description)
VALUES 
('JAVA_101', 'Java核心编程', 2, '面向学生的Java基础课程'), -- id=1
('PYTHON_101', 'Python入门', 2, 'Python基础语法课程');     -- id=2

-- 4. 课程时段数据（关联课程1+地点1，时间为当前时段）
INSERT INTO sessions (course_id, location_id, start_time, end_time)
VALUES 
(1, 1, DATEADD('HOUR', -1, CURRENT_TIMESTAMP()), DATEADD('HOUR', 2, CURRENT_TIMESTAMP())), -- 有效时段（测试考勤）
(1, 1, DATEADD('DAY', 1, CURRENT_TIMESTAMP()), DATEADD('HOUR', 3, DATEADD('DAY', 1, CURRENT_TIMESTAMP()))); -- 未来时段

-- 5. 学生课程关联数据（初始选课：student_wang(id=3)选课程1）
INSERT INTO student_courses (student_id, course_id)
VALUES 
(3, 1); -- 学生wang选Java课程（测试考勤）

-- 启用外键约束
SET REFERENTIAL_INTEGRITY TRUE;

-- ===================== 测试辅助数据（可选） =====================
-- 临时码测试数据（可选，也可通过API创建）
-- INSERT INTO temp_codes (code, location_id, owner_id, valid_minutes, expires_at, remaining_uses)
-- VALUES ('888999', 1, 3, 30, DATEADD('MINUTE', 30, CURRENT_TIMESTAMP()), 5);