package com.project.backend.repository;

import com.project.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findFirstByUsernameOrderByIdDesc(String username);

    Optional<User> findByIdAndRole(Long id, String role);

    Optional<User> findByUsername(String username);
}
