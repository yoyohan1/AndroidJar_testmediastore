package com.example.unitytestmediastore;

/**
 * Created by Administrator on 2019/3/10.
 */

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by shenhua on 1/17/2017.
 * Email shenhuanet@126.com
 */
public class FileUtils {

    private static FileUtils instance;
    private static final int SUCCESS = 1;
    private static final int FAILED = 0;
    private Context context;
    private FileOperateCallback callback;
    private volatile boolean isSuccess;
    private String errorStr;

    public static FileUtils getInstance(Context context) {
        if (instance == null)
            instance = new FileUtils(context);
        return instance;
    }

    private FileUtils(Context context) {
        this.context = context;
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (callback != null) {
                if (msg.what == SUCCESS) {
                    callback.onSuccess();
                }
                if (msg.what == FAILED) {
                    callback.onFailed(msg.obj.toString());
                }
            }
        }
    };

    public FileUtils copyAssetsToSD(final String srcPath, final String sdPath,final boolean isOveride) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                copyAssetsToDst(context, srcPath, sdPath,isOveride);
                if (isSuccess)
                    handler.obtainMessage(SUCCESS).sendToTarget();
                else
                    handler.obtainMessage(FAILED, errorStr).sendToTarget();
            }
        }).start();
        return this;
    }

    public void setFileOperateCallback(FileOperateCallback callback) {
        this.callback = callback;
    }

    private void copyAssetsToDst(Context context, String srcPath, String dstPath,boolean isOveride) {
        try {
            String fileNames[] = context.getAssets().list(srcPath);

//            for (String fileName : fileNames)
//            {
//                Log.i("Unity","fileName：：：：：：:"+fileName);
//            }
 /*
 * 这个fileNames包含文件夹和文件
 *
 * */

            if (fileNames.length > 0)
            {
                File file = new File( dstPath);//Environment.getExternalStorageDirectory(),,写上这个会多加个/storage/emulated/0 造成错误 困扰我
                if (!file.exists()) file.mkdirs();

                //遍历该文件夹内所有文件（包含文件夹）
                for (String fileName : fileNames)
                {
                    String tempDesPath = dstPath + File.separator + fileName;

                    if (isOveride==false&&new File(tempDesPath).exists())
                        continue;

                   // Log.i("Unity","tempDesPath:"+tempDesPath);

                    // 拷贝assets下的某一个目录
                    if (!srcPath.equals(""))
                    {
                        copyAssetsToDst(context, srcPath + File.separator + fileName, tempDesPath,isOveride);
                    }
                    else
                    {
                        // 拷贝整个assets文件夹
                        copyAssetsToDst(context, fileName, tempDesPath,isOveride);
                    }
                }
            }
            else
            {
                //Log.i("Unity", "dstPath---------------------------1"+dstPath);

                File outFile = new File( dstPath);//,写上这个会多加个/storage/emulated/0 造成错误 困扰我Environment.getExternalStorageDirectory(),

                InputStream is = context.getAssets().open(srcPath);
                FileOutputStream fos = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
                //Log.i("Unity", "outFile---------------------------2");
                Log.i("Unity", outFile.getAbsolutePath());
            }
            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            errorStr = e.getMessage();
            isSuccess = false;
        }
    }


    public interface FileOperateCallback {
        void onSuccess();

        void onFailed(String error);
    }

}

