-- 第一步：禁用外键约束检查（双重保障）
SET REFERENTIAL_INTEGRITY FALSE;

-- 第二步：使用CASCADE级联删除表，彻底删除表及所有依赖约束
DROP TABLE IF EXISTS student_courses CASCADE;
DROP TABLE IF EXISTS sessions CASCADE;
DROP TABLE IF EXISTS courses CASCADE;
DROP TABLE IF EXISTS locations CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 第三步：重新启用外键约束检查
SET REFERENTIAL_INTEGRITY TRUE;

-- 第四步：手动创建表结构（去掉自增ID，避免与手动插入ID冲突）
-- 1. 地点表（去掉AUTO_INCREMENT，手动指定ID）
CREATE TABLE locations (
    id BIGINT PRIMARY KEY, -- 去掉AUTO_INCREMENT，手动指定ID
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    qr_token VARCHAR(100),
    address VARCHAR(255)
);

-- 2. 用户表（去掉AUTO_INCREMENT，手动指定ID）
CREATE TABLE users (
    id BIGINT PRIMARY KEY, -- 去掉AUTO_INCREMENT，手动指定ID
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL
);

-- 3. 课程表（去掉AUTO_INCREMENT，手动指定ID）
CREATE TABLE courses (
    id BIGINT PRIMARY KEY, -- 去掉AUTO_INCREMENT，手动指定ID
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    teacher_id BIGINT NOT NULL,
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

-- 4. 会话表（保留AUTO_INCREMENT，无需手动指定ID）
CREATE TABLE sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (location_id) REFERENCES locations(id)
);

-- 5. 学生课程关联表（无自增ID，复合主键）
CREATE TABLE student_courses (
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    PRIMARY KEY (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- 第五步：插入测试数据（手动指定ID，确保与测试用例匹配）
-- 用户数据
INSERT INTO users (id, username, password, role)
VALUES (1, 'admin2', '$2b$10$ZW2NeWxUmrg2dq6QBTCoQ.K5lkQe1kJokCPssJwCRBE4cDZDAdfkm', 'ROLE_ADMIN');

INSERT INTO users (id, username, password, role)
VALUES (3, 'teacher1', '$2b$10$r/QIOuAJLJmEWiGn2c6oYO1837pgN7VIbb7sp2xfpy71EGPrUtWW2', 'ROLE_TEACHER');

INSERT INTO users (id, username, password, role)
VALUES (2, 'student1', '$2b$10$oGlFYw0XlvSFP72bowyg2e.0Aq.dkJzLFKvZkt.zLQl1IduDC0Aua', 'ROLE_STUDENT');

INSERT INTO users (id, username, password, role)
VALUES (4, 'test2', '$2b$10$z1IQ22QTGBiAsmjo3SFdEOJJId.Os0iHjhlT5Od4GKi6aWKYTbDFK', 'ROLE_STUDENT');

-- 地点数据（确保ID=1/2插入成功）
INSERT INTO locations (id, code, name, qr_token, address)
VALUES (1, 'MAIN01', 'Main Gate', 'initial-token', 'Main Gate Address');

INSERT INTO locations (id, code, name, qr_token, address)
VALUES (2, 'TEST_LOC001', 'Test Main Gate', 'initial-test-token', 'Test Address 1');

-- 课程数据
INSERT INTO courses (id, code, name, teacher_id)
VALUES (1, 'C001', 'Test Course', 3);

-- 会话数据
INSERT INTO sessions (course_id, location_id, start_time, end_time)
VALUES (1, 1, TIMESTAMP '2025-01-01 10:00:00', TIMESTAMP '2025-01-01 12:00:00');

INSERT INTO sessions (course_id, location_id, start_time, end_time)
VALUES (1, 1, DATEADD('MINUTE', -20, CURRENT_TIMESTAMP()), DATEADD('MINUTE', 20, CURRENT_TIMESTAMP()));

-- 学生课程关联数据
INSERT INTO student_courses (student_id, course_id)
VALUES (2, 1);