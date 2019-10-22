package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button goToAddTaskButton = findViewById(R.id.goAddTaskButton);
        goToAddTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View event) {
                Intent goToAddTask = new Intent (MainActivity.this, AddTask.class);
                MainActivity.this.startActivity(goToAddTask);
            }
        });
    }


//    utton addTaskButton = findViewById(R.id.ButtonTaskAdd);
//        addTaskButton.setOnClickListener((event) -> {
//        Intent goToAddTask = new Intent(MainActivity.this, AddTask.class);
//        MainActivity.this.startActivity(goToAddTask);
//    });
}
