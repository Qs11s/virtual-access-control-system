MERGE INTO users (username, password, role) KEY (username)
VALUES ('admin2', '$2b$10$ZW2NeWxUmrg2dq6QBTCoQ.K5lkQe1kJokCPssJwCRBE4cDZDAdfkm', 'ROLE_ADMIN');

MERGE INTO users (username, password, role) KEY (username)
VALUES ('teacher1', '$2b$10$r/QIOuAJLJmEWiGn2c6oYO1837pgN7VIbb7sp2xfpy71EGPrUtWW2', 'ROLE_TEACHER');

MERGE INTO users (username, password, role) KEY (username)
VALUES ('student1', '$2b$10$oGlFYw0XlvSFP72bowyg2e.0Aq.dkJzLFKvZkt.zLQl1IduDC0Aua', 'ROLE_STUDENT');

MERGE INTO users (username, password, role) KEY (username)
VALUES ('test2', '$2b$10$z1IQ22QTGBiAsmjo3SFdEOJJId.Os0iHjhlT5Od4GKi6aWKYTbDFK', 'ROLE_STUDENT');

MERGE INTO locations (code, name, qr_token) KEY (code)
VALUES ('MAIN01', 'Main Gate', 'initial-token');

MERGE INTO courses (code, name, teacher_id) KEY (code)
VALUES ('C001', 'Test Course', (SELECT id FROM users WHERE username='teacher1'));

INSERT INTO sessions (course_id, location_id, start_time, end_time)
VALUES (
  (SELECT id FROM courses WHERE code='C001'),
  (SELECT id FROM locations WHERE code='MAIN01'),
  TIMESTAMP '2025-01-01 10:00:00',
  TIMESTAMP '2025-01-01 12:00:00'
);

INSERT INTO sessions (course_id, location_id, start_time, end_time)
VALUES (
  (SELECT id FROM courses WHERE code='C001'),
  (SELECT id FROM locations WHERE code='MAIN01'),
  DATEADD('MINUTE', -20, CURRENT_TIMESTAMP()),
  DATEADD('MINUTE',  20, CURRENT_TIMESTAMP())
);

MERGE INTO student_courses (student_id, course_id) KEY (student_id, course_id)
SELECT u.id, c.id FROM users u, courses c
WHERE u.username='student1' AND c.code='C001';
