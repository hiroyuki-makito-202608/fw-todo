package com.example.todoapp;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TodoCategoryMapper {
    List<String> findAll();
    int insertIfAbsent(String name);
}
