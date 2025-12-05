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
}
