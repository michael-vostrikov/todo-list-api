package com.todolist.api.service;

import com.todolist.api.entity.TodoItem;
import com.todolist.api.entity.TodoItem_;
import com.todolist.api.enums.TodoItemStatus;
import com.todolist.api.exception.InvalidSortPropertyException;
import com.todolist.api.mapper.TodoItemMapper;
import com.todolist.api.repository.TodoItemRepository;
import com.todolist.api.repository.TodoItemSpecification;
import com.todolist.api.request.AddTodoItemRequest;
import com.todolist.api.request.ChangeTodoItemDescriptionRequest;
import com.todolist.api.request.ListTodoItemRequest;
import com.todolist.api.response.TodoItemResponse;
import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoItemService {

    private static final Set<String> SORTABLE_PROPERTIES = Set.of(
        TodoItem_.ID,
        TodoItem_.DESCRIPTION,
        TodoItem_.STATUS,
        TodoItem_.CREATED_AT,
        TodoItem_.DUE_DATE,
        TodoItem_.DONE_AT
    );

    private final TodoItemRepository todoItemRepository;

    private final TodoItemMapper todoItemMapper;

    private final Clock clock;

    @Transactional
    public TodoItemResponse add(AddTodoItemRequest request) {
        var entity = TodoItem.create(request.description(), request.dueDate(), Instant.now(clock));

        todoItemRepository.save(entity);

        return todoItemMapper.toDto(entity);
    }

    public Page<TodoItemResponse> list(ListTodoItemRequest filter, Pageable pageable) {
        ensureSortSupported(pageable.getSort());

        Specification<TodoItem> spec = Specification.unrestricted();

        if (filter.status() != null) {
            spec = spec.and(TodoItemSpecification.hasStatus(filter.status()));
        }

        return todoItemRepository.findAll(spec, pageable).map(todoItemMapper::toDto);
    }

    public TodoItemResponse view(int id) {
        return todoItemMapper.toDto(findEntity(id));
    }

    @Transactional
    public TodoItemResponse changeDescription(int id, ChangeTodoItemDescriptionRequest request) {
        var entity = findEntityForUpdate(id);

        entity.changeDescription(request.description());

        return todoItemMapper.toDto(entity);
    }

    @Transactional
    public TodoItemResponse markAsDone(int id) {
        var entity = findEntityForUpdate(id);

        entity.markAsDone(Instant.now(clock));

        return todoItemMapper.toDto(entity);
    }

    @Transactional
    public TodoItemResponse markAsNotDone(int id) {
        var entity = findEntityForUpdate(id);

        entity.markAsNotDone();

        return todoItemMapper.toDto(entity);
    }

    @Transactional
    public void markAsPastDue(int id) {
        var entity = findEntityForUpdate(id);

        // it may happen that status changed right after we got list of ids to process
        // we don't need exception here, just skip not matching
        if (entity.getStatus() != TodoItemStatus.NOT_DONE) {
            return;
        }

        entity.markAsPastDue();
    }

    private TodoItem findEntity(int id) {
        return todoItemRepository.findById(id)
            .orElseThrow(() -> notFound(id));
    }

    private TodoItem findEntityForUpdate(int id) {
        return todoItemRepository.findByIdForUpdate(id)
            .orElseThrow(() -> notFound(id));
    }

    private static EntityNotFoundException notFound(int id) {
        return new EntityNotFoundException("TodoItem not found: " + id);
    }

    private static void ensureSortSupported(Sort sort) {
        for (Sort.Order order : sort) {
            if (!SORTABLE_PROPERTIES.contains(order.getProperty())) {
                throw new InvalidSortPropertyException(order.getProperty());
            }
        }
    }
}
