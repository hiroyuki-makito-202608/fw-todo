package com.example.todoapp;

import java.util.List;
import java.time.LocalDate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TodoService {

    private final TodoMapper todoMapper;
    private final TodoCategoryMapper categoryMapper;
    private final TodoSubtaskMapper subtaskMapper;

    public TodoService(TodoMapper todoMapper, TodoCategoryMapper categoryMapper, TodoSubtaskMapper subtaskMapper) {
        this.todoMapper = todoMapper;
        this.categoryMapper = categoryMapper;
        this.subtaskMapper = subtaskMapper;
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

    @Transactional
    public int create(Todo todo) {
        normalizeSubtasks(todo);
        todo.setCategory(ensureCategory(todo.getCategory()));
        if (todo.getCompleted() == null) {
            todo.setCompleted(false);
        }
        if (todo.getStatus() == null || todo.getStatus().isBlank()) {
            todo.setStatus(Boolean.TRUE.equals(todo.getCompleted()) ? "COMPLETED" : "TODO");
        }
        log.info("Todo created{}", todo.getId() == null ? "" : ": id=" + todo.getId());
        int inserted = todoMapper.insert(todo);
        saveSubtasks(todo);
        return inserted;
    }

    @Transactional
    public int update(Todo todo) {
        normalizeSubtasks(todo);
        todo.setCategory(ensureCategory(todo.getCategory()));
        if (todo.getStatus() == null || todo.getStatus().isBlank()) {
            todo.setStatus(Boolean.TRUE.equals(todo.getCompleted()) ? "COMPLETED" : "TODO");
        }
        if (!List.of("TODO", "IN_PROGRESS", "COMPLETED").contains(todo.getStatus())) {
            throw new IllegalArgumentException("Invalid status");
        }
        log.info("Todo updated{}", todo.getId() == null ? "" : ": id=" + todo.getId());
        int updated = todoMapper.update(todo);
        if (updated > 0) {
            saveSubtasks(todo);
        }
        return updated;
    }

    public List<TodoSubtask> findSubtasks(Long todoId) {
        return subtaskMapper.findByTodoId(todoId);
    }

    public List<TodoSubtask> findTodoSubtasks(Long todoId) {
        if (todoMapper.findById(todoId) == null) {
            throw new TodoSubtaskNotFoundException("Todo was not found.");
        }
        return subtaskMapper.findByTodoId(todoId);
    }

    @Transactional
    public TodoSubtask addSubtask(Long todoId, String title) {
        if (todoMapper.findByIdForUpdate(todoId) == null) {
            throw new TodoSubtaskNotFoundException("Todo was not found.");
        }
        List<TodoSubtask> current = subtaskMapper.findByTodoId(todoId);
        if (current.size() >= 10) {
            throw new IllegalArgumentException("A todo can have at most 10 subtasks.");
        }
        TodoSubtask subtask = new TodoSubtask();
        subtask.setTodoId(todoId);
        subtask.setTitle(validateSubtaskTitle(title));
        subtask.setPosition(current.stream()
                .map(TodoSubtask::getPosition)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max().orElse(-1) + 1);
        subtask.setCompleted(false);
        subtaskMapper.insert(subtask);
        return subtask;
    }

    @Transactional
    public TodoSubtask updateSubtask(Long todoId, Long subtaskId, String title,
            Boolean completed, Integer position) {
        if (todoMapper.findById(todoId) == null) {
            throw new TodoSubtaskNotFoundException("Todo was not found.");
        }
        if (title == null && completed == null && position == null) {
            throw new IllegalArgumentException("At least one subtask field must be provided.");
        }
        if (title != null) {
            title = validateSubtaskTitle(title);
        }
        if (position != null && position < 0) {
            throw new IllegalArgumentException("Position must be zero or greater.");
        }
        TodoSubtask subtask = subtaskMapper.findByTodoId(todoId).stream()
                .filter(item -> item.getId().equals(subtaskId))
                .findFirst().orElseThrow(() -> new TodoSubtaskNotFoundException("Subtask was not found."));
        if (title != null) subtask.setTitle(title);
        if (completed != null) subtask.setCompleted(completed);
        if (position != null) subtask.setPosition(position);
        subtaskMapper.update(subtask);
        return subtask;
    }

    @Transactional
    public boolean deleteSubtask(Long todoId, Long subtaskId) {
        if (todoMapper.findById(todoId) == null) {
            throw new TodoSubtaskNotFoundException("Todo was not found.");
        }
        if (subtaskMapper.delete(subtaskId, todoId) == 0) {
            throw new TodoSubtaskNotFoundException("Subtask was not found.");
        }
        return true;
    }

    public boolean updateSubtaskCompleted(Long todoId, Long subtaskId, boolean completed) {
        return subtaskMapper.updateCompleted(subtaskId, todoId, completed) > 0;
    }

    private void normalizeSubtasks(Todo todo) {
        List<String> subtasks = todo.getSubtasks() == null ? List.of() : todo.getSubtasks().stream()
                .map(value -> value == null ? "" : value.trim())
                .filter(value -> !value.isEmpty())
                .toList();
        if (subtasks.size() > 10) {
            throw new IllegalArgumentException("サブタスクは10件まで登録できます。");
        }
        if (subtasks.stream().anyMatch(value -> value.length() > 255)) {
            throw new IllegalArgumentException("サブタスクは255文字以内で入力してください。");
        }
        todo.setSubtasks(subtasks);
    }

    private String validateSubtaskTitle(String title) {
        String normalized = title == null ? "" : title.trim();
        if (normalized.isEmpty() || normalized.length() > 255) {
            throw new IllegalArgumentException("Subtask title must contain 1 to 255 characters.");
        }
        return normalized;
    }

    private void saveSubtasks(Todo todo) {
        java.util.Map<String, java.util.ArrayDeque<Boolean>> completionByTitle = new java.util.HashMap<>();
        for (TodoSubtask old : subtaskMapper.findByTodoId(todo.getId())) {
            completionByTitle.computeIfAbsent(old.getTitle(), ignored -> new java.util.ArrayDeque<>())
                    .add(Boolean.TRUE.equals(old.getCompleted()));
        }
        subtaskMapper.deleteByTodoId(todo.getId());
        for (int i = 0; i < todo.getSubtasks().size(); i++) {
            String title = todo.getSubtasks().get(i);
            java.util.ArrayDeque<Boolean> priorStates = completionByTitle.get(title);
            boolean completed = priorStates != null && !priorStates.isEmpty() && priorStates.removeFirst();
            TodoSubtask subtask = new TodoSubtask();
            subtask.setTodoId(todo.getId());
            subtask.setTitle(title);
            subtask.setPosition(i);
            subtask.setCompleted(completed);
            subtaskMapper.insert(subtask);
        }
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
