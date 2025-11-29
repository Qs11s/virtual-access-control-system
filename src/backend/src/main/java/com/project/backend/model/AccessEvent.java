package com.project.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_events")
public class AccessEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Location location;

    private LocalDateTime accessTime;

    private boolean allowed;

    private String method;

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Location getLocation() {
        return location;
    }

    public LocalDateTime getAccessTime() {
        return accessTime;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public String getMethod() {
        return method;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setAccessTime(LocalDateTime accessTime) {
        this.accessTime = accessTime;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
