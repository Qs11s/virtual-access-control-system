package com.project.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 创建临时码的响应数据DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TempCodeCreateResponse {
    private String code; // 6位临时码
    private LocalDateTime expiresAt; // 过期时间
}