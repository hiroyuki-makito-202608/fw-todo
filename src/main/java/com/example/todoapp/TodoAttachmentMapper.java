package com.example.todoapp;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoAttachmentMapper {
    int insert(TodoAttachment attachment);
    TodoAttachment findById(@Param("id") Long id);
    List<TodoAttachment> findByTodoId(@Param("todoId") Long todoId);
    int deleteById(@Param("id") Long id);
    int deleteByTodoId(@Param("todoId") Long todoId);
}
