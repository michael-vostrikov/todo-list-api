package com.todolist.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import com.todolist.api.entity.TodoItem;
import com.todolist.api.enums.TodoItemStatus;
import java.util.List;
import java.util.Optional;
import java.time.Instant;

public interface TodoItemRepository
        extends JpaRepository<TodoItem, Integer>, JpaSpecificationExecutor<TodoItem> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ti FROM TodoItem ti WHERE ti.id = :id")
    Optional<TodoItem> findByIdForUpdate(Integer id);

    @Query("SELECT t.id FROM TodoItem t WHERE t.dueDate < :currentDate AND status = :status")
    List<Integer> findIdsByStatusAndDueDateBefore(TodoItemStatus status, Instant currentDate, Pageable pageable);
}
