package com.example.shanj.speedtest;

import android.graphics.drawable.Drawable;

/**
 * Created by shanj on 2016/6/25.
 */
public class AppInfo {
    /**
     * 名称
     */
    String appname = "";
    /**
     * 包名
     */
    String packagename = "";
    /**
     * 图标
     */
    Drawable appicon = null;
    /**
     * 是否被选中
     */
    boolean isSelect  = false;
    /**
     * 0代表应用自身 1代表系统应用 2代表安装应用
     */
    int isSystemApp = 0;
    /**
     * 应用启动时间
     */
    int startTime = 0;
    /**
     * activity名称
     */
    String activityName;
    
    int isMainActvity;

}
