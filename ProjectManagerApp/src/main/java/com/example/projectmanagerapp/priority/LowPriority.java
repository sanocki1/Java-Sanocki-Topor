package com.example.projectmanagerapp.priority;

import com.example.projectmanagerapp.entity.Task;

public class LowPriority implements PriorityLevel {

    @Override
    public Task.TaskType getPriority() {
        return Task.TaskType.LOW_PRIORITY;
    }
}
