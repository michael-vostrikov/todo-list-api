### TODO list API

This application shows simple API on Java for a service to manage TODO items.

By requirements there is no authentication.\
Application works in memory and does not save any data on disk.\
Status transitions are in the entity, services orchestrate transactions and locking.\
There are unit tests for the entity and services, and integration tests for every endpoint.\
I added database migrations with Flyway, because the code is supposed to be production-ready.\
Error messages are not translated. Errors are returned as RFC 9457 problem details, validation errors are in the `errors` property.\
New item is added with default status NOT_DONE.\
List of items is sorted by createdAt by default. I used Java Specification for filters.\
Items with passed due date are marked as PAST_DUE by a background job, it is safe to run on several instances.\
Route structure is a matter of agreement in a team, I used RPC-style routes.


### Tech stack

\- Java 21\
\- Maven 3.9\
\- Spring Boot 4\
\- H2 database\
\- Docker\
\- Database migrations with Flyway\
\- Input data validation with Jakarta Validation\
\- Unit and integration tests\
\- Swagger


### Technical details

```
# build and run in Docker (dev profile)
docker compose up -d

# build and run on host (dev profile)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# build and run production image
docker compose -f docker-compose.prod.yml up -d --build

# run tests in docker
docker exec -it todo-list-api ./mvnw test

# run tests on host:
./mvnw test
```

Application uses port 8080.

Swagger documentation and H2 console are enabled only in the `dev` profile:\
http://127.0.0.1:8080/swagger-ui/index.html

Health check: http://127.0.0.1:8080/actuator/health

Routes:
```
POST  /todo-item/add
GET   /todo-item/list
GET   /todo-item/view?id
PATCH /todo-item/change-description?id
POST  /todo-item/mark-as-not-done?id
POST  /todo-item/mark-as-done?id
```
