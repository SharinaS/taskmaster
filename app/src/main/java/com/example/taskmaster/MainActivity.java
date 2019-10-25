package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskItemInteractionListener {

    private List<Task> tasks;
    public AppDatabase db;

    private RecyclerView.Adapter taskAdapter;

    @Override
    protected void onResume() {
        super.onResume();
        // grab username from sharedprefs and use it to update the label that displays username
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("username", "user");
        TextView nameTextView = findViewById(R.id.helloTextView);
        nameTextView.setText("Hello " + username + "!");

        // Database Info with database name of taskmaster
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "taskmaster")
                .allowMainThreadQueries().build();

        // List of Tasks for Recycler View
        this.tasks = new LinkedList<>();

        // get everything from the database (addAll is a built in method) then add all that stuff to the list.
        // We're returning the list from getall, which is being added to tasks.
        this.tasks.addAll(db.taskDao().getall());

//        tasks.add(new Task("Lab Homework", "Finish Thurs Lab"));
//        tasks.add(new Task("Workout", "Take a walk today"));
//        tasks.add(new Task("Retro Homework", "Do Retro for Thurs lab"));

        // Render Task Items to the screen, in RecyclerView
        // starter code at: // https://developer.android.com/guide/topics/ui/layout/recyclerview
        RecyclerView recyclerView = findViewById(R.id.tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Specify an Adapter
        recyclerView.setAdapter(new TaskAdapter(tasks, this));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



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
    }

    // === Starts activity over in Settings ===
    public void goToSettingsActivity(View v) {
        Intent i = new Intent(this, Settings.class);
        this.startActivity(i);
    }

    // === Takes user to detail page when a task is clicked on ===
    @Override
    public void taskItemClickedOn(Task task) {
        // go to the other activity... create the intent to start that activity
        Intent clickedOnTask = new Intent(MainActivity.this, TaskDetail.class);

        // add extra info about that task
        clickedOnTask.putExtra("task", task.getTitle());
        clickedOnTask.putExtra("taskBody", task.getBody());

        // start the activity
        MainActivity.this.startActivity(clickedOnTask);
    }
}
