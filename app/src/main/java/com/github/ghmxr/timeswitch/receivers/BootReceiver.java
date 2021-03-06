package com.github.ghmxr.timeswitch.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null) return;
        if(intent.getAction()==null) return;
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            //Do starting the service
            SharedPreferences settings=context.getSharedPreferences(PublicConsts.PREFERENCES_NAME, Activity.MODE_PRIVATE);
            if(settings.getBoolean(PublicConsts.PREFERENCES_AUTO_START,PublicConsts.PREFERENCES_AUTO_START_DEFAULT)
                    &&settings.getBoolean(PublicConsts.PREFERENCE_SERVICE_ENABLED,PublicConsts.PREFERENCE_SERVICE_ENABLED_DEFAULT))
            {
                //context.startService(new Intent(context, TimeSwitchService.class));
                TimeSwitchService.startService(context);
            }
        }
    }
}
