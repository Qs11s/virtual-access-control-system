package com.project.backend.repository;

import com.project.backend.model.Course;
import com.project.backend.model.StudentCourse;
import com.project.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {

    List<StudentCourse> findByStudent(User student);

    Optional<StudentCourse> findByStudentAndCourse(User student, Course course);
}
