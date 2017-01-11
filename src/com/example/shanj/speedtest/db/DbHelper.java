
package com.example.shanj.speedtest.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "speed.db";
    private static final int DB_VERSION = 1;
    private static final String TAB_USER = "table_user";

    private static DbHelper mInstance = null;

    public static DbHelper getInstance(Context context) {
        if (mInstance == null) {
            synchronized (DbHelper.class) {
                if (mInstance == null) {
                    mInstance = new DbHelper(context);
                }
            }
        }
        return mInstance;
    }

    private DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }


    private void createTable(SQLiteDatabase db) {
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(TAB_USER).append("(")
                .append(ActivityData.ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
                .append(ActivityData.ACTIVITY_NAME).append(" TEXT,")
                .append(ActivityData.PKG_NAME).append(" TEXT,")
                .append(ActivityData.START_TIME).append(" INTEGER,")
                .append(ActivityData.IS_MAIN).append(" INTEGER")
                .append(");");
        db.execSQL(sb.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static void addBean(Context context, ActivityData user) {
        Log.d("shuqi", "addBean " + user.toString());
        DbHelper helper = getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = ActivityData.getContentValues(user);
        db.insert(TAB_USER, null, cv);
        db.close();
    }

    public static void deleteBean(Context context) {
        DbHelper helper = getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(TAB_USER, null, null);
        db.close();
    }

    public static void updateBean(Context context, ActivityData user) {
        Log.d("shuqi", "updateBean " + user.toString());
        DbHelper helper = getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        String where = ActivityData.ACTIVITY_NAME + "=?";
        String[] whereArgs = new String[] {
            user.activityName + ""
        };
        ContentValues cv = ActivityData.getContentValues(user);
        int count = db.update(TAB_USER, cv, where, whereArgs);
        Log.d("shuqi", "count " + count);
        if (count <= 0) {
            addBean(context, user);
        }
        db.close();
    }

    public static List<ActivityData> queryAllBean(Context context) {
        List<ActivityData> users = new ArrayList<>();
        DbHelper helper = getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        //String orderBy = ActivityData.PKG_NAME + ", " + ActivityData.IS_MAIN + ", " +ActivityData.START_TIME ;
        Cursor cur = db.query(TAB_USER, null, null, null, null, null, null);
        if (cur != null && cur.getCount() > 0) {
            int len = cur.getCount();
            for (int i = 0; i < len; i++) {
                cur.moveToPosition(i);
                ActivityData user = ActivityData.fromCursor(cur);
                Log.d("shanjinwei", "queryAllBean: "+user.toString());
                users.add(user);
            }
            cur.close();
        }
        db.close();
        return sortData(users);
    }

    public static List<ActivityData> sortData(List<ActivityData> users){
        Collections.sort(users, new Comparator<ActivityData>() {
            @Override
            public int compare(ActivityData data1, ActivityData data2) {
                if(data1.pkgName.equalsIgnoreCase(data2.pkgName)){
                    boolean ismain1= data1.isMainAcitivty==1;
                    boolean ismain2= data2.isMainAcitivty ==1;
                    if(ismain1 || ismain2){
                        return ismain1? -1:1;
                    }else{
                        return sortApptime(data1, data2);
                    }
                }else{
                    return sortApptime(data1, data2);
                }
            }
        });
        return users;
    }

    private static int sortApptime(ActivityData data1, ActivityData data2){
        if(data1.startTime >= data2.startTime){
            return 1;
        }else{
            return -1;
        }
    }
}
