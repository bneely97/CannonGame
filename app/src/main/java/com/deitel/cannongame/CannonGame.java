package com.deitel.cannongame;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CannonGame extends Activity {

    private GestureDetector gestureDector;
    private CannonView cannonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //call super's onCreate method
        setContentView(R.layout.main); //inflate the layout

        //get the CannonView
        cannonView = (CannonView) findViewById(R.id.cannonView);

        //initialize the GestureDetector
        //gestureDector = new GestureDetector(this,gestureListener);

        //allow volume keys to set game volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //end method onCreate
    }

    //pause app when pushed to background
    @Override
    public void onPause() {
        super.onPause(); //call the super method
        cannonView.stopGame(); //terminates the game
    }

    //release resources
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cannonView.releaseResources();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            cannonView.alignCannon(event);
        }
        return gestureDector.onTouchEvent(event);
    }

    SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            cannonView.fireCannonball(e);
            return true;
        }
    };
}
