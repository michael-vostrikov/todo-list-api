package com.todolist.api.controller;

import com.todolist.api.entity.TodoItem;
import com.todolist.api.repository.TodoItemRepository;
import com.todolist.api.service.TodoItemService;
import com.todolist.api.request.AddTodoItemRequest;
import com.todolist.api.request.ListTodoItemRequest;
import com.todolist.api.request.ChangeTodoItemDescriptionRequest;
import com.todolist.api.response.ValidationErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
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

    @GetMapping("/todo-item/list")
    public ResponseEntity<Page<TodoItem>> list(
        @ParameterObject
        @ModelAttribute ListTodoItemRequest filter,

        @ParameterObject
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        return ResponseEntity.ok(todoItemService.list(filter, pageable));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = TodoItem.class))),
        @ApiResponse(responseCode = "404",
            content = @Content(schema = @Schema(implementation = String.class))),
    })
    @GetMapping("/todo-item/view")
    public ResponseEntity<TodoItem> view(
        @RequestParam(name = "id") int id
    ) {
        return ResponseEntity.ok(todoItemService.view(id));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = TodoItem.class))),
        @ApiResponse(responseCode = "400",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "422",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    @PutMapping("/todo-item/change-description")
    public ResponseEntity<TodoItem> changeDescription(
        @RequestParam(name = "id") int id,
        @Valid @RequestBody ChangeTodoItemDescriptionRequest request
    ) {
        return ResponseEntity.ok(todoItemService.changeDescription(id, request));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = TodoItem.class))),
        @ApiResponse(responseCode = "400",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
        @ApiResponse(responseCode = "404",
            content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "422",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    @PutMapping("/todo-item/mark-as-done")
    public ResponseEntity<TodoItem> markAsDone(
        @RequestParam(name = "id") int id
    ) {
        return ResponseEntity.ok(todoItemService.markAsDone(id));
    }

}
