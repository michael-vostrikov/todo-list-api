package com.todolist.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.todolist.api.entity.TodoItem;
import com.todolist.api.enums.TodoItemStatus;
import com.todolist.api.exception.EntityValidationException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TodoItemTest {

    private static final Instant NOW = Instant.parse("2026-02-01T00:00:00Z");

    private static final Instant DUE_DATE = NOW.plus(Duration.ofDays(1));

    @Test
    void createdItemIsNotDone() {
        var item = TodoItem.create("Item description", DUE_DATE, NOW);

        assertThat(item.getStatus()).isEqualTo(TodoItemStatus.NOT_DONE);
        assertThat(item.getDescription()).isEqualTo("Item description");
        assertThat(item.getDueDate()).isEqualTo(DUE_DATE);
        assertThat(item.getCreatedAt()).isEqualTo(NOW);
        assertThat(item.getDoneAt()).isNull();
    }

    @Test
    void markAsDoneSetsDoneAt() {
        var item = TodoItem.create("Item description", DUE_DATE, NOW);

        item.markAsDone(NOW);

        assertThat(item.getStatus()).isEqualTo(TodoItemStatus.DONE);
        assertThat(item.getDoneAt()).isEqualTo(NOW);
    }

    @Test
    void markAsDoneIsIdempotent() {
        var item = TodoItem.create("Item description", DUE_DATE, NOW);

        item.markAsDone(NOW);
        item.markAsDone(NOW.plusSeconds(60));

        assertThat(item.getDoneAt()).isEqualTo(NOW);
    }

    @Test
    void markAsNotDoneClearsDoneAt() {
        var item = TodoItem.create("Item description", DUE_DATE, NOW);
        item.markAsDone(NOW);

        item.markAsNotDone();

        assertThat(item.getStatus()).isEqualTo(TodoItemStatus.NOT_DONE);
        assertThat(item.getDoneAt()).isNull();
    }

    @Test
    void pastDueItemCannotBeChanged() {
        var item = TodoItem.create("Item description", DUE_DATE, NOW);
        item.markAsPastDue();

        assertThatThrownBy(() -> item.changeDescription("New item description"))
            .isInstanceOf(EntityValidationException.class);
        assertThatThrownBy(() -> item.markAsDone(NOW))
            .isInstanceOf(EntityValidationException.class);
        assertThatThrownBy(item::markAsNotDone)
            .isInstanceOf(EntityValidationException.class);

        assertThat(item.getStatus()).isEqualTo(TodoItemStatus.PAST_DUE);
        assertThat(item.getDescription()).isEqualTo("Item description");
    }

    @Test
    void doneItemCannotBecomePastDue() {
        var item = TodoItem.create("Item description", DUE_DATE, NOW);
        item.markAsDone(NOW);

        assertThatThrownBy(item::markAsPastDue)
            .isInstanceOf(IllegalStateException.class);
    }
}
