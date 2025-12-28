package com.project.backend.dto;

import lombok.Data;

@Data
public class CourseDto {
    private Long id;
    private String name;
    private String code;
    private Long teacherId;          // 替代原 teacher(String)
    private String teacherUsername;  // 可选：返回给前端展示
    private String description;
}