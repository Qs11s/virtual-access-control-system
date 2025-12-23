package com.project.backend.controller;

import com.project.backend.model.AccessEvent;
import com.project.backend.model.Attendance;
import com.project.backend.repository.AccessEventRepository;
import com.project.backend.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminSystemController {

    @Autowired
    private AccessEventRepository accessEventRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    // 优化：支持过滤参数的门禁日志分页查询
    @GetMapping("/access-events")
    public ResponseEntity<Page<AccessEvent>> getAccessEvents(
            @RequestParam(required = false) Long userId, // 按用户ID过滤
            @RequestParam(required = false) Long locationId, // 按地点ID过滤
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromTime, // 开始时间
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toTime, // 结束时间
            @RequestParam(defaultValue = "0") int page, // 页码
            @RequestParam(defaultValue = "10") int size) { // 每页条数
        Pageable pageable = PageRequest.of(page, size);
        // 调用扩展后的过滤查询方法
        Page<AccessEvent> events = accessEventRepository.findFilteredAccessEvents(userId, locationId, fromTime, toTime, pageable);
        return ResponseEntity.ok(events);
    }

    // 原有考勤概览方法保留不变
    @GetMapping("/attendance/overview")
    public ResponseEntity<Map<String, Object>> getAttendanceOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        long totalAttendance = attendanceRepository.count();
        long presentCount = attendanceRepository.countByStatus("PRESENT");
        long absentCount = attendanceRepository.countByStatus("ABSENT");
        
        overview.put("total", totalAttendance);
        overview.put("present", presentCount);
        overview.put("absent", absentCount);
        
        return ResponseEntity.ok(overview);
    }
}