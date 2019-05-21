package com.lingwo.cameralibrary.cameraalbum.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.lingwo.cameralibrary.R;
import com.lingwo.cameralibrary.cameraalbum.imageloader.BGAImage;
import com.lingwo.cameralibrary.cameraalbum.model.BGAMediaModel;
import com.lingwo.cameralibrary.cameraalbum.util.BGAPhotoPickerUtil;

import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.baseadapter.BGAAdapterViewAdapter;
import cn.bingoogolapple.baseadapter.BGABaseAdapterUtil;
import cn.bingoogolapple.baseadapter.BGAViewHolderHelper;

/**
 * @Description: $TODO (这里用一句话描述这个类的作用)
 * @author: xiaji
 * @date: 2019-4-19 14:08
 * Copyright (c) 2017, Lingwo. All rights reserved
 */
public class BGANineMediaLayout extends FrameLayout implements AdapterView.OnItemClickListener, View.OnClickListener {
    private static final int ITEM_NUM_COLUMNS = 3;
    private PhotoAdapter mPhotoAdapter;
    private BGAImageView mPhotoIv;
    private BGAImageView mVideoPlayIv;
    private RelativeLayout contentRl;
    private BGAHeightWrapGridView mPhotoGv;
    private Delegate mDelegate;
    private int mCurrentClickItemPosition;

    private int mItemCornerRadius;
    private boolean mShowAsLargeWhenOnlyOne;
    private int mItemWhiteSpacing;
    private int mOtherWhiteSpacing;
    private int mPlaceholderDrawableResId;
    private int mItemSpanCount;
    private boolean isVideo;

    private int mItemWidth;


    public BGANineMediaLayout(@NonNull Context context) {
        this(context, null);
    }

