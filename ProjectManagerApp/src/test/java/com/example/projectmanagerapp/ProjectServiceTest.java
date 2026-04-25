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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

        Optional<Project> result = projectService.getProjectById(1L);

        assertTrue(result.isPresent());
        assertEquals("P1", result.get().getName());
        verify(projectRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when project not found by id")
    void shouldReturnEmptyWhenGetProjectByIdNotFound() {
        when(projectRepository.findById(2L)).thenReturn(java.util.Optional.empty());

        Optional<Project> result = projectService.getProjectById(2L);

        assertTrue(result.isEmpty());
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
    @DisplayName("Should return null when updating non-existing project")
    void shouldReturnNullWhenUpdateProjectNotFound() {
        Project p = new Project();
        p.setId(2L);
        p.setName("DoesNotExist");

        when(projectRepository.existsById(2L)).thenReturn(false);

        Project updated = projectService.updateProject(p);

        assertNull(updated);
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
}
