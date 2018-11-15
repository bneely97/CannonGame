package com.deitel.cannongame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.util.HashMap;
import java.util.Map;

public class CannonView extends SurfaceView
        //implements SurfaceHolder.Callback
{
    private CannonThread cannonThread;
    private Activity activity;
    private boolean dialogIsDisplayed = false;

    // constants for gameplay
    private static final int TARGET_PIECES = 7; //sections in the target
    private static final int MISS_PENALTY = 2; // seconds deducted on a miss
    private static final int HIT_REWARD = 3; //seconds added on a hit

    // variables for the game loop and tacking stats
    private boolean gameOver;
    private double timeLeft;
    private int shotsFired;
    private double totalTimeElapsed;

    // variables for the blocker and target
    private Line blocker; //start and end points
    private int blockerDistance; //distance from the left
    private int blockerBeginning; //distance from top
    private int blockerEnd; //bottom edge distance from top
    private int initialBlockerVelocity; //initial speed multiplier
    private float blockerVelocity; //speed multiplier during game
    private Line target; //start and end points
    private int targetDistance; //distance from the left
    private int targetBeginning; //distance from top
    private double pieceLength; //length of target piece
    private int targetEnd; //bottom edge distance from top
    private int initialTargetVelocity; //initial speed multiplier
    private float targetVelocity; //speed multiplier during game

    private int lineWidth;
    private boolean[] hitStates;
    private int targetPiecesHit;

    // variables for the cannon and cannonball
    private Point cannonball;
    private int cannonballVelocityX;
    private int cannonballVelocityY;
    private boolean cannonballOnScreen;
    private int cannonballRadius;
    private int cannonballSpeed;
    private int cannonBaseRadius;
    private int cannonLength;
    private Point barrelEnd;
    private int screenWidth;
    private int screenHeight;

    // constants and variables for managing sounds
    private static final int TARGET_SOUND_ID = 0;
    private static final int CANNON_SOUND_ID = 1;
    private static final int BLOCKER_SOUND_ID = 2;
    private SoundPool soundPool;
    private Map<Integer, Integer> soundMap;

    // Paint variables used when drawing each item on the screen
    private Paint textPaint;
    private Paint cannonballPaint;
    private Paint cannonPaint;
    private Paint blockerPaint;
    private Paint targetPaint;
    private Paint backgroundPaint;

    public CannonView(Context context, AttributeSet attrs) {
        super(context,attrs);
        activity = (Activity) context;

        blocker = new Line();
        target = new Line();
        cannonball = new Point();

        hitStates = new boolean[TARGET_PIECES];

        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        soundMap = new HashMap<Integer, Integer>();
        soundMap.put(TARGET_SOUND_ID, soundPool.load(context, R.raw.target_hit, 1));
        soundMap.put(CANNON_SOUND_ID, soundPool.load(context, R.raw.cannon_fire, 1));
        soundMap.put(BLOCKER_SOUND_ID, soundPool.load(context, R.raw.blocker_hit, 1));

        textPaint = new Paint();
        cannonPaint = new Paint();
        cannonballPaint = new Paint();
        blockerPaint = new Paint();
        targetPaint = new Paint();
        backgroundPaint = new Paint();
    }

    public void stopGame() {

    }

    public void releaseResources() {

    }

    public void fireCannonball(MotionEvent e) {

    }

    public double alignCannon(MotionEvent event) {
        double angle = 0;

        return angle;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged( w, h, oldw, oldh);
        screenWidth=w;
        screenHeight=h;
        cannonBaseRadius = h / 18;
        cannonLength = w / 8;
        cannonballRadius = w / 38;
        cannonballSpeed = w * 3 / 2;
        lineWidth = w / 24;
        blockerDistance = w*5/8; blockerBeginning = h/8; blockerEnd = h*3/8;
        targetDistance = w*7/8; targetBeginning = h/8; targetEnd = h*7/8;
        pieceLength = (targetEnd-targetBeginning) / TARGET_PIECES;
        initialTargetVelocity = -h/4;
        target.start = new Point(targetDistance, targetBeginning);
        target.end = new Point(targetDistance, targetEnd);
        barrelEnd = new Point(cannonLength, h/2);
        textPaint.setTextSize(w / 20);
        textPaint.setAntiAlias(true);
        cannonPaint.setStrokeWidth(lineWidth*1.5f);
        blockerPaint.setStrokeWidth(lineWidth);
        targetPaint.setStrokeWidth(lineWidth);
        backgroundPaint.setColor(Color.WHITE);

        newGame();
    }

    public void newGame() {
        for (int i=0;i<TARGET_PIECES;i++) hitStates[i]=false;

        targetPiecesHit=0;
        blockerVelocity=initialBlockerVelocity;
        targetVelocity=initialTargetVelocity;
        timeLeft=10;
        cannonballOnScreen = false;
        shotsFired=0;
        totalTimeElapsed=0.0;
        blocker.start.set(blockerDistance, blockerBeginning);
        blocker.end.set(blockerDistance, blockerEnd);
        target.start.set(targetDistance, targetDistance);
        target.end.set(targetDistance, targetEnd);

        if (gameOver) {
            gameOver = false;
            cannonThread = new CannonThread(getHolder());
            cannonThread.start();
        }
    }
}
