package com.project.backend.service;

import com.project.backend.model.Location;
import com.project.backend.repository.LocationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QrTokenScheduler {

    private final LocationRepository locationRepository;
    private final QrTokenService qrTokenService;

    public QrTokenScheduler(LocationRepository locationRepository, QrTokenService qrTokenService) {
        this.locationRepository = locationRepository;
        this.qrTokenService = qrTokenService;
    }

    @Scheduled(initialDelay = 1000, fixedRate = 30000)
    public void refreshQrTokens() {
        List<Location> locations = locationRepository.findAll();
        for (Location location : locations) {
            String newQrToken = qrTokenService.generateToken();
            location.setQrToken(newQrToken);
            locationRepository.save(location);
        }
        System.out.println("[QR Scheduler] QR tokens refreshed.");
    }
}
