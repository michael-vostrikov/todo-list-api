package com.todolist.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import com.todolist.api.request.AddTodoItemRequest;
import com.todolist.api.request.ChangeTodoItemDescriptionRequest;
import com.todolist.api.response.ValidationErrorResponse;
import com.todolist.api.entity.TodoItem;
import org.springframework.http.HttpStatus;
import static org.assertj.core.api.Assertions.*;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
  locations = "classpath:application-integration-test.properties"
)
@AutoConfigureRestTestClient
class TodoItemControllerTest {
    @Autowired
    private RestTestClient restTestClient;

	@Test
	void testPastDueItemCannotBeUpdated() {
	    // create an item with soon due date
	    var addItemRequest = new AddTodoItemRequest(
	        "Item description",
	         Instant.now().plus(1, ChronoUnit.SECONDS)
         );

	    TodoItem entity = restTestClient.post()
	        .uri("/todo-item/add")
	        .body(addItemRequest)
	        .exchange()
	        .expectStatus().isOk()
	        .expectBody(TodoItem.class)
	        .returnResult()
            .getResponseBody()
        ;

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNotNull();

        // wait till @Scheduled
        try { java.lang.Thread.sleep(6000); } catch (Exception e) {}

	    var changeDescriptionRequest = new ChangeTodoItemDescriptionRequest("New item description");

        ValidationErrorResponse response = restTestClient.put()
            .uri("/todo-item/change-description?id=" + entity.getId())
            .body(changeDescriptionRequest)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .expectBody(ValidationErrorResponse.class)
            .returnResult()
            .getResponseBody()
        ;

        assertThat(response.errors).containsKey("status");

        entity.getId();
	}

}
