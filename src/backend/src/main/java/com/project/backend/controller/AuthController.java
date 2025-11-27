package com.project.backend.controller;

import com.project.backend.dto.LoginRequest;
import com.project.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody LoginRequest request) {
        String result = authService.register(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
public String login(@RequestBody LoginRequest request) {
    return authService.login(request.getUsername(), request.getPassword());
}
}
