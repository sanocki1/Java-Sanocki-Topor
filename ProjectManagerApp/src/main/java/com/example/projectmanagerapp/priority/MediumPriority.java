package com.example.projectmanagerapp.priority;

import com.example.projectmanagerapp.entity.Tasks;

public class MediumPriority implements PriorityLevel {

    @Override
    public Tasks.TaskType getPriority() {
        return Tasks.TaskType.MEDIUM_PRIORITY;
    }
}
