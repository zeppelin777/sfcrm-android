package com.android.zr2.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.format.Formatter;
import android.view.View;
import com.android.zr2.base.BaseApp;
import androidx.core.content.ContextCompat;


public class UiUtils {
    /**
     * dip转换px
     */
    public static int dip2px(int dip) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    /**
     * px转换dip
     */

    public static int px2dip(int px) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    /***
     * 根据资源id 获取 资源数组
     *
     * @param id
     * @return
     */
    public static String[] getStringArray(int id) {
        return getResources().getStringArray(id);
    }

    public static String[] getStringArray(Context context, int id) {
        return context.getResources().getStringArray(id);
    }

    /**
     * 获取resource 对象
     */
    public static Resources getResources() {
        return getContext().getResources();
    }

    /**
     * 获取上下文
     *
     * @return
     */
    public static Context getContext() {
        return BaseApp.Companion.getInstance();
    }

    /**
     * 根据布局id  获取 view对象
     *
     * @param id
     * @return
     */
    public static View inflate(int id) {
        return View.inflate(UiUtils.getContext(), id, null);
    }

    /**
     * long ----> MB
     *
     * @param size
     * @return
     */
    public static String formatFileSize(long size) {
        return Formatter.formatFileSize(getContext(), size);
    }

    /**
     * id ---Drawable对象
     *
     * @param id
     * @return
     */
    public static Drawable getDrawable(int id) {
        return ContextCompat.getDrawable(UiUtils.getContext(), id);
    }

    /**
     * id ---dimen
     *
     * @param id
     * @return
     */
    public static int getDimens(int id) {
        return getResources().getDimensionPixelSize(id); //  ---->px
    }


    /**
     * id --->String
     *
     * @param id
     * @return
     */
    public static String getString(int id) {
        return getResources().getString(id);
    }

    public static int getColor(int id) {
        return ContextCompat.getColor(UiUtils.getContext(), id);
    }


}
