package com.example.projectmanagerapp;

import com.example.projectmanagerapp.entity.Project;
import com.example.projectmanagerapp.entity.User;
import com.example.projectmanagerapp.repository.ProjectRepository;
import com.example.projectmanagerapp.service.ProjectService;
import com.example.projectmanagerapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectServiceTest {

    private ProjectRepository projectRepository;
    private ProjectService projectService;
    private UserService userService;

    @BeforeEach
    void setup() {
        projectRepository = Mockito.mock(ProjectRepository.class);
        userService = Mockito.mock(UserService.class);
        projectService = new ProjectService(projectRepository, userService);
    }

    @Test
    @DisplayName("Should return all projects")
    void shouldReturnAllProjects() {
        Project p1 = new Project();
        p1.setName("WebApplicationProject");
        Project p2 = new Project();
        p2.setName("MobileApplicationProject");

        when(projectRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<Project> projects = projectService.getAllProjects();

        assertEquals(2, projects.size());
        verify(projectRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return project by id")
    void shouldReturnProjectById() {
        Project p = new Project();
        p.setId(1L);
        p.setName("WebApplicationProject");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));

        Optional<Project> result = projectService.getProjectById(1L);

        assertTrue(result.isPresent());
        assertEquals("WebApplicationProject", result.get().getName());
        verify(projectRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when project not found by id")
    void shouldReturnEmptyWhenGetProjectByIdNotFound() {
        when(projectRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Project> result = projectService.getProjectById(2L);

        assertTrue(result.isEmpty());
        verify(projectRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Should create project")
    void shouldCreateProject() {
        Project p = new Project();
        p.setName("NewProject");

        when(projectRepository.save(p)).thenReturn(p);

        Project created = projectService.createProject(p);

        assertEquals("NewProject", created.getName());
        verify(projectRepository, times(1)).save(p);
    }

    @Test
    @DisplayName("Should update project")
    void shouldUpdateProject() {
        Project p = new Project();
        p.setId(1L);
        p.setName("UpdatedProject");

        when(projectRepository.existsById(1L)).thenReturn(true);
        when(projectRepository.save(p)).thenReturn(p);

        Optional<Project> updated = projectService.updateProject(p);

        assertTrue(updated.isPresent());
        assertEquals("UpdatedProject", updated.get().getName());
        verify(projectRepository, times(1)).existsById(1L);
        verify(projectRepository, times(1)).save(p);
    }

    @Test
    @DisplayName("Should return empty when updating non-existing project")
    void shouldReturnEmptyWhenUpdateProjectNotFound() {
        Project p = new Project();
        p.setId(2L);
        p.setName("NonExistentProject");

        when(projectRepository.existsById(2L)).thenReturn(false);

        Optional<Project> updated = projectService.updateProject(p);

        assertTrue(updated.isEmpty());
        verify(projectRepository, times(1)).existsById(2L);
        verify(projectRepository, never()).save(p);
    }

    @Test
    @DisplayName("Should delete project")
    void shouldDeleteProject() {
        when(projectRepository.existsById(1L)).thenReturn(true);

        boolean deleted = projectService.deleteProject(1L);

        assertTrue(deleted);
        verify(projectRepository, times(1)).existsById(1L);
        verify(projectRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should return false when deleting non-existing project")
    void shouldReturnFalseWhenDeleteProjectNotFound() {
        when(projectRepository.existsById(2L)).thenReturn(false);

        boolean deleted = projectService.deleteProject(2L);

        assertFalse(deleted);
        verify(projectRepository, times(1)).existsById(2L);
        verify(projectRepository, never()).deleteById(2L);
    }

    @Test
    @DisplayName("Should assign user to project successfully")
    void shouldAssignUserToProject() {
        Project project = new Project();
        project.setId(1L);
        User user = new User();
        user.setId(1L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(projectRepository.save(project)).thenReturn(project);

        boolean result = projectService.assignUserToProject(1L, 1L);

        assertTrue(result);
        assertTrue(project.getUsers().contains(user));
        verify(projectRepository, times(1)).findById(1L);
        verify(userService, times(1)).getUserById(1L);
        verify(projectRepository, times(1)).save(project);
    }

    @Test
    @DisplayName("Should return false when assigning user to non-existing project")
    void shouldReturnFalseWhenAssignUserToNonExistingProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
        when(userService.getUserById(1L)).thenReturn(Optional.of(new User()));

        boolean result = projectService.assignUserToProject(1L, 1L);

        assertFalse(result);
        verify(projectRepository, times(1)).findById(1L);
        verify(userService, times(1)).getUserById(1L);
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return false when assigning non-existing user to project")
    void shouldReturnFalseWhenAssignNonExistingUserToProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(new Project()));
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        boolean result = projectService.assignUserToProject(1L, 1L);

        assertFalse(result);
        verify(projectRepository, times(1)).findById(1L);
        verify(userService, times(1)).getUserById(1L);
        verify(projectRepository, never()).save(any());
    }
}