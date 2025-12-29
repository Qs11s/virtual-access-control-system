package com.project.backend.controller;

import com.project.backend.model.AccessEvent;
import com.project.backend.repository.AccessEventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/access")
public class AdminAccessLogController {

    private final AccessEventRepository accessEventRepository;

    public AdminAccessLogController(AccessEventRepository accessEventRepository) {
        this.accessEventRepository = accessEventRepository;
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<Map<String, Object>>> getByLocation(@PathVariable Long locationId) {
        List<AccessEvent> list = accessEventRepository
                .findTop50ByLocation_IdOrderByAccessTimeDesc(locationId);

        List<Map<String, Object>> result = list.stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("userId", e.getUser() != null ? e.getUser().getId() : null);
            m.put("username", e.getUser() != null ? e.getUser().getUsername() : null);
            m.put("locationId", e.getLocation() != null ? e.getLocation().getId() : null);
            m.put("locationCode", e.getLocation() != null ? e.getLocation().getCode() : null);
            m.put("accessTime", e.getAccessTime());
            m.put("method", e.getMethod());
            m.put("allowed", e.isAllowed());
            return m;
        }).toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<Map<String, Object>>> getByUser(@PathVariable String username) {
        List<AccessEvent> list = accessEventRepository
                .findTop50ByUser_UsernameOrderByAccessTimeDesc(username);

        List<Map<String, Object>> result = list.stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("userId", e.getUser() != null ? e.getUser().getId() : null);
            m.put("username", e.getUser() != null ? e.getUser().getUsername() : null);
            m.put("locationId", e.getLocation() != null ? e.getLocation().getId() : null);
            m.put("locationCode", e.getLocation() != null ? e.getLocation().getCode() : null);
            m.put("accessTime", e.getAccessTime());
            m.put("method", e.getMethod());
            m.put("allowed", e.isAllowed());
            return m;
        }).toList();

        return ResponseEntity.ok(result);
    }
}
