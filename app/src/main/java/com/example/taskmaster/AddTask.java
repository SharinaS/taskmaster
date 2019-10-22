package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AddTask extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        Button buttonAddTask = findViewById((R.id.addTaskButton));
        // add event listener to the button
        buttonAddTask.setOnClickListener(new View.OnClickListener() {
            // make text Submitted! appear with button click
            @Override
            public void onClick(View event) {
//                TextView editSubmitted = findViewById(R.id.submitted);
//                editSubmitted.setVisibility(View.VISIBLE);
                AddTask.this.findViewById(R.id.submitted).setVisibility(View.VISIBLE);
            }
        });
    }
}
