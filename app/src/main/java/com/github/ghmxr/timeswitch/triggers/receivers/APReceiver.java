package com.github.ghmxr.timeswitch.triggers.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;

public class APReceiver extends BaseBroadcastReceiver{
    private boolean mLock=true;
    public static final String ACTION_AP_STATE_CHANGED="android.net.wifi.WIFI_AP_STATE_CHANGED";
    public static int AP_STATE_DISABLING=10;
    public static int AP_STATE_DISABLED=11;
    public static int AP_STATE_ENABLING=12;
    public static int AP_STATE_ENABLED=13;

    public APReceiver(Context context, TaskItem item) {
        super(context,item);
    }

    /**
     * @deprecated
     */
    public void registerReceiver(){
        //if(!isRegistered) {

            //isRegistered=true;
       // }
    }

    /**
     * @deprecated
     */
    public void unRegisterReceiver(){
        //if(isRegistered){
        try{
            context.unregisterReceiver(this);
        }catch (Exception e){
            e.printStackTrace();
        }
           // isRegistered=false;
        //}
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null||intent.getAction()==null) return;
        if(intent.getAction().equals(ACTION_AP_STATE_CHANGED)){
            int state=intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,0);
            if(item==null) return;
            if(item.trigger_type== PublicConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED&&state==AP_STATE_ENABLED){
                runActions();
            }else{
                mLock=false;
            }
            if(item.trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED&&state==AP_STATE_DISABLED){
                runActions();
            }else{
                mLock=false;
            }
        }
    }

    @Override
    public void activate() {
        try{
            IntentFilter filter=new IntentFilter();
            filter.addAction(ACTION_AP_STATE_CHANGED);//"android.net.wifi.WIFI_AP_STATE_CHANGED"
            context.registerReceiver(this,filter);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void cancel() {
        super.cancel();
    }

    private void runActions(){
        if(!mLock){
            mLock=true;
            runProcessTask();
        }
    }
}
