package com.example.projectmanagerapp.priority;

import com.example.projectmanagerapp.entity.Task;

public class HighPriority implements PriorityLevel {

    @Override
    public Task.TaskType getPriority() {
        return Task.TaskType.HIGH_PRIORITY;
    }
}
