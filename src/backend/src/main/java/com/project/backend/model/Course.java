package com.project.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "courses")
@Data // 关键：Lombok的@Data注解会自动为所有字段生成get/set/toString等方法
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String code;

    // 新增：teacher字段（对应Controller中调用的setTeacher/getTeacher）
    private String teacher;

    // 新增：description字段（对应Controller中调用的setDescription/getDescription）
    private String description;
}