package com.project.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Data // 自动生成所有字段的get/set方法，包括新增的courseId
public class SessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    // 新增：courseId字段（兼容Controller中setCourseId/getCourseId的调用）
    // 注：@Column与数据库字段对应，name="course_id"和上面JoinColumn保持一致
    @Column(name = "course_id", insertable = false, updatable = false)
    private Long courseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}