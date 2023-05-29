package com.android.shufeng.utils;

import android.os.Build;
import android.os.Environment;

import java.io.File;

import androidx.annotation.RequiresApi;


public class FileUtils {

    private static final int DOWNLOAD = 0;
    private static final int PICTURE = 1;
    private static final int CACHE = 2;

    private static File getDir(int dir) {
        switch (dir) {
            case DOWNLOAD:
                return UiUtils.getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            case PICTURE:
                return UiUtils.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            case CACHE:
                return UiUtils.getContext().getExternalFilesDir("Cache");
        }
        return UiUtils.getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
    }

    /**
     * 获取数据缓存目录
     *
     * @return
     */
    public static File getCache() {
        return getDir(CACHE);
    }

    public static File getPicture() {
        return getDir(PICTURE);
    }

    public static File getDownload() {
        return getDir(DOWNLOAD);
    }

    public static File getTemporary() {
        return new File(getDir(CACHE), "temporary");
    }

    public static File getPublicDownload() {
        File downloadFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "zhongrong");
        if (!downloadFile.exists()) {
            downloadFile.mkdirs();
        }
        return downloadFile;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static String getRelativeDownloadPath() {
        return Environment.DIRECTORY_DOWNLOADS + "/zhongrong";
    }


    /**
     * 获取存贮文件的文件夹路径
     *
     * @return
     */
    public static File createFolders() {
        File baseDir;
        if (android.os.Build.VERSION.SDK_INT < 8) {
            baseDir = Environment.getExternalStorageDirectory();
        } else {
            baseDir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        }
        if (baseDir == null)
            return Environment.getExternalStorageDirectory();
        File aviaryFolder = new File(baseDir, "zhongrong");
        if (aviaryFolder.exists())
            return aviaryFolder;
        if (aviaryFolder.isFile())
            aviaryFolder.delete();
        if (aviaryFolder.mkdirs())
            return aviaryFolder;
        return Environment.getExternalStorageDirectory();
    }


    public static File getEmptyFile(String name) {
        File folder = FileUtils.createFolders();
        if (folder != null) {
            if (folder.exists()) {
                File file = new File(folder, name);
                return file;
            }
        }
        return null;
    }


}
