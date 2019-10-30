package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.exception.ApolloException;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import type.CreateTaskInput;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskItemInteractionListener {


    private List<Task> tasks;
    public AppDatabase db;


    // Class variable for recycler view
    RecyclerView recyclerView;

    // Class variable for recycler view adapter
    private RecyclerView.Adapter taskAdapter;

    // Class variable for awsAppSyncClient
    AWSAppSyncClient awsAppSyncClient;

    // main activity tag for logging
    private static final String TAG = "MainActivity"; // this helps filter logs

    //=============== for helping with accessing the great internet: ============
    public void putDataOnPage(String data) {
        TextView internetTextView = findViewById(R.id.internetStuff);
        internetTextView.setText(data); // sets the data into the textview

        // Use gson to parse json
        Gson gson = new Gson();

        Task[] taskFromInternetArr = gson.fromJson(data, Task[].class);
        Log.i(TAG, Arrays.toString(taskFromInternetArr));

        // update list with new data
//        this.tasks.addAll(Arrays.asList(taskFromInternetArr));
        this.tasks = new LinkedList<>();
        this.tasks.addAll(Arrays.asList(taskFromInternetArr));
        Log.i(TAG, tasks.toString());

        // notify adapter that data set change
//        this.taskAdapter.notifyDataSetChanged();

    }
    // ====================================================

    @Override
    protected void onResume() {
        super.onResume();

        // grab username from sharedprefs and use it to update the label that displays username
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("username", "user");

        Log.w(TAG, username); // logging of username string

        TextView nameTextView = findViewById(R.id.helloTextView);
        nameTextView.setText("Hello " + username + "!");

        // ====== Database Code ===========
        // Database Info with database name of taskmaster from strings.xml
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, getString(R.string.database_name))
                .allowMainThreadQueries().build();

        // List of Tasks for Recycler View
        this.tasks = new LinkedList<>();

        // get everything from the database (addAll is a built in method) then add all that stuff to the list.
        // We're returning the list from getall, which is being added to tasks.
        this.tasks.addAll(db.taskDao().getall());

        // ====== Recycler and Adapter Code =========
        // Render Task Items to the screen, in RecyclerView
        // starter code at: // https://developer.android.com/guide/topics/ui/layout/recyclerview
        recyclerView = findViewById(R.id.tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Specify an Adapter
        // for use with database only:
        recyclerView.setAdapter(new TaskAdapter(tasks, this));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ============ Connect to AWS =====================
        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        // run graphql queries
        runAddTaskMutation();
        queryAllTasks();

        // ============ Data from the Internet =============
        OkHttpClient client = new OkHttpClient();

        // == Request Builder
        Request request = new Request.Builder()
                .url("http://taskmaster-api.herokuapp.com/tasks")
                .build();


        // callback function used here
        client.newCall(request).enqueue(new LogDataWhenItComesBackCallback(this));

        // =================================================

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

    // ============= Add Task Stuff to AWS Amplify Database with a Mutation ==============
    public void runAddTaskMutation() {
        CreateTaskInput createTaskInput = CreateTaskInput.builder()
                .title("Shopping")
                .body("Check shopping list for food items")
                .taskState(1)
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

    // ======== Query the AWS DynamoDB ==============
    public void queryAllTasks() {
        awsAppSyncClient.query(ListTasksQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(getAllTasksCallback);
    }

    public GraphQLCall.Callback<ListTasksQuery.Data> getAllTasksCallback = new GraphQLCall.Callback<ListTasksQuery.Data>() {
        @Override
        public void onResponse(@Nonnull final com.apollographql.apollo.api.Response<ListTasksQuery.Data> response) {
            Log.i("graphqlgetall", response.data().listTasks().items().toString()); // <---- gives us a list of task items. Will have to be converted later

            Handler handlerForMainThread = new Handler(Looper.getMainLooper()){ // <------- not being used yet
                @Override
                public void handleMessage(Message inputMessage) {
                    Log.i("qraphqlgetall", "made it to the callback");
                    List<ListTasksQuery.Item> items = response.data().listTasks().items();
                    tasks.clear();
                    for(ListTasksQuery.Item item : items) {
                        tasks.add(new Task(item)); // <-- new constructor written for this
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

// ===== Class to support accessing data from Internet ========
class LogDataWhenItComesBackCallback implements Callback {

    MainActivity actualMainActivityInstance;

    // == constructor for this class
    public LogDataWhenItComesBackCallback(MainActivity actualMainActivityInstance) {
        this.actualMainActivityInstance = actualMainActivityInstance;
    }

    private static final String TAG = "example.Callback";

    // This is called by OKHttp if request fails
    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        Log.e(TAG, "internet error");
        Log.e(TAG, e.getMessage());

    }

    // OKHttp calls this is the request works
    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        String responseBody = response.body().string();       // <--------------------------------- what is response.body?
        Log.i(TAG, responseBody);

        // defines a class that does nice things on the main/UI thread for us
        // this code will run once sendToTarget activates.
        Handler handlerForMainThread = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                // get the data from Message object and pass to actualMainActivityInstance
                // inputMessage.obj is the responseBody defined above
                actualMainActivityInstance.putDataOnPage((String)inputMessage.obj);
            }
        };

        // Get a message from the handler and send it
        // ResponseBody will be the message.obj b/c it's the second argument to obtain message.
        Message completeMessage = handlerForMainThread.obtainMessage(0, responseBody);
        completeMessage.sendToTarget(); // <--- calls .handleMessage on the main thread
    }
}
