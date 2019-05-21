package com.lingwo.cameralibrary.cameraalbum.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.lingwo.cameralibrary.R;
import com.lingwo.cameralibrary.cameraalbum.imageloader.BGAImage;
import com.lingwo.cameralibrary.cameraalbum.model.BGAMediaFolderModel;
import com.lingwo.cameralibrary.cameraalbum.model.BGAMediaModel;
import com.lingwo.cameralibrary.cameraalbum.util.BGAPhotoPickerUtil;

import java.util.ArrayList;

import cn.bingoogolapple.baseadapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.baseadapter.BGAViewHolderHelper;

/**
 * @Description: $TODO (这里用一句话描述这个类的作用)
 * @author: xiaji
 * @date: 2019-4-29 16:48
 * Copyright (c) 2017, Lingwo. All rights reserved
 */
public class BGAMediaPickerAdapter extends BGARecyclerViewAdapter<BGAMediaModel> {

    private ArrayList<BGAMediaModel> mSelectedPhotos = new ArrayList<>();
    private int mPhotoSize;


    public BGAMediaPickerAdapter(RecyclerView recyclerView) {
        super(recyclerView);
        mPhotoSize = BGAPhotoPickerUtil.getScreenWidth() / 6;
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.bga_pp_item_photo_picker;
    }

    @Override
    protected void setItemChildListener(BGAViewHolderHelper helper, int viewType) {
        helper.setItemChildClickListener(R.id.iv_item_photo_picker_flag);
        helper.setItemChildClickListener(R.id.iv_item_photo_picker_photo);
    }

    @Override
    protected void fillData(BGAViewHolderHelper helper, int position, BGAMediaModel model) {
        BGAImage.display(helper.getImageView(R.id.iv_item_photo_picker_photo), R.mipmap.bga_pp_ic_holder_dark, model.getThumbNail(), mPhotoSize);

        if (model.getMediaType() == BGAMediaModel.MEDIA_TYPE_VIDEO){
            helper.setVisibility(R.id.iv_item_nine_video_play, View.VISIBLE);
        }else {
            helper.setVisibility(R.id.iv_item_nine_video_play, View.GONE);
        }
        BGAImage.display(helper.getImageView(R.id.iv_item_nine_video_play), R.mipmap.bga_pp_ic_holder_play, "", mPhotoSize);

        if (mSelectedPhotos.contains(model)) {
            helper.setImageResource(R.id.iv_item_photo_picker_flag, R.mipmap.bga_pp_ic_cb_checked);
            helper.getImageView(R.id.iv_item_photo_picker_photo).setColorFilter(helper.getConvertView().getResources().getColor(R.color.bga_pp_photo_selected_mask));
        } else {
            helper.setImageResource(R.id.iv_item_photo_picker_flag, R.mipmap.bga_pp_ic_cb_normal);
            helper.getImageView(R.id.iv_item_photo_picker_photo).setColorFilter(null);
        }
    }

    public void setSelectedPhotos(ArrayList<BGAMediaModel> selectedPhotos) {
        if (selectedPhotos != null) {
            mSelectedPhotos = selectedPhotos;
        }
        notifyDataSetChanged();
    }

    public ArrayList<BGAMediaModel> getSelectedPhotos() {
        return mSelectedPhotos;
    }

    public int getSelectedCount() {
        return mSelectedPhotos.size();
    }

    public void setPhotoFolderModel(BGAMediaFolderModel mediaFolderModel){
        setData(mediaFolderModel.getPhotos());
    }
}
