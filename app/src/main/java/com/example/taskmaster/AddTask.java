package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.exception.ApolloException;

import java.util.List;

import javax.annotation.Nonnull;

import type.CreateTaskInput;

public class AddTask extends AppCompatActivity {

    public AppDatabase db;
    private List<Task> tasks;
    private RecyclerView.Adapter newTaskAdapter;

    // Instance variable for awsAppSyncClient
    AWSAppSyncClient awsAppSyncClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // ============ Connect to AWS =====================
        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();


        // ========== Room (Local) Database ================
        // database info, and database that you are connecting to - taskmaster (found in strings.xml)
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, getString(R.string.database_name))
                .allowMainThreadQueries().build();

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
                // db.taskDao().saveNewTask(newTask); // <--- saves to local database

                runAddTaskMutation(newTask);

                // Automatically let person return to prior page they were on once activity complete.
                AddTask.this.finish();
            }
        });
    }

    // ============= Add Task Stuff to AWS Amplify Database with a Mutation ==============
    // Starting code from https://aws-amplify.github.io/docs/android/start
    public void runAddTaskMutation(Task task) {
        CreateTaskInput createTaskInput = CreateTaskInput.builder()
                .title(task.getTitle())
                .body(task.getBody())
                .taskState(StatusConverter.toInt(task.getTaskState())) // <-- uses the status converter to return the int of the enum
                .build();
        awsAppSyncClient.mutate(CreateTaskMutation.builder().input(createTaskInput).build())
                .enqueue(addTaskCallback);
    }

    public GraphQLCall.Callback<CreateTaskMutation.Data> addTaskCallback = new GraphQLCall.Callback<CreateTaskMutation.Data>(){
        @Override
        public void onResponse(@Nonnull final com.apollographql.apollo.api.Response<CreateTaskMutation.Data> response) {
            Log.i("graphql insert", "added a task");
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("graphql insert", e.getMessage());
        }
    };
}
