package com.project.backend.controller;

import com.project.backend.model.AccessEvent;
import com.project.backend.model.Attendance;
import com.project.backend.repository.AccessEventRepository;
import com.project.backend.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminSystemController {

    @Autowired
    private AccessEventRepository accessEventRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    // 1. 分页查看最近门禁记录
    @GetMapping("/access-events")
    public ResponseEntity<Page<AccessEvent>> getAccessEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AccessEvent> events = accessEventRepository.findAll(pageable);
        return ResponseEntity.ok(events);
    }

    // 2. 全局考勤概览（按课程统计）
    @GetMapping("/attendance/overview")
    public ResponseEntity<Map<String, Object>> getAttendanceOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 示例：统计总考勤数、已签到数、未签到数（按课程分组）
        long totalAttendance = attendanceRepository.count();
        long presentCount = attendanceRepository.countByStatus("PRESENT");
        long absentCount = attendanceRepository.countByStatus("ABSENT");
        
        overview.put("total", totalAttendance);
        overview.put("present", presentCount);
        overview.put("absent", absentCount);
        
        // 可扩展：按课程统计（需Attendance关联Course）
        // List<Object[]> courseStats = attendanceRepository.countByCourseGroup();
        // overview.put("courseStats", courseStats);
        
        return ResponseEntity.ok(overview);
    }
}