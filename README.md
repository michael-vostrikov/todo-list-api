### TODO list API

This application shows simple API on Java for a service to manage TODO items.

By requirements there is no authentication.\
Application works in memory and does not save any data on disk.\
Logic is in services.\
There is 1 integration test and 2 unit tests, it should be enough to show the idea.\
I added database migrations with Flyway, because the code is supposed to be production-ready.\
Error messages are not translated.\
New item is added with default status NOT_DONE.\
List of items is sorted by createdAt by default. I used Java Specification for filters.
Route structure is a matter of agreement in a team, I used RPC-style routes.


### Tech stack

\- Java 17 and higher\
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
# build and run in Docker
docker compose up -d

# build and run on host
./mvnw spring-boot:run

# run tests in docker
docker exec -it todo-list-api ./mvnw test

# run tests on host:
./mvnw test
```

Application uses port 8080.

After running open Swagger documentation:
http://127.0.0.1:8080/swagger-ui/index.html

Routes:
```
POST /todo-item/add
GET  /todo-item/list
GET  /todo-item/view?id
PUT  /todo-item/change-description?id
PUT  /todo-item/mark-as-not-done?id
PUT  /todo-item/mark-as-done?id
```
