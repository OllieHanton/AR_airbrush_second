package com.example.ar_airbrush_second;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
//import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;

import com.example.ar_airbrush_second.bluetooth.BluetoothActivity;
import com.google.ar.core.Anchor;
//import com.google.ar.core.HitResult;
//import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
//import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
//import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

//import java.lang.ref.WeakReference;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);
        getSupportActionBar().hide();
    }
    public void openBT(View v) {
        startActivity(new Intent(MainActivity.this, BluetoothActivity.class));
    }

    public void startARview(View v) {
        startActivity(new Intent(MainActivity.this, ARviewActivity.class));
    }

}