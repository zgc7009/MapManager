package com.appycamp.mapmanager;

import android.app.Application;

import com.appycamp.mapmanager.network.NetworkRequestManager;

/**
 * Created by Zach on 6/13/2015.
 */
public class MainApplication extends Application{

    private static MainApplication mApplication;

    public static MainApplication getApplication(){
        return mApplication;
    }

    public static String getAplicationPackageName(){
        return MainApplication.class.getPackage().getName();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        NetworkRequestManager.initQueue(this);
    }
}
