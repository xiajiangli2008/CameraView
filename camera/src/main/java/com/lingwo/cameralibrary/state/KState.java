package com.lingwo.cameralibrary.state;

import android.view.SurfaceHolder;

/**
 * @Description: $TODO (这里用一句话描述这个类的作用)
 * @author: xiaji
 * @date: 2019-3-18 14:36
 * Copyright (c) 2017, Lingwo. All rights reserved
 */
public interface KState extends State {

    void confirm(SurfaceHolder holder, float screenProp, int type);
}
