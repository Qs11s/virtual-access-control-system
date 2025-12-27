package com.project.backend;

import com.project.backend.dto.CourseDto;
import com.project.backend.model.User;
import com.project.backend.repository.*;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CourseSecurityIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(CourseSecurityIntegrationTest.class);

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

    private String adminToken;
    private String studentToken;

    @BeforeEach
    public void setupTestData() throws Exception {
        // 关键修复：按照从子表到父表的顺序清理数据
        // 1. 先清理所有引用users表的子表
        tempCodeRepository.deleteAll();
        accessEventRepository.deleteAll();
        attendanceRepository.deleteAll();
        studentCourseRepository.deleteAll();
        
        // 2. 最后清理users表本身
        userRepository.deleteAll();
        
        // 3. 创建管理员用户
        User admin = new User();
        admin.setUsername("admin2");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole("ROLE_ADMIN");
        userRepository.save(admin);
        
        // 4. 创建学生用户
        User student = new User();
        student.setUsername("student1");
        student.setPassword(passwordEncoder.encode("student123"));
        student.setRole("ROLE_STUDENT");
        userRepository.save(student);
        
        logger.info("===== 创建测试用户完成 =====");
        
        // 5. 获取管理员Token
        String adminLoginJson = "{\"username\":\"admin2\",\"password\":\"admin123\"}";
        MvcResult adminResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminLoginJson))
                .andExpect(status().isOk())
                .andReturn();
        
        String adminResponse = adminResult.getResponse().getContentAsString();
        this.adminToken = objectMapper.readTree(adminResponse).get("token").asText();
        logger.info("===== 管理员登录成功 =====");
        logger.info("管理员token: {}", this.adminToken);

        // 6. 获取学生Token
        String studentLoginJson = "{\"username\":\"student1\",\"password\":\"student123\"}";
        MvcResult studentResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentLoginJson))
                .andExpect(status().isOk())
                .andReturn();

        String studentResponse = studentResult.getResponse().getContentAsString();
        this.studentToken = objectMapper.readTree(studentResponse).get("token").asText();
        logger.info("===== 学生登录成功 =====");
        logger.info("学生token: {}", this.studentToken);
    }

    @Test
    void summary_should_return_401_when_anonymous() throws Exception {
        CourseDto courseDto = new CourseDto();
        courseDto.setName("测试课程");
        courseDto.setTeacher("测试教师");

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testStudentPostCourse_Return403() throws Exception {
        CourseDto courseDto = new CourseDto();
        courseDto.setName("Python实战");
        courseDto.setTeacher("王老师");

        mockMvc.perform(post("/courses")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminPostCourse_Return200() throws Exception {
        CourseDto courseDto = new CourseDto();
        courseDto.setName("Java高级开发");
        courseDto.setTeacher("李老师");

        mockMvc.perform(post("/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDto)))
                .andExpect(status().isOk());
    }
}