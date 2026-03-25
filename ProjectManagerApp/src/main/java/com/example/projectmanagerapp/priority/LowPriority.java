package com.example.projectmanagerapp.priority;

import com.example.projectmanagerapp.entity.Tasks;

public class LowPriority implements PriorityLevel {

    @Override
    public Tasks.TaskType getPriority() {
        return Tasks.TaskType.LOW_PRIORITY;
    }
}
