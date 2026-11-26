package com.example.todoapp;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TodoAttachment {
    private Long id;
    private Long todoId;
    private String originalFileName;
    private String storedFileName;
    private String contentType;
    private Long fileSize;
    private LocalDateTime createdAt;
}
