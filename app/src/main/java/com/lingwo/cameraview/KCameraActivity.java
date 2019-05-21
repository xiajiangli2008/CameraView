package com.lingwo.cameraview;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.lingwo.cameralibrary.KCameraView;
import com.lingwo.cameralibrary.listener.ClickListener;
import com.lingwo.cameralibrary.listener.ErrorListener;
import com.lingwo.cameralibrary.listener.JCameraListener;
import com.lingwo.cameralibrary.util.FileUtil;

/**
 * @Description: 视频拍摄、拍摄截屏
 * @author: xiaji
 * @date: 2019-5-10 15:06
 * Copyright (c) 2017, Lingwo. All rights reserved
 */
public class KCameraActivity extends AppCompatActivity {

    KCameraView kCameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            this.getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        kCameraView = (KCameraView)findViewById(R.id.kCameraView);

        kCameraView.setSaveVideoPath(FileUtil.DST_FOLDER_NAME);
        kCameraView.setMediaQuality(KCameraView.MEDIA_QUALITY_MIDDLE);
        kCameraView.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {

            }

            @Override
            public void AudioPermissionError() {
                Toast.makeText(KCameraActivity.this, "需要录音权限", Toast.LENGTH_SHORT).show();
            }
        });

        kCameraView.setJCameraLisenter(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {

                FileUtil.saveCaptureBitmap(KCameraActivity.this, bitmap);
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {

                FileUtil.saveVideoFirstFrame(KCameraActivity.this, firstFrame);
            }
        });

        kCameraView.setBackClickListener(new ClickListener() {
            @Override
            public void onClick() {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //全屏显示
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        kCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        kCameraView.onPause();
    }
}
