package com.lingwo.cameralibrary.state;


import android.content.Context;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.lingwo.cameralibrary.CameraInterface;
import com.lingwo.cameralibrary.view.CameraView;

/**
 * @Description: $TODO (这里用一句话描述这个类的作用)
 * @author: xiaji
 * @date: 2019-3-18 13:45
 * Copyright (c) 2017, Lingwo. All rights reserved
 */
public class KCameraMachine implements KState {

    public final static int PICTRUE_STATE = 0;
    public final static int VIDEO_STATE = 1;

    private Context context;
    private KState state;
    private CameraView view;

    private int tyeModel = -1;

    public KCameraMachine(Context context, CameraView view, CameraInterface.CameraOpenOverCallback cameraOpenOverCallback){

        this.context = context;
        this.state = new KPreviewState(this);
        this.view = view;
    }

    public CameraView getView(){
        return view;
    }

    public Context getContext() {
        return context;
    }

    public void setTyeModel(int type){
        this.tyeModel = type;
    }

    @Override
    public void start(SurfaceHolder holder, float screenProp) {
        state.start(holder, screenProp);
    }

    @Override
    public void stop() {
        state.stop();
    }

    @Override
    public void foucs(float x, float y, CameraInterface.FocusCallback callback) {
        state.foucs(x, y, callback);
    }

    @Override
    public void swtich(SurfaceHolder holder, float screenProp) {
        state.swtich(holder, screenProp);
    }

    @Override
    public void restart() {
        state.restart();
    }

    @Override
    public void capture() {
        state.capture();
    }

    @Override
    public void record(Surface surface, float screenProp) {
        state.record(surface, screenProp);
    }

    @Override
    public void stopRecord(boolean isShort, long time) {
        state.stopRecord(isShort, time);
    }

    @Override
    public void cancle(SurfaceHolder holder, float screenProp) {
        state.cancle(holder, screenProp);
    }

    @Override
    public void confirm() {

    }

    @Override
    public void confirm(SurfaceHolder holder, float screenProp, int type) {
        state.confirm(holder, screenProp, type);
    }

    @Override
    public void zoom(float zoom, int type) {
        state.zoom(zoom, type);
    }

    @Override
    public void flash(String mode) {
        state.flash(mode);
    }
}
