package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.exception.ApolloException;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

public class AllTasks extends AppCompatActivity implements TaskAdapter.OnTaskItemInteractionListener {

    private List<Task> tasks;

    // Instance variable for recycler view
    RecyclerView recyclerView;

    // Instance variable for awsAppSyncClient
    AWSAppSyncClient awsAppSyncClient;

    @Override
    protected void onResume() {
        super.onResume();

        // Connect to AWS
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
        setContentView(R.layout.activity_all_tasks);


        tasks = new LinkedList<>();

        // ====== Recycler and Adapter Code =========
        recyclerView = findViewById(R.id.tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new TaskAdapter(tasks, this));
    }

    // ======== Query the AWS DynamoDB ==============
    public void queryAllTasks() {
        Log.i("graphqlgetall", "made it into queryAllTasks method");

        awsAppSyncClient.query(ListTasksQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
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

    // === Takes user to detail page when a task is clicked on ===
    @Override
    public void taskItemClickedOn(Task task) {
        // go to the other activity... create the intent to start that activity
        Intent clickedOnTask = new Intent(AllTasks.this, TaskDetail.class);

        // add extra info about that task
        clickedOnTask.putExtra("task", task.getTitle());
        clickedOnTask.putExtra("taskBody", task.getBody());

        // start the activity
        AllTasks.this.startActivity(clickedOnTask);
    }
}
