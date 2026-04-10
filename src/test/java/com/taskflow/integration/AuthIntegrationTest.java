package com.taskflow.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.taskflow.AbstractPostgresIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class AuthIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerThenLogin_returnsJwt() throws Exception {
        String email = "auth-it-" + UUID.randomUUID() + "@example.com";
        String registerJson =
                """
                {"name":"Integration User","email":"%s","password":"password123"}
                """
                        .formatted(email);

        mockMvc.perform(post("/auth/register").contentType(APPLICATION_JSON).content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value(email));

        String loginJson =
                """
                {"email":"%s","password":"password123"}
                """
                        .formatted(email);

        mockMvc.perform(post("/auth/login").contentType(APPLICATION_JSON).content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.email").value(email));
    }
}
