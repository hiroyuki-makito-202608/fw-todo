package com.example.todoapp;

import java.util.List;
import java.time.LocalDate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TodoService {

    private final TodoMapper todoMapper;
    private final TodoCategoryMapper categoryMapper;

    public TodoService(TodoMapper todoMapper, TodoCategoryMapper categoryMapper) {
        this.todoMapper = todoMapper;
        this.categoryMapper = categoryMapper;
    }

    public List<Todo> search(String keyword, String category, String order, LocalDate from, LocalDate to) {
        return todoMapper.search(keyword, category, order, from, to);
    }

    public List<Todo> search(String keyword, String category, String order) {
        return search(keyword, category, order, null, null);
    }

    public Todo findById(Long id) {
        return todoMapper.findById(id);
    }

    public int create(Todo todo) {
        todo.setCategory(ensureCategory(todo.getCategory()));
        if (todo.getCompleted() == null) {
            todo.setCompleted(false);
        }
        if (todo.getStatus() == null || todo.getStatus().isBlank()) {
            todo.setStatus(Boolean.TRUE.equals(todo.getCompleted()) ? "COMPLETED" : "TODO");
        }
        log.info("Todo created{}", todo.getId() == null ? "" : ": id=" + todo.getId());
        return todoMapper.insert(todo);
    }

    public int update(Todo todo) {
        todo.setCategory(ensureCategory(todo.getCategory()));
        if (todo.getStatus() == null || todo.getStatus().isBlank()) {
            todo.setStatus(Boolean.TRUE.equals(todo.getCompleted()) ? "COMPLETED" : "TODO");
        }
        if (!List.of("TODO", "IN_PROGRESS", "COMPLETED").contains(todo.getStatus())) {
            throw new IllegalArgumentException("Invalid status");
        }
        log.info("Todo updated{}", todo.getId() == null ? "" : ": id=" + todo.getId());
        return todoMapper.update(todo);
    }

    public int delete(Long id) {
        log.info("Todo deleted{}", id == null ? "" : ": id=" + id);
        return todoMapper.deleteById(id);
    }

    public List<String> findCategories() {
        return categoryMapper.findAll();
    }

    public List<Todo> findAllForKanban() {
        return todoMapper.findAllForKanban();
    }

    public boolean updateStatus(Long id, String status) {
        if (!List.of("TODO", "IN_PROGRESS", "COMPLETED").contains(status)) {
            throw new IllegalArgumentException("ステータスが不正です。");
        }
        return todoMapper.updateStatus(id, status) > 0;
    }

    public void addCategory(String name) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isEmpty() || normalized.length() > 255) {
            throw new IllegalArgumentException("ジャンル名は1〜255文字で入力してください。");
        }
        categoryMapper.insertIfAbsent(normalized);
    }

    private String ensureCategory(String name) {
        if (name == null || name.isBlank() || name.trim().length() > 255) {
            throw new IllegalArgumentException("有効なジャンルを選択してください。");
        }
        String normalized = name.trim();
        categoryMapper.insertIfAbsent(normalized);
        return normalized;
    }
}
