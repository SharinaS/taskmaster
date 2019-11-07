package com.example.taskmaster;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.amazonaws.amplify.generated.graphql.ListTasksQuery;

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
    private String image;
    // taskState specified with an enum
    //private TaskState taskState;

    // == Task Constructor
    public Task(String title, String body, String image) {
        this.title = title;
        this.body = body;
        this.image = image;
        this.taskState = TaskState.NEW;

    }

    // == Overloaded Task Constructor for AWS method getAllTasksCallback()
    public Task(ListTasksQuery.Item item) {
        this.title = item.title();
        this.body = item.body();
        this.taskState = StatusConverter.toStatus(item.taskState());
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

    public TaskState getTaskState() {
        return taskState;
    }

    public String getImage() {
        return image;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", taskState=" + taskState +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
