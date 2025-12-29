package com.project.backend.controller;

import com.project.backend.model.Course;
import com.project.backend.model.SessionEntity;
import com.project.backend.repository.CourseRepository;
import com.project.backend.repository.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teacher")
public class TeacherCourseController {

    private final CourseRepository courseRepository;
    private final SessionRepository sessionRepository;

    public TeacherCourseController(CourseRepository courseRepository,
                                   SessionRepository sessionRepository) {
        this.courseRepository = courseRepository;
        this.sessionRepository = sessionRepository;
    }

    @GetMapping("/courses/my")
    public ResponseEntity<List<Map<String, Object>>> myCourses(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        boolean isTeacher = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_TEACHER"));

        if (!isTeacher) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only teachers can view their courses");
        }

        String username = userDetails.getUsername();

        List<Course> courses = courseRepository.findByTeacher_Username(username);

        List<Map<String, Object>> result = courses.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("code", c.getCode());
            m.put("description", c.getDescription());
            return m;
        }).toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/courses/{courseId}/sessions")
    public ResponseEntity<List<Map<String, Object>>> courseSessions(@PathVariable Long courseId,
                                                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        String username = userDetails.getUsername();

        boolean isTeacher = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_TEACHER"));

        if (!isTeacher) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only teachers can view sessions");
        }

        Course course = courseRepository.findByIdAndTeacher_Username(courseId, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Course not owned by this teacher"));

        List<SessionEntity> sessions = sessionRepository.findByCourse_IdOrderByStartTimeAsc(course.getId());

        List<Map<String, Object>> result = sessions.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("courseId", s.getCourse().getId());
            m.put("locationId", s.getLocation() != null ? s.getLocation().getId() : null);
            m.put("startTime", s.getStartTime());
            m.put("endTime", s.getEndTime());
            return m;
        }).toList();

        return ResponseEntity.ok(result);
    }
}
