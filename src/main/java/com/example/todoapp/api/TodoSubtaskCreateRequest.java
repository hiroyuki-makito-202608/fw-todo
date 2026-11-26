package com.example.todoapp.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TodoSubtaskCreateRequest(
        @NotBlank(message = "Subtask title is required.")
        @Size(max = 255, message = "Subtask title must be at most 255 characters.")
        String title) {
}
