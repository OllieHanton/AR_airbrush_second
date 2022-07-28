package com.example.ar_airbrush_second;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
//import android.view.MotionEvent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ar_airbrush_second.bluetooth.SerialService;
import com.example.ar_airbrush_second.bluetooth.CommsFragment;
import com.google.android.filament.MaterialInstance;
import com.google.android.filament.TextureSampler;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.ar.core.Anchor;
//import com.google.ar.core.HitResult;
//import com.google.ar.core.Plane;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.collision.Ray;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Light;
import com.google.ar.sceneform.rendering.ModelRenderable;
//import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
//import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;

import com.google.ar.core.Pose;


//import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.lang.Math;

public class ARviewActivity extends AppCompatActivity implements ServiceConnection, PopupMenu.OnMenuItemClickListener, Scene.OnUpdateListener {

    //AR related:
    // object of ArFragment Class
    private ArFragment arCam;
    //helps to render the 3d model only once when we tap the screen
    private int clickNo = 0;

    public TransformableNode model;
    public FilamentAsset filamentAsset;
    private ModelRenderable foxRenderable;
    private ModelRenderable modelRenderable;

    //Design mode (0) or create mode (1) toggled flag
    private int toggleMode = 0;

    //Novice level (with text) or expert level (no text) - scale chosen for create mode... toggled flag.
    //Novice =0, intermediate =1, expert =2
    private int novicelevel = 1;

    //slider value - equivalent to spraying phase between 0 and 5
    private int sliderChangedValue;
    public boolean visualiseLayersOn = false;

    //number to count through script messages from file... between 51
    private int scriptCounter = 0;
    private int scriptCounterLast = 0;

    private TextView substrateDistance;
    private TextView costEvaluator;
    private AnchorNode anchorNode = null;
    private Anchor mainanchor = null;
    private Anchor copyOfMainanchor = null;
    private List<AnchorNode> anchorNodeList = new ArrayList<>();
    private List<Node> drawnNodesList = new ArrayList<>(); //list for all nodes, where model is the parent of all of them!
    private Node nodeForLine;
    public boolean objectFlag = false;

    public Node dielectricNode = new Node();
    public Node phosphorNode = new Node();
    public Node PEDOTNode = new Node();

    public Node electrode1Node = new Node();
    public Node electrode2Node = new Node();
    public Node electrode3Node = new Node();
    public Node electrode4Node = new Node();
    public Node electrode5Node = new Node();

    public Vector3 anchorNodePositionx = new Vector3(0,0,0);
    public Vector3 anchorNodePositionz = new Vector3(0,0,0);

    Vector3 objectPosition = null;
    Vector3 objectScale = null;
    Quaternion objectRotation = null;

    Pose objectCenter = null; // objectpose updated for translations (to update currently null).

    public boolean imageBeingPoppedupFlag = false;

    Toast toastMessage;

    // Settings values - (User level: 1=novice, 3=expert)
    public int level = 1;
    public boolean uv = true;
    public boolean laser = true;
    public boolean positionWarnings = true;
    public boolean virtualPaint;// = false;
    public boolean sprayBounds;// = false;
    public boolean cost = true;
    public boolean distance = true;

    public boolean triggerPress = false;

    //Design chosen from design library flag
    //1 = Dispray logo
    //2 = WC
    //3 = indicator
    //4 = bell
    private int designChosen = 1;

    //hardcoded cost:
    //designs: Object thickness: 3mm
    //
    //logo_design: 20366.0832 mm^3
    //logo_electrodes: 8604.9122 mm^3
    //
    //wc_design: 5382.8429 mm^3
    //wc_electrodes: 14352.4028 mm^3
    //
    //indicator_design: 18906.1919 mm^3
    //indicator_electrodes: 21679.8142 mm^3
    //
    //bell_design: 8223.0246 mm^3
    //bell_electrodes: 8208.1725 mm^3

    public double costPencePerMMSquared = 0.0005;
    private double design1area = (20366.08+8604.91)/3;
    private double design2area = (5582.84+14352.40)/3;
    private double design3area = (18906.19+21679.81)/3;
    private double design4area = (8223.02+8208.17)/3;

