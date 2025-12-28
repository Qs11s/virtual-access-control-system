package com.project.backend.service;

import com.project.backend.model.User;
import com.project.backend.repository.UserRepository;
import com.project.backend.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String register(String username, String password) {
        if (userRepo.findFirstByUsernameOrderByIdDesc(username).isPresent()) {
            return "Username already exists!";
        }
        User user = new User(username, passwordEncoder.encode(password));
        userRepo.save(user);
        return "Register success!";
    }

    public String login(String username, String password) {
        return userRepo.findFirstByUsernameOrderByIdDesc(username)
                .map(user -> passwordEncoder.matches(password, user.getPassword())
                        ? jwtUtil.generateToken(username)
                        : "Wrong password!")
                .orElse("User not found!");
    }
}
