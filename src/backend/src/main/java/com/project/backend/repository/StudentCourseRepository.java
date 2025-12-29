package com.project.backend.repository;

import com.project.backend.model.Course;
import com.project.backend.model.StudentCourse;
import com.project.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, StudentCourse.StudentCourseId> {

    List<StudentCourse> findByStudent(User student);

    Optional<StudentCourse> findByStudentAndCourse(User student, Course course);

    void deleteByStudentAndCourse(User student, Course course);

    boolean existsByStudent_IdAndCourse_Id(Long studentId, Long courseId);

    void deleteByStudent_IdAndCourse_Id(Long studentId, Long courseId);

    List<StudentCourse> findByStudent_Id(Long studentId);

    long countByCourse_Id(Long courseId);
}
