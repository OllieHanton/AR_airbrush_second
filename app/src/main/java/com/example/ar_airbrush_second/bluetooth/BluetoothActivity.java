package com.example.ar_airbrush_second.bluetooth;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.ar_airbrush_second.R;

public class BluetoothActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {
    String[] PERMISSIONS;
    private ActivityResultLauncher<String[]> multiplePermissionLauncher;

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

        // region Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PERMISSIONS = new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            PERMISSIONS = new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
            };
        }
        //endregion

        ActivityResultContracts.RequestMultiplePermissions multiplePermissionsContract = new ActivityResultContracts.RequestMultiplePermissions();
        multiplePermissionLauncher = registerForActivityResult(multiplePermissionsContract, isGranted -> {
            Log.d("PERMISSIONS", "Launcher result: " + isGranted.toString());
            if (isGranted.containsValue(false)) {
                Log.d("PERMISSIONS", "At least one of the permissions was not granted, launching again...");
                multiplePermissionLauncher.launch(PERMISSIONS);
            }
        });
        askPermissions(multiplePermissionLauncher);
    }

    //region Permissions methods
    private void askPermissions(ActivityResultLauncher<String[]> multiplePermissionLauncher) {
        if (!hasPermissions(PERMISSIONS)) {
            Log.d("PERMISSIONS", "Launching multiple contract permission launcher for ALL required permissions");
            multiplePermissionLauncher.launch(PERMISSIONS);
        } else {
            Log.d("PERMISSIONS", "All permissions are already granted");
        }
    }

    private boolean hasPermissions(String[] permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("PERMISSIONS", "Permission is not granted: " + permission);
                    return false;
                }
                Log.d("PERMISSIONS", "Permission already granted: " + permission);
            }
            return true;
        }
        return false;
    }
    //endregion

    @Override
    public void onBackStackChanged() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}