package com.project.backend.service;

import com.project.backend.model.Course;
import com.project.backend.model.StudentCourse;
import com.project.backend.model.User;
import com.project.backend.repository.CourseRepository;
import com.project.backend.repository.StudentCourseRepository;
import com.project.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StudentCourseService {

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * 选课：按学生ID+课程ID关联
     */
    public void enroll(Long studentId, Long courseId) {
        // 1. 校验学生存在且为学生角色
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在：ID=" + studentId));
        if (!"ROLE_STUDENT".equals(student.getRole())) {
            throw new RuntimeException("用户不是学生角色：ID=" + studentId);
        }

        // 2. 校验课程存在
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在：ID=" + courseId));

        // 3. 校验是否已选课（调用仓库的existsByStudentIdAndCourseId）
        if (studentCourseRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new RuntimeException("学生已选该课程：学生ID=" + studentId + "，课程ID=" + courseId);
        }

        // 4. 新增选课关系
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setStudent(student);
        studentCourse.setCourse(course);
        studentCourseRepository.save(studentCourse);
    }

    /**
     * 退课：按学生ID+课程ID解除关联
     */
    public void unenroll(Long studentId, Long courseId) {
        // 1. 校验是否已选课
        if (!studentCourseRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new RuntimeException("学生未选该课程：学生ID=" + studentId + "，课程ID=" + courseId);
        }

        // 2. 删除选课关系（调用仓库的deleteByStudentIdAndCourseId）
        studentCourseRepository.deleteByStudentIdAndCourseId(studentId, courseId);
    }

    /**
     * 检查学生是否已选某课程
     */
    public boolean isEnrolled(Long studentId, Long courseId) {
        return studentCourseRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }
}