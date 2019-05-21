package com.lingwo.cameralibrary;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Environment;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import com.lingwo.cameralibrary.listener.ClickListener;
import com.lingwo.cameralibrary.listener.ErrorListener;
import com.lingwo.cameralibrary.listener.JCameraListener;
import com.lingwo.cameralibrary.util.CheckPermission;
import com.lingwo.cameralibrary.util.FileUtil;
import com.lingwo.cameralibrary.util.LogUtil;
import com.lingwo.cameralibrary.util.ScreenUtils;
import com.lingwo.cameralibrary.view.CameraView;
import com.lingwo.cameralibrary.state.KCameraMachine;

import java.io.File;

/**
 * @Description: $TODO (这里用一句话描述这个类的作用)
 * @author: xiaji
 * @date: 2019-3-18 13:43
 * Copyright (c) 2017, Lingwo. All rights reserved
 */
public class KCameraView extends FrameLayout implements CameraInterface.CameraOpenOverCallback, SurfaceHolder.Callback, CameraView {

    //Camera状态机
    private KCameraMachine machine;

    //闪关灯状态
    private static final int TYPE_FLASH_AUTO = 0x031;
    private static final int TYPE_FLASH_ON = 0x032;
    private static final int TYPE_FLASH_OFF = 0x033;
    private int type_flash = TYPE_FLASH_OFF;

    //拍照浏览时候的类型
    public static final int TYPE_PICTURE = 0x011;
    public static final int TYPE_VIDEO = 0x012;
    public static final int TYPE_SHORT = 0x013;
    public static final int TYPE_DEFAULT = 0x014;

    //录制视频比特率
    public static final int MEDIA_QUALITY_HIGH = 20 * 100000;
    public static final int MEDIA_QUALITY_MIDDLE = 16 * 100000;
    public static final int MEDIA_QUALITY_LOW = 12 * 100000;
    public static final int MEDIA_QUALITY_POOR = 8 * 100000;
    public static final int MEDIA_QUALITY_FUNNY = 4 * 100000;
    public static final int MEDIA_QUALITY_DESPAIR = 2 * 100000;
    public static final int MEDIA_QUALITY_SORRY = 1 * 80000;

    private JCameraListener jCameraListener;
    private ClickListener backClickListener;
    private ErrorListener errorLisenter;

    private Context mContext;
    private VideoView mVideoView;
    private ImageView mSwitchCamera;
    private ImageView mFlashLamp;
    private FoucsView mFoucsView;
    private ImageView imgBack;
    private ImageView btnRecordVideo;
    private ImageView btnCapturePhoto;
    private Chronometer mTimer;
    private View mCaptureLayout;

    private int layout_width;
    private float screenProp = 0f;

    private Bitmap captureBitmap;   //拍照的捕获的图片
    private Bitmap firstFrame;      //第一帧图片
    private String videoUrl;        //视频URL

    private int zoomGradient = 0;   //缩放梯度

    private boolean firstTouch = true;
    private float firstTouchLength = 0;

    private boolean isStartRecord = false;

    public KCameraView(Context context){
        this(context, null);
    }

