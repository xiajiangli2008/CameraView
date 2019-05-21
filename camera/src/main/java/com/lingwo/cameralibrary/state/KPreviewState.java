package com.lingwo.cameralibrary.state;

import android.graphics.Bitmap;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.lingwo.cameralibrary.CameraInterface;
import com.lingwo.cameralibrary.KCameraView;
import com.lingwo.cameralibrary.util.LogUtil;

/**
 * @Description: $TODO (这里用一句话描述这个类的作用)
 * @author: xiaji
 * @date: 2019-3-18 14:13
 * Copyright (c) 2017, Lingwo. All rights reserved
 */
public class KPreviewState implements KState {
    public static final String TAG = "KPreviewState";

    private KCameraMachine machine;

    KPreviewState(KCameraMachine machine){
        this.machine = machine;
    }

    @Override
    public void start(SurfaceHolder holder, float screenProp) {
        CameraInterface.getInstance().doStartPreview(holder, screenProp);
    }

    @Override
    public void stop() {
        CameraInterface.getInstance().doStopPreview();
    }

    @Override
    public void foucs(float x, float y, CameraInterface.FocusCallback callback) {
        LogUtil.v("preview state foucs");
        if (machine.getView().handlerFoucs(x, y)) {
            CameraInterface.getInstance().handleFocus(machine.getContext(), x, y, callback);
        }
    }

    @Override
    public void swtich(SurfaceHolder holder, float screenProp) {
        CameraInterface.getInstance().switchCamera(holder, screenProp);
    }

    @Override
    public void restart() {

    }

    @Override
    public void capture() {
        CameraInterface.getInstance().takePicture(new CameraInterface.TakePictureCallback() {
            @Override
            public void captureResult(Bitmap bitmap, boolean isVertical) {
                machine.getView().showPicture(bitmap, isVertical);
                LogUtil.i("capture");
            }
        });
    }

    @Override
    public void record(Surface surface, float screenProp) {
        CameraInterface.getInstance().startRecord(surface, screenProp, null);
    }

    @Override
    public void stopRecord(final boolean isShort, long time) {
        CameraInterface.getInstance().stopRecord(isShort, new CameraInterface.StopRecordCallback() {
            @Override
            public void recordResult(String url, Bitmap firstFrame) {
                if (isShort) {
                    machine.getView().resetState(KCameraView.TYPE_SHORT);
                } else {
                    machine.getView().playVideo(firstFrame, url);
                    //machine.setState(machine.getBorrowVideoState());
                }
            }
        });
    }

    @Override
    public void cancle(SurfaceHolder holder, float screenProp) {

    }

    @Override
    public void confirm() {

    }

    @Override
    public void confirm(SurfaceHolder holder, float screenProp, int type) {

        if (type == KCameraMachine.PICTRUE_STATE){
            //CameraInterface.getInstance().doStartPreview(holder, screenProp);
            machine.getView().confirmState(KCameraView.TYPE_PICTURE);
        }else if (type == KCameraMachine.VIDEO_STATE){
            machine.getView().confirmState(KCameraView.TYPE_VIDEO);
        }
    }

    @Override
    public void zoom(float zoom, int type) {
        LogUtil.v(TAG, "zoom ===================================== ");
        CameraInterface.getInstance().setZoom(zoom, type);
    }

    @Override
    public void flash(String mode) {
        CameraInterface.getInstance().setFlashMode(mode);
    }
}
