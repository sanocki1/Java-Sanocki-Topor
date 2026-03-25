package com.example.projectmanagerapp.priority;

import com.example.projectmanagerapp.entity.Tasks;

public class HighPriority implements PriorityLevel {

    @Override
    public Tasks.TaskType getPriority() {
        return Tasks.TaskType.HIGH_PRIORITY;
    }
}