    public KCameraView(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public KCameraView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        mContext = context;

        //init data
        layout_width = ScreenUtils.getScreenWidth(mContext);
        zoomGradient = (int)(layout_width / 16f);
        LogUtil.v("zoom === " + zoomGradient);
        machine = new KCameraMachine(getContext(), this, this);

        //init view
        setWillNotDraw(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_camera, this);
        mTimer = (Chronometer)view.findViewById(R.id.timer);
        mCaptureLayout = (View)view.findViewById(R.id.capture_layout);
        mVideoView = (VideoView)view.findViewById(R.id.k_video_preview);
        imgBack = (ImageView)view.findViewById(R.id.img_back);
        btnRecordVideo = (ImageView)view.findViewById(R.id.img_record);
        btnCapturePhoto = (ImageView)view.findViewById(R.id.img_take_capture);
        mSwitchCamera = (ImageView)view.findViewById(R.id.image_switch);
        mFlashLamp = (ImageView)view.findViewById(R.id.image_flash);
        setFlashRes();
        mFlashLamp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                type_flash++;
                if (type_flash > TYPE_FLASH_OFF){
                    type_flash = TYPE_FLASH_AUTO;
                }
                setFlashRes();
            }
        });
        mFoucsView = (FoucsView)view.findViewById(R.id.fouce_view);
        mVideoView.getHolder().addCallback(this);
        //切换摄像头
        mSwitchCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                machine.swtich(mVideoView.getHolder(), screenProp);
            }
        });
        //拍照、录像
        btnRecordVideo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isStartRecord){

                    if (CheckPermission.getRecordState() != CheckPermission.STATE_SUCCESS){
                        if (errorLisenter != null){
                            errorLisenter.AudioPermissionError();
                            return;
                        }
                    }

                    isStartRecord = true;
                    mSwitchCamera.setVisibility(INVISIBLE);
                    mFlashLamp.setVisibility(INVISIBLE);
                    machine.record(mVideoView.getHolder().getSurface(), screenProp);
                    btnRecordVideo.setImageResource(R.drawable.icon_video_end);
                    mTimer.setBase(SystemClock.elapsedRealtime());
                    mTimer.start();
                }else {
                    machine.stopRecord(false, videoDuration(mTimer.getText().toString()));
                    btnRecordVideo.setImageResource(R.drawable.icon_video_start);
                    mTimer.stop();
                    isStartRecord = false;
                }
            }
        });

        btnCapturePhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwitchCamera.setVisibility(INVISIBLE);
                mFlashLamp.setVisibility(INVISIBLE);
                machine.capture();
            }
        });

        imgBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (backClickListener != null)
                    backClickListener.onClick();
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float widthSize = mVideoView.getMeasuredWidth();
        float heightSize = mVideoView.getMeasuredHeight();
        if (screenProp == 0){
            screenProp = heightSize / widthSize;
        }
    }

    @Override
    public void cameraHasOpened() {
        CameraInterface.getInstance().doStartPreview(mVideoView.getHolder(), screenProp);
    }

    //生命周期onResume
    public void onResume(){
        LogUtil.v("KCameraView onResume ======= ");
        resetState(TYPE_DEFAULT);
        CameraInterface.getInstance().registerSensorManager(mContext);
        CameraInterface.getInstance().setSwitchView(mSwitchCamera, mFlashLamp);
        machine.start(mVideoView.getHolder(), screenProp);
    }

    //生命周期onPause
    public void onPause(){
        LogUtil.v("KCameraView onPause ======= ");
        if (!isStartRecord){
            stopVideo();
            resetState(TYPE_PICTURE);
        }else {
            machine.stopRecord(false, videoDuration(mTimer.getText().toString()));
            btnRecordVideo.setImageResource(R.drawable.icon_video_start);
            mTimer.stop();
            isStartRecord = false;
        }
        CameraInterface.getInstance().isPreview(false);
        CameraInterface.getInstance().unregisterSensorManager(mContext);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        LogUtil.v("KCameraView SurfaceCreated");
        new Thread(){
            @Override
            public void run() {
                CameraInterface.getInstance().doOpenCamera(KCameraView.this);
            }
        }.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        LogUtil.v("KCameraView SurfaceDestroyed");
        CameraInterface.getInstance().doDestroyCamera();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 1) {
                    //显示对焦指示器
                    setFocusViewWidthAnimation(event.getX(), event.getY());
                }
                if (event.getPointerCount() == 2) {
                    Log.i("CJT", "ACTION_DOWN = " + 2);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    firstTouch = true;
                }
                if (event.getPointerCount() == 2) {
                    //第一个点
                    float point_1_X = event.getX(0);
                    float point_1_Y = event.getY(0);
                    //第二个点
                    float point_2_X = event.getX(1);
                    float point_2_Y = event.getY(1);

                    float result = (float) Math.sqrt(Math.pow(point_1_X - point_2_X, 2) + Math.pow(point_1_Y -
                            point_2_Y, 2));

                    if (firstTouch) {
                        firstTouchLength = result;
                        firstTouch = false;
                    }
                    if ((int) (result - firstTouchLength) / zoomGradient != 0) {
                        firstTouch = true;
                        machine.zoom(result - firstTouchLength, CameraInterface.TYPE_CAPTURE);
                    }
//                    Log.i("CJT", "result = " + (result - firstTouchLength));
                }
                break;
            case MotionEvent.ACTION_UP:
                firstTouch = true;
                break;
        }
        return true;
    }

    //对焦框指示器动画
    private void setFocusViewWidthAnimation(float x, float y) {
        machine.foucs(x, y, new CameraInterface.FocusCallback() {
            @Override
            public void focusSuccess() {
                mFoucsView.setVisibility(INVISIBLE);
            }
        });
    }

    /**************************************************
     * 对外提供的API                     *
     **************************************************/

    public void setSaveVideoPath(String path) {
        path = Environment.getExternalStorageDirectory() + File.separator + path + File.separator + "Video";
        CameraInterface.getInstance().setSaveVideoPath(path);
    }

    public void setJCameraLisenter(JCameraListener jCameraLisenter) {
        this.jCameraListener = jCameraLisenter;
    }

    //启动Camera错误回调
    public void setErrorLisenter(ErrorListener errorLisenter) {
        this.errorLisenter = errorLisenter;
        CameraInterface.getInstance().setErrorLinsenter(errorLisenter);
    }

    //设置录制质量
    public void setMediaQuality(int quality) {
        CameraInterface.getInstance().setMediaQuality(quality);
    }

    @Override
    public void resetState(int type) {
        switch (type) {
            case TYPE_VIDEO:
                stopVideo();    //停止播放
                //初始化VideoView
                FileUtil.deleteFile(videoUrl);
                mVideoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                machine.start(mVideoView.getHolder(), screenProp);
                break;
            case TYPE_PICTURE:
                break;
            case TYPE_SHORT:
                break;
            case TYPE_DEFAULT:
                mVideoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                break;
        }
        mSwitchCamera.setVisibility(VISIBLE);
        mFlashLamp.setVisibility(VISIBLE);
    }

    @Override
    public void confirmState(int type) {
        switch (type) {
            case TYPE_VIDEO:
                stopVideo();    //停止播放
                mVideoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                machine.start(mVideoView.getHolder(), screenProp);
                if (jCameraListener != null) {
                    jCameraListener.recordSuccess(videoUrl, firstFrame);
                }
                break;
            case TYPE_PICTURE:
                mVideoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                machine.start(mVideoView.getHolder(), screenProp);
                if (jCameraListener != null) {
                    jCameraListener.captureSuccess(captureBitmap);
                }
                break;
            case TYPE_SHORT:
                break;
            case TYPE_DEFAULT:
                break;
        }
        mSwitchCamera.setVisibility(VISIBLE);
        mFlashLamp.setVisibility(VISIBLE);
    }

    @Override
    public void showPicture(Bitmap bitmap, boolean isVertical) {
        captureBitmap = bitmap;
        machine.confirm(mVideoView.getHolder(), screenProp, KCameraMachine.PICTRUE_STATE);
    }

    @Override
    public void playVideo(Bitmap firstFrame, String url) {

        videoUrl = url;
        KCameraView.this.firstFrame = firstFrame;

        machine.confirm(mVideoView.getHolder(), screenProp, KCameraMachine.VIDEO_STATE);
    }

    @Override
    public void stopVideo() {

    }

    @Override
    public void setTip(String tip) {

    }

    @Override
    public void startPreviewCallback() {
        LogUtil.i("startPreviewCallback");
        handlerFoucs(mFoucsView.getWidth() / 2, mFoucsView.getHeight() / 2);
    }

    @Override
    public boolean handlerFoucs(float x, float y) {
        if (y > mCaptureLayout.getTop()) {
            return false;
        }
        mFoucsView.setVisibility(VISIBLE);
        if (x < mFoucsView.getWidth() / 2) {
            x = mFoucsView.getWidth() / 2;
        }
        if (x > layout_width - mFoucsView.getWidth() / 2) {
            x = layout_width - mFoucsView.getWidth() / 2;
        }
        if (y < mFoucsView.getWidth() / 2) {
            y = mFoucsView.getWidth() / 2;
        }
        if (y > mCaptureLayout.getTop() - mFoucsView.getWidth() / 2) {
            y = mCaptureLayout.getTop() - mFoucsView.getWidth() / 2;
        }
        mFoucsView.setX(x - mFoucsView.getWidth() / 2);
        mFoucsView.setY(y - mFoucsView.getHeight() / 2);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFoucsView, "scaleX", 1, 0.6f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFoucsView, "scaleY", 1, 0.6f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mFoucsView, "alpha", 1f, 0.4f, 1f, 0.4f, 1f, 0.4f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY).before(alpha);
        animSet.setDuration(400);
        animSet.start();
        return true;
    }

    public void setBackClickListener(ClickListener clickListener){
        this.backClickListener = clickListener;
    }

    private void setFlashRes() {
        switch (type_flash) {
            case TYPE_FLASH_AUTO:
                mFlashLamp.setImageResource(com.lingwo.cameralibrary.R.drawable.ic_flash_auto);
                machine.flash(Camera.Parameters.FLASH_MODE_AUTO);
                break;
            case TYPE_FLASH_ON:
                mFlashLamp.setImageResource(com.lingwo.cameralibrary.R.drawable.ic_flash_on);
                machine.flash(Camera.Parameters.FLASH_MODE_ON);
                break;
            case TYPE_FLASH_OFF:
                mFlashLamp.setImageResource(com.lingwo.cameralibrary.R.drawable.ic_flash_off);
                machine.flash(Camera.Parameters.FLASH_MODE_OFF);
                break;
        }
    }

    private long videoDuration(String duration){

        int hour = 0, minute = 0, second = 0;
        if (duration.length() == 7){
            hour = Integer.parseInt(duration.split(":")[0]);
            minute = Integer.parseInt(duration.split(":")[1]);
            second = Integer.parseInt(duration.split(":")[2]);
        }else if (duration.length() == 5){
            minute = Integer.parseInt(duration.split(":")[0]);
            second = Integer.parseInt(duration.split(":")[1]);
        }


        return (hour * 3600 + minute * 60 + second);
    }
}
