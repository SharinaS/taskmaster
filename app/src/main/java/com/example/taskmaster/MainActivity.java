package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Task> tasks;

    @Override
    protected void onResume() {
        super.onResume();
        // grab username from sharedprefs and use it to update the label
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("username", "user");
        TextView nameTextView = findViewById(R.id.helloTextView);
        nameTextView.setText("Hello " + username + "!");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hardcoded Tasks for Recycler View
        this.tasks = new LinkedList<>();
        tasks.add(new Task("Lab Homework", "Finish Thurs Lab"));
        tasks.add(new Task("Workout", "Take a walk today"));
        tasks.add(new Task("Retro Homework", "Do Retro for Thurs lab"));

        // Render Task Items to the screen, in RecyclerView
        // starter code at: // https://developer.android.com/guide/topics/ui/layout/recyclerview
        RecyclerView recyclerView = findViewById(R.id.tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Specify an Adapter
        recyclerView.setAdapter(new TaskAdapter());


        // Button that takes user to Add Task Page
        Button goToAddTaskButton = findViewById(R.id.goAddTaskButton);
        goToAddTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View event) {
                Intent goToAddTaskView = new Intent (MainActivity.this, AddTask.class);
                MainActivity.this.startActivity(goToAddTaskView);
            }
        });

        // Button that takes user to All Tasks Page
        Button goToAllTasksButton = findViewById(R.id.goAllTasksButton);
        goToAllTasksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View event) {
                Intent goToAllTasksView = new Intent (MainActivity.this, AllTasks.class);
                MainActivity.this.startActivity(goToAllTasksView);
            }
        });

//        // Task Button called Go Food Shopping should send user to detail view and show new Task Title
//        final Button foodShoppingTaskButton = findViewById(R.id.taskShopping);
//        foodShoppingTaskButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View event) {
//                // go to the other activity... create the intent to start that activity
//                Intent goTofoodShoppingTaskDetail = new Intent(MainActivity.this, TaskDetail.class);
//
//                // add extra info about that task
//                goTofoodShoppingTaskDetail.putExtra("task", foodShoppingTaskButton.getText().toString());
//
//                // start the activity
//                MainActivity.this.startActivity(goTofoodShoppingTaskDetail);
//            }
//        });
//
//        // Second Task Button - Homework To Do - stores extra info and sends user to Details Page
//        final Button homeworkToDoButton = findViewById(R.id.taskDoHomework);
//        homeworkToDoButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View event) {
//                // go to the other activity... create the intent to start that activity
//                Intent homeworkTask = new Intent(MainActivity.this, TaskDetail.class);
//
//                // add extra info about that task
//                homeworkTask.putExtra("task", homeworkToDoButton.getText().toString());
//
//                // start the activity
//                MainActivity.this.startActivity(homeworkTask);
//            }
//        });
//
//        // Third task button - Workout Goals - stores extra info and sends user to Details Page
//        final Button workoutGoalsButton = findViewById(R.id.taskWorkout);
//        workoutGoalsButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View event) {
//                // go to the other activity... create the intent to start that activity
//                Intent workoutGoalsTask = new Intent(MainActivity.this, TaskDetail.class);
//
//                // add extra info about that task
//                workoutGoalsTask.putExtra("task", workoutGoalsButton.getText().toString());
//
//                // start the activity
//                MainActivity.this.startActivity(workoutGoalsTask);
//            }
//        });
    }

    // Starts activity over in Settings
    public void goToSettingsActivity(View v) {
        Intent i = new Intent(this, Settings.class);
        this.startActivity(i);
    }
}
