package com.project.backend.repository;

import com.project.backend.model.Attendance;
import com.project.backend.model.SessionEntity;
import com.project.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdHVkZW50MSIsImlhdCI6MTc2NDg1MjA1NSwiZXhwIjoxNzY0OTM4NDU1fQ.FiGsOYTmoSt1RgspBWlIv_RvgU57eMUW91FJoSyU26g


import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudent(User student);

    List<Attendance> findBySession(SessionEntity session);

    Optional<Attendance> findByStudentAndSession(User student, SessionEntity session);

    boolean existsByStudentAndSession(User student, SessionEntity session);
}
