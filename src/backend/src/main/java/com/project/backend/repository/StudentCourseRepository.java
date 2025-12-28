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

    // 原有方法（保留）
    List<StudentCourse> findByStudent(User student);

    Optional<StudentCourse> findByStudentAndCourse(User student, Course course);

    void deleteByStudentAndCourse(User student, Course course);

    // ========= 修复：按外键 id 进行派生查询（适配复合主键表结构） =========
    boolean existsByStudent_IdAndCourse_Id(Long studentId, Long courseId);

    void deleteByStudent_IdAndCourse_Id(Long studentId, Long courseId);

    List<StudentCourse> findByStudent_Id(Long studentId);
}