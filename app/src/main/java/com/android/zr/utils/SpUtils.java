package com.android.zr.utils;

import android.content.Context;
import android.content.SharedPreferences;


import java.util.Set;

public class SpUtils {


    /**
     * 保存string
     *
     * @param key
     * @param value
     */
    public static void saveString(String key, String value) {
        SharedPreferences sp = UiUtils.getContext().getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(key, value).commit();
    }


    /**
     * 获取string
     *
     * @param key
     * @return
     */
    public static String getString(String key) {
        SharedPreferences sp = UiUtils.getContext().getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(key, "");
    }

    /**
     * 保存long
     *
     * @param key
     * @param value
     */
    public static void saveLong(String key, Long value) {
        SharedPreferences sp = UiUtils.getContext().getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putLong(key, value).commit();
    }

    public static long getLong(String key) {
        SharedPreferences sp = UiUtils.getContext().getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        return sp.getLong(key, 0);
    }

    /**
     * 保存Int
     *
     * @param key
     * @param value
     */
    public static void saveInt(String key, int value) {
        SharedPreferences sp = UiUtils.getContext().getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putInt(key, value).commit();
    }

    /**
     * 获取int
     *
     * @param key
     * @return
     */
    public static int getInt(String key) {
        SharedPreferences sp = UiUtils.getContext().getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        return sp.getInt(key, 0);
    }

    /**
     * 获取int
     *
     * @param key
     * @param defValue 如果根据key没有找到对应的value，则返回的默认值
     * @return
     */
    public static int getInt(String key, int defValue) {
        SharedPreferences sp = UiUtils.getContext().getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        return sp.getInt(key, defValue);
    }


    /**
     * 保存boolean
     *
     * @param key
     * @param value
     */
    public static void saveBoolean(String key, boolean value) {
        SharedPreferences sp = UiUtils.getContext().getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(key, value).commit();
    }

    /**
     * 获取boolean
     *
     * @param key
     * @return
     */
    public static boolean getBoolean(String key) {
        SharedPreferences sp = UiUtils.getContext().getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(key, false);
    }

    /**
     * 保存HashSet
     *
     * @param key
     * @param set
     */
    public static void saveSet(String key, Set<String> set) {
        SharedPreferences sp = UiUtils.getContext().getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putStringSet(key, set).commit();
    }

    /**
     * 获取HashSet
     *
     * @param key
     * @return
     */
    public static Set<String> getSet(String key) {
        SharedPreferences sp = UiUtils.getContext().getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        return sp.getStringSet(key, null);
    }


    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
        return sp.getInt(key, defValue);
    }

}
