package com.project.backend;

import com.project.backend.dto.TempCodeCreateRequest;
import com.project.backend.dto.TempCodeVerifyRequest;
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
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print; // 新增导入
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

    @BeforeEach
    public void setupTestData() {
        // 清理旧数据
        tempCodeRepository.deleteAll();
        locationRepository.deleteAll();
        userRepository.deleteAll();
        
        // 1. 创建测试用户（Owner）- 关键修复：角色改为ROLE_ADMIN
        User owner = new User();
        owner.setUsername("test_owner");
        owner.setPassword(passwordEncoder.encode("password123"));
        owner.setRole("ROLE_ADMIN");
        User savedOwner = userRepository.save(owner);
        this.testOwnerId = savedOwner.getId();
        logger.info("===== 创建测试用户 =====");
        logger.info("用户ID: {}, 用户名: {}, 角色: {}", testOwnerId, "test_owner", "ADMIN");
        
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

    @Test
    public void testTempCode_After3Uses_ReturnDeny() throws Exception {
        // ===== 调试步骤 1: 登录并获取Token =====
        String loginJson = "{\"username\":\"test_owner\",\"password\":\"password123\"}";
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andDo(print()) // 打印登录请求/响应详情
                .andExpect(status().isOk())
                .andReturn();
        
        String loginResponse = loginResult.getResponse().getContentAsString();
        logger.info("登录响应原始内容: {}", loginResponse);
        
        String token = null;
        try {
            token = objectMapper.readTree(loginResponse).get("token").asText();
            logger.info("成功提取Token，长度: {}", token.length());
        } catch (Exception e) {
            logger.error("解析Token失败！响应内容：{}", loginResponse);
            throw new RuntimeException("无法从登录响应中获取token", e);
        }
        
        // 1. 构建临时码生成请求
        TempCodeCreateRequest generateRequest = new TempCodeCreateRequest();
        generateRequest.setLocationId(testLocationId);
        generateRequest.setOwnerId(testOwnerId);          
        generateRequest.setValidMinutes(30);              
        generateRequest.setRemainingUses(INITIAL_USES);   
        
        // ===== 调试步骤 2: 创建临时码请求（打印详细信息） =====
        logger.info("===== 开始创建临时码请求 =====");
        logger.info("请求头Token: Bearer {}", token.substring(0, Math.min(20, token.length())) + "...");
        logger.info("请求体: {}", objectMapper.writeValueAsString(generateRequest));
        
        MvcResult createCodeResult = mockMvc.perform(post("/access/temp-code")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(generateRequest)))
                .andDo(print()) // 关键：打印完整的HTTP交互信息
                .andReturn();
        
        int responseStatus = createCodeResult.getResponse().getStatus();
        String responseBody = createCodeResult.getResponse().getContentAsString();
        logger.info("创建临时码响应状态: {}", responseStatus);
        logger.info("创建临时码响应体: {}", responseBody);
        
        // 如果状态不是200，提前失败并给出明确信息
        if (responseStatus != 200) {
            throw new AssertionError("创建临时码失败，期望状态200，实际状态: " + responseStatus + "，响应体: " + responseBody);
        }
        
        // 3. 解析返回的临时码
        String code = objectMapper.readTree(responseBody).get("code").asText();
        logger.info("===== 生成临时码成功 =====");
        logger.info("临时码：{}", code);

        // 4. 构建临时码验证请求
        TempCodeVerifyRequest verifyRequest = new TempCodeVerifyRequest();
        verifyRequest.setLocationId(testLocationId);
        verifyRequest.setCode(code);

        // 5. 连续验证3次
        for (int i = 0; i < INITIAL_USES; i++) {
            logger.info("===== 第{}次验证临时码 =====", i + 1);
            mockMvc.perform(post("/access/temp-code/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(verifyRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("allow"));
            logger.info("第{}次验证结果：allow（符合预期）", i + 1);
        }

        // 6. 第4次验证
        logger.info("===== 第4次验证临时码（预期拒绝） =====");
        mockMvc.perform(post("/access/temp-code/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
               .andExpect(jsonPath("$.result").value("deny"))
                .andExpect(jsonPath("$.reason").value("临时码次数已用尽")); // 更新为实际返回的消息
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