package com.example.unitytestmediastore;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.example.unitytestmediastore.MediaStore.IMediaThumbnailCallback;
import com.example.unitytestmediastore.MediaStore.ImagesMediaStore;
import com.example.unitytestmediastore.MediaStore.*;
import com.example.unitytestmediastore.MediaStore.VideoMediaStore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.threeglasses.threebox.mylibrary.CompareApi;
import com.threeglasses.threebox.mylibrary.OnDataFinishedListener;
import com.unity3d.player.UnityPlayer;

//import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static android.os.Build.VERSION.SDK_INT;


public class MainActivity {

    private static MainActivity instance;
    private static Context mContext;//此处应该是static 不然下边会报错
    private static Gson gson = new Gson();
    private ImagesMediaStore imagesMediaStore = new ImagesMediaStore();
    private VideoMediaStore videoMediaStore = new VideoMediaStore();
    private AudioMediaStore audioMediaStore = new AudioMediaStore();

    public static MainActivity getInstance() {
        if (instance == null)
            instance = new MainActivity();
        return instance;
    }

    public void InitCompareApi(Context context) {
        this.mContext = context;
        Log.i("Unity", "InitCompareApi成功！mContext:" + mContext.getPackageName());
    }

    /*
     *@author yoyohan
     *@description:
     */
    public void GetAllPhotoInfo() {

        //Log.i("Unity", "GetAllPhotoInfo mContext:" + mContext.getPackageName());

        imagesMediaStore.GetAllPhotoInfo();

        /*
        this.updateMedia(Environment.getExternalStorageDirectory().toString(), new IMediaThumbnailCallback() {//"file://" + Environment.getExternalStorageDirectory().getAbsolutePath()
            @Override
            public void onMediaThumbnailCallback(String path) {
                Log.i("Unity", "开始获取所有图片！");
                imagesMediaStore.GetAllPhotoInfo();
            }
        });*/
    }

    /*
     *@author yoyohan
     *@description:
     */
    public void GetAllMovieInfo() {
        videoMediaStore.GetAllMovieInfo();
    }

    public void GetAllAudioInfo() {
        audioMediaStore.GetAllAudioInfo();
    }

