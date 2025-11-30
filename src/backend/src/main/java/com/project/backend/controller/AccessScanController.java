package com.project.backend.controller;

import com.project.backend.model.AccessEvent;
import com.project.backend.model.Location;
import com.project.backend.model.User;
import com.project.backend.repository.AccessEventRepository;
import com.project.backend.repository.LocationRepository;
import com.project.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/access")
public class AccessScanController {

    private final AccessEventRepository accessEventRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public AccessScanController(
            AccessEventRepository accessEventRepository,
            LocationRepository locationRepository,
            UserRepository userRepository) {
        this.accessEventRepository = accessEventRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/scan")
    public ResponseEntity<?> scan(@RequestBody Map<String, Object> payload,
                                  @AuthenticationPrincipal UserDetails userDetails) {

        Long locationId = Long.valueOf(payload.get("locationId").toString());
        String method = payload.get("method").toString();
        String qrToken = payload.containsKey("qrToken") ? payload.get("qrToken").toString() : "";

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        boolean allowed = true;

        if (method.equalsIgnoreCase("QR")) {
            allowed = location.getQrToken().equals(qrToken);
        }

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow();

        AccessEvent event = new AccessEvent();
        event.setUser(user);
        event.setLocation(location);
        event.setAccessTime(LocalDateTime.now());
        event.setMethod(method);
        event.setAllowed(allowed);
        accessEventRepository.save(event);

        if (!allowed) {
            return ResponseEntity.status(403)
                    .body(Map.of("allowed", false, "message", "Access denied"));
        }

        return ResponseEntity.ok(Map.of("allowed", true, "message", "Access granted"));
    }
}

