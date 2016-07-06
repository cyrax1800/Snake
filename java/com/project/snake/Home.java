package com.project.snake;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class Home extends Activity {

    RelativeLayout layoutHome;

    // main menu variables
    RelativeLayout layoutMenu;
    ImageView snakeLogo;
    ImageView btnPlay;
    ImageView btnSetting;
    ImageView btnCredits;
    ImageView btnExit;

    // pop up menu variables
    RelativeLayout layoutTransparent;
    RelativeLayout layoutPopUp;
    ImageView btnClose;

    // credits layout
    RelativeLayout layoutCredits;

    // setting menu variables
    RelativeLayout layoutSetting;
    ImageView settingLogo;
    ImageView settingSound;
    ImageView settingMusic;
    ImageView valueSound;
    ImageView valueMusic;

    MediaPlayer mpBackgroundMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_home);

        // get device display dimension
        getWindowManager().getDefaultDisplay().getMetrics(Static.display);

        layoutHome = (RelativeLayout) findViewById(R.id.layoutHome);
        layoutMenu = (RelativeLayout) findViewById(R.id.layoutMenu);
        CreateView();

        mpBackgroundMusic = MediaPlayer.create(Home.this, R.raw.background_music);
        mpBackgroundMusic.setLooping(true);
        mpBackgroundMusic.start();
        if (!ReadMusicValue()) mpBackgroundMusic.pause();

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent snakeIntent = new Intent(Home.this, Snake.class);
                startActivity(snakeIntent);
            }
        });

        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Static.SetViewEnabled(layoutMenu, false);
                ShowSetting();
            }
        });

        btnCredits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Static.SetViewEnabled(layoutMenu, false);
                ShowCredits();
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mpBackgroundMusic.isPlaying()) mpBackgroundMusic.stop();
                mpBackgroundMusic.release();
                finish();
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutTransparent.setVisibility(View.INVISIBLE);
                Static.SetViewEnabled(layoutMenu, true);
            }
        });

        valueSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (valueSound.getTag().equals(true)) {
                    SetValue(valueSound, false);
                    SaveSoundValue(false);
                }
                else {
                    SetValue(valueSound, true);
                    SaveSoundValue(true);
                }
            }
        });

        valueMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (valueMusic.getTag().equals(true)) {
                    mpBackgroundMusic.pause();
                    SetValue(valueMusic, false);
                    SaveMusicValue(false);
                }
                else {
                    mpBackgroundMusic.start();
                    SetValue(valueMusic, true);
                    SaveMusicValue(true);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (layoutTransparent.getVisibility() == View.INVISIBLE) {
            if (mpBackgroundMusic.isPlaying()) mpBackgroundMusic.stop();
            mpBackgroundMusic.release();
            finish();
        }
        else {
            layoutTransparent.setVisibility(View.INVISIBLE);
            Static.SetViewEnabled(layoutMenu, true);
        }
    }

    public void CreateView() {
        // create snake logo
        snakeLogo = new ImageView(getApplicationContext());
        snakeLogo.setLayoutParams(new LinearLayout.LayoutParams(Static.display.widthPixels * 80 / 100, Static.display.heightPixels * 30 * 75 / 100 / 100));
        snakeLogo.setX(Static.display.widthPixels * 10 / 100);
        snakeLogo.setY(Static.display.heightPixels * 30 * 125 / 100 / 1000);
        snakeLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        snakeLogo.setImageResource(R.drawable.snake_logo);
        layoutMenu.addView(snakeLogo);

        int menuHeight = Static.display.heightPixels * 70 * 20 / 100 / 100;
        int menuWidth = menuHeight * 2;
        int menuSpace = Static.display.heightPixels * 70 * 5 / 100 / 100;

        // create play button
        btnPlay = new ImageView(getApplicationContext());
        btnPlay.setLayoutParams(new LinearLayout.LayoutParams(menuWidth, menuHeight));
        btnPlay.setX((Static.display.widthPixels - menuWidth) / 2);
        btnPlay.setY(Static.display.heightPixels * 30 / 100);
        btnPlay.setScaleType(ImageView.ScaleType.FIT_CENTER);
        btnPlay.setImageResource(R.drawable.btn_play);
        layoutMenu.addView(btnPlay);

        // create setting button
        btnSetting = new ImageView(getApplicationContext());
        btnSetting.setLayoutParams(new LinearLayout.LayoutParams(menuWidth, menuHeight));
        btnSetting.setX((Static.display.widthPixels - menuWidth) / 2);
        btnSetting.setY(Static.display.heightPixels * 30 / 100 + (menuHeight + menuSpace));
        btnSetting.setScaleType(ImageView.ScaleType.FIT_CENTER);
        btnSetting.setImageResource(R.drawable.btn_setting);
        layoutMenu.addView(btnSetting);

        // create credits button
        btnCredits = new ImageView(getApplicationContext());
        btnCredits.setLayoutParams(new LinearLayout.LayoutParams(menuWidth, menuHeight));
        btnCredits.setX((Static.display.widthPixels - menuWidth) / 2);
        btnCredits.setY(Static.display.heightPixels * 30 / 100 + 2 * (menuHeight + menuSpace));
        btnCredits.setScaleType(ImageView.ScaleType.FIT_CENTER);
        btnCredits.setImageResource(R.drawable.btn_credits);
        layoutMenu.addView(btnCredits);

        // create exit button
        btnExit = new ImageView(getApplicationContext());
        btnExit.setLayoutParams(new LinearLayout.LayoutParams(menuWidth, menuHeight));
        btnExit.setX((Static.display.widthPixels - menuWidth) / 2);
        btnExit.setY(Static.display.heightPixels * 30 / 100 + 3 * (menuHeight + menuSpace));
        btnExit.setScaleType(ImageView.ScaleType.FIT_CENTER);
        btnExit.setImageResource(R.drawable.btn_exit);
        layoutMenu.addView(btnExit);

        btnPlay.setOnTouchListener(Static.btnPressAnimation);
        btnSetting.setOnTouchListener(Static.btnPressAnimation);
        btnCredits.setOnTouchListener(Static.btnPressAnimation);
        btnExit.setOnTouchListener(Static.btnPressAnimation);

        // create layout transparent
        layoutTransparent = new RelativeLayout(getApplicationContext());
        layoutTransparent.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layoutTransparent.setBackgroundColor(getResources().getColor(R.color.transparentColor));
        layoutTransparent.setVisibility(View.INVISIBLE);
        layoutHome.addView(layoutTransparent);

        int popUpSize = Math.min(Static.display.widthPixels, Static.display.heightPixels) * 90 / 100;

        // create pop up layout
        layoutPopUp = new RelativeLayout(getApplicationContext());
        layoutPopUp.setLayoutParams(new RelativeLayout.LayoutParams(popUpSize, popUpSize));
        layoutPopUp.setX((Static.display.widthPixels - popUpSize) / 2);
        layoutPopUp.setY((Static.display.heightPixels - popUpSize) / 2);
        layoutPopUp.setBackgroundResource(R.drawable.pop_up_background);
        layoutTransparent.addView(layoutPopUp);

        // create pop up close button
        btnClose = new ImageView(getApplicationContext());
        btnClose.setLayoutParams(new LinearLayout.LayoutParams(popUpSize / 10, popUpSize / 10));
        btnClose.setX((Static.display.widthPixels + popUpSize) / 2 - (popUpSize * 16 / 100));
        btnClose.setY((Static.display.heightPixels - popUpSize) / 2 + (popUpSize * 6 / 100));
        btnClose.setScaleType(ImageView.ScaleType.FIT_CENTER);
        btnClose.setImageResource(R.drawable.btn_close);
        layoutTransparent.addView(btnClose);

        btnClose.setOnTouchListener(Static.btnPressAnimation);

        // create credits layout
        layoutCredits = new RelativeLayout(getApplicationContext());
        layoutCredits.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layoutCredits.setBackgroundResource(R.drawable.credits);
        layoutPopUp.addView(layoutCredits);

        // create setting layout
        layoutSetting = new RelativeLayout(getApplicationContext());
        layoutSetting.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layoutPopUp.addView(layoutSetting);

        popUpSize /= 10;

        // create setting logo
        settingLogo = new ImageView(getApplicationContext());
        settingLogo.setLayoutParams(new LinearLayout.LayoutParams(popUpSize * 4, popUpSize));
        settingLogo.setX(popUpSize * 3);
        settingLogo.setY(popUpSize * 3 / 2);
        settingLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        settingLogo.setImageResource(R.drawable.setting_logo);
        layoutSetting.addView(settingLogo);

        // create sound setting
        settingSound = new ImageView(getApplicationContext());
        settingSound.setLayoutParams(new LinearLayout.LayoutParams(popUpSize * 4, popUpSize));
        settingSound.setX(popUpSize * 2);
        settingSound.setY(popUpSize * 4);
        settingSound.setScaleType(ImageView.ScaleType.FIT_START);
        settingSound.setImageResource(R.drawable.setting_sound);
        layoutSetting.addView(settingSound);

        // create sound value
        valueSound = new ImageView(getApplicationContext());
        valueSound.setLayoutParams(new RelativeLayout.LayoutParams(popUpSize * 2, popUpSize));
        valueSound.setX(popUpSize * 6);
        valueSound.setY(popUpSize * 4);
        valueSound.setScaleType(ImageView.ScaleType.FIT_START);
        SetValue(valueSound, ReadSoundValue());
        layoutSetting.addView(valueSound);

        // create music setting
        settingMusic = new ImageView(getApplicationContext());
        settingMusic.setLayoutParams(new LinearLayout.LayoutParams(popUpSize * 4, popUpSize));
        settingMusic.setX(popUpSize * 2);
        settingMusic.setY(popUpSize * 6);
        settingMusic.setScaleType(ImageView.ScaleType.FIT_START);
        settingMusic.setImageResource(R.drawable.setting_music);
        layoutSetting.addView(settingMusic);

        // create music value
        valueMusic = new ImageView(getApplicationContext());
        valueMusic.setLayoutParams(new RelativeLayout.LayoutParams(popUpSize * 2, popUpSize));
        valueMusic.setX(popUpSize * 6);
        valueMusic.setY(popUpSize * 6);
        valueMusic.setScaleType(ImageView.ScaleType.FIT_START);
        valueMusic.setImageResource(R.drawable.value_on);
        SetValue(valueMusic, ReadMusicValue());
        layoutSetting.addView(valueMusic);

        valueSound.setOnTouchListener(Static.btnPressAnimation);
        valueMusic.setOnTouchListener(Static.btnPressAnimation);
    }

    public void ShowSetting() {
        layoutCredits.setVisibility(View.INVISIBLE);
        layoutTransparent.setVisibility(View.VISIBLE);
        layoutSetting.setVisibility(View.VISIBLE);
    }

    public void ShowCredits() {
        layoutSetting.setVisibility(View.INVISIBLE);
        layoutTransparent.setVisibility(View.VISIBLE);
        layoutCredits.setVisibility(View.VISIBLE);
    }

    public void SetValue(ImageView view, boolean value) {
        if (value) {
            view.setImageResource(R.drawable.value_on);
            view.setTag(true);
            view.setLayoutParams(new RelativeLayout.LayoutParams(view.getLayoutParams().height * 65 / 50, view.getLayoutParams().height));
        }
        else {
            view.setImageResource(R.drawable.value_off);
            view.setTag(false);
            view.setLayoutParams(new RelativeLayout.LayoutParams(view.getLayoutParams().height * 80 / 50, view.getLayoutParams().height));
        }
    }

    public void SaveSoundValue(boolean value) {
        SharedPreferences setting = getSharedPreferences(getString(R.string.settingFileName), MODE_PRIVATE);
        SharedPreferences.Editor editor = setting.edit();
        editor.putBoolean(getString(R.string.settingSound), value);
        editor.apply();
    }

    public boolean ReadSoundValue() {
        SharedPreferences setting = getSharedPreferences(getString(R.string.settingFileName), MODE_PRIVATE);
        return setting.getBoolean(getString(R.string.settingSound), true);
    }

    public void SaveMusicValue(boolean value) {
        SharedPreferences setting = getSharedPreferences(getString(R.string.settingFileName), MODE_PRIVATE);
        SharedPreferences.Editor editor = setting.edit();
        editor.putBoolean(getString(R.string.settingMusic), value);
        editor.apply();
    }

    public boolean ReadMusicValue() {
        SharedPreferences setting = getSharedPreferences(getString(R.string.settingFileName), MODE_PRIVATE);
        return setting.getBoolean(getString(R.string.settingMusic), true);
    }
}
