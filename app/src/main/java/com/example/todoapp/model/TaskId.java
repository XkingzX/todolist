package com.example.todoapp.model;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class TaskId {
    @Exclude
    public String TaskId; //Firestore Document ID

    public <T extends TaskId> T withId(@NonNull final String id){
        this.TaskId = id;
        return (T) this; //Trả về đối tượng kiểu "TaskId" hoặc lớp con của nó
    }
}
