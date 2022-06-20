package com.example.ar_airbrush_second;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.example.ar_airbrush_second.bluetooth.SelectDeviceFragment;


public class PrefFragment extends PreferenceFragmentCompat {

    private static SharedPreferences sharedPref;
    public static SharedPreferences.Editor mEditor;
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
                getFragmentManager().beginTransaction().replace(R.id.prefFrame, new SelectDeviceFragment()).commit();
                return true;
            }
        });

        // Features (UV and laser)
        SwitchPreference uvSwitch = findPreference("uv");
        assert uvSwitch != null;
        uvSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean switched = ((SwitchPreference) preference)
                        .isChecked();
                uv = !switched;
                mEditor = sharedPref.edit();
                mEditor.putBoolean("uv", uv).commit();
//                uvSwitch.setSummary(!uv ? "Disabled" : "Enabled");
                // Toast
                if (uv) {
                    // TODO Add UV on arduino command
                    // Toast
                    Toast.makeText(getActivity(), "UV Light Enabled", Toast.LENGTH_SHORT).show();
                }
                else {
                    // TODO Add UV off arduino command
                    // Toast
                    Toast.makeText(getActivity(), "UV Light Disabled", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
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
                    // TODO Add laser on arduino command
                    // Toast
                    Toast.makeText(getActivity(), "Laser Enabled", Toast.LENGTH_SHORT).show();
                }
                else {
                    // TODO Add laser off arduino command
                    // Toast
                    Toast.makeText(getActivity(), "Laser Disabled", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }
}
