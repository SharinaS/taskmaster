package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Update;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.UpdateTaskMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import java.io.File;

import javax.annotation.Nonnull;


import type.UpdateTaskInput;

public class TaskDetail extends AppCompatActivity {
    AWSAppSyncClient awsAppSyncClient;
    private static final String TAG = "sharina";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // == Accesses Extra Data From MainActivity Buttons ==

        // get the data added to the intent & use it to display this activity
        String task = getIntent().getStringExtra("task");

        // grab the label from the page
        TextView titleView = findViewById(R.id.detailTaskTitle);
        // set its text to be the item name
        titleView.setText(task);

        // Gets the body of the task to render it
        String taskBody = getIntent().getStringExtra("taskBody");
        TextView bodyView = findViewById(R.id.task_detail_desc);
        bodyView.setText(taskBody);

        // == Get the String of the image to ultimately render it

        // get the string from the intent
        String taskImageStr = getIntent().getStringExtra("taskImage");

//        TextView imageView = findViewById(R.id.testImageString);
//        imageView.setText(taskImageStr);

        // Use Picasso to display the image
        Picasso.get().load(
                "https://taskmaster288ee44745fb44eaa8db972bda84b42f-local.s3.us-west-2.amazonaws.com/"
                        + taskImageStr).into((ImageView)findViewById(R.id.taskImageShow));


    }

    public void buttonToChangeDB(View view){

        String idStuff = getIntent().getStringExtra("taskId"); // from allTasks page
        Log.i(TAG, "Button was pushed");

        UpdateTaskInput updateTaskInput = UpdateTaskInput.builder()
                .id(idStuff)
                .title("Picolas Cage is my first love")
                .build();
        awsAppSyncClient.mutate(UpdateTaskMutation.builder().input(updateTaskInput).build())
                .enqueue(new GraphQLCall.Callback<UpdateTaskMutation.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<UpdateTaskMutation.Data> response) {
                        Log.i(TAG, "Task Title has been Changed!");
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.i(TAG, "Well, changing the task title did NOT work");
                    }
                });



    }
}
