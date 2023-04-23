package com.android.zr2.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtils {


    private static Toast toast;

    public static void showToast(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        }

        toast.setText(msg);
        toast.show();
    }

    /**
     * 默认的toast
     *
     * @param msg
     */
    public static void showToast(String msg) {
        showToast(UiUtils.getContext(), msg);
    }


    /**
     * 显示在屏幕中间的toast
     *
     * @param msg
     */
    public static void showToastCenter(String msg) {
        showToastCenter(UiUtils.getContext(), msg);
    }

    public static void showToastCenter(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
            //显示在屏幕中间
            toast.setGravity(Gravity.CENTER, 0, 0);
        }
        toast.setText(msg);
        toast.show();
    }


    /**
     * 静态toast,long time
     *
     * @param context 上下文
     * @param msg     显示的内容
     */
    public static void showToastLong(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        }

        toast.setText(msg);
        toast.show();
    }

    /**
     * 默认的toast
     *
     * @param msg
     */
    public static void showToastLong(String msg) {
        showToastLong(UiUtils.getContext(), msg);
    }
}