    public BGANineMediaLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BGANineMediaLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefaultAttrs();
        initCustomAttrs(context, attrs);
        afterInitDefaultAndCustomAttrs();
    }

    private void initDefaultAttrs() {
        mItemWidth = 0;
        mShowAsLargeWhenOnlyOne = true;
        mItemCornerRadius = 0;
        mItemWhiteSpacing = BGABaseAdapterUtil.dp2px(4);
        mPlaceholderDrawableResId = R.mipmap.bga_pp_ic_holder_light;
        mOtherWhiteSpacing = BGABaseAdapterUtil.dp2px(100);
        mItemSpanCount = 3;
        isVideo = false;
    }

    private void initCustomAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BGANinePhotoLayout);
        final int N = typedArray.getIndexCount();
        for (int i = 0; i < N; i++) {
            initCustomAttr(typedArray.getIndex(i), typedArray);
        }
        typedArray.recycle();
    }

    private void initCustomAttr(int attr, TypedArray typedArray) {
        if (attr == R.styleable.BGANinePhotoLayout_bga_npl_showAsLargeWhenOnlyOne) {
            mShowAsLargeWhenOnlyOne = typedArray.getBoolean(attr, mShowAsLargeWhenOnlyOne);
        } else if (attr == R.styleable.BGANinePhotoLayout_bga_npl_itemCornerRadius) {
            mItemCornerRadius = typedArray.getDimensionPixelSize(attr, mItemCornerRadius);
        } else if (attr == R.styleable.BGANinePhotoLayout_bga_npl_itemWhiteSpacing) {
            mItemWhiteSpacing = typedArray.getDimensionPixelSize(attr, mItemWhiteSpacing);
        } else if (attr == R.styleable.BGANinePhotoLayout_bga_npl_otherWhiteSpacing) {
            mOtherWhiteSpacing = typedArray.getDimensionPixelOffset(attr, mOtherWhiteSpacing);
        } else if (attr == R.styleable.BGANinePhotoLayout_bga_npl_placeholderDrawable) {
            mPlaceholderDrawableResId = typedArray.getResourceId(attr, mPlaceholderDrawableResId);
        } else if (attr == R.styleable.BGANinePhotoLayout_bga_npl_itemWidth) {
            mItemWidth = typedArray.getDimensionPixelSize(attr, mItemWidth);
        } else if (attr == R.styleable.BGANinePhotoLayout_bga_npl_itemSpanCount) {
            mItemSpanCount = typedArray.getInteger(attr, mItemSpanCount);
        } else if (attr == R.styleable.BGANinePhotoLayout_bga_npl_video){
            isVideo = typedArray.getBoolean(attr, isVideo);
        }
    }

    private void afterInitDefaultAndCustomAttrs() {
        if (mItemWidth == 0) {
            mItemWidth = (BGAPhotoPickerUtil.getScreenWidth() - mOtherWhiteSpacing - (mItemSpanCount - 1) * mItemWhiteSpacing) / mItemSpanCount;
        }

        contentRl = new RelativeLayout(getContext());

        mPhotoIv = new BGAImageView(getContext());
        mPhotoIv.setClickable(true);
        mPhotoIv.setOnClickListener(this);

        contentRl.addView(mPhotoIv, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mVideoPlayIv = new BGAImageView(getContext());
        mVideoPlayIv.setSquare(true);
        mVideoPlayIv.setImageResource(R.mipmap.bga_pp_ic_holder_play);
        mVideoPlayIv.setVisibility(GONE);
        contentRl.addView(mVideoPlayIv, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mPhotoGv = new BGAHeightWrapGridView(getContext());
        mPhotoGv.setHorizontalSpacing(mItemWhiteSpacing);
        mPhotoGv.setVerticalSpacing(mItemWhiteSpacing);
        mPhotoGv.setNumColumns(ITEM_NUM_COLUMNS);
        mPhotoGv.setOnItemClickListener(this);
        mPhotoAdapter = new PhotoAdapter(getContext());
        mPhotoGv.setAdapter(mPhotoAdapter);

        addView(contentRl, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(mPhotoGv);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        mCurrentClickItemPosition = position;
        if (mDelegate != null) {
            mDelegate.onClickNinePhotoItem(this, view, mCurrentClickItemPosition, mPhotoAdapter.getItem(mCurrentClickItemPosition), mPhotoAdapter.getData());
        }
    }

    @Override
    public void onClick(View view) {
        mCurrentClickItemPosition = 0;
        if (mDelegate != null) {
            mDelegate.onClickNinePhotoItem(this, view, mCurrentClickItemPosition, mPhotoAdapter.getItem(mCurrentClickItemPosition), mPhotoAdapter.getData());
        }
    }

    /**
     * 设置图片路径数据集合
     *
     * @param photos
     */
    public void setData(ArrayList<BGAMediaModel> photos){
        if (photos.size() == 0){
            setVisibility(GONE);
        }else {
            setVisibility(VISIBLE);

            if (photos.size() == 1 && mShowAsLargeWhenOnlyOne){
                mPhotoGv.setVisibility(GONE);
                mPhotoAdapter.setData(photos);
                mPhotoIv.setVisibility(VISIBLE);

                int size = mItemWidth * 2 + mItemWhiteSpacing + mItemWidth / 4;
                mPhotoIv.setMaxWidth(size);
                mPhotoIv.setMaxHeight(size);

                if (mItemCornerRadius > 0) {
                    mPhotoIv.setCornerRadius(mItemCornerRadius);
                }

                if (photos.get(0).getMediaType() == BGAMediaModel.MEDIA_TYPE_VIDEO){
                    mVideoPlayIv.setVisibility(VISIBLE);
                    mVideoPlayIv.setMaxWidth(size);

                    if (mItemCornerRadius > 0) {
                        mVideoPlayIv.setCornerRadius(mItemCornerRadius);
                    }
                }
                else {
                    mVideoPlayIv.setVisibility(GONE);
                }

                BGAImage.display(mPhotoIv, mPlaceholderDrawableResId, (photos.get(0).getMediaType() == BGAMediaModel.MEDIA_TYPE_PHOTO ? photos.get(0).getMediaUrl() : photos.get(0).getThumbNail()), size);
            }else {
                mPhotoIv.setVisibility(GONE);
                mVideoPlayIv.setVisibility(GONE);
                mPhotoGv.setVisibility(VISIBLE);

                ViewGroup.LayoutParams layoutParams = mPhotoGv.getLayoutParams();

                if (mItemSpanCount > 3) {
                    int itemSpanCount = photos.size() < mItemSpanCount ? photos.size() : mItemSpanCount;
                    mPhotoGv.setNumColumns(itemSpanCount);
                    layoutParams.width = mItemWidth * itemSpanCount + (itemSpanCount - 1) * mItemWhiteSpacing;
                } else {
                    mPhotoGv.setNumColumns(3);
                    layoutParams.width = mItemWidth * 3 + 2 * mItemWhiteSpacing;
                    /*if (photos.size() == 1) {
                        mPhotoGv.setNumColumns(1);
                        layoutParams.width = mItemWidth;
                    } else if (photos.size() == 2) {
                        mPhotoGv.setNumColumns(2);
                        layoutParams.width = mItemWidth * 2 + mItemWhiteSpacing;
                    } else if (photos.size() == 4) {
                        mPhotoGv.setNumColumns(2);
                        layoutParams.width = mItemWidth * 2 + mItemWhiteSpacing;
                    } else {
                        mPhotoGv.setNumColumns(3);
                        layoutParams.width = mItemWidth * 3 + 2 * mItemWhiteSpacing;
                    }*/
                }

                mPhotoGv.setLayoutParams(layoutParams);
                mPhotoAdapter.setData(photos);
            }
        }
    }

    public void setDelegate(Delegate delegate) {
        mDelegate = delegate;
    }

    public ArrayList<BGAMediaModel> getData() {
        return (ArrayList<BGAMediaModel>) mPhotoAdapter.getData();
    }

    public int getItemCount() {
        return mPhotoAdapter.getCount();
    }

    public BGAMediaModel getCurrentClickItem() {
        return mPhotoAdapter.getItem(mCurrentClickItemPosition);
    }

    public int getCurrentClickItemPosition() {
        return mCurrentClickItemPosition;
    }

    private class PhotoAdapter extends BGAAdapterViewAdapter<BGAMediaModel>{
        private int mImageSize;
        private boolean mIsVideo;

        public PhotoAdapter(Context context){
            super(context, R.layout.bga_pp_item_nine_photo);
            mImageSize = BGAPhotoPickerUtil.getScreenWidth() / (mItemSpanCount > 3 ? 8 : 6);
        }

        @Override
        protected void fillData(BGAViewHolderHelper helper, int position, BGAMediaModel model) {
            if (mItemCornerRadius > 0){
                BGAImageView imageView = helper.getView(R.id.iv_item_nine_photo_photo);
                imageView.setCornerRadius(mItemCornerRadius);
            }

            BGAImage.display(helper.getImageView(R.id.iv_item_nine_photo_photo), mPlaceholderDrawableResId, (model.getMediaType() == BGAMediaModel.MEDIA_TYPE_PHOTO ? model.getMediaUrl() : model.getThumbNail()), mImageSize);

            if (model.getMediaType() == BGAMediaModel.MEDIA_TYPE_VIDEO)
                helper.setVisibility(R.id.iv_item_nine_video_play, VISIBLE);
            else
                helper.setVisibility(R.id.iv_item_nine_video_play, GONE);
            BGAImage.display(helper.getImageView(R.id.iv_item_nine_video_play), R.mipmap.bga_pp_ic_holder_play, "", mImageSize);
        }
    }

    public interface Delegate{
        void onClickNinePhotoItem(BGANineMediaLayout ninePhotoLayout, View view, int position, BGAMediaModel model, List<BGAMediaModel> models);
    }
}
