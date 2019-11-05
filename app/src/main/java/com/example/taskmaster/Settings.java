package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.amazonaws.amplify.generated.graphql.ListTeamsQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;



public class Settings extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // Instance variable for awsAppSyncClient
    AWSAppSyncClient awsAppSyncClient;

    final List<ListTeamsQuery.Item> teams = new LinkedList<>();

    public String teamIdFromDB = null;
    public String teamNameFromDB = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Connect to AWS
        awsAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

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
                                Spinner spinner =  findViewById(R.id.spinner2);
                                ArrayAdapter<String> adapter = new ArrayAdapter(Settings.this,
                                        android.R.layout.simple_spinner_item, teamNames);

                                // Specify the layout to use when the list of choices appears
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(adapter);

                                // When something gets picked, tell Settings
                                spinner.setOnItemSelectedListener(Settings.this);

                            }
                        };
                        h.obtainMessage().sendToTarget();
                    }
                    @Override
                    public void onFailure(@Nonnull ApolloException e) {

                    }
                });
    }

    // spinner needs these two methods
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        teamIdFromDB = teams.get(position).id();
        teamNameFromDB = teams.get(position).name();
        Log.i("teamInfo", teamNameFromDB);
        Log.i("teamInfo", teamIdFromDB); // shows the id that is in the database of the team chosen in dropdown
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void onSaveButtonPressed(View view) {
        System.out.println("button pressed");

        // === save what the user types into Shared Preferences
//        EditText nameEditText = findViewById(R.id.enterUsername);
//        EditText teamNameEditText = findViewById(R.id.enterTeamName);
//        String name = nameEditText.getText().toString();
//        String team = teamNameEditText.getText().toString();

        // grab the SharedPreference in which to save the data
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // save the data
        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString("username", name);
//        editor.putString("teamname", team);

        // data pulled from Spinner, which pulls from AWS DB
        editor.putString("teamId", teamIdFromDB);
        editor.apply();

        editor.putString("nameOfTeam", teamNameFromDB);
        editor.apply();

        // Send user back to the Main Page
        Intent goToMainActivity = new Intent (this, MainActivity.class);
        this.startActivity(goToMainActivity);
    }
}

