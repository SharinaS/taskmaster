package com.example.taskmaster;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity
public class Task {

    // == Database setup
    @PrimaryKey(autoGenerate = true)
    private long id;

    // == Enum stuff
    @TypeConverters(StatusConverter.class)
    public TaskState taskState;

    public enum TaskState {
        NEW(0),
        ASSIGNED(1),
        IN_PROGRESS(2),
        COMPLETE(3);

        private int code;

        TaskState(int code){
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }


    // == Task Instance variables
    private String title;
    private String body;
    // taskState specified with an enum
    //private TaskState taskState;

    // == Task Constructor
    public Task(String title, String body) {
        this.title = title;
        this.body = body;
        this.taskState = TaskState.NEW;
    }

    // == Setters
    public void setTaskState(TaskState taskState) {
        this.taskState = taskState;
    }

    public void setId(long id) {
        this.id = id;
    }


    // == Getters
    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public long getId() {
        return id;
    }
}
