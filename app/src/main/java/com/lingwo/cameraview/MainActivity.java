package com.lingwo.cameraview;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.lingwo.cameralibrary.cameraalbum.activity.BGAMediaPickerActivity;
import com.lingwo.cameralibrary.cameraalbum.activity.BGAPhotoPickerPreviewActivity;
import com.lingwo.cameralibrary.cameraalbum.model.BGAMediaModel;
import com.lingwo.cameralibrary.cameraalbum.widget.BGASortableNineMediaLayout;
import com.lingwo.cameralibrary.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private final int GET_PERMISSION_REQUEST = 100; //权限申请自定义码

    @BindView(R.id.btnCapture)
    Button btnCapture;
    @BindView(R.id.btnCaptureVideo)
    Button btnCaptureVideo;
    @BindView(R.id.nineMediaEditLayout)
    BGASortableNineMediaLayout nineMediaEditLayout;

    private ArrayList<BGAMediaModel> mediaUrls = new ArrayList<>();

    private String photoPath;
    private String videoPath;

    private Dialog mediaBottomDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getVideoRecordPermissions();
            }
        });

        btnCaptureVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCapturePermissions();
            }
        });

        nineMediaEditLayout.setData(null);
        nineMediaEditLayout.setMaxItemCount(9);
        nineMediaEditLayout.setEditable(true);
        nineMediaEditLayout.setPlusEnable(true);
        nineMediaEditLayout.setSortable(false);

        nineMediaEditLayout.setDelegate(new BGASortableNineMediaLayout.Delegate() {
            @Override
            public void onClickAddNinePhotoItem(BGASortableNineMediaLayout sortableNineMediaLayout, View view, int position, ArrayList<BGAMediaModel> models) {
                showBottomDialog();
            }

            @Override
            public void onClickDeleteNinePhotoItem(BGASortableNineMediaLayout sortableNineMediaLayout, View view, int position, BGAMediaModel model, ArrayList<BGAMediaModel> models) {

                nineMediaEditLayout.removeItem(position);
                mediaUrls = nineMediaEditLayout.getData();
            }

            @Override
            public void onClickNinePhotoItem(BGASortableNineMediaLayout sortableNineMediaLayout, View view, int position, BGAMediaModel model, ArrayList<BGAMediaModel> models) {

                switch (model.getMediaType()){
                    case BGAMediaModel.MEDIA_TYPE_PHOTO:

                        ArrayList<String> photoModels = new ArrayList<>();
                        int notPhotoCount = 0;
                        int photoSize = 0;
                        int currentPhotoPosition = 0;
                        for (int index = 0; index < models.size(); index++) {
                            BGAMediaModel item = models.get(index);
                            if (item.getMediaType() == BGAMediaModel.MEDIA_TYPE_PHOTO){
                                photoModels.add(item.getThumbNail());
                                if (index == position)
                                    currentPhotoPosition = photoSize;

                                photoSize ++;
                            }
                            else
                                notPhotoCount ++;
                        }
                        Intent photoPickerPreviewIntent = new BGAPhotoPickerPreviewActivity.IntentBuilder(MainActivity.this)
                                .previewPhotos(photoModels) // 当前预览的图片路径集合
                                .selectedPhotos(photoModels) // 当前已选中的图片路径集合
                                .maxChooseCount(nineMediaEditLayout.getMaxItemCount() - notPhotoCount) // 图片选择张数的最大值
                                .currentPosition(currentPhotoPosition) // 当前预览图片的索引
                                .isFromTakePhoto(false) // 是否是拍完照后跳转过来
                                .build();
                        startActivityForResult(photoPickerPreviewIntent, 1003);
                        break;
                    case BGAMediaModel.MEDIA_TYPE_VOICE:
                        break;
                    case BGAMediaModel.MEDIA_TYPE_VIDEO:

                        //播放视频
                        try {
                            File file = new File(model.getMediaUrl());
                            if (!file.exists()) {
                                return;
                            }
                            Log.i("MainActivity","path = " + model.getMediaUrl());
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            //设置intent的data和Type属性。
                            Uri uri;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                //适配Android 7.0
                                uri = FileProvider.getUriForFile(MainActivity.this, "com.lingwo.cameraview.provider", file);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } else {
                                uri = Uri.fromFile(file);
                            }
                            String extension = MimeTypeMap.getFileExtensionFromUrl(model.getMediaUrl());
                            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                            intent.setDataAndType(uri, mimeType);
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.i("MainActivity","openFile e = " + e.getMessage());
                        }
                        break;
                }
            }

            @Override
            public void onNinePhotoItemExchanged(BGASortableNineMediaLayout sortableNineMediaLayout, int fromPosition, int toPosition, ArrayList<BGAMediaModel> models) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1001) {

            ArrayList<BGAMediaModel> selectedPhotos = BGAMediaPickerActivity.getSelectedPhotos(data);
            nineMediaEditLayout.addMoreData(selectedPhotos);
            mediaUrls = nineMediaEditLayout.getData();

        }else if (resultCode == RESULT_OK && requestCode == 1002){

            photoPath = data.getStringExtra("photoPath");
            videoPath = data.getStringExtra("videoPath");
            boolean isTakePhoto = data.getBooleanExtra("isPhoto", true);

            ArrayList<BGAMediaModel> mediaList = new ArrayList<>();
            BGAMediaModel item = null;
            if (isTakePhoto)
                item = new BGAMediaModel(BGAMediaModel.MEDIA_TYPE_PHOTO, photoPath, photoPath);
            else
                item = new BGAMediaModel(BGAMediaModel.MEDIA_TYPE_VIDEO, photoPath, videoPath);

            mediaList.add(item);
            nineMediaEditLayout.addMoreData(mediaList);

            mediaUrls = nineMediaEditLayout.getData();

        }else if (requestCode == 1003){

            // 选中后点击图片预览修改后的结果
            ArrayList<BGAMediaModel> mediaModelList = new ArrayList<>();
            for (String path : BGAPhotoPickerPreviewActivity.getSelectedPhotos(data)) {
                BGAMediaModel item = new BGAMediaModel(BGAMediaModel.MEDIA_TYPE_PHOTO, path, path);
                mediaModelList.add(item);
            }

            for (int index = 0; index < nineMediaEditLayout.getData().size(); index++){

                if (nineMediaEditLayout.getData().get(index).getMediaType() != BGAMediaModel.MEDIA_TYPE_PHOTO)
                    continue;
                String thumbnail = nineMediaEditLayout.getData().get(index).getThumbNail();
                boolean isExist = false;
                for (int num = 0; num < mediaModelList.size(); num++){

                    if (thumbnail.equals(mediaModelList.get(num).getThumbNail())){
                        isExist = true;
                        break;
                    }
                }

                if (!isExist)
                    nineMediaEditLayout.getData().remove(index);
            }

            nineMediaEditLayout.setData(nineMediaEditLayout.getData());

            mediaUrls = nineMediaEditLayout.getData();
        }
    }

    /**
     * 系统相册选择
     */
    @AfterPermissionGranted(2011)
    private void chooseSystemPhotoWrapper(){
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

        if (EasyPermissions.hasPermissions(this, perms)){

            Intent photoPickerIntent = new BGAMediaPickerActivity.IntentBuilder(this)
                    .mediaFileDir(FileUtil.DST_FOLDER_NAME)
                    .maxChooseCount(nineMediaEditLayout.getMaxItemCount() - nineMediaEditLayout.getItemCount())
                    .selectedPhotos(null)
                    .pauseOnScroll(false)
                    .build();

            startActivityForResult(photoPickerIntent, 1001);
        }else
            EasyPermissions.requestPermissions(this, "系统相册需要以下权限：\n\n1、访问设备存储权限\n\n2、摄像头权限", 2011, perms);
    }

    /**
     * 拍照、短视频
     */
    @AfterPermissionGranted(2010)
    private void getCapturePermissions(){
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)){

            startActivity(new Intent(MainActivity.this, JCameraActivity.class));
        }else {

            EasyPermissions.requestPermissions(this,
                    "视频拍摄需要以下权限：\n\n1、访问设备存储权限\n\n2、访问麦克风权限\n\n3、摄像头权限", 2010, perms);
        }

    }

    /**
     * 视频拍摄、拍照截图
     */
    @AfterPermissionGranted(2009)
    private void getVideoRecordPermissions() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)){

            startActivity(new Intent(MainActivity.this, KCameraActivity.class));
        }else {

            EasyPermissions.requestPermissions(this,
                    "视频拍摄需要以下权限：\n\n1、访问设备存储权限\n\n2、访问麦克风权限\n\n3、摄像头权限", 2009, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (requestCode == 2009) {
            Toast.makeText(this, "您拒绝了「视频拍摄」所需要的相关权限!", Toast.LENGTH_SHORT).show();
        }else if (requestCode == 2010)
            Toast.makeText(this, "您拒绝了「图片选择」所需要的相关权限!", Toast.LENGTH_SHORT).show();
    }

    private void showBottomDialog(){

        if (mediaBottomDialog == null){

            mediaBottomDialog = new Dialog(this, R.style.BottomDialog);
            LinearLayout dialogView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.dialog_bottom, null);

            dialogView.findViewById(R.id.txt_take_picture).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    mediaBottomDialog.dismiss();
                    //startActivityForResult(new Intent(MainActivity.this, ));
                }
            });

            dialogView.findViewById(R.id.txt_choice_photo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    mediaBottomDialog.dismiss();
                    chooseSystemPhotoWrapper();
                }
            });

            dialogView.findViewById(R.id.txt_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mediaBottomDialog.dismiss();
                }
            });

            mediaBottomDialog.setContentView(dialogView);
            Window dialogWindow = mediaBottomDialog.getWindow();
            dialogWindow.setGravity(Gravity.BOTTOM);

            WindowManager.LayoutParams layoutParams = dialogWindow.getAttributes();
//            layoutParams.x = 0; // 新位置X坐标
//            layoutParams.y = 0; // 新位置Y坐标
            layoutParams.width = (int) getResources().getDisplayMetrics().widthPixels;
//            dialogView.measure(0, 0);
//            layoutParams.height = dialogView.getMeasuredHeight();

            dialogWindow.setAttributes(layoutParams);
        }

        mediaBottomDialog.show();
    }
}
