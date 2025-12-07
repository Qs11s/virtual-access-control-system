package com.project.backend.repository;

import com.project.backend.model.TempCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TempCodeRepository extends JpaRepository<TempCode, Long> {
    Optional<TempCode> findByCodeAndLocationIdAndExpiresAtAfterAndRemainingUsesGreaterThan(
            String code, Long locationId, LocalDateTime now, Integer remainingUses);
}