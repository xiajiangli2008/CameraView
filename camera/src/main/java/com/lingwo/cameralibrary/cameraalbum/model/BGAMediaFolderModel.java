package com.lingwo.cameralibrary.cameraalbum.model;

import java.util.ArrayList;

/**
 * @Description: 多媒体文件夹Model
 * @author: xiaji
 * @date: 2019-4-29 15:46
 * Copyright (c) 2017, Kumi. All rights reserved
 */
public class BGAMediaFolderModel {

    public String folderName;
    public String coverPath;
    private ArrayList<BGAMediaModel> mPhotos = new ArrayList<>();

    public BGAMediaFolderModel(){

    }

    public BGAMediaFolderModel(String folderName, String coverPath){
        this.folderName = folderName;
        this.coverPath = coverPath;
    }

    public void addLastPhoto(BGAMediaModel mediaModel){
        mPhotos.add(mediaModel);
    }

    public ArrayList<BGAMediaModel> getPhotos(){
        return mPhotos;
    }

    public int getCount(){
        return mPhotos.size();
    }
}
