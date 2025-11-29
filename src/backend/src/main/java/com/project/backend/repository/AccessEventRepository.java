package com.project.backend.repository;

import com.project.backend.model.AccessEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessEventRepository extends JpaRepository<AccessEvent, Long> {
}
