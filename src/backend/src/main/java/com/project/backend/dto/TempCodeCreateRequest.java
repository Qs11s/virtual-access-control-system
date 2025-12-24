package com.project.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建临时码的请求参数DTO
 */
@Data // 简化getter/setter，需引入lombok依赖（或手动编写getter/setter）
public class TempCodeCreateRequest {
    @NotNull(message = "门禁位置ID不能为空")
    private Long locationId;

    @NotNull(message = "归属用户ID不能为空")
    private Long ownerId;

    @NotNull(message = "有效分钟数不能为空")
    @Min(value = 1, message = "有效分钟数不能小于1")
    private Integer validMinutes;

    @NotNull(message = "剩余使用次数不能为空")
    @Min(value = 1, message = "剩余使用次数不能小于1")
    private Integer remainingUses;
}