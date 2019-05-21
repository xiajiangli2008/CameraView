package com.lingwo.cameralibrary.cameraalbum.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingwo.cameralibrary.R;
import com.lingwo.cameralibrary.cameraalbum.adapter.BGAMediaPickerAdapter;
import com.lingwo.cameralibrary.cameraalbum.imageloader.BGARVOnScrollListener;
import com.lingwo.cameralibrary.cameraalbum.model.BGAMediaFolderModel;
import com.lingwo.cameralibrary.cameraalbum.model.BGAMediaModel;
import com.lingwo.cameralibrary.cameraalbum.pw.BGAMediaFolderPw;
import com.lingwo.cameralibrary.cameraalbum.pw.BGAPhotoFolderPw;
import com.lingwo.cameralibrary.cameraalbum.util.BGAAsyncTask;
import com.lingwo.cameralibrary.cameraalbum.util.BGALoadMediaTask;
import com.lingwo.cameralibrary.cameraalbum.util.BGAPhotoHelper;
import com.lingwo.cameralibrary.cameraalbum.util.BGAPhotoPickerUtil;
import com.lingwo.cameralibrary.cameraalbum.widget.VoiceProgressDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import cn.bingoogolapple.baseadapter.BGAGridDivider;
import cn.bingoogolapple.baseadapter.BGAOnItemChildClickListener;
import cn.bingoogolapple.baseadapter.BGAOnNoDoubleClickListener;

/**
 * @Description: $TODO (这里用一句话描述这个类的作用)
 * @author: xiaji
 * @date: 2019-4-29 16:23
 * Copyright (c) 2017, Kumi. All rights reserved
 */
public class BGAMediaPickerActivity extends BGAPPToolbarActivity implements BGAOnItemChildClickListener, BGAAsyncTask.Callback<ArrayList<BGAMediaFolderModel>> {

    private static final String EXTRA_MEDIA_FILE_DIR = "EXTRA_MEDIA_FILE_DIR";
    private static final String EXTRA_CAMERA_FILE_DIR = "EXTRA_CAMERA_FILE_DIR";
    private static final String EXTRA_SELECTED_PHOTOS = "EXTRA_SELECTED_PHOTOS";
    private static final String EXTRA_MAX_CHOOSE_COUNT = "EXTRA_MAX_CHOOSE_COUNT";
    private static final String EXTRA_PAUSE_ON_SCROLL = "EXTRA_PAUSE_ON_SCROLL";

    private static final String STATE_SELECTED_PHOTOS = "STATE_SELECTED_PHOTOS";

    /**
     * 拍照的请求码
     */
    private static final int REQUEST_CODE_TAKE_PHOTO = 1;
    /**
     * 预览照片的请求码
     */
    private static final int RC_PREVIEW = 2;

    private static final int SPAN_COUNT = 3;

    private TextView mTitleTv;
    private ImageView mArrowIv;
    private TextView mSubmitTv;
    private RecyclerView mContentRv;

    private BGAMediaFolderModel mCurrentMedialFolderModel;

    /**
     * 相册图片、音频缩略图、视频缩略图路径
     */
    private String mediaFileDir = "";
    /**
     * 最多选择多少张图片，默认等于1，为单选
     */
    private int mMaxChooseCount = 1;
    /**
     * 右上角按钮文本
     */
    private String mTopRightBtnText;
    /**
     * 媒体文件目录数据集合
     */
    private ArrayList<BGAMediaFolderModel> mPhotoFolderModels;

    private BGAMediaPickerAdapter mPicAdapter;

    private BGAPhotoHelper mPhotoHelper;

    private BGAMediaFolderPw mPhotoFolderPw;

    private BGALoadMediaTask mLoadMediaTask;

    private AppCompatDialog mLoadingDialog;

    private ArrayList<BGAMediaModel> selectedVoiceOrVideoList;

    private BGAOnNoDoubleClickListener mOnClickShowPhotoFolderListener = new BGAOnNoDoubleClickListener() {
        @Override
        public void onNoDoubleClick(View v) {
            if (mPhotoFolderModels != null && mPhotoFolderModels.size() > 0) {
                showPhotoFolderPw();
            }
        }
    };

    public static class IntentBuilder{
        private Intent mIntent;

        public IntentBuilder(Context context){
            mIntent = new Intent(context, BGAMediaPickerActivity.class);
        }

