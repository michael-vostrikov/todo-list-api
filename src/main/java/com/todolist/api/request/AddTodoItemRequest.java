package com.todolist.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Future;
import java.time.Instant;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class AddTodoItemRequest {
    @NotNull
    @Size(min = 1, max = 2000)
    @Schema(example = "Item description")
    public final String description;

    @NotNull
    @Future
    @Schema(example = "2026-06-12T00:00:00Z")
    public final Instant dueDate;
}
