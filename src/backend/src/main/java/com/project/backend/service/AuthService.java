package com.project.backend.service;

import com.project.backend.model.User;
import com.project.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public String register(String username, String password) {
        if (userRepo.findByUsername(username).isPresent()) {
            return "Username already exists!";
        }
        String hashedPassword = passwordEncoder.encode(password);

        User user = new User(username, password);
        userRepo.save(user);
        return "Register success!";
    }

   public String login(String username, String password) {
    return userRepo.findByUsername(username)
        .map(user -> user.getPassword().equals(password)
            ? "TOKEN:" + jwtUtil.generateToken(username)
            : "Wrong password!"
        )
        .orElse("User not found!");
}

