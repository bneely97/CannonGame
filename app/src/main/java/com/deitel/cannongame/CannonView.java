package com.deitel.cannongame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
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
        implements SurfaceHolder.Callback
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

        getHolder().addCallback(this);

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
        if (cannonThread != null)
            cannonThread.setRunning(false);
    }

    public void releaseResources() {
        soundPool.release(); // release all resources used by the SoundPool
        soundPool = null;
    }

    public void fireCannonball(MotionEvent event) {
        if (cannonballOnScreen) // if a cannonball is already on the screen
            return; // do nothing

        double angle = alignCannon(event); // get the cannon barrel's angle

        // move the cannonball to be inside the cannon
        cannonball.x = cannonballRadius; // align x-coordinate with cannon
        cannonball.y = screenHeight / 2; // centers ball vertically

        // get the x component of the total velocity
        cannonballVelocityX = (int) (cannonballSpeed * Math.sin(angle));

        // get the y component of the total velocity
        cannonballVelocityY = (int) (-cannonballSpeed * Math.cos(angle));
        cannonballOnScreen = true; // the cannonball is on the screen
        ++shotsFired; // increment shotsFired

        // play cannon fired sound
        soundPool.play(soundMap.get(CANNON_SOUND_ID), 1, 1, 1, 0, 1f);
    }

    public double alignCannon(MotionEvent event) {
        // get the location of the touch in this view
        Point touchPoint = new Point((int) event.getX(), (int) event.getY());

        // compute the touch's distance from center of the screen
        // on the y-axis
        double centerMinusY = (screenHeight / 2 - touchPoint.y);

        double angle = 0; // initialize angle to 0

        // calculate the angle the barrel makes with the horizontal
        if (centerMinusY != 0) // prevent division by 0
            angle = Math.atan((double) touchPoint.x / centerMinusY);

        // if the touch is on the lower half of the screen
        if (touchPoint.y > screenHeight / 2)
            angle += Math.PI; // adjust the angle

        // calculate the endpoint of the cannon barrel
        barrelEnd.x = (int) (cannonLength * Math.sin(angle));
        barrelEnd.y =
                (int) (-cannonLength * Math.cos(angle) + screenHeight / 2);

        return angle; // return the computed angle
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

    // called repeatedly by the CannonThread to update game elements
    private void updatePositions(double elapsedTimeMS)
    {
        double interval = elapsedTimeMS / 1000.0; // convert to seconds

        if (cannonballOnScreen) // if there is currently a shot fired
        {
            // update cannonball position
            cannonball.x += interval * cannonballVelocityX;
            cannonball.y += interval * cannonballVelocityY;

            // check for collision with blocker
            if (cannonball.x + cannonballRadius > blockerDistance &&
                    cannonball.x - cannonballRadius < blockerDistance &&
                    cannonball.y + cannonballRadius > blocker.start.y &&
                    cannonball.y - cannonballRadius < blocker.end.y)
            {
                cannonballVelocityX *= -1; // reverse cannonball's direction
                timeLeft -= MISS_PENALTY; // penalize the user

                // play blocker sound
                soundPool.play(soundMap.get(BLOCKER_SOUND_ID), 1, 1, 1, 0, 1f);
            } // end if

            // check for collisions with left and right walls
            else if (cannonball.x + cannonballRadius > screenWidth ||
                    cannonball.x - cannonballRadius < 0)
                cannonballOnScreen = false; // remove cannonball from screen

                // check for collisions with top and bottom walls
            else if (cannonball.y + cannonballRadius > screenHeight ||
                    cannonball.y - cannonballRadius < 0)
                cannonballOnScreen = false; // make the cannonball disappear

                // check for cannonball collision with target
            else if (cannonball.x + cannonballRadius > targetDistance &&
                    cannonball.x - cannonballRadius < targetDistance &&
                    cannonball.y + cannonballRadius > target.start.y &&
                    cannonball.y - cannonballRadius < target.end.y)
            {
                // determine target section number (0 is the top)
                int section =
                        (int) ((cannonball.y - target.start.y) / pieceLength);

                // check if the piece hasn't been hit yet
                if ((section >= 0 && section < TARGET_PIECES) &&
                        !hitStates[section])
                {
                    hitStates[section] = true; // section was hit
                    cannonballOnScreen = false; // remove cannonball
                    timeLeft += HIT_REWARD; // add reward to remaining time

                    // play target hit sound
                    soundPool.play(soundMap.get(TARGET_SOUND_ID), 1,
                            1, 1, 0, 1f);

                    // if all pieces have been hit
                    if (++targetPiecesHit == TARGET_PIECES)
                    {
                        cannonThread.setRunning(false);
                        showGameOverDialog(R.string.win); // show winning dialog
                        gameOver = true; // the game is over
                    } // end if
                } // end if
            } // end else if
        } // end if

        // update the blocker's position
        double blockerUpdate = interval * blockerVelocity;
        blocker.start.y += blockerUpdate;
        blocker.end.y += blockerUpdate;

        // update the target's position
        double targetUpdate = interval * targetVelocity;
        target.start.y += targetUpdate;
        target.end.y += targetUpdate;

        // if the blocker hit the top or bottom, reverse direction
        if (blocker.start.y < 0 || blocker.end.y > screenHeight)
            blockerVelocity *= -1;

        // if the target hit the top or bottom, reverse direction
        if (target.start.y < 0 || target.end.y > screenHeight)
            targetVelocity *= -1;

        timeLeft -= interval; // subtract from time left

        // if the timer reached zero
        if (timeLeft <= 0.0)
        {
            timeLeft = 0.0;
            gameOver = true; // the game is over
            cannonThread.setRunning(false);
            showGameOverDialog(R.string.lose); // show the losing dialog
        } // end if
    }

    public void showGameOverDialog (int messageId) {
        // create a dialog displaying the given String
        final AlertDialog.Builder dialogBuilder =
                new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(getResources().getString(messageId));
        dialogBuilder.setCancelable(false);

        // display number of shots fired and total time elapsed
        dialogBuilder.setMessage(getResources().getString(
                R.string.result_format, shotsFired, totalTimeElapsed));
        dialogBuilder.setPositiveButton(R.string.reset_game,
                new DialogInterface.OnClickListener()
                {
                    // called when "Reset Game" Button is pressed
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialogIsDisplayed = false;
                        newGame(); // set up and start a new game
                    } // end method onClick
                } // end anonymous inner class
        ); // end call to setPositiveButton

        activity.runOnUiThread(
                new Runnable() {
                    public void run()
                    {
                        dialogIsDisplayed = true;
                        dialogBuilder.show(); // display the dialog
                    } // end method run
                } // end Runnable
        ); // end call to runOnUiThread
    }

    private void drawGameElements(Canvas canvas) {
        // clear the background
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(),
                backgroundPaint);

        // display time remaining
        canvas.drawText(getResources().getString(
                R.string.time_remaining_format, timeLeft), 30, 50, textPaint);

        // if a cannonball is currently on the screen, draw it
        if (cannonballOnScreen)
            canvas.drawCircle(cannonball.x, cannonball.y, cannonballRadius,
                    cannonballPaint);

        // draw the cannon barrel
        canvas.drawLine(0, screenHeight / 2, barrelEnd.x, barrelEnd.y,
                cannonPaint);

        // draw the cannon base
        canvas.drawCircle(0, (int) screenHeight / 2,
                (int) cannonBaseRadius, cannonPaint);

        // draw the blocker
        canvas.drawLine(blocker.start.x, blocker.start.y, blocker.end.x,
                blocker.end.y, blockerPaint);

        Point currentPoint = new Point(); // start of current target section

        // initialize curPoint to the starting point of the target
        currentPoint.x = target.start.x;
        currentPoint.y = target.start.y;

        // draw the target
        for (int i = 1; i <= TARGET_PIECES; ++i)
        {
            // if this target piece is not hit, draw it
            if (!hitStates[i - 1])
            {
                // alternate coloring the pieces yellow and blue
                if (i % 2 == 0)
                    targetPaint.setColor(Color.YELLOW);
                else
                    targetPaint.setColor(Color.BLUE);

                canvas.drawLine(currentPoint.x, currentPoint.y, target.end.x,
                        (int) (currentPoint.y + pieceLength), targetPaint);
            } // end if

            // move curPoint to the start of the next piece
            currentPoint.y += pieceLength;
        } // end for
    } // end method drawGameElements

    // called when surface changes size
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format,
                               int width, int height)
    {
    } // end method surfaceChanged

    // called when surface is first created
    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        if (!dialogIsDisplayed)
        {
            cannonThread = new CannonThread(holder);
            cannonThread.setRunning(true);
            cannonThread.start(); // start the game loop thread
        } // end if
    } // end method surfaceCreated

    // called when the surface is destroyed
    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        // ensure that thread terminates properly
        boolean retry = true;
        cannonThread.setRunning(false);

        while (retry)
        {
            try
            {
                cannonThread.join();
                retry = false;
            } // end try
            catch (InterruptedException e)
            {
            } // end catch
        } // end while
    } // end method surfaceDestroyed

    private class CannonThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private boolean threadIsRunning = true;

        public void setRunning(boolean running) {
            threadIsRunning=running;
        }

        public CannonThread(SurfaceHolder holder) {
            surfaceHolder=holder;
            setName("CannonThread");
        }

        @Override
        public void run() {
            Canvas canvas=null;
            long previousFrameTime = System.currentTimeMillis();

            while (threadIsRunning) {
                try {
                    canvas=surfaceHolder.lockCanvas(null);
                    synchronized (surfaceHolder) {
                        long currentTime = System.currentTimeMillis();
                        double elapsedTimeMS = currentTime - previousFrameTime;
                        totalTimeElapsed += elapsedTimeMS / 1000.0;
                        updatePositions(elapsedTimeMS);
                        drawGameElements(canvas);
                        previousFrameTime=currentTime;
                    }
                }
                finally {
                    if (canvas!=null) surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

} // end CannonView
