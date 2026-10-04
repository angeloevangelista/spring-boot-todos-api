package br.com.fiap.CrudApi.service;

import br.com.fiap.CrudApi.model.Todo;
import br.com.fiap.CrudApi.repository.TodoRepository;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
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

  public void delete(Long id) {
    this.todoRepository.deleteById(id);
  }

  public Optional<Todo> update(Long id, Todo todo) {
    return this.todoRepository.findById(id).map(p -> {
      p.setTitle(todo.getTitle());
      p.setCompleted(todo.getCompleted());

      return this.todoRepository.save(p);
    });
  }
}
