package com.github.ghmxr.timeswitch.triggers.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.github.ghmxr.timeswitch.TaskItem;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class CustomBroadcastReceiver extends BaseBroadcastReceiver{
    String action;
    public CustomBroadcastReceiver(Context context, String action, TaskItem item) {
        super(context,item);
        this.action=action;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if(intent==null) return;
        if(intent.getAction()==null) return;
        if(intent.getAction().equals(action)){
            runProcessTask();
        }
    }

    @Override
    public void activate() {
        try{
            context.registerReceiver(this,new IntentFilter(action));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
