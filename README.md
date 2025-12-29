
# Virtual Access Control and Attendance System

## 1. Overview

This project is a **virtual access control and attendance management backend** for a
university/classroom scenario. It provides:

* JWT-based login and role management
* Course sessions and student attendance
* Teacher tools to view and approve attendance (e.g. early leave)
* Admin reports and temporary door access codes

The current implementation focuses on the **Spring Boot backend**. Any frontend or mobile
client can consume the REST APIs exposed by this service.

---

## 2. Main Features

### 2.1 Authentication & Roles

* `POST /auth/login` returns a JWT token
* Roles:

  * `ROLE_ADMIN`
  * `ROLE_TEACHER`
  * `ROLE_STUDENT`

### 2.2 Attendance

* Student check-in / check-out per session

* Attendance status values:

  * `ON_TIME` – check-in within on-time threshold
  * `LATE` – check-in after threshold
  * `EARLY_LEAVE` – checked out much earlier than end time
  * `NONE` – checked in but never checked out (derived/used for reporting)

* Teacher can:

  * View attendance list and summary per session
  * Mark a session as early ended (update `endTime` to now)
  * Approve early leave (single or batch), with `earlyLeaveReason`

### 2.3 Admin Reports

Administrator can:

* View raw attendance records for a session
* View summary metrics, including:

  * `totalEnrolled`
  * `totalCheckedIn`
  * `onTime`
  * `late`
  * `earlyLeave`
  * `earlyLeaveApproved`
  * `none` (checked in but no checkout)
  * `absent` (enrolled but no attendance record)

### 2.4 Temporary Access Codes (Door Access)

* Admin creates temporary codes for specific locations
* Anonymous verification endpoint for door terminals
* Each successful verification is logged as an `AccessEvent`

Main behaviours:

* Temporary code has:

  * `locationId`
  * Validity period (minutes)
  * Maximum usage count
  * `expiresAt`
  * `remainingUses`
  * `ownerId` (who created the code)
* Verification returns:

  * `{"result": "allow", "reason": "验证成功，允许开门"}`
  * Or `{"result": "deny", "reason": "...原因..."}`

---

## 3. Technology Stack

* Java 17
* Spring Boot 3.x
* Spring Security + JWT
* Spring Data JPA
* H2 Database

  * File-based DB for runtime
  * In-memory H2 for tests
* Maven
* JUnit 5, Mockito, Spring Boot Test

---

## 4. Project Structure (Backend)

```text
virtual-access-control-system/
└─ src/
   └─ backend/
      ├─ pom.xml
      ├─ src/
      │  ├─ main/
      │  │  ├─ java/
      │  │  │  └─ com/project/backend/...
      │  │  └─ resources/
      │  │     ├─ application.yml
      │  │     ├─ schema.sql
      │  │     └─ data.sql
      │  └─ test/
      │     ├─ java/
      │     │  └─ com/project/backend/...
      │     └─ resources/
      │        └─ application-test.yml
```

Key packages:

* `com.project.backend.controller` – REST controllers (auth, attendance, teacher, admin, access)
* `com.project.backend.service` – business logic services (AttendanceService, TempCodeService, etc.)
* `com.project.backend.model` – JPA entities (User, Course, SessionEntity, Attendance, TempCode, etc.)
* `com.project.backend.repository` – Spring Data JPA repositories

---

## 5. Getting Started

### 5.1 Prerequisites

* JDK 17+
* Maven 3.8+
* Git
* curl or Postman (for testing APIs)

### 5.2 Run the Backend

From the backend module:

```bash
cd src/backend
mvn spring-boot:run
```

The backend starts on:

