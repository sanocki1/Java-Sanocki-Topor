package com.example.projectmanagerapp.service;

import com.example.projectmanagerapp.entity.Project;
import com.example.projectmanagerapp.entity.User;
import com.example.projectmanagerapp.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserService userService;

    public ProjectService(ProjectRepository projectRepository, UserService userService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    public Optional<Project> updateProject(Project project) {
        if (!projectRepository.existsById(project.getId())) {
            return Optional.empty();
        }
        return Optional.of(projectRepository.save(project));
    }

    public boolean deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            return false;
        }
        projectRepository.deleteById(id);
        return true;
    }

    public boolean assignUserToProject(Long projectId, Long userId) {
        Optional<Project> project = projectRepository.findById(projectId);
        Optional<User> user = userService.getUserById(userId);
        if (project.isEmpty() || user.isEmpty()) {
            return false;
        }
        project.get().getUsers().add(user.get());
        projectRepository.save(project.get());
        return true;
    }
}