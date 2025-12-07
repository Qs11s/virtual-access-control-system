package com.project.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScheduleItemDto {
    private LocalDateTime date;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String courseName;
    private String locationName;
}