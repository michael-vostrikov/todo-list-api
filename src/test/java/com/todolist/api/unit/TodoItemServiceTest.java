package com.app.userapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.*;
import com.todolist.api.repository.TodoItemRepository;
import com.todolist.api.entity.TodoItem;
import com.todolist.api.enums.TodoItemStatus;
import com.todolist.api.request.ChangeTodoItemDescriptionRequest;
import com.todolist.api.service.TodoItemService;
import com.todolist.api.exception.EntityValidationException;
import java.util.Optional;
import java.time.Instant;
import java.time.Clock;
import java.time.ZoneId;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.todolist.api.mapper.TodoItemMapper;

class TodoItemServiceTest {

    private final TodoItemRepository todoItemRepository = mock(TodoItemRepository.class);;

    private final TodoItemMapper todoItemMapper = mock(TodoItemMapper.class);

    private final Clock fixedClock = Clock.fixed(
        Instant.parse("2026-02-01T00:00:00Z"),
        ZoneId.of("UTC")
    );

    private final TodoItemService todoItemService = new TodoItemService(todoItemRepository, todoItemMapper, fixedClock);

	@Test
	void testChangeDescriptionAllowed() {
	    var now = Instant.now();
	    var newDescription = "New item description";

	    var entity = new TodoItem();
	    entity.setId(1);
	    entity.setDescription("Item description");
	    entity.setDueDate(now);
	    entity.setStatus(TodoItemStatus.NOT_DONE);

	    when(todoItemRepository.findByIdForUpdate(entity.getId())).thenReturn(Optional.of(entity));

	    var request = new ChangeTodoItemDescriptionRequest(newDescription);
	    todoItemService.changeDescription(entity.getId(), request);

        entity.setDescription(newDescription);

        verify(todoItemRepository).save(entity);
	}

	@Test
	void testChangeDescriptionForPastDueItemNotAllowed() {
	    var now = Instant.now();
	    var newDescription = "New item description";

	    var entity = new TodoItem();
	    entity.setId(1);
	    entity.setDescription("Item description");
	    entity.setDueDate(now);
	    entity.setStatus(TodoItemStatus.PAST_DUE);

	    when(todoItemRepository.findByIdForUpdate(entity.getId())).thenReturn(Optional.of(entity));

	    var request = new ChangeTodoItemDescriptionRequest(newDescription);

	    assertThrows(EntityValidationException.class, () -> todoItemService.changeDescription(entity.getId(), request));
        verify(todoItemRepository, never()).save(any(TodoItem.class));
	}

}
