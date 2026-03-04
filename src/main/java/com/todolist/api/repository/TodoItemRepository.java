package com.todolist.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Pageable;
import com.todolist.api.entity.TodoItem;

public interface TodoItemRepository
        extends JpaRepository<TodoItem, Integer>, JpaSpecificationExecutor<TodoItem> {
}