* [http://localhost:8080](http://localhost:8080)

H2 console (for debugging):

* [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

Default H2 console settings:

* JDBC URL: `jdbc:h2:file:./data/db`
* User: `sa`
* Password: *(empty)*

### 5.3 Demo Accounts

Initial demo users (from `data.sql`):

| Role    | Username        | Password     |
| ------- | --------------- | ------------ |
| Admin   | `admin`         | `admin123`   |
| Teacher | `teacher_zhang` | `teacher123` |
| Student | `student_wang`  | `student123` |
| Student | `student_li`    | `student123` |

---

## 6. API Overview (Short)

下面只是一个简要总览，方便前端 / 测试同学快速对接。

### 6.1 Authentication

* `POST /auth/login`

  * Body:

    ```json
    {
      "username": "admin",
      "password": "admin123"
    }
    ```
  * Response (example):

    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiJ9..."
    }
    ```

JWT 使用方式：

* 所有需要登录的接口，都在 Header 中带上：
  `Authorization: Bearer <token>`

### 6.2 Attendance (Student)

* `POST /attendance/checkin`

  * Header: `Authorization: Bearer <student-token>`
  * Body:

    ```json
    {
      "sessionId": 2
    }
    ```

* `POST /attendance/checkout`

  * Header: `Authorization: Bearer <student-token>`
  * Body:

    ```json
    {
      "sessionId": 2
    }
    ```

后端会根据课程开始/结束时间和当前时间，自动计算：

* 签到：`ON_TIME` 或 `LATE`
* 签退：可能变为 `EARLY_LEAVE`

### 6.3 Teacher Attendance APIs

所有 Teacher 端接口都需要教师的 JWT，并且只允许访问**自己课程的 session**。

* `GET /teacher/attendance/session/{sessionId}`
  返回该节课所有学生的考勤记录（含 earlyLeaveApproved, earlyLeaveReason）。

* `GET /teacher/attendance/session/{sessionId}/summary`
  返回统计数据：

  * `totalCheckedIn`
  * `onTime`, `late`
  * `earlyLeave`, `earlyLeaveApproved`
  * `none`

* `POST /teacher/attendance/{attendanceId}/approve-early-leave`

  * Body:

    ```json
    {
      "reason": "课堂内容提前完成，允许学生提前离场"
    }
    ```

* `POST /teacher/attendance/session/{sessionId}/approve-early-leave`
  批量批准：

  * Body:

    ```json
    {
      "attendanceIds": [112, 113],
      "reason": "本次课程提前结束，允许提前签退"
    }
    ```

* `POST /teacher/attendance/session/{sessionId}/early-end`
  将该节课的 `endTime` 更新为当前时间，后续签退视为正常。

### 6.4 Admin Attendance APIs

所有 Admin 端接口都需要管理员 JWT。

* `GET /admin/attendance/session/{sessionId}`
  返回该节课的完整考勤列表。

* `GET /admin/attendance/session/{sessionId}/summary`
  返回包含以下字段的汇总：

  * `totalEnrolled`
  * `totalCheckedIn`
  * `onTime`, `late`
  * `earlyLeave`, `earlyLeaveApproved`
  * `none`
  * `absent`

### 6.5 Temporary Access Code APIs

#### Admin 创建临时码

* `POST /access/temp-code`

  * Header: `Authorization: Bearer <admin-token>`
  * Body（示例）：

    ```json
    {
      "locationId": 1,
      "expiresInMinutes": 10,
      "maxUses": 2
    }
    ```
  * Response（示例）：

    ```json
    {
      "code": "948446",
      "expiresAt": "2025-12-29T09:15:53.245324123"
    }
    ```

#### 终端验证临时码

* `POST /access/temp-code/verify`

  * 无需登录（门禁设备调用）
  * Body：

    ```json
    {
      "locationId": 1,
      "code": "948446"
    }
    ```
  * Response（示例）：

    ```json
    {
      "result": "allow",
      "reason": "验证成功，允许开门"
    }
    ```

---

## 7. Testing

### 7.1 Automated Tests

运行全部后端测试：

```bash
cd src/backend
mvn test
```

测试环境使用 **内存 H2 数据库** 和独立的测试配置（`application-test.yml`），每次运行前会初始化干净的数据，保证测试可重复。

主要白盒测试覆盖：

* `AttendanceService`

  * 准点签到与迟到区分
  * 未选课学生无法签到
  * 课前/课后不能签到
  * 重复签到拦截
  * 提前签退标记为 `EARLY_LEAVE`
  * 不存在记录或重复签退时抛出异常

* `TempCodeService`

  * 临时码不存在 → `deny / 密码无效`
  * 已过期 → `deny / 临时码已过期`
  * 次数用尽 → `deny / 临时码次数已用尽`
  * 正常使用 → `allow / 验证成功，允许开门`，并减少 `remainingUses`，更新 `usedAt`，写入 `AccessEvent`

### 7.2 Manual API Testing

黑盒测试可以通过 `curl` 或 Postman 完成，典型场景包括：

* 不同角色登录获取 JWT
* 学生：签到 + 签退，校验 status 变化
* 教师：查看考勤列表与统计、审批早退、提前下课
* 管理员：查看考勤统计、缺勤人数
* 临时门禁码：管理员生成，终端验证

---

## 8. License

For academic use in software engineering coursework.

