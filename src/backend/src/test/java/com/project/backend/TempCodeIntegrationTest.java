package com.project.backend;

import com.project.backend.controller.TempCodeController.TempCodeGenerateRequest;
import com.project.backend.controller.TempCodeController.TempCodeVerifyRequest;
import com.project.backend.model.Location;
import com.project.backend.model.TempCode;
import com.project.backend.model.User;
import com.project.backend.repository.LocationRepository;
import com.project.backend.repository.TempCodeRepository;
import com.project.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TempCodeIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(TempCodeIntegrationTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TempCodeRepository tempCodeRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private static final int INITIAL_USES = 3;
    private Long testLocationId;
    private Long testOwnerId;

    /**
     * 测试前置方法：创建测试数据
     */
    @BeforeEach
    public void setupTestData() {
        // 清理旧数据
        tempCodeRepository.deleteAll();
        locationRepository.deleteAll();
        userRepository.deleteAll();
        
        // 1. 创建测试用户（Owner）
        User owner = new User();
        owner.setUsername("test_owner");
        owner.setPassword(passwordEncoder.encode("password123"));
        owner.setRole("ROLE_STUDENT");
        User savedOwner = userRepository.save(owner);
        this.testOwnerId = savedOwner.getId();
        logger.info("===== 创建测试用户 =====");
        logger.info("用户ID: {}, 用户名: {}", testOwnerId, "test_owner");
        
        // 2. 创建测试Location
        Location location = new Location();
        location.setCode("TEST_LOC_" + UUID.randomUUID().toString().substring(0, 8));
        location.setName("Test Location");
        location.setQrToken("initial-test-token");
        location.setAddress("Test Address");
        Location savedLocation = locationRepository.save(location);
        this.testLocationId = savedLocation.getId();
        logger.info("===== 创建测试Location =====");
        logger.info("Location ID: {}, 编码: {}, 名称: {}", 
                testLocationId, location.getCode(), location.getName());
    }

    /**
     * 核心测试方法：验证临时码使用3次后返回拒绝
     */
    @Test
    public void testTempCode_After3Uses_ReturnDeny() throws Exception {
        // 1. 构建临时码生成请求
        TempCodeGenerateRequest generateRequest = new TempCodeGenerateRequest();
        generateRequest.setLocationId(testLocationId);
        generateRequest.setOwnerId(testOwnerId);
        generateRequest.setValidMinutes(30); // 有效期30分钟
        generateRequest.setRemainingUses(INITIAL_USES); // 初始可用次数3次

        // 2. 调用接口生成临时码
        String tempCodeResponse = mockMvc.perform(post("/access/temp-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(generateRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // 3. 解析返回的临时码
        String code = objectMapper.readTree(tempCodeResponse).get("code").asText();
        logger.info("===== 生成临时码成功 =====");
        logger.info("临时码：{}", code);

        // 4. 构建临时码验证请求
        TempCodeVerifyRequest verifyRequest = new TempCodeVerifyRequest();
        verifyRequest.setLocationId(testLocationId);
        verifyRequest.setCode(code);

        // 5. 连续验证3次（预期均返回allow）
        for (int i = 0; i < INITIAL_USES; i++) {
            logger.info("===== 第{}次验证临时码 =====", i + 1);
            mockMvc.perform(post("/access/temp-code/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(verifyRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("allow"));
            logger.info("第{}次验证结果：allow（符合预期）", i + 1);
        }

        // 6. 第4次验证（预期返回deny，提示次数用尽）
        logger.info("===== 第4次验证临时码（预期拒绝） =====");
        mockMvc.perform(post("/access/temp-code/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("deny"))
                .andExpect(jsonPath("$.reason").value("密码无效/已过期/次数用尽"));
        logger.info("第4次验证结果:deny(符合预期)");

        // 7. 断言数据库中临时码剩余次数为0
        TempCode usedTempCode = tempCodeRepository.findByCodeAndLocationIdAndExpiresAtAfterAndRemainingUsesGreaterThan(
                code, testLocationId, LocalDateTime.now(), -1
        ).orElseThrow(() -> new RuntimeException("临时码不存在，请检查生成逻辑"));
        
        assertEquals(0, usedTempCode.getRemainingUses());
        logger.info("===== 数据库断言通过 =====");
        logger.info("临时码剩余次数：{}(预期:0)", usedTempCode.getRemainingUses());
    }
}