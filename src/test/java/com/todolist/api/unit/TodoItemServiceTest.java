package com.todolist.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.todolist.api.entity.TodoItem;
import com.todolist.api.enums.TodoItemStatus;
import com.todolist.api.exception.EntityValidationException;
import com.todolist.api.exception.InvalidSortPropertyException;
import com.todolist.api.mapper.TodoItemMapper;
import com.todolist.api.repository.TodoItemRepository;
import com.todolist.api.request.AddTodoItemRequest;
import com.todolist.api.request.ChangeTodoItemDescriptionRequest;
import com.todolist.api.request.ListTodoItemRequest;
import com.todolist.api.service.TodoItemService;
import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class TodoItemServiceTest {

    private static final Instant NOW = Instant.parse("2026-02-01T00:00:00Z");

    private static final Instant DUE_DATE = NOW.plus(Duration.ofDays(1));

    private static final int ID = 1;

    @Mock
    private TodoItemRepository todoItemRepository;

    private TodoItemService todoItemService;

    @BeforeEach
    void setUp() {
        var clock = Clock.fixed(NOW, ZoneOffset.UTC);
        var mapper = Mappers.getMapper(TodoItemMapper.class);

        todoItemService = new TodoItemService(todoItemRepository, mapper, clock);
    }

    @Test
    void addCreatesNotDoneItem() {
        var response = todoItemService.add(new AddTodoItemRequest("Item description", DUE_DATE));

        var captor = ArgumentCaptor.forClass(TodoItem.class);
        verify(todoItemRepository).save(captor.capture());

        var saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(TodoItemStatus.NOT_DONE);
        assertThat(saved.getCreatedAt()).isEqualTo(NOW);
        assertThat(saved.getDueDate()).isEqualTo(DUE_DATE);

        assertThat(response.description()).isEqualTo("Item description");
        assertThat(response.status()).isEqualTo(TodoItemStatus.NOT_DONE);
    }

    @Test
    void viewThrowsWhenItemNotFound() {
        when(todoItemRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoItemService.view(ID))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void changeDescriptionAllowed() {
        var entity = notDoneItem();
        when(todoItemRepository.findByIdForUpdate(ID)).thenReturn(Optional.of(entity));

        var response = todoItemService.changeDescription(ID, new ChangeTodoItemDescriptionRequest("New item description"));

        assertThat(entity.getDescription()).isEqualTo("New item description");
        assertThat(response.description()).isEqualTo("New item description");
    }

    @Test
    void changeDescriptionForPastDueItemNotAllowed() {
        var entity = notDoneItem();
        entity.markAsPastDue();
        when(todoItemRepository.findByIdForUpdate(ID)).thenReturn(Optional.of(entity));

        var request = new ChangeTodoItemDescriptionRequest("New item description");

        assertThatThrownBy(() -> todoItemService.changeDescription(ID, request))
            .isInstanceOf(EntityValidationException.class);
        assertThat(entity.getDescription()).isEqualTo("Item description");
    }

    @Test
    void markAsDoneUsesClock() {
        var entity = notDoneItem();
        when(todoItemRepository.findByIdForUpdate(ID)).thenReturn(Optional.of(entity));

        var response = todoItemService.markAsDone(ID);

        assertThat(response.status()).isEqualTo(TodoItemStatus.DONE);
        assertThat(response.doneAt()).isEqualTo(NOW);
    }

    @Test
    void markAsNotDoneClearsDoneAt() {
        var entity = notDoneItem();
        entity.markAsDone(NOW);
        when(todoItemRepository.findByIdForUpdate(ID)).thenReturn(Optional.of(entity));

        var response = todoItemService.markAsNotDone(ID);

        assertThat(response.status()).isEqualTo(TodoItemStatus.NOT_DONE);
        assertThat(response.doneAt()).isNull();
    }

    @Test
    void markAsPastDueChangesNotDoneItem() {
        var entity = notDoneItem();
        when(todoItemRepository.findByIdForUpdate(ID)).thenReturn(Optional.of(entity));

        todoItemService.markAsPastDue(ID);

        assertThat(entity.getStatus()).isEqualTo(TodoItemStatus.PAST_DUE);
    }

    @Test
    void markAsPastDueSkipsItemDoneInMeantime() {
        var entity = notDoneItem();
        entity.markAsDone(NOW);
        when(todoItemRepository.findByIdForUpdate(ID)).thenReturn(Optional.of(entity));

        todoItemService.markAsPastDue(ID);

        assertThat(entity.getStatus()).isEqualTo(TodoItemStatus.DONE);
    }

    @Test
    void listRejectsUnknownSortProperty() {
        var pageable = PageRequest.of(0, 20, Sort.by("unknown"));
        var filter = new ListTodoItemRequest(null);

        assertThatThrownBy(() -> todoItemService.list(filter, pageable))
            .isInstanceOf(InvalidSortPropertyException.class);
        verifyNoInteractions(todoItemRepository);
    }

    private static TodoItem notDoneItem() {
        return TodoItem.create("Item description", DUE_DATE, NOW);
    }
}
