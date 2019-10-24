package com.example.taskmaster;

public class Task {
    // title, body and a state
    // state should be new, assigned, in progress or complete
    // enum -

    private String title;
    private String body;
    private TaskState taskState;


    public Task(String title, String body) {
        this.title = title;
        this.body = body;
        this.taskState = TaskState.NEW;
    }

    public void setTaskState(TaskState taskState) {
        this.taskState = taskState;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }
}
