package com.todolist.api.repository;

import com.todolist.api.entity.TodoItem;
import com.todolist.api.enums.TodoItemStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface TodoItemRepository extends JpaRepository<TodoItem, Integer>, JpaSpecificationExecutor<TodoItem> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TodoItem t WHERE t.id = :id")
    Optional<TodoItem> findByIdForUpdate(Integer id);

    @Query("SELECT t.id FROM TodoItem t WHERE t.status = :status AND t.dueDate < :dueDate ORDER BY t.id")
    List<Integer> findIdsByStatusAndDueDateBefore(TodoItemStatus status, Instant dueDate, Pageable pageable);
}
