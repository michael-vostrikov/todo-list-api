package com.todolist.api.service;

import com.todolist.api.entity.TodoItem;
import com.todolist.api.enums.TodoItemStatus;
import com.todolist.api.repository.TodoItemRepository;
import com.todolist.api.repository.TodoItemSpecification;
import com.todolist.api.request.AddTodoItemRequest;
import com.todolist.api.request.ListTodoItemRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.time.Instant;

@Service
public class TodoItemService {

    @Autowired
    private TodoItemRepository todoItemRepository;

    @Transactional
    public TodoItem add(AddTodoItemRequest request) {
        var entity = new TodoItem();

        entity.setStatus(TodoItemStatus.NOT_DONE);

        entity.setDescription(request.description);
        entity.setDueDate(request.dueDate);
        entity.setDoneAt(null);

        todoItemRepository.save(entity);

        return entity;
    }

    public Page<TodoItem> list(ListTodoItemRequest filter, Pageable pageable) {
        Specification<TodoItem> spec = Specification.unrestricted();

        if (filter.status != null) {
            spec = spec.and(TodoItemSpecification.hasStatus(filter.status));
        }

        var page = todoItemRepository.findAll(spec, pageable);

        return page;
    }

}
