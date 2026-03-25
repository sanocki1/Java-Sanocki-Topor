package com.example.projectmanagerapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Tasks {
    public enum TaskType {
        LOW_PRIORITY,
        MEDIUM_PRIORITY,
        HIGH_PRIORITY
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String title;
    @Enumerated (EnumType.STRING)
    private TaskType task_type;
    @ManyToOne
    @JoinColumn(
            name = "project_id",
            nullable = false
    )
    private Projects projects;
    @ManyToOne
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private Users user;
}