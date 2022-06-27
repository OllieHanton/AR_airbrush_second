package com.example.ar_airbrush_second;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.example.ar_airbrush_second.bluetooth.BluetoothActivity;
import com.example.ar_airbrush_second.bluetooth.SerialService;
import com.example.ar_airbrush_second.bluetooth.TerminalFragment;
import com.example.ar_airbrush_second.bluetooth.TextUtil;

public class PrefFragment extends PreferenceFragmentCompat implements ServiceConnection {

    private SharedPreferences sharedPref;
    public SharedPreferences.Editor mEditor;
    boolean uv;
    boolean laser;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        sharedPref = this.getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        uv = sharedPref.getBoolean("uv", false);
        laser = sharedPref.getBoolean("uv", false);

        // add preference fragment from xml folder
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference selectDevice = findPreference("bt");
        selectDevice.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference selectDevice) {
                startActivity(new Intent(getActivity(), BluetoothActivity.class));
                return true;
            }
        });

        //region Features (UV and laser)
        SwitchPreference uvSwitch = findPreference("uv");
        assert uvSwitch != null;
        uvSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean switched = ((SwitchPreference) preference)
                    .isChecked();
            uv = !switched;
            mEditor = sharedPref.edit();
            mEditor.putBoolean("uv", uv).commit();
//                uvSwitch.setSummary(!uv ? "Disabled" : "Enabled");
            // Toast
            if (uv) {
                 send("<UV ON>");
                // Toast
                Toast.makeText(getActivity(), "UV Light Enabled", Toast.LENGTH_SHORT).show();
            } else {
                send("<UV OFF>");
                // Toast
                Toast.makeText(getActivity(), "UV Light Disabled", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        SwitchPreference laserSwitch = findPreference("laser");
        assert laserSwitch != null;
        laserSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean switched = ((SwitchPreference) preference)
                        .isChecked();
                laser = !switched;
                mEditor = sharedPref.edit();
                mEditor.putBoolean("laser", laser).commit();

                if (laser) {
                    send("<LASER ON>");
                    // Toast
                    Toast.makeText(getActivity(), "Laser Enabled", Toast.LENGTH_SHORT).show();
                } else {
                    send("<LASER OFF>");
                    // Toast
                    Toast.makeText(getActivity(), "Laser Disabled", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        }); //endregion
    }

    private SerialService service;
    public TerminalFragment.Connected connected;

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(getActivity(), SerialService.class);
        getActivity().bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        connected = TerminalFragment.Connected.True;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    public void send(String str) {
        if (connected != TerminalFragment.Connected.True) {
            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            byte[] data;
            data = (str).getBytes();
            service.write(data);
        } catch (Exception e) {
            Log.d("ERROR","Connection lost?");
        }
    }
}
