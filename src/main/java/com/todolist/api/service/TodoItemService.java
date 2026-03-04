package com.todolist.api.service;

import com.todolist.api.entity.TodoItem;
import com.todolist.api.enums.TodoItemStatus;
import com.todolist.api.repository.TodoItemRepository;
import com.todolist.api.repository.TodoItemSpecification;
import com.todolist.api.request.AddTodoItemRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
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

}
