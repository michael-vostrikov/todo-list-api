package com.todolist.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.todolist.api.entity.TodoItem;

public interface TodoItemRepository
        extends JpaRepository<TodoItem, Integer> {
}
