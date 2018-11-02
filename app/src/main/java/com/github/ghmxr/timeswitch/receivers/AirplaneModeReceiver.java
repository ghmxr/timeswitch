package com.github.ghmxr.timeswitch.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

public class AirplaneModeReceiver extends BroadcastReceiver implements Runnable {
    private Context context;
    private TaskItem item;
    private boolean isRegistered=false;
    public AirplaneModeReceiver(Context context,TaskItem item) {
        this.context=context;
        this.item=item;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null||intent.getAction()==null) return;
        boolean enabled=intent.getBooleanExtra("state",false);
        if(item==null) return;
        if(item.trigger_type== PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON&&enabled){
            activate();
        }else if(item.trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF&&!enabled){
            activate();
        }

    }

    public void registerReceiver(){
        if(!isRegistered){
            context.registerReceiver(this,new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
            isRegistered=true;
        }
    }

    public void unRegisterReceiver(){
        if(isRegistered){
            context.unregisterReceiver(this);
            isRegistered=false;
        }
    }

    private void activate(){
        new Thread(this).start();
    }

    @Override
    public void run() {
        new ProcessTaskItem(context,item).activateTaskItem();
    }
}
