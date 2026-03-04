package com.todolist.api.request;

import com.todolist.api.enums.TodoItemStatus;
import lombok.Data;

@Data
public class ListTodoItemRequest {
    public TodoItemStatus status;
}
