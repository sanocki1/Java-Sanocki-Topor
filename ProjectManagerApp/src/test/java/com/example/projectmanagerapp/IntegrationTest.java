package com.example.projectmanagerapp;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.springframework.http.MediaType;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
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
    @DisplayName("Should return not found when trying to assign a non-existing user to non-existing project")
    void shouldReturnNotFoundForAssigningNonExistingUserToNonExistingProject() throws Exception {
        mockMvc.perform(post("/api/projects/999/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update an existing user")
    void shouldUpdateUser() throws Exception {
        long userId = createUser("OldUsername");
        mockMvc.perform(put("/api/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": %d,
                                  "username": "UpdatedUsername"
                                }
                                """.formatted(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("UpdatedUsername"));
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
    @DisplayName("Should update an existing task")
    void shouldUpdateTask() throws Exception {
        long projectId = createProject("UpdateTaskName", "Update task project description");
        long userId = createUser("updateTaskUsername");
        long taskId = createTask("OldTaskTitle", projectId, userId);
        mockMvc.perform(put("/api/tasks/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": %d,
                                  "title": "UpdatedTaskTitle",
                                  "task_type": "MEDIUM_PRIORITY",
                                  "projects": {"id": %d},
                                  "user": {"id": %d}
                                }
                                """.formatted(taskId, projectId, userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("UpdatedTaskTitle"))
                .andExpect(jsonPath("$.task_type").value("MEDIUM_PRIORITY"));
    }

    @Test
    @DisplayName("Should return not found when trying to update non-existing user")
    void shouldReturnNotFoundForUpdatingNonExistingUser() throws Exception {
        mockMvc.perform(put("/api/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 999,
                                  "username": "UpdatedUsername"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return not found when trying to update non-existing project")
    void shouldReturnNotFoundForUpdatingNonExistingProject() throws Exception {
        mockMvc.perform(put("/api/projects/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 999,
                                  "name": "UpdatedProjectName",
                                  "description": "Updated project description"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return not found when trying to update non-existing task")
    void shouldReturnNotFoundForUpdatingNonExistingTask() throws Exception {
        mockMvc.perform(put("/api/tasks/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 999,
                                  "title": "UpdatedTaskTitle",
                                  "task_type": "MEDIUM_PRIORITY"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete an existing user")
    void shouldDeleteUser() throws Exception {
        long userId = createUser("UserToDelete");
        mockMvc.perform(delete("/api/users/delete/{id}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should delete an existing project")
    void shouldDeleteProject() throws Exception {
        long projectId = createProject("ProjectToDelete", "Project for deletion testing");
        mockMvc.perform(delete("/api/projects/delete/{id}", projectId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should delete an existing task")
    void shouldDeleteTask() throws Exception {
        long projectId = createProject("TaskDeleteProject", "Project for task deletion testing");
        long userId = createUser("deleteTaskUser");
        long taskId = createTask("ProjectToDelete", projectId, userId);
        mockMvc.perform(delete("/api/tasks/delete/{id}", taskId))
                .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("Should return not found when trying to delete non-existing user")
    void shouldReturnNotFoundForDeletingNonExistingUser() throws Exception {
        mockMvc.perform(delete("/api/users/delete/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return not found when trying to delete non-existing project")
    void shouldReturnNotFoundForDeletingNonExistingProject() throws Exception {
        mockMvc.perform(delete("/api/projects/delete/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return not found when trying to delete non-existing task")
    void shouldReturnNotFoundForDeletingNonExistingTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/delete/{id}", 999))
                .andExpect(status().isNotFound());
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
    @DisplayName("Should retrieve all projects")
    void shouldGetAllProjects() throws Exception {
        mockMvc.perform(get("/api/projects/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should retrieve all users")
    void shouldGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should retrieve a user by id")
    void shouldRetrieveUserById() throws Exception {
        long userId = createUser("johnSmith");
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should retrieve a project by id")
    void shouldRetrieveProjectById() throws Exception {
        long projectId = createProject("ProjectById", "Project for testing retrieval by id");
        mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should retrieve a task by id")
    void shouldRetrieveTaskById() throws Exception {
        long projectId =  createProject("ProjectById", "Project for testing retrieval by id");
        long userId = createUser("markSmith");
        long taskId = createTask("TaskById",  projectId, userId);
        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should return not found for non-existing user")
    void shouldReturnNotFoundForNonExistingUser() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return not found for non-existing project")
    void shouldReturnNotFoundForNonExistingProject() throws Exception {
        mockMvc.perform(get("/api/projects/{id}", 999L))
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

    private long createTask(String title, long projectId, long userId) throws Exception {
        String response = mockMvc.perform(post("/api/tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {
                                  "title": "%s",
                                  "task_type": "HIGH_PRIORITY",
                                  "projects": {"id": %d},
                                  "user": {"id": %d}
                                }
                                """.formatted(title, projectId, userId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

}