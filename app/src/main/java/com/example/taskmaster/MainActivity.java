package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.exception.ApolloException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskItemInteractionListener {


    private List<Task> tasks;
    public AppDatabase db;


    // Instance variable for recycler view
    RecyclerView recyclerView;

    // Instance variable for recycler view adapter
    private RecyclerView.Adapter taskAdapter;

    // Instance variable for awsAppSyncClient
    AWSAppSyncClient awsAppSyncClient;

    // main activity tag for logging
    private static final String TAG = "MainActivity"; // this helps filter logs


    @Override
    protected void onResume() {
        super.onResume();

        // grab username from sharedprefs and use it to update the label that displays username
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("username", "user");

        Log.w(TAG, username); // logging of username string

        TextView nameTextView = findViewById(R.id.helloTextView);
        nameTextView.setText("Hello " + username + "!");

        // ============ Connect to AWS =====================
        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        // run graphql queries
        queryAllTasks();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tasks = new LinkedList<>();

        // ====== Recycler and Adapter Code =========
        // starter code at: // https://developer.android.com/guide/topics/ui/layout/recyclerview
        recyclerView = findViewById(R.id.tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new TaskAdapter(tasks, this));

        // ===== Buttons =========
        //  Button that takes user to Add Task Page
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


    // ======== Query the AWS DynamoDB ==============
    public void queryAllTasks() {
        Log.i("graphqlgetall", "made it into queryAllTasks method");
        awsAppSyncClient.query(ListTasksQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(getAllTasksCallback);
    }

    public GraphQLCall.Callback<ListTasksQuery.Data> getAllTasksCallback = new GraphQLCall.Callback<ListTasksQuery.Data>() {
        @Override
        public void onResponse(@Nonnull final com.apollographql.apollo.api.Response<ListTasksQuery.Data> response) {
            // log will show a list of task items
            Log.i("graphqlgetall", response.data().listTasks().items().toString()); // <-- shows what was put into db

            Handler handlerForMainThread = new Handler(Looper.getMainLooper()){

                @Override
                public void handleMessage(Message inputMessage) {
                    Log.i("qraphqlgetall", "made it to the callback");

                    List<ListTasksQuery.Item> items = response.data().listTasks().items();
                    tasks.clear();

                    for(ListTasksQuery.Item item : items) {
                        tasks.add(new Task(item)); // <-- new constructor written for this to make items into Tasks
                    }
                    // tell recyclerView that stuff has changed
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            };
            // last step for updating the recyclerView given presence of handler to deal with main thread.
            handlerForMainThread.obtainMessage().sendToTarget();
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("graphqlgetall", e.getMessage());
        }
    };
}

