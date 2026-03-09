package com.todolist.api.entity;

import com.todolist.api.enums.TodoItemStatus;
import com.todolist.api.exception.EntityValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "todo_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TodoItemStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "due_date", nullable = false)
    private Instant dueDate;

    @Column(name = "done_at")
    private Instant doneAt;

    public static TodoItem create(String description, Instant dueDate, Instant createdAt) {
        var item = new TodoItem();
        item.description = description;
        item.dueDate = dueDate;
        item.createdAt = createdAt;
        item.status = TodoItemStatus.NOT_DONE;
        return item;
    }

    public void changeDescription(String description) {
        ensureUpdateAllowed();
        this.description = description;
    }

    public void markAsDone(Instant now) {
        ensureUpdateAllowed();

        // repeated call must not move doneAt
        if (status == TodoItemStatus.DONE) {
            return;
        }

        status = TodoItemStatus.DONE;
        doneAt = now;
    }

    public void markAsNotDone() {
        ensureUpdateAllowed();

        status = TodoItemStatus.NOT_DONE;
        doneAt = null;
    }

    public void markAsPastDue() {
        if (status != TodoItemStatus.NOT_DONE) {
            throw new IllegalStateException("Only NOT_DONE item can become PAST_DUE, current status: " + status);
        }

        status = TodoItemStatus.PAST_DUE;
    }

    private void ensureUpdateAllowed() {
        if (status == TodoItemStatus.PAST_DUE) {
            throw new EntityValidationException(Map.of("status", "Cannot change entity which is in PAST_DUE status"));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TodoItem other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        // constant for the entity lifetime, id is assigned only on persist
        return getClass().hashCode();
    }
}
