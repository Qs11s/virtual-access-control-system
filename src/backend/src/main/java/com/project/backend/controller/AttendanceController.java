package com.project.backend.controller;

import com.project.backend.dto.AttendanceSummary;
import com.project.backend.model.Attendance;
import com.project.backend.model.SessionEntity;
import com.project.backend.model.StudentCourse;
import com.project.backend.model.User;
import com.project.backend.repository.AttendanceRepository;
import com.project.backend.repository.SessionRepository;
import com.project.backend.repository.StudentCourseRepository;
import com.project.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final StudentCourseRepository studentCourseRepository;

    public AttendanceController(AttendanceRepository attendanceRepository,
                                SessionRepository sessionRepository,
                                UserRepository userRepository,
                                StudentCourseRepository studentCourseRepository) {
        this.attendanceRepository = attendanceRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.studentCourseRepository = studentCourseRepository;
    }

    @GetMapping("/ping")
    public String ping() {
        return "attendance-ok";
    }

    @PostMapping("/checkin")
    public ResponseEntity<String> checkIn(@RequestParam Long sessionId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        String username = auth.getName();
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        Optional<StudentCourse> enrollment = studentCourseRepository.findByStudentAndCourse(student, session.getCourse());
        if (enrollment.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student not enrolled in this course");
        }

        boolean alreadyCheckedIn = attendanceRepository.existsByStudentAndSession(student, session);
        if (alreadyCheckedIn) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Already checked in for this session");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = session.getStartTime();
        Duration diff = Duration.between(startTime, now);

        String status;
        if (diff.isNegative() || diff.toMinutes() <= 10) {
            status = "ON_TIME";
        } else {
            status = "LATE";
        }

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setSession(session);
        attendance.setCheckInTime(now);
        attendance.setStatus(status);
        attendanceRepository.save(attendance);

        return ResponseEntity.ok("Check-in recorded");
    }

    @GetMapping("/me")
    public List<AttendanceSummary> getMyAttendance() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        String username = auth.getName();
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Attendance> records = attendanceRepository.findByStudent(student);
        return records.stream()
                .map(a -> new AttendanceSummary(
                        a.getId(),
                        a.getStudent().getId(),
                        a.getSession().getId(),
                        a.getCheckInTime(),
                        a.getCheckOutTime(),
                        a.getStatus()
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/session/{sessionId}")
    public List<AttendanceSummary> getSessionAttendance(@PathVariable Long sessionId) {
        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        List<Attendance> records = attendanceRepository.findBySession(session);
        return records.stream()
                .map(a -> new AttendanceSummary(
                        a.getId(),
                        a.getStudent().getId(),
                        a.getSession().getId(),
                        a.getCheckInTime(),
                        a.getCheckOutTime(),
                        a.getStatus()
                ))
                .collect(Collectors.toList());
    }
}
