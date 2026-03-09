package com.todolist.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangeTodoItemDescriptionRequest(
    @NotNull
    @Size(min = 1, max = 2000)
    @Schema(example = "New item description")
    String description
) {
}
