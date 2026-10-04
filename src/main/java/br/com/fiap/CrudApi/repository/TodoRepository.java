package br.com.fiap.CrudApi.repository;

import br.com.fiap.CrudApi.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> { }

/**
 * class Pessoa<T> {
 *   private T objetoPreferido;
 * }
 */
