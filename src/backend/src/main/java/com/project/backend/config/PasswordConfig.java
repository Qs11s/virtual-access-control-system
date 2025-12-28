package com.project.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 1. 定义支持的加密算法（key=前缀，value=编码器）
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        // 优先用BCrypt（生产环境）
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        // 支持{noop}明文（测试环境）
        encoders.put("noop", NoOpPasswordEncoder.getInstance());

        // 2. 创建委托编码器，默认用bcrypt（可改为noop方便测试）
        DelegatingPasswordEncoder delegatingEncoder = new DelegatingPasswordEncoder("bcrypt", encoders);
        // 兼容无前缀的密码（可选：无前缀时按默认算法处理）
        delegatingEncoder.setDefaultPasswordEncoderForMatches(NoOpPasswordEncoder.getInstance());

        return delegatingEncoder;
    }
}