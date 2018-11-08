package com.deitel.cannongame;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.widget.Toast;
import android.media.AudioManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

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
}
