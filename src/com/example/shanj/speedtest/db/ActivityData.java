
package com.example.shanj.speedtest.db;

import android.content.ContentValues;
import android.database.Cursor;

public class ActivityData {

    public static final String ID = "_id";
    public static final String ACTIVITY_NAME = "activityName";
    public static final String PKG_NAME = "pkgName";
    public static final String START_TIME = "startTime";
    public static final String IS_MAIN = "isMainAcitivty";

    public int id;
    public String activityName;
    public String pkgName;
    public int startTime;
    public int isMainAcitivty;


    public static ContentValues getContentValues(ActivityData user) {
        ContentValues cv = new ContentValues();
        cv.put(ACTIVITY_NAME, user.activityName);
        cv.put(PKG_NAME, user.pkgName);
        cv.put(START_TIME, user.startTime);
        cv.put(IS_MAIN, user.isMainAcitivty);
        return cv;
    }

    public static ActivityData fromCursor(Cursor cur) {
        ActivityData user = new ActivityData();
        user.id = cur.getInt(cur.getColumnIndex(ID));
        user.activityName = cur.getString(cur.getColumnIndex(ACTIVITY_NAME));
        user.pkgName = cur.getString(cur.getColumnIndex(PKG_NAME));
        user.startTime = cur.getInt(cur.getColumnIndex(START_TIME));
        user.isMainAcitivty = cur.getInt(cur.getColumnIndex(IS_MAIN));
        return user;
    }

    @Override
    public String toString() {
        return "ActivityData:[" + "activityName =" + activityName + ", pkgName = " + pkgName
                + ", startTime = " + startTime + ", isMainAcitivty = " + isMainAcitivty
                + "]";
    }
}
