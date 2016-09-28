package com.kingdom.test.clearprocess.utils;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by admin on 2016/9/22.
 */
public class FileUtils {
    //获取文件夹的大小
    public long getFolderSize(File file) {
        long size = 0;
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    size += getFolderSize(f);
                } else {
                    size += getFileSize(f);
                }
            }
        }
        return size;
    }

    //获取文件的大小
    public long getFileSize(File file) {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                size = fis.available();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return size;
    }

    //删除文件夹
    public boolean deleteFolder(String path){
        boolean flag =false;
        File file = new File(path);
        if (!file.exists()){
            return flag;
        }else {
            if (file.isFile()){
                return deleteFile(path);
            }else {
                return deleteDirectory(path);
            }
        }
    }




    //删除文件
    public boolean deleteFile(String path){
        boolean flag =false;
        File file = new File(path);
        if (file.isFile()&&file.exists()){
            file.delete();
            flag = true;
        }
        return flag;
    }


    //删除文件夹及其子文件
    private boolean deleteDirectory(String path) {
        //如果path不以文件分隔符结尾则添加分隔符
        if (!path.endsWith(File.separator)){
            path=path+File.separator;
        }
        File file = new File(path);
        //如果file不是文件夹或不存在则退出
        if (!file.isDirectory()||!file.exists()){
            return false;
        }
        boolean flag = true;
        //删除文件夹下的所有文件及其子文件
        File[] files = file.listFiles();
        for ( File f:files) {
            if (f.isFile()){
                flag=deleteFile(f.getAbsolutePath());
                //如果此时flag为false，则代表该文件下没有文件了故退出
                if (!flag)break;
            }else {
                flag=deleteDirectory(f.getAbsolutePath());
                if (!flag)break;
            }
        }
        if (!flag)return false;
        //删除当前目录
        if (file.delete()){
            return true;
        }else {
            return false;
        }
    }
}
