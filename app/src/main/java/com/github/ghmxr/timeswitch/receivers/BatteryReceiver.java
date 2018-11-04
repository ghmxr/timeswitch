package com.github.ghmxr.timeswitch.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class BatteryReceiver extends BroadcastReceiver implements Runnable{

    /**
     * 百分比（0~100）
     */
    public static int Battery_percentage=-1;
    /**
     * 单位 毫伏（mV）
     */
    public static int Battery_voltage=-1;
    /**
     * 单位 摄氏度*10
     */
    public static int Battery_temperature=-100;

    public static boolean isInstant=false;

    private Context context;
    private TaskItem item;
    private boolean mLock=true;
    private boolean isRegistered=false;

    public BatteryReceiver(Context context,TaskItem item){
        this.context=context;
        this.item=item;
    }

    public BatteryReceiver(Context context){
        this.context=context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null) return;
        final String ACTION=intent.getAction();
        if(ACTION==null) return;
        if(ACTION.equals(Intent.ACTION_BATTERY_CHANGED)){
            Battery_percentage=intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
            Battery_voltage=intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,-1);
            Battery_temperature=intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-100);
            isInstant=true;
            if(item==null) return;
            if(item.trigger_type== PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE){
                int percentage_received=intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
                if(percentage_received>item.battery_percentage){
                    if(item.isenabled) activate();
                }
                if(percentage_received<=item.battery_percentage){
                    restore();
                }
            }else if(item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE){
                int percentage_received=intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
                if(percentage_received<item.battery_percentage){
                    if(item.isenabled) activate();
                }
                if(percentage_received>=item.battery_percentage){
                    restore();
                }
            }else if(item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE){
                int temperature=intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
                if(temperature>item.battery_temperature*10){
                    if(item.isenabled) activate();
                }
                if(temperature<=item.battery_temperature*10){
                    restore();
                }
            }else if(item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE){
                int temperature=intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
                if(temperature<item.battery_temperature*10){
                    if(item.isenabled) activate();
                }
                if(temperature>=item.battery_temperature*10){
                    restore();
                }
            }

        }



    }

    public void registerReceiver(){
        if(!isRegistered){
            this.context.registerReceiver(this,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            this.isRegistered=true;
            Log.i("BatteryReceiver","Battery Receiver registered!!!");
        }
    }

    @Override
    public void run(){
        new ProcessTaskItem(this.context,this.item).activateTaskItem();
        //if(TimeSwitchService.service_queue!=null&&TimeSwitchService.service_queue.size()>0) new ProcessTaskItem(TimeSwitchService.service_queue.getLast(),item).activateTaskItem();
    }

    private void activate(){
        if(!mLock) {
            new Thread(this).start();
            this.mLock=true;
        }
    }

    private void restore(){
        this.mLock=false;
    }

    public void unregisterReceiver(){
        if(isRegistered){
            this.context.unregisterReceiver(this);
            this.isRegistered=false;
        }
    }
}
