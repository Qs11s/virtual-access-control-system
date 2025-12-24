package com.project.backend.controller;

import com.project.backend.dto.StudentCourseResponse;
import com.project.backend.service.StudentCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/courses")
public class StudentCourseController {

    @Autowired
    private StudentCourseService studentCourseService;

    /**
     * 选课接口：POST /admin/courses/{courseId}/students/{studentId}
     */
    @PostMapping("/{courseId}/students/{studentId}")
    public ResponseEntity<StudentCourseResponse> enrollStudent(
            @PathVariable Long courseId,
            @PathVariable Long studentId
    ) {
        StudentCourseResponse response = studentCourseService.enrollStudent(studentId, courseId);
        if ("success".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 退课接口：DELETE /admin/courses/{courseId}/students/{studentId}
     */
    @DeleteMapping("/{courseId}/students/{studentId}")
    public ResponseEntity<StudentCourseResponse> unenrollStudent(
            @PathVariable Long courseId,
            @PathVariable Long studentId
    ) {
        StudentCourseResponse response = studentCourseService.unenrollStudent(studentId, courseId);
        return ResponseEntity.ok(response);
    }
}