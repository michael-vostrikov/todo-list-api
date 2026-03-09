package com.todolist.api.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.todolist.api.enums.TodoItemStatus;
import com.todolist.api.repository.TodoItemRepository;
import com.todolist.api.request.AddTodoItemRequest;
import com.todolist.api.request.ChangeTodoItemDescriptionRequest;
import com.todolist.api.response.TodoItemResponse;
import com.todolist.api.service.TodoItemBackgroundService;
import com.todolist.api.support.MutableClock;
import com.todolist.api.support.TestClockConfig;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@AutoConfigureRestTestClient
@Import(TestClockConfig.class)
class TodoItemControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private TodoItemRepository todoItemRepository;

    @Autowired
    private TodoItemBackgroundService todoItemBackgroundService;

    @Autowired
    private MutableClock clock;

    @BeforeEach
    void setUp() {
        todoItemRepository.deleteAll();
        // database stores microseconds, keep test time comparable with stored values
        clock.setInstant(Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }

    @Test
    void addReturnsCreatedItem() {
        var result = restTestClient.post()
            .uri("/todo-item/add")
            .body(new AddTodoItemRequest("Item description", dueDateInFuture()))
            .exchange()
            .expectStatus().isCreated()
            .expectBody(TodoItemResponse.class)
            .returnResult();

        var item = result.getResponseBody();

        assertThat(item).isNotNull();
        assertThat(item.id()).isNotNull();
        assertThat(item.status()).isEqualTo(TodoItemStatus.NOT_DONE);
        assertThat(item.createdAt()).isEqualTo(clock.instant());
        assertThat(result.getResponseHeaders().getLocation()).isNotNull();
        assertThat(result.getResponseHeaders().getLocation().toString())
            .endsWith("/todo-item/view?id=" + item.id());
    }

    @Test
    void addRejectsInvalidRequest() {
        restTestClient.post()
            .uri("/todo-item/add")
            .body(new AddTodoItemRequest("", Instant.now().minus(Duration.ofDays(1))))
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.errors.description").exists()
            .jsonPath("$.errors.dueDate").exists();
    }

    @Test
    void viewReturnsItem() {
        var created = add();

        var item = restTestClient.get()
            .uri("/todo-item/view?id={id}", created.id())
            .exchange()
            .expectStatus().isOk()
            .expectBody(TodoItemResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(item).isEqualTo(created);
    }

    @Test
    void viewReturnsNotFoundForUnknownItem() {
        restTestClient.get()
            .uri("/todo-item/view?id={id}", Integer.MAX_VALUE)
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void viewRejectsNonPositiveId() {
        restTestClient.get()
            .uri("/todo-item/view?id=0")
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.errors.id").exists();
    }

    @Test
    void listFiltersByStatus() {
        add();
        var done = add();
        markAsDone(done.id());

        restTestClient.get()
            .uri("/todo-item/list?status=DONE")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.page.totalElements").isEqualTo(1)
            .jsonPath("$.content[0].id").isEqualTo(done.id());
    }

    @Test
    void listRejectsUnknownSortProperty() {
        restTestClient.get()
            .uri("/todo-item/list?sort=unknown")
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.errors.sort").exists();
    }

    @Test
    void changeDescriptionUpdatesItem() {
        var created = add();

        var item = restTestClient.patch()
            .uri("/todo-item/change-description?id={id}", created.id())
            .body(new ChangeTodoItemDescriptionRequest("New item description"))
            .exchange()
            .expectStatus().isOk()
            .expectBody(TodoItemResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(item).isNotNull();
        assertThat(item.description()).isEqualTo("New item description");
    }

    @Test
    void changeDescriptionRejectsEmptyDescription() {
        var created = add();

        restTestClient.patch()
            .uri("/todo-item/change-description?id={id}", created.id())
            .body(new ChangeTodoItemDescriptionRequest(""))
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.errors.description").exists();
    }

    @Test
    void markAsDoneIsIdempotent() {
        var created = add();

        var first = markAsDone(created.id());

        clock.advance(Duration.ofMinutes(1));
        var second = markAsDone(created.id());

        assertThat(first.status()).isEqualTo(TodoItemStatus.DONE);
        assertThat(first.doneAt()).isEqualTo(clock.instant().minus(Duration.ofMinutes(1)));
        assertThat(second.doneAt()).isEqualTo(first.doneAt());
    }

    @Test
    void markAsNotDoneClearsDoneAt() {
        var created = add();
        markAsDone(created.id());

        var item = restTestClient.post()
            .uri("/todo-item/mark-as-not-done?id={id}", created.id())
            .exchange()
            .expectStatus().isOk()
            .expectBody(TodoItemResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(item).isNotNull();
        assertThat(item.status()).isEqualTo(TodoItemStatus.NOT_DONE);
        assertThat(item.doneAt()).isNull();
    }

    @Test
    void pastDueItemCannotBeUpdated() {
        var created = add();

        clock.advance(Duration.ofHours(2));
        todoItemBackgroundService.processDueDate();

        restTestClient.get()
            .uri("/todo-item/view?id={id}", created.id())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo(TodoItemStatus.PAST_DUE.name());

        restTestClient.patch()
            .uri("/todo-item/change-description?id={id}", created.id())
            .body(new ChangeTodoItemDescriptionRequest("New item description"))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .expectBody()
            .jsonPath("$.errors.status").exists();

        restTestClient.post()
            .uri("/todo-item/mark-as-done?id={id}", created.id())
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    private TodoItemResponse add() {
        var item = restTestClient.post()
            .uri("/todo-item/add")
            .body(new AddTodoItemRequest("Item description", dueDateInFuture()))
            .exchange()
            .expectStatus().isCreated()
            .expectBody(TodoItemResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(item).isNotNull();
        return item;
    }

    private TodoItemResponse markAsDone(int id) {
        var item = restTestClient.post()
            .uri("/todo-item/mark-as-done?id={id}", id)
            .exchange()
            .expectStatus().isOk()
            .expectBody(TodoItemResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(item).isNotNull();
        return item;
    }

    // @Future is checked against the system time, not against the test clock
    private static Instant dueDateInFuture() {
        return Instant.now().plus(Duration.ofHours(1)).truncatedTo(ChronoUnit.MILLIS);
    }
}
