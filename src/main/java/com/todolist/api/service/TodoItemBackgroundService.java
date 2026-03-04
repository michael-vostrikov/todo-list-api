package com.todolist.api.service;

import com.todolist.api.entity.TodoItem;
import com.todolist.api.enums.TodoItemStatus;
import com.todolist.api.repository.TodoItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.time.Instant;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class TodoItemBackgroundService {

    @Autowired
    private TodoItemRepository todoItemRepository;

    @Autowired
    private TodoItemService todoItemService;

    @Scheduled(fixedDelay = 4000)
    public void processDueDate() {
        var limit = 1000;
        var currentDate = Instant.now();

        do {
            // we need to load entity with lock later, so we get only ids here
            // we cannot load them all with lock here, because processing can take a long time

            List<Integer> idsToProcess = todoItemRepository.findIdsByStatusAndDueDateBefore(
                TodoItemStatus.NOT_DONE,
                currentDate,
                // we always use page 0, because we filter by status here, and status changes
                PageRequest.of(0, limit)
            );

            if (idsToProcess.size() == 0) {
                break;
            }

            System.out.println("Found " + idsToProcess.size() + " records");

            for (Integer todoItemId : idsToProcess) {
                System.out.println("Processing TodoItem: " + todoItemId);

                todoItemService.markAsPastDue(todoItemId);
            }
        } while (true);
    }
}
