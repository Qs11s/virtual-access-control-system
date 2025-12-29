package com.project.backend.controller;

import com.project.backend.model.Attendance;
import com.project.backend.model.SessionEntity;
import com.project.backend.repository.AttendanceRepository;
import com.project.backend.repository.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teacher/attendance")
public class TeacherAttendanceViewController {

    private final SessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;

    public TeacherAttendanceViewController(SessionRepository sessionRepository,
                                           AttendanceRepository attendanceRepository) {
        this.sessionRepository = sessionRepository;
        this.attendanceRepository = attendanceRepository;
    }

    private SessionEntity loadAndCheckSession(Long sessionId, UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        boolean isTeacher = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_TEACHER"));

        if (!isTeacher) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only teachers can view attendance");
        }

        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        if (session.getCourse() == null || session.getCourse().getTeacher() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Session course has no teacher");
        }

        String teacherUsername = session.getCourse().getTeacher().getUsername();
        if (!teacherUsername.equals(userDetails.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Session not owned by this teacher");
        }

        return session;
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<Map<String, Object>>> sessionAttendance(@PathVariable Long sessionId,
                                                                       @AuthenticationPrincipal UserDetails userDetails) {
        SessionEntity session = loadAndCheckSession(sessionId, userDetails);

        List<Attendance> list = attendanceRepository.findBySession_Id(session.getId());

        List<Map<String, Object>> result = list.stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("studentId", a.getStudent().getId());
            m.put("studentUsername", a.getStudent().getUsername());
            m.put("sessionId", a.getSession().getId());
            m.put("checkInTime", a.getCheckInTime());
            m.put("checkOutTime", a.getCheckOutTime());
            m.put("status", a.getStatus());
            return m;
        }).toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/session/{sessionId}/summary")
    public ResponseEntity<Map<String, Object>> sessionSummary(@PathVariable Long sessionId,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        SessionEntity session = loadAndCheckSession(sessionId, userDetails);

        List<Attendance> list = attendanceRepository.findBySession_Id(session.getId());

        long onTime = list.stream()
                .filter(a -> "ON_TIME".equals(a.getStatus()))
                .count();

        long late = list.stream()
                .filter(a -> "LATE".equals(a.getStatus()))
                .count();

        long earlyLeave = list.stream()
                .filter(a -> "EARLY_LEAVE".equals(a.getStatus()))
                .count();

        long total = list.size();

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("totalCheckedIn", total);
        result.put("onTime", onTime);
        result.put("late", late);
        result.put("earlyLeave", earlyLeave);

        return ResponseEntity.ok(result);
    }
}
