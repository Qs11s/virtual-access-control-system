package com.project.backend.controller;

import com.project.backend.model.Location;
import com.project.backend.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/admin/locations")
public class AdminLocationController {

    @Autowired
    private LocationRepository locationRepository;

    // 1. 分页查询所有地点
    @GetMapping
    public ResponseEntity<Page<Location>> getAllLocations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Location> locations = locationRepository.findAll(pageable);
        return ResponseEntity.ok(locations);
    }

    // 2. 创建新地点
    @PostMapping
    public ResponseEntity<Location> createLocation(@RequestBody Location location) {
        Location savedLocation = locationRepository.save(location);
        return ResponseEntity.ok(savedLocation);
    }

    // 3. 更新地点信息
    @PutMapping("/{id}")
    public ResponseEntity<Location> updateLocation(@PathVariable Long id, @RequestBody Location locationDetails) {
        Optional<Location> optionalLocation = locationRepository.findById(id);
        if (optionalLocation.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Location location = optionalLocation.get();
        // 按Location实体的字段更新（示例：name、address等）
        location.setName(locationDetails.getName());
        location.setAddress(locationDetails.getAddress());
        Location updatedLocation = locationRepository.save(location);
        return ResponseEntity.ok(updatedLocation);
    }

    // 4. 删除地点
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        if (!locationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        locationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}