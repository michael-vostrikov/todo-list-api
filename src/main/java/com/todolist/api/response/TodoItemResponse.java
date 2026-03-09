package com.todolist.api.response;

import com.todolist.api.enums.TodoItemStatus;
import java.time.Instant;

public record TodoItemResponse(
    Integer id,
    String description,
    TodoItemStatus status,
    Instant createdAt,
    Instant dueDate,
    Instant doneAt
) {
}
