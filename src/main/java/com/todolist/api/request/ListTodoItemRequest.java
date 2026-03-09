package com.todolist.api.request;

import com.todolist.api.enums.TodoItemStatus;

public record ListTodoItemRequest(TodoItemStatus status) {
}
