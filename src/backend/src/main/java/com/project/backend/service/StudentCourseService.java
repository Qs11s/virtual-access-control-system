package com.project.backend.service;

import com.project.backend.dto.StudentCourseResponse;
import com.project.backend.model.Course;
import com.project.backend.model.StudentCourse;
import com.project.backend.model.User;
import com.project.backend.repository.CourseRepository;
import com.project.backend.repository.StudentCourseRepository;
import com.project.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional // 保证选课/退课操作的事务一致性
public class StudentCourseService {

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * 选课操作：将学生关联到课程
     */
    public StudentCourseResponse enrollStudent(Long studentId, Long courseId) {
        StudentCourseResponse response = new StudentCourseResponse();

        // 1. 验证学生是否存在且为 STUDENT 角色
        Optional<User> studentOpt = userRepository.findByIdAndRole(studentId, "ROLE_STUDENT");
        if (studentOpt.isEmpty()) {
            response.setStatus("fail");
            response.setMessage("学生不存在或角色无效（仅学生可选课）");
            return response;
        }

        // 2. 验证课程是否存在
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            response.setStatus("fail");
            response.setMessage("课程不存在");
            return response;
        }

        // 3. 检查是否已选课（避免重复）
        if (studentCourseRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            response.setStatus("fail");
            response.setMessage("该学生已选此课程，无需重复选课");
            return response;
        }

        // 4. 新增选课关系
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setStudent(studentOpt.get());
        studentCourse.setCourse(courseOpt.get());
        studentCourseRepository.save(studentCourse);

        // 5. 构建成功响应
        response.setStatus("success");
        response.setMessage("选课成功");
        return response;
    }

    /**
     * 退课操作：删除学生与课程的关联关系
     */
    public StudentCourseResponse unenrollStudent(Long studentId, Long courseId) {
        StudentCourseResponse response = new StudentCourseResponse();

        // 1. 检查是否存在选课关系
        boolean exists = studentCourseRepository.existsByStudentIdAndCourseId(studentId, courseId);
        if (!exists) {
            response.setStatus("success");
            response.setMessage("该学生未选此课程，无需退课");
            return response;
        }

        // 2. 删除选课关系
        studentCourseRepository.deleteByStudentIdAndCourseId(studentId, courseId);

        // 3. 构建成功响应
        response.setStatus("success");
        response.setMessage("退课成功");
        return response;
    }
}