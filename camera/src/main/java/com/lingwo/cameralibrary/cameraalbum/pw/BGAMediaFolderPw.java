package com.lingwo.cameralibrary.cameraalbum.pw;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.lingwo.cameralibrary.R;
import com.lingwo.cameralibrary.cameraalbum.imageloader.BGAImage;
import com.lingwo.cameralibrary.cameraalbum.model.BGAMediaFolderModel;
import com.lingwo.cameralibrary.cameraalbum.util.BGAPhotoPickerUtil;

import java.util.ArrayList;

import cn.bingoogolapple.baseadapter.BGAOnRVItemClickListener;
import cn.bingoogolapple.baseadapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.baseadapter.BGAViewHolderHelper;

/**
 * @Description: $TODO (这里用一句话描述这个类的作用)
 * @author: xiaji
 * @date: 2019-4-30 9:43
 * Copyright (c) 2017, Lingwo. All rights reserved
 */
public class BGAMediaFolderPw extends BGABasePopupWindow implements BGAOnRVItemClickListener {

    public static final int ANIM_DURATION = 300;
    private LinearLayout mRootLl;
    private RecyclerView mContentRv;
    private FolderAdapter mFolderAdapter;
    private Delegate mDelegate;
    private int mCurrentPosition;

    public BGAMediaFolderPw(Activity activity, View anchorView, Delegate delegate){
        super(activity, R.layout.bga_pp_pw_photo_folder, anchorView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        mDelegate = delegate;
    }

    @Override
    protected void initView() {
        mRootLl = findViewById(R.id.ll_photo_folder_root);
        mContentRv = findViewById(R.id.rv_photo_folder_content);
    }

    @Override
    protected void setListener() {
        mRootLl.setOnClickListener(this);
        mFolderAdapter = new FolderAdapter(mContentRv);
        mFolderAdapter.setOnRVItemClickListener(this);
    }

    @Override
    protected void processLogic() {
        setAnimationStyle(android.R.style.Animation);
        setBackgroundDrawable(new ColorDrawable(0x90000000));

        mContentRv.setLayoutManager(new LinearLayoutManager(mActivity));
        mContentRv.setAdapter(mFolderAdapter);
    }

    /**
     * 设置目录数据集合
     *
     * @param data
     */
    public void setData(ArrayList<BGAMediaFolderModel> data) {
        mFolderAdapter.setData(data);
    }

    @Override
    public void show() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            int[] location = new int[2];
            mAnchorView.getLocationInWindow(location);
            int offsetY = location[1] + mAnchorView.getHeight();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                setHeight(BGAPhotoPickerUtil.getScreenHeight() - offsetY);
            }
            showAtLocation(mAnchorView, Gravity.NO_GRAVITY, 0, offsetY);
        } else {
            showAsDropDown(mAnchorView);
        }

        ViewCompat.animate(mContentRv).translationY(-mWindowRootView.getHeight()).setDuration(0).start();
        ViewCompat.animate(mContentRv).translationY(0).setDuration(ANIM_DURATION).start();
        ViewCompat.animate(mRootLl).alpha(0).setDuration(0).start();
        ViewCompat.animate(mRootLl).alpha(1).setDuration(ANIM_DURATION).start();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ll_photo_folder_root) {
            dismiss();
        }
    }

    @Override
    public void dismiss() {
        ViewCompat.animate(mContentRv).translationY(-mWindowRootView.getHeight()).setDuration(ANIM_DURATION).start();
        ViewCompat.animate(mRootLl).alpha(1).setDuration(0).start();
        ViewCompat.animate(mRootLl).alpha(0).setDuration(ANIM_DURATION).start();

        if (mDelegate != null) {
            mDelegate.executeDismissAnim();
        }

        mContentRv.postDelayed(new Runnable() {
            @Override
            public void run() {
                BGAMediaFolderPw.super.dismiss();
            }
        }, ANIM_DURATION);
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    @Override
    public void onRVItemClick(ViewGroup parent, View itemView, int position) {
        if (mDelegate != null && mCurrentPosition != position) {
            mDelegate.onSelectedFolder(position);
        }
        mCurrentPosition = position;
        dismiss();
    }

    private class FolderAdapter extends BGARecyclerViewAdapter<BGAMediaFolderModel>{

        private int mImageSize;

        public FolderAdapter(RecyclerView recyclerView){
            super(recyclerView, R.layout.bga_pp_item_photo_folder);

            mData = new ArrayList<>();
            mImageSize = BGAPhotoPickerUtil.getScreenWidth() / 10;
        }

        @Override
        protected void fillData(BGAViewHolderHelper helper, int position, BGAMediaFolderModel model) {
            helper.setText(R.id.tv_item_photo_folder_name, model.folderName);
            helper.setText(R.id.tv_item_photo_folder_count, String.valueOf(model.getCount()));
            BGAImage.display(helper.getImageView(R.id.iv_item_photo_folder_photo), R.mipmap.bga_pp_ic_holder_light, model.coverPath, mImageSize);
        }
    }

    public interface Delegate {
        void onSelectedFolder(int position);

        void executeDismissAnim();
    }
}
