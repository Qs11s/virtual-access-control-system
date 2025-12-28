package com.project.backend.repository;

import com.project.backend.model.Course;
import com.project.backend.model.StudentCourse;
import com.project.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {
    // 原有方法（保留）
    List<StudentCourse> findByStudent(User student);
    Optional<StudentCourse> findByStudentAndCourse(User student, Course course);
    void deleteByStudentAndCourse(User student, Course course);

    // ========== 新增：Service中调用的缺失方法 ==========
    // 1. 按学生ID+课程ID判断是否存在（适配Service的existsByStudentIdAndCourseId）
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    
    // 2. 按学生ID+课程ID删除（适配Service的deleteByStudentIdAndCourseId）
    void deleteByStudentIdAndCourseId(Long studentId, Long courseId);
}