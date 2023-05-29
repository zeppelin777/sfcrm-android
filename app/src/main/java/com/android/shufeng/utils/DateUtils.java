package com.android.shufeng.utils;


import java.text.SimpleDateFormat;
import java.util.Date;


public class DateUtils {

    public static String getFormatDateTime(String pattern, long dateTime) {
        SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getInstance();
        sdf.applyPattern(pattern);

        return sdf.format(new Date(dateTime));
    }

}
