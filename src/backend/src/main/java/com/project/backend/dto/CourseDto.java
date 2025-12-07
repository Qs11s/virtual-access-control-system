package com.project.backend.dto;

import lombok.Data;

@Data
public class CourseDto {
    private Long id;
    private String name;
    private String teacher;
    private String description;
}