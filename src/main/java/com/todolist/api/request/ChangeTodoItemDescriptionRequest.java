package com.todolist.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChangeTodoItemDescriptionRequest {
    @NotNull()
    @Size(min = 1, max = 2000)
    @Schema(example = "New item description")
    public final String description;
}
