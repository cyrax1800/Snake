package com.project.snake;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Snake extends Activity {

    BoardCell[][] board = new BoardCell[Static.boardWidth][Static.boardHeight]; // contains all the board cells data
    ArrayDeque<Point> snakeBody; // tail is first and head is last
    int lastDirection; // last direction of the snake
    int nextDirection; // next direction the snake will move
    int mustAddedSnakeBody; // number of snake body must be added

    RelativeLayout layoutSnake;
    TextView tvScore;
    TextView tvHighScore;
    ImageView btnPause;

    // pause menu variables
    RelativeLayout layoutTransparent;
    ImageView btnResume;
    ImageView btnQuit;

    int score;
    int speed;

    MediaPlayer mpSnakeEat; // sound when snake eat
    MediaPlayer mpGameOver; // sound when game over

    Thread snakeTimer; // timer to move snake
    Thread skillTimer; // timer to make skill

    // wait and notify variables for pause
    final Object pauseLock = new Object();
    boolean isPaused = false;

    // Lightning variables
    boolean isLightningActive;
    double lightningTimeLeft;
    ImageView lightningIcon;
    Thread lightningTimer;

    // Times 2 variables
    boolean isTimes2Active;
    double times2TimeLeft;
    ImageView times2Icon;
    Thread times2Timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_snake);

        layoutSnake = (RelativeLayout) findViewById(R.id.layoutSnake);

        mpGameOver = MediaPlayer.create(Snake.this, R.raw.game_over);
        mpSnakeEat = MediaPlayer.create(Snake.this, R.raw.snake_eat);

        CreateView();

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PauseGame();
                layoutTransparent.setVisibility(View.VISIBLE);
            }
        });

        btnResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutTransparent.setVisibility(View.INVISIBLE);
                ResumeGame();
            }
        });

        btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snakeTimer.interrupt();
                skillTimer.interrupt();
                lightningTimer.interrupt();
                times2Timer.interrupt();
                finish();
            }
        });

        // event listener when screen orientation changed
        OrientationEventListener onScreenOrientationChanged = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                if ((orientation >= 315 || orientation < 45) && lastDirection != Static.enumDirectionDown) {
                    nextDirection = Static.enumDirectionUp;
                }
                else if (orientation >= 45 && orientation < 135 && lastDirection != Static.enumDirectionRight) {
                    nextDirection = Static.enumDirectionLeft;
                }
                else if (orientation >= 135 && orientation < 225 && lastDirection != Static.enumDirectionUp) {
                    nextDirection = Static.enumDirectionDown;
                }
                else if (orientation >= 225 && orientation < 315 && lastDirection != Static.enumDirectionLeft) {
                    nextDirection = Static.enumDirectionRight;
                }
            }
        };
        // attach OrientationEventListener to this activity
        if (onScreenOrientationChanged.canDetectOrientation()) {
            onScreenOrientationChanged.enable();
        }
        else {
            finish();
        }

        InitializeSnake();
    }

    @Override
    public void onBackPressed() {
        if (layoutTransparent.getVisibility() == View.INVISIBLE) {
            PauseGame();
            layoutTransparent.setVisibility(View.VISIBLE);
        }
        else {
            layoutTransparent.setVisibility(View.INVISIBLE);
            ResumeGame();
        }
    }

    public void CreateView() {
        // calculate snake size and border size
        int borderLeft = (int) ((Static.display.widthPixels * 22) / 360);
        int borderTop = (int) ((Static.display.heightPixels * 25) / 640);
        int snakeWidth = (int) ((Static.display.widthPixels - 2 * borderLeft) / Static.boardWidth);
        int snakeHeight = (int) ((Static.display.heightPixels - 2 * borderTop) / Static.boardHeight);

        // create text view score
        tvScore = new TextView(getApplicationContext());
        tvScore.setLayoutParams(new LinearLayout.LayoutParams(Static.display.widthPixels - 2 * borderLeft, borderTop));
        tvScore.setX(borderLeft);
        tvScore.setY(0);
        tvScore.setTextColor(getResources().getColor(R.color.textColor));
        tvScore.setTypeface(null, Typeface.BOLD);
        tvScore.setTextSize(TypedValue.COMPLEX_UNIT_PX, Static.display.heightPixels * 18 / 640);
        tvScore.setGravity(Gravity.CENTER_VERTICAL);
        layoutSnake.addView(tvScore);

        // create text view high score
        tvHighScore = new TextView(getApplicationContext());
        tvHighScore.setLayoutParams(new LinearLayout.LayoutParams(Static.display.widthPixels - 2 * borderLeft, borderTop));
        tvHighScore.setX(borderLeft);
        tvHighScore.setY(Static.display.heightPixels - borderTop);
        tvHighScore.setTextColor(getResources().getColor(R.color.textColor));
        tvHighScore.setTypeface(null, Typeface.BOLD);
        tvHighScore.setTextSize(TypedValue.COMPLEX_UNIT_PX, Static.display.heightPixels * 18 / 640);
        tvHighScore.setGravity(Gravity.CENTER_VERTICAL);
        layoutSnake.addView(tvHighScore);

        // create pause button
        int pauseMenuSize = Math.min(borderTop, borderLeft);
        btnPause = new ImageView(getApplicationContext());
        btnPause.setLayoutParams(new LinearLayout.LayoutParams(pauseMenuSize * 90 / 100, pauseMenuSize * 90 / 100));
        btnPause.setX(Static.display.widthPixels - borderLeft + (borderLeft - pauseMenuSize * 90 / 100) / 2);
        btnPause.setY((borderTop - pauseMenuSize * 90 / 100) / 2);
        btnPause.setScaleType(ImageView.ScaleType.FIT_CENTER);
        btnPause.setImageResource(R.drawable.btn_pause);
        layoutSnake.addView(btnPause);

        btnPause.setOnTouchListener(Static.btnPressAnimation);

        // create lightning icon
        lightningIcon = new ImageView(getApplicationContext());
        lightningIcon.setLayoutParams(new LinearLayout.LayoutParams(borderTop, borderTop));
        lightningIcon.setX(Static.display.widthPixels - borderLeft - borderTop);
        lightningIcon.setY(Static.display.heightPixels - borderTop);
        lightningIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        layoutSnake.addView(lightningIcon);
        SetLightning(false);

        // create times 2 icon
        times2Icon = new ImageView(getApplicationContext());
        times2Icon.setLayoutParams(new LinearLayout.LayoutParams(borderTop, borderTop));
        times2Icon.setX(Static.display.widthPixels - borderLeft - 2 * borderTop);
        times2Icon.setY(Static.display.heightPixels - borderTop);
        times2Icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        layoutSnake.addView(times2Icon);
        SetTimes2(false);

        // fit all board cell in center of screen
        borderLeft = (int) ((Static.display.widthPixels - snakeWidth * Static.boardWidth) / 2);
        borderTop = (int) ((Static.display.heightPixels - snakeHeight * Static.boardHeight) / 2);

        // initialize all boardImage and add it to layoutSnake
        for (int i = 0; i < Static.boardWidth; i++) {
            for (int j = 0; j < Static.boardHeight; j++) {
                ImageView ivBoardCell = new ImageView(Snake.this);
                ivBoardCell.setLayoutParams(new RelativeLayout.LayoutParams(snakeWidth, snakeHeight));
                ivBoardCell.setScaleType(ImageView.ScaleType.FIT_XY);
                ivBoardCell.setX(borderLeft + snakeWidth * i);
                ivBoardCell.setY(borderTop + snakeHeight * j);
                layoutSnake.addView(ivBoardCell);
                board[i][j] = new BoardCell(Static.enumBoardStatusEmpty, ivBoardCell);
            }
        }

        // create layout transparent
        layoutTransparent = new RelativeLayout(getApplicationContext());
        layoutTransparent.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layoutTransparent.setBackgroundColor(getResources().getColor(R.color.transparentColor));
        layoutTransparent.setVisibility(View.INVISIBLE);
        layoutSnake.addView(layoutTransparent);

        int menuHeight = Static.display.heightPixels * 70 * 20 / 100 / 100;
        int menuWidth = menuHeight * 2;
        int menuSpace = Static.display.heightPixels * 70 * 10 / 100 / 100;

        // create resume button
        btnResume = new ImageView(getApplicationContext());
        btnResume.setLayoutParams(new LinearLayout.LayoutParams(menuWidth, menuHeight));
        btnResume.setX((Static.display.widthPixels - menuWidth) / 2);
        btnResume.setY((Static.display.heightPixels - menuHeight * 2 - menuSpace) / 2);
        btnResume.setScaleType(ImageView.ScaleType.FIT_CENTER);
        btnResume.setImageResource(R.drawable.btn_resume);
        layoutTransparent.addView(btnResume);

        // create quit button
        btnQuit = new ImageView(getApplicationContext());
        btnQuit.setLayoutParams(new LinearLayout.LayoutParams(menuWidth, menuHeight));
        btnQuit.setX((Static.display.widthPixels - menuWidth) / 2);
        btnQuit.setY((Static.display.heightPixels - menuHeight * 2 - menuSpace) / 2 + menuHeight + menuSpace);
        btnQuit.setScaleType(ImageView.ScaleType.FIT_CENTER);
        btnQuit.setImageResource(R.drawable.btn_quit);
        layoutTransparent.addView(btnQuit);

        btnResume.setOnTouchListener(Static.btnPressAnimation);
        btnQuit.setOnTouchListener(Static.btnPressAnimation);
    }

    public void InitializeSnake() {
        // Clear all data and set boardStatus and boardImage to default value
        for (int i = 0; i <= Static.boardWidth; i++) {
            for (int j = 0; j <= Static.boardHeight; j++) {
                SetBoard(i, j, Static.enumBoardStatusEmpty);
            }
        }

        // set score to default (0)
        score = 0;
        UpdateScore(0);

        // set high score
        String highScore = "High Score : " + String.format(Locale.US, "%,d", ReadHighScoreValue()).replaceAll(",", " ");
        tvHighScore.setText(highScore);

        // reset the snake
        snakeBody = new ArrayDeque<Point>();
        mustAddedSnakeBody = 3;
        MoveSnakeBody(Static.boardWidth / 2, Static.boardHeight / 2 + 1);
        MoveSnakeBody(Static.boardWidth / 2, Static.boardHeight / 2);
        MoveSnakeBody(Static.boardWidth / 2, Static.boardHeight / 2 - 1);
        CreateItem(Static.enumBoardStatusApple);

        lastDirection = Static.enumDirectionUp;
        nextDirection = Static.enumDirectionUp;

        // make the timer for snake move
        snakeTimer = new Thread() {
            public void run() {
                while (!isInterrupted()) {
                    synchronized (pauseLock) {
                        while (isPaused) {
                            try {
                                pauseLock.wait();
                            }
                            catch (InterruptedException e) {}
                        }
                    }
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Boolean continueSnakeTimer = false;
                                Point head = snakeBody.getLast();
                                switch (nextDirection) {
                                    case Static.enumDirectionLeft:
                                        continueSnakeTimer = MoveSnake(head.x - 1, head.y);
                                        break;
                                    case Static.enumDirectionRight:
                                        continueSnakeTimer = MoveSnake(head.x + 1, head.y);
                                        break;
                                    case Static.enumDirectionUp:
                                        continueSnakeTimer = MoveSnake(head.x, head.y - 1);
                                        break;
                                    case Static.enumDirectionDown:
                                        continueSnakeTimer = MoveSnake(head.x, head.y + 1);
                                        break;
                                }
                                lastDirection = nextDirection;
                                // if snake hit border or its own body then interrupt the timer
                                if (!continueSnakeTimer) {
                                    GameOver();
                                }
                            }
                        });
                        Thread.sleep(GetSnakeSpeed());
                    }
                    catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };

        // make timer to add new skill and remove the old ones if exist
        skillTimer = new Thread() {
            public void run() {
                while (!isInterrupted()) {
                    synchronized (pauseLock) {
                        while (isPaused) {
                            try {
                                pauseLock.wait();
                            }
                            catch (InterruptedException e) {}
                        }
                    }
                    try {
                        // new skill will appear every 30s
                        Thread.sleep(30000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // make a random skill
                                int skill = (int) (Math.random() * 12345);
                                skill %= Static.numberOfSkill;
                                skill += Static.skillStartIndex;
                                // delete old skill if exist
                                for (int i = 0; i < Static.boardWidth; i++) {
                                    for (int j = 0; j < Static.boardHeight; j++) {
                                        if (board[i][j].getBoardStatus() >= Static.skillStartIndex) {
                                            SetBoard(i, j, Static.enumBoardStatusEmpty);
                                        }
                                    }
                                }
                                CreateItem(skill);
                            }
                        });
                    }
                    catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };

        // make timer for lightning skill
        lightningTimer = new Thread() {
            public void run() {
                while (!isInterrupted()) {
                    synchronized (pauseLock) {
                        while (isPaused) {
                            try {
                                pauseLock.wait();
                            }
                            catch (InterruptedException e) {}
                        }
                    }
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isLightningActive) {
                                    if (lightningTimeLeft >= 5) {
                                        lightningTimeLeft -= 0.5;
                                    }
                                    else if (lightningTimeLeft > 0) {
                                        lightningTimeLeft -= 0.5;
                                        if ((int) lightningIcon.getTag() == R.drawable.lightning) {
                                            lightningIcon.setImageResource(R.drawable.lightning_inactive);
                                            lightningIcon.setTag(R.drawable.lightning_inactive);
                                        }
                                        else {
                                            lightningIcon.setImageResource(R.drawable.lightning);
                                            lightningIcon.setTag(R.drawable.lightning);
                                        }
                                    }
                                    else {
                                        SetLightning(false);
                                    }
                                }
                            }
                        });
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };

        // make timer for times 2 skill
        times2Timer = new Thread() {
            public void run() {
                while (!isInterrupted()) {
                    synchronized (pauseLock) {
                        while (isPaused) {
                            try {
                                pauseLock.wait();
                            }
                            catch (InterruptedException e) {}
                        }
                    }
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isTimes2Active) {
                                    if (times2TimeLeft >= 5) {
                                        times2TimeLeft -= 0.5;
                                    }
                                    else if (times2TimeLeft > 0) {
                                        times2TimeLeft -= 0.5;
                                        if ((int) times2Icon.getTag() == R.drawable.times2) {
                                            times2Icon.setImageResource(R.drawable.times2_inactive);
                                            times2Icon.setTag(R.drawable.times2_inactive);
                                        }
                                        else {
                                            times2Icon.setImageResource(R.drawable.times2);
                                            times2Icon.setTag(R.drawable.times2);
                                        }
                                    }
                                    else {
                                        SetTimes2(false);
                                    }
                                }
                            }
                        });
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };

        snakeTimer.start();
        skillTimer.start();
        lightningTimer.start();
        times2Timer.start();
    }

    public void SetLightning(Boolean active) {
        if (active) {
            lightningTimeLeft = 15;
            lightningIcon.setImageResource(R.drawable.lightning);
            lightningIcon.setTag(R.drawable.lightning);
            isLightningActive = true;
        }
        else {
            isLightningActive = false;
            lightningIcon.setImageResource(R.drawable.lightning_inactive);
            lightningIcon.setTag(R.drawable.lightning_inactive);
        }
    }

    public void SetTimes2(Boolean active) {
        if (active) {
            times2TimeLeft = 15;
            times2Icon.setImageResource(R.drawable.times2);
            times2Icon.setTag(R.drawable.times2);
            isTimes2Active = true;
        }
        else {
            isTimes2Active = false;
            times2Icon.setImageResource(R.drawable.times2_inactive);
            times2Icon.setTag(R.drawable.times2_inactive);
        }
    }

    public boolean SetBoard(int x, int y, int enumBoardStatus) {
        // set the boardImage and boardStatus to enumBoardStatus
        // return true if success and false otherwise
        if (x >= 0 && x < Static.boardWidth && y >= 0 && y < Static.boardHeight)
        {
            board[x][y].setBoardStatus(enumBoardStatus);
            switch (enumBoardStatus)
            {
                case Static.enumBoardStatusHead:
                    // rotate head image suit the direction
                    Matrix headMatrix = new Matrix();
                    Bitmap headBeforeRotate = BitmapFactory.decodeResource(getResources(), R.drawable.snake_head);
                    headMatrix.postRotate(((nextDirection%180 == 0 ? 0 : 180) + nextDirection) % 360);
                    Bitmap headAfterRotate = Bitmap.createBitmap(headBeforeRotate, 0, 0, headBeforeRotate.getWidth(), headBeforeRotate.getHeight(), headMatrix, true);
                    board[x][y].setBoardImage(headAfterRotate);
                    break;
                case Static.enumBoardStatusBody:
                    board[x][y].setBoardImage(R.drawable.snake_body);
                    break;
                case Static.enumBoardStatusApple:
                    board[x][y].setBoardImage(R.drawable.apple);
                    break;
                case Static.enumBoardStatusLighting:
                    board[x][y].setBoardImage(R.drawable.lightning);
                    break;
                case Static.enumBoardStatusShortenPotion:
                    board[x][y].setBoardImage(R.drawable.shorten_potion);
                    break;
                case Static.enumBoardStatusTimes2:
                    board[x][y].setBoardImage(R.drawable.times2);
                    break;
                default:
                    board[x][y].setBoardImage(0);
                    break;
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean SetBoard(Point location,  int enumBoardStatus) {
        return SetBoard(location.x, location.y, enumBoardStatus);
    }

    public boolean MoveSnake(int destX, int destY) {
        // move the snake to nextDirection and act according to the content of the destination
        // if content of the destination is apple then add new snake body, update score and create new apple
        // else if content of the destination is empty then move snake to next direction
        // else means that the snake hit it self so game over
        if (destX >= 0 && destX < Static.boardWidth && destY >= 0 && destY < Static.boardHeight)
        {
            if (board[destX][destY].getBoardStatus() == Static.enumBoardStatusApple)
            {
                mustAddedSnakeBody += 1;
                speed += 1;
                if(ReadSoundValue()) mpSnakeEat.start();
                MoveSnakeBody(destX, destY);
                UpdateScore(10);
                CreateItem(Static.enumBoardStatusApple);
                return true;
            }
            else if (board[destX][destY].getBoardStatus() == Static.enumBoardStatusEmpty)
            {
                MoveSnakeBody(destX, destY);
                return true;
            }
            else if (board[destX][destY].getBoardStatus() == Static.enumBoardStatusLighting) {
                if(ReadSoundValue()) mpSnakeEat.start();
                SetLightning(true);
                MoveSnakeBody(destX, destY);
                return true;
            }
            else if (board[destX][destY].getBoardStatus() == Static.enumBoardStatusShortenPotion) {
                if(ReadSoundValue()) mpSnakeEat.start();
                // cut snake body to half
                int snakeLength = (int) Math.floor((double) snakeBody.size() / 2.0);
                for (int i = 1; i <= snakeLength; i++) {
                    SetBoard(snakeBody.getFirst(), Static.enumBoardStatusEmpty);
                    snakeBody.pop();
                }
                MoveSnakeBody(destX, destY);
                return true;
            }
            else if (board[destX][destY].getBoardStatus() == Static.enumBoardStatusTimes2) {
                if(ReadSoundValue()) mpSnakeEat.start();
                SetTimes2(true);
                MoveSnakeBody(destX, destY);
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public void MoveSnakeBody(int headX, int headY) {
        // if snakeBody didn't empty then change the head to body
        if (!snakeBody.isEmpty()) {
            SetBoard(snakeBody.getLast(), Static.enumBoardStatusBody);
        }
        // add new head to snakeBody last and set it to boardImage
        snakeBody.addLast(new Point(headX, headY));
        SetBoard(snakeBody.getLast(), Static.enumBoardStatusHead);
        // if increment body is zero and the snake contain at least one body then remove the tail
        if (mustAddedSnakeBody <= 0)
        {
            if (snakeBody.size() != 1) {
                SetBoard(snakeBody.getFirst(), Static.enumBoardStatusEmpty);
                snakeBody.pop();
            }
        }
        else {
            mustAddedSnakeBody--;
        }
    }

    public int GetSnakeSpeed() {
        double snakeSpeed = 500;
        for (int i = 1; i <= speed; i++) {
            snakeSpeed = snakeSpeed * 70 / 100;
        }
        int finalSpeed = Math.max(50, (int) Math.ceil(snakeSpeed));
        if (isLightningActive) finalSpeed /= 2;
        return finalSpeed;
    };

    public void UpdateScore(int value) {
        if (isTimes2Active) value *= 2;
        score += value;
        String scoreText = "Score : " + String.format(Locale.US, "%,d", score);
        tvScore.setText(scoreText);
    }

    public boolean CreateItem(int enumBoardStatus) {
        // make a list of possible location for item, then shuffle the list
        // try each possibility to create new item
        List<Point> possibilityLocation = new ArrayList<Point>();
        for (int i = 0; i < Static.boardWidth; i++) {
            for (int j = 0; j < Static.boardHeight; j++) {
                if (board[i][j].getBoardStatus() == Static.enumBoardStatusEmpty) {
                    possibilityLocation.add(new Point(i, j));
                }
            }
        }
        Collections.shuffle(possibilityLocation);
        if (!possibilityLocation.isEmpty()) {
            Point point = possibilityLocation.get(0);
            SetBoard(point.x, point.y, enumBoardStatus);
            return true;
        }
        else {
            return false;
        }
    }

    public void GameOver() {
        if (ReadSoundValue()) mpGameOver.start();
        snakeTimer.interrupt();
        skillTimer.interrupt();
        lightningTimer.interrupt();
        times2Timer.interrupt();
        // game over alert
        boolean newHighScore = (score > ReadHighScoreValue());
        SaveHighScoreValue(Math.max(score, ReadHighScoreValue()));
        new AlertDialog.Builder(Snake.this).setTitle("Snake").setMessage((newHighScore ? "NEW HIGH SCORE !!" : "GAME OVER !!") + "\nYour score : " + score)
                .setPositiveButton("Play again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        recreate();
                    }
                })
                .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false).show();
    }

    public void PauseGame() {
        synchronized (pauseLock) {
            isPaused = true;
        }
    }

    public void ResumeGame() {
        synchronized (pauseLock) {
            isPaused = false;
            pauseLock.notifyAll();
            pauseLock.notify();
        }
    }

    public void SaveHighScoreValue(int value) {
        SharedPreferences setting = getSharedPreferences(getString(R.string.settingFileName), MODE_PRIVATE);
        SharedPreferences.Editor editor = setting.edit();
        editor.putInt(getString(R.string.highScore), value);
        editor.apply();
    }

    public int ReadHighScoreValue() {
        SharedPreferences setting = getSharedPreferences(getString(R.string.settingFileName), MODE_PRIVATE);
        return setting.getInt(getString(R.string.highScore), 0);
    }

    public boolean ReadSoundValue() {
        SharedPreferences setting = getSharedPreferences(getString(R.string.settingFileName), MODE_PRIVATE);
        return setting.getBoolean(getString(R.string.settingSound), true);
    }
}
