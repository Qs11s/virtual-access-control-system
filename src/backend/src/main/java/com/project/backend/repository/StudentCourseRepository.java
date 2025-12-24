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

    // 新增：按学生ID和课程ID判断是否已选课（无需查询完整User/Course实体，提升性能）
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    // 新增：按学生ID和课程ID删除选课关系（直接操作主键，高效删除）
    void deleteByStudentIdAndCourseId(Long studentId, Long courseId);

    // 新增：按学生ID查询选课记录（便于“我的课程”查询）
    List<StudentCourse> findByStudentId(Long studentId);
}