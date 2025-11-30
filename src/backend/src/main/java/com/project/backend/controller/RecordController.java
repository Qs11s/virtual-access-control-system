package com.project.backend.controller;

import com.project.backend.dto.AccessRecordResponse;
import com.project.backend.model.User;
import com.project.backend.service.AccessEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/records")
public class RecordController {

    private final AccessEventService accessEventService;

    @GetMapping("/my")
    public List<AccessRecordResponse> getMyRecords(@AuthenticationPrincipal User user) {
        return accessEventService.getUserRecords(user.getId());
    }
}
