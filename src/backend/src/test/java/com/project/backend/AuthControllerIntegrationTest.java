package com.project.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.backend.model.User;
import com.project.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 新增：注入所有可能引用users表的Repository
    @Autowired
    private AccessEventRepository accessEventRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private TempCodeRepository tempCodeRepository;
    @Autowired
    private StudentCourseRepository studentCourseRepository;

    @BeforeEach
    public void setup() {
        // 关键修复：按照从子表到父表的顺序清理数据，避免外键约束冲突
        // 1. 先清理所有引用users表的子表
        tempCodeRepository.deleteAll();        // temp_codes表可能引用users(owner_id)
        accessEventRepository.deleteAll();     // access_events表引用users(user_id)
        attendanceRepository.deleteAll();      // attendance表引用users(student_id)
        studentCourseRepository.deleteAll();   // student_courses表引用users(student_id)

        // 2. 最后清理users表本身
        userRepository.deleteAll();
        
        // 创建测试用户
        User student = new User();
        student.setUsername("student1");
        student.setPassword(passwordEncoder.encode("student123"));
        student.setRole("ROLE_STUDENT");
        userRepository.save(student);
    }

    @Test
    public void testLogin_Success_ReturnToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("student1");
        loginRequest.setPassword("student123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    public void testLogin_UserNotFound_Return401() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("non_exist_user");
        loginRequest.setPassword("123456");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    // 登录请求体封装
    static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public String getPassword() { return password; }

        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }
    }
}