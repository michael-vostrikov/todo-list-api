package com.todolist.api.controller;

import com.todolist.api.entity.TodoItem;
import com.todolist.api.repository.TodoItemRepository;
import com.todolist.api.service.TodoItemService;
import com.todolist.api.request.AddTodoItemRequest;
import com.todolist.api.response.ValidationErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
public class TodoItemController {

    @Autowired
    private TodoItemService todoItemService;

    @Autowired
    private TodoItemRepository todoItemRepository;

    @ApiResponses({
        @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = TodoItem.class))),
        @ApiResponse(responseCode = "400",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    @PostMapping("/todo-item/add")
    public ResponseEntity<TodoItem> add(
        @Valid @RequestBody AddTodoItemRequest request
    ) {
        return ResponseEntity.ok(todoItemService.add(request));
    }

}
