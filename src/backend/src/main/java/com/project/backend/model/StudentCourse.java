package com.project.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 学生-课程关联表实体类
 * 对应数据库表：student_courses
 * 复合主键：student_id + course_id
 */
@Entity
@Table(name = "student_courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourse {

    /**
     * 复合主键（注意：必须保证非空）
     */
    @EmbeddedId
    private StudentCourseId id = new StudentCourseId();

    /**
     * 关联学生（映射复合主键的 studentId）
     * 注意：这里不要再写 insertable=false, updatable=false，
     * 否则在 @MapsId 场景下可能导致 Hibernate 无法写入外键列/主键列。
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("studentId")
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /**
     * 关联课程（映射复合主键的 courseId）
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("courseId")
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * 复合主键类（必须实现 Serializable）
     */
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentCourseId implements Serializable {

        private static final long serialVersionUID = 1L;

        @Column(name = "student_id")
        private Long studentId;

        @Column(name = "course_id")
        private Long courseId;
    }
}
