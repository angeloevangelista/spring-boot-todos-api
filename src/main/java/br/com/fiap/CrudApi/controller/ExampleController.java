package br.com.fiap.CrudApi.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/example")
public class ExampleController {
    // http://142.251.133.78/search?q=bolo+de+café

    // HTTP Methods/Verbs:
    //  GET -> Pegar
    //  POST -> Criar
    //  PUT -> Atualizar
    //  DELETE -> Excluir

    @GetMapping
    public String sayHello() {
      return "Hello, World!";
    }

    @GetMapping("/hello")
    public String sayMyName(@RequestParam(required = false) String name) {
      return String.format("Hello, %s", name);
    }
}
