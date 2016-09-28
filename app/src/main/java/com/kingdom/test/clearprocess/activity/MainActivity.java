package com.kingdom.test.clearprocess.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.kingdom.test.clearprocess.view.CircularProgrssBar;
import com.kingdom.test.clearprocess.utils.MemoryUtils;
import com.kingdom.test.clearprocess.R;

import java.io.File;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private CircularProgrssBar circularProgrssBarRAM;
    private long totalMemory;
    private long availMemory;
    private LinearLayout mLayoutClearMemory;
    private CircularProgrssBar circularProgrssBarROM;
    private LinearLayout mLayoutClearRubbish;
    private LinearLayout mLayoutBatteryOptimize;
    private LinearLayout mLayoutWifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayoutClearMemory = (LinearLayout) findViewById(R.id.layout_memory_clear);
        mLayoutClearRubbish = (LinearLayout) findViewById(R.id.layout_clear_rubbish);
        mLayoutBatteryOptimize = (LinearLayout) findViewById(R.id.layout_battery_optimize);
        mLayoutWifi = (LinearLayout) findViewById(R.id.layout_wifi);

        mLayoutClearMemory.setOnClickListener(this);
        mLayoutWifi.setOnClickListener(this);
        mLayoutClearRubbish.setOnClickListener(this);
        mLayoutBatteryOptimize.setOnClickListener(this);
        circularProgrssBarRAM = (CircularProgrssBar) findViewById(R.id.cpb_progressbar);
        circularProgrssBarRAM.setOnClickListener(this);
        circularProgrssBarROM = (CircularProgrssBar) findViewById(R.id.cpb_progressbar_rom);
        circularProgrssBarROM.setOnClickListener(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        MemoryUtils memoryUtils = new MemoryUtils();
        totalMemory = memoryUtils.getTotalMemory();
        availMemory = memoryUtils.getAvailMemory(this);
        circularProgrssBarRAM.setMaxRAM(Formatter.formatFileSize(this, totalMemory));
        circularProgrssBarRAM.setUseRAM(Formatter.formatFileSize(this, totalMemory - availMemory));
        Log.i("tag1", "availMemory:" + availMemory + "  totalMemory:" + totalMemory);
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (totalMemory > 0) {
                    for (int i = 0; i < (totalMemory - availMemory) * 100 / totalMemory; i++) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        circularProgrssBarRAM.setProgress(i);
                    }
                }
            }
        }.start();

        //获取ROM大小
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getPath());
        final long blockCount = statFs.getBlockCount();
        final long blockSize = statFs.getBlockSize();
        final long availableBlocks = statFs.getAvailableBlocks();
        //ROM的大小
        final long romSpace = blockCount * blockSize;
        //可用ROM的大小
        final long availableRomSpace = availableBlocks * blockSize;

        //获取SD卡大小
        File pathSD = Environment.getExternalStorageDirectory();
        StatFs statFsSD = new StatFs(pathSD.getPath());
        final long blockCountSD = statFsSD.getBlockCount();
        final long blockSizeSD = statFsSD.getBlockSize();
        final long availableBlocksSD = statFsSD.getAvailableBlocks();
        //SD卡的大小
        final long SDSpace = blockCountSD * blockSizeSD;
        //可用SD卡的大小
        final long availableSDSpace = availableBlocksSD * blockSizeSD;
        circularProgrssBarROM.setMaxRAM(Formatter.formatFileSize(this, romSpace + SDSpace));
        circularProgrssBarROM.setUseRAM(Formatter.formatFileSize(this, romSpace + SDSpace - availableRomSpace - availableSDSpace));

        new Thread() {
            @Override
            public void run() {
                super.run();
                for (int i = 0; i < (romSpace + SDSpace - availableRomSpace - availableSDSpace) * 100 / (romSpace + SDSpace); i++) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    circularProgrssBarROM.setProgress(i);
                }
            }
        }.start();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.cpb_progressbar:
                startActivity(new Intent(this, MemoryActivity.class));
                break;
            case R.id.layout_memory_clear:
                startActivity(new Intent(this, MemoryActivity.class));
                break;
            case R.id.layout_clear_rubbish:
                startActivity(new Intent(this, RubbishActivity.class));
                break;
            case R.id.cpb_progressbar_rom:
                startActivity(new Intent(this, RubbishActivity.class));
                break;
            case R.id.layout_battery_optimize:
                startActivity(new Intent(this, BatteryActivity.class));
                break;
            case R.id.layout_wifi:
                startActivity(new Intent(this, WifiActivity.class));
                break;
        }
    }
}
