package com.project.backend.repository;

import com.project.backend.model.SessionEntity;
import com.project.backend.model.Course;
import com.project.backend.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionRepository extends JpaRepository<SessionEntity, Long> {

    List<SessionEntity> findByCourse(Course course);

    // 新增：findByCourseId方法（匹配Controller中调用的findByCourseId(Long)）
    // 遵循JPA命名规范：findBy + 字段名（CourseId，字段名首字母大写）
    List<SessionEntity> findByCourseId(Long courseId);

    List<SessionEntity> findByLocationAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            Location location,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}
