package com.project.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccessRecordResponse {
    private String locationName;
    private String locationCode;
    private String accessTime;
    private String method;
    private boolean allowed;
}
