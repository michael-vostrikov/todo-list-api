package com.todolist.api.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.todolist.api.enums.TodoItemStatus;
import com.todolist.api.repository.TodoItemRepository;
import com.todolist.api.service.TodoItemBackgroundService;
import com.todolist.api.service.TodoItemService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TodoItemBackgroundServiceTest {

    private static final Instant NOW = Instant.parse("2026-02-01T00:00:00Z");

    @Mock
    private TodoItemRepository todoItemRepository;

    @Mock
    private TodoItemService todoItemService;

    private TodoItemBackgroundService backgroundService;

    @BeforeEach
    void setUp() {
        var clock = Clock.fixed(NOW, ZoneOffset.UTC);

        backgroundService = new TodoItemBackgroundService(todoItemRepository, todoItemService, clock, 2);
    }

    @Test
    void marksAllFoundItemsAsPastDue() {
        when(todoItemRepository.findIdsByStatusAndDueDateBefore(eq(TodoItemStatus.NOT_DONE), eq(NOW), any(Pageable.class)))
            .thenReturn(List.of(1, 2), List.of(3), List.of());

        backgroundService.processDueDate();

        verify(todoItemService).markAsPastDue(1);
        verify(todoItemService).markAsPastDue(2);
        verify(todoItemService).markAsPastDue(3);
    }

    @Test
    void doesNothingWhenNoItemsFound() {
        when(todoItemRepository.findIdsByStatusAndDueDateBefore(eq(TodoItemStatus.NOT_DONE), eq(NOW), any(Pageable.class)))
            .thenReturn(List.of());

        backgroundService.processDueDate();

        verifyNoInteractions(todoItemService);
    }
}
