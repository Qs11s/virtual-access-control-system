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

import java.time.LocalDateTime;
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
        LocalDateTime now = LocalDateTime.now();

        List<Attendance> list = attendanceRepository.findBySession_Id(session.getId());

        List<Map<String, Object>> result = list.stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("studentId", a.getStudent().getId());
            m.put("studentUsername", a.getStudent().getUsername());
            m.put("sessionId", a.getSession().getId());
            m.put("checkInTime", a.getCheckInTime());
            m.put("checkOutTime", a.getCheckOutTime());

            String status = a.getStatus();
            if (a.getCheckInTime() != null
                    && a.getCheckOutTime() == null
                    && now.isAfter(a.getSession().getEndTime())) {
                status = "NONE";
            }
            m.put("status", status);
            m.put("earlyLeaveApproved", a.getEarlyLeaveApproved());
            m.put("earlyLeaveReason", a.getEarlyLeaveReason());

            return m;
        }).toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/session/{sessionId}/summary")
    public ResponseEntity<Map<String, Object>> sessionSummary(@PathVariable Long sessionId,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        SessionEntity session = loadAndCheckSession(sessionId, userDetails);
        LocalDateTime now = LocalDateTime.now();

        List<Attendance> list = attendanceRepository.findBySession_Id(session.getId());

        long onTime = list.stream()
                .filter(a -> "ON_TIME".equals(a.getStatus()))
                .count();

        long late = list.stream()
                .filter(a -> "LATE".equals(a.getStatus()))
                .count();

        long earlyLeaveApproved = list.stream()
                .filter(a -> "EARLY_LEAVE".equals(a.getStatus()))
                .filter(a -> Boolean.TRUE.equals(a.getEarlyLeaveApproved()))
                .count();

        long earlyLeaveUnapproved = list.stream()
                .filter(a -> "EARLY_LEAVE".equals(a.getStatus()))
                .filter(a -> !Boolean.TRUE.equals(a.getEarlyLeaveApproved()))
                .count();

        long none = list.stream()
                .filter(a -> a.getCheckInTime() != null)
                .filter(a -> a.getCheckOutTime() == null)
                .filter(a -> now.isAfter(a.getSession().getEndTime()))
                .count();

        long total = list.size();

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("totalCheckedIn", total);
        result.put("onTime", onTime);
        result.put("late", late);
        result.put("earlyLeave", earlyLeaveUnapproved);
        result.put("earlyLeaveApproved", earlyLeaveApproved);
        result.put("none", none);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/session/{sessionId}/early-end")
    public ResponseEntity<Map<String, Object>> earlyEnd(@PathVariable Long sessionId,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        SessionEntity session = loadAndCheckSession(sessionId, userDetails);

        LocalDateTime now = LocalDateTime.now();
        session.setEndTime(now);
        sessionRepository.save(session);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("newEndTime", session.getEndTime());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{attendanceId}/approve-early-leave")
    public ResponseEntity<Map<String, Object>> approveEarlyLeave(@PathVariable Long attendanceId,
                                                                 @AuthenticationPrincipal UserDetails userDetails,
                                                                 @RequestBody ApproveRequest request) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attendance not found"));

        SessionEntity session = loadAndCheckSession(attendance.getSession().getId(), userDetails);

        if (!"EARLY_LEAVE".equals(attendance.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only EARLY_LEAVE records can be approved");
        }

        attendance.setEarlyLeaveApproved(true);
        attendance.setEarlyLeaveReason(request != null ? request.getReason() : null);
        attendanceRepository.save(attendance);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getId());
        result.put("attendanceId", attendance.getId());
        result.put("earlyLeaveApproved", attendance.getEarlyLeaveApproved());
        result.put("earlyLeaveReason", attendance.getEarlyLeaveReason());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/session/{sessionId}/approve-early-leave")
    public ResponseEntity<Map<String, Object>> approveEarlyLeaveBatch(@PathVariable Long sessionId,
                                                                      @AuthenticationPrincipal UserDetails userDetails,
                                                                      @RequestBody ApproveBatchRequest request) {
        SessionEntity session = loadAndCheckSession(sessionId, userDetails);

        if (request == null || request.getAttendanceIds() == null || request.getAttendanceIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "attendanceIds is required");
        }

        List<Attendance> list = attendanceRepository.findAllById(request.getAttendanceIds());

        int updatedCount = 0;
        for (Attendance a : list) {
            if (!a.getSession().getId().equals(session.getId())) {
                continue;
            }
            if (!"EARLY_LEAVE".equals(a.getStatus())) {
                continue;
            }
            a.setEarlyLeaveApproved(true);
            a.setEarlyLeaveReason(request.getReason());
            updatedCount++;
        }

        attendanceRepository.saveAll(list);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("updatedCount", updatedCount);

        return ResponseEntity.ok(result);
    }

    public static class ApproveRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public static class ApproveBatchRequest {
        private List<Long> attendanceIds;
        private String reason;

        public List<Long> getAttendanceIds() {
            return attendanceIds;
        }

        public void setAttendanceIds(List<Long> attendanceIds) {
            this.attendanceIds = attendanceIds;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
