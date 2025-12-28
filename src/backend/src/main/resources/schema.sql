-- 统一表结构定义（兼容所有功能，无自增冲突）
-- 1. 用户表（RBAC角色，自增ID）
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_STUDENT' COMMENT 'ROLE_STUDENT/ROLE_TEACHER/ROLE_ADMIN'
);

-- 2. 门禁地点表（自增ID）
CREATE TABLE IF NOT EXISTS locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    qr_token VARCHAR(100),
    address VARCHAR(255)
);

-- 3. 门禁事件记录表
CREATE TABLE IF NOT EXISTS access_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    access_time TIMESTAMP NOT NULL,
    method VARCHAR(50) NOT NULL,
    allowed BOOLEAN NOT NULL,
    status VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (location_id) REFERENCES locations(id)
);

-- 4. 课程表（关联教师用户）
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    teacher_id BIGINT NOT NULL,
    description VARCHAR(255),
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

-- 5. 课程时段表
CREATE TABLE IF NOT EXISTS sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (location_id) REFERENCES locations(id)
);

-- 6. 学生课程关联表（复合主键，无自增ID）
CREATE TABLE IF NOT EXISTS student_courses (
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    PRIMARY KEY (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- 7. 考勤表
CREATE TABLE IF NOT EXISTS attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    check_in_time TIMESTAMP,
    check_out_time TIMESTAMP,
    status VARCHAR(50),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (session_id) REFERENCES sessions(id)
);

-- 8. 临时码表（支撑临时码功能）
CREATE TABLE IF NOT EXISTS temp_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(6) NOT NULL UNIQUE,
    location_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    valid_minutes INT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    remaining_uses INT NOT NULL,
    used_at TIMESTAMP,
    FOREIGN KEY (location_id) REFERENCES locations(id),
    FOREIGN KEY (owner_id) REFERENCES users(id)
);