        /**
         * 拍照后图片保存的目录。如果传 null 表示没有拍照功能，如果不为 null 则具有拍照功能，
         */
        public IntentBuilder cameraFileDir(@Nullable File cameraFileDir) {
            mIntent.putExtra(EXTRA_CAMERA_FILE_DIR, cameraFileDir);
            return this;
        }

        /**
         * 图片选择张数的最大值
         *
         * @param maxChooseCount
         * @return
         */
        public IntentBuilder maxChooseCount(int maxChooseCount) {
            mIntent.putExtra(EXTRA_MAX_CHOOSE_COUNT, maxChooseCount);
            return this;
        }

        /**
         * 当前已选中的图片路径集合，可以传 null
         */
        public IntentBuilder selectedPhotos(@Nullable ArrayList<BGAMediaModel> selectedPhotos) {
            mIntent.putParcelableArrayListExtra(EXTRA_SELECTED_PHOTOS, selectedPhotos);
            return this;
        }

        /**
         * 滚动列表时是否暂停加载图片，默认为 false
         */
        public IntentBuilder pauseOnScroll(boolean pauseOnScroll) {
            mIntent.putExtra(EXTRA_PAUSE_ON_SCROLL, pauseOnScroll);
            return this;
        }

        /**
         * 相册图片、音频、视频预览图路径设置
         * @return
         */
        public IntentBuilder mediaFileDir(String dir){
            mIntent.putExtra(EXTRA_MEDIA_FILE_DIR, dir);
            return this;
        }

        public Intent build() {
            return mIntent;
        }
    }

