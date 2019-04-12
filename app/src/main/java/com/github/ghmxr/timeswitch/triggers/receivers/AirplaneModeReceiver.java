package com.github.ghmxr.timeswitch.triggers.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;

public class AirplaneModeReceiver extends BaseBroadcastReceiver{

    public AirplaneModeReceiver(Context context,TaskItem item) {
        super(context,item);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null||intent.getAction()==null) return;
        boolean enabled=intent.getBooleanExtra("state",false);
        if(item==null) return;
        if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON&&enabled){
            runProcessTask();
        }else if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF&&!enabled){
            runProcessTask();
        }

    }

    @Override
    public void activate() {
        try{
            context.registerReceiver(this,new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void cancel() {
        super.cancel();
    }

}
