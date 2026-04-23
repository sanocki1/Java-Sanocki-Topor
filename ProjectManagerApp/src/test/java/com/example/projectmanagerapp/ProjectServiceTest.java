package com.example.projectmanagerapp;

import com.example.projectmanagerapp.entity.Project;
import com.example.projectmanagerapp.repository.ProjectRepository;
import com.example.projectmanagerapp.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProjectServiceTest {

    private ProjectRepository projectRepository;
    private ProjectService projectService;

    @BeforeEach
    void setup() {
        projectRepository = Mockito.mock(ProjectRepository.class);
        projectService = new ProjectService(projectRepository);
    }

    @Test
    @DisplayName("Should return all projects")
    void shouldReturnAllProjects() {
        Project p1 = new Project();
        p1.setName("Project1");
        Project p2 = new Project();
        p2.setName("Project2");

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
        p.setName("P1");

        when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(p));

        Project result = projectService.getProjectById(1L);

        assertEquals("P1", result.getName());
        verify(projectRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw when project not found by id")
    void shouldThrowWhenGetProjectByIdNotFound() {
        when(projectRepository.findById(2L)).thenReturn(java.util.Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> projectService.getProjectById(2L));

        verify(projectRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Should create project")
    void shouldCreateProject() {
        Project p = new Project();
        p.setName("New");

        when(projectRepository.save(p)).thenReturn(p);

        Project created = projectService.createProject(p);

        assertEquals("New", created.getName());
        verify(projectRepository, times(1)).save(p);
    }

    @Test
    @DisplayName("Should update project")
    void shouldUpdateProject() {
        Project p = new Project();
        p.setId(1L);
        p.setName("Updated");

        when(projectRepository.existsById(1L)).thenReturn(true);
        when(projectRepository.save(p)).thenReturn(p);

        Project updated = projectService.updateProject(p);

        assertEquals("Updated", updated.getName());
        verify(projectRepository, times(1)).existsById(1L);
        verify(projectRepository, times(1)).save(p);
    }

    @Test
    @DisplayName("Should throw when updating non-existing project")
    void shouldThrowWhenUpdateProjectNotFound() {
        Project p = new Project();
        p.setId(2L);
        p.setName("DoesNotExist");

        when(projectRepository.existsById(2L)).thenReturn(false);

        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> projectService.updateProject(p));

        verify(projectRepository, times(1)).existsById(2L);
    }

    @Test
    @DisplayName("Should delete project")
    void shouldDeleteProject() {
        when(projectRepository.existsById(1L)).thenReturn(true);

        projectService.deleteProject(1L);

        verify(projectRepository, times(1)).existsById(1L);
        verify(projectRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw when deleting non-existing project")
    void shouldThrowWhenDeleteProjectNotFound() {
        when(projectRepository.existsById(2L)).thenReturn(false);

        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> projectService.deleteProject(2L));

        verify(projectRepository, times(1)).existsById(2L);
    }
}
