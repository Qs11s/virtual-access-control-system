package com.project.backend.controller;

import com.project.backend.dto.CourseDto;
import com.project.backend.dto.SessionDto;
import com.project.backend.model.Course;
import com.project.backend.model.SessionEntity;
import com.project.backend.repository.CourseRepository;
import com.project.backend.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SessionRepository sessionRepository;

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

    private CourseDto convertToCourseDto(Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
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