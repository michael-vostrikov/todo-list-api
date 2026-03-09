package com.todolist.api.mapper;

import com.todolist.api.entity.TodoItem;
import com.todolist.api.response.TodoItemResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TodoItemMapper {
    TodoItemResponse toDto(TodoItem entity);
}
