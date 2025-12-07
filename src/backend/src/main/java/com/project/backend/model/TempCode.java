package com.project.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TEMP_CODES")
public class TempCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 6)
    private String code;

    @Column(nullable = false)
    private Long locationId;

    private Long ownerId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Integer remainingUses;

    private LocalDateTime usedAt;

    public TempCode() {}

    public TempCode(String code, Long locationId, Long ownerId, LocalDateTime expiresAt, Integer remainingUses) {
        this.code = code;
        this.locationId = locationId;
        this.ownerId = ownerId;
        this.expiresAt = expiresAt;
        this.remainingUses = remainingUses;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public Integer getRemainingUses() { return remainingUses; }
    public void setRemainingUses(Integer remainingUses) { this.remainingUses = remainingUses; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
}