package com.todolist.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import com.todolist.api.enums.TodoItemStatus;

@Entity
@Table(name = "todo_item")
@Data
public class TodoItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TodoItemStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "due_date", nullable = false)
    private Instant dueDate;

    @Column(name = "done_at", nullable = true)
    private Instant doneAt;
}
