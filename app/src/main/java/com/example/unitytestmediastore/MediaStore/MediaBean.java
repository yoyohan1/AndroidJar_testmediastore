package com.example.unitytestmediastore.MediaStore;

/**
 * Created by Administrator on 2019/2/25.
 */
public class MediaBean {

    public String path;
    public long size;
    public String displayName;
    //视频缩略图路径，不一定全都有，如果没有可以调取MediaControl.getVideoThumbnailBytes 获取
    public String videoThumbnail;
    public int width;
    public int height;

    public MediaBean(String path, long size, String displayName) {
        this.path = path;
        this.size = size;
        this.displayName = displayName;
    }

    public MediaBean SetWidth(int width) {
        this.width = width;
        return this;
    }

    public MediaBean SetHeight(int height) {
        this.height = height;
        return this;
    }

    @Override
    public String toString() {
        return "name---->" + displayName + "\nsize---->" + size + "\npath----->" + path + "\nvideoThumbnail----->" + videoThumbnail;
    }
}
