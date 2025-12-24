package com.project.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生-课程关联操作（选课/退课）响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourseResponse {
    private String status; // success/fail
    private String message; // 操作提示信息
}