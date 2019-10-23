package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class TaskDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);


        // get the data added to the intent & use it to display this activity
        String task = getIntent().getStringExtra("task");

        // grab the label from the page
        TextView view = findViewById(R.id.detailTaskTitle);
        // set its text to be the item name
        view.setText(task);
    }
}
