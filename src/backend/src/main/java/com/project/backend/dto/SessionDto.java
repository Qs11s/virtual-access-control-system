package com.project.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SessionDto {
    private Long id;
    private Long courseId;
    private String courseName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String locationName;
}