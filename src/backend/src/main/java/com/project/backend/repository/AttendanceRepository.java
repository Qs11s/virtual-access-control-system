package com.project.backend.repository;

import com.project.backend.model.Attendance;
import com.project.backend.model.SessionEntity;
import com.project.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudent(User student);

    List<Attendance> findBySession(SessionEntity session);

    Optional<Attendance> findByStudentAndSession(User student, SessionEntity session);

    boolean existsByStudentAndSession(User student, SessionEntity session);

    // 新增：countByStatus方法（匹配Controller中调用的countByStatus(String)）
    // 遵循JPA命名规范：countBy + 字段名（Status，字段名首字母大写）
    // 返回值为long：符合JPA统计方法的默认返回类型（计数结果为长整型）
    long countByStatus(String status);
}