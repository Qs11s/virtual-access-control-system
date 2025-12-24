package com.project.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 验证临时码的请求参数DTO
 */
@Data
public class TempCodeVerifyRequest {
    @NotNull(message = "门禁位置ID不能为空")
    private Long locationId;

    @NotBlank(message = "临时码不能为空")
    private String code;
}