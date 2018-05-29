package com.shakuro.firebaseproject;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class FirebaseApplication extends Application {
    private static Context context;
    private static Application application;

    @Override
    public void onCreate(){
        super.onCreate();

        context = getApplicationContext();
        application = this;
    }

    public static Context getContext() {
        return context;
    }

    public static Application getApplication() {
        return application;
    }


    public static String getAppVersion() {
        String version = "";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            version = String.format("%s (%d)", pInfo.versionName, pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {

        }
        return version;
    }

    public static int getVersionCode() {
        int versionCode = 0;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {

        }
        return versionCode;
    }
}
