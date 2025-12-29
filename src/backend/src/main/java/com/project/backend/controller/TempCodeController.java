package com.project.backend.controller;

import com.project.backend.dto.TempCodeCreateRequest;
import com.project.backend.dto.TempCodeCreateResponse;
import com.project.backend.dto.TempCodeVerifyRequest;
import com.project.backend.dto.TempCodeVerifyResponse;
import com.project.backend.service.TempCodeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/access/temp-code")
public class TempCodeController {

    @Autowired
    private TempCodeService tempCodeService;

    @PostMapping
    public ResponseEntity<TempCodeCreateResponse> generateTempCode(
            @Valid @RequestBody TempCodeCreateRequest request
    ) {
        TempCodeCreateResponse response = tempCodeService.createTempCode(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<TempCodeVerifyResponse> verifyTempCode(
            @Valid @RequestBody TempCodeVerifyRequest request
    ) {
        TempCodeVerifyResponse response = tempCodeService.verifyTempCode(request);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", e.getMessage());
        errorResponse.put("status", "error");
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
