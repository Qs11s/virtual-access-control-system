package com.project.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TempCodeCreateRequest {

    @NotNull(message = "门禁位置ID不能为空")
    private Long locationId;

    @NotNull(message = "有效分钟数不能为空")
    @Min(value = 1, message = "有效分钟数不能小于1")
    private Integer expiresInMinutes;

    @NotNull(message = "最大使用次数不能为空")
    @Min(value = 1, message = "最大使用次数不能小于1")
    private Integer maxUses;
}
