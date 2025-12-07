package com.project.backend.controller;

import com.project.backend.dto.ScheduleItemDto;
import com.project.backend.model.SessionEntity;
import com.project.backend.model.Course; // 新增：导入缺失的Course类
import com.project.backend.repository.CourseRepository;
import com.project.backend.repository.SessionRepository;
import com.project.backend.repository.StudentCourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/me")
    public ResponseEntity<List<ScheduleItemDto>> getMySchedule(Authentication authentication) {
        String currentUsername = authentication.getName();
        List<SessionEntity> sessions = sessionRepository.findAll();
        List<ScheduleItemDto> scheduleItems = sessions.stream()
                .sorted((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime()))
                .map(this::convertToScheduleItemDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(scheduleItems);
    }

    private ScheduleItemDto convertToScheduleItemDto(SessionEntity session) {
        ScheduleItemDto dto = new ScheduleItemDto();
        dto.setDate(session.getStartTime().toLocalDate().atStartOfDay());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        
        // 关键：调用session.getCourseId()（已在SessionEntity中新增该方法）
        // 空值防护：避免courseId为null时调用findById报错
        Long courseId = session.getCourseId();
        if (courseId != null) {
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course != null) {
                dto.setCourseName(course.getName());
            }
        }
        
        return dto;
    }
}