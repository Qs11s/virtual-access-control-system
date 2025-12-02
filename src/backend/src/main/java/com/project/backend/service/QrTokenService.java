package com.project.backend.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class QrTokenService {

    // 生成随机的 8 位 Token
    public String generateToken() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
