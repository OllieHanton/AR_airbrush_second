package com.example.ar_airbrush_second;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
//import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Objects;

public class ARviewActivity extends AppCompatActivity{

    // object of ArFragment Class
    private ArFragment arCam;

    //helps to render the 3d model only once when we tap the screen
    private int clickNo = 0;

    //Design chosen from design library flag
    private int designChosen = 1;

    //Design mode (0) or create mode (1) toggled flag
    private int toggleMode=0;

    //Novice level (with text) or expert level (no text) - scale chosen for makeingmode... toggled flag. Maybe at lowest level have it so that
    private int novicelevel;

    //layout token for AR layout for dynamic layout implementation - taken from AR fragment
    private ArSceneView arSceneView;

    private int sliderChangedValue;

    Toast toastMessage;

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
                public void onProgressChanged(SeekBar seekBar, int scriptProgress, boolean fromUser) {
                    sliderChangedValue = scriptProgress;
                    if (toastMessage!= null) {
                        toastMessage.cancel();
                    }
                    toastMessage=Toast.makeText(ARviewActivity.this, "Spraying phase " + sliderChangedValue, Toast.LENGTH_SHORT);
                    toastMessage.show();
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            //handle distance from substrate text:
            //substratedistance.setTextColor(Color.WHITE);

            //handle createmode back button
            backbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int progressIncrementor=slider.getProgress();
                    progressIncrementor--;
                    if(progressIncrementor>=0 && progressIncrementor<=slider.getMax()) {
                        slider.setProgress(progressIncrementor);
                    }
                }
            });

            //handle createmode forwards button
            forwardsbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int progressIncrementor=slider.getProgress();
                    progressIncrementor++;
                    if(progressIncrementor>=0 && progressIncrementor<=slider.getMax()) {
                        slider.setProgress(progressIncrementor);
                    }
                }
            });

            //Toggle between design and create modes
            togglebutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(toggleMode==0) {
                        //set create mode
                        costevaluator.setVisibility(View.INVISIBLE);
                        substratedistance.setVisibility(View.VISIBLE);
                        slider.setVisibility(View.VISIBLE);
                        backbutton.setVisibility(View.VISIBLE);
                        forwardsbutton.setVisibility(View.VISIBLE);
                        toggleMode=1;
                        togglebutton.setImageResource(R.drawable.airbrush_64);
                        topTextInstructions.setText("You are now in Create Mode! Tap the toggle button to the left to return to Design Mode.");
                    }
                    else {
                        //set design mode
                        costevaluator.setVisibility(View.VISIBLE);
                        substratedistance.setVisibility(View.INVISIBLE);
                        slider.setVisibility(View.INVISIBLE);
                        backbutton.setVisibility(View.INVISIBLE);
                        forwardsbutton.setVisibility(View.INVISIBLE);
                        toggleMode=0;
                        togglebutton.setImageResource(R.drawable.design_icon);
                        topTextInstructions.setText("You are in Design Mode! Slowly move camera to initiate augmented reality. When white dots appear, tap to place and edit your design.");
                    }
                }
            });

            //handle settings button in both design and create modes
            settingsdropdown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (toastMessage!= null) {
                        toastMessage.cancel();
                    }
                    toastMessage=Toast.makeText(ARviewActivity.this, "Settings open", Toast.LENGTH_SHORT);
                    toastMessage.show();
                }
            });

            if(toggleMode==0) {
                designMode();
            }
            else {
                designMode();
                //createMode();
            }
        }
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
        designChosen=0;
        arCam.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            clickNo++;
            //Add 3d model: the 3d model comes to the scene only when clickNo is one that means once
            if (clickNo == 1) {

                Anchor anchor = hitResult.createAnchor();

                //make costevaluator visible.. - this is currently buggy where it doesn't appear straight away.
                TextView costevaluator = findViewById(R.id.designmode_text_cost_evaluator);
                costevaluator.setVisibility(View.VISIBLE);

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
            }
        });
    }

    //alternative mode where object is fixed/editting no longer enabled and script/progress bar/forwardsback buttons are implemented
    private void createMode() {
        novicelevel=1;
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
}
