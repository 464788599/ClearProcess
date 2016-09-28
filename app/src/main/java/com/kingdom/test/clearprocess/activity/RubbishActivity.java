package com.kingdom.test.clearprocess.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kingdom.test.clearprocess.EventBus.FirstEvent;
import com.kingdom.test.clearprocess.R;
import com.kingdom.test.clearprocess.adapter.ApkAdapter;
import com.kingdom.test.clearprocess.adapter.AppCacheAdapter;
import com.kingdom.test.clearprocess.adapter.AppDataAdapter;
import com.kingdom.test.clearprocess.adapter.BigFileAdapter;
import com.kingdom.test.clearprocess.javabean.RubbishInfoBean;
import com.kingdom.test.clearprocess.utils.FileUtils;
import com.kingdom.test.clearprocess.view.MyScrollView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RubbishActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnClear;
    private ImageView mIvBack;
    private PackageManager mPackageManager;
    private RelativeLayout mLayoutApk;
    private ImageView mIvShow5;
    private RelativeLayout mLayoutBigFolder;
    private ImageView mIvShow3;
    private ListView mLvappdata;
    private RelativeLayout mLayoutAppData;
    private ImageView mIvShow1;
    private RelativeLayout mLayoutAppCache;
    private RelativeLayout mLayoutRubbishSize;
    private TextView mTvRubbishSizeApk;
    private TextView mTvAppCacheSize;
    private TextView mTvRubbishSizeAppData;
    private TextView mTvRubbishSizeBigFolder;
    private RelativeLayout mLayoutTitle;
    private boolean isShowAPPData;
    private boolean isShowBigFile;
    private ProgressBar mpbScanAppData;
    private List<RubbishInfoBean> bigFiles = new ArrayList<>();
    private List<RubbishInfoBean> apkInfo = new ArrayList<>();
    private List<RubbishInfoBean> appCacheInfo = new ArrayList<>();
    private List<RubbishInfoBean> appDataInfo = new ArrayList<>();
    private ListView mLvBigFile;
    private long bigFilesSize;
    private ProgressBar mpbScanBigFile;
    private ListView mLvApk;
    private long apkSizes;
    private boolean isShowAPk;
    private ProgressBar mpbScanAPk;
    private boolean isScanCompleteAppData;
    private boolean isScanCompleteBigFile;
    private boolean isScanCompleteAPk;
    private ImageView mIvCompleteAppData;
    private ImageView mIvCompleteBigFolder;
    private ImageView mIvCompleteApk;
    private boolean isScanCompleteLogFile;
    private long logFileSize;
    private boolean isScanCompleteAppCache;
    private ImageView mIvShow6;
    private ProgressBar mpbScanAppCache;
    private ImageView mIvCompleteAppCache;
    private long appCacheSizes;
    private ListView mLvappCache;
    private long appDataSzies;

    private MyScrollView mScrollView;
    private LinearLayout mLyoutRubKinds;
    private RelativeLayout mLayoutSysFolder;
    private TextView mTvRubbishSizeSysFolder;
    private ImageView mIvShow4;
    private RelativeLayout mLayoutXzcl;
    private TextView mTvRubbishSizeXzcl;
    private ImageView mIvShow2;
    private CheckBox mCBcheckBox;
    private TextView mTvRubbishSizeUnit;
    private TextView mTvRubbishSizeNum;
    private Handler handle = new Handler();
    private boolean isShowappCase;
    private boolean isShowSysFile;
    private boolean isSCaning = true;//判断是否在扫描
    private boolean isSCanContinue = true;//判断是否继续扫描

    //每隔多少事件重复执行
    private Runnable update = new Runnable() {
        @Override
        public void run() {
            if (isScanCompleteBigFile && isScanCompleteAPk && isScanCompleteAppData && isScanCompleteAppCache && isScanCompleteLogFile) {
                //应用数据
                Collections.sort(appDataInfo);
                Collections.reverse(appDataInfo);
                mTvRubbishSizeAppData.setVisibility(View.VISIBLE);
                mTvRubbishSizeAppData.setTextColor(Color.BLACK);
                mLvappdata.setVisibility(View.VISIBLE);
                isShowAPPData = true;
                mIvShow1.setBackgroundResource(R.drawable.up);
                mTvRubbishSizeAppData.setText(Formatter.formatFileSize(getBaseContext(), appDataSzies));
                mLvappdata.setAdapter(new AppDataAdapter(appDataInfo, getBaseContext()));
                mIvCompleteAppData.setVisibility(View.GONE);

                //大文件
                mLvBigFile.setAdapter(new BigFileAdapter(getBaseContext(), bigFiles));
                mTvRubbishSizeBigFolder.setText(Formatter.formatFileSize(getBaseContext(), bigFilesSize));
                mTvRubbishSizeBigFolder.setTextColor(Color.BLACK);
                mTvRubbishSizeBigFolder.setVisibility(View.VISIBLE);
                isShowBigFile = true;
                mIvShow3.setBackgroundResource(R.drawable.up);
                mLvBigFile.setVisibility(View.VISIBLE);
                mpbScanBigFile.setVisibility(View.GONE);
                mIvCompleteBigFolder.setVisibility(View.GONE);

                //apk文件
                mLvApk.setAdapter(new ApkAdapter(getBaseContext(), apkInfo));
                mTvRubbishSizeApk.setText(Formatter.formatFileSize(getBaseContext(), apkSizes));
                mTvRubbishSizeApk.setTextColor(Color.BLACK);
                mTvRubbishSizeApk.setVisibility(View.VISIBLE);
                isShowAPk = true;
                mIvShow5.setBackgroundResource(R.drawable.up);
                mLvApk.setVisibility(View.VISIBLE);
                mpbScanAPk.setVisibility(View.GONE);
                mIvCompleteApk.setVisibility(View.GONE);

                //appCache
                Collections.sort(appCacheInfo);
                Collections.reverse(appCacheInfo);
                mLvappCache.setAdapter(new AppCacheAdapter(getBaseContext(), appCacheInfo));
                mTvAppCacheSize.setText(Formatter.formatFileSize(getBaseContext(), appCacheSizes));
                mTvAppCacheSize.setTextColor(Color.BLACK);
                mTvAppCacheSize.setVisibility(View.VISIBLE);
                isShowappCase = true;
                mIvShow6.setBackgroundResource(R.drawable.up);
                mLvappCache.setVisibility(View.VISIBLE);
                mpbScanAppCache.setVisibility(View.GONE);
                mIvCompleteAppCache.setVisibility(View.GONE);

                //系统文件
                mTvRubbishSizeSysFolder.setText(Formatter.formatFileSize(getBaseContext(), logFileSize));
                mTvRubbishSizeSysFolder.setTextColor(Color.BLACK);
                mTvRubbishSizeSysFolder.setVisibility(View.VISIBLE);
                mTvLogFileSize.setText(Formatter.formatFileSize(getBaseContext(), logFileSize));
                isShowSysFile = true;
                mIvShow4.setBackgroundResource(R.drawable.up);
                mLyoutLogFile.setVisibility(View.VISIBLE);
                mpbScanSysFile.setVisibility(View.GONE);
                mIvCompleteSysFile.setVisibility(View.GONE);


                mTvScaningFile.setVisibility(View.GONE);

                handle.removeCallbacks(update);

                isSCaning = false;
            } else {
                handle.postDelayed(update, 5);
            }
        }
    };
    private ProgressBar mpbScanSysFile;
    private ImageView mIvCompleteSysFile;
    private RelativeLayout mLyoutLogFile;
    private TextView mTvLogFileSize;
    private long totalRubSize;

    private Handler handler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            totalRubSize += (long) msg.obj;
            if (totalRubSize / (1024 * 1024 * 1024) < 1) {
                String ss = Formatter.formatFileSize(getBaseContext(), totalRubSize);
                Log.i("MB", ss.trim());
                String s = ss.trim().substring(0, 3);
                mTvRubbishSizeNum.setText(s);
                mTvRubbishSizeUnit.setText("MB");
            } else {
                String ss = Formatter.formatFileSize(getBaseContext(), totalRubSize);
                Log.i("GB", ss.trim());
                String s = ss.trim().substring(0, 4);
                mTvRubbishSizeNum.setText(s);
                mTvRubbishSizeUnit.setText("GB");
            }
        }
    };

    private Handler handler3 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String path = (String) msg.obj;
            mTvScaningFile.setText("正在扫描：" + path);
        }
    };
    private TextView mTvScaningFile;
    private CheckBox mCbLogFile;
    private ArrayList<String> cachePathList = new ArrayList<>();
    private long clearSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rubbish);
        initWidget();
        EventBus.getDefault().register(this);
        bigFiles.clear();
        apkInfo.clear();
        appCacheInfo.clear();
        appDataInfo.clear();
        //显示扫描结果,监听是否扫描完毕
        handle.post(update);
        //获取app缓存
        getAppCache();
        //获取app的数据大小
        getAPPDataSize();
        //扫描手机内的大文件
        getBigFile();
        //扫描手机内的APK文件
        getAPK();
        //扫描获取日志文件
        getLogFile();
    }
