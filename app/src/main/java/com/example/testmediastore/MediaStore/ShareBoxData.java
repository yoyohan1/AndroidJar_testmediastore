package com.example.testmediastore.MediaStore;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;

public class ShareBoxData {

    private static ShareBoxData instance;
    private Context context;

    public static ShareBoxData getInstance(Context context) {
        if (instance == null)
            instance = new ShareBoxData(context);
        return instance;
    }

    private ShareBoxData(Context context) {
        this.context = context;
        if (context == null) {
            throw new RuntimeException("context is null");
        }
    }

    public String getBoxUserInfo() {
        return this.getDataWithKey(context, "userMessage");
    }

    public void setBoxUserInfo(String message) {
        this.setDataWithKey(context, "userMessage", message);
    }

    public String getDataWithKey(Context context, String key) {
        try {
            Context packageContext = context.createPackageContext("com.xunixianshi.a3boxa2launcher", 2);
            SharedPreferences preferences = packageContext.getSharedPreferences("com.xunixianshi.a3boxa2launcher_preferences", 0);
            return preferences.getString(key, "");
        } catch (NameNotFoundException var5) {
            var5.printStackTrace();
            return "";
        }
    }

    public void setDataWithKey(Context context, String key, String message) {
        try {
            Context packageContext = context.createPackageContext("com.xunixianshi.a3boxa2launcher", 2);
            SharedPreferences preferences = packageContext.getSharedPreferences("com.xunixianshi.a3boxa2launcher_preferences", 0);
            preferences.edit().putString(key, message).apply();
        } catch (NameNotFoundException var6) {
            var6.printStackTrace();
        }

    }
}
