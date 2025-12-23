package com.project.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "locations") // 明确指定表名，避免歧义
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true) // 明确指定列名，与数据库一致
    private String code;

    @Column(name = "name", nullable = false) // 明确指定列名
    private String name;

    @Column(name = "qr_token") // 明确指定列名（对应数据库的qr_token）
    private String qrToken;

    @Column(name = "address") // 关键：明确指定列名=address，与data.sql一致
    private String address; // 无拼写错误，无额外注解干扰
}