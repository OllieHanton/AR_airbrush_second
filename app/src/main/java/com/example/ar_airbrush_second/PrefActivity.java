package com.example.ar_airbrush_second;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class PrefActivity extends AppCompatActivity {

    // Bring up pref screen
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // change title bar text
        getSupportActionBar().setTitle("Preferences");
        setContentView(R.layout.activity_preferences);
        // inflate fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.prefFrame, new PrefFragment()).commit();
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    /*private SharedPreferences.OnSharedPreferenceChangeListener listener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    if (key.equals("setSmsSend") {
                        smsSendPrefChanged(); // the function you want called
                    }
                }
            };

    @Override
    protected void onCreate() {
        super.onCreate();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(listener);
        ...
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
        ...
    }*/
}
