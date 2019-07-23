package com.example.unitytestmediastore.MediaStore;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.example.unitytestmediastore.MainActivity;
import com.google.gson.Gson;
import com.unity3d.player.UnityPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImagesMediaStore {

    private static Gson gson = new Gson();

    public void GetAllPhotoInfo() {
        Log.i("cxs", "开始获取图片！1");
        final Activity mUnityPlayer = UnityPlayer.currentActivity;
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<MediaBean> mediaBeen = new ArrayList<MediaBean>();
                HashMap<String, List<MediaBean>> allPhotosTemp = new HashMap<>();//所有照片
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                String[] projImage = {MediaStore.Images.Media._ID
                        , MediaStore.Images.Media.DATA
                        , MediaStore.Images.Media.SIZE
                        , MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.HEIGHT};
                final Cursor mCursor = mUnityPlayer.getContentResolver().query(mImageUri,
                        projImage,
                        MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png", "image/jpg", "image/bmp"},
                        MediaStore.Images.Media.DATE_MODIFIED + " desc");//

                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        // 获取图片的路径
                        String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        int size = (int) (mCursor.getDouble(mCursor.getColumnIndex(MediaStore.Video.Media.SIZE)) / 1024);//int超过G会溢出
                        String displayName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                        int width = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Images.Media.WIDTH));
                        int height = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Images.Media.HEIGHT));

//                        //用于展示相册初始化界面
//                        if (path.contains("/storage/emulated/0/messageBoard/photoImgs")) {
//                            mediaBeen.add(new MediaBean(path, size, displayName).SetWidth(width).SetHeight(height));
//                        }

                        // 获取该图片的父路径名
                        String dirPath = new File(path).getParentFile().getAbsolutePath();

                        //过滤3box路径下的
                        if (dirPath.replace(Environment.getExternalStorageDirectory().getPath(), "").startsWith("/3box/")) {
                            Log.i("cxs", "过滤掉3box路径下的" + "  该图片为 size=" + size + ",path=" + path + ",name=" + displayName);
                            continue;
                        }

                        //过滤小于100K的
                        if (size < 100) {
                            Log.i("cxs", "过滤掉小于100K的" + "  该图片为 size=" + size + ",path=" + path + ",name=" + displayName);
                            continue;
                        }


                        //存储对应关系
                        if (allPhotosTemp.containsKey(dirPath)) {
                            List<MediaBean> data = allPhotosTemp.get(dirPath);
                            data.add(new MediaBean(path, size, displayName).SetWidth(width).SetHeight(height));
                            //Log.i("cxs","getAllPhotoInfo "+data.size()+",path="+data.get(0).path+",name="+data.get(0).displayName);
                        } else {
                            List<MediaBean> data = new ArrayList<>();
                            data.add(new MediaBean(path, size, displayName).SetWidth(width).SetHeight(height));
                            allPhotosTemp.put(dirPath, data);
                            //Log.i("cxs","getAllPhotoInfo else "+data.size()+",path="+data.get(0).path+",name="+data.get(0).displayName);
                        }
                    }
                    mCursor.close();
                }
                MainActivity.getInstance().SendMessageToUnity(0, gson.toJson(allPhotosTemp), 0);
            }
        }).start();
    }

}
