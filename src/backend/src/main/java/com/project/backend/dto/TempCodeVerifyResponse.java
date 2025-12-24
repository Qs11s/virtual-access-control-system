package com.project.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证临时码的响应数据DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TempCodeVerifyResponse {
    private String result; // allow/deny
    private String reason; // 响应说明
}