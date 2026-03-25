package com.example.projectmanagerapp.controller;

import com.example.projectmanagerapp.entity.Projects;
import com.example.projectmanagerapp.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;;

    public ProjectController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @GetMapping
    public List<Projects> getAllProjects() {
        return projectRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Projects createProject(@RequestBody Projects project) {
        return projectRepository.save(project);
    }
}
