package com.project.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "Backend is running.";
    }

    @GetMapping("/home")
    public String home() {
        return "Hello, you are authenticated!";
    }
}
