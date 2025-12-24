package com.project.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_events")
public class AccessEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 访问用户（关联用户表）
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // 显式指定外键列，保证非空
    private User user;

    /**
     * 门禁位置（关联位置表）
     */
    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false) // 显式指定外键列，保证非空
    private Location location;

    /**
     * 访问时间
     */
    @Column(nullable = false)
    private LocalDateTime accessTime;

    /**
     * 是否允许访问
     */
    @Column(nullable = false)
    private boolean allowed;

    /**
     * 访问方式（如：TEMP_CODE、CARD、PASSWORD等）
     */
    @Column(nullable = false)
    private String method;

    /**
     * 访问状态描述（如：ALLOWED、DENIED）
     */
    private String status;

    // 无参构造器（JPA必需）
    public AccessEvent() {}

    // 全参构造器（便于快速创建）
    public AccessEvent(User user, Location location, LocalDateTime accessTime, boolean allowed, String method, String status) {
        this.user = user;
        this.location = location;
        this.accessTime = accessTime;
        this.allowed = allowed;
        this.method = method;
        this.status = status;
    }

    // 所有字段的getter/setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public LocalDateTime getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(LocalDateTime accessTime) {
        this.accessTime = accessTime;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        // 根据状态自动设置allowed字段
        if ("ALLOWED".equalsIgnoreCase(status) || "success".equalsIgnoreCase(status)) {
            this.allowed = true;
        } else if ("DENIED".equalsIgnoreCase(status) || "failed".equalsIgnoreCase(status)) {
            this.allowed = false;
        }
    }

    // 兼容旧代码的辅助方法（可选保留）
    public void setLocationId(Long locationId) {
        if (locationId != null) {
            Location location = new Location();
            location.setId(locationId);
            this.location = location;
        }
    }

    public void setAccessMethod(String accessMethod) {
        this.method = accessMethod;
    }
}