package com.project.backend.controller;

import com.project.backend.dto.AttendanceRequest;
import com.project.backend.model.Attendance;
import com.project.backend.model.SessionEntity;
import com.project.backend.model.User;
import com.project.backend.repository.AttendanceRepository;
import com.project.backend.repository.SessionRepository;
import com.project.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/attendance")
public class AttendanceCheckoutController {

    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public AttendanceCheckoutController(AttendanceRepository attendanceRepository,
                                        SessionRepository sessionRepository,
                                        UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, Object>> checkout(@RequestBody AttendanceRequest request,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        if (request == null || request.getSessionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing sessionId");
        }

        boolean isStudent = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_STUDENT"));

        if (!isStudent) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can checkout");
        }

        User user = userRepository.findFirstByUsernameOrderByIdDesc(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        SessionEntity session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        Attendance attendance = attendanceRepository
                .findFirstBySession_IdAndStudent_UsernameOrderByIdDesc(request.getSessionId(), userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No check-in record for this session"));

        if (attendance.getCheckOutTime() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already checked out");
        }

        LocalDateTime now = LocalDateTime.now();
        attendance.setCheckOutTime(now);

        LocalDateTime endTime = session.getEndTime();
        if (endTime != null && now.isBefore(endTime.minusMinutes(5))) {
            attendance.setStatus("EARLY_LEAVE");
        }

        attendanceRepository.save(attendance);

        Map<String, Object> body = new HashMap<>();
        body.put("id", attendance.getId());
        body.put("studentId", attendance.getStudent().getId());
        body.put("sessionId", session.getId());
        body.put("checkInTime", attendance.getCheckInTime());
        body.put("checkOutTime", attendance.getCheckOutTime());
        body.put("status", attendance.getStatus());

        return ResponseEntity.ok(body);
    }
}
