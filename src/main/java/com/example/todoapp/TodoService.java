package com.example.todoapp;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TodoService {

    private final TodoMapper todoMapper;

    public TodoService(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public List<Todo> search(String keyword, String category, String order) {
        return todoMapper.search(keyword, category, order);
    }

    public Todo findById(Long id) {
        return todoMapper.findById(id);
    }

    public int create(Todo todo) {
        log.info("Todo created: id={}", todo.getId());
        return todoMapper.insert(todo);
    }

    public int update(Todo todo) {
        log.info("Todo updated: id={}", todo.getId());
        return todoMapper.update(todo);
    }

    public int delete(Long id) {
        log.info("Todo deleted: id={}", id);
        return todoMapper.deleteById(id);
    }
}
