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
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;
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
    private List<Task> taskTeamNames;
    private String teamname;
    private String teamId;
    RecyclerView recyclerView; // Instance variable for awsAppSyncClient

    AWSAppSyncClient awsAppSyncClient;

    private static final String TAG = "MainActivity"; // this helps filter logs


    @Override
    protected void onResume() {
        super.onResume();

        // get Username from AWS cognito login
        String cognitoUsername = AWSMobileClient.getInstance().getUsername();
        TextView helloTextView = findViewById(R.id.helloTextView);
        helloTextView.setText("Hello, " + cognitoUsername + "!");

        //===== Shared Preferences ========
        // grab username and teamname from sharedprefs
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("username", "user");
        teamId = prefs.getString("teamId", "2b6533c1-931f-4aef-91a1-ccf65635b4ed");
        this.teamname = prefs.getString("teamname", "team");

        Log.w(TAG, username);

        // Add info from shared prefs to textViews on mainactivity page:
        TextView nameTextView = findViewById(R.id.helloTextView);
        TextView teamTextView = findViewById(R.id.team);
        nameTextView.setText("Hello " + username + "!");
        teamTextView.setText("Team Name: " + teamname);

        // ==== Call Method ==============
        // run graphql queries
        queryAllTasks();
        queryTeamTasks();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // for starting of app, to show a team's tasks
        teamId = "2b6533c1-931f-4aef-91a1-ccf65635b4ed";

        tasks = new LinkedList<>();
        taskTeamNames = new LinkedList<>();

        // Connect to AWS
        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        // ====== Initialization for AWS cognito =======
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                Log.i("sharina.login", result.getUserState().toString());
                if (result.getUserState().toString().equals("SIGNED_OUT")) {
                    AWSMobileClient.getInstance().showSignIn(MainActivity.this,

                            // ToDo: SignIn options - can change background image.

                            new com.amazonaws.mobile.client.Callback<UserStateDetails>() {
                                @Override
                                public void onResult(UserStateDetails result) {
                                    Log.i("sharina.signin", result.getUserState().toString());
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });



        // =============== Recycler and Adapter Code =========
        // starter code at: // https://developer.android.com/guide/topics/ui/layout/recyclerview
        recyclerView = findViewById(R.id.tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new TaskAdapter(tasks, this));


        // =============== Buttons =========
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

        // Button that lets a user log out
        Button goLogoutButton = findViewById(R.id.logout);
        goLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cognitoUsername = AWSMobileClient.getInstance().getUsername();
                AWSMobileClient.getInstance().signOut();

                // change greeting on main activity to a goodbye
                TextView helloTextView = findViewById(R.id.helloTextView);
                helloTextView.setText("Bye Bye, " + cognitoUsername + "!");
            }
        });


    }

    // === Starts activity over in Settings ===
    public void goToSettingsActivity(View v) {
        Intent i = new Intent(this, Settings.class);
        this.startActivity(i);
    }

     //======== Query the AWS DynamoDB for All Tasks to Get Team Names ==============

    public void queryAllTasks() {

        awsAppSyncClient.query(ListTasksQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
                .enqueue(getAllTasksCallback);
    }

    // **** This method should produce an id that can be used in queryTeamTasks... but it's not yet working *******
    public GraphQLCall.Callback<ListTasksQuery.Data> getAllTasksCallback = new GraphQLCall.Callback<ListTasksQuery.Data>() {
        @Override
        public void onResponse(@Nonnull final com.apollographql.apollo.api.Response<ListTasksQuery.Data> response) {
            Handler handlerForMainThread = new Handler(Looper.getMainLooper()){
                @Override
                public void handleMessage(Message inputMessage) {
                    List<ListTasksQuery.Item> items = response.data().listTasks().items();

                    Log.i("idInfo", teamname);

                    teamId = "";

                    for(ListTasksQuery.Item item : items) {
                        Log.i("items", item.toString());

                        //Log.i("idInfo", item.team().id()); // gets team Id
                        //Log.i("nameInfo", item.team().name());

                        if(teamname.equals("team")) {
                            teamId = item.team().id(); // now, put that teamId into queryTeamTasks id
                            Log.i("idInfo", teamId);
                        } else if (teamname.equals(item.team().id())){
                            Log.i("idInfo", "teamname doesn't match");
                        }
                    }
                    Log.i("itemAdded", taskTeamNames.toString());

                }
            };

            handlerForMainThread.obtainMessage().sendToTarget();
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("graphqlgetall", e.getMessage());
        }
    };



    // ======== Query the AWS DynamoDB for Team Task Info ==============

    public void queryTeamTasks() {

        // hardcoded id from teamOne in database
        String id = "2b6533c1-931f-4aef-91a1-ccf65635b4ed";

        // *************** There is a Null Pointer Exception! Something about id == null when teamId
        // is used in place of id. ************************

        // create query
        GetTeamQuery query = GetTeamQuery.builder().id(teamId).build();
        awsAppSyncClient.query(query)
                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
                .enqueue(new GraphQLCall.Callback<GetTeamQuery.Data>() {

            @Override
            public void onResponse(@Nonnull Response<GetTeamQuery.Data> response) {

                List<GetTeamQuery.Item> tasks = response.data().getTeam().listOfTasks().items();
                final LinkedList<Task> appTasks = new LinkedList<>();

                for(GetTeamQuery.Item task : tasks) {
                    Log.i("taskTitle", task.title());
                    appTasks.add(new Task(task.title(), task.body()));
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

