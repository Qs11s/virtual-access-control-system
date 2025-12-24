package com.project.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "student_courses")
@Data
public class StudentCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false) // 新增：非空约束
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false) // 新增：非空约束
    private Course course;
}