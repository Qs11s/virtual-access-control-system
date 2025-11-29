package com.project.backend.controller;

import com.project.backend.model.Location;
import com.project.backend.repository.LocationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/location")
public class LocationController {

    private final LocationRepository repo;

    public LocationController(LocationRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/create")
    public ResponseEntity<Location> create(@RequestBody Location location) {
        location.setQrToken(UUID.randomUUID().toString());
        repo.save(location);
        return ResponseEntity.ok(location);
    }

    @PostMapping("/refresh/{code}")
    public ResponseEntity<Location> refresh(@PathVariable String code) {
        Location location = repo.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        location.setQrToken(UUID.randomUUID().toString());
        repo.save(location);

        return ResponseEntity.ok(location);
    }

    @GetMapping("/all")
    public ResponseEntity<?> all() {
        return ResponseEntity.ok(repo.findAll());
    }
}
