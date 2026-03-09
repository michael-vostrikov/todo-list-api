package com.todolist.api.service;

import com.todolist.api.enums.TodoItemStatus;
import com.todolist.api.repository.TodoItemRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TodoItemBackgroundService {

    private final TodoItemRepository todoItemRepository;

    private final TodoItemService todoItemService;

    private final Clock clock;

    private final int batchSize;

    public TodoItemBackgroundService(
        TodoItemRepository todoItemRepository,
        TodoItemService todoItemService,
        Clock clock,
        @Value("${todo.past-due.batch-size}") int batchSize
    ) {
        this.todoItemRepository = todoItemRepository;
        this.todoItemService = todoItemService;
        this.clock = clock;
        this.batchSize = batchSize;
    }

    /**
     * Safe to run on several instances at the same time:
     * every item is changed under a row lock and its status is checked again after locking.
     */
    @Scheduled(fixedDelayString = "${todo.past-due.fixed-delay-ms}")
    public void processDueDate() {
        var now = Instant.now(clock);
        var processed = 0;

        while (true) {
            // we need to load entity with lock later, so we get only ids here
            // we cannot load them all with lock here, because processing can take a long time

            List<Integer> idsToProcess = todoItemRepository.findIdsByStatusAndDueDateBefore(
                TodoItemStatus.NOT_DONE,
                now,
                // we always use page 0, because we filter by status here, and status changes
                PageRequest.of(0, batchSize)
            );

            if (idsToProcess.isEmpty()) {
                break;
            }

            log.debug("Found {} TodoItems with passed due date", idsToProcess.size());

            for (Integer todoItemId : idsToProcess) {
                log.debug("Marking TodoItem {} as PAST_DUE", todoItemId);

                todoItemService.markAsPastDue(todoItemId);
            }

            processed += idsToProcess.size();
        }

        if (processed > 0) {
            log.info("Processed {} TodoItems with passed due date", processed);
        }
    }
}
