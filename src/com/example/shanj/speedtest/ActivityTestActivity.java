package com.example.shanj.speedtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ActivityTestActivity extends Activity implements View.OnClickListener {


    private CheckBox lstTesactCheckBox;
    private TextView lstTesactActnametv;
    private ActivityInfo[] infos;
    private Button mClearBtn;
    private ArrayList<AppInfo> appInfos;
    private Adapter adapter;
    private SharedPreferences act_select_preferences;
    private SharedPreferences.Editor act_select_editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_layout);
        mClearBtn = (Button) findViewById(R.id.act_clear);
        ListView mLv = (ListView) findViewById(R.id.activity_list);
        Intent intent = getIntent();
        String pkg = intent.getStringExtra("pkg");
        Intent intentMain = getPackageManager().getLaunchIntentForPackage(pkg);
        String mainactName = " ";
        appInfos = new ArrayList<>();
        if (intentMain != null){
            mainactName = getPackageManager().getLaunchIntentForPackage(pkg).getComponent().getClassName();
        }else {
            mainactName = "NoMainActivity";
        }
        act_select_preferences = getSharedPreferences("act_select_info",MODE_PRIVATE);
        act_select_editor = act_select_preferences.edit();
        try {
            infos = getPackageManager().getPackageInfo(pkg, PackageManager.GET_ACTIVITIES).activities;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        for (ActivityInfo actInfo:infos) {
            AppInfo info = new AppInfo();
            if(actInfo.name.equals(mainactName)){
                continue;
            }
            info.activityName = actInfo.name;
            appInfos.add(info);
        }
        adapter = new Adapter(this);
        mClearBtn.setOnClickListener(this);
        mLv.setAdapter(adapter);
        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (act_select_preferences.getBoolean(appInfos.get(i).activityName,false)){
                    appInfos.get(i).isSelect = false;
                    act_select_editor.putBoolean(appInfos.get(i).activityName,false);
                    act_select_editor.apply();
                    adapter.notifyDataSetChanged();
                }else {
                    appInfos.get(i).isSelect = true;
                    act_select_editor.putBoolean(appInfos.get(i).activityName,true);
                    act_select_editor.apply();
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    //清空或者全选
    private void clearOrAllSelect(){
        if (mClearBtn.getText().equals("清空")){
            for (AppInfo info : appInfos) {
                info.isSelect = false;
                act_select_editor.putBoolean(info.activityName,false);
            }
            act_select_editor.apply();
            adapter.notifyDataSetChanged();
            mClearBtn.setText("全选");
        }else if (mClearBtn.getText().equals("全选")){
            for (AppInfo info : appInfos) {
                info.isSelect = true;
                act_select_editor.putBoolean(info.activityName,true);
            }
            act_select_editor.apply();
            adapter.notifyDataSetChanged();
            mClearBtn.setText("清空");
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.act_clear:
                clearOrAllSelect();
                break;
            default:
                break;
        }

    }


    class Adapter extends BaseAdapter{
        private LayoutInflater mLayoutInflater;

        Adapter( Context context) {
            this.mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return appInfos.size();
        }

        @Override
        public Object getItem(int i) {
            return appInfos.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null){
                view = mLayoutInflater.inflate(R.layout.act_item,viewGroup, false);
                viewHolder = new ViewHolder();
                viewHolder.checkBox = (CheckBox) view.findViewById(R.id.lst_tesact_checkBox);
                viewHolder.actName = (TextView) view.findViewById(R.id.lst_tesact_actnametv);
                view.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) view.getTag();
            }
            String[] arrs = appInfos.get(i).activityName.split("\\.");
            String temp = arrs[arrs.length-1];
            viewHolder.actName.setText(temp);
            viewHolder.checkBox.setChecked(act_select_preferences.getBoolean(appInfos.get(i).activityName,false));
            return view;
        }
        class ViewHolder {
            public TextView actName;
            public CheckBox checkBox;
        }
    }
}
