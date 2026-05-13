package com.example.projectmanagerapp;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.springframework.http.MediaType;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest
public class IntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    MockMvc mockMvc;

    private final ObjectMapper objectMapper  = new ObjectMapper();

    @BeforeAll
    static void setup() {
        postgres.start();
    }

    @BeforeEach
    void beforeEach() {
    }

    @AfterAll
    static void cleanup() {
        postgres.stop();
    }

    @Test
    void shouldConnectToPostgres() {
        assertTrue(postgres.isCreated());
        assertTrue(postgres.isRunning());
    }

    @Test
    void shouldCreateProject() throws Exception {
        mockMvc.perform(post("/api/projects/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "test2000",
                                  "description": "testowy opis2",
                                  "users": []
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("test2000"))
                .andExpect(jsonPath("$.description").value("testowy opis2"));
    }

    @Test
    void shouldCreateUser() throws Exception {
        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "uzytkownik2000",
                                  "projects": []
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("uzytkownik2000"));
    }

    @Test
    void shouldAssignUserToAProject() throws Exception {
        long projectId = createProject("testProject", "TestProjectDescription");
        long userId = createUser("testUser");
        mockMvc.perform(post("/api/projects/{projectId}/users/{userId}", projectId, userId))
                .andExpect(status().isOk());
    }

    private long createProject(String name, String description) throws Exception {
        String response = mockMvc.perform(post("/api/projects/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "%s"
                                }
                                """.formatted(name, description)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private long createUser(String username) throws Exception {
        String response = mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s"
                                }
                                """.formatted(username)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

}
