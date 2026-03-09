package com.todolist.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record AddTodoItemRequest(
    @NotNull
    @Size(min = 1, max = 2000)
    @Schema(example = "Item description")
    String description,

    @NotNull
    @Future
    @Schema(example = "2026-06-12T00:00:00Z")
    Instant dueDate
) {
}
