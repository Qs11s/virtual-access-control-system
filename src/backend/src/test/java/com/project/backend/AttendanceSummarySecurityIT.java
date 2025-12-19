package com.project.backend;

import com.project.backend.model.SessionEntity;
import com.project.backend.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AttendanceSummarySecurityIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessionRepository sessionRepository;

    private Long anySessionId() {
        List<SessionEntity> sessions = sessionRepository.findAll();
        assertThat(sessions).isNotEmpty();
        return sessions.get(0).getId();
    }

    private String obtainToken(String username, String password) throws Exception {
        String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        MvcResult result = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getContentAsString();
    }

    @Test
    void summary_should_return_401_when_anonymous() throws Exception {
        Long sessionId = anySessionId();
        mockMvc.perform(get("/attendance/session/" + sessionId + "/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void summary_should_return_403_for_student() throws Exception {
        Long sessionId = anySessionId();
        String token = obtainToken("student1", "student123");
        mockMvc.perform(
                get("/attendance/session/" + sessionId + "/summary")
                        .header("Authorization", "Bearer " + token)
        )
                .andExpect(status().isForbidden());
    }

    @Test
    void summary_should_return_200_for_teacher() throws Exception {
        Long sessionId = anySessionId();
        String token = obtainToken("teacher1", "teacher123");
        mockMvc.perform(
                get("/attendance/session/" + sessionId + "/summary")
                        .header("Authorization", "Bearer " + token)
        )
                .andExpect(status().isOk());
    }
}
