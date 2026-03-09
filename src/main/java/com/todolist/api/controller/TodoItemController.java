package com.todolist.api.controller;

import com.todolist.api.request.AddTodoItemRequest;
import com.todolist.api.request.ChangeTodoItemDescriptionRequest;
import com.todolist.api.request.ListTodoItemRequest;
import com.todolist.api.response.TodoItemResponse;
import com.todolist.api.service.TodoItemService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/todo-item")
@RequiredArgsConstructor
public class TodoItemController {

    private final TodoItemService todoItemService;

    @ApiResponses({
        @ApiResponse(responseCode = "201",
            content = @Content(schema = @Schema(implementation = TodoItemResponse.class))),
        @ApiResponse(responseCode = "400",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/add")
    public ResponseEntity<TodoItemResponse> add(@Valid @RequestBody AddTodoItemRequest request) {
        var response = todoItemService.add(request);

        var location = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/todo-item/view")
            .queryParam("id", response.id())
            .build()
            .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200"),
        @ApiResponse(responseCode = "400",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/list")
    public ResponseEntity<Page<TodoItemResponse>> list(
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
            content = @Content(schema = @Schema(implementation = TodoItemResponse.class))),
        @ApiResponse(responseCode = "400",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/view")
    public ResponseEntity<TodoItemResponse> view(@RequestParam(name = "id") @Positive int id) {
        return ResponseEntity.ok(todoItemService.view(id));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = TodoItemResponse.class))),
        @ApiResponse(responseCode = "400",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/change-description")
    public ResponseEntity<TodoItemResponse> changeDescription(
        @RequestParam(name = "id") @Positive int id,
        @Valid @RequestBody ChangeTodoItemDescriptionRequest request
    ) {
        return ResponseEntity.ok(todoItemService.changeDescription(id, request));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = TodoItemResponse.class))),
        @ApiResponse(responseCode = "400",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/mark-as-done")
    public ResponseEntity<TodoItemResponse> markAsDone(@RequestParam(name = "id") @Positive int id) {
        return ResponseEntity.ok(todoItemService.markAsDone(id));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = TodoItemResponse.class))),
        @ApiResponse(responseCode = "400",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/mark-as-not-done")
    public ResponseEntity<TodoItemResponse> markAsNotDone(@RequestParam(name = "id") @Positive int id) {
        return ResponseEntity.ok(todoItemService.markAsNotDone(id));
    }
}