    /**
     * 获取已选择的图片集合
     *
     * @param intent
     * @return
     */
    public static ArrayList<BGAMediaModel> getSelectedPhotos(Intent intent) {
        return intent.getParcelableArrayListExtra(EXTRA_SELECTED_PHOTOS);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.bga_pp_activity_photo_picker);
        mContentRv = findViewById(R.id.rv_photo_picker_content);
    }

    @Override
    protected void setListener() {
        mPicAdapter = new BGAMediaPickerAdapter(mContentRv);
        mPicAdapter.setOnItemChildClickListener(this);

        if (getIntent().getBooleanExtra(EXTRA_PAUSE_ON_SCROLL, false)) {
            mContentRv.addOnScrollListener(new BGARVOnScrollListener(this));
        }
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {

        mediaFileDir = getIntent().getStringExtra(EXTRA_MEDIA_FILE_DIR);
        // 获取拍照图片保存目录
//        File cameraFileDir = (File) getIntent().getSerializableExtra(EXTRA_CAMERA_FILE_DIR);
//        if (cameraFileDir != null) {
//            mPhotoHelper = new BGAPhotoHelper(cameraFileDir);
//        }
        selectedVoiceOrVideoList = new ArrayList<>();

        mPhotoHelper = new BGAPhotoHelper();

        // 获取图片选择的最大张数
        mMaxChooseCount = getIntent().getIntExtra(EXTRA_MAX_CHOOSE_COUNT, 1);
        if (mMaxChooseCount < 1) {
            mMaxChooseCount = 1;
        }

        // 获取右上角按钮文本
        mTopRightBtnText = getString(R.string.bga_pp_confirm);

        GridLayoutManager layoutManager = new GridLayoutManager(this, SPAN_COUNT, LinearLayoutManager.VERTICAL, false);
        mContentRv.setLayoutManager(layoutManager);
        mContentRv.addItemDecoration(BGAGridDivider.newInstanceWithSpaceRes(R.dimen.bga_pp_size_photo_divider));

        ArrayList<BGAMediaModel> selectedPhotos = getIntent().getParcelableArrayListExtra(EXTRA_SELECTED_PHOTOS);
        if (selectedPhotos != null && selectedPhotos.size() > mMaxChooseCount) {
            BGAMediaModel selectedPhoto = selectedPhotos.get(0);
            selectedPhotos.clear();
            selectedPhotos.add(selectedPhoto);
        }

        mContentRv.setAdapter(mPicAdapter);
        mPicAdapter.setSelectedPhotos(selectedPhotos);
    }

    @Override
    protected void onStart() {
        super.onStart();
        showLoadingDialog();
        mLoadMediaTask = new BGALoadMediaTask(this, this, mediaFileDir).perform();
    }

    private void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new AppCompatDialog(this);
            mLoadingDialog.setContentView(R.layout.bga_pp_dialog_loading);
            mLoadingDialog.setCancelable(false);
        }
        mLoadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bga_pp_menu_photo_picker, menu);
        MenuItem menuItem = menu.findItem(R.id.item_photo_picker_title);
        View actionView = menuItem.getActionView();

        mTitleTv = actionView.findViewById(R.id.tv_photo_picker_title);
        mArrowIv = actionView.findViewById(R.id.iv_photo_picker_arrow);
        mSubmitTv = actionView.findViewById(R.id.tv_photo_picker_submit);

        mTitleTv.setOnClickListener(mOnClickShowPhotoFolderListener);
        mArrowIv.setOnClickListener(mOnClickShowPhotoFolderListener);
        mSubmitTv.setOnClickListener(new BGAOnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                returnSelectedPhotos(mPicAdapter.getSelectedPhotos());
            }
        });

        mTitleTv.setText(R.string.bga_pp_all_image);
        if (mCurrentMedialFolderModel != null) {
            mTitleTv.setText(mCurrentMedialFolderModel.folderName);
        }

        renderTopRightBtn();

        return true;
    }

    /**
     * 返回已选中的图片集合
     *
     * @param selectedPhotos
     */
    private void returnSelectedPhotos(ArrayList<BGAMediaModel> selectedPhotos) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(EXTRA_SELECTED_PHOTOS, selectedPhotos);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void showPhotoFolderPw() {
        if (mArrowIv == null) {
            return;
        }

        if (mPhotoFolderPw == null) {
            mPhotoFolderPw = new BGAMediaFolderPw(this, mToolbar, new BGAMediaFolderPw.Delegate() {
                @Override
                public void onSelectedFolder(int position) {
                    reloadPhotos(position);
                }

                @Override
                public void executeDismissAnim() {
                    ViewCompat.animate(mArrowIv).setDuration(BGAPhotoFolderPw.ANIM_DURATION).rotation(0).start();
                }
            });
        }
        mPhotoFolderPw.setData(mPhotoFolderModels);
        mPhotoFolderPw.show();

        ViewCompat.animate(mArrowIv).setDuration(BGAPhotoFolderPw.ANIM_DURATION).rotation(-180).start();
    }

    /**
     * 显示只能选择 mMaxChooseCount 张图的提示
     */
    private void toastMaxCountTip() {
        BGAPhotoPickerUtil.show(getString(R.string.bga_pp_toast_photo_picker_max, mMaxChooseCount));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
                ArrayList<String> photos = new ArrayList<>(Arrays.asList(mPhotoHelper.getCameraFilePath()));

                Intent photoPickerPreview = new BGAPhotoPickerPreviewActivity.IntentBuilder(this)
                        .isFromTakePhoto(true)
                        .maxChooseCount(1)
                        .previewPhotos(photos)
                        .selectedPhotos(photos)
                        .currentPosition(0)
                        .build();
                startActivityForResult(photoPickerPreview, RC_PREVIEW);
            } else if (requestCode == RC_PREVIEW) {
                if (BGAPhotoPickerPreviewActivity.getIsFromTakePhoto(data)) {
                    // 从拍照预览界面返回，刷新图库
                    mPhotoHelper.refreshGallery();
                }

                ArrayList<BGAMediaModel> mediaModelList = new ArrayList<>();
                mediaModelList.addAll(selectedVoiceOrVideoList);
                for (String path : BGAPhotoPickerPreviewActivity.getSelectedPhotos(data)) {
                    BGAMediaModel item = new BGAMediaModel(BGAMediaModel.MEDIA_TYPE_PHOTO, path, path);
                    mediaModelList.add(item);
                }

//                for (int index = 0; index < mPicAdapter.getSelectedPhotos().size(); index++){
//
//                    if (mPicAdapter.getSelectedPhotos().get(index).getMediaType() != BGAMediaModel.MEDIA_TYPE_PHOTO)
//                        continue;
//
//                    String thumbnail = mPicAdapter.getSelectedPhotos().get(index).getThumbNail();
//                    boolean isExist = false;
//                    for (int num = 0; num < mediaModelList.size(); num++){
//
//                        if (thumbnail.equals(mediaModelList.get(num).getThumbNail())){
//                            isExist = true;
//                            break;
//                        }
//                    }
//
//                    if (!isExist)
//                        mPicAdapter.getSelectedPhotos().remove(index);
//                }
//
//                mPicAdapter.getSelectedPhotos().addAll(mediaModelList);

                mPicAdapter.setSelectedPhotos(mediaModelList);

                returnSelectedPhotos(mPicAdapter.getSelectedPhotos());
            }
        } else if (resultCode == RESULT_CANCELED && requestCode == RC_PREVIEW) {
            if (BGAPhotoPickerPreviewActivity.getIsFromTakePhoto(data)) {
                // 从拍照预览界面返回，删除之前拍的照片
                mPhotoHelper.deleteCameraFile();
            } else {

                ArrayList<BGAMediaModel> mediaModelList = new ArrayList<>();
                mediaModelList.addAll(selectedVoiceOrVideoList);
                for (String path : BGAPhotoPickerPreviewActivity.getSelectedPhotos(data)) {
                    BGAMediaModel item = new BGAMediaModel(BGAMediaModel.MEDIA_TYPE_PHOTO, path, path);
                    mediaModelList.add(item);
                }

//                for (int index = 0; index < mPicAdapter.getSelectedPhotos().size(); index++){
//
//                    if (mPicAdapter.getSelectedPhotos().get(index).getMediaType() != BGAMediaModel.MEDIA_TYPE_PHOTO)
//                        continue;
//
//                    String thumbnail = mPicAdapter.getSelectedPhotos().get(index).getThumbNail();
//                    boolean isExist = false;
//                    for (int num = 0; num < mediaModelList.size(); num++){
//
//                        if (thumbnail.equals(mediaModelList.get(num).getThumbNail())){
//                            isExist = true;
//                            mediaModelList.remove(num);
//                            break;
//                        }
//                    }
//
//                    if (!isExist)
//                        mPicAdapter.getSelectedPhotos().remove(index);
//                }
//
//                mPicAdapter.getSelectedPhotos().addAll(mediaModelList);

                mPicAdapter.setSelectedPhotos(mediaModelList);
                renderTopRightBtn();
            }
        }
    }

    /**
     * 渲染右上角按钮
     */
    private void renderTopRightBtn() {
        if (mSubmitTv == null) {
            return;
        }

        if (mPicAdapter.getSelectedCount() == 0) {
            mSubmitTv.setEnabled(false);
            mSubmitTv.setText(mTopRightBtnText);
        } else {
            mSubmitTv.setEnabled(true);
            mSubmitTv.setText(mTopRightBtnText + "(" + mPicAdapter.getSelectedCount() + "/" + mMaxChooseCount + ")");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        BGAPhotoHelper.onSaveInstanceState(mPhotoHelper, outState);
        outState.putParcelableArrayList(STATE_SELECTED_PHOTOS, mPicAdapter.getSelectedPhotos());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        BGAPhotoHelper.onRestoreInstanceState(mPhotoHelper, savedInstanceState);
        mPicAdapter.setSelectedPhotos(savedInstanceState.<BGAMediaModel>getParcelableArrayList(STATE_SELECTED_PHOTOS));
    }

    @Override
    public void onItemChildClick(ViewGroup viewGroup, View view, int position) {
        if (view.getId() == R.id.iv_item_photo_picker_photo) {
            changeToPreview(position);
        } else if (view.getId() == R.id.iv_item_photo_picker_flag) {
            handleClickSelectFlagIv(position);
        }
    }

    /**
     * 跳转到图片选择预览界面
     *
     * @param position 当前点击的item的索引位置
     */
    private void changeToPreview(int position) {

        int mediaType = mPicAdapter.getData().get(position).getMediaType();

        switch (mediaType){
            case BGAMediaModel.MEDIA_TYPE_PHOTO:
                //打开图片预览器
                ArrayList<String> photoModels = new ArrayList<>();
                int notPhotoCount = 0;
                int photoSize = 0;
                int currentPhotoPosition = 0;
                for (int index = 0; index < mPicAdapter.getData().size(); index++) {
                    BGAMediaModel item = mPicAdapter.getData().get(index);
                    if (item.getMediaType() == BGAMediaModel.MEDIA_TYPE_PHOTO){
                        photoModels.add(item.getThumbNail());
                        if (index == position)
                            currentPhotoPosition = photoSize;

                        photoSize ++;
                    }
                }

                //int currentPosition = position;
                ArrayList<String> selectedPhotoModels = new ArrayList<>();
                selectedVoiceOrVideoList.clear();
                for (int num = 0; num < mPicAdapter.getSelectedPhotos().size(); num++){
                    BGAMediaModel item = mPicAdapter.getSelectedPhotos().get(num);
                    if (item.getMediaType() == BGAMediaModel.MEDIA_TYPE_PHOTO){
                        selectedPhotoModels.add(item.getThumbNail());
                    }else{
                        notPhotoCount ++;
                        selectedVoiceOrVideoList.add(item);
                    }

                }

                Intent photoPickerPreviewIntent = new BGAPhotoPickerPreviewActivity.IntentBuilder(this)
                        .previewPhotos(photoModels)
                        .selectedPhotos(selectedPhotoModels)
                        .maxChooseCount(mMaxChooseCount - notPhotoCount)
                        .currentPosition(currentPhotoPosition)
                        .isFromTakePhoto(false)
                        .build();
                startActivityForResult(photoPickerPreviewIntent, RC_PREVIEW);
                break;
            case BGAMediaModel.MEDIA_TYPE_VOICE:
                //播放音频预览
                new VoiceProgressDialog(this, mPicAdapter.getData().get(position).getMediaUrl())
                        .builder()
                        .setCancelable(false)
                        .show();
                break;
            case BGAMediaModel.MEDIA_TYPE_VIDEO:
                //播放视频
                try {
                    File file = new File(mPicAdapter.getData().get(position).getMediaUrl());
                    if (!file.exists()) {
                        return;
                    }
                    Log.i("BGAMediaPickerActivity","path = " + mPicAdapter.getData().get(position).getMediaUrl());
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //设置intent的data和Type属性。
                    Uri uri = mPhotoHelper.createFileUri(file);
                    String extension = MimeTypeMap.getFileExtensionFromUrl( mPicAdapter.getData().get(position).getMediaUrl());
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                    intent.setDataAndType(uri, mimeType);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.i("BGAMediaPickerActivity","openFile e = " + e.getMessage());
                }
                break;
        }

    }

    /**
     * 处理点击选择按钮事件
     *
     * @param position 当前点击的item的索引位置
     */
    private void handleClickSelectFlagIv(int position) {
        BGAMediaModel currentPhoto = mPicAdapter.getItem(position);
        if (mMaxChooseCount == 1) {
            // 单选

            if (mPicAdapter.getSelectedCount() > 0) {
                BGAMediaModel selectedPhoto = mPicAdapter.getSelectedPhotos().remove(0);
                if (TextUtils.equals(selectedPhoto.getMediaUrl(), currentPhoto.getMediaUrl())) {
                    mPicAdapter.notifyItemChanged(position);
                } else {
                    int preSelectedPhotoPosition = mPicAdapter.getData().indexOf(selectedPhoto);
                    mPicAdapter.notifyItemChanged(preSelectedPhotoPosition);
                    mPicAdapter.getSelectedPhotos().add(currentPhoto);
                    mPicAdapter.notifyItemChanged(position);
                }
            } else {
                mPicAdapter.getSelectedPhotos().add(currentPhoto);
                mPicAdapter.notifyItemChanged(position);
            }
            renderTopRightBtn();
        } else {
            // 多选

            if (!mPicAdapter.getSelectedPhotos().contains(currentPhoto) && mPicAdapter.getSelectedCount() == mMaxChooseCount) {
                toastMaxCountTip();
            } else {
                if (mPicAdapter.getSelectedPhotos().contains(currentPhoto)) {
                    mPicAdapter.getSelectedPhotos().remove(currentPhoto);
                } else {
                    mPicAdapter.getSelectedPhotos().add(currentPhoto);
                }
                mPicAdapter.notifyItemChanged(position);

                renderTopRightBtn();
            }
        }
    }

    private void reloadPhotos(int position) {
        if (position < mPhotoFolderModels.size()) {
            mCurrentMedialFolderModel = mPhotoFolderModels.get(position);
            if (mTitleTv != null) {
                mTitleTv.setText(mCurrentMedialFolderModel.folderName);
            }

            mPicAdapter.setPhotoFolderModel(mCurrentMedialFolderModel);
        }
    }

    @Override
    public void onPostExecute(ArrayList<BGAMediaFolderModel> bgaMediaFolderModels) {
        dismissLoadingDialog();
        mLoadMediaTask = null;
        mPhotoFolderModels = bgaMediaFolderModels;
        reloadPhotos(mPhotoFolderPw == null ? 0 : mPhotoFolderPw.getCurrentPosition());
    }

    @Override
    public void onTaskCancelled() {
        dismissLoadingDialog();
        mLoadMediaTask = null;
    }

    private void cancelLoadPhotoTask() {
        if (mLoadMediaTask != null) {
            mLoadMediaTask.cancelTask();
            mLoadMediaTask = null;
        }
    }

    @Override
    protected void onDestroy() {
        dismissLoadingDialog();
        cancelLoadPhotoTask();

        super.onDestroy();
    }
}
