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

    public Project updateProject(Project project) {
        if (!projectRepository.existsById(project.getId())) {
            return null;
        }
        return projectRepository.save(project);
    }

    public boolean deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            return false;
        }
        projectRepository.deleteById(id);
        return true;
    }

    public boolean assignUserToProject(Long projectId, Long userId) {
        System.out.println("huj1");
        Optional<Project> project = projectRepository.findById(projectId);
        Optional<User> user = userService.getUserById(userId);
        System.out.println("huj2");
        if (project.isEmpty() || user.isEmpty()) {
            return false;
        }
        System.out.println("huj3");
        project.get().getUsers().add(user.get());
        System.out.println("huj4");
        projectRepository.save(project.get());
        System.out.println("huj5");
        return true;
    }
}