package com.example.todoapp.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record TodoSubtaskUpdateRequest(
        @Size(max = 255, message = "Subtask title must be at most 255 characters.")
        String title,
        Boolean completed,
        @Min(value = 0, message = "Position must be zero or greater.")
        Integer position) {
}
