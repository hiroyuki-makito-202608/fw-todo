package com.example.todoapp;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoSubtaskMapper {
    List<TodoSubtask> findByTodoId(@Param("todoId") Long todoId);
    int deleteByTodoId(@Param("todoId") Long todoId);
    int insert(TodoSubtask subtask);
    int updateCompleted(@Param("id") Long id, @Param("todoId") Long todoId, @Param("completed") boolean completed);
    int update(TodoSubtask subtask);
    int delete(@Param("id") Long id, @Param("todoId") Long todoId);
}
