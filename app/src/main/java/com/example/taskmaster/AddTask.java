package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

public class AddTask extends AppCompatActivity {

    public AppDatabase db;
    private List<Task> tasks;
    private RecyclerView.Adapter newTaskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // database info, and database name of taskmaster
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "taskmaster")
                .allowMainThreadQueries().build();


        // add new task to linked list
//        tasks.add(0, newTask);

        Button buttonAddTask = findViewById((R.id.addTaskButton));
        // add event listener to the button
        buttonAddTask.setOnClickListener(new View.OnClickListener() {
            // make text Submitted! appear with button click
            @Override
            public void onClick(View event) {
                AddTask.this.findViewById(R.id.submitted).setVisibility(View.VISIBLE);

                EditText taskTitle = findViewById(R.id.taskTitle);
                EditText taskBody = findViewById(R.id.taskDescription);
                String stringTitle = taskTitle.getText().toString();
                String stringBody = taskBody.getText().toString();

                Task newTask = new Task(stringTitle, stringBody);
                db.taskDao().addTask(newTask);
            }
        });
    }
}
