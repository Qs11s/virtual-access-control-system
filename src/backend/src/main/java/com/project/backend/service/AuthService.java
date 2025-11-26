package com.project.backend.service;

import com.project.backend.model.User;
import com.project.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;

    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public String register(String username, String password) {
        if (userRepo.findByUsername(username).isPresent()) {
            return "Username already exists!";
        }

        User user = new User(username, password);
        userRepo.save(user);
        return "Register success!";
    }

    public String login(String username, String password) {
        return userRepo.findByUsername(username)
            .map(user -> user.getPassword().equals(password) ? "Login success!" : "Wrong password!")
            .orElse("User not found!");
    }
}
