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

@RestController
public class VerifyController {

    private final AccessEventRepository accessEventRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public VerifyController(AccessEventRepository accessEventRepository,
                            LocationRepository locationRepository,
                            UserRepository userRepository) {
        this.accessEventRepository = accessEventRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    record VerifyRequest(String locationCode, String qrToken) {}

    @PostMapping("/verify/qr")
    public ResponseEntity<?> verify(@RequestBody VerifyRequest request,
                                    @AuthenticationPrincipal UserDetails userDetails) {

        Location location = locationRepository.findByCode(request.locationCode())
                .orElseThrow(() -> new RuntimeException("Location not found"));

        boolean allowed = location.getQrToken().equals(request.qrToken());

        User user = userRepository.findFirstByUsernameOrderByIdDesc(userDetails.getUsername())
                .orElseThrow();

        AccessEvent event = new AccessEvent();
        event.setUser(user);
        event.setLocation(location);
        event.setAccessTime(LocalDateTime.now());
        event.setMethod("QR");
        event.setAllowed(allowed);
        accessEventRepository.save(event);

        if (!allowed) {
            return ResponseEntity.status(403).body("{\"allowed\":false,\"message\":\"Invalid QR token\"}");
        }

        return ResponseEntity.ok("{\"allowed\":true,\"message\":\"Access granted\"}");
    }
}
