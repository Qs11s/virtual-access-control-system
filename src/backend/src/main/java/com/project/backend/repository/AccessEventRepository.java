package com.project.backend.repository;

import com.project.backend.model.AccessEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AccessEventRepository extends JpaRepository<AccessEvent, Long> {

    /**
     * 按用户ID查询访问记录，按访问时间倒序排列
     */
    List<AccessEvent> findByUserIdOrderByAccessTimeDesc(Long userId);

    /**
     * 多条件筛选访问记录，支持分页
     */
    @Query("SELECT e FROM AccessEvent e WHERE " +
            "(:#{#userId} IS NULL OR e.user.id = :userId) AND " +
            "(:#{#locationId} IS NULL OR e.location.id = :locationId) AND " +
            "(:#{#fromTime} IS NULL OR e.accessTime >= :fromTime) AND " +
            "(:#{#toTime} IS NULL OR e.accessTime <= :toTime) " +
            "ORDER BY e.accessTime DESC")
    Page<AccessEvent> findFilteredAccessEvents(
            @Param("userId") Long userId,
            @Param("locationId") Long locationId,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime,
            Pageable pageable
    );
}
