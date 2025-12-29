package com.project.backend.controller;

import com.project.backend.model.Attendance;
import com.project.backend.model.SessionEntity;
import com.project.backend.repository.AttendanceRepository;
import com.project.backend.repository.SessionRepository;
import com.project.backend.repository.StudentCourseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/attendance")
public class AdminAttendanceController {

    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    private final StudentCourseRepository studentCourseRepository;

    public AdminAttendanceController(AttendanceRepository attendanceRepository,
                                     SessionRepository sessionRepository,
                                     StudentCourseRepository studentCourseRepository) {
        this.attendanceRepository = attendanceRepository;
        this.sessionRepository = sessionRepository;
        this.studentCourseRepository = studentCourseRepository;
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<Map<String, Object>>> getSessionAttendance(@PathVariable Long sessionId) {
        List<Attendance> list = attendanceRepository.findBySession_Id(sessionId);

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
    public ResponseEntity<Map<String, Object>> getSessionAttendanceSummary(@PathVariable Long sessionId) {
        List<Attendance> list = attendanceRepository.findBySession_Id(sessionId);

        long onTime = list.stream()
                .filter(a -> "ON_TIME".equals(a.getStatus()) || "PRESENT".equals(a.getStatus()))
                .count();

        long late = list.stream()
                .filter(a -> "LATE".equals(a.getStatus()))
                .count();

        long earlyLeave = list.stream()
                .filter(a -> "EARLY_LEAVE".equals(a.getStatus()))
                .count();

        long totalCheckedIn = list.size();

        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        Long courseId = session.getCourse().getId();
        long totalEnrolled = studentCourseRepository.countByCourse_Id(courseId);

        long absent = 0;
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(session.getEndTime()) && totalEnrolled > totalCheckedIn) {
            absent = totalEnrolled - totalCheckedIn;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("totalEnrolled", totalEnrolled);
        result.put("totalCheckedIn", totalCheckedIn);
        result.put("onTime", onTime);
        result.put("late", late);
        result.put("earlyLeave", earlyLeave);
        result.put("absent", absent);

        return ResponseEntity.ok(result);
    }
}
