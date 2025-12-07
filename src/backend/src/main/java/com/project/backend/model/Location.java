package com.project.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "locations")
@Data // 自动为所有字段生成get/set方法，包括新增的address
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String code;

    private String qrToken;

    // 新增：address字段（对应Controller中调用的getAddress()方法）
    private String address;
}