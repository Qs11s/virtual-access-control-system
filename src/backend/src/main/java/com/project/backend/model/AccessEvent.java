package com.project.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_events")
public class AccessEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== 补充缺失的 user 属性（核心修复点）==========
    @ManyToOne
    private User user;

    @ManyToOne
    @JoinColumn(name = "location_id") // 显式指定列名，避免重复映射
    private Location location;

    private LocalDateTime accessTime;

    private boolean allowed;

    private String method;

    // 新增 status 属性（无冲突）
    private String status;

    // ========== 原有 getter/setter 完整保留 ==========
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user; // 现在有 user 属性，不会报错
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
        this.user = user; // 现在有 user 属性，不会报错
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

    // ========== 适配 Controller 调用的方法（无冗余属性）==========
    public void setLocationId(Long locationId) {
        if (locationId != null) {
            Location location = new Location();
            location.setId(locationId); // 假设 Location 类有 id 属性和 setId 方法
            this.location = location;
        }
    }

    public void setAccessMethod(String accessMethod) {
        this.method = accessMethod;
    }

    public void setStatus(String status) {
        this.status = status;
        if ("允许".equals(status) || "success".equals(status)) {
            this.allowed = true;
        } else if ("拒绝".equals(status) || "failed".equals(status)) {
            this.allowed = false;
        }
    }

    // ========== status 的 getter 方法 ==========
    public String getStatus() {
        return status;
    }
}