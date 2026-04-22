package com.example.projectmanagerapp.priority;

import com.example.projectmanagerapp.entity.Task;

public class MediumPriority implements PriorityLevel {

    @Override
    public Task.TaskType getPriority() {
        return Task.TaskType.MEDIUM_PRIORITY;
    }
}
