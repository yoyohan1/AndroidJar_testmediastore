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

public class VideoMediaStore {

    private static Gson gson = new Gson();

    public void GetAllMovieInfo() {
        Log.i("cxs", "开始获取视频！");
        final Activity mUnityPlayer = UnityPlayer.currentActivity;
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<MediaBean> mediaBeen = new ArrayList<MediaBean>();
                HashMap<String, List<MediaBean>> allPhotosTemp = new HashMap<>();//所有照片
                Uri mImageUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] projImage = {MediaStore.Video.Media._ID
                        , MediaStore.Video.Media.DATA
                        , MediaStore.Video.Media.SIZE
                        , MediaStore.Video.Media.DISPLAY_NAME};
                final Cursor mCursor = mUnityPlayer.getContentResolver().query(mImageUri,
                        projImage,
                        MediaStore.Video.Media.MIME_TYPE + "=? or " + MediaStore.Video.Media.MIME_TYPE + "=? or " + MediaStore.Video.Media.MIME_TYPE + "=? or " + MediaStore.Video.Media.MIME_TYPE + "=?",
                        new String[]{"video/mp4", "video/mkv", "video/webm", "video/avi"},
                        MediaStore.Video.Media.DATE_MODIFIED + " desc");

                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        // 获取图片的路径
                        String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Media.DATA));
                        int size = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Video.Media.SIZE)) / 1024;
                        String displayName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                        //用于展示相册初始化界面
//                        if (path.contains("/storage/emulated/0/messageBoard/photoImgs")) {
//                            mediaBeen.add(new MediaBean(path, size, displayName));
//                        }

                        //显示用户3box存储中大于5M的视频，但不包含下载目录的视频
                        if (size < 1024 * 5)
                            continue;

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
                MainActivity.getInstance().SendMessageToUnity(1, gson.toJson(allPhotosTemp), 0);
            }
        }).start();
    }


}
