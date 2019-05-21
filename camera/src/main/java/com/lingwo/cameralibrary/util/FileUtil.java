package com.lingwo.cameralibrary.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.1.4
 * 创建日期：2017/4/25
 * 描    述：
 * =====================================
 */
public class FileUtil {
    private static final String TAG = "CJT";
    private static final File parentPath = Environment.getExternalStorageDirectory();
    private static String storagePath = "";
    public static String DST_FOLDER_NAME = "KJCamera";

    private static String initPath(String dir) {
        storagePath = parentPath.getAbsolutePath() + File.separator + DST_FOLDER_NAME + dir;
        File f = new File(storagePath);
        if (!f.exists()) {
            f.mkdir();
        }
        return storagePath;
    }

    public static String saveCaptureBitmap(Context context, Bitmap b){
        String path = initPath("/WaterMask");
        return saveBitmap(context, path, b);
    }

    public static String saveVideoFirstFrame(Context context, Bitmap b){
        String path = initPath("/Video/PreviewVideo");
        return saveBitmap(context, path, b);
    }

    public static String saveVoiceBitmap(Context context, Bitmap b){
        String path = initPath("/Voice/PreviewVoice");
        return saveBitmap(context, path, b);
    }

    private static String saveBitmap(Context context, String dir, Bitmap b) {

        long dataTake = System.currentTimeMillis();
        String jpegName = dir + File.separator + "picture_" + dataTake + ".jpg";
        File file = new File(jpegName);
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(Uri.fromFile(file)));
            return jpegName;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean deleteFile(String url) {
        boolean result = false;
        File file = new File(url);
        if (file.exists()) {
            result = file.delete();
        }
        return result;
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
