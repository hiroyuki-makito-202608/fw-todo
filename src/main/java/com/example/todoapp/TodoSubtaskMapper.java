package com.example.todoapp;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoSubtaskMapper {
    List<TodoSubtask> findByTodoId(@Param("todoId") Long todoId);
    int deleteByTodoId(@Param("todoId") Long todoId);
    int insert(@Param("todoId") Long todoId, @Param("title") String title,
            @Param("position") int position, @Param("completed") boolean completed);
    int updateCompleted(@Param("id") Long id, @Param("todoId") Long todoId, @Param("completed") boolean completed);
}
