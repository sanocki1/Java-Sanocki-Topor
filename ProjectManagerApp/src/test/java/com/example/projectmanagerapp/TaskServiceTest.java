package com.example.projectmanagerapp;

import com.example.projectmanagerapp.entity.Task;
import com.example.projectmanagerapp.repository.TaskRepository;
import com.example.projectmanagerapp.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    private TaskRepository taskRepository;
    private TaskService taskService;

    @BeforeEach
    void setup() {
        taskRepository = Mockito.mock(TaskRepository.class);
        taskService = new TaskService(taskRepository);
    }

    @Test
    @DisplayName("Should return all tasks")
    void shouldReturnAllTasks() {
        Task t1 = new Task();
        t1.setTitle("Task1");
        Task t2 = new Task();
        t2.setTitle("Task2");

        when(taskRepository.findAll()).thenReturn(Arrays.asList(t1, t2));

        List<Task> tasks = taskService.getAllTasks();

        assertEquals(2, tasks.size());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return task by id")
    void shouldReturnTaskById() {
        Task t = new Task();
        t.setId(1L);
        t.setTitle("T1");

        when(taskRepository.findById(1L)).thenReturn(java.util.Optional.of(t));

        Optional<Task> result = taskService.getTaskById(1L);

        assertTrue(result.isPresent());
        assertEquals("T1", result.get().getTitle());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when task not found by id")
    void shouldReturnEmptyWhenGetTaskByIdNotFound() {
        when(taskRepository.findById(2L)).thenReturn(java.util.Optional.empty());

        Optional<Task> result = taskService.getTaskById(2L);

        assertTrue(result.isEmpty());
        verify(taskRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Should create task")
    void shouldCreateTask() {
        Task t = new Task();
        t.setTitle("New");

        when(taskRepository.save(t)).thenReturn(t);

        Task created = taskService.createTask(t);

        assertEquals("New", created.getTitle());
        verify(taskRepository, times(1)).save(t);
    }

    @Test
    @DisplayName("Should update task")
    void shouldUpdateTask() {
        Task t = new Task();
        t.setId(1L);
        t.setTitle("Updated");

        when(taskRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.save(t)).thenReturn(t);

        Task updated = taskService.updateTask(t);

        assertEquals("Updated", updated.getTitle());
        verify(taskRepository, times(1)).existsById(1L);
        verify(taskRepository, times(1)).save(t);
    }

    @Test
    @DisplayName("Should return null when updating non-existing task")
    void shouldReturnNullWhenUpdateTaskNotFound() {
        Task t = new Task();
        t.setId(2L);
        t.setTitle("DoesNotExist");

        when(taskRepository.existsById(2L)).thenReturn(false);

        Task updated = taskService.updateTask(t);

        assertNull(updated);
        verify(taskRepository, times(1)).existsById(2L);
        verify(taskRepository, never()).save(t);
    }

    @Test
    @DisplayName("Should delete task")
    void shouldDeleteTask() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        boolean deleted = taskService.deleteTask(1L);

        assertTrue(deleted);
        verify(taskRepository, times(1)).existsById(1L);
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should return false when deleting non-existing task")
    void shouldReturnFalseWhenDeleteTaskNotFound() {
        when(taskRepository.existsById(2L)).thenReturn(false);

        boolean deleted = taskService.deleteTask(2L);

        assertFalse(deleted);
        verify(taskRepository, times(1)).existsById(2L);
        verify(taskRepository, never()).deleteById(2L);
    }
}
