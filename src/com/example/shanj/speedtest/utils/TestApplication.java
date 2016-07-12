package com.example.shanj.speedtest.utils;

import android.app.Application;
import android.content.Context;

/**
 * Created by shanj on 2016/6/25.
 */
public class TestApplication extends Application {

    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    private static Context mContext;
    public static Context getContext(){
        return mContext;
    }
}
