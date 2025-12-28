package com.project.backend.controller;

import com.project.backend.model.AccessEvent;
import com.project.backend.model.Attendance;
import com.project.backend.model.Location;
import com.project.backend.model.SessionEntity;
import com.project.backend.model.User;
import com.project.backend.repository.AccessEventRepository;
import com.project.backend.repository.AttendanceRepository;
import com.project.backend.repository.LocationRepository;
import com.project.backend.repository.SessionRepository;
import com.project.backend.repository.StudentCourseRepository;
import com.project.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/access")
public class AccessScanController {

    private final AccessEventRepository accessEventRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final AttendanceRepository attendanceRepository;

    public AccessScanController(AccessEventRepository accessEventRepository,
                                LocationRepository locationRepository,
                                UserRepository userRepository,
                                SessionRepository sessionRepository,
                                StudentCourseRepository studentCourseRepository,
                                AttendanceRepository attendanceRepository) {
        this.accessEventRepository = accessEventRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @PostMapping("/scan")
    public ResponseEntity<Map<String, Object>> scan(@RequestBody Map<String, Object> payload,
                                                    @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        Long locationId = getRequiredLong(payload, "locationId");
        String method = payload.getOrDefault("method", "QR").toString();
        String qrToken = payload.get("qrToken") != null ? payload.get("qrToken").toString() : null;

        Long sessionId = null;
        if (payload.get("sessionId") != null) {
            sessionId = getRequiredLong(payload, "sessionId");
        }

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));

        User user = userRepository.findFirstByUsernameOrderByIdDesc(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean allowed = true;
        String denyReason = null;

        if ("QR".equalsIgnoreCase(method)) {
            String req = qrToken == null ? null : qrToken.trim();
            String db = location.getQrToken() == null ? null : location.getQrToken().trim();
            if (req == null || db == null || !req.equalsIgnoreCase(db)) {
                allowed = false;
                denyReason = "Invalid QR token: req=" + req + ", db=" + db;
            }
        }

        boolean isStudent = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_STUDENT"));

        Attendance attendance = null;

        if (allowed && isStudent && sessionId != null) {
            SessionEntity session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

            boolean enrolled = studentCourseRepository
                    .existsByStudent_IdAndCourse_Id(user.getId(), session.getCourse().getId());

            if (!enrolled) {
                allowed = false;
                denyReason = "Student not enrolled: studentId=" + user.getId()
                        + ", courseId=" + session.getCourse().getId();
            } else {
                boolean alreadyCheckedIn = attendanceRepository.existsByStudentAndSession(user, session);
                if (!alreadyCheckedIn) {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime startTime = session.getStartTime();
                    Duration diff = Duration.between(startTime, now);

                    String status = (diff.isNegative() || diff.toMinutes() <= 10) ? "ON_TIME" : "LATE";

                    attendance = new Attendance();
                    attendance.setStudent(user);
                    attendance.setSession(session);
                    attendance.setCheckInTime(now);
                    attendance.setStatus(status);
                    attendanceRepository.save(attendance);
                }
            }
        }

        AccessEvent event = new AccessEvent();
        event.setUser(user);
        event.setLocation(location);
        event.setAccessTime(LocalDateTime.now());
        event.setMethod(method);
        event.setAllowed(allowed);
        accessEventRepository.save(event);

        Map<String, Object> body = new HashMap<>();
        body.put("allowed", allowed);
        body.put("message", allowed
                ? "Access granted"
                : (denyReason != null ? denyReason : "Access denied"));
        body.put("locationId", locationId);
        body.put("method", method);
        body.put("accessEventId", event.getId());

        if (sessionId != null) {
            body.put("sessionId", sessionId);
        }

        if (attendance != null) {
            Map<String, Object> attendanceInfo = new HashMap<>();
            attendanceInfo.put("id", attendance.getId());
            attendanceInfo.put("status", attendance.getStatus());
            attendanceInfo.put("checkInTime", attendance.getCheckInTime());
            attendanceInfo.put("checkOutTime", attendance.getCheckOutTime());
            body.put("attendance", attendanceInfo);
        }

        HttpStatus status = allowed ? HttpStatus.OK : HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(body);
    }

    private Long getRequiredLong(Map<String, Object> payload, String fieldName) {
        Object value = payload.get(fieldName);
        if (value == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Missing field: " + fieldName
            );
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid long value for field: " + fieldName
            );
        }
    }
}
