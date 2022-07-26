package com.example.ar_airbrush_second.bluetooth;
/**/

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ar_airbrush_second.ARviewActivity;
import com.example.ar_airbrush_second.MainActivity;
import com.example.ar_airbrush_second.R;

import java.nio.charset.StandardCharsets;

public class CommsFragment extends Fragment implements ServiceConnection, SerialListener {

    public enum Connected {False, Pending, True}

    private String deviceAddress;
    private SerialService service;
    public Connected connected = Connected.False;
    public boolean initialStart = true;

    Toast toastMessage;

    // Lifecycle
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        deviceAddress = getArguments().getString("device");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (service != null) {
            service.attach(this);
            Log.d("DEBUG", "Service not null");
        } else {
            getActivity().startService(new Intent(getActivity().getApplicationContext(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
            Log.d("DEBUG", "Service is null, needs to be started");
        }
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        } else Log.d("DEBUG", "initialStart or service are null");
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if (initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    // Serial + UI
    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    private void status(String str) {
        toastMessage = Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT);
        toastMessage.show();
    }

    public String btMessage;

    public void receive(byte[] data) {
        btMessage = new String(data, StandardCharsets.UTF_8);
        SharedPreferences preferences = service.getApplicationContext().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("btMessage", btMessage);
        editor.commit();
    }

    // SerialListener
    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
        startActivity(new Intent(getActivity(), MainActivity.class));
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        receive(data);
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }
}
