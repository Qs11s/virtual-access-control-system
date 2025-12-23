package com.project.backend.dto;

import lombok.Data;

@Data
public class CourseDto {
    private Long id;
    private String name;
    private String code;  // 添加这个字段
    private String teacher;
    private String description;
}