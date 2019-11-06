package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.amplify.generated.graphql.CreateTeamMutation;
import com.amazonaws.amplify.generated.graphql.ListTeamsQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import type.CreateTaskInput;
import type.CreateTeamInput;

public class AddTask extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public AppDatabase db;

    // Attaching a file
    private static final int READ_REQUEST_CODE = 42;

    // Instance variable for awsAppSyncClient
    AWSAppSyncClient awsAppSyncClient;

    final List<ListTeamsQuery.Item> teams = new LinkedList<>();
    public String teamName = null;

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

        // Grab all team data from the cloud and update spinner with the team names
        ListTeamsQuery query = ListTeamsQuery.builder().build();
        awsAppSyncClient.query(query)
                .enqueue(new GraphQLCall.Callback<ListTeamsQuery.Data>() {
            @Override
            public void onResponse(@Nonnull final Response<ListTeamsQuery.Data> response) {
                // response should have all Team data from the cloud
                // send response to the main thread in order to put data into spinner
                Handler h = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message message) {
                        teams.addAll(response.data().listTeams().items());

                        //linkedList to hold teamnames
                        LinkedList<String> teamNames = new LinkedList<>();
                        for (ListTeamsQuery.Item team : teams) {
                            teamNames.add(team.name());
                        }

                        // add teamNames to the spinner
                        Spinner spinner =  findViewById(R.id.spinner);
                        ArrayAdapter<String> adapter = new ArrayAdapter(AddTask.this,
                                android.R.layout.simple_spinner_item, teamNames);
                        // Specify the layout to use when the list of choices appears
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(adapter);

                        // When something gets picked, tell AddTask
                        spinner.setOnItemSelectedListener(AddTask.this);

                        // ToDo: Add nameOfTeam and teamId to saved preferences, just like in Settings page, so when add task button is pushed, those things update the main activity page.

                    }
                };
                h.obtainMessage().sendToTarget();
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {

            }
        });

        // Add a Task Button with Listener
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

    // ============= Add Task Stuff to AWS Amplify Database (with a Mutation) ==============
    // Starting code from https://aws-amplify.github.io/docs/android/start
    public void runAddTaskMutation(Task task) {
        CreateTaskInput createTaskInput = CreateTaskInput.builder()
                .taskTeamId(teamName)
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
            Log.i("graphqlinsert", "added a task");
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("graphql insert", e.getMessage());
        }
    };
    // =========================

    // methods from spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        teamName = teams.get(position).id();
        Log.i("sharina", teamName); // shows the id that is in the database of the team chosen in dropdown
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    // =========== Pick a File using S3 ================
    // https://developer.android.com/guide/topics/providers/document-provider
    public void pickFile (View v) {
        Intent intent = new Intent (Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
        // note that in the view, the common attributes for button is set to pickFile in the onClick option
    }

}


