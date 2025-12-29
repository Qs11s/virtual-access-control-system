package com.project.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApproveEarlyLeaveBatchRequest {

    private List<Long> attendanceIds;

    private String reason;
}
