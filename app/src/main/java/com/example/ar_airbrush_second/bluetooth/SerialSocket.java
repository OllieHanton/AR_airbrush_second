package com.example.ar_airbrush_second.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Executors;

@SuppressLint("MissingPermission")
class SerialSocket implements Runnable {

    private static final UUID BLUETOOTH_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BroadcastReceiver disconnectBroadcastReceiver;

    private final Context context;
    private SerialListener listener;
    private final BluetoothDevice device;
    private BluetoothSocket socket;
    private boolean connected;

    SerialSocket(Context context, BluetoothDevice device) {
        if (context instanceof Activity)
            throw new InvalidParameterException("expected non UI context");
        this.context = context;
        this.device = device;
        disconnectBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (listener != null)
                    listener.onSerialIoError(new IOException("background disconnect"));
                disconnect(); // disconnect now, else would be queued until UI re-attached
            }
        };
    }

    String getName() {
        return device.getName() != null ? device.getName() : device.getAddress();
    }

    // connect-success and most connect-errors are returned asynchronously to listener
    void connect(SerialListener listener) throws IOException {
        this.listener = listener;
        context.registerReceiver(disconnectBroadcastReceiver, new IntentFilter(Constants.INTENT_ACTION_DISCONNECT));
        Executors.newSingleThreadExecutor().submit(this);
    }

    void disconnect() {
        listener = null; // ignore remaining data and errors
        // connected = false; // run loop will reset connected
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            socket = null;
        }
        try {
            context.unregisterReceiver(disconnectBroadcastReceiver);
        } catch (Exception ignored) {
        }
    }

    void write(byte[] data) throws IOException {
        if (!connected)
            throw new IOException("not connected");
        socket.getOutputStream().write(data);
    }

    // connect & read
    @Override
    public void run() {
        try {
            socket = device.createRfcommSocketToServiceRecord(BLUETOOTH_SPP);
            socket.connect();
            if (listener != null)
                listener.onSerialConnect();
        } catch (Exception e) {
            if (listener != null)
                listener.onSerialConnectError(e);
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            socket = null;
            return;
        }
        connected = true;
        try {
            final byte delimiterLF = 10; // LF in UTF-8
            int bufPosition = 0;
            byte[] buffer = new byte[1024];
            byte[] dataPacket = new byte[1024];
            while (true) {
                int bytesAvailable = socket.getInputStream().read(buffer);
                for (int i = 0; i < bytesAvailable; i++) {
                    byte b = buffer[i];
                    if (b == delimiterLF) {
                        byte[] data = Arrays.copyOf(dataPacket, bufPosition);
//                        Log.d("DEBUG", new String(data, StandardCharsets.UTF_8));
                        if (listener != null) listener.onSerialRead(data);
                        bufPosition = 0;
                    } else dataPacket[bufPosition++] = b;
                }
            }
        } catch (Exception e) {
            Log.d("RECEIVE", "Cannot receive bytes...");
            connected = false;
            if (listener != null)
                listener.onSerialIoError(e);
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            socket = null;
        }
    }
}
