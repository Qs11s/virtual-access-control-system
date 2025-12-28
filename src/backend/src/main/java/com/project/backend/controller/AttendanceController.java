package com.project.backend.controller;

import com.project.backend.dto.AttendanceRequest;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<Map<String, Object>> checkIn(@RequestBody AttendanceRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        String username = auth.getName();
        User student = userRepository.findFirstByUsernameOrderByIdDesc(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Long sessionId = request.getSessionId();
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
        String status = (diff.isNegative() || diff.toMinutes() <= 10) ? "ON_TIME" : "LATE";

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setSession(session);
        attendance.setCheckInTime(now);
        attendance.setStatus(status);
        attendanceRepository.save(attendance);

        AttendanceSummary summary = new AttendanceSummary(
                attendance.getId(),
                attendance.getStudent().getId(),
                attendance.getSession().getId(),
                attendance.getCheckInTime(),
                attendance.getCheckOutTime(),
                attendance.getStatus()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Check-in recorded");
        response.put("data", summary);
        response.put("code", HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public List<AttendanceSummary> getMyAttendance() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        String username = auth.getName();

        List<Attendance> records = attendanceRepository
                .findByStudent_UsernameOrderByCheckInTimeDesc(username);

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

    @GetMapping("/session/{sessionId}/summary")
    public Map<String, Object> getSessionSummary(@PathVariable Long sessionId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        boolean hasTeacherOrAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_TEACHER") || a.equals("ROLE_ADMIN"));

        if (!hasTeacherOrAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only teacher or admin can view this summary");
        }

        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        List<Attendance> records = attendanceRepository.findBySession(session);

        long onTime = records.stream().filter(a -> "ON_TIME".equals(a.getStatus())).count();
        long late = records.stream().filter(a -> "LATE".equals(a.getStatus())).count();

        Map<String, Object> resp = new HashMap<>();
        resp.put("sessionId", sessionId);
        resp.put("checkedIn", records.size());
        resp.put("onTime", onTime);
        resp.put("late", late);
        return resp;
    }
}
