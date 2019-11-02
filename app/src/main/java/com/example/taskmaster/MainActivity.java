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

import com.amazonaws.amplify.generated.graphql.GetTeamQuery;
import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
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

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskItemInteractionListener {


    private List<Task> tasks; // Instance variable for recycler view
    RecyclerView recyclerView; // Instance variable for awsAppSyncClient
    private String teamname;
    AWSAppSyncClient awsAppSyncClient;

    private static final String TAG = "MainActivity"; // this helps filter logs


    @Override
    protected void onResume() {
        super.onResume();

        //===== Shared Preferences ========
        // grab username and teamname from sharedprefs
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("username", "user");
        this.teamname = prefs.getString("teamname", "team");

        Log.w(TAG, username);

        // Add info from shared prefs to textViews on mainactivity page:
        TextView nameTextView = findViewById(R.id.helloTextView);
        TextView teamTextView = findViewById(R.id.team);
        nameTextView.setText("Hello " + username + "!");
        teamTextView.setText("Team Name: " + teamname);

        // ==== Call Method ==============
        // run graphql queries
        queryTeamTasks();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tasks = new LinkedList<>();

        // Connect to AWS
        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

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
    public void goToSettingsActivity(View v) {  // <------------------------ When does this run?
        Intent i = new Intent(this, Settings.class);
        this.startActivity(i);
    }



    // ======== Query the AWS DynamoDB ==============

    public void queryTeamTasks() {

        // hardcoded id from teamOne in database
        String id = "2b6533c1-931f-4aef-91a1-ccf65635b4ed";

        // create query
        GetTeamQuery query = GetTeamQuery.builder().id(id).build();
        awsAppSyncClient.query(query)
                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
                .enqueue(new GraphQLCall.Callback<GetTeamQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<GetTeamQuery.Data> response) {

                List<GetTeamQuery.Item> tasks = response.data().getTeam().listOfTasks().items();
                final LinkedList<Task> appTasks = new LinkedList<>();

                for(GetTeamQuery.Item task : tasks) {
                    appTasks.add(new Task(task.title(), task.body()));  // <------ get taskstate?
                }
                Handler handler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage (Message message) {
                        recyclerView.setAdapter(new TaskAdapter(appTasks, MainActivity.this));
                    }

                };
                handler.obtainMessage().sendToTarget();
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {

            }
        });


    }
//    public void queryTeamTasks() {
//        Log.i("graphqlgetall", "made it into queryTeamTasks method");
//
////        awsAppSyncClient.query(ListTasksQuery.builder().build())
//        awsAppSyncClient.query(ListTeamsQuery.builder().build())
//                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
//                .enqueue(getTeamTasksCallback);
//    }
//
//    public GraphQLCall.Callback<GetTeamQuery.Data> getTeamTasksCallback = new GraphQLCall.Callback<GetTeamsQuery.Data>() {
//
//        @Override
//        public void onResponse(@Nonnull final com.apollographql.apollo.api.Response<ListTeamsQuery.Data> response) {
//
//            List<GetTeamQuery.Item> tasks = response.data().listTeams().items();  // <------------------ RETURN HERE!!!!!!!!
//
////            Handler handlerForMainThread = new Handler(Looper.getMainLooper()){
////
////                @Override
////                public void handleMessage(Message inputMessage) {
////                    Log.i("qraphqlgetall", "made it to the callback");
////
////                    List<ListTasksQuery.Item> items = response.data().listTasks().items();
////                    tasks.clear();
////
////                    for(ListTasksQuery.Item item : items) {
////                        tasks.add(new Task(item)); // <-- new constructor written for this to make items into Tasks
////                    }
////                    // tell recyclerView that stuff has changed
////                    recyclerView.getAdapter().notifyDataSetChanged();
////                }
////            };
////            // last step for updating the recyclerView given presence of handler to deal with main thread.
////            handlerForMainThread.obtainMessage().sendToTarget();
//        }
//
//        @Override
//        public void onFailure(@Nonnull ApolloException e) {
//            Log.e("graphqlgetall", e.getMessage());
//        }
//    };

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

