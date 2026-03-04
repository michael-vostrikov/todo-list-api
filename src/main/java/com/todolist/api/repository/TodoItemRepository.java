package com.todolist.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import com.todolist.api.entity.TodoItem;
import java.util.Optional;

public interface TodoItemRepository
        extends JpaRepository<TodoItem, Integer>, JpaSpecificationExecutor<TodoItem> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ti FROM TodoItem ti WHERE ti.id = :id")
    Optional<TodoItem> findByIdForUpdate(Integer id);

}
