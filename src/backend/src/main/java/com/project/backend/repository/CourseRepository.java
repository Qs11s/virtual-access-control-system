package com.project.backend.repository;

import com.project.backend.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCode(String code);

    List<Course> findByTeacher_Username(String username);

    Optional<Course> findByIdAndTeacher_Username(Long id, String username);
}
