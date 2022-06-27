package com.example.ar_airbrush_second.bluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import com.example.ar_airbrush_second.PrefFragment;
import com.example.ar_airbrush_second.R;

public class BluetoothActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        // change title bar text
        getSupportActionBar().setTitle("Select a paired bluetooth device");
        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().replace(R.id.deviceFrame, new DevicesFragment()).commit();
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