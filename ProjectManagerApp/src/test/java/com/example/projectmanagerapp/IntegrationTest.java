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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

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
    @DisplayName("Should connect to PostgreSQL database")
    void shouldConnectToPostgres() {
        assertTrue(postgres.isCreated());
        assertTrue(postgres.isRunning());
    }

    @Test
    @DisplayName("Should create a new project")
    void shouldCreateProject() throws Exception {
        mockMvc.perform(post("/api/projects/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "SampleProject",
                                  "description": "A sample project for testing purposes",
                                  "users": []
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("SampleProject"))
                .andExpect(jsonPath("$.description").value("A sample project for testing purposes"));
    }

    @Test
    @DisplayName("Should create a new user")
    void shouldCreateUser() throws Exception {
        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "testUser",
                                  "projects": []
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    @DisplayName("Should assign user to a project")
    void shouldAssignUserToAProject() throws Exception {
        long projectId = createProject("DevelopmentProject", "Project focused on software development");
        long userId = createUser("assignUser");
        mockMvc.perform(post("/api/projects/{projectId}/users/{userId}", projectId, userId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should update an existing project")
    void shouldUpdateProject() throws Exception {
        long projectId = createProject("OldProjectName", "Old project description");
        mockMvc.perform(put("/api/projects/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": %d,
                                  "name": "UpdatedProjectName",
                                  "description": "Updated project description"
                                }
                                """.formatted(projectId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedProjectName"))
                .andExpect(jsonPath("$.description").value("Updated project description"));
    }

    @Test
    @DisplayName("Should delete an existing project")
    void shouldDeleteProject() throws Exception {
        long projectId = createProject("ProjectToDelete", "Project for deletion testing");
        mockMvc.perform(delete("/api/projects/delete/{id}", projectId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should create a new task")
    void shouldCreateTask() throws Exception {
        long projectId = createProject("TaskManagementProject", "Project for managing tasks");
        long userId = createUser("aliceSmith");
        mockMvc.perform(post("/api/tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Implement user authentication",
                                  "task_type": "HIGH_PRIORITY",
                                  "projects": {"id": %d},
                                  "user": {"id": %d}
                                }
                                """.formatted(projectId, userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Implement user authentication"));
    }

    @Test
    @DisplayName("Should retrieve all tasks")
    void shouldGetAllTasks() throws Exception {
        mockMvc.perform(get("/api/tasks/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should return not found for non-existing project")
    void shouldReturnNotFoundForNonExistingProject() throws Exception {
        mockMvc.perform(get("/api/projects/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return not found for non-existing user")
    void shouldReturnNotFoundForNonExistingUser() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return not found for non-existing task")
    void shouldReturnNotFoundForNonExistingTask() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", 999L))
                .andExpect(status().isNotFound());
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