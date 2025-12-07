package com.project.backend.controller;

import com.project.backend.model.User;
import com.project.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    // 1. 分页查询所有用户
    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findAll(pageable);
        return ResponseEntity.ok(users);
    }

    // 2. 查询单个用户
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 3. 创建指定角色的用户
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // 确保角色合法（STUDENT/TEACHER/ADMIN）
        if (user.getRole() == null || (!"STUDENT".equals(user.getRole()) 
                && !"TEACHER".equals(user.getRole()) && !"ADMIN".equals(user.getRole()))) {
            user.setRole("STUDENT"); // 默认学生
        }
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    // 4. 更新用户信息（含角色）
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optionalUser.get();
        user.setUsername(userDetails.getUsername());
        user.setPassword(userDetails.getPassword()); // 注意：实际项目需加密密码
        // 校验角色合法性
        if (userDetails.getRole() != null && ("STUDENT".equals(userDetails.getRole()) 
                || "TEACHER".equals(userDetails.getRole()) || "ADMIN".equals(userDetails.getRole()))) {
            user.setRole(userDetails.getRole());
        }
        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }
}