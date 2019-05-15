package com.github.ghmxr.timeswitch.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
import android.support.annotation.NonNull;

import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;

/**
 * @deprecated
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class LogUtil {

    public static void putExceptionLog(@NonNull Context context, @NonNull Exception e){
        putLog(context,e.toString());
       // Toast.makeText(context,"Exception:"+e.toString()+"\n",Toast.LENGTH_SHORT).show();
        //Message message=new Message();
        //message.what=TimeSwitchService.MESSAGE_DISPLAY_TOAST;
        //message.obj="Exception:"+e.toString();
        //TimeSwitchService.sendMessage(message);
    }

    public static void putLog(@NonNull Context context,@NonNull String log){
        SharedPreferences.Editor editor=context.getSharedPreferences(PublicConsts.PREFERENCES_LOGS_NAME, Activity.MODE_PRIVATE).edit();
        editor.putString(String.valueOf(System.currentTimeMillis()),log);
        editor.apply();
    }
}