    /**
     * @Description:刷新媒体库
     * @author yoyohan
     * @create 2019/6/9 21:37
     */
    public void updateMedia(String path) {

        //Log.i("Unity", "updateMedia mContext:" + mContext.getPackageName());

        //当大于等于Android 4.4时
        if (SDK_INT >= Build.VERSION_CODES.KITKAT) {
            MediaScannerConnection.scanFile(mContext, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(uri);
                    mContext.sendBroadcast(mediaScanIntent);
                    Log.i("Unity", "更新媒体库成功！path:" + path);
                }
            });

        } else {//Andrtoid4.4以下版本
            Log.i("Unity", "updateMedia2-----------------------------------");
            mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile((new File(path).getParentFile()))));
        }
    }


    /**
     * @param
     * @return 返回类型
     * @throws
     * @Description: TODO 发送更新媒体库广播
     * @author hechuang
     * @create 2019/6/17
     */
    public void updataAllMedia() {
        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
    }

    public String getExternalPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }


    public void getPhotoThumbnailFile(final String path1) {
        MediaControl.getInstance(mContext).getPhotoThumbnailFile(path1, new IMediaThumbnailCallback() {
            @Override
            public void onMediaThumbnailCallback(String path2) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("fromPath", path1);
                jsonObject.addProperty("thumbnailPath", path2);
                Log.i("Unity", "Android端sdk发送：" + jsonObject.toString());
                MainActivity.getInstance().SendMessageToUnity(3, jsonObject.toString(), 0);
            }
        });
    }

    public void getVideoThumbnailBytes(final String path1) {
        MediaControl.getInstance(mContext).getVideoThumbnailBytes(path1, new IVideoThumbnailCallback() {
            @Override
            public void onVideoThumbnailCallback(String path2) {
                //android.util.Base64.encodeToString(bitmap, android.util.Base64.DEFAULT);
                //Base64.encodeBase64(bitmap).toString();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("fromPath", path1);
                jsonObject.addProperty("thumbnailPath", path2);
                MainActivity.getInstance().SendMessageToUnity(4, jsonObject.toString(), 0);
            }
        });
    }

    public void setBoxUserInfo(String msg) {
        ShareBoxData.getInstance(mContext).setBoxUserInfo(msg);
    }

    public String getBoxUserInfo() {
        return ShareBoxData.getInstance(mContext).getBoxUserInfo();
    }


    public static final int CONTEXT_INCLUDE_CODE = 0x00000001;//第一个标记是让我们可以通过类加载器去构建相关类
    public static final int CONTEXT_IGNORE_SECURITY = 0x00000002;//但是需要第二个参数的配合：忽略安全限制。一般Flag就配置这样个类型。

    public byte[] GetStreamingAssetsFileByPackageName(String packageName, String path) throws PackageManager.NameNotFoundException {

        Context bContext = UnityPlayer.currentActivity.createPackageContext(packageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);

        if (bContext == null)
            return new byte[]{};

        InputStream inputStream = null;
        try {
            inputStream = bContext.getAssets().open(path);
        } catch (IOException e) {
            Log.e("Unity", "GetStreamingAssetsFileByPackageName报错：" + e.getMessage());
        }

        return this.readtextbytes(inputStream);
    }

    /*
     *@author yoyohan
     *@description:
     */
    private byte[] readtextbytes(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //长度这里暂时先写成1024
        byte buf[] = new byte[1024];

        int len;

        try {

            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();

        } catch (IOException e) {
            Log.e("Unity", "GetStreamingAssetsFileByPackageName报错：" + e.getMessage());
        }

        return outputStream.toByteArray();
    }


    /*
     *@author yoyohan
     *@description:拷贝asset文件夹下的内容
     */
    public void CopyAssetToSDCard(String fromDir, final String desDir, boolean isOveride) {
        FileUtils.getInstance(UnityPlayer.currentActivity).copyAssetsToSD(fromDir, desDir, isOveride).setFileOperateCallback(new FileUtils.FileOperateCallback() {
            @Override
            public void onSuccess() {
                SendMessageToUnity(2, "拷贝到" + desDir + "成功！", 0);
            }

            @Override
            public void onFailed(String error) {
                SendMessageToUnity(2, "拷贝到" + desDir + "失败！", 1);
            }
        });
    }

    private CompareApi compareApi = null;

    private void InitMylibrart_release_arr() {
        Log.i("Unity", "开始InitMylibrart_release_arr！");
        this.compareApi = new CompareApi();
        this.compareApi.init(mContext);
        Log.i("Unity", "InitMylibrart_release_arr完成！");
    }

    OnDataFinishedListener onDataFinishedListener = new OnDataFinishedListener() {
        @Override
        public void onDataSuccessfully(int i) {
            SendMessageToUnity(7, i + "", 0);
        }

        @Override
        public void onDataFailed() {
            SendMessageToUnity(7, "获取图片和视频的类型失败！！！！", 1);
        }
    };

    public void getCompareVideo(String path) {
        if (compareApi == null) {
            InitMylibrart_release_arr();
        }
        Log.i("cxs", "安卓端getCompareVideo方法接收到的path：" + path);
        this.compareApi.getCompareVideo(Uri.parse(path), onDataFinishedListener);
    }

    public void getCompareImage(String path) {
        if (compareApi == null) {
            InitMylibrart_release_arr();
        }
        Log.i("cxs", "安卓端getCompareImage方法接收到的path：" + path);
        this.compareApi.getCompareImage(Uri.parse(path), onDataFinishedListener);
    }


    /**
     * @param :
     * @return :
     * created at 2019/1/10
     * @Description:发送消息给Unity根据requestId辨别 10001GetAllPhotoInfo
     * @author : launcher-u3d
     */
    public void SendMessageToUnity(int requestId, String msg, int status) {
        Log.i("cxs", "SendMessageToUnity!requestId:" + requestId + " msg:" + msg);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("opCode", 4);
        jsonObject.addProperty("status", status);
        jsonObject.addProperty("requestId", requestId);
        jsonObject.addProperty("msg", msg);

        UnityPlayer.UnitySendMessage("YouDaSdk", "OnOperationResponce", jsonObject.toString());
    }
}
