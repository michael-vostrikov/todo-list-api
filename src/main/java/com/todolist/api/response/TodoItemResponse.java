package com.todolist.api.response;

import com.todolist.api.enums.TodoItemStatus;
import java.util.Map;
import java.time.Instant;
import lombok.Data;

@Data
public class TodoItemResponse {
    private final int id;

    private final String description;

    private final TodoItemStatus status;

    private final Instant createdAt;

    private final Instant dueDate;

    private final Instant doneAt;
}