    public int objectResource = R.raw.logo_tiny;;
    public int objectWiringResource = R.raw.logo_welectrode_tiny;
    public boolean wiresToggle = false;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arview);
        getSupportActionBar().hide();

        if (checkSystemSupport(this)) {

            //Initiate ARCore functionality:
            setUpAnchorPoints();

            //initiate objects relating to each overlay button
            FloatingActionButton toggleButton = findViewById(R.id.toggle_mode_button);
            ImageButton settingsDropDown = findViewById(R.id.settings_drop_down_menu);
            costEvaluator = findViewById(R.id.designmode_text_cost_evaluator);
            substrateDistance = findViewById(R.id.createmode_text_distance_to_substrate);
            SeekBar slider = findViewById(R.id.createmode_seekbar);
            FloatingActionButton backButton = findViewById(R.id.createmode_back);
            FloatingActionButton forwardsButton = findViewById(R.id.createmode_forward);
            TextView topTextInstructions = findViewById(R.id.createmode_top_text_instructions);
            TextView sidetiptextbox = findViewById(R.id.createmode_tips);
            Button wiresButton = findViewById(R.id.addwiresbutton);
            Button deleteButton = findViewById(R.id.deleteobjectbutton);
            Button layerViewButton = findViewById(R.id.layerpreviewbutton);
            ImageView changingImageView = findViewById(R.id.changingImageView);

            //handle slider functionality - script here?
            slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int sliderProgress, boolean fromUser) {
                    turnOffElectrodeConnections(); //delete electrode animate objects
                    sliderChangedValue = sliderProgress;
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
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //clear drawn nodes and electrode connection pointers
                    clearAllDrawnNodes();
                    if (scriptCounter > 0) {
                        scriptCounterLast = scriptCounter;
                        scriptCounter--;
                    }
                    //fixing going back into previous stages...
                    if (scriptCounter == 2) {
                        slider.setProgress(0);
                        scriptCounter = 2;
                    }
                    if (scriptCounter == 13) {
                        slider.setProgress(1);
                        scriptCounter = 13;
                    }
                    if (scriptCounter == 22) {
                        slider.setProgress(2);
                        scriptCounter = 22;
                    }
                    if (scriptCounter == 32) {
                        slider.setProgress(3);
                        scriptCounter = 32;
                    }
                    if (scriptCounter == 41) {
                        slider.setProgress(4);
                        scriptCounter = 41;
                    }
                    implementScript();
                }
            });

            //handle createmode forwards button
            forwardsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //clear drawn nodes and electrode connection pointers
                    clearAllDrawnNodes();
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
                    /*if (anchorNode != null) {
                        //Get the current Pose and transform it then set a new anchor at the new pose
                        Session session = arCam.getArSceneView().getSession();
                        Anchor currentAnchor = anchorNode.getAnchor();
                        Pose oldPose = currentAnchor.getPose();
                        Pose newPose = oldPose.compose(Pose.makeTranslation(-0.05f,0,0));
                        anchorNode = moveRenderable(anchorNode, newPose);
                    }*/

                }
            });

            //Toggle between design and create modes
            toggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //reformat text for when the user toggles in or out...
                    topTextInstructions.setTextSize(14);
                    setTextBoxColour(R.drawable.tealtextbackground);
                    if (toggleMode == 0) {
                        //set create mode
                        costEvaluator.setVisibility(View.INVISIBLE);
                        substrateDistance.setVisibility(View.VISIBLE);
                        slider.setVisibility(View.VISIBLE);
                        backButton.setVisibility(View.VISIBLE);
                        forwardsButton.setVisibility(View.VISIBLE);
                        toggleMode = 1;
                        toggleButton.setImageResource(R.drawable.airbrush_64);
                        topTextInstructions.setText(getString(R.string.create_mode_welcome));
                        sidetiptextbox.setVisibility(View.VISIBLE);
                        changingImageView.setVisibility(View.INVISIBLE);
                        wiresButton.setVisibility(View.INVISIBLE);
                        deleteButton.setVisibility(View.INVISIBLE);
                        layerViewButton.setVisibility(View.INVISIBLE);
                        //suspend editable functionality of object here
                        //...

                        //Temporarily add colour changed object here...


                    } else {
                        //set design mode
                        costEvaluator.setVisibility(View.VISIBLE);
                        substrateDistance.setVisibility(View.INVISIBLE);
                        slider.setVisibility(View.INVISIBLE);
                        backButton.setVisibility(View.INVISIBLE);
                        forwardsButton.setVisibility(View.INVISIBLE);
                        toggleMode = 0;
                        toggleButton.setImageResource(R.drawable.design_icon);
                        topTextInstructions.setText(getString(R.string.design_mode_welcome));
                        sidetiptextbox.setVisibility(View.INVISIBLE);
                        changingImageView.setVisibility(View.INVISIBLE);
                        wiresButton.setVisibility(View.VISIBLE);
                        deleteButton.setVisibility(View.VISIBLE);
                        layerViewButton.setVisibility(View.VISIBLE);
                        //re-add editable functionality of object here
                        //...
                    }
                }
            });

            //handle settings button in both design and create modes
            settingsDropDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSettings(v);
                }
            });

            //handle settings button in both design and create modes
            wiresButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int wireNoWireModel;
                    if(wiresToggle) {
                        wireNoWireModel = objectResource;
                        wiresToggle=false;
                    }
                    else{
                        wireNoWireModel = objectWiringResource;
                        wiresToggle=true;
                    }
                    objectPosition = model.getLocalPosition();
                    objectScale = model.getLocalScale();
                    objectRotation = model.getLocalRotation();

                    model.getTranslationController();

                    //model.isEnabled = false;
                    //arCam.getArSceneView().getScene().removeChild(model);
                    anchorNode.removeChild(model);
                    ModelRenderable.builder()
                            //Currently hardcoded text object:
                            .setSource(ARviewActivity.this, wireNoWireModel)
                            .setIsFilamentGltf(true)
                            .build()
                            .thenAccept(modelRenderable -> readdModel(mainanchor, modelRenderable))
                            .exceptionally(throwable -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ARviewActivity.this);
                                builder.setMessage("Something is not right" + throwable.getMessage()).show();
                                return null;
                            });

                        //drawLine(anchorNode, -0.1f, 0f, -0.1f, 0.1f, 0f, 0.1f);
                        //drawHighlightedCircle(anchorNode, 0.1f, 0.01f, 0.1f, 0f, 0.1f, false);
                    //}



                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //mainanchor.detach();
                    anchorNode.removeChild(model);
                    clickNo=0;

                    /*//Delete the Anchor if it exists
                    //Log.d(TAG, "Deleteing anchor");
                    int currentAnchorIndex;
                    if (numberOfAnchors < 1) {
                        Toast.makeText(ARviewActivity.this, "All nodes deleted", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    removeAnchorNode(currentSelectedAnchorNode);
                    currentSelectedAnchorNode = null;

                    //Remove the wiring if it also exists
                    //removeLine(nodeForLine);*/
                }
            });

            layerViewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //dielectricNode = new Node();
                    //phosphorNode = new Node();
                    //PEDOTNode = new Node();
                    if (visualiseLayersOn) {
                        //clearAllDrawnNodes();
                        model.removeChild(dielectricNode);
                        model.removeChild(phosphorNode);
                        model.removeChild(PEDOTNode);
                        visualiseLayersOn = false;
                    } else {

                        Vector3 Point2 = model.getWorldPosition();

                        //First, find the vector extending between the two points and define a look rotation
                        //in terms of this Vector.
                        //final Vector3 difference = Vector3.subtract(Point1, Point2);
                        //final Vector3 directionFromTopToBottom = difference.normalized();
                        //final Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
                        com.google.ar.sceneform.rendering.Color orangeDielectric = new com.google.ar.sceneform.rendering.Color(0.91f, 0.5f, 0.07f);//(float)(1-232/255), (float)(1-128/255), (float)(1-18/255));
                        //com.google.ar.sceneform.rendering.Color orangeDielectric = new com.google.ar.sceneform.rendering.Color(0.99f, 0, 0,0.001f);//(float)(1-232/255), (float)(1-128/255), (float)(1-18/255));
                        com.google.ar.sceneform.rendering.Color yellowPhosphor = new com.google.ar.sceneform.rendering.Color(0.96f, 0.78f, 0.01f);
                        com.google.ar.sceneform.rendering.Color greenPEDOT = new com.google.ar.sceneform.rendering.Color(0.30f, 0.91f, 0.30f);
                        MaterialFactory.makeOpaqueWithColor(getApplicationContext(), orangeDielectric)
                                .thenAccept(
                                        material -> {
                                            ModelRenderable cubeModel = ShapeFactory.makeCube(
                                                    new Vector3(.08f, .001f, .08f),
                                                    new Vector3(0f, 0.015f, 0f), material);

                                            Anchor lineAnchor = anchorNode.getAnchor(); //changed to have anchor of node1...
                                            dielectricNode = new Node();
                                            dielectricNode.setParent(model);
                                            dielectricNode.setRenderable(cubeModel);
                                            //nodeForLine.setWorldPosition(Vector3.add(Point1, Point2).scaled(.5f));
                                            //nodeForLine.setWorldRotation(rotationFromAToB);
                                        }
                                );
                        MaterialFactory.makeOpaqueWithColor(getApplicationContext(), yellowPhosphor)
                                .thenAccept(
                                        material -> {
                                            ModelRenderable cubeModel = ShapeFactory.makeCube(
                                                    new Vector3(.08f, .001f, .08f),
                                                    new Vector3(0f, 0.03f, 0f), material);

                                            Anchor lineAnchor = anchorNode.getAnchor(); //changed to have anchor of node1...
                                            phosphorNode = new Node();
                                            phosphorNode.setParent(model);
                                            phosphorNode.setRenderable(cubeModel);
                                            //nodeForLine.setWorldPosition(Vector3.add(Point1, Point2).scaled(.5f));
                                            //nodeForLine.setWorldRotation(rotationFromAToB);
                                        }
                                );
                        MaterialFactory.makeOpaqueWithColor(getApplicationContext(), greenPEDOT)
                                .thenAccept(
                                        material -> {
                                            ModelRenderable cubeModel = ShapeFactory.makeCube(
                                                    new Vector3(.08f, .001f, .08f),
                                                    new Vector3(0f, 0.045f, 0f), material);

                                            Anchor lineAnchor = anchorNode.getAnchor(); //changed to have anchor of node1...
                                            PEDOTNode = new Node();
                                            PEDOTNode.setParent(model);
                                            PEDOTNode.setRenderable(cubeModel);
                                            //nodeForLine.setWorldPosition(Vector3.add(Point1, Point2).scaled(.5f));
                                            //nodeForLine.setWorldRotation(rotationFromAToB);
                                        }
                                );
                        visualiseLayersOn = true;
                    }
                }
            });

            designMode();
        }
    }

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

    //%%%%%%%%%%%%%%%%%%%%%%%
    /*private AnchorNode moveRenderable(AnchorNode markAnchorNodeToMove, Pose newPoseToMoveTo) {
        //Move a renderable to a new pose
        if (markAnchorNodeToMove != null) {
            arCam.getArSceneView().getScene().removeChild(markAnchorNodeToMove);
            anchorNodeList.remove(markAnchorNodeToMove);
        } else {
            Log.d(TAG,"moveRenderable - markAnchorNode was null, the little £$%^...");
            return null;
        }
        Frame frame = arFragment.getArSceneView().getArFrame();
        Session session = arFragment.getArSceneView().getSession();
        Anchor markAnchor = session.createAnchor(newPoseToMoveTo.extractTranslation());
        AnchorNode newMarkAnchorNode = new AnchorNode(markAnchor);
        newMarkAnchorNode.setRenderable(andyRenderable);
        newMarkAnchorNode.setParent(arFragment.getArSceneView().getScene());
        anchorNodeList.add(newMarkAnchorNode);

        //Delete the line if it is drawn
        removeLine(nodeForLine);

        return newMarkAnchorNode;
    }*/
    //%%%%%%%%%%%%%%%%%%%%%%%

    private void drawLine(AnchorNode node1, float x1, float y1, float z1, float x2, float y2, float z2) { //Vector3 adaptedPosition1, Vector3 adaptedPosition2) {
        Vector3 Point1 = node1.getWorldPosition();
        Vector3 Point2 = node1.getWorldPosition();

        Point1.x = Point1.x + x1;
        Point1.y = Point1.y + y1;
        Point1.z = Point1.z + z1;

        Point2.x = Point2.x + x2;
        Point2.y = Point2.y + y2;
        Point2.z = Point2.z + z2;

        //First, find the vector extending between the two points and define a look rotation
        //in terms of this Vector.
        final Vector3 difference = Vector3.subtract(Point1, Point2);
        final Vector3 directionFromTopToBottom = difference.normalized();
        final Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
        com.google.ar.sceneform.rendering.Color newColor = new com.google.ar.sceneform.rendering.Color(0, 0, 255);
        MaterialFactory.makeOpaqueWithColor(getApplicationContext(), newColor)
                .thenAccept(
                        material -> {
                            /* Then, create a rectangular prism, using ShapeFactory.makeCube() and use the difference vector
                                   to extend to the necessary length.  */
                            //Log.d(TAG,"drawLine insie .thenAccept");
                            ModelRenderable model = ShapeFactory.makeCube(
                                    new Vector3(.01f, .01f, difference.length()),
                                    Vector3.zero(), material);
                            /* Last, set the world rotation of the node to the rotation calculated earlier and set the world position to
                                   the midpoint between the given points . */
                            Anchor lineAnchor = node1.getAnchor(); //changed to have anchor of node1...
                            nodeForLine = new Node();
                            nodeForLine.setParent(node1);
                            nodeForLine.setRenderable(model);
                            nodeForLine.setWorldPosition(Vector3.add(Point1, Point2).scaled(.5f));
                            nodeForLine.setWorldRotation(rotationFromAToB);
                        }
                );
    }

    private void deleteRenderable(Node inputToDelete) {
        if (inputToDelete != null) {
            //Log.e(TAG, "removeLine lineToRemove is not mull");
            arCam.getArSceneView().getScene().removeChild(inputToDelete);
            inputToDelete.setParent(null);
            inputToDelete = null;
        }
    }


    private void drawHighlightedCircle(AnchorNode node1, float radius, float height, float centreadjustx, float centreadjusty, float centreadjustz, boolean green) { //Vector3 adaptedPosition1, Vector3 adaptedPosition2) {
        Vector3 cylinderLocation = node1.getWorldPosition();

        cylinderLocation.x = cylinderLocation.x + centreadjustx;
        cylinderLocation.y = cylinderLocation.y + centreadjusty;
        cylinderLocation.z = cylinderLocation.z + centreadjustz;

        //First, find the vector extending between the two points and define a look rotation
        //in terms of this Vector.
        //final Vector3 difference = Vector3.subtract(Point1, Point2);
        //final Vector3 directionFromTopToBottom = difference.normalized();
        //final Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
        com.google.ar.sceneform.rendering.Color cylinderColor = new com.google.ar.sceneform.rendering.Color(255, 255, 255, 0f);
        if (green = true) {
            cylinderColor = new com.google.ar.sceneform.rendering.Color(0, 255, 0, 128f);
        } else {
            cylinderColor = new com.google.ar.sceneform.rendering.Color(255, 0, 0, 128f);
        }
        MaterialFactory.makeOpaqueWithColor(getApplicationContext(), cylinderColor)
                .thenAccept(
                        material -> {
                            /* Then, create a rectangular prism, using ShapeFactory.makeCube() and use the difference vector
                                   to extend to the necessary length.  */
                            //Log.d(TAG,"drawLine insie .thenAccept");
                            ModelRenderable model = ShapeFactory.makeCylinder(radius, height, cylinderLocation, material);
                            /* Last, set the world rotation of the node to the rotation calculated earlier and set the world position to
                                   the midpoint between the given points . */
                            Anchor lineAnchor = node1.getAnchor(); //changed to have anchor of node1...
                            nodeForLine = new Node();
                            nodeForLine.setParent(node1);
                            nodeForLine.setRenderable(model);
                            nodeForLine.setWorldPosition(cylinderLocation);
                            //nodeForLine.setWorldRotation(rotationFromAToB);
                        }
                );
    }

