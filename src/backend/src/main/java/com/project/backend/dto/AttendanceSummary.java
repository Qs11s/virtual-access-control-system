package com.project.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AttendanceSummary {

    private Long id;
    private Long studentId;
    private Long sessionId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String status;
}