//    /**
//     * 窗口有焦点的时候，即所有的布局绘制完毕的时候，我们来获取布局的高度和myScrollView距离父类布局的顶部位置
//     */
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            mLayoutRubbishSizeHeight = mLayoutRubbishSize.getHeight();
//            mLayoutRubbishSizeWidth = mLayoutRubbishSize.getWidth();
//            mLayoutTitleHeight = mLayoutTitle.getHeight();
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initWidget() {
        mScrollView = (MyScrollView) findViewById(R.id.sv_scrollView);
        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mBtnClear = (Button) findViewById(R.id.btn_clear);
        mLyoutRubKinds = (LinearLayout) findViewById(R.id.lyout_rub_kinds);

        //安装包
        mLayoutApk = (RelativeLayout) findViewById(R.id.layout_apk);
        mIvShow5 = (ImageView) findViewById(R.id.iv_show5);
        mTvRubbishSizeApk = (TextView) findViewById(R.id.tv_rubbishsize_apk);
        mLvApk = (ListView) findViewById(R.id.lv_apk);
        mpbScanAPk = (ProgressBar) findViewById(R.id.pb_scan5);
        mIvCompleteApk = (ImageView) findViewById(R.id.iv_complete3);

        //系统文件
        mLayoutSysFolder = (RelativeLayout) findViewById(R.id.layout_sys_folder);
        mTvRubbishSizeSysFolder = (TextView) findViewById(R.id.tv_rubbishsize_sys_folder);
        mIvShow4 = (ImageView) findViewById(R.id.iv_show4);
        mpbScanSysFile = (ProgressBar) findViewById(R.id.pb_scan6);
        mIvCompleteSysFile = (ImageView) findViewById(R.id.iv_complete6);
        mLyoutLogFile = (RelativeLayout) findViewById(R.id.layout_log_file);
        mTvLogFileSize = (TextView) findViewById(R.id.tv_log_file_size);
        mCbLogFile = (CheckBox) findViewById(R.id.cb_log_file);

        //大文件
        mLayoutBigFolder = (RelativeLayout) findViewById(R.id.layout_big_folder);
        mTvRubbishSizeBigFolder = (TextView) findViewById(R.id.tv_rubbishsize_big_folder);
        mLvBigFile = (ListView) findViewById(R.id.lv_big_file);
        mpbScanBigFile = (ProgressBar) findViewById(R.id.pb_scan1);
        mIvCompleteBigFolder = (ImageView) findViewById(R.id.iv_complete2);
        mIvShow3 = (ImageView) findViewById(R.id.iv_show3);

        //卸载残留
        mLayoutXzcl = (RelativeLayout) findViewById(R.id.layout_xzcl);
        mTvRubbishSizeXzcl = (TextView) findViewById(R.id.tv_rubbishsize_xzcl);
        mIvShow2 = (ImageView) findViewById(R.id.iv_show2);

        //程序数据
        mLvappdata = (ListView) findViewById(R.id.lv_app_data);
        mLayoutAppData = (RelativeLayout) findViewById(R.id.layout_app_data);
        mTvRubbishSizeAppData = (TextView) findViewById(R.id.tv_rubbishsize_appdata);
        mpbScanAppData = (ProgressBar) findViewById(R.id.pb_scan);
        mIvShow1 = (ImageView) findViewById(R.id.iv_show1);
        mIvCompleteAppData = (ImageView) findViewById(R.id.iv_complete1);


        //应用缓存
        mLayoutAppCache = (RelativeLayout) findViewById(R.id.layout_app_cache);
        mCBcheckBox = (CheckBox) findViewById(R.id.cb_checkBox);
        mTvAppCacheSize = (TextView) findViewById(R.id.tv_app_cache_size);
        mpbScanAppCache = (ProgressBar) findViewById(R.id.pb_scan3);
        mIvShow6 = (ImageView) findViewById(R.id.iv_show6);
        mIvCompleteAppCache = (ImageView) findViewById(R.id.iv_complete4);
        mLvappCache = (ListView) findViewById(R.id.lv_app_cache);

        //标题部分
        mLayoutRubbishSize = (RelativeLayout) findViewById(R.id.layout_rubbish_size);
        mLayoutTitle = (RelativeLayout) findViewById(R.id.layout_title);
        mTvRubbishSizeUnit = (TextView) findViewById(R.id.tv_rubbishsize_unit);
        mTvRubbishSizeNum = (TextView) findViewById(R.id.tv_rubbishsize_num);
        mTvScaningFile = (TextView) findViewById(R.id.tv_scan_file);

        //设置点击监听
        mBtnClear.setOnClickListener(this);
        mIvBack.setOnClickListener(this);
        mLayoutAppData.setOnClickListener(this);
        mLayoutBigFolder.setOnClickListener(this);
        mLayoutApk.setOnClickListener(this);
        mLayoutAppCache.setOnClickListener(this);
        mLayoutSysFolder.setOnClickListener(this);

//        mScrollView.setOnScrollListener(new MyScrollView.OnScrollListener() {
//            @Override
//            public void onScroll(float downY, float upY, float moveY) {
//                Log.i("taggg","downY:"+downY+"  upY:"+upY+"  moveY:"+moveY);
//                if (moveY<0&&-moveY<mLayoutRubbishSizeHeight){
//                    float scale = -moveY/mLayoutRubbishSizeHeight;//0~1
//                    Log.i("scale",scale+"\n");
//                    //设置mLayoutRubbishSize缩放中心点
//                    ViewHelper.setPivotX(mLayoutRubbishSize,mLayoutRubbishSizeWidth/2);
//                    ViewHelper.setPivotX(mLayoutRubbishSize,mLayoutTitleHeight+mLayoutRubbishSizeHeight/2);
//                    ViewHelper.setScaleX(mLayoutRubbishSize,scale);
//                    ViewHelper.setScaleY(mLayoutRubbishSize,scale);
//                }else {
//
//                }
//
//            }
//        });
    }


    //获取app的数据大小
    private void getAPPDataSize() {
//        isScanCompleteAppData = false;
//        caches = 0;
//        appDatas = 0;
//        new Thread() {
//            @Override
//            public void run() {
//                super.run();
//                mPackageManager = RubbishActivity.this.getPackageManager();
//                List<ApplicationInfo> appInfoList = mPackageManager.getInstalledApplications(0);
//                for (ApplicationInfo appInfo : appInfoList) {
//                    if ((appInfo.flags & appInfo.FLAG_SYSTEM) == 0) {
//                        try {
//                            String packageName = appInfo.packageName;
//                            Drawable appIcon = mPackageManager.getApplicationIcon(appInfo);
//                            String appName = (String) mPackageManager.getApplicationLabel(appInfo);
//                            queryPacakgeSize(packageName);
//                            if (cacheSize > 0) {
//                                appCacheSizeList.add(new AppCacheBean(appName, appIcon, cacheSize));
//                            }
//                            if (dataSize > 0) {
//                                appDataSizeList.add(new AppCacheBean(appName, appIcon, dataSize));
//                            }
//                            caches += cacheSize;
//                            appDatas += dataSize;
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//                Collections.sort(appCacheSizeList);
//                Collections.reverse(appCacheSizeList);
//                Collections.sort(appDataSizeList);
//                Collections.reverse(appDataSizeList);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mIvCompleteAppData.setVisibility(View.VISIBLE);
//                        mpbScanAppData.setVisibility(View.GONE);
//                        isScanCompleteAppData = true;
//                    }
//                });
//            }
//        }.start();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    appDataSzies = 0;
                    isScanCompleteAppData = false;
                    mPackageManager = RubbishActivity.this.getPackageManager();
                    List<ApplicationInfo> appInfoList = mPackageManager.getInstalledApplications(0);
                    for (ApplicationInfo appInfo : appInfoList) {
                        try {
                            String packageName = appInfo.packageName;
                            Drawable appIcon = mPackageManager.getApplicationIcon(appInfo);
                            String appName = (String) mPackageManager.getApplicationLabel(appInfo);
                            File file1 = new File("/data/data/" + packageName + "/files");
                            if (file1.exists()) {
                                long size = new FileUtils().getFolderSize(file1);
                                if (size > 0) {
                                    appDataInfo.add(new RubbishInfoBean(appName, appIcon, size, file1.getPath()));
                                    appDataSzies += size;

                                    Message msg = handler2.obtainMessage();
                                    msg.obj = size;
                                    handler2.sendMessage(msg);
                                }
                            }
                            File file2 = new File(Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + packageName + "/files");
                            if (file2.exists()) {
                                long size = new FileUtils().getFolderSize(file2);
                                if (size > 0) {
                                    appDataInfo.add(new RubbishInfoBean(appName, appIcon, size, file2.getPath()));
                                    appDataSzies += size;

                                    Message msg = handler2.obtainMessage();
                                    msg.obj = size;
                                    handler2.sendMessage(msg);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //appdata文件相关
                            isScanCompleteAppData = true;
                            mIvCompleteAppData.setVisibility(View.VISIBLE);
                            mpbScanAppData.setVisibility(View.GONE);
                        }
                    });
                }
            }.start();
        } else {
            //android 6.0 以上动态获取读取内存卡的权限
            ActivityCompat.requestPermissions(RubbishActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    120);
        }


    }

    private void getBigFile() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    File file = new File(Environment.getExternalStorageDirectory().getPath());
                    isScanCompleteBigFile = false;
                    bigFilesSize = 0;
                    scanBigFile(file);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //大文件相关
                            isScanCompleteBigFile = true;
                            mIvCompleteBigFolder.setVisibility(View.VISIBLE);
                            mpbScanBigFile.setVisibility(View.GONE);
                        }
                    });

                }
            }.start();

        } else {
            //android 6.0 以上动态获取读取内存卡的权限
            ActivityCompat.requestPermissions(RubbishActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    121);
        }


    }

    private void getAPK() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    File file = new File(Environment.getExternalStorageDirectory().getPath());
                    isScanCompleteAPk = false;
                    apkSizes = 0;
                    scanApk(file);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //apk文件相关
                            isScanCompleteAPk = true;
                            mIvCompleteApk.setVisibility(View.VISIBLE);
                            mpbScanAPk.setVisibility(View.GONE);
                        }
                    });

                }
            }.start();

        } else {
            //android 6.0 以上动态获取读取内存卡的权限
            ActivityCompat.requestPermissions(RubbishActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    122);
        }


    }

    private void getLogFile() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    File file = new File(Environment.getExternalStorageDirectory().getPath());
                    isScanCompleteLogFile = false;
                    logFileSize = 0;
                    scanLog(file);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //日志文件相关
                            isScanCompleteLogFile = true;
                            mIvCompleteSysFile.setVisibility(View.VISIBLE);
                            mpbScanSysFile.setVisibility(View.GONE);
                        }
                    });

                }
            }.start();

        } else {
            //android 6.0 以上动态获取读取内存卡的权限
            ActivityCompat.requestPermissions(RubbishActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    123);
        }
    }

    //获取应用的缓存大小
    public void getAppCache() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    appCacheSizes = 0;
                    isScanCompleteAppCache = false;
                    mPackageManager = RubbishActivity.this.getPackageManager();
                    List<ApplicationInfo> appInfoList = mPackageManager.getInstalledApplications(0);
                    for (ApplicationInfo appInfo : appInfoList) {
                        try {
                            String packageName = appInfo.packageName;
                            Drawable appIcon = mPackageManager.getApplicationIcon(appInfo);
                            String appName = (String) mPackageManager.getApplicationLabel(appInfo);
                            File file1 = new File("/data/data/" + packageName + "/cache");
                            if (file1.exists()) {
                                long size = new FileUtils().getFolderSize(file1);
                                if (size > 0) {
                                    Log.i("zxc", file1.getPath() + "/" + size);
                                    appCacheInfo.add(new RubbishInfoBean(appName, appIcon, size, file1.getPath()));
                                    appCacheSizes += size;

                                    Message msg = handler2.obtainMessage();
                                    msg.obj = size;
                                    handler2.sendMessage(msg);
                                }
                            }
                            File file2 = new File(Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + packageName + "/cache");
                            if (file2.exists()) {
                                long size = new FileUtils().getFolderSize(file2);
                                if (size > 0) {
                                    Log.i("zxc", file2.getPath() + "/" + size);
                                    appCacheInfo.add(new RubbishInfoBean(appName, appIcon, size, file2.getPath()));
                                    appCacheSizes += size;

                                    Message msg = handler2.obtainMessage();
                                    msg.obj = size;
                                    handler2.sendMessage(msg);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //appCache文件相关
                            isScanCompleteAppCache = true;
                            mIvCompleteAppCache.setVisibility(View.VISIBLE);
                            mpbScanAppCache.setVisibility(View.GONE);
                        }
                    });
                }
            }.start();
        } else {
//android 6.0 以上动态获取读取内存卡的权限
            ActivityCompat.requestPermissions(RubbishActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    124);
        }


    }

    //android 6.0 以上动态获取读取内存卡的权限
    @Override
    public void onRequestPermissionsResult(final int requestCode, String[] permissions, final int[] grantResults) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (requestCode == 120) {
                    if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                        appDataSzies = 0;
                        isScanCompleteAppData = false;
                        mPackageManager = RubbishActivity.this.getPackageManager();
                        List<ApplicationInfo> appInfoList = mPackageManager.getInstalledApplications(0);
                        for (ApplicationInfo appInfo : appInfoList) {
                            try {
                                String packageName = appInfo.packageName;
                                Drawable appIcon = mPackageManager.getApplicationIcon(appInfo);
                                String appName = (String) mPackageManager.getApplicationLabel(appInfo);
                                File file1 = new File("/data/data/" + packageName + "/files");
                                if (file1.exists()) {
                                    long size = new FileUtils().getFolderSize(file1);
                                    if (size > 0) {
                                        appDataInfo.add(new RubbishInfoBean(appName, appIcon, size, file1.getPath()));
                                        appDataSzies += size;
                                    }
                                }
                                File file2 = new File(Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + packageName + "/files");
                                if (file2.exists()) {
                                    long size = new FileUtils().getFolderSize(file2);
                                    if (size > 0) {
                                        appDataInfo.add(new RubbishInfoBean(appName, appIcon, size, file2.getPath()));
                                        appDataSzies += size;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //appData文件相关
                                isScanCompleteAppData = true;
                                mIvCompleteAppData.setVisibility(View.VISIBLE);
                                mpbScanAppData.setVisibility(View.GONE);
                            }
                        });


                    }
                }
                if (requestCode == 121) {
                    if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                        File file = new File(Environment.getExternalStorageDirectory().getPath());
                        isScanCompleteBigFile = false;
                        bigFilesSize = 0;
                        scanBigFile(file);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //大文件相关
                                isScanCompleteBigFile = true;
                                mIvCompleteBigFolder.setVisibility(View.VISIBLE);
                                mpbScanBigFile.setVisibility(View.GONE);
                            }
                        });

                    }
                }


                if (requestCode == 122) {
                    if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                        File file = new File(Environment.getExternalStorageDirectory().getPath());
                        isScanCompleteAPk = false;
                        apkSizes = 0;

                        scanApk(file);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //apk文件相关
                                isScanCompleteAPk = true;
                                mIvCompleteApk.setVisibility(View.VISIBLE);
                                mpbScanAPk.setVisibility(View.GONE);
                            }
                        });
                    }
                }


                if (requestCode == 123) {
                    if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                        File file = new File(Environment.getExternalStorageDirectory().getPath());
                        isScanCompleteLogFile = false;
                        logFileSize = 0;
                        scanLog(file);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                            //大文件相关
//                            isScanCompleteBigFile =true;
//                            mIvCompleteBigFolder.setVisibility(View.VISIBLE);
//                            mpbScanBigFile.setVisibility(View.GONE);
                            }
                        });
                    }
                }

                if (requestCode == 124) {
                    if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                        appCacheSizes = 0;
                        isScanCompleteAppCache = false;
                        mPackageManager = RubbishActivity.this.getPackageManager();
                        List<ApplicationInfo> appInfoList = mPackageManager.getInstalledApplications(0);
                        for (ApplicationInfo appInfo : appInfoList) {
                            try {
                                String packageName = appInfo.packageName;
                                Drawable appIcon = mPackageManager.getApplicationIcon(appInfo);
                                String appName = (String) mPackageManager.getApplicationLabel(appInfo);
                                File file1 = new File("/data/data/" + packageName + "/cache");
                                if (file1.exists()) {
                                    long size = new FileUtils().getFolderSize(file1);
                                    if (size > 0) {
                                        Log.i("zxc", file1.getPath() + "/" + size);
                                        appCacheInfo.add(new RubbishInfoBean(appName, appIcon, size, file1.getPath()));
                                        appCacheSizes += size;
                                    }
                                }
                                File file2 = new File(Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + packageName + "/cache");
                                if (file2.exists()) {
                                    long size = new FileUtils().getFolderSize(file2);
                                    if (size > 0) {
                                        Log.i("zxc", file2.getPath() + "/" + size);
                                        appCacheInfo.add(new RubbishInfoBean(appName, appIcon, size, file2.getPath()));
                                        appCacheSizes += size;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //appCache文件相关
                                isScanCompleteAppCache = true;
                                mIvCompleteAppCache.setVisibility(View.VISIBLE);
                                mpbScanAppCache.setVisibility(View.GONE);
                            }
                        });

                    }
                }
            }
        }.start();

    }

    //扫描大文件
    private void scanBigFile(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (final File f : files) {
                if (isSCanContinue){
                    if (f.isDirectory()) {
                        scanBigFile(f);
                    } else {
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                FileUtils fileUtils = new FileUtils();
                                long size = fileUtils.getFileSize(f);
                                Message msg2 = handler3.obtainMessage();
                                if (f.getPath().length() > 30) {
                                    String ss = f.getPath().substring(f.getPath().length() - 30, f.getPath().length());
                                    msg2.obj = ss;
                                    handler3.sendMessage(msg2);
                                } else {
                                    msg2.obj = f.getPath();
                                    handler3.sendMessage(msg2);
                                }
                                if (size > 10 * 1024 * 1024 && !f.getName().contains(".apk")) {
                                    bigFilesSize += size;
                                    bigFiles.add(new RubbishInfoBean(f.getName(), null, size, f.getPath()));
                                    Message msg = handler2.obtainMessage();
                                    msg.obj = size;
                                    handler2.sendMessage(msg);
                                }
                            }
                        }.start();
                    }
                }

            }
        }
    }

    //扫描apk
    private void scanApk(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (final File f : files) {
                if (isSCanContinue){
                    if (f.isDirectory()) {
                        scanApk(f);
                    } else {
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                if (f.getName().contains(".apk") && !f.getName().contains(".apk.")) {
                                    Log.i("aaapk", f.getPath() + "::::" + Formatter.formatFileSize(getBaseContext(), new FileUtils().getFileSize(f)));
                                    //由apk文件获取apk信息
                                    PackageManager pm = RubbishActivity.this.getPackageManager();
                                    PackageInfo info = pm.getPackageArchiveInfo(f.getPath(), PackageManager.GET_ACTIVITIES);
                                    if (info != null) {
                                        info.applicationInfo.sourceDir = f.getPath();
                                        info.applicationInfo.publicSourceDir = f.getPath();
                                        ApplicationInfo appInfo = info.applicationInfo;
                                        Drawable apkIcon = pm.getApplicationIcon(appInfo);
                                        String apkName = (String) pm.getApplicationLabel(appInfo);
                                        String apkPath = f.getPath();
                                        long apkSize = new FileUtils().getFileSize(f);
                                        apkSizes += apkSize;
                                        apkInfo.add(new RubbishInfoBean(apkName, apkIcon, apkSize, apkPath));

                                        Message msg = handler2.obtainMessage();
                                        msg.obj = apkSize;
                                        handler2.sendMessage(msg);

                                    }
                                }
                            }
                        }.start();
                    }
                }

            }
        }
    }

    //扫描log
    private void scanLog(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (final File f : files) {
                if (isSCanContinue){
                    if (f.isDirectory()) {
                        scanLog(f);
                    } else {
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                if (f.getName().contains(".log") || f.getName().contains("log.txt")) {
                                    logFileSize += new FileUtils().getFileSize(f);
                                    Message msg = handler2.obtainMessage();
                                    msg.obj = new FileUtils().getFileSize(f);
                                    handler2.sendMessage(msg);
                                    Log.i("wsx", logFileSize + "");
                                    cachePathList.add(f.getPath());
                                }
                            }
                        }.start();
                    }
                }

            }
        }
    }

    //扫描app缓存
    private void scanAppCache(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (final File f : files) {
                if (f.isDirectory()) {
                    if (f.getName().contains("cache")) {
                        Log.i("258qq", f.getPath());
                    } else {
                        scanAppCache(f);
                    }
                } else {

                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                if (!isSCaning) {
                    finish();
                }else {
                    new AlertDialog.Builder(this).setPositiveButton("确信", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isSCanContinue =false;
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setTitle("是否中断扫描").show();
                }
                break;
            case R.id.btn_clear:
                clearAction();
                break;
            case R.id.layout_app_data:
                if (!isShowAPPData) {
                    mLvappdata.setVisibility(View.VISIBLE);
                    mIvShow1.setBackgroundResource(R.drawable.up);
                    isShowAPPData = true;
                } else {
                    mLvappdata.setVisibility(View.GONE);
                    mIvShow1.setBackgroundResource(R.drawable.down);
                    isShowAPPData = false;
                }
                break;
            case R.id.layout_big_folder:
                if (!isShowBigFile) {
                    mLvBigFile.setVisibility(View.VISIBLE);
                    mIvShow3.setBackgroundResource(R.drawable.up);
                    isShowBigFile = true;
                } else {
                    mLvBigFile.setVisibility(View.GONE);
                    mIvShow3.setBackgroundResource(R.drawable.down);
                    isShowBigFile = false;
                }
                break;
            case R.id.layout_apk:
                if (!isShowAPk) {
                    mLvApk.setVisibility(View.VISIBLE);
                    mIvShow5.setBackgroundResource(R.drawable.up);
                    isShowAPk = true;
                } else {
                    mLvApk.setVisibility(View.GONE);
                    mIvShow5.setBackgroundResource(R.drawable.down);
                    isShowAPk = false;
                }
                break;
            case R.id.layout_app_cache:
                if (!isShowappCase) {
                    mLvappCache.setVisibility(View.VISIBLE);
                    mIvShow6.setBackgroundResource(R.drawable.up);
                    isShowappCase = true;
                } else {
                    mLvappCache.setVisibility(View.GONE);
                    mIvShow6.setBackgroundResource(R.drawable.down);
                    isShowappCase = false;
                }
                break;
            case R.id.layout_sys_folder:
                if (!isShowSysFile) {
                    mLyoutLogFile.setVisibility(View.VISIBLE);
                    mIvShow4.setBackgroundResource(R.drawable.up);
                    isShowSysFile = true;
                } else {
                    mLyoutLogFile.setVisibility(View.GONE);
                    mIvShow4.setBackgroundResource(R.drawable.down);
                    isShowSysFile = false;
                }
                break;
        }
    }

    private void clearAction() {
//        private List<RubbishInfoBean> bigFiles = new ArrayList<>();
//        private List<RubbishInfoBean> apkInfo = new ArrayList<>();
//        private List<RubbishInfoBean> appCacheInfo = new ArrayList<>();
//        private List<RubbishInfoBean> appDataInfo = new ArrayList<>();
        for (RubbishInfoBean info : appCacheInfo) {
            if (info.isChecked()) {
                String path = info.getPath();
                new FileUtils().deleteFolder(path);
                Log.i("path", "缓存删除成功:" + info.getName());
            }
        }

        for (RubbishInfoBean info : apkInfo) {
            if (info.isChecked()) {
                String path = info.getPath();
                new FileUtils().deleteFolder(path);
                Log.i("path", "apk删除成功:" + info.getName());
            }

        }

        for (RubbishInfoBean info : appDataInfo) {
            if (info.isChecked()) {
                String path = info.getPath();
                new FileUtils().deleteFolder(path);
                Log.i("path", "app数据删除成功:" + info.getName());
            }

        }

        for (RubbishInfoBean info : bigFiles) {
            if (info.isChecked()) {
                String path = info.getPath();
                new FileUtils().deleteFolder(path);
                Log.i("path", "大文件删除成功:" + info.getName());
            }

        }
        if (mCbLogFile.isChecked()) {
            for (String path : cachePathList) {
                new FileUtils().deleteFolder(path);
                Log.i("path", "大文件删除成功:" + path);
            }

        }
        finish();
        Intent intent = new Intent(this, ClearedActivity.class);
        intent.putExtra("clearSize", clearSize);
        startActivity(intent);
    }

    //接受点击checkBox发送的消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void addSize(FirstEvent event) {
        switch (event.getTemp()) {
            case "add":
                clearSize += event.getSize();
                Log.i("add", clearSize + "");
                break;
            case "reduce":
                clearSize -= event.getSize();
                Log.i("reduce", clearSize + "");

                break;
        }

        mBtnClear.setText("清理垃圾 " + Formatter.formatFileSize(this, clearSize));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==event.KEYCODE_BACK){
            if (!isSCaning) {
                finish();
            }else {
                new AlertDialog.Builder(this).setPositiveButton("确信", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isSCanContinue =false;
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setTitle("是否中断扫描").show();
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    //查询应用的大小
//    private void queryPacakgeSize(String packageName) throws Exception {
//        if (packageName != null) {
//            try {
//                if (mPackageManager == null) {
//                    mPackageManager = getPackageManager();
//                }
//                //通过反射机制获得该隐藏函数
//                Method myUserId = UserHandle.class.getDeclaredMethod("myUserId");
//                Method getPackageSizeInfo = mPackageManager.getClass().getDeclaredMethod("getPackageSizeInfo", String.class, int.class, IPackageStatsObserver.class);
//                Log.i("taf", getPackageSizeInfo + "");
//                //调用该函数，并且给其分配参数 ，待调用流程完成后会回调PkgSizeObserver类的函数
//                getPackageSizeInfo.invoke(mPackageManager, packageName, myUserId.invoke(mPackageManager), new PkgSizeObserver());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//
//        }
//    }
//    public class PkgSizeObserver extends IPackageStatsObserver.Stub {
//        @Override
//        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) {
//            //缓存大小
//            cacheSize = pStats.cacheSize;
//            //数据大小
//            dataSize = pStats.dataSize;
//            //程序大小 =
//            codeSize = pStats.codeSize;
//            Log.i("tgggf", "cachesize--->" + Formatter.formatFileSize(getBaseContext(), cacheSize) + " datasize---->" + Formatter.formatFileSize(getBaseContext(), dataSize) + " codeSize---->" + Formatter.formatFileSize(getBaseContext(), codeSize));
//        }
//    }
}
