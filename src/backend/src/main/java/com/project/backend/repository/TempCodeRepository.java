package com.project.backend.repository;

import com.project.backend.model.TempCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 临时码数据访问层，继承JpaRepository实现基础CRUD操作
 * 自定义查询方法适配临时码验证与查询场景
 */
@Repository
public interface TempCodeRepository extends JpaRepository<TempCode, Long> {

    /**
     * 基础查询：按临时码 + 门禁位置ID精准定位
     * 用途：先判断临时码是否存在，再在Service层分步校验过期时间和剩余次数
     * @param code 6位临时码
     * @param locationId 门禁位置ID
     * @return 临时码实体（Optional包装，避免空指针）
     */
    Optional<TempCode> findByCodeAndLocationId(String code, Long locationId);

    /**
     * 复合查询：查询有效临时码（未过期 + 剩余次数>0）
     * 用途：直接筛选符合业务有效性的临时码，简化批量查询场景
     * @param code 6位临时码
     * @param locationId 门禁位置ID
     * @param now 当前时间（用于判断是否过期）
     * @param remainingUses 剩余次数阈值（固定传入0，判断>0）
     * @return 有效临时码实体（Optional包装）
     */
    Optional<TempCode> findByCodeAndLocationIdAndExpiresAtAfterAndRemainingUsesGreaterThan(
            String code,
            Long locationId,
            LocalDateTime now,
            Integer remainingUses
    );
}