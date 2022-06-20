package com.example.ar_airbrush_second.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ar_airbrush_second.PrefActivity;
import com.example.ar_airbrush_second.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SelectDeviceFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.select_device,
                container, false);
        requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
        requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN);
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        return view;
    }

    public void onResume(){
        super.onResume();

        // Set title bar
        ((PrefActivity) getActivity())
                .setActionBarTitle("Bluetooth Connection");
    }

    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if(isGranted) {
                    Toast.makeText(getActivity(), "Permissions granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Permissions not granted", Toast.LENGTH_SHORT).show();
                }

                if (ContextCompat.checkSelfPermission(
                        getActivity(), Manifest.permission.BLUETOOTH_CONNECT) ==
                        PackageManager.PERMISSION_GRANTED) {

                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                    // Get List of Paired Bluetooth Device
                    @SuppressLint("MissingPermission") Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                    List<Object> deviceList = new ArrayList<>();
                    if (pairedDevices.size() > 0) {
                        // There are paired devices. Get the name and address of each paired device.
                        for (BluetoothDevice device : pairedDevices) {
                            @SuppressLint("MissingPermission") String deviceName = device.getName();
                            String deviceHardwareAddress = device.getAddress(); // MAC address
                            DeviceInfoModel deviceInfoModel = new DeviceInfoModel(deviceName, deviceHardwareAddress);
                            deviceList.add(deviceInfoModel);
                        }
                        // Display paired device using recyclerView
                        RecyclerView recyclerView = getActivity().findViewById(R.id.selectDevice);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        DeviceListAdapter deviceListAdapter = new DeviceListAdapter(getActivity(), deviceList);
                        recyclerView.setAdapter(deviceListAdapter);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                    }
                    else {
                        View view = getActivity().findViewById(R.id.selectDevice);
                        Snackbar snackbar = Snackbar.make(view, "Activate Bluetooth or pair a Bluetooth device", Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            }
                        });
                        snackbar.show();
                    }
                } /*else {
                    // Directly ask for permission
                    // The registered ActivityResultCallback gets the result of this request
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                }*/
            });

}