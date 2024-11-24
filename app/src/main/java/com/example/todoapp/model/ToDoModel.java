package com.example.todoapp.model;

public class ToDoModel extends TaskId{

    private String task, due;
    private int status;

    public String getDue() {
        return due;
    }

    public String getTask() {
        return task;
    }

    public int getStatus() {
        return status;
    }
}
