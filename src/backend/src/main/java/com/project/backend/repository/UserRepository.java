package com.project.backend.repository;

import com.project.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    // 新增：按用户ID和角色查询（验证是否为学生/教师/管理员）
    Optional<User> findByIdAndRole(Long id, String role);
}