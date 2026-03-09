package com.todolist.api.service;

import com.todolist.api.entity.TodoItem;
import com.todolist.api.enums.TodoItemStatus;
import com.todolist.api.repository.TodoItemRepository;
import com.todolist.api.repository.TodoItemSpecification;
import com.todolist.api.request.AddTodoItemRequest;
import com.todolist.api.request.ListTodoItemRequest;
import com.todolist.api.request.ChangeTodoItemDescriptionRequest;
import com.todolist.api.response.TodoItemResponse;
import com.todolist.api.exception.EntityValidationException;
import org.springframework.stereotype.Service;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import com.todolist.api.mapper.TodoItemMapper;
import lombok.RequiredArgsConstructor;
import java.time.Clock;

@Service
@RequiredArgsConstructor
public class TodoItemService {

    private final TodoItemRepository todoItemRepository;

    private final TodoItemMapper todoItemMapper;

    private final Clock clock;

    @Transactional
    public TodoItemResponse add(AddTodoItemRequest request) {
        var entity = new TodoItem();

        entity.setStatus(TodoItemStatus.NOT_DONE);

        entity.setDescription(request.description);
        entity.setDueDate(request.dueDate);
        entity.setDoneAt(null);

        todoItemRepository.save(entity);

        return todoItemMapper.toDto(entity);
    }

    public Page<TodoItemResponse> list(ListTodoItemRequest filter, Pageable pageable) {
        Specification<TodoItem> spec = Specification.unrestricted();

        if (filter.status != null) {
            spec = spec.and(TodoItemSpecification.hasStatus(filter.status));
        }

        var page = todoItemRepository.findAll(spec, pageable);

        return page.map(entity -> todoItemMapper.toDto(entity));
    }

    public TodoItemResponse view(int id) {
        var entity = findEntity(id, false);

        return todoItemMapper.toDto(entity);
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
    public TodoItemResponse changeDescription(int id, ChangeTodoItemDescriptionRequest request) {
        var entity = findEntity(id, true);
        ensureUpdateAllowed(entity);

        entity.setDescription(request.getDescription());

        todoItemRepository.save(entity);

        return todoItemMapper.toDto(entity);
    }

    private void ensureUpdateAllowed(TodoItem entity) {
        if (entity.getStatus() == TodoItemStatus.PAST_DUE) {
            throw new EntityValidationException(Map.of("status", "Cannot change entity which is in PAST_DUE status"));
        }
    }

    @Transactional
    public TodoItemResponse markAsDone(int id) {
        var entity = findEntity(id, true);
        ensureUpdateAllowed(entity);

        entity.setStatus(TodoItemStatus.DONE);
        entity.setDoneAt(Instant.now(clock));

        todoItemRepository.save(entity);

        return todoItemMapper.toDto(entity);
    }

    @Transactional
    public TodoItemResponse markAsNotDone(int id) {
        var entity = findEntity(id, true);
        ensureUpdateAllowed(entity);

        entity.setStatus(TodoItemStatus.NOT_DONE);
        entity.setDoneAt(null);

        todoItemRepository.save(entity);

        return todoItemMapper.toDto(entity);
    }

    @Transactional
    public void markAsPastDue(int id) {
        var entity = findEntity(id, true);

        // it may happen that status changed right after we got list of ids to process
        // we don't need exception here, just skip not matching
        if (entity.getStatus() != TodoItemStatus.NOT_DONE) {
            return;
        }

        entity.setStatus(TodoItemStatus.PAST_DUE);

        todoItemRepository.save(entity);
    }
}
