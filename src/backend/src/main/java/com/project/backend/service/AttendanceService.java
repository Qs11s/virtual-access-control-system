package com.project.backend.service;

import com.project.backend.exception.DuplicateCheckInException;
import com.project.backend.exception.ExpiredSessionException;
import com.project.backend.exception.StudentNotEnrolledException;
import com.project.backend.model.Attendance;
import com.project.backend.model.Course;
import com.project.backend.model.SessionEntity;
import com.project.backend.model.StudentCourse;
import com.project.backend.model.User;
import com.project.backend.repository.AttendanceRepository;
import com.project.backend.repository.SessionRepository;
import com.project.backend.repository.StudentCourseRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AttendanceService {

    private final SessionRepository sessionRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final AttendanceRepository attendanceRepository;

    public AttendanceService(SessionRepository sessionRepository,
                             StudentCourseRepository studentCourseRepository,
                             AttendanceRepository attendanceRepository) {
        this.sessionRepository = sessionRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public Attendance checkIn(User student, Long sessionId) {
        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(session.getStartTime()) || now.isAfter(session.getEndTime())) {
            throw new ExpiredSessionException();
        }

        Course course = session.getCourse();
        Optional<StudentCourse> scOpt = studentCourseRepository.findByStudentAndCourse(student, course);
        if (scOpt.isEmpty()) {
            throw new StudentNotEnrolledException();
        }

        Optional<Attendance> existingOpt = attendanceRepository.findByStudentAndSession(student, session);
        if (existingOpt.isPresent()) {
            Attendance existing = existingOpt.get();
            if (existing.getCheckInTime() != null) {
                throw new DuplicateCheckInException();
            }
        }

        Attendance attendance = existingOpt.orElseGet(Attendance::new);
        attendance.setStudent(student);
        attendance.setSession(session);
        attendance.setCheckInTime(now);

        LocalDateTime start = session.getStartTime();
        Duration diff = Duration.between(start, now);
        if (diff.toMinutes() <= 10) {
            attendance.setStatus("PRESENT");
        } else {
            attendance.setStatus("LATE");
        }

        return attendanceRepository.save(attendance);
    }

    public Attendance checkOut(User student, Long sessionId) {
        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        Attendance attendance = attendanceRepository.findByStudentAndSession(student, session)
                .orElseThrow(() -> new RuntimeException("No existing attendance record"));

        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("Already checked out");
        }

        LocalDateTime now = LocalDateTime.now();
        attendance.setCheckOutTime(now);

        LocalDateTime end = session.getEndTime();
        Duration diff = Duration.between(now, end);
        if (diff.toMinutes() > 5) {
            attendance.setStatus("EARLY_LEAVE");
        }

        return attendanceRepository.save(attendance);
    }
}