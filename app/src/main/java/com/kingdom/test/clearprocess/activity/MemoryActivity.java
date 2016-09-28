package com.kingdom.test.clearprocess.activity;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jaredrummler.android.processes.ProcessManager;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.kingdom.test.clearprocess.R;
import com.kingdom.test.clearprocess.javabean.AppBean;
import com.kingdom.test.clearprocess.utils.MemoryUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTvRelaaseMemory;
    private ListView mLvProcess;
    private ActivityManager mActivityManager;
    private List<ActivityManager.RunningAppProcessInfo> runningProcessInfoList;
    private List<AppBean> appList = new ArrayList<>();
    private ApplicationInfo applicationInfo;
    private PackageManager packageManager;
    private Button mBtnClear;
    private ProcessAdapter processAdapter;
    private TextView mTvMemory;
    private RelativeLayout mRelativeLayout;
    private TextView mTvRelaaseMemoryMB;
    private TextView mTvRunAPPCount;
    private long availMemory;
    private long totalMemory;
    private TextView mTvRelaaseMemoryText;
    private int totalReleaseMemory2;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            totalReleaseMemory2 = (int) msg.obj;
            mBtnClear.setText("清理" + totalReleaseMemory2 + "MB");
            mTvRelaaseMemory.setText(totalReleaseMemory2 + "");
        }
    };
    private ImageView mIvBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        //初始化集合
        appList.clear();
        initWidget();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //初始化集合
        appList.clear();
        processAdapter = new ProcessAdapter();
        mLvProcess.setAdapter(processAdapter);
        getRunningApp();
    }

    //初始化控件
    private void initWidget() {
        mTvRelaaseMemory = (TextView) findViewById(R.id.tv_release_memory_num);
        mTvRelaaseMemoryMB = (TextView) findViewById(R.id.tv_release_memory_MB);
        mTvRelaaseMemoryText = (TextView) findViewById(R.id.tv_release_memory_text);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.layout_meo);
        mLvProcess = (ListView) findViewById(R.id.lv_show_process);
        mBtnClear = (Button) findViewById(R.id.btn_clear);
        mTvMemory = (TextView) findViewById(R.id.tv_memeory);
        mTvRunAPPCount = (TextView) findViewById(R.id.tv_runningAPP_count);
        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mBtnClear.setOnClickListener(this);
    }

    //获取正在运行的程序
    private void getRunningApp() {
        mActivityManager = (ActivityManager) MemoryActivity.this.getSystemService(Context.ACTIVITY_SERVICE);
        packageManager = MemoryActivity.this.getPackageManager();

        new Thread() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                super.run();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    //获取正在运行的进程在SDK版本小于21时
                    runningProcessInfoList = mActivityManager.getRunningAppProcesses();
                    for (ActivityManager.RunningAppProcessInfo info : runningProcessInfoList) {
                        String processName = info.processName;
                        try {
                            applicationInfo = packageManager.getApplicationInfo(processName, 0);
                            //判断该程序不是自己的程序才加入到集合中
                            if (!processName.contains("com.kingdom.test.clearprocess")) {
                                String applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
                                Drawable applicationIcon = packageManager.getApplicationIcon(applicationInfo);
                                int pid = info.pid;
                                int importance = info.importance;
                                //程序占用的内存
                                Debug.MemoryInfo[] processMemoryInfo = mActivityManager.getProcessMemoryInfo(new int[]{pid});
                                Debug.MemoryInfo memoryInfo = processMemoryInfo[0];
                                //单位为KB
                                long totalPrivateDirty = memoryInfo.getTotalPrivateDirty();
                                float privateDirty = totalPrivateDirty / 1024;
                                //延迟动态添加
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                appList.add(new AppBean(applicationName, applicationIcon, privateDirty, processName, importance));

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        processAdapter.notifyDataSetChanged();
                                        mTvRunAPPCount.setText(appList.size() + "");
                                    }
                                });
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    //android5.0之后通过getRunningAppProcesses()只能获取到自己的application信息，导入一个Jar包才有ProcessManager
                    List<AndroidAppProcess> runAPPList  = ProcessManager.getRunningAppProcesses();

                    for (AndroidAppProcess rAppInfo:runAPPList) {
                        try {
                            String packageName = rAppInfo.getPackageName();
                            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
                            //判断该程序不是自己的程序才加入到集合中
                            if (!packageName.contains("clearprocess")&&!packageName.contains("ystem")) {
                                String applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
                                Drawable applicationIcon = packageManager.getApplicationIcon(applicationInfo);
                                int pid = rAppInfo.pid;
//                                int importance = rAppInfo;
                                //程序占用的内存
                                Debug.MemoryInfo[] processMemoryInfo = mActivityManager.getProcessMemoryInfo(new int[]{pid});
                                Debug.MemoryInfo memoryInfo = processMemoryInfo[0];
                                //单位为KB
                                long totalPrivateDirty = memoryInfo.getTotalPrivateDirty();
                                float privateDirty = totalPrivateDirty / 1024;
                                //延迟动态添加

                                appList.add(new AppBean(applicationName, applicationIcon, privateDirty, packageName, 0));

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        processAdapter.notifyDataSetChanged();
                                        mTvRunAPPCount.setText(appList.size() + "");
                                    }
                                });
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();


        MemoryUtils memoryUtils = new MemoryUtils();
        totalMemory = memoryUtils.getTotalMemory();
        availMemory = memoryUtils.getAvailMemory(getBaseContext());
        new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        for (int i = 0; i < (totalMemory - availMemory) * 100 / totalMemory; i++) {
                            final int finalI = i;

                            try {
                                Thread.sleep(30);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTvRelaaseMemory.setText(finalI + "%");
                                }
                            });
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //设置适配器
                                mTvRelaaseMemoryMB.setVisibility(View.VISIBLE);
                                mTvRelaaseMemoryText.setVisibility(View.VISIBLE);
                                //按importance从小到大排序
                                Collections.sort(appList);
                                //再倒序，使之importance从大到小排序
                                Collections.reverse(appList);

                                mTvMemory.setText(Formatter.formatFileSize(getBaseContext(), totalMemory - availMemory) + "/" + Formatter.formatFileSize(getBaseContext(), totalMemory));
                                //初始化集合中checkBOx的选中状态
                                for (AppBean x : appList) {
                                    if (x.getImportance() > 200) {
                                        x.setChecked(true);
                                    } else {
                                        x.setChecked(false);
                                    }
                                }
                                totalReleaseMemory2 = 0;
                                for (AppBean x : appList) {
                                    if (x.isChecked()) {
                                        totalReleaseMemory2 += x.getMemory();
                                    }
                                }
                                processAdapter.notifyDataSetChanged();
                                mTvRelaaseMemory.setText(totalReleaseMemory2 + "");
                                mBtnClear.setText("清理 " + totalReleaseMemory2 + "MB");
                            }
                        });
                    }
                }.start();


    }


    //显示listview的适配器
    private class ProcessAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return appList.size();
        }

        @Override
        public AppBean getItem(int position) {
            return appList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MemoryActivity.this).inflate(R.layout.item_scanlistview, null);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.mTvAppName = (TextView) convertView.findViewById(R.id.tv_item_app_name);
                viewHolder.mTvOccupyMemory = (TextView) convertView.findViewById(R.id.tv_memory);
                viewHolder.mIvAppIcon = (ImageView) convertView.findViewById(R.id.iv_item_app_icon);
                viewHolder.mCbAppSelect = (CheckBox) convertView.findViewById(R.id.cb_item_app_select);
                convertView.setTag(viewHolder);
            }
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.mTvOccupyMemory.setText(getItem(position).getMemory() + "MB");

            viewHolder.mTvAppName.setText(getItem(position).getAppName());
            viewHolder.mIvAppIcon.setBackground(getItem(position).getAppICon());

            viewHolder.mCbAppSelect.setChecked(getItem(position).isChecked());

            //checkBox的点击事件
            viewHolder.mCbAppSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //点击之后状态取反
                    getItem(position).setTag(getItem(position).isChecked());

                    if (getItem(position).isChecked()) {
                        totalReleaseMemory2 += getItem(position).getMemory();
                        Message msg = handler.obtainMessage();
                        msg.obj = totalReleaseMemory2;
                        handler.sendMessage(msg);
                    } else {
                        totalReleaseMemory2 -= getItem(position).getMemory();
                        Message msg = handler.obtainMessage();
                        msg.obj = totalReleaseMemory2;
                        handler.sendMessage(msg);
                    }
                }
            });

            return convertView;
        }
    }

    class ViewHolder {
        ImageView mIvAppIcon;
        TextView mTvAppName;
        CheckBox mCbAppSelect;
        TextView mTvOccupyMemory;
    }


    //点击事件
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_clear:
                clearAction();
                break;
            case R.id.iv_back:
                finish();
                break;
        }
    }

    //点击清理后
    private void clearAction() {
        for (AppBean x : appList) {
            if (x.isChecked()) {
                String packageName = x.getProcessName();
                mActivityManager.killBackgroundProcesses(packageName);
            }
        }
        Intent intent = new Intent(this, ClearedActivity.class);
        intent.putExtra("释放的内存", totalReleaseMemory2);
        startActivity(intent);
        finish();

    }

}
