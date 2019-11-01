package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void onSaveButtonPressed(View view) {
        System.out.println("button pressed");

        // === save what the user types into Shared Preferences
        EditText nameEditText = findViewById(R.id.enterUsername);
        String name = nameEditText.getText().toString();

        // grab the SharedPreference in which to save the data
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // save the data
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", name);
        editor.apply();

        // Send user back to the Main Page
        Intent goToMainActivity = new Intent (this, MainActivity.class);
        this.startActivity(goToMainActivity);
    }
}
