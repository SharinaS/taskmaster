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
import com.amazonaws.amplify.generated.graphql.CreateTeamMutation;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.List;

import javax.annotation.Nonnull;

import type.CreateTaskInput;
import type.CreateTeamInput;

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



        // Connect to AWS
        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        //=================== Create a Mutation - Run app and go to AddTask View =======================
//        CreateTeamInput input = CreateTeamInput.builder()
//                .name("TeamThree")
//                .build();
//        CreateTeamMutation createTeamMutation = CreateTeamMutation.builder().input(input).build();
//        awsAppSyncClient.mutate(createTeamMutation).enqueue(new GraphQLCall.Callback<CreateTeamMutation.Data>() {
//            @Override
//            public void onResponse(@Nonnull Response<CreateTeamMutation.Data> response) {
//                Log.i("sharina", "successful mutation");
//            }
//
//            @Override
//            public void onFailure(@Nonnull ApolloException e) {
//                Log.i("sharina", "failure for mutation");
//            }
//        });
        //===========================================

        // Add Task Button with Listener
        Button buttonAddTask = findViewById((R.id.addTaskButton));
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

                // add task to dynamoDB
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


