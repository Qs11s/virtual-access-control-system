package com.project.backend.repository;

import com.project.backend.model.SessionEntity;
import com.project.backend.model.Course;
import com.project.backend.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionRepository extends JpaRepository<SessionEntity, Long> {

    List<SessionEntity> findByCourse(Course course);

    List<SessionEntity> findByLocationAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            Location location,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}
