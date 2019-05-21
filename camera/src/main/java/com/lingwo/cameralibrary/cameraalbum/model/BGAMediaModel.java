package com.lingwo.cameralibrary.cameraalbum.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @Description: 九宫格多媒体Model
 * @author: xiaji
 * @date: 2019-4-17 9:32
 * Copyright (c) 2017, Kumi. All rights reserved
 */
public class BGAMediaModel implements Parcelable {

    public final static int MEDIA_TYPE_VIDEO = 1;
    public final static int MEDIA_TYPE_VOICE = 2;
    public final static int MEDIA_TYPE_FILE = 3;
    public final static int MEDIA_TYPE_PHOTO = 4;

    private int mediaType;
    private String thumbNail;
    private String mediaUrl;

    public BGAMediaModel(int mediaType, String thumbNail, String mediaUrl){

        this.mediaType = mediaType;
        this.thumbNail = thumbNail;
        this.mediaUrl = mediaUrl;
    }

    protected BGAMediaModel(Parcel in) {
        mediaType = in.readInt();
        thumbNail = in.readString();
        mediaUrl = in.readString();
    }

    public static final Creator<BGAMediaModel> CREATOR = new Creator<BGAMediaModel>() {
        @Override
        public BGAMediaModel createFromParcel(Parcel in) {
            return new BGAMediaModel(in);
        }

        @Override
        public BGAMediaModel[] newArray(int size) {
            return new BGAMediaModel[size];
        }
    };

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public String getThumbNail() {
        return thumbNail;
    }

    public void setThumbNail(String thumbNail) {
        this.thumbNail = thumbNail;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mediaType);
        parcel.writeString(thumbNail);
        parcel.writeString(mediaUrl);
    }

    @Override
    public boolean equals(Object obj) {

        BGAMediaModel inItem = (BGAMediaModel)obj;
        return thumbNail.equals(inItem.getThumbNail());
    }
}
