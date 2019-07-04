package com.example.testmediastore.MediaStore;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.testmediastore.MainActivity;
import com.google.gson.Gson;
import com.unity3d.player.UnityPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AudioMediaStore {

    private static Gson gson = new Gson();

    public void GetAllAudioInfo() {
        Log.i("cxs", "开始获取音乐！");
        final Activity mUnityPlayer = UnityPlayer.currentActivity;
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<MediaBean> mediaBeen = new ArrayList<MediaBean>();
                HashMap<String, List<MediaBean>> allPhotosTemp = new HashMap<>();//所有照片
                Uri mImageUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] projImage = {MediaStore.Audio.Media._ID
                        , MediaStore.Audio.Media.DATA
                        , MediaStore.Audio.Media.SIZE
                        , MediaStore.Audio.Media.DISPLAY_NAME};

                final  Cursor mCursor = mUnityPlayer.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                        null, MediaStore.Audio.AudioColumns.IS_MUSIC);

//                final Cursor mCursor = mUnityPlayer.getContentResolver().query(mImageUri,
//                        projImage,
//                        MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE+ "=? or "+ MediaStore.Images.Media.MIME_TYPE+ "=?",
//                        new String[]{"image/jpeg", "image/png", "image/jpg","image/bmp"},
//                        MediaStore.Images.Media.DATE_MODIFIED + " desc");

                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        // 获取图片的路径
                        String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                        int size = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media.SIZE)) / 1024;
                        String displayName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
//                        //用于展示相册初始化界面
//                        if (path.contains("/storage/emulated/0/messageBoard/photoImgs")) {
//                            mediaBeen.add(new MediaBean(path, size, displayName).SetWidth(width).SetHeight(height));
//                        }

                        // 获取该图片的父路径名
                        String dirPath = new File(path).getParentFile().getAbsolutePath();

                        //存储对应关系
                        if (allPhotosTemp.containsKey(dirPath)) {
                            List<MediaBean> data = allPhotosTemp.get(dirPath);
                            data.add(new MediaBean(path, size, displayName));
                            //Log.i("cxs","getAllPhotoInfo "+data.size()+",path="+data.get(0).path+",name="+data.get(0).displayName);
                            continue;
                        } else {
                            List<MediaBean> data = new ArrayList<>();
                            data.add(new MediaBean(path, size, displayName));
                            allPhotosTemp.put(dirPath, data);
                            //Log.i("cxs","getAllPhotoInfo else "+data.size()+",path="+data.get(0).path+",name="+data.get(0).displayName);
                        }
                    }
                    mCursor.close();
                }
                MainActivity.getInstance().SendMessageToUnity(5, gson.toJson(allPhotosTemp), 0);
            }
        }).start();
    }

}
