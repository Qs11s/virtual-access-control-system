package com.project.backend.controller;

import com.project.backend.dto.LoginRequest;
import com.project.backend.model.User;
import com.project.backend.repository.UserRepository;
import com.project.backend.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtUtil jwtUtil,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody LoginRequest request) {
        Optional<User> existing = userRepository.findFirstByUsernameOrderByIdDesc(request.getUsername());
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        String role = resolveRoleFromUsername(request.getUsername());
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        Optional<User> optionalUser = userRepository.findFirstByUsernameOrderByIdDesc(request.getUsername());
        if (optionalUser.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        User user = optionalUser.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        String token = jwtUtil.generateToken(user.getUsername());
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("token", token);
        return ResponseEntity.ok(successResponse);
    }

    private String resolveRoleFromUsername(String username) {
        if (username == null) {
            return "ROLE_STUDENT";
        }
        String u = username.toLowerCase();
        if (u.startsWith("admin")) {
            return "ROLE_ADMIN";
        }
        if (u.startsWith("teacher")) {
            return "ROLE_TEACHER";
        }
        return "ROLE_STUDENT";
    }
}
