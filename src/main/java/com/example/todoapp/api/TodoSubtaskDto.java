package com.example.todoapp.api;

import com.example.todoapp.TodoSubtask;

public record TodoSubtaskDto(Long id, Long todoId, String title, Integer position, Boolean completed) {
    public static TodoSubtaskDto from(TodoSubtask subtask) {
        return new TodoSubtaskDto(subtask.getId(), subtask.getTodoId(), subtask.getTitle(),
                subtask.getPosition(), subtask.getCompleted());
    }
}
