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

    /**
     * 管理员创建临时码
     */
    @PostMapping
    public ResponseEntity<TempCodeCreateResponse> generateTempCode(
            @Valid @RequestBody TempCodeCreateRequest request // 开启参数校验
    ) {
        TempCodeCreateResponse response = tempCodeService.createTempCode(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 验证临时码（公开访问）
     */
    @PostMapping("/verify")
    public ResponseEntity<TempCodeVerifyResponse> verifyTempCode(
            @Valid @RequestBody TempCodeVerifyRequest request // 开启参数校验
    ) {
        TempCodeVerifyResponse response = tempCodeService.verifyTempCode(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 局部异常处理（控制器内专用）
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", e.getMessage());
        errorResponse.put("status", "error");
        return ResponseEntity.badRequest().body(errorResponse);
    }
}