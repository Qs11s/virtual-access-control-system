package com.project.backend.controller;

import com.project.backend.dto.AttendanceResponse;
import com.project.backend.dto.CourseDto;
import com.project.backend.dto.SessionDto;
import com.project.backend.model.Attendance;
import com.project.backend.model.Course;
import com.project.backend.model.SessionEntity;
import com.project.backend.repository.AttendanceRepository;
import com.project.backend.repository.CourseRepository;
import com.project.backend.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SessionRepository sessionRepository;

    // 新增：注入考勤仓库（支持课程考勤查询功能）
    @Autowired
    private AttendanceRepository attendanceRepository;

    @GetMapping
    public ResponseEntity<List<CourseDto>> getAllCourses() {
        List<CourseDto> courseDtos = courseRepository.findAll().stream()
                .map(this::convertToCourseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(courseDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDto> getCourseById(@PathVariable Long id) {
        return courseRepository.findById(id)
                .map(course -> ResponseEntity.ok(convertToCourseDto(course)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CourseDto> createCourse(@RequestBody CourseDto courseDto) {
        Course course = new Course();
        course.setName(courseDto.getName());
        course.setTeacher(courseDto.getTeacher());
        course.setDescription(courseDto.getDescription());
        
        // 生成课程代码（如果前端没有提供）
        if (courseDto.getCode() == null || courseDto.getCode().trim().isEmpty()) {
            // 生成简单的课程代码，例如：基于名称的缩写
            String code = generateCourseCode(courseDto.getName());
            course.setCode(code);
        } else {
            course.setCode(courseDto.getCode());
        }
        
        Course savedCourse = courseRepository.save(course);
        return ResponseEntity.ok(convertToCourseDto(savedCourse));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable Long id, @RequestBody CourseDto courseDto) {
        return courseRepository.findById(id)
                .map(existingCourse -> {
                    existingCourse.setName(courseDto.getName());
                    existingCourse.setTeacher(courseDto.getTeacher());
                    existingCourse.setDescription(courseDto.getDescription());
                    
                    // 更新课程代码（如果提供）
                    if (courseDto.getCode() != null && !courseDto.getCode().trim().isEmpty()) {
                        existingCourse.setCode(courseDto.getCode());
                    }
                    
                    Course updatedCourse = courseRepository.save(existingCourse);
                    return ResponseEntity.ok(convertToCourseDto(updatedCourse));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        if (courseRepository.existsById(id)) {
            courseRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/sessions")
    public ResponseEntity<List<SessionDto>> getSessionsByCourseId(@PathVariable Long id) {
        List<SessionDto> sessionDtos = sessionRepository.findByCourseId(id).stream()
                .map(this::convertToSessionDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(sessionDtos);
    }

    @PostMapping("/sessions")
    public ResponseEntity<SessionDto> createSession(@RequestBody SessionDto sessionDto) {
        SessionEntity session = new SessionEntity();
        session.setCourseId(sessionDto.getCourseId());
        session.setStartTime(sessionDto.getStartTime());
        session.setEndTime(sessionDto.getEndTime());
        SessionEntity savedSession = sessionRepository.save(session);
        return ResponseEntity.ok(convertToSessionDto(savedSession));
    }

    @PutMapping("/sessions/{id}")
    public ResponseEntity<SessionDto> updateSession(@PathVariable Long id, @RequestBody SessionDto sessionDto) {
        return sessionRepository.findById(id)
                .map(existingSession -> {
                    existingSession.setStartTime(sessionDto.getStartTime());
                    existingSession.setEndTime(sessionDto.getEndTime());
                    SessionEntity updatedSession = sessionRepository.save(existingSession);
                    return ResponseEntity.ok(convertToSessionDto(updatedSession));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        if (sessionRepository.existsById(id)) {
            sessionRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // 新增：按课程ID查询考勤明细（所有会话的考勤记录）
    @GetMapping("/{id}/attendance")
    public ResponseEntity<List<AttendanceResponse>> getCourseAttendance(@PathVariable Long id) {
        // 验证课程是否存在
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        // 查询课程下所有会话
        List<SessionEntity> sessions = sessionRepository.findByCourseId(id);
        if (sessions.isEmpty()) {
            return ResponseEntity.ok(List.of()); // 无会话时返回空列表
        }

        // 汇总所有会话的考勤记录并转换为AttendanceResponse
        List<AttendanceResponse> courseAttendance = sessions.stream()
                .flatMap(session -> attendanceRepository.findBySession(session).stream())
                .map(attendance -> {
                    AttendanceResponse response = new AttendanceResponse();
                    response.setId(attendance.getId());
                    response.setSessionId(attendance.getSession().getId());
                    response.setCheckInTime(attendance.getCheckInTime());
                    response.setCheckOutTime(attendance.getCheckOutTime());
                    response.setStatus(attendance.getStatus());
                    return response;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(courseAttendance);
    }

    // 新增：按课程ID汇总考勤（各会话出勤人数统计）
    @GetMapping("/{id}/attendance/summary")
    public ResponseEntity<Map<String, Object>> getCourseAttendanceSummary(@PathVariable Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        List<SessionEntity> sessions = sessionRepository.findByCourseId(id);
        Map<String, Object> summary = new HashMap<>();
        summary.put("courseId", id);
        summary.put("courseName", course.getName());

        // 统计每个会话的出勤人数
        Map<Long, Integer> sessionAttendanceCount = sessions.stream()
                .collect(Collectors.toMap(
                        SessionEntity::getId,
                        session -> attendanceRepository.findBySession(session).size()
                ));
        summary.put("sessionAttendanceCount", sessionAttendanceCount);

        return ResponseEntity.ok(summary);
    }

    // 生成课程代码的辅助方法
    private String generateCourseCode(String courseName) {
        if (courseName == null || courseName.trim().isEmpty()) {
            return "COURSE" + System.currentTimeMillis();
        }
        
        // 简单的实现：取每个单词的首字母
        String[] words = courseName.split("\\s+");
        StringBuilder code = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                code.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        
        // 如果生成的代码太短，添加时间戳
        if (code.length() < 3) {
            code.append("_").append(System.currentTimeMillis() % 10000);
        }
        
        return code.toString();
    }

    private CourseDto convertToCourseDto(Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setCode(course.getCode()); // 添加code字段
        dto.setTeacher(course.getTeacher());
        dto.setDescription(course.getDescription());
        return dto;
    }

    private SessionDto convertToSessionDto(SessionEntity session) {
        SessionDto dto = new SessionDto();
        dto.setId(session.getId());
        dto.setCourseId(session.getCourseId());
        Course course = courseRepository.findById(session.getCourseId()).orElse(null);
        if (course != null) {
            dto.setCourseName(course.getName());
        }
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        return dto;
    }
}