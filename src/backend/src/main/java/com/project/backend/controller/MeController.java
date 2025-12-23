package com.project.backend.controller;

import com.project.backend.dto.AttendanceSummary;
import com.project.backend.dto.CourseDto;
import com.project.backend.model.Course;
import com.project.backend.model.StudentCourse;
import com.project.backend.model.User;
import com.project.backend.repository.AttendanceRepository;
import com.project.backend.repository.StudentCourseRepository;
import com.project.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/me")
public class MeController {

    private final StudentCourseRepository studentCourseRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;

    // 构造函数注入（与现有代码风格一致，无需@Autowired）
    public MeController(StudentCourseRepository studentCourseRepository,
                        UserRepository userRepository,
                        AttendanceRepository attendanceRepository) {
        this.studentCourseRepository = studentCourseRepository;
        this.userRepository = userRepository;
        this.attendanceRepository = attendanceRepository;
    }

    // 新增：GET /me/courses - 查询当前学生已选课程列表
    @GetMapping("/courses")
    public ResponseEntity<List<CourseDto>> getMyCourses(Authentication authentication) {
        // 获取当前登录用户
        String username = authentication.getName();
        User currentStudent = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 查询学生选课记录并转换为CourseDto
        List<CourseDto> myCourses = studentCourseRepository.findByStudent(currentStudent).stream()
                .map(StudentCourse::getCourse)
                .map(this::convertToCourseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(myCourses);
    }

    // 复用：GET /me/attendance - 查询当前学生考勤记录（与AttendanceController功能一致，统一路径）
    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceSummary>> getMyAttendance(Authentication authentication) {
        String username = authentication.getName();
        User currentStudent = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<AttendanceSummary> myAttendance = attendanceRepository.findByStudent(currentStudent).stream()
                .map(attendance -> new AttendanceSummary(
                        attendance.getId(),
                        attendance.getStudent().getId(),
                        attendance.getSession().getId(),
                        attendance.getCheckInTime(),
                        attendance.getCheckOutTime(),
                        attendance.getStatus()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(myAttendance);
    }

    // 私有方法：Course转CourseDto（与现有CourseController中的转换逻辑一致）
    private CourseDto convertToCourseDto(Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setTeacher(course.getTeacher());
        dto.setDescription(course.getDescription());
        return dto;
    }
}