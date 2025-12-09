package com.project.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "USERS")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @Column(nullable = false)
    private String role = "ROLE_STUDENT";

    public User() {
        this.role = "ROLE_STUDENT";
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.role = "ROLE_STUDENT";
    }

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        setRole(role);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        if (role == null || role.isEmpty()) {
            return;
        }
        if (role.startsWith("ROLE_")) {
            this.role = role;
        } else if ("STUDENT".equals(role) || "TEACHER".equals(role) || "ADMIN".equals(role)) {
            this.role = "ROLE_" + role;
        }
    }
}
