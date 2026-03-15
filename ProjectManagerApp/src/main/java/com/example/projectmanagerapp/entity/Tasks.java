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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String title;
    @Enumerated (EnumType.STRING)
    private String task_type;
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