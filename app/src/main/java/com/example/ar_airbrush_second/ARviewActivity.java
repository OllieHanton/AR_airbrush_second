package com.example.ar_airbrush_second;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
//import android.view.MotionEvent;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ar_airbrush_second.bluetooth.SerialService;
import com.example.ar_airbrush_second.bluetooth.TerminalFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.ar.core.Anchor;
//import com.google.ar.core.HitResult;
//import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.rendering.ModelRenderable;
//import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
//import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

//import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ARviewActivity extends AppCompatActivity implements ServiceConnection, PopupMenu.OnMenuItemClickListener {

    // object of ArFragment Class
    private ArFragment arCam;

    //helps to render the 3d model only once when we tap the screen
    private int clickNo = 0;

    //Design chosen from design library flag
    private int designChosen = 1;

    //Design mode (0) or create mode (1) toggled flag
    private int toggleMode = 0;

    //Novice level (with text) or expert level (no text) - scale chosen for create mode... toggled flag.
    //Novice =0, intermediate =1, expert =2
    private int novicelevel = 0;

    //layout token for AR layout for dynamic layout implementation - taken from AR fragment
    private ArSceneView arSceneView;

    //slider value - equivalent to spraying phase between 0 and 5
    private int sliderChangedValue;

    //number to count through script messages from file... between 51
    private int scriptCounter = 0;
    private int scriptCounterLast = 0;

    Toast toastMessage;

    // Settings values
    public int level;
    public boolean uv;
    public boolean laser;

    public static boolean checkSystemSupport(Activity activity) {
        // checking whether the API version of the running Android >= 24
        // that means Android Nougat 7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            String openGlVersion = ((ActivityManager) Objects.requireNonNull(activity.getSystemService(Context.ACTIVITY_SERVICE))).getDeviceConfigurationInfo().getGlEsVersion();
            // checking whether the OpenGL version >= 3.0

            if (Double.parseDouble(openGlVersion) >= 3.0) {
                return true;
            } else {
                Toast.makeText(activity, "App needs OpenGl Version 3.0 or later", Toast.LENGTH_SHORT).show();
                activity.finish();
                return false;
            }
        } else {
            Toast.makeText(activity, "App does not support required Build Version", Toast.LENGTH_SHORT).show();
            activity.finish();
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arview);


        if (checkSystemSupport(this)) {

            //Initiate ARCore functionality:
            setUpAnchorPoints();

            //initiate objects relating to each overlay button
            FloatingActionButton togglebutton = findViewById(R.id.toggle_mode_button);
            ImageButton settingsdropdown = findViewById(R.id.settings_drop_down_menu);
            TextView costevaluator = findViewById(R.id.designmode_text_cost_evaluator);
            TextView substratedistance = findViewById(R.id.createmode_text_distance_to_substrate);
            SeekBar slider = findViewById(R.id.createmode_seekbar);
            FloatingActionButton backbutton = findViewById(R.id.createmode_back);
            FloatingActionButton forwardsbutton = findViewById(R.id.createmode_forward);
            TextView topTextInstructions = findViewById(R.id.createmode_top_text_instructions);
            //handle slider functionality - script here?
            slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int sliderProgress, boolean fromUser) {
                    sliderChangedValue = sliderProgress;
                    //temp toast message for phases:
                    /*if (toastMessage!= null) {
                        toastMessage.cancel();
                    }
                    toastMessage=Toast.makeText(ARviewActivity.this, "Spraying phase " + sliderChangedValue, Toast.LENGTH_SHORT);
                    toastMessage.show();*/

                    updateScriptCounters();
                    implementScript();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            //handle "distance from substrate" text:

            tactileButtons();
            //handle createmode back button
            backbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*int progressIncrementor=slider.getProgress();
                    progressIncrementor--;
                    if(progressIncrementor>=0 && progressIncrementor<=slider.getMax()) {
                        slider.setProgress(progressIncrementor);
                    }*/
                    //remove the progress incrementor above and put the functionality into implementScript...
                    if (scriptCounter > 0) {
                        scriptCounterLast = scriptCounter;
                        scriptCounter--;

                    }
                    implementScript();
                }
            });

            //handle createmode forwards button
            forwardsbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*int progressIncrementor=slider.getProgress();
                    progressIncrementor++;
                    if(progressIncrementor>=0 && progressIncrementor<=slider.getMax()) {
                        slider.setProgress(progressIncrementor);
                    }*/
                    //remove the progress incrementor above and put the functionality into implementScript...
                    if (scriptCounter < 51) {
                        scriptCounterLast = scriptCounter;
                        scriptCounter++;
                    }
                    implementScript();
                }
            });

            //Toggle between design and create modes
            togglebutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (toggleMode == 0) {
                        //set create mode
                        costevaluator.setVisibility(View.INVISIBLE);
                        substratedistance.setVisibility(View.VISIBLE);
                        slider.setVisibility(View.VISIBLE);
                        backbutton.setVisibility(View.VISIBLE);
                        forwardsbutton.setVisibility(View.VISIBLE);
                        toggleMode = 1;
                        togglebutton.setImageResource(R.drawable.airbrush_64);
                        topTextInstructions.setText(getString(R.string.create_mode_welcome));
                    } else {
                        //set design mode
                        costevaluator.setVisibility(View.VISIBLE);
                        substratedistance.setVisibility(View.INVISIBLE);
                        slider.setVisibility(View.INVISIBLE);
                        backbutton.setVisibility(View.INVISIBLE);
                        forwardsbutton.setVisibility(View.INVISIBLE);
                        toggleMode = 0;
                        togglebutton.setImageResource(R.drawable.design_icon);
                        topTextInstructions.setText(getString(R.string.design_mode_welcome));
                    }
                }
            });

            //handle settings button in both design and create modes
            settingsdropdown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSettings(v);
                }
            });

            //if(toggleMode==0) {
            designMode();
            //}
            //else {
            //    designMode();
            //createMode();
            //}
        }
    }

    public void showSettings(View v) {
        level = getIntSetting("level");
        uv = getBoolSetting("uv");
        laser = getBoolSetting("laser");
        Log.d("DEBUG", "Settings Retrieved");

        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.menu_settings);
        Menu menu = popup.getMenu();
        switch (level) {
            case 1:
                menu.findItem(R.id.settings_beginner).setChecked(true);
                break;
            case 2:
                menu.findItem(R.id.settings_intermediate).setChecked(true);
                break;
            case 3:
                menu.findItem(R.id.settings_advanced).setChecked(true);
                break;
        }
        menu.findItem(R.id.settings_uv).setChecked(uv);
        menu.findItem(R.id.settings_laser).setChecked(laser);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_beginner: {
                level = 1;
                saveIntSetting("level", level);
                Log.d("SETTING LEVEL", String.valueOf(level));
                break;
            }
            case R.id.settings_intermediate: {
                level = 2;
                saveIntSetting("level", level);
                Log.d("SETTING LEVEL", String.valueOf(level));
                break;
            }
            case R.id.settings_advanced: {
                level = 3;
                saveIntSetting("level", level);
                Log.d("SETTING LEVEL", String.valueOf(level));
                break;
            }
            case R.id.settings_uv: {
                // Select to enable automatic UV light during spraying
                uv = !item.isChecked();
                item.setChecked(uv);
                saveBoolSetting("uv", uv);
                if (toastMessage != null) {
                    toastMessage.cancel();
                }
                if (uv) {
                    toastMessage = Toast.makeText(this, "UV Light Enabled", Toast.LENGTH_SHORT);
                } else {
                    toastMessage = Toast.makeText(this, "UV Light Disabled", Toast.LENGTH_SHORT);
                }
                toastMessage.show();
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                item.setActionView(new View(getApplicationContext()));
                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return false;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        return false;
                    }
                });
                break;
            }
            case R.id.settings_laser: {
                // Select to enable a red laser indicating spray region
                laser = !item.isChecked();
                item.setChecked(laser);
                saveBoolSetting("laser", laser);
                if (toastMessage != null) {
                    toastMessage.cancel();
                }
                if (laser) {
                    send("<LASER ON>");
                    toastMessage = Toast.makeText(this, "Laser Enabled", Toast.LENGTH_SHORT);
                } else {
                    send("<LASER OFF>");
                    toastMessage = Toast.makeText(this, "Laser Disabled", Toast.LENGTH_SHORT);
                }
                toastMessage.show();
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                item.setActionView(new View(getApplicationContext()));
                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return false;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        return false;
                    }
                });
                break;
            }
        }
        return false;
    }

    //method to handle the updating of the top text instructions by using the independently updated scriptCounter value and the strings from values/script.xml. Also updates progress through script
    private void implementScript() {
        TextView topTextInstructions = findViewById(R.id.createmode_top_text_instructions);
        SeekBar slider = findViewById(R.id.createmode_seekbar);
        int progressIncrementor = slider.getProgress();
        if (novicelevel == 0) {
            if (scriptCounter == 0) {
                topTextInstructions.setText(getString(R.string.create_mode_welcome));
            } else {
                int resourceId = this.getResources().getIdentifier("script_" + scriptCounter, "string", this.getPackageName());
                topTextInstructions.setText(getString(resourceId));

                //if tip_script_x != null then implement as a toast
            }
        }

        if (toastMessage != null) {
            toastMessage.cancel();
        }

        //set progressIncrementor based on where the script is
        if (scriptCounter == 0) {
            progressIncrementor = 0;// and update...
            toastMessage = Toast.makeText(ARviewActivity.this, "Phase " + sliderChangedValue + ": intro", Toast.LENGTH_SHORT);
            toastMessage.show();
        }
        if (scriptCounter == 3) {
            progressIncrementor = 1;// and update...
            toastMessage = Toast.makeText(ARviewActivity.this, "Phase " + sliderChangedValue + ": Spraying Base electrode", Toast.LENGTH_SHORT);
            toastMessage.show();
        }
        if (scriptCounter == 14) {
            progressIncrementor = 2;// and update...
            toastMessage = Toast.makeText(ARviewActivity.this, "Phase " + sliderChangedValue + ": Spraying dielectric", Toast.LENGTH_SHORT);
            toastMessage.show();
        }
        if (scriptCounter == 23) {
            progressIncrementor = 3;// and update...
            toastMessage = Toast.makeText(ARviewActivity.this, "Phase " + sliderChangedValue + ": Spraying electroluminescent coat", Toast.LENGTH_SHORT);
            toastMessage.show();
        }
        if (scriptCounter == 33) {
            progressIncrementor = 4;// and update...
            toastMessage = Toast.makeText(ARviewActivity.this, "Phase " + sliderChangedValue + ": Spraying transparent conductive electrode", Toast.LENGTH_SHORT);
            toastMessage.show();
        }
        if (scriptCounter == 43) {
            progressIncrementor = 5;// and update...
            toastMessage = Toast.makeText(ARviewActivity.this, "Phase " + sliderChangedValue + ": Electrode attachment", Toast.LENGTH_SHORT);
            toastMessage.show();
        }
        if (progressIncrementor <= slider.getMax()) {
            slider.setProgress(progressIncrementor);
        }
        uvControl();
    }

    //method to set script number when progress bar is changed (N.B. the other way around is handled within "implementScript()"), use should always be followed by implementscript()
    private void updateScriptCounters() {
        SeekBar slider = findViewById(R.id.createmode_seekbar);
        int progressIncrementor = slider.getProgress();
        if (progressIncrementor == 0) {
            scriptCounter = 1;
        }
        if (progressIncrementor == 1) {
            scriptCounter = 3;
        }
        if (progressIncrementor == 2) {
            scriptCounter = 14;
        }
        if (progressIncrementor == 3) {
            scriptCounter = 23;
        }
        if (progressIncrementor == 4) {
            scriptCounter = 33;
        }
        if (progressIncrementor == 5) {
            scriptCounter = 43;
        }
        scriptCounterLast = scriptCounter - 1;
    }

    //initialise AR environment before entering design mode
    private void setUpAnchorPoints() {
        //ArFragment is linked up with its respective id used in the activity_main.xml
        arCam = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_camera_area);

        //To remove white "discover controller" icon while Ar is calculating anchors
        arCam.getPlaneDiscoveryController().hide();
        arCam.getPlaneDiscoveryController().setInstructionView(null);
        //return();
    }

    //default setup - mode where object can be editted
    private void designMode() {
        designChosen = 0;
        arCam.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            clickNo++;
            //Add 3d model: the 3d model comes to the scene only when clickNo is one that means once
            if (clickNo == 1) {

                Anchor anchor = hitResult.createAnchor();
                ModelRenderable.builder()
                        //Currently hardcoded text object:
                        .setSource(this, R.raw.dispray_texttiny)
                        .setIsFilamentGltf(true)
                        .build()
                        .thenAccept(modelRenderable -> addModel(anchor, modelRenderable))
                        .exceptionally(throwable -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage("Something is not right" + throwable.getMessage()).show();
                            return null;
                        });
                //make costevaluator visible.. - this is currently buggy where it doesn't appear straight away.
                TextView costevaluator = findViewById(R.id.designmode_text_cost_evaluator);
                costevaluator.setVisibility(View.VISIBLE);
            }
        });
    }

    //alternative mode where object is fixed/editting no longer enabled and script/progress bar/forwardsback buttons are implemented
    private void createMode() {
        novicelevel = 1;
    }

    private void toggleExperience() {
        /*if(novicelevel==0){
            novicelevel=1;
        }
        else {
            novicelevel=0;
        }*/
    }

    private void addModel(Anchor anchor, ModelRenderable modelRenderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        // Creating a AnchorNode with a specific anchor
        anchorNode.setParent(arCam.getArSceneView().getScene());
        //attaching the anchorNode with the ArFragment
        TransformableNode model = new TransformableNode(arCam.getTransformationSystem());
        model.setParent(anchorNode);
        //attaching the anchorNode with the TransformableNode
        model.setRenderable(modelRenderable);
        //attaching the 3d model with the TransformableNode that is already attached with the node
        model.select();

    }

    //region Settings methods
    private boolean getBoolSetting(String key) {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE);
        return preferences.getBoolean(key, false);
    }

    private void saveBoolSetting(String key, boolean value) {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private int getIntSetting(String key) {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE);
        return preferences.getInt(key, 1);
    }

    private void saveIntSetting(String key, int value) {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    //endregion

    //region BT Commands
    private int forceState;
    public String btMessage;

    private void uvControl() {
        // UV On/Off
        if (uv) {
            if (toastMessage != null) {
                toastMessage.cancel();
            }
            if ((scriptCounter == 27 && scriptCounterLast == 26) || (scriptCounter == 29 && scriptCounterLast == 30)) {
                send("<UV ON>");
                toastMessage = Toast.makeText(this, "UV Light On!", Toast.LENGTH_SHORT);
                toastMessage.show();
            }
            if ((scriptCounter == 30 && scriptCounterLast == 29) || (scriptCounter == 26 && scriptCounterLast == 27)) {
                send("<UV OFF>");
                toastMessage = Toast.makeText(this, "UV Light Off!", Toast.LENGTH_SHORT);
                toastMessage.show();
            }
            if (scriptCounter == 0 || scriptCounter == 51) {
                send("<UV OFF>");
            }
        }
    }

    private void laserControl() {
        // TODO send laser on msg
    }

    public void tactileButtons() {
//        Log.d("BUTTON", btMessage);
        if (Objects.equals(btMessage, "fwd")) {
            Log.d("BUTTON", btMessage);
            if (scriptCounter < 51) {
                scriptCounterLast = scriptCounter;
                scriptCounter++;
            }
            implementScript();
        }
        if (Objects.equals(btMessage, "bck")) {
            Log.d("BUTTON", btMessage);
            if (scriptCounter > 0) {
                scriptCounterLast = scriptCounter;
                scriptCounter--;
            }
            implementScript();
        }
    }
    //endregion

    //region BT Service
    private SerialService service;
    public TerminalFragment.Connected connected;

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(this, SerialService.class);
        this.bindService(intent, this, Context.BIND_AUTO_CREATE);
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
            Toast.makeText(this, "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            byte[] data;
            data = (str).getBytes();
            service.write(data);
        } catch (Exception e) {
            Log.d("ERROR", "Connection lost?");
        }
    }

    public void receive(byte[] data) {
        btMessage = new String(data, StandardCharsets.UTF_8);
        Log.d("MESSAGE RECEIVED", btMessage);
    }
    //endregion
}
