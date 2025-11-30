package com.project.backend.service;

import com.project.backend.dto.AccessRecordResponse;
import com.project.backend.model.AccessEvent;
import com.project.backend.repository.AccessEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccessEventService {

    private final AccessEventRepository accessEventRepository;

    public List<AccessRecordResponse> getUserRecords(Long userId) {

        List<AccessEvent> events =
                accessEventRepository.findByUserIdOrderByAccessTimeDesc(userId);

        return events.stream().map(event -> {
            var location = event.getLocation();
            return new AccessRecordResponse(
                    location != null ? location.getName() : "Unknown",
                    location != null ? location.getCode() : "Unknown",
                    event.getAccessTime().toString(),
                    event.getMethod(),
                    event.isAllowed()
            );
        }).collect(Collectors.toList());
    }
}
