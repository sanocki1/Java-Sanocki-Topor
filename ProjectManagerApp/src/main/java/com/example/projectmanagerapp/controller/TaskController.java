package com.example.projectmanagerapp.controller;

import com.example.projectmanagerapp.entity.Projects;
import com.example.projectmanagerapp.entity.Tasks;
import com.example.projectmanagerapp.repository.TaskRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task API", description = "API for managing tasks")
public class TaskController {

    private final TaskRepository taskRepository;;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping
    @Operation(summary = "Get all tasks")
    public List<Tasks> getAllTasks() {
        return taskRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new task")
    public Tasks createTask(@RequestBody Tasks task) {
        return taskRepository.save(task);
    }
}
