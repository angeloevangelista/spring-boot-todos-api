package br.com.fiap.CrudApi.controller;

import br.com.fiap.CrudApi.model.Todo;
import br.com.fiap.CrudApi.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.util.List;

@RestController
@RequestMapping(value = "/todo", produces = "application/json")
public class TodoController {
  private final TodoService todoService;

  public TodoController(TodoService todoService) {
    this.todoService = todoService;
  }

  @GetMapping()
  public List<Todo> getAll() {
    return this.todoService.findAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<Todo> getOne(@PathVariable("id") Long idTodo) {
    return ResponseEntity.of(this.todoService.findById(idTodo));
  }

  @PostMapping
  public Todo create(@RequestBody Todo todo) {
    return this.todoService.save(todo);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Todo> update(
      @PathVariable("id") Long id,
      @RequestBody Todo todo
  ) {
    return ResponseEntity.of(this.todoService.update(id, todo));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("id") Long id) {
    this.todoService.delete(id);
  }
}
