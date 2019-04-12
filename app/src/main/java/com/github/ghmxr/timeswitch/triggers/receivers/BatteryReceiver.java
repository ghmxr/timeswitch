package com.github.ghmxr.timeswitch.triggers.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class BatteryReceiver extends BaseBroadcastReceiver{

    private boolean mLock=true;

    public BatteryReceiver(Context context, TaskItem item){
        super(context,item);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null) return;
        final String ACTION=intent.getAction();
        if(ACTION==null) return;
        if(ACTION.equals(Intent.ACTION_BATTERY_CHANGED)){
            if(item==null) return;
            if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE){
                int percentage_received=intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
                if(percentage_received>item.battery_percentage){
                    if(item.isenabled) runActions();
                }
                if(percentage_received<=item.battery_percentage){
                    restore();
                }
            }else if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE){
                int percentage_received=intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
                if(percentage_received<item.battery_percentage){
                    if(item.isenabled) runActions();
                }
                if(percentage_received>=item.battery_percentage){
                    restore();
                }
            }else if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE){
                int temperature=intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
                if(temperature>item.battery_temperature*10){
                    if(item.isenabled) runActions();
                }
                if(temperature<=item.battery_temperature*10){
                    restore();
                }
            }else if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE){
                int temperature=intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
                if(temperature<item.battery_temperature*10){
                    if(item.isenabled) runActions();
                }
                if(temperature>=item.battery_temperature*10){
                    restore();
                }
            }

        }

    }

    @Override
    public void activate() {
        try{
            context.registerReceiver(this,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void runActions(){
        if(!mLock) {
            this.mLock=true;
            runProcessTask();
        }
    }

    private void restore(){
        this.mLock=false;
    }

}
