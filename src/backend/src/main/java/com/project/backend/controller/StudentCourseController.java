package com.project.backend.controller;

import com.project.backend.model.Course;
import com.project.backend.model.StudentCourse;
import com.project.backend.model.User;
import com.project.backend.repository.CourseRepository;
import com.project.backend.repository.StudentCourseRepository;
import com.project.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 学生-课程关联控制器（选课/退课）
 */
@RestController
@RequestMapping("/admin/courses")
public class StudentCourseController {

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * 选课接口：关联学生和课程
     */
    @PostMapping("/{courseId}/students/{studentId}")
    public ResponseEntity<Map<String, String>> enrollStudent(
            @PathVariable Long courseId,
            @PathVariable Long studentId) {
        
        Map<String, String> response = new HashMap<>();

        // 1. 校验学生是否存在且为学生角色
        User student = userRepository.findById(studentId)
                .orElse(null);
        if (student == null) {
            response.put("status", "failed");
            response.put("message", "学生不存在");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        if (!"ROLE_STUDENT".equals(student.getRole())) {
            response.put("status", "failed");
            response.put("message", "该用户不是学生角色，无法选课");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 2. 校验课程是否存在
        Course course = courseRepository.findById(courseId)
                .orElse(null);
        if (course == null) {
            response.put("status", "failed");
            response.put("message", "课程不存在");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 3. 校验是否已选课
        Optional<StudentCourse> existing = studentCourseRepository.findByStudentAndCourse(student, course);
        if (existing.isPresent()) {
            response.put("status", "failed");
            response.put("message", "学生已选该课程，无需重复选课");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 4. 新增选课关系
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setStudent(student);
        studentCourse.setCourse(course);
        studentCourseRepository.save(studentCourse);

        // 5. 成功响应
        response.put("status", "success");
        response.put("message", "学生" + student.getUsername() + "成功选课程" + course.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * 退课接口：解除学生和课程的关联
     */
    @DeleteMapping("/{courseId}/students/{studentId}")
    public ResponseEntity<Map<String, String>> unenrollStudent(
            @PathVariable Long courseId,
            @PathVariable Long studentId) {
        
        Map<String, String> response = new HashMap<>();

        // 1. 校验学生和课程是否存在
        User student = userRepository.findById(studentId).orElse(null);
        Course course = courseRepository.findById(courseId).orElse(null);
        if (student == null) {
            response.put("status", "failed");
            response.put("message", "学生不存在");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        if (course == null) {
            response.put("status", "failed");
            response.put("message", "课程不存在");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 2. 校验是否已选课
        Optional<StudentCourse> existing = studentCourseRepository.findByStudentAndCourse(student, course);
        if (existing.isEmpty()) {
            response.put("status", "success");
            response.put("message", "学生未选该课程，无需退课");
            return ResponseEntity.ok(response);
        }

        // 3. 删除选课关系
        studentCourseRepository.delete(existing.get());

        // 4. 成功响应
        response.put("status", "success");
        response.put("message", "学生" + student.getUsername() + "成功退课程" + course.getName());
        return ResponseEntity.ok(response);
    }
}