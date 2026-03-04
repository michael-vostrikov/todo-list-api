package com.todolist.api.service;

import com.todolist.api.entity.TodoItem;
import com.todolist.api.enums.TodoItemStatus;
import com.todolist.api.repository.TodoItemRepository;
import com.todolist.api.repository.TodoItemSpecification;
import com.todolist.api.request.AddTodoItemRequest;
import com.todolist.api.request.ListTodoItemRequest;
import com.todolist.api.request.ChangeTodoItemDescriptionRequest;
import com.todolist.api.exception.EntityValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.EntityNotFoundException;
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

    public TodoItem view(int id) {
        var entity = findEntity(id, false);

        return entity;
    }

    private TodoItem findEntity(int id, boolean forUpdate) {
        return
            (forUpdate
                ? todoItemRepository.findByIdForUpdate(id)
                : todoItemRepository.findById(id)
            )
            .orElseThrow(() -> new EntityNotFoundException("TodoItem not found: " + id));
    }

    @Transactional
    public TodoItem changeDescription(int id, ChangeTodoItemDescriptionRequest request) {
        var entity = findEntity(id, true);
        ensureUpdateAllowed(entity);

        entity.setDescription(request.getDescription());

        todoItemRepository.save(entity);

        return entity;
    }

    private void ensureUpdateAllowed(TodoItem entity) {
        if (entity.getStatus() == TodoItemStatus.PAST_DUE) {
            throw new EntityValidationException(Map.of("status", "Cannot change entity which is in PAST_DUE status"));
        }
    }

    @Transactional
    public TodoItem markAsDone(int id) {
        var entity = findEntity(id, true);
        ensureUpdateAllowed(entity);

        entity.setStatus(TodoItemStatus.DONE);
        entity.setDoneAt(Instant.now());

        todoItemRepository.save(entity);

        return entity;
    }

}
