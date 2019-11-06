package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import type.CreateTaskInput;
import type.CreateTeamInput;

public class AddTask extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "sharina.AddTask";

    public AppDatabase db;

    // Attaching a file
    private static final int READ_REQUEST_CODE = 42;
    private static volatile S3ObjectManagerImplementation s3ObjectManager;
    private static int RESULT_LOAD_IMAGE = 1;
    private String photoPath;

    // Instance variable for awsAppSyncClient
    AWSAppSyncClient awsAppSyncClient;

    final List<ListTeamsQuery.Item> teams = new LinkedList<>();
    public String teamName = null;
    public String teamIdFromDB = null;
    public String teamNameFromDB = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);


        // ======= Upload File to AWS with TransferUtility==============
        // https://aws-amplify.github.io/docs/android/storage

        // Upload and download objects from AWS
//        getApplicationContext().startService(new Intent(getApplicationContext(), TransferService.class));
//
//        // Initialize the AWSMobileClient if not initialized
//        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
//            @Override
//            public void onResult(UserStateDetails result) {
//                Log.i(TAG, "AWSMobileClient initialized. User State is " + result.getUserState().toString());
//                uploadWithTransferUtility();
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Log.e(TAG, "Initialization error.", e);
//
//            }
//        });


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

    // ====== File Upload with GraphQL APIs ================
    // https://aws-amplify.github.io/docs/android/storage

//    public void choosePhoto() {
//        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(i, RESULT_LOAD_IMAGE);
//    }


    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                //showImage(uri);
            }
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uri,
                    filePathColumn, null, null, null);
            Log.i(TAG, "cursor: " + cursor);

//            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != resultData) {
//                cursor.moveToFirst();
//                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//
//                String picturePath = cursor.getString(columnIndex);
//                Log.i(TAG, "picturePath " + picturePath);
//                cursor.close();
//                // String picturePath contains the path of selected Image
//                photoPath = picturePath;
//                Log.i(TAG, "photoPath: " + photoPath);
//            }

        }
    }
//    public static final S3ObjectManagerImplementation getS3ObjectManager(final Context context) {
//        if (s3ObjectManager == null) {
//            AmazonS3Client s3Client = new AmazonS3Client(getCredentialsProvider(context));
//            s3Client.setRegion(Region.getRegion("us-east-1")); // you can set the region of bucket here
//            s3ObjectManager = new S3ObjectManagerImplementation(s3Client);
//        }
//        return s3ObjectManager;
//    }
//
//    // initialize and fetch cognito credentials provider for S3 Object Manager
//    public static final AWSCredentialsProvider getCredentialsProvider(final Context context){
//        final CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
//                context,
//                Constants.COGNITO_IDENTITY, // Identity pool ID
//                Regions.fromName(Constants.COGNITO_REGION) // Region
//        );
//        return credentialsProvider;
//    }
//
//    private void save() {
//
//    }

    // ======== File Upload from AWS  with TransferUtility continued ========
//    public void uploadWithTransferUtility() {
//        TransferUtility transferUtility =
//                TransferUtility.builder()
//                        .context(getApplicationContext())
//                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
//                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
//                        .build();
//    }


}


