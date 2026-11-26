package com.example.todoapp;

public class TodoSubtaskNotFoundException extends RuntimeException {
    public TodoSubtaskNotFoundException(String message) {
        super(message);
    }
}
