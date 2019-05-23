package com.github.ghmxr.timeswitch.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.github.ghmxr.timeswitch.activities.LogActivity;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class LogUtil {

    public static void putLog(@NonNull Context context,@NonNull String log){
        SharedPreferences.Editor editor=context.getSharedPreferences(PublicConsts.PREFERENCES_LOGS_NAME, Activity.MODE_PRIVATE).edit();
        editor.putString(String.valueOf(System.currentTimeMillis()),log);
        editor.apply();
        LogActivity.sendEmptyMessage(LogActivity.MESSAGE_REQUEST_REFRESH);
    }
}
