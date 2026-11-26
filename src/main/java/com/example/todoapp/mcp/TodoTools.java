package com.example.todoapp.mcp;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import com.example.todoapp.Todo;
import com.example.todoapp.TodoService;
import com.example.todoapp.api.HolidayClient;
import com.example.todoapp.api.TodoDto;

@Component
public class TodoTools {

    private final TodoService todoService;
    private final HolidayClient holidayClient;

    public TodoTools(TodoService todoService, HolidayClient holidayClient) {
        this.todoService = todoService;
        this.holidayClient = holidayClient;
    }

    @McpTool(name = "list_todos", description = "\u3084\u308B\u3053\u3068\u306E\u4E00\u89A7\u3092\u8FD4\u3059\uFF08\u671F\u9593\u30FB\u30B8\u30E3\u30F3\u30EB\u3067\u7D5E\u308C\u308B\uFF09")
    public List<TodoDto> listTodos(
            @McpToolParam(required = false, description = "\u3084\u308B\u3053\u3068\u306E\u540D\u524D\u306E\u4E00\u90E8") String keyword,
            @McpToolParam(required = false, description = "\u30B8\u30E3\u30F3\u30EB") String category,
            @McpToolParam(required = false, description = "\u958B\u59CB\u65E5\uFF08yyyy-MM-dd\uFF09") String from,
            @McpToolParam(required = false, description = "\u7D42\u4E86\u65E5\uFF08yyyy-MM-dd\uFF09") String to) {
        LocalDate fromDate = parseDate(from);
        LocalDate toDate = parseDate(to);
        return todoService.search(keyword, category, "asc", fromDate, toDate).stream()
                .map(TodoDto::from)
                .toList();
    }

    @McpTool(name = "get_todo", description = "\u3084\u308B\u3053\u3068\u30921\u4EF6\u8FD4\u3059")
    public TodoDto getTodo(@McpToolParam(required = true, description = "Todo ID") Long id) {
        Todo todo = todoService.findById(id);
        return todo == null ? null : TodoDto.from(todo);
    }

    @McpTool(name = "add_todo", description = "\u3084\u308B\u3053\u3068\u30921\u4EF6\u8DB3\u3059")
    public TodoDto addTodo(
            @McpToolParam(required = true, description = "Title") String title,
            @McpToolParam(required = true, description = "Category") String category,
            @McpToolParam(required = true, description = "Priority") Integer priority,
            @McpToolParam(required = false, description = "Detail") String detail,
            @McpToolParam(required = false, description = "Due date (yyyy-MM-dd)") String dueDate,
            @McpToolParam(required = false, description = "Completed state") Boolean completed) {
        validateCategory(category);
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setDetail(detail);
        todo.setCategory(category);
        todo.setPriority(priority);
        todo.setDueDate(parseDate(dueDate));
        todo.setCompleted(completed == null ? false : completed);
        todoService.create(todo);
        Todo created = todoService.findById(todo.getId());
        return created == null ? null : TodoDto.from(created);
    }

    @McpTool(name = "update_todo", description = "\u3084\u308B\u3053\u3068\u30921\u4EF6\u76F4\u3059\uFF08\u671F\u9650\u3092\u5909\u3048\u308B\u306E\u3082\u3053\u308C\uFF09")
    public TodoDto updateTodo(
            @McpToolParam(required = true, description = "Todo ID") Long id,
            @McpToolParam(required = false, description = "Title") String title,
            @McpToolParam(required = false, description = "Detail") String detail,
            @McpToolParam(required = false, description = "Category") String category,
            @McpToolParam(required = false, description = "Priority") Integer priority,
            @McpToolParam(required = false, description = "Due date (yyyy-MM-dd)") String dueDate,
            @McpToolParam(required = false, description = "Completed state") Boolean completed) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            return null;
        }
        if (title != null) todo.setTitle(title);
        if (detail != null) todo.setDetail(detail);
        if (category != null) {
            validateCategory(category);
            todo.setCategory(category);
        }
        if (priority != null) todo.setPriority(priority);
        if (dueDate != null) todo.setDueDate(LocalDate.parse(dueDate));
        if (completed != null) todo.setCompleted(completed);
        todoService.update(todo);
        Todo updated = todoService.findById(id);
        return updated == null ? null : TodoDto.from(updated);
    }

    @McpTool(name = "complete_todo", description = "\u3084\u308B\u3053\u3068\u3092\u5B8C\u4E86\u306B\u3059\u308B")
    public TodoDto completeTodo(@McpToolParam(required = true, description = "Todo ID") Long id) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            return null;
        }
        todo.setCompleted(true);
        todoService.update(todo);
        Todo completed = todoService.findById(id);
        return completed == null ? null : TodoDto.from(completed);
    }

    @McpTool(name = "delete_todo", description = "\u3084\u308B\u3053\u3068\u30921\u4EF6\u6D88\u3059")
    public boolean deleteTodo(@McpToolParam(required = true, description = "Todo ID") Long id) {
        if (todoService.findById(id) == null) {
            return false;
        }
        todoService.delete(id);
        return true;
    }

    @McpTool(name = "find_free_days", description = "\u671F\u9593\u306E\u4E2D\u3067\u3001\u671F\u9650\u306E\u3084\u308B\u3053\u3068\u304C\u7121\u304F\u3001\u571F\u65E5\u3067\u3082\u795D\u65E5\u3067\u3082\u306A\u3044\u300C\u7A7A\u3044\u3066\u3044\u308B\u65E5\u300D\u3092\u8FD4\u3059\u3002\u3084\u308B\u3053\u3068\u306E\u671F\u9650\u3092\u52D5\u304B\u3059\u5148\u3092\u6C7A\u3081\u308B\u306E\u306B\u4F7F\u3046")
    public List<LocalDate> findFreeDays(
            @McpToolParam(required = true, description = "Start date (yyyy-MM-dd)") String from,
            @McpToolParam(required = true, description = "End date (yyyy-MM-dd)") String to) {
        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);
        Set<LocalDate> dueDates = todoService.search(null, null, "asc", start, end).stream()
                .map(Todo::getDueDate)
                .filter(date -> date != null)
                .collect(java.util.stream.Collectors.toSet());
        Set<String> holidays = holidayClient.getHolidays().holidays().keySet();

        return start.datesUntil(end.plusDays(1))
                .filter(date -> !dueDates.contains(date))
                .filter(date -> date.getDayOfWeek() != DayOfWeek.SATURDAY
                        && date.getDayOfWeek() != DayOfWeek.SUNDAY)
                .filter(date -> !holidays.contains(date.toString()))
                .toList();
    }

    private LocalDate parseDate(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    private void validateCategory(String category) {
        if (category == null || category.isBlank() || category.length() > 255) {
            throw new IllegalArgumentException("Category must contain 1 to 255 characters");
        }
    }
}
