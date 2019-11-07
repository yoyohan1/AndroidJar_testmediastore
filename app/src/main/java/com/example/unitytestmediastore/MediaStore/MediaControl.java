package com.example.unitytestmediastore.MediaStore;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;

/**
 * Created by Administrator on 2019/4/27.
 */

public class MediaControl {

    private static final String TAG = "MediaControl";
    private static MediaControl instance;
    private Context context;

    public static MediaControl getInstance(Context context) {
        if (instance == null)
            instance = new MediaControl(context);
        return instance;
    }

    private MediaControl(Context context) {
        this.context = context;
        if (context == null) {
            throw new RuntimeException("context is null");
        }
    }

    /**
     * 获取手机所有图片,
     *
     * @param callBack
     */
    public void getAllPhotos(final IMediaDataCallback callBack) {
        getMediaDataWithType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, callBack);
    }

    /**
     * 获取手机所有视频
     *
     * @param callBack
     */
    public void getAllVideo(final IMediaDataCallback callBack) {
        getMediaDataWithType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, callBack);
    }


    /**
     * 获取压缩图片
     *
     * @param path
     */
    public void getPhotoThumbnailFile(final String path, final IMediaThumbnailCallback callback) {
        Luban.with(context).load(path).filter(new CompressionPredicate() {
            @Override
            public boolean apply(String path) {
                return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
            }
        }).setCompressListener(new OnCompressListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(File file) {
                if (callback != null) {
                    callback.onMediaThumbnailCallback(file.getPath());
                }
            }

            @Override
            public void onError(Throwable e) {
                if (callback != null) {
                    callback.onMediaThumbnailCallback(path);
                }
            }
        }).launch();
    }


    /**
     * 获取视频的缩略图
     *
     * @param path
     */
    public void getVideoThumbnailBytes(final String path, final IVideoThumbnailCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String outPath = "";

                try {
                    Bitmap videoThumbnail = ThumbnailUtils.createVideoThumbnail(path, MINI_KIND);

                    if (videoThumbnail != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        videoThumbnail.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        outPath = path + ".png";

                        FileOutputStream fos = new FileOutputStream(outPath);
                        fos.write(baos.toByteArray());
                        fos.close();
                        baos.close();
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (callback != null) {
                    callback.onVideoThumbnailCallback(outPath);
                }
            }
        }).start();

    }


    /**
     * 根据多媒体类型获取多媒体的列表
     *
     * @param uri
     * @param callBack
     */
    private void getMediaDataWithType(final Uri uri, final IMediaDataCallback callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<MediaBean> medias = new ArrayList<>();
                Cursor cursor = context.getContentResolver()
                        .query(uri, null, null, null, null);
                while (cursor.moveToNext()) {
                    //获取图片的名称
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                    //获取图片的生成日期
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    //获取图片的详细信息
                    long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));

                    //视频缩略图路径
                    String albumPath = "";
                    if (uri == MediaStore.Video.Media.EXTERNAL_CONTENT_URI) {
                        //获取视频的缩略图
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));

                        Cursor thumbCursor = context.getApplicationContext().getContentResolver().query(
                                MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                                null, MediaStore.Video.Thumbnails.VIDEO_ID
                                        + "=" + id, null, null);
                        if (thumbCursor.moveToFirst()) {
                            albumPath = thumbCursor.getString(thumbCursor
                                    .getColumnIndex(MediaStore.Video.Thumbnails.DATA));
                        }
                    }

                    if (size != 0) {
                        MediaBean mediaBean = new MediaBean(path, size, name);
                        mediaBean.videoThumbnail = albumPath;
                        Log.e(TAG, "media ---->" + mediaBean.toString());
                        medias.add(mediaBean);
                    }
                }
                cursor.close();
                if (callBack != null) {
                    callBack.onGetMediaDataCallBack(medias);
                }

                Log.e(TAG, "media size---->" + medias.size());
            }
        }).start();
    }
}
