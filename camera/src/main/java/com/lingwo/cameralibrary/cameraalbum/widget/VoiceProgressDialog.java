package com.lingwo.cameralibrary.cameraalbum.widget;

import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lingwo.cameralibrary.R;
import com.lingwo.cameralibrary.cameraalbum.util.BGAPhotoPickerUtil;

import java.lang.ref.WeakReference;

/**
 * @Description: 播放音频进度Dialog
 * @author: xiaji
 * @date: 2019-4-25 13:57
 * Copyright (c) 2017, Lingwo. All rights reserved
 */
public class VoiceProgressDialog {
    private Context context;
    private Dialog dialog;
    private Display display;
    private ProgressBar progressBar;
    private TextView txtVoiceTime;
    private ImageView btnClose;
    private FrameLayout flayoutBg;
    private MediaPlayer mediaPlayer;
    private String fileUrl;
    WifiManager.WifiLock wifiLock;

    private MyHandler myHandler = new MyHandler(this);

    public VoiceProgressDialog(Context context, String fileUrl){
        this.context = context;
        this.fileUrl = fileUrl;
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
        wifiLock = ((WifiManager)context.getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
    }

    public VoiceProgressDialog builder(){

        View view = LayoutInflater.from(context).inflate(R.layout.view_voice_picker, null);

        progressBar = (ProgressBar)view.findViewById(R.id.progressBar);
        txtVoiceTime = (TextView)view.findViewById(R.id.txtVoiceTime);
        flayoutBg = (FrameLayout)view.findViewById(R.id.flayout_bg);
        btnClose = (ImageView)view.findViewById(R.id.btnClose);

        dialog = new Dialog(context, R.style.BGAAlertDialogStyle);
        dialog.setContentView(view);

        flayoutBg.setLayoutParams(new FrameLayout.LayoutParams((int)(display.getWidth() * 0.85), FrameLayout.LayoutParams.WRAP_CONTENT));

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPlayVoice();
                dialog.dismiss();
            }
        });

        return this;
    }

    public VoiceProgressDialog setCancelable(boolean cancel) {
        dialog.setCancelable(cancel);
        dialog.setCanceledOnTouchOutside(cancel);
        return this;
    }

    private void startPlayVoice(){

        try {
            wifiLock.acquire();
            mediaPlayer.reset();

            mediaPlayer.setDataSource(fileUrl);
            mediaPlayer.prepare();
            String totalDuration = BGAPhotoPickerUtil.getFormatHMS(mediaPlayer.getDuration());
            progressBar.setMax(mediaPlayer.getDuration());
            txtVoiceTime.setText("00:00:00" + "/" + totalDuration);

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopPlayVoice();
                    dialog.dismiss();
                }
            });

            mediaPlayer.start();

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//
//                    try {
//                        while (mediaPlayer != null && mediaPlayer.isPlaying()){
//
//                            Thread.sleep(1000);
//                            myHandler.sendEmptyMessage(0);
//                        }
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//
//
//                }
//            }).start();
            myHandler.sendEmptyMessageDelayed(0, 1000);
        }catch (Exception e){
            if (wifiLock.isHeld())
                wifiLock.release();
            e.printStackTrace();
        }
    }

    private void stopPlayVoice(){

        if (wifiLock.isHeld())
            wifiLock.release();

        if (mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void show(){
        dialog.show();
        startPlayVoice();
    }

    private class MyHandler extends Handler {

        private final WeakReference<VoiceProgressDialog> mDialog;

        private MyHandler(VoiceProgressDialog dialog){
            mDialog = new WeakReference<VoiceProgressDialog>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            VoiceProgressDialog dialog = mDialog.get();

            try {
                if (dialog.mediaPlayer != null && dialog.mediaPlayer.isPlaying()){
                    int progress = dialog.mediaPlayer.getCurrentPosition();

                    dialog.progressBar.setProgress(progress);

                    txtVoiceTime.setText(BGAPhotoPickerUtil.getFormatHMS(progress) + "/" + BGAPhotoPickerUtil.getFormatHMS(mediaPlayer.getDuration()));

                    dialog.myHandler.sendEmptyMessageDelayed(0, 1000);
                }
            }catch (Exception e){
                e.printStackTrace();
            }


        }
    }
}
