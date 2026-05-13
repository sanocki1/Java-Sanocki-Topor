package com.example.projectmanagerapp.entity.priority;

import com.example.projectmanagerapp.entity.Task;

public class HighPriority implements PriorityLevel {

    @Override
    public Task.TaskType getPriority() {
        return Task.TaskType.HIGH_PRIORITY;
    }
}
