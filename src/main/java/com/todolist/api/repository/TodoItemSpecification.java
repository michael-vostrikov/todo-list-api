package com.todolist.api.repository;

import com.todolist.api.entity.TodoItem;
import com.todolist.api.entity.TodoItem_;
import com.todolist.api.enums.TodoItemStatus;
import org.springframework.data.jpa.domain.Specification;

public final class TodoItemSpecification {

    private TodoItemSpecification() {
    }

    public static Specification<TodoItem> hasStatus(TodoItemStatus status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(TodoItem_.status), status);
    }
}
