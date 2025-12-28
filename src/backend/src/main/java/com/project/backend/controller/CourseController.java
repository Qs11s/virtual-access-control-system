package com.project.backend.controller;

import com.project.backend.dto.AttendanceResponse;
import com.project.backend.dto.CourseDto;
import com.project.backend.dto.SessionDto;
import com.project.backend.model.Attendance;
import com.project.backend.model.Course;
import com.project.backend.model.SessionEntity;
import com.project.backend.model.StudentCourse;
import com.project.backend.model.User;
import com.project.backend.repository.AttendanceRepository;
import com.project.backend.repository.CourseRepository;
import com.project.backend.repository.SessionRepository;
import com.project.backend.repository.StudentCourseRepository;
import com.project.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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

    @Autowired
    private AttendanceRepository attendanceRepository;

    // 新增：选课关联仓库
    @Autowired
    private StudentCourseRepository studentCourseRepository;

    // 新增：用户仓库
    @Autowired
    private UserRepository userRepository;

    // 新增：我的课程接口
    @GetMapping("/my")
    public ResponseEntity<List<CourseDto>> getMyCourses(Authentication authentication) {
        // 1. 获取当前登录用户名
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        
        // 2. 查询用户信息
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在：" + username));
        
        // 3. 查询已选课程
        List<StudentCourse> studentCourses = studentCourseRepository.findByStudent(user);
        
        // 4. 转换为CourseDto
        List<CourseDto> courseDtos = studentCourses.stream()
                .map(sc -> convertToCourseDto(sc.getCourse()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(courseDtos);
    }

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
        
        if (courseDto.getCode() == null || courseDto.getCode().trim().isEmpty()) {
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

    @GetMapping("/{id}/attendance")
    public ResponseEntity<List<AttendanceResponse>> getCourseAttendance(@PathVariable Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        List<SessionEntity> sessions = sessionRepository.findByCourseId(id);
        if (sessions.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

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

    @GetMapping("/{id}/attendance/summary")
    public ResponseEntity<Map<String, Object>> getCourseAttendanceSummary(@PathVariable Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        List<SessionEntity> sessions = sessionRepository.findByCourseId(id);
        Map<String, Object> summary = new HashMap<>();
        summary.put("courseId", id);
        summary.put("courseName", course.getName());

        Map<Long, Integer> sessionAttendanceCount = sessions.stream()
                .collect(Collectors.toMap(
                        SessionEntity::getId,
                        session -> attendanceRepository.findBySession(session).size()
                ));
        summary.put("sessionAttendanceCount", sessionAttendanceCount);

        return ResponseEntity.ok(summary);
    }

    private String generateCourseCode(String courseName) {
        if (courseName == null || courseName.trim().isEmpty()) {
            return "COURSE" + System.currentTimeMillis();
        }
        
        String[] words = courseName.split("\\s+");
        StringBuilder code = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                code.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        
        if (code.length() < 3) {
            code.append("_").append(System.currentTimeMillis() % 10000);
        }
        
        return code.toString();
    }

    private CourseDto convertToCourseDto(Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setCode(course.getCode());
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