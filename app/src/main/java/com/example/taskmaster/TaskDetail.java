package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class TaskDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // == Accesses Extra Data From MainActivity Buttons ==

        // get the data added to the intent & use it to display this activity
        String task = getIntent().getStringExtra("task");
        // grab the label from the page
        TextView titleView = findViewById(R.id.detailTaskTitle);
        // set its text to be the item name
        titleView.setText(task);

        // Gets the body of the task to render it
        String taskBody = getIntent().getStringExtra("taskBody");
        TextView textView = findViewById(R.id.task_detail_desc);
        textView.setText(taskBody);
    }
}
