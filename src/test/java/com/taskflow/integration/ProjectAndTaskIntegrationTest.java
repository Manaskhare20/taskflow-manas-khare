package com.taskflow.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.taskflow.AbstractPostgresIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class ProjectAndTaskIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void authenticatedUser_createsProjectAndTask() throws Exception {
        String email = "proj-it-" + UUID.randomUUID() + "@example.com";
        String registerJson =
                """
                {"name":"Owner","email":"%s","password":"password123"}
                """
                        .formatted(email);

        MvcResult auth = mockMvc.perform(post("/auth/register").contentType(APPLICATION_JSON).content(registerJson))
                .andExpect(status().isCreated())
                .andReturn();

        String token = JsonPath.read(auth.getResponse().getContentAsString(), "$.token");

        String projectJson = """
                {"name":"Sprint board","description":"Integration project"}
                """;

        MvcResult projectResult = mockMvc.perform(post("/projects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(projectJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Sprint board"))
                .andReturn();

        String projectId =
                JsonPath.read(projectResult.getResponse().getContentAsString(), "$.id");

        String taskJson = """
                {"title":"Ship feature","description":"End-to-end flow","status":"todo","priority":"high"}
                """;

        mockMvc.perform(post("/projects/" + projectId + "/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Ship feature"))
                .andExpect(jsonPath("$.projectId").value(projectId));
    }
}
