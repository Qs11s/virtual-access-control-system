package com.project.backend.controller;

import com.project.backend.model.Attendance;
import com.project.backend.repository.AttendanceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/attendance")
public class AdminAttendanceController {

    private final AttendanceRepository attendanceRepository;

    public AdminAttendanceController(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
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