//makeCylinder(float radius, float height, Vector3 center, Material material)

    public void showSettings(View v) {
        level = getIntSetting("level");
        uv = getBoolSetting("uv");
        laser = getBoolSetting("laser");
        positionWarnings = getBoolSetting("warnings");
        virtualPaint = getBoolSetting("paint");
        sprayBounds = getBoolSetting("bounds");
        distance = getBoolSetting("dist");
        cost = getBoolSetting("cost");
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
        menu.findItem(R.id.settings_warning).setChecked(positionWarnings);
        menu.findItem(R.id.settings_paint).setChecked(virtualPaint);
        menu.findItem(R.id.settings_bounds).setChecked(sprayBounds);
        menu.findItem(R.id.settings_dist).setChecked(distance);
        menu.findItem(R.id.settings_cost).setChecked(cost);
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
                laserControl();
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
            case R.id.settings_warning: {
                // Select to enable automatic UV light during spraying
                positionWarnings = !item.isChecked();
                item.setChecked(positionWarnings);
                saveBoolSetting("warnings", positionWarnings);
                if (toastMessage != null) {
                    toastMessage.cancel();
                }
                if (positionWarnings) {
                    toastMessage = Toast.makeText(this, "Position Warnings Enabled", Toast.LENGTH_SHORT);
                } else {
                    toastMessage = Toast.makeText(this, "Position Warnings Disabled", Toast.LENGTH_SHORT);
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
            case R.id.settings_paint: {
                // Select to enable virtual paint
                virtualPaint = !item.isChecked();
                item.setChecked(virtualPaint);
                saveBoolSetting("paint", virtualPaint);
                if (toastMessage != null) {
                    toastMessage.cancel();
                }
                if (virtualPaint) {
                    toastMessage = Toast.makeText(this, "Virtual Paint Enabled", Toast.LENGTH_SHORT);
                } else {
                    toastMessage = Toast.makeText(this, "Virtual Paint Disabled", Toast.LENGTH_SHORT);
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
            case R.id.settings_bounds: {
                // Select to enable spray bounds functionality
                sprayBounds = !item.isChecked();
                item.setChecked(sprayBounds);
                saveBoolSetting("bounds", sprayBounds);
                if (toastMessage != null) {
                    toastMessage.cancel();
                }
                if (sprayBounds) {
                    toastMessage = Toast.makeText(this, "Spray Bounds Enabled", Toast.LENGTH_SHORT);
                } else {
                    toastMessage = Toast.makeText(this, "Spray Bounds Disabled", Toast.LENGTH_SHORT);
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
            case R.id.settings_dist: {
                // Select to enable distance function
                distance = !item.isChecked();
                item.setChecked(distance);
                saveBoolSetting("dist", distance);
                if (toastMessage != null) {
                    toastMessage.cancel();
                }
                if (distance) {
                    toastMessage = Toast.makeText(this, "Distance Indicator Enabled", Toast.LENGTH_SHORT);
                } else {
                    toastMessage = Toast.makeText(this, "Distance Indicator Disabled", Toast.LENGTH_SHORT);
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
            case R.id.settings_cost: {
                // Select to enable cost function
                cost = !item.isChecked();
                item.setChecked(cost);
                saveBoolSetting("cost", cost);
                if (toastMessage != null) {
                    toastMessage.cancel();
                }
                if (cost) {
                    toastMessage = Toast.makeText(this, "Cost Indicator Enabled", Toast.LENGTH_SHORT);
                } else {
                    toastMessage = Toast.makeText(this, "Cost Indicator Disabled", Toast.LENGTH_SHORT);
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
    //TODO Also should call modules and functionality (e.g. UV light) while in create mode.
    private void implementScript() {
        TextView topTextInstructions = findViewById(R.id.createmode_top_text_instructions);
        SeekBar slider = findViewById(R.id.createmode_seekbar);
        ImageView changingImageView = findViewById(R.id.changingImageView);
        TextView tipTextView = findViewById(R.id.createmode_tips);
        int progressIncrementor = slider.getProgress();
        topTextInstructions.setTextSize(14);
        if (scriptCounter == 0) {
            topTextInstructions.setText(getString(R.string.create_mode_welcome));
        } else {
            int resourceId = this.getResources().getIdentifier("script_" + scriptCounter, "string", this.getPackageName());
            topTextInstructions.setText(getString(resourceId));
            //if tip_script_x != null then implement as a toast
        }

        //implement tips when novice level is 1 and make invisible in all other circumstances.
        int tipResourceId = 0;
        tipResourceId = this.getResources().getIdentifier("tip_script_" + scriptCounter, "string", this.getPackageName());
        if (tipResourceId == 0) {
            tipTextView.setVisibility(View.INVISIBLE);
        } else if (novicelevel == 1) {
            tipTextView.setText(getString(tipResourceId));
            tipTextView.setVisibility(View.VISIBLE);
        } else {
            tipTextView.setVisibility(View.INVISIBLE);
        }


        //clear popup image each time button is pressed...
        changingImageView.setVisibility(View.INVISIBLE);

        if (scriptCounter != 12 && scriptCounter != 21 && scriptCounter != 31 && scriptCounter != 40 && tvTimer != null) {
            timer.cancel();
            tvTimer.setVisibility(View.GONE);
            tvTimer = null;
        }

        if (toastMessage != null) {
            toastMessage.cancel();
        }
        //toastMessage = Toast.makeText(ARviewActivity.this, "Script " + tipResourceId, Toast.LENGTH_SHORT);
        //toastMessage.show();
        //set progressIncrementor based on where the script is
        if (scriptCounter == 0) {
            progressIncrementor = 0;// and update...
            //toastMessage = Toast.makeText(ARviewActivity.this, "Phase " + sliderChangedValue + ": intro", Toast.LENGTH_SHORT);
            //toastMessage.show();
        }
        if (scriptCounter == 1) {
            displayImageFor3Secs(changingImageView, R.drawable.phone_in_hand, 0.21f); //phone image
            setTextBoxColour(R.drawable.tealtextbackground);
        }
        if (scriptCounter == 2) {
            setTextBoxColour(R.drawable.tealtextbackground);
        }
        //////////////////////phase1
        if (scriptCounter == 3) {
            progressIncrementor = 1;// and update...
            //toastMessage = Toast.makeText(ARviewActivity.this, "Phase " + sliderChangedValue + ": Spraying Base electrode", Toast.LENGTH_SHORT);
            //toastMessage.show();
            topTextInstructions.setTextSize(20);
            setTextBoxColour(R.drawable.redtextbackground);
            displayImageFor3Secs(changingImageView, R.drawable.backplane, 0.32f); //backplane image
        }
        if (scriptCounter == 4) {
            displayImageFor3Secs(changingImageView, R.drawable.respirator, 0.32f); //PPE image
        }
        if (scriptCounter == 7) {
            displayImageFor3Secs(changingImageView, R.drawable.compressor, 0.32f); //Compressor image
        }
        if (scriptCounter == 10) {
            displayImageFor3Secs(changingImageView, R.drawable.french_curve4, 0.32f); //Stencil image
        }
        if (scriptCounter == 12) {
            countDownStart();
        }
        if (scriptCounter == 13) {
            setTextBoxColour(R.drawable.redtextbackground);
        }
        //////////////////////phase2
        if (scriptCounter == 14) {
            progressIncrementor = 2;// and update...
            //toastMessage = Toast.makeText(ARviewActivity.this, "Phase " + sliderChangedValue + ": Spraying dielectric", Toast.LENGTH_SHORT);
            //toastMessage.show();
            topTextInstructions.setTextSize(20);
            setTextBoxColour(R.drawable.orangetextbackground);
            displayImageFor3Secs(changingImageView, R.drawable.dielectric, 0.43f); //dielectric image
        }
        if (scriptCounter == 15) {
            displayImageFor3Secs(changingImageView, R.drawable.respirator, 0.43f); //PPE image
        }
        if (scriptCounter == 17) {
            displayImageFor3Secs(changingImageView, R.drawable.compressor, 0.43f); //Compressor image
        }
        if (scriptCounter == 18) {
            turnOffElectrodeConnections();
        }
        if (scriptCounter == 19) {
            highlightElectrodeConnections(1);
        }
        if (scriptCounter == 20) {
            turnOffElectrodeConnections();
        }
        if (scriptCounter == 21) {
            countDownStart();
        }
        if (scriptCounter == 22) {
            setTextBoxColour(R.drawable.orangetextbackground);
        }
        //////////////////////phase3
        if (scriptCounter == 23) {
            progressIncrementor = 3;// and update...
            //toastMessage = Toast.makeText(ARviewActivity.this, "Phase " + sliderChangedValue + ": Spraying electroluminescent coat", Toast.LENGTH_SHORT);
            //toastMessage.show();
            topTextInstructions.setTextSize(20);
            setTextBoxColour(R.drawable.yellowtextbackground);
            displayImageFor3Secs(changingImageView, R.drawable.lumicolour, 0.55f); //electroluminescent image
        }
        if (scriptCounter == 24) {
            displayImageFor3Secs(changingImageView, R.drawable.respirator, 0.55f); //PPE image
        }
        if (scriptCounter == 27) {
            displayImageFor3Secs(changingImageView, R.drawable.compressor, 0.55f); //Compressor image
        }
        if (scriptCounter == 31) {
            countDownStart();
        }
        if (scriptCounter == 32) {
            setTextBoxColour(R.drawable.yellowtextbackground);
        }
        //////////////////////phase4
        if (scriptCounter == 33) {
            progressIncrementor = 4;// and update...
            //toastMessage = Toast.makeText(ARviewActivity.this, "Phase " + sliderChangedValue + ": Spraying transparent conductive electrode", Toast.LENGTH_SHORT);
            //toastMessage.show();
            topTextInstructions.setTextSize(20);
            setTextBoxColour(R.drawable.greentextbackground);
            displayImageFor3Secs(changingImageView, R.drawable.pedot, 0.68f); //PEDOT image
        }
        if (scriptCounter == 34) {
            displayImageFor3Secs(changingImageView, R.drawable.respirator, 0.68f); //PPE image
        }
        if (scriptCounter == 36) {
            displayImageFor3Secs(changingImageView, R.drawable.compressor, 0.68f); //compressor image
        }
        if (scriptCounter == 40) {
            countDownStart();
        }
        if (scriptCounter == 41) {
            setTextBoxColour(R.drawable.greentextbackground);
        }
        //////////////////////phase5
        if (scriptCounter == 42) {
            progressIncrementor = 5;// and update...
            //toastMessage = Toast.makeText(ARviewActivity.this, "Phase " + sliderChangedValue + ": Electrode attachment", Toast.LENGTH_SHORT);
            //toastMessage.show();
            topTextInstructions.setTextSize(20);
            setTextBoxColour(R.drawable.bluetextbackground);
            displayImageFor3Secs(changingImageView, R.drawable.control, 0.80f); //Control image
        }
        if (scriptCounter == 44) {
            displayImageFor3Secs(changingImageView, R.drawable.multimeter, 0.80f); //multimeter image
        }
        if (scriptCounter == 45) {
            turnOffElectrodeConnections();
        }
        if (scriptCounter == 46) {
            displayImageFor3Secs(changingImageView, R.drawable.electrode_attachment2, 0.80f); //electrodes image
            highlightElectrodeConnections(0);
        }
        if (scriptCounter == 47) {
            turnOffElectrodeConnections();
        }
        if (scriptCounter == 51) {
            displayImageFor3Secs(changingImageView, R.drawable.fireworks2, 0.80f); //fireworks image
            setTextBoxColour(R.drawable.bluetextbackground);
        }
        if (progressIncrementor <= slider.getMax()) {
            slider.setProgress(progressIncrementor);
        }
        uvControl();
    }

    private void setTextBoxColour(int colour) {//}, int thumbColor){
        SeekBar slider = findViewById(R.id.createmode_seekbar);

        TextView topTextInstructions = findViewById(R.id.createmode_top_text_instructions);
        TextView tipTextView = findViewById(R.id.createmode_tips);
        Drawable background = ContextCompat.getDrawable(ARviewActivity.this, colour);
        topTextInstructions.setBackground(background);
        //tipTextView.setBackground(background);

        slider.setThumbTintList(ColorStateList.valueOf(getResources().getColor(R.color.turquoise)));
       // slider.setThumb
    }

    private void displayImageFor3Secs(ImageView inputImageView, int resourceId, float horizontalSkew) {
        if (level == 1) {
            if (imageBeingPoppedupFlag) {
                imgTimer.cancel();
                inputImageView.setVisibility(View.GONE);
                imageBeingPoppedupFlag = false;
            }
            if (!imageBeingPoppedupFlag) {
                imageBeingPoppedupFlag = true;
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) inputImageView.getLayoutParams();
                params.horizontalBias = horizontalSkew;
                inputImageView.setLayoutParams(params);
                inputImageView.setImageResource(resourceId); //set image
                inputImageView.setClipToOutline(true);
                inputImageView.setVisibility(View.VISIBLE);
                imgTimer(inputImageView);
            }
        }
    }

    //region Image Timer
    public CountDownTimer imgTimer;

    private void imgTimer(ImageView inputImageView) {
        imgTimer = new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                inputImageView.setVisibility(View.GONE);
                imageBeingPoppedupFlag = false;
            }
        }.start();
    }
    //endregion

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
            scriptCounter = 42;
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
        designChosen = getIntSetting("designChoice");
        if(designChosen == 1) {
            objectResource = R.raw.logo_tiny;
            objectWiringResource = R.raw.logo_welectrode_tiny;
        }
        else if(designChosen == 2){
            objectResource=R.raw.wc_tiny;
            objectWiringResource=R.raw.wc_welectrode_tiny;
        }
        else if(designChosen == 3){
            objectResource=R.raw.indicator_tiny;
            objectWiringResource=R.raw.indicator_welectrode_tiny;
        }
        else if(designChosen == 4){
            objectResource=R.raw.bell_tiny;
            objectWiringResource=R.raw.bell_welectrode_tiny;
        }

        int finalObjectResource = objectResource;

        arCam.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            clickNo++;
            //Add 3d model: the 3d model comes to the scene only when clickNo is one that means once
            if (clickNo == 1) {

                mainanchor = hitResult.createAnchor();
                ModelRenderable.builder()
                        //Currently hardcoded text object:
                        .setSource(this, finalObjectResource)
                        .setIsFilamentGltf(true)
                        .build()
                        .thenAccept(modelRenderable -> addModel(mainanchor, modelRenderable))
                        .exceptionally(throwable -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage("Something is not right" + throwable.getMessage()).show();
                            return null;
                        });

                /*if (anchorNode != null) {
                    anchorNode.setRenderable(modelRenderable);
                }*/
                //com.google.ar.sceneform.rendering.Color newColor = new com.google.ar.sceneform.rendering.Color(0, 0, 255);
                //ModelRenderable newRenderable = modelRenderable.makeCopy();
                //newRenderable.getMaterial().setFloat3("baseColorTint", 0.0f, 0.0f, 1.0f);
                //model.setRenderable(newRenderable);
            }
        });
    }

    /*void onRenderableLoaded(Renderable model) {
        Node modelNode = new Node();
        modelNode.setRenderable(model);
        scene.addChild(modelNode);
        modelNode.setLocalPosition(new Vector3(0, 0, 0));
    }*/

    private void addModel(Anchor mainanchor, ModelRenderable modelRenderable) {
        //modelRenderable.getMaterial().setFloat4("baseColorTint",  0.0f, 0.0f, 0.0f, 1.0f); //this breaks it
        anchorNode = new AnchorNode(mainanchor);
        // Creating a AnchorNode with a specific anchor
        anchorNode.setParent(arCam.getArSceneView().getScene());
        //attaching the anchorNode with the ArFragment
        model = new TransformableNode(arCam.getTransformationSystem());
        model.getScaleController().setMaxScale(10f);
        model.getScaleController().setMinScale(1f);


        model.setParent(anchorNode);

        com.google.ar.sceneform.rendering.Color newColor = new com.google.ar.sceneform.rendering.Color(0, 0, 255);
        model.setLight(Light.builder(Light.Type.POINT).setColor(newColor).build());

        //ModelRenderable newColorCopyofRenderable = modelRenderable.makeCopy();
        //newColorCopyofRenderable.getMaterial().setFloat3("baseColorTint", newColor);
        //model.setRenderable(newColorCopyofRenderable);

        model.setRenderable(modelRenderable);

        arCam.getArSceneView().getScene().addOnUpdateListener(this);
        arCam.getArSceneView().getScene().addChild(anchorNode);
        //attaching the 3d model with the TransformableNode that is already attached with the node
        model.select();
        filamentAsset = model.getRenderableInstance().getFilamentAsset();
    }

    //method for readding a model when wires button is pressed
    private void readdModel(Anchor mainanchor, ModelRenderable modelRenderable) {
        //modelRenderable.getMaterial().setFloat4("baseColorTint",  0.0f, 0.0f, 0.0f, 1.0f); //this breaks it
        anchorNode = new AnchorNode(mainanchor);
        // Creating a AnchorNode with a specific anchor
        anchorNode.setParent(arCam.getArSceneView().getScene());
        //attaching the anchorNode with the ArFragment
        model = new TransformableNode(arCam.getTransformationSystem());
        model.getScaleController().setMaxScale(10f);
        model.getScaleController().setMinScale(1f);

        model.setLocalPosition(objectPosition);
        model.setLocalScale(objectScale);
        model.setLocalRotation(objectRotation);

        model.setParent(anchorNode);

        com.google.ar.sceneform.rendering.Color newColor = new com.google.ar.sceneform.rendering.Color(0, 0, 255);
        model.setLight(Light.builder(Light.Type.POINT).setColor(newColor).build());

        //ModelRenderable newColorCopyofRenderable = modelRenderable.makeCopy();
        //newColorCopyofRenderable.getMaterial().setFloat3("baseColorTint", newColor);
        //model.setRenderable(newColorCopyofRenderable);



        model.setRenderable(modelRenderable);

        arCam.getArSceneView().getScene().addOnUpdateListener(this);
        arCam.getArSceneView().getScene().addChild(anchorNode);
        //attaching the 3d model with the TransformableNode that is already attached with the node
        model.select();
        filamentAsset = model.getRenderableInstance().getFilamentAsset();
    }

    /*public void changeMaterials () {
        MaterialInstance[] materialInstances = filamentAsset.getMaterialInstances();

        TextureSampler textureSampler = new TextureSampler();

        for (MaterialInstance materialInstance : materialInstances) {
            //if (materialInstance.getName() == "example_name") {
                materialInstance.setParameter("baseColorFactor", 0.3f, 0.5f, 0.7f); // Values for Red, Green and Blue
            //}
        }
    }*/

    public void highlightElectrodeConnections(int redgreen){
        //todo
        //do them in green for when we are spraying them
        //do them in red for when you need to not spray over them...
        com.google.ar.sceneform.rendering.Color chosenElectrodeColour;
        com.google.ar.sceneform.rendering.Color electrodeHighlightRed = new com.google.ar.sceneform.rendering.Color(0.8f, 0.1f, 0.1f);
        com.google.ar.sceneform.rendering.Color electrodeHighlightGreen = new com.google.ar.sceneform.rendering.Color(0.1f, 0.8f, 0.1f);

        if(redgreen==1){
            chosenElectrodeColour=electrodeHighlightRed;
        }
        else{
            chosenElectrodeColour=electrodeHighlightGreen;
        }

        if(designChosen==1){
            Vector3 electrode1 = new Vector3(0.12f, 0.001f, -0.01f);
            Vector3 electrode2 = new Vector3(0.12f, 0.001f, 0.01f);
            Vector3 electrode3 = new Vector3(-0.08f, 0.001f, 0.04f);
            createElectrodesNodes(chosenElectrodeColour, electrode1, electrode2, electrode3, electrode3, electrode3);
        }
        if(designChosen==2){
            Vector3 electrode1 = new Vector3(-0.01f, 0.001f, 0.07f);
            Vector3 electrode2 = new Vector3(0.02f, 0.001f, 0.07f);
            Vector3 electrode3 = new Vector3(0.04f, 0.001f, 0.07f);
            createElectrodesNodes(chosenElectrodeColour, electrode1, electrode2, electrode3, electrode3, electrode3);
        }
        if(designChosen==3){
            Vector3 electrode1 = new Vector3(0f, 0.001f, 0.08f);
            Vector3 electrode2 = new Vector3(0.02f, 0.001f, 0.08f);
            Vector3 electrode3 = new Vector3(0.04f, 0.001f, 0.08f);
            Vector3 electrode4 = new Vector3(0.06f, 0.001f, 0.08f);
            Vector3 electrode5 = new Vector3(0.08f, 0.001f, 0.08f);
            createElectrodesNodes(chosenElectrodeColour, electrode1, electrode2, electrode3, electrode4, electrode5);
        }
        if(designChosen==4){
            Vector3 electrode1 = new Vector3(0.7f, 0.001f, -0.01f);
            Vector3 electrode2 = new Vector3(0.7f, 0.001f, 0.01f);
            createElectrodesNodes(chosenElectrodeColour, electrode1, electrode2, electrode2, electrode2, electrode2);
        }



    }

    public void createElectrodesNodes( com.google.ar.sceneform.rendering.Color chosenElectrodeColour, Vector3 electrode1, Vector3 electrode2, Vector3 electrode3, Vector3 electrode4, Vector3 electrode5){
        MaterialFactory.makeOpaqueWithColor(getApplicationContext(), chosenElectrodeColour)
                .thenAccept(
                        material -> {
                            ModelRenderable cylinderHighlight = ShapeFactory.makeCylinder(0.015f,0.005f,
                                    electrode1, material);

                            Anchor lineAnchor = anchorNode.getAnchor(); //changed to have anchor of node1...
                            electrode1Node = new Node();
                            electrode1Node.setParent(model);
                            electrode1Node.setRenderable(cylinderHighlight);
                            //nodeForLine.setWorldPosition(Vector3.add(Point1, Point2).scaled(.5f));
                            //nodeForLine.setWorldRotation(rotationFromAToB);
                        }
                );
        MaterialFactory.makeOpaqueWithColor(getApplicationContext(), chosenElectrodeColour)
                .thenAccept(
                        material -> {
                            ModelRenderable cylinderHighlight = ShapeFactory.makeCylinder(0.015f,0.005f,
                                    electrode2, material);

                            Anchor lineAnchor = anchorNode.getAnchor(); //changed to have anchor of node1...
                            electrode2Node = new Node();
                            electrode2Node.setParent(model);
                            electrode2Node.setRenderable(cylinderHighlight);
                            //nodeForLine.setWorldPosition(Vector3.add(Point1, Point2).scaled(.5f));
                            //nodeForLine.setWorldRotation(rotationFromAToB);
                        }
                );
        MaterialFactory.makeOpaqueWithColor(getApplicationContext(), chosenElectrodeColour)
                .thenAccept(
                        material -> {
                            ModelRenderable cylinderHighlight = ShapeFactory.makeCylinder(0.015f,0.005f,
                                    electrode3, material);

                            Anchor lineAnchor = anchorNode.getAnchor(); //changed to have anchor of node1...
                            electrode3Node = new Node();
                            electrode3Node.setParent(model);
                            electrode3Node.setRenderable(cylinderHighlight);
                            //nodeForLine.setWorldPosition(Vector3.add(Point1, Point2).scaled(.5f));
                            //nodeForLine.setWorldRotation(rotationFromAToB);
                        }
                );
        MaterialFactory.makeOpaqueWithColor(getApplicationContext(), chosenElectrodeColour)
                .thenAccept(
                        material -> {
                            ModelRenderable cylinderHighlight = ShapeFactory.makeCylinder(0.015f,0.005f,
                                    electrode4, material);

                            Anchor lineAnchor = anchorNode.getAnchor(); //changed to have anchor of node1...
                            electrode4Node = new Node();
                            electrode4Node.setParent(model);
                            electrode4Node.setRenderable(cylinderHighlight);
                            //nodeForLine.setWorldPosition(Vector3.add(Point1, Point2).scaled(.5f));
                            //nodeForLine.setWorldRotation(rotationFromAToB);
                        }
                );
        MaterialFactory.makeOpaqueWithColor(getApplicationContext(), chosenElectrodeColour)
                .thenAccept(
                        material -> {
                            ModelRenderable cylinderHighlight = ShapeFactory.makeCylinder(0.015f,0.005f,
                                    electrode5, material);

                            Anchor lineAnchor = anchorNode.getAnchor(); //changed to have anchor of node1...
                            electrode5Node = new Node();
                            electrode5Node.setParent(model);
                            electrode5Node.setRenderable(cylinderHighlight);
                            //nodeForLine.setWorldPosition(Vector3.add(Point1, Point2).scaled(.5f));
                            //nodeForLine.setWorldRotation(rotationFromAToB);
                        }
                );
    }

    public void turnOffElectrodeConnections(){
        model.removeChild(electrode1Node);
        model.removeChild(electrode2Node);
        model.removeChild(electrode3Node);
        model.removeChild(electrode4Node);
        model.removeChild(electrode5Node);
    }

    public void clearAllDrawnNodes(){
        //arraylist get length
        int arrayListLength = drawnNodesList.size();
        //loop to go through each arraylist element and delete from parent node
        for (int counter = 0; counter < arrayListLength; counter++) {
            if(drawnNodesList.get(counter) != null) {
                anchorNode.removeChild(drawnNodesList.get(counter));
                model.removeChild(drawnNodesList.get(counter));
            }
        }
        //loop to go through each arraylist element and remove from arraylist
        /*for (int counter = 0; counter < arrayListLength; counter++) {
            drawnNodesList.remove(counter);
        }*/ // this currently crashes the app...
    }


    @Override
    public void onUpdate(FrameTime frameTime) {

        distanceCalculatingAndDisplaying(); //done
        costCalculatingAndDisplaying(); //done
        virtualPaintARImplementation(); //to do
        positionAndAngleWarning(); //done
        warningForOutsideSprayBounds(); //to do

    }

    //function to calculate distance from object and display it in specific textview
    public void distanceCalculatingAndDisplaying(){
        Frame frame = arCam.getArSceneView().getArFrame();
        if(distance==true && toggleMode==1) {
            substrateDistance.setVisibility(View.VISIBLE);
            //logic to calculate distance from camera to object
            if (anchorNode != null) {
                Pose objectPose = mainanchor.getPose();
                Pose cameraPose = frame.getCamera().getPose();
                float dx = objectPose.tx() - cameraPose.tx();
                float dy = objectPose.ty() - cameraPose.ty();
                float dz = objectPose.tz() - cameraPose.tz();

                ///Compute the straight-line distance. Round value to 2dp in the process:
                float distanceMeters = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                distanceMeters = distanceMeters * 100;
                distanceMeters = Math.round(distanceMeters);
                int intDistanceMeters = (int) distanceMeters;
                //update text view with distance as phone moves - subtract length of the airbrush for distance from nozzle to object...
                substrateDistance.setText(intDistanceMeters + "cm");
            }
        }
        else if(toggleMode==0){
            substrateDistance.setVisibility(View.INVISIBLE);
        }

    }

    //function to calculate cost
    public void costCalculatingAndDisplaying(){
        if(cost==true && toggleMode==0) {
            costEvaluator.setVisibility(View.VISIBLE);
            //calculate cost, round value to 2dp in the process:
            float scale = model.getWorldScale().x;
            double designArea = design1area;
            if (designChosen == 4) {
                designArea = design4area;
            } else if (designChosen == 3) {
                designArea = design3area;
            } else if (designChosen == 2) {
                designArea = design2area;
            }
            double costi = design1area * costPencePerMMSquared * scale;
            costi = costi * 100;
            costi = Math.round(costi);
            costi = costi / 100;
            costEvaluator.setText("£" + costi);
        }
        else if(toggleMode==1){
            costEvaluator.setVisibility(View.INVISIBLE);
        }
    }

    //////////////function to show angle of spray and warn if wrong angle
    //positionWarnings - global variable stored in settings
    //check x,y,z angles and if greater than say 30 degrees feed a warning back to user
    public void positionAndAngleWarning() {
        Frame frame = arCam.getArSceneView().getArFrame();
        TextView warningTextBox = findViewById(R.id.warningtextbox);
        int bothWarnings=0;
        if(positionWarnings) {
            if (true){//scriptCounter == 1) { //update here

                //logic to calculate distance from camera to object
                if (anchorNode != null) {
                    Pose objectPose = mainanchor.getPose();
                    Pose cameraPose = frame.getCamera().getPose();
                    float dx = objectPose.tx() - cameraPose.tx();
                    float dy = objectPose.ty() - cameraPose.ty();
                    float dz = objectPose.tz() - cameraPose.tz();
                    ///Compute the straight-line distance. Round value to 2dp in the process:
                    float distanceMeters = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                    distanceMeters = distanceMeters * 100;
                    distanceMeters = Math.round(distanceMeters);
                    //Handle trigger press and warning that you might be too far away...:
                        if (triggerPress) {
                            if (distanceMeters > 45) {
                                warningTextBox.setText("Warning: spraying occuring greater than optimal distance from substrate");
                                warningTextBox.setVisibility(View.VISIBLE);
                                bothWarnings++;
                            }
                        }
                }
                //logic to calculate angle warning
                if (anchorNode != null) {
                    double xangleFromVertical = 0;
                    double yangleFromVertical = 0;
                    double zangleFromVertical = 0;
                    Camera arCamera = arCam.getArSceneView().getScene().getCamera();
                    Ray ray = new Ray(arCamera.getWorldPosition(),arCamera.getForward());

                    //ray.getOrigin();
                    xangleFromVertical = ray.getDirection().x;
                    zangleFromVertical = ray.getDirection().z;
                    //substrateDistance.setText("xdirection: " + ray.getDirection().x + ", ydirection: " + ray.getDirection().y + ", zdirection: " + ray.getDirection().z);

                    if (triggerPress) {
                        if (xangleFromVertical >0.25 || xangleFromVertical <-0.25 || zangleFromVertical >0.25 || zangleFromVertical <-0.25) { //45% threshold
                            //implement warning bar: "Warning: spraying occuring greater than optimal distance from substrate"
                            warningTextBox.setVisibility(View.VISIBLE);
                            warningTextBox.setText("Warning: angle of airbrush not perpendicular to surface");
                            bothWarnings++;
                        }
                    }
                }
                if(bothWarnings==2){
                    warningTextBox.setVisibility(View.VISIBLE);
                    warningTextBox.setText("Warning: airbrush not perpendicular to surface and distance is greater than optimal for spraying");
                }
            }
        }
    }



    ///////////function for circles appearing but make it toggleable... and make it when you press the trigger
    //virtualPaint - global variable stored in settings
    public void virtualPaintARImplementation(){
        Frame frame = arCam.getArSceneView().getArFrame();
        //if button pressed
        if(virtualPaint){ //virtualPaint
            if (anchorNode != null) {
                Pose objectPose = mainanchor.getPose(); //object post for if we aren't moving the object around.
                Pose cameraPose = frame.getCamera().getPose();
                Vector3 objectWorldPosition = anchorNode.getWorldPosition(); //..

                //float x = cameraPose.tx();
                //float y = cameraPose.ty();
                //float z = cameraPose.tz();

                // get the camera
                Camera arCamera = arCam.getArSceneView().getScene().getCamera();

                Ray ray = new Ray(arCamera.getWorldPosition(),arCamera.getForward());
                HitTestResult result = arCam.getArSceneView().getScene().hitTest(ray);

                Node nonTransformableModel = (Node)model;
                if (result.getNode()==nonTransformableModel) {

                    float intersectionx = result.getPoint().x;
                    float intersectiony = result.getPoint().y;
                    float intersectionz = result.getPoint().z;
                    //substrateDistance.setText("Intersect x: " + intersectionx + "Intersect y: " + intersectiony + "Intersect z: " + intersectionz);

                    //objectWorldPosition.x = objectWorldPosition.x + intersectionx;
                    //objectWorldPosition.y = objectWorldPosition.y + intersectiony;
                    //objectWorldPosition.z = objectWorldPosition.z + intersectionz;

                    com.google.ar.sceneform.rendering.Color newColor = new com.google.ar.sceneform.rendering.Color(0, 0.2f, 0.8f, 0.1f); //a=0 is transparent, a=1 is opaque
                    //newColor.set(0, 0.2f, 1, 0.1f);
                    MaterialFactory.makeOpaqueWithColor(getApplicationContext(), newColor)
                            .thenAccept(
                                    material -> {
                                        // Then, create a rectangular prism, using ShapeFactory.makeCube() and use the difference vector to extend to the necessary length.
                                        //Log.d(TAG,"drawLine insie .thenAccept");
                                        ModelRenderable modelTemp = ShapeFactory.makeCylinder(
                                                //new Vector3(0.01f, 0.01f, 0.01f),
                                                0.005f, 0.001f,
                                                Vector3.zero(), material);

                                        //Last, set the world rotation of the node to the rotation calculated earlier and set the world position to the midpoint between the given points.
                                        //Anchor lineAnchor = anchorNode.getAnchor(); //changed to have anchor of node1...
                                        nodeForLine = new Node();
                                        nodeForLine.setParent(model);//result.getNode());
                                        nodeForLine.setWorldPosition(new Vector3(result.getPoint().x, result.getPoint().y+0.03f, result.getPoint().z));//new Vector3(anchorNode.getWorldPosition().x, 0, anchorNode.getWorldPosition().y));
                                        nodeForLine.setRenderable(modelTemp);
                                        //nodeForLine.setLocalPosition(new Vector3(0f, 0f, 0f));
                                        drawnNodesList.add(nodeForLine);

                                        //nodeForLine.setWorldRotation(rotationFromAToB);
                                    }
                            );

                    /*MaterialFactory.makeOpaqueWithColor(this, color)
                            .thenAccept(material -> {
                                // The sphere is in local coordinate space, so make the center 0,0,0
                                Renderable sphere = ShapeFactory.makeSphere(0.05f, Vector3.zero(),
                                        material);

                                Node indicatorModel = new Node();
                                indicatorModel.setParent(hitTestResult.getNode());
                                indicatorModel.setWorldPosition(hitTestResult.getPoint());
                                indicatorModel.setRenderable(sphere);
                            });*/
                    //substrateDistance.setText("anchor x: " + anchorNode.getWorldPosition().x + " anchor y: " + anchorNode.getWorldPosition().y + " anchor z: " + anchorNode.getWorldPosition().z);
                }
                //result.reset();

                //}
            }
        }
        /*if (anchorNode != null) {
            Vector3 Point1 = anchorNode.getWorldPosition();

            Point1.x = Point1.x + x1;
            Point1.y = Point1.y + y1;
            Point1.z = Point1.z + z1;

            Point2.x = Point2.x + x2;
            Point2.y = Point2.y + y2;
            Point2.z = Point2.z + z2;

            //First, find the vector extending between the two points and define a look rotation
            //in terms of this Vector.
            final Vector3 difference = Vector3.subtract(Point1, Point2);
            final Vector3 directionFromTopToBottom = difference.normalized();
            final Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
            com.google.ar.sceneform.rendering.Color newColor = new com.google.ar.sceneform.rendering.Color(0, 0, 255);
            MaterialFactory.makeOpaqueWithColor(getApplicationContext(), newColor)
                    .thenAccept(
                            material -> {
                            // Then, create a rectangular prism, using ShapeFactory.makeCube() and use the difference vector
                             //      to extend to the necessary length.
                                //Log.d(TAG,"drawLine insie .thenAccept");
                                ModelRenderable model = ShapeFactory.makeCube(
                                        new Vector3(.01f, .01f, difference.length()),
                                        Vector3.zero(), material);
                            // Last, set the world rotation of the node to the rotation calculated earlier and set the world position to
                             //      the midpoint between the given points .
                                Anchor lineAnchor = node1.getAnchor(); //changed to have anchor of node1...
                                nodeForLine = new Node();
                                nodeForLine.setParent(node1);
                                nodeForLine.setRenderable(model);
                                nodeForLine.setWorldPosition(Vector3.add(Point1, Point2).scaled(.5f));
                                nodeForLine.setWorldRotation(rotationFromAToB);
                            }
                    );
        }*/
    }


    //////////////function to show if not pointed at actual object!! (basically initialise object thickness and update if larger and then if smaller by more than a certain threshold its likely not pointing at the object....)
    //sprayBounds - global variable stored in settings
    //using intersectionx intersectiony and intersectionz
    public void warningForOutsideSprayBounds(){
        //content
        Camera arCamera = arCam.getArSceneView().getScene().getCamera();
        Ray ray = new Ray(arCamera.getWorldPosition(),arCamera.getForward());
        HitTestResult result = arCam.getArSceneView().getScene().hitTest(ray);
        //if(sprayBounds) {
            //if(triggerPress) {
                if (result.getNode() == null) {
                    //throw warning - need to fix, doesn't currently work
                    //toastMessage = Toast.makeText(this, "spray not pointed at object warning", Toast.LENGTH_SHORT);
                    //toastMessage.show();
                }
            //}
        //}
    }

    //region Timer
    public TextView tvTimer;
    public CountDownTimer timer;

    private void countDownStart() {
        if (scriptCounter == 12 || scriptCounter == 21 || scriptCounter == 31 || scriptCounter == 40) {
            tvTimer = findViewById(R.id.timer);
            timer = new CountDownTimer(15 * 60000, 1000) {
                public void onTick(long millisUntilFinished) {
                    tvTimer.setText(new SimpleDateFormat("mm:ss").format(new Date(millisUntilFinished)));
                    tvTimer.setTextSize(100);
                    tvTimer.setVisibility(View.VISIBLE);
                }

                public void onFinish() {
                    tvTimer.setText("Your material should be touch dry.\nPlease proceed to the next stage!");
                    tvTimer.setTextSize(30);
                }
            }.start();
        }
    }
    //endregion

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

    private String getStrSetting(String key) {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE);
        return preferences.getString(key, "");
    }

    private void saveStrSetting(String key, String value) {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
    //endregion

    //region BT Commands
    private void uvControl() {
        // UV On/Off
        uv = getBoolSetting("uv");
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
        laser = getBoolSetting("laser");
        if (laser) send("<LASER ON>");
        else send("<LASER OFF>");
    }

    public void tactileButtons() {
        if (toggleMode == 1) {
            if (Objects.equals(storedMessage, "fwd")) {
                if (scriptCounter < 51) {
                    scriptCounterLast = scriptCounter;
                    scriptCounter++;
                }
                implementScript();
            }
            if (Objects.equals(storedMessage, "bck")) {
                if (scriptCounter > 0) {
                    scriptCounterLast = scriptCounter;
                    scriptCounter--;
                }
                implementScript();
            }
        }
    }

    public void triggerControl() {
        if (toggleMode == 1) {
            if (Objects.equals(storedMessage, "pressed")) {
                triggerPress = true;
            }
            if (Objects.equals(storedMessage, "released")) {
                triggerPress = false;
            }
        }
    }
    //endregion

    //region BT Service
    private SerialService service;
    public CommsFragment.Connected connected;
    public String storedMessage;

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(this, SerialService.class);
        this.bindService(intent, this, Context.BIND_AUTO_CREATE);

        receiveHandler.postDelayed(receiveRunnable, 1);
    }

    @Override
    public void onPause() {
        super.onPause();
        receiveHandler.removeCallbacks(receiveRunnable);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        connected = CommsFragment.Connected.True;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    public void send(String str) {
        if (connected != CommsFragment.Connected.True) {
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

    public void receiveMSG() {
        storedMessage = getStrSetting("btMessage");
        if (storedMessage != null) {
            tactileButtons();
            triggerControl();
        }
        saveStrSetting("btMessage", "");
    }
    //endregion

    //region Constantly Run
    Runnable receiveRunnable = new Runnable() {
        @Override
        public void run() {
            receiveMSG();
            receiveHandler.postDelayed(receiveRunnable, 1);
        }
    };
    Handler receiveHandler = new Handler();
    //endregion
}

