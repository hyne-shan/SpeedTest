package com.example.shanj.speedtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shanj.speedtest.db.ActivityData;
import com.example.shanj.speedtest.db.DbHelper;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity implements View.OnClickListener {

    public ArrayList<AppInfo> appList, testActList, testAppList,readInfoList,testActInfoList;
    private String defaultinfo = "";
    private ListAdapter adapter;
    private ProgressDialog progressDialog;
    private Dialog dialog;
    private SharedPreferences app_select_preferences, act_select_preferences;
    private SharedPreferences.Editor app_select_editor, act_select_editor;
    private Button mClearBtn;
    private ListView mListView;
    private int number;//启动次数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button mButton = (Button) findViewById(R.id.button);
        mClearBtn = (Button) findViewById(R.id.clear);
        mListView = (ListView) findViewById(R.id.app_list);
        act_select_preferences = getSharedPreferences("act_select_info", MODE_PRIVATE);
        app_select_preferences = getSharedPreferences("app_select_info", MODE_PRIVATE);

        defaultinfo = getResources().getString(R.string.displyinfo);
        number = app_select_preferences.getInt("number", 5);
        //存放所有应用信息
        appList = new ArrayList<>();
        //存放单个app需要测试的act
        testActList = new ArrayList<>();
        //存放需要测试的应用
        testAppList = new ArrayList<>();
        mButton.setOnClickListener(this);
        mClearBtn.setOnClickListener(this);
        getApplist();
        adapter = new ListAdapter(this);
        mListView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.addSubMenu(0, 0, 0, getResources().getString(R.string.testinfo));
        menu.add(0, 1, 0, getResources().getString(R.string.startnumber));
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                readTestInfo();
                break;
            case 1:
                setTestCount();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    //meth
    //获取应用列表
    private void getApplist() {
        app_select_editor = app_select_preferences.edit();
        act_select_editor = act_select_preferences.edit();
        PackageManager pm = getPackageManager();
        //Intent intent = new Intent(Intent.ACTION_MAIN);
        //intent.addCategory(Intent.CATEGORY_LAUNCHER);
        //List<ResolveInfo> packages = pm.queryIntentActivities(intent, 0);
        List<ApplicationInfo> packages = pm.getInstalledApplications(0);
        //ResolveInfo resolveInfo;
        ApplicationInfo packageInfo;
        AppInfo tmpInfo;
        for (int i = 0; i < packages.size(); i++) {
            packageInfo = packages.get(i);
            //resolveInfo = packages.get(i);
            tmpInfo = new AppInfo();
            tmpInfo.appname = packageInfo.loadLabel(pm).toString();
            tmpInfo.packagename = packageInfo.packageName;
            tmpInfo.appicon = packageInfo.loadIcon(pm);
            tmpInfo.isSelect = app_select_preferences.getBoolean(packageInfo.packageName, false);
            Log.d("shanjinwei", "tmpInfo.appname :"+ tmpInfo.appname);
            Intent intent = pm.getLaunchIntentForPackage(packageInfo.packageName);
            if (intent != null){
                tmpInfo.activityName = pm.getLaunchIntentForPackage(packageInfo.packageName).getComponent().getClassName();
            }else {
                tmpInfo.activityName = "NoMainActivity";
            }
            Log.d("shanjinwei", "tmpInfo.activityName :"+ tmpInfo.activityName);
            if ("com.example.shanj.speedtest".equals(tmpInfo.packagename)) {
                continue;
            }
            //judge system app
            try {

                if (intent != null){
                    if ((this.getPackageManager().getApplicationInfo(tmpInfo.packagename, 0).flags
                            & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
                        tmpInfo.isSystemApp = 1;
                    } else {
                        tmpInfo.isSystemApp = 2;
                    }
                }else {
                    if ((this.getPackageManager().getApplicationInfo(tmpInfo.packagename, 0).flags
                            & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
                        tmpInfo.isSystemApp = 3;
                    } else {
                        tmpInfo.isSystemApp = 4;
                    }
                }

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            appList.add(tmpInfo);
        }
        Comparator<AppInfo> comparator = new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo appInfo, AppInfo t1) {
                return appInfo.isSystemApp - t1.isSystemApp;
            }
        };
        Collections.sort(appList, comparator);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (appList.get(i).isSelect) {
                    appList.get(i).isSelect = false;
                    app_select_editor.putBoolean(appList.get(i).packagename, false);
                    app_select_editor.apply();
                    if (!appList.get(i).activityName.equals("NoMainActivity")){
                        act_select_editor.putBoolean(appList.get(i).activityName,false);
                        act_select_editor.apply();
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    appList.get(i).isSelect = true;
                    app_select_editor.putBoolean(appList.get(i).packagename, true);
                    app_select_editor.apply();
                    if (!appList.get(i).activityName.equals("NoMainActivity")){
                        act_select_editor.putBoolean(appList.get(i).activityName,true);
                        act_select_editor.apply();
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });

    }


    //setestcount
    private void setTestCount() {
        //启动次数选择
        dialog = new Dialog(MainActivity.this);
        dialog.setTitle(getResources().getString(R.string.setrunnumber));
        dialog.setContentView(R.layout.numberpick_dialog);
        final NumberPicker np = (NumberPicker) dialog.findViewById(R.id.numberPicker);
        Button defineBtn = (Button) dialog.findViewById(R.id.define);
        Button cancelBtn = (Button) dialog.findViewById(R.id.cancel);
        np.setMaxValue(10);
        np.setMinValue(1);
        np.setValue(number);
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                //不做操作
            }
        });

        defineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                number = np.getValue();
                app_select_editor.putInt("number", number);
                app_select_editor.apply();
                dialog.dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    //read test info
    private void  readTestInfo() {
        String testinfo = "";
        List<ActivityData> acts = DbHelper.queryAllBean(MainActivity.this);
        for (ActivityData act : acts) {
            if (act.activityName.equals(getPackageManager().getLaunchIntentForPackage(act.pkgName)
                    .getComponent().getClassName())){
                try {
                    PackageManager packageManager = getPackageManager();
                    ApplicationInfo resolveInfo = packageManager.getApplicationInfo(act.pkgName,0);
                    act.activityName = (String) resolveInfo.loadLabel(packageManager);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                testinfo += act.activityName + " : " + act.startTime + "ms \n";
            }
            else {
                String[] arrs = act.activityName.split("\\.");
                String temp = arrs[arrs.length-1];
                testinfo +="    " + temp + " : " + act.startTime + "ms \n";
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(testinfo.equals("") ? "无测试数据" : testinfo.substring(0, testinfo.length() - 2));


        final String temp = testinfo.equals("") ? "无测试数据" : testinfo.substring(0, testinfo.length() - 2);
        builder.setNegativeButton("复制测试信息", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, temp));
            }
        });
        builder.setPositiveButton("清空测试信息", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //情况数据
            }
        });
        builder.create().show();
    }

    //kill progress
    public void killProcessByPID(String pkg) {
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("am force-stop " + pkg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * ReadStartTime
     *
     * @param pkg    包名
     * @param intent 主activity
     * @return 返回信息
     */
    private String getAppStartinfo(String pkg, String intent) {
        String result = "";
        DataInputStream dis;
        try {
            String command = "am start -W " + pkg + "/" + intent;
            Runtime runtime = Runtime.getRuntime();
            Process p = runtime.exec(command);
            dis = new DataInputStream(p.getInputStream());
            String line;
            while ((line = dis.readLine()) != null) {
                if (result.equals("")) {
                    result += line;
                } else {
                    result += "\n" + line;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    Comparator<AppInfo> comparator = new Comparator<AppInfo>() {

        @Override
        public int compare(AppInfo testAppInfo, AppInfo t1) {
            if (testAppInfo.startTime > t1.startTime) {
                return 1;
            }
            if (testAppInfo.startTime < t1.startTime) {
                return -1;
            }
            if (testAppInfo.startTime == t1.startTime) {
                return 0;
            }
            return 0;
        }
    };

    /**
     * 测试启动的方法
     */
    private void actTest() {
        progressDialog = new ProgressDialog(MainActivity.this);
        testAppList.clear();
        testActList.clear();
        new AsyncTask<Void, Integer, Void>() {
            String displyinfo = "";
            int totalTestNumber;
            int count = 0;
            String appName = " ";
            @Override
            protected Void doInBackground(Void... voids) {
                //获取需要测试act的数量*测试次数的结果
                for (AppInfo app : appList) {
                    if (app.isSelect) {
                        try {
                            ActivityInfo[] infos = getPackageManager().getPackageInfo(app.packagename, PackageManager.GET_ACTIVITIES).activities;
                            for (ActivityInfo activityInfo : infos) {
                                if (act_select_preferences.getBoolean(activityInfo.name, false)) {
                                    totalTestNumber++;
                                }
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setMax(totalTestNumber * number);
                progressDialog.setIndeterminate(false);
                progressDialog.setCancelable(true);
                publishProgress(count);
                AppInfo info;
                ActivityInfo[] infos;
                //启动规定次数的总时间
                int toataltime = 0;
                //记录测试过程中的进度
                int count = 0;
                //计算时间
                for (AppInfo app : appList) {
                    if (app.isSelect) {
                        try {
                            appName = app.appname;
                            infos = getPackageManager().getPackageInfo(app.packagename, PackageManager.GET_ACTIVITIES).activities;
                            for (ActivityInfo activityInfo : infos) {
                                if (act_select_preferences.getBoolean(activityInfo.name, false)) {
                                    info = new AppInfo();
                                    info.activityName = activityInfo.name;
                                    info.packagename = app.packagename;
                                    info.appname = app.appname;
                                    if ( activityInfo.name.equals
                                            (getPackageManager().getLaunchIntentForPackage(app.packagename).getComponent().getClassName())){
                                        info.isMainActvity = 1;
                                    }
                                    //计算单个act启动的时间
                                    for (int i = 0; i < number; i++) {
                                        count++;
                                        publishProgress(count);
                                        //计算之前关闭进程
                                        killProcessByPID(info.packagename);
                                        //模拟用户操作，暂停2s
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        //计算启动时间并匹配一段信息的时间
                                        String tempinfo = getAppStartinfo(info.packagename, info.activityName);
                                        String[] arrs = tempinfo.split("\n");
                                        String temp = arrs[4];
                                        Pattern p = Pattern.compile("[^0-9]");
                                        Matcher m = p.matcher(temp);
                                        int temptime = Integer.parseInt(m.replaceAll("").trim());
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        //启动之后关闭进程
                                        killProcessByPID(app.packagename);
                                        //得到总时间
                                        toataltime += temptime;
                                    }
                                    info.startTime = toataltime / number;
                                    testActList.add(info);
                                    Collections.sort(testActList, comparator);
                                }
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                progressDialog.setProgress(values[0]);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("正在测试" + appName);
                progressDialog.show();
            }

            @Override
            protected void onPostExecute(Void s) {
                super.onPostExecute(s);
                String temp = "";
                for (AppInfo info : testActList) {
                    ActivityData act = new ActivityData();
                    act.activityName = info.activityName;
                    act.pkgName = info.packagename;
                    act.startTime = info.startTime;
                    act.isMainAcitivty = info.isMainActvity;
                    DbHelper.updateBean(MainActivity.this, act);
                    if (info.isMainActvity == 1) {
                        info.activityName = info.appname;
                    }
                    String[] arrs = info.activityName.split("\\.");
                    if (arrs.length > 2){
                        temp = "    "+arrs[arrs.length - 1];

                    }else {
                        temp = arrs[arrs.length - 1];
                    }
                    displyinfo += temp + " : " + info.startTime + "ms \n";
                }
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(displyinfo.equals("") ? defaultinfo : displyinfo.substring(0, displyinfo.length() - 2)).create().show();
                if (displyinfo.length() > 2) {
                    app_select_editor.putString("testinfo", displyinfo.substring(0, displyinfo.length() - 2));
                    app_select_editor.apply();
                }
            }
        }.execute();
    }



    /**
     * 清空或者全选
     */
    private void ClearOrAllSelect() {
        if (mClearBtn.getText().equals("清空")) {
            for (AppInfo app : appList) {
                app.isSelect = false;
                app_select_editor.putBoolean(app.packagename, false);
                app_select_editor.apply();
                adapter.notifyDataSetChanged();
            }
            mClearBtn.setText("全选");
        } else if (mClearBtn.getText().equals("全选")) {
            for (AppInfo app : appList) {
                app.isSelect = true;
                app_select_editor.putBoolean(app.packagename, true);
                app_select_editor.apply();
                adapter.notifyDataSetChanged();
            }
            mClearBtn.setText("清空");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                actTest();
                break;
            case R.id.clear:
                ClearOrAllSelect();
                break;
            default:
                break;
        }
    }

    class ListAdapter extends BaseAdapter {
        private LayoutInflater mLayoutInflater;

        ListAdapter(Context context) {
            this.mLayoutInflater = LayoutInflater.from(context);
        }


        @Override
        public int getCount() {
            return appList.size();
        }

        @Override
        public Object getItem(int i) {
            return appList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = mLayoutInflater.inflate(R.layout.item, viewGroup, false);

                viewHolder = new ViewHolder();
                viewHolder.iconIV = (ImageView) view.findViewById(R.id.icon);
                viewHolder.nameTv = (TextView) view.findViewById(R.id.name);
                viewHolder.pkgTv = (TextView) view.findViewById(R.id.pkg);
                viewHolder.checkBox = (CheckBox) view.findViewById(R.id.checkBox);
                viewHolder.moreActIv = (ImageView) view.findViewById(R.id.moreact);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.iconIV.setImageDrawable(appList.get(i).appicon);
            viewHolder.nameTv.setText(appList.get(i).appname);
            viewHolder.pkgTv.setText(appList.get(i).packagename);
            viewHolder.checkBox.setChecked(app_select_preferences.getBoolean(appList.get(i).packagename, false));
            viewHolder.moreActIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Toast.makeText(MainActivity.this, appList.get(i).appname, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, ActivityTestActivity.class);
                    intent.putExtra("pkg", appList.get(i).packagename);
                    startActivity(intent);
                   /* if (!appList.get(i).activityName.equals("NoMainActivity")){

                    }else {
                        Toast.makeText(MainActivity.this, "没有主界面", Toast.LENGTH_SHORT).show();
                    }*/
                }
            });
            return view;
        }

        class ViewHolder {
            public ImageView iconIV, moreActIv;
            public TextView nameTv;
            public TextView pkgTv;
            public CheckBox checkBox;
        }
    }
}



