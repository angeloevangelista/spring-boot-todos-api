# Duvidas

- [Linkedin](https://linkedin.com/in/angelo-evangelista-5474a2177)

## Part 1

- [ ] Generate project
  - Spring Web
  - Spring Data JPA
  - Oracle Drive
  - Validation

- [ ] Setup Oracle Database (docker only)

  ```shell
  # Run container
  docker run -it --rm \
    --name oracle-free \
    -p 1521:1521 \
    -e ORACLE_USER=SYS \
    -e ORACLE_PWD=MyStrongPassword123 \
    container-registry.oracle.com/database/free:latest


  # Connect with DBeaver
  #
  #   db_host: localhost
  #   db_service: FREE
  #   db_user: SYS
  #   db_pass: MyStrongPassword123
  #   db_role: SYSDBA


  # Create user
  #
  #   ALTER SESSION SET CONTAINER = FREEPDB1;
  #   CREATE USER SPRING_APP IDENTIFIED BY spring_app;
  #   GRANT CREATE SESSION TO SPRING_APP;
  #   GRANT CREATE TABLE TO SPRING_APP;
  #   GRANT CREATE SEQUENCE TO SPRING_APP;
  #   ALTER USER SPRING_APP QUOTA UNLIMITED ON USERS;

  # Use this connection
  #
  #   db_host: localhost
  #   db_service: FREEPDB1
  #   db_user: SPRING_APP
  #   db_pass: spring_app
  ```

- [ ] Configure datasource on properties
  - `spring.datasource.url`
  - `spring.datasource.username`
  - `spring.datasource.password`

---

## Part 2

- [ ] Create controller: SampleController. Annotations: `@RestController`, `@RequestMapping`, `@GetMapping`, `@ResponseStatus`, `@RequestBody`, `@RequestParam`, `@PathVariable`

  ```java
  package br.com.fiap.SampleCrud.controller;

  import org.springframework.http.ResponseEntity;
  import org.springframework.web.bind.annotation.*;

  @RestController
  @RequestMapping("/api/sample")
  public class SampleController {

    @GetMapping("/health")
    public String health() {
      return "Healthy!";
    }

    @GetMapping("/hello")
    public String hello(@RequestParam String name) {
      return "Hello, person with name " + name + "!";
    }

    @GetMapping("/hello/{id}")
    public String hello(@PathVariable("id") Long id) {
      return "Hello, ID " + id + "!";
    }

    @PostMapping("/payload")
    public ResponseEntity<Object> payload(@RequestBody Object payload) {
      return ResponseEntity.ok(payload);
    }
  }
  ```

---

## Part 3

- [ ] Prepare SQL for TODOs

  ```sql
  CREATE TABLE TODOS (
    ID INTEGER GENERATED ALWAYS AS IDENTITY (
      START WITH 1
      INCREMENT BY 1
    ) PRIMARY KEY,
    TITLE VARCHAR2(100) NOT NULL,
    COMPLETED CHAR(1) CHECK (COMPLETED IN ('Y', 'N')) NOT NULL
  );
  ```

- [ ] Create model: Todo. Annotations: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`

  ```java
  package br.com.fiap.SampleCrud.models;

  import jakarta.persistence.*;

  @Entity
  @Table(name = "TODOS")
  public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "COMPLETED")
    private Character completed;

    public Todo() { }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public Character getCompleted() {
      return completed;
    }

    public void setCompleted(Character completed) {
      this.completed = completed;
    }
  }
  ```

- [ ] Add hibernate validation on properties: `spring.jpa.hibernate.ddl-auto=validate`

- [ ] Create repository: TodoRepository

  ```java
  package br.com.fiap.SampleCrud.repository;

  import br.com.fiap.SampleCrud.models.Todo;
  import org.springframework.data.jpa.repository.JpaRepository;

  public interface TodoRepository extends JpaRepository<Todo, Long> { }
  ```

- [ ] Create service: TodoService. Annotations: `@Service`

  ```java
  package br.com.fiap.SampleCrud.service;

  import br.com.fiap.SampleCrud.models.Todo;
  import br.com.fiap.SampleCrud.repository.TodoRepository;
  import org.springframework.stereotype.Service;

  import java.util.List;
  import java.util.Optional;

  @Service
  public class TodoService {
    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
      this.todoRepository = todoRepository;
    }

    public List<Todo> findAll() {
      return this.todoRepository.findAll();
    }

    public Optional<Todo> findById(Long id) {
      return this.todoRepository.findById(id);
    }

    public Todo save(Todo todo) {
      return this.todoRepository.save(todo);
    }

    public void deleteById(Long id) {
      this.todoRepository.deleteById(id);
    }

    public Optional<Todo> update(Long id, Todo todo) {
      return this.todoRepository.findById(id)
        .map(pTodo -> {
          pTodo.setTitle(todo.getTitle());
          pTodo.setCompleted(todo.getCompleted());

          return this.todoRepository.save(pTodo);
        });
    }
  }
  ```

- [ ] Create controller: TodoController

  ```java
  package br.com.fiap.SampleCrud.controller;

  import br.com.fiap.SampleCrud.models.Todo;
  import br.com.fiap.SampleCrud.service.TodoService;
  import org.springframework.http.HttpStatus;
  import org.springframework.http.ResponseEntity;
  import org.springframework.web.bind.annotation.*;

  import java.util.List;

  @RestController
  @RequestMapping("/api/todos")
  public class TodoController {
    private final TodoService todoService;

    public TodoController(TodoService todoService) {
      this.todoService = todoService;
    }

    @GetMapping()
    public List<Todo> get() {
      return this.todoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Todo> getById(@PathVariable Long id) {
      var todo = this.todoService.findById(id);

      if (todo.isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      return ResponseEntity.of(todo);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public Todo post(@RequestBody Todo todo) {
      return this.todoService.save(todo);
    }
  }
  ```
