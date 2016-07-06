package com.project.snake;

import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class Static {

    // enumeration board status
    public static final int enumBoardStatusEmpty = 0;
    public static final int enumBoardStatusHead = 1;
    public static final int enumBoardStatusBody = 2;
    public static final int enumBoardStatusApple = 3;
    public static final int enumBoardStatusLighting = 4;
    public static final int enumBoardStatusShortenPotion = 5;
    public static final int enumBoardStatusTimes2 = 6;

    public static final int skillStartIndex = 4;
    public static final int numberOfSkill = 3;


    // enumeration direction
    public static final int enumDirectionUp = 0;
    public static final int enumDirectionRight = 270;
    public static final int enumDirectionDown = 180;
    public static final int enumDirectionLeft = 90;


    // board width and height
    public static final int boardWidth = 16;
    public static final int boardHeight = 30;

    // dimension of screen in pixel
    public static DisplayMetrics display = new DisplayMetrics();

    // shrink control when touch down and normalize it when touch up
    public static final View.OnTouchListener btnPressAnimation = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setScaleX((float)0.9);
                v.setScaleY((float)0.9);
            }
            else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.setScaleX((float)1);
                v.setScaleY((float)1);
            }
            return false;
        }
    };

    // set view and all it's children enabled
    public static void SetViewEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                SetViewEnabled(child, enabled);
            }
        }
    }
}