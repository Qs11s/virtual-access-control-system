package com.project.backend.repository;

import com.project.backend.model.AccessEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccessEventRepository extends JpaRepository<AccessEvent, Long> {
    List<AccessEvent> findByUserIdOrderByAccessTimeDesc(Long userId);
}
