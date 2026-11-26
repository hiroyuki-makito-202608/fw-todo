package com.example.todoapp;

import lombok.Data;

@Data
public class TodoSubtask {
    private Long id;
    private Long todoId;
    private String title;
    private Integer position;
    private Boolean completed;
}
