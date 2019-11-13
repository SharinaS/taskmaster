package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
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
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.S3ObjectManagerImplementation;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import type.CreateTaskInput;
import type.CreateTeamInput;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class AddTask extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "sharina.AddTask";

    public AppDatabase db;

    // Attaching a file
    private static final int READ_REQUEST_CODE = 42;
    private static volatile S3ObjectManagerImplementation s3ObjectManager;
    private static int RESULT_LOAD_IMAGE = 1;
    private String photoPath;
    private String imageKey;

    // Instance variable for awsAppSyncClient
    AWSAppSyncClient awsAppSyncClient;

    final List<ListTeamsQuery.Item> teams = new LinkedList<>();
    public String teamName = null;
    public String teamIdFromDB = null;
    public String teamNameFromDB = null;

    // accessing a location
    private FusedLocationProviderClient mLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // ======= Location Data Stuff ============
        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, 10);

        mLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationProviderClient.getLastLocation().addOnSuccessListener(AddTask.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        
                    }
                });
            }
        });





         //======= Upload File to AWS with TransferUtility==============
         // https://aws-amplify.github.io/docs/android/storage

         //Upload and download objects from AWS
        getApplicationContext().startService(new Intent(getApplicationContext(), TransferService.class));

        String[] permissions = {READ_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permissions, 1);


        // Connect to AWS
        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        //============= Create a Mutation to Manually Add Teams to DB - Run app and go to AddTask View ======
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

                Task newTask = new Task(stringTitle, stringBody, null);
                // add task to dynamoDB
                runAddTaskMutation(newTask);

                // Automatically let person return to prior page they were on once activity complete.
                AddTask.this.finish();
            }
        });

    }

    // ============== methods from spinner ==========
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        teamName = teams.get(position).id();
        Log.i("sharina", teamName); // shows the id that is in the database of the team chosen in dropdown

        teamIdFromDB = teams.get(position).id();
        teamNameFromDB = teams.get(position).name();

        // grab the SharedPreference in which to save the data
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // save the data
        SharedPreferences.Editor editor = prefs.edit();

        // data pulled from Spinner, which pulls from AWS DB
        editor.putString("teamId", teamIdFromDB);
        editor.apply();

        editor.putString("nameOfTeam", teamNameFromDB);
        editor.apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    // ============= Add Task Stuff to AWS Amplify Database (with a Mutation) ==============
    // Starting code from https://aws-amplify.github.io/docs/android/start
    public void runAddTaskMutation(Task task) {
        CreateTaskInput createTaskInput = CreateTaskInput.builder()
                .taskTeamId(teamName)
                .title(task.getTitle())
                .body(task.getBody())
                .taskState(StatusConverter.toInt(task.getTaskState())) // <-- uses the status converter to return the int of the enum
                .image(imageKey)
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


    // =========== Pick a File and Upload to Cloud Using S3 ================
    // https://developer.android.com/guide/topics/providers/document-provider

    public void pickFile (View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, READ_REQUEST_CODE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());

                // actually get path from URI
                Uri selectedImage = uri;
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                Log.i(TAG, "cursor: " + cursor);

                Log.i(TAG, "uri is: " + uri);

                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                imageKey = "public/"+ UUID.randomUUID().toString();

                TransferUtility transferUtility =
                        TransferUtility.builder()
                                .context(getApplicationContext())
                                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                                .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                                .build();
                TransferObserver uploadObserver =
                        transferUtility.upload(
                                //"public/"+ UUID.randomUUID().toString(),
                                imageKey,
                                new File(picturePath));

                // Attach a listener to the observer to get state update and progress notifications
                uploadObserver.setTransferListener(new TransferListener() {

                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (TransferState.COMPLETED == state) {
                            // Handle a completed upload.
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                        int percentDone = (int)percentDonef;

                        Log.d(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent
                                + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
                    }

                    @Override
                    public void onError(int id, Exception ex) {

                    }
                });
            }
        }
    }
}


