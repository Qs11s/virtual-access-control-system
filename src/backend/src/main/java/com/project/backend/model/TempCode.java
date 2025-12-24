package com.project.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 临时访问码实体类，对应数据库表 TEMP_CODES
 * 用于存储管理员创建的临时门禁访问码信息
 */
@Entity
@Table(name = "TEMP_CODES")
public class TempCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 6位数字临时码（唯一约束，确保不重复）
     */
    @Column(nullable = false, unique = true, length = 6)
    private String code;

    /**
     * 门禁位置ID（关联Location表的主键，非空）
     */
    @Column(nullable = false)
    private Long locationId;

    /**
     * 临时码归属用户ID（关联User表的主键，非空）
     */
    @Column(nullable = false)
    private Long ownerId;

    /**
     * 临时码有效分钟数（非空，记录有效期时长）
     */
    @Column(nullable = false)
    private Integer validMinutes;

    /**
     * 临时码过期时间（非空，由创建时间+validMinutes计算得出）
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * 剩余可使用次数（非空，默认为大于0的整数）
     */
    @Column(nullable = false)
    private Integer remainingUses;

    /**
     * 临时码最后一次使用时间（可选，记录最新使用轨迹）
     */
    private LocalDateTime usedAt;

    // 无参构造器（JPA 必需，不可删除，用于反射实例化）
    public TempCode() {}

    // 全参构造器（不含id，id由数据库自增生成）
    public TempCode(String code, Long locationId, Long ownerId, Integer validMinutes, LocalDateTime expiresAt, Integer remainingUses) {
        this.code = code;
        this.locationId = locationId;
        this.ownerId = ownerId;
        this.validMinutes = validMinutes;
        this.expiresAt = expiresAt;
        this.remainingUses = remainingUses;
    }

    // 所有字段的 getter/setter 方法（完整保留，确保属性可读写）
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Integer getValidMinutes() { return validMinutes; }
    public void setValidMinutes(Integer validMinutes) { this.validMinutes = validMinutes; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public Integer getRemainingUses() { return remainingUses; }
    public void setRemainingUses(Integer remainingUses) { this.remainingUses = remainingUses; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
}