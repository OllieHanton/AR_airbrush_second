package com.example.ar_airbrush_second;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class DesignChoiceActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    Toast toastMessage;
    private SharedPreferences sharedPref;
    public int uv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_designchoice);
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        // change title bar text
        getSupportActionBar().setTitle("Design Library");

        ImageView img1 = (ImageView) findViewById(R.id.imageView1);
        ImageView img2 = (ImageView) findViewById(R.id.imageView2);
        ImageView img3 = (ImageView) findViewById(R.id.imageView3);
        ImageView img4 = (ImageView) findViewById(R.id.imageView4);
        img1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (toastMessage!= null) {
                    toastMessage.cancel();
                }
                toastMessage=Toast.makeText(DesignChoiceActivity.this, "Design 1 chosen", Toast.LENGTH_SHORT);
                toastMessage.show();
                sharedPref = getSharedPreferences("settings", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("designChoice", 1);
                editor.commit();
            }
        });
        img2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (toastMessage!= null) {
                    toastMessage.cancel();
                }
                toastMessage=Toast.makeText(DesignChoiceActivity.this, "Design 2 chosen", Toast.LENGTH_SHORT);
                toastMessage.show();
                sharedPref = getSharedPreferences("settings", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("designChoice", 1);
                editor.commit();
            }
        });
        img3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (toastMessage!= null) {
                    toastMessage.cancel();
                }
                toastMessage=Toast.makeText(DesignChoiceActivity.this, "Design 3 chosen", Toast.LENGTH_SHORT);
                toastMessage.show();
                sharedPref = getSharedPreferences("settings", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("designChoice", 2);
                editor.commit();
            }
        });
        img4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (toastMessage!= null) {
                    toastMessage.cancel();
                }
                toastMessage=Toast.makeText(DesignChoiceActivity.this, "Design 4 chosen", Toast.LENGTH_SHORT);
                toastMessage.show();
                sharedPref = getSharedPreferences("settings", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("designChoice", 3);
                editor.commit();
            }
        });

        Button uploadbutton = (Button) findViewById(R.id.uploadbutton);
        uploadbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (toastMessage!= null) {
                    toastMessage.cancel();
                }
                toastMessage=Toast.makeText(DesignChoiceActivity.this, "Upload custom design", Toast.LENGTH_SHORT);
                toastMessage.show();
                sharedPref = getSharedPreferences("settings", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("designChoice", 4);
                editor.commit();
            }
        });

    }
    @Override
    public void onBackStackChanged() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount()>0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
