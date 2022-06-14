package com.example.ar_airbrush_second;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
//import android.view.MotionEvent;
import android.widget.Toast;

import com.google.ar.core.Anchor;
//import com.google.ar.core.HitResult;
//import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
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

    private int clickNo = 0; //helps to render the 3d model only once when we tap the screen



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arview);

            //if (checkSystemSupport(this)) {
                //ArFragment is linked up with its respective id used in the activity_main.xml
                arCam = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arCameraArea);

                //To remove white "discover controller"
                arCam.getPlaneDiscoveryController().hide();
                arCam.getPlaneDiscoveryController().setInstructionView(null);

                arCam.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
                    clickNo++;
                    //the 3d model comes to the scene only when clickNo is one that means once
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

                    }

                });

            //} else {

            //    return;

            //}


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
