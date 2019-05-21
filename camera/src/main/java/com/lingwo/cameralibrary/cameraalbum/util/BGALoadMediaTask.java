package com.lingwo.cameralibrary.cameraalbum.util;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.lingwo.cameralibrary.R;
import com.lingwo.cameralibrary.cameraalbum.model.BGAMediaFolderModel;
import com.lingwo.cameralibrary.cameraalbum.model.BGAMediaModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 异步加载本地所有媒体文件任务
 * @author: xiaji
 * @date: 2019-4-29 15:46
 * Copyright (c) 2017, Lingwo. All rights reserved
 */
public class BGALoadMediaTask extends BGAAsyncTask<Void, ArrayList<BGAMediaFolderModel>> {

    private Context mContext;
    private String mediaFileDir;

    public BGALoadMediaTask(Callback<ArrayList<BGAMediaFolderModel>> callback, Context context){
        super(callback);
        mContext = context.getApplicationContext();
    }

    public BGALoadMediaTask(Callback<ArrayList<BGAMediaFolderModel>> callback, Context context, String mediaFileDir){
        this(callback, context);
        this.mediaFileDir = mediaFileDir;
    }

    private static boolean isNotImageFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return true;
        }

        File file = new File(path);
        return !file.exists() || file.length() == 0;

        // 获取图片的宽和高，但不把图片加载到内存中
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(path, options);
//        return options.outMimeType == null;
    }

    @Override
    protected ArrayList<BGAMediaFolderModel> doInBackground(Void... voids) {

        ArrayList<BGAMediaFolderModel> imageFolderModels = new ArrayList<>();

        BGAMediaFolderModel allImageFolderModel = new BGAMediaFolderModel();
        allImageFolderModel.folderName = mContext.getString(R.string.bga_pp_all_image);
        imageFolderModels.add(allImageFolderModel);

        Map<String, BGAMediaFolderModel> imageFolderModelMap = new HashMap<>();

        Cursor cursor = null;

        try {
            cursor = mContext.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media.DATA},
                    MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                    new String[]{"image/jpeg", "image/png", "image/jpg"},
                    MediaStore.Images.Media.DATE_ADDED + " DESC"
            );

            BGAMediaFolderModel otherImageFolderModel;
            if (cursor != null && cursor.getCount() > 0){
                boolean firstInto = true;
                while (cursor.moveToNext()){

                    BGAMediaModel mediaModel = null;
                    String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

                    if (imagePath.startsWith(Environment.getExternalStorageDirectory() + File.separator + mediaFileDir)){

                        if (isNotImageFile(imagePath))
                            continue;

                        if (imagePath.startsWith(Environment.getExternalStorageDirectory() + File.separator + mediaFileDir + File.separator + "WaterMask")){
                            //纯图片媒体
                            if (firstInto){
                                allImageFolderModel.coverPath = imagePath;
                                firstInto = false;
                            }

                            mediaModel = new BGAMediaModel(BGAMediaModel.MEDIA_TYPE_PHOTO, imagePath, imagePath);
                        }else if (imagePath.startsWith(Environment.getExternalStorageDirectory() + File.separator + mediaFileDir + File.separator + "Voice")){

                            //音频媒体
                            String filename = imagePath.substring(imagePath.lastIndexOf("/")+1, imagePath.lastIndexOf("."));
                            String filePath = Environment.getExternalStorageDirectory() + File.separator + mediaFileDir + File.separator + "Voice" + File.separator + filename + ".amr";
                            File file = new File(filePath);
                            if (file.exists()){
                                if (firstInto){
                                    allImageFolderModel.coverPath = imagePath;
                                    firstInto = false;
                                }

                                mediaModel = new BGAMediaModel(BGAMediaModel.MEDIA_TYPE_VOICE, imagePath, filePath);
                            }else {
                                continue;
                            }
                        }else if (imagePath.startsWith(Environment.getExternalStorageDirectory() + File.separator + mediaFileDir + File.separator + "Video")){

                            //视频媒体
                            String filename = imagePath.substring(imagePath.lastIndexOf("/")+1, imagePath.lastIndexOf("."));
                            String filePath = Environment.getExternalStorageDirectory() + File.separator + mediaFileDir + File.separator + "Video" + File.separator + filename + ".mp4";
                            File file = new File(filePath);
                            if (file.exists()){
                                if (firstInto){
                                    allImageFolderModel.coverPath = imagePath;
                                    firstInto = false;
                                }

                                mediaModel = new BGAMediaModel(BGAMediaModel.MEDIA_TYPE_VIDEO, imagePath, filePath);
                            }else {
                                continue;
                            }
                        }

                        //所有媒体文件目录每次都添加
                        allImageFolderModel.addLastPhoto(mediaModel);

                        String folderPath = null;
                        //其他媒体文件目录
                        File folder = new File(imagePath).getParentFile();
                        if (folder != null){
                            folderPath = folder.getAbsolutePath();
                        }

                        if (TextUtils.isEmpty(folderPath)) {
                            int end = imagePath.lastIndexOf(File.separator);
                            if (end != -1) {
                                folderPath = imagePath.substring(0, end);
                            }
                        }

                        if (!TextUtils.isEmpty(folderPath)){
                            if (imageFolderModelMap.containsKey(folderPath)){
                                otherImageFolderModel = imageFolderModelMap.get(folderPath);
                            }else {
                                String folderName = folderPath.substring(folderPath.lastIndexOf(File.separator) + 1);
                                if (TextUtils.isEmpty(folderName)){
                                    folderName = "/";
                                }

                                otherImageFolderModel = new BGAMediaFolderModel(folderName, imagePath);
                                imageFolderModelMap.put(folderPath, otherImageFolderModel);
                            }
                            otherImageFolderModel.addLastPhoto(mediaModel);
                        }
                    }
                }

                //添加其他媒体文件目录
                imageFolderModels.addAll(imageFolderModelMap.values());
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null)
                cursor.close();
        }

        return imageFolderModels;
    }

    public BGALoadMediaTask perform(){
        if (Build.VERSION.SDK_INT >= 11)
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            execute();
        return this;
    }
}
