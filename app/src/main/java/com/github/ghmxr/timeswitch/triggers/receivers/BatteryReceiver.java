package com.github.ghmxr.timeswitch.triggers.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.annotation.Nullable;

import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class BatteryReceiver extends BaseBroadcastReceiver{

    /**
     * 百分比（0~100）
     */
    public static int Battery_percentage=-1;
    /**
     * 单位 毫伏（mV）
     */
    public static int Battery_voltage=-1;
    /**
     * 单位 0.1摄氏度
     */
    public static int Battery_temperature=-100;

    public static boolean isInstant=false;

    private boolean mLock=true;

    public BatteryReceiver(Context context,@Nullable TaskItem item){
        super(context,item);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null) return;
        final String ACTION=intent.getAction();
        if(ACTION==null) return;
        if(ACTION.equals(Intent.ACTION_BATTERY_CHANGED)){
            if(item==null){
                Battery_percentage=intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
                Battery_voltage=intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,-1);
                Battery_temperature=intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-100);
                isInstant=true;
                return;
            }
            if(item.trigger_type== PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE){
                int percentage_received=intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
                if(percentage_received>item.battery_percentage){
                    if(item.isenabled) runActions();
                }
                if(percentage_received<=item.battery_percentage){
                    restore();
                }
            }else if(item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE){
                int percentage_received=intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
                if(percentage_received<item.battery_percentage){
                    if(item.isenabled) runActions();
                }
                if(percentage_received>=item.battery_percentage){
                    restore();
                }
            }else if(item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE){
                int temperature=intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
                if(temperature>item.battery_temperature*10){
                    if(item.isenabled) runActions();
                }
                if(temperature<=item.battery_temperature*10){
                    restore();
                }
            }else if(item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE){
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

    /**
     * @deprecated
     */
    public void registerReceiver(){
       // if(!isRegistered){

           // this.isRegistered=true;
           // Log.i("BatteryReceiver","Battery Receiver registered!!!");
       // }
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

    /**
     * @deprecated
     */
    public void unregisterReceiver(){
       // if(isRegistered){
        try{
            context.unregisterReceiver(this);
        }catch (Exception e){
            e.printStackTrace();
        }
           // this.isRegistered=false;
       // }
    }
}
