package com.project.backend.repository;

import com.project.backend.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCode(String code);
}
