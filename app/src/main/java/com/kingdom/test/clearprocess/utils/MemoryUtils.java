package com.kingdom.test.clearprocess.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;

/**
 * Created by admin on 2016/9/18.
 */
public class MemoryUtils {

    private long availMemory;
    private long totalMemory;

    public long getAvailMemory(Context context) {// 获取android当前可用内存大小

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        //mi.availMem; 当前系统的可用内存
        availMemory = mi.availMem;
        Log.i("qqq","mi.availMem:"+availMemory);
        return availMemory;
//        return Formatter.formatFileSize(this, mi.availMem);// 将获取的内存大小规格化
    }

    public long getTotalMemory() {
        final String mem_path = "/proc/meminfo";// 系统内存信息文件，第一行为内存大小
        Reader reader = null;
        BufferedReader bufferedReader = null;

        try {
            reader = new FileReader(mem_path);
            bufferedReader = new BufferedReader(reader, 8192);
            totalMemory = Long.parseLong(bufferedReader.readLine().split("\\s+")[1]) * 1024L;//这里*1024是转换为单位B（字节）

        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalMemory;
//        return Formatter.formatFileSize(this,totalMemory);// 将获取的内存大小规格化;
    }
}
