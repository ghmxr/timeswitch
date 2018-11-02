package com.github.ghmxr.timeswitch.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

public class APReceiver extends BroadcastReceiver implements Runnable {
    private Context context;
    private TaskItem item;
    private boolean isRegistered=false;

    public static final String ACTION_AP_STATE_CHANGED="android.net.wifi.WIFI_AP_STATE_CHANGED";
    public static int AP_STATE_DISABLING=10;
    public static int AP_STATE_DISABLED=11;
    public static int AP_STATE_ENABLING=12;
    public static int AP_STATE_ENABLED=13;

    public APReceiver(Context context, TaskItem item) {
        this.context=context;
        this.item=item;
    }

    public void registerReceiver(){
        if(!isRegistered) {
            IntentFilter filter=new IntentFilter();
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//"android.net.wifi.WIFI_AP_STATE_CHANGED"
            context.registerReceiver(this,filter);
            isRegistered=true;
        }
    }

    public void unRegisterReceiver(){
        if(isRegistered){
            context.unregisterReceiver(this);
            isRegistered=false;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null||intent.getAction()==null) return;
        if(intent.getAction().equals(ACTION_AP_STATE_CHANGED)){
            int state=intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,0);
            if(item==null) return;
            if(item.trigger_type== PublicConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED&&state==AP_STATE_ENABLED){
                activate();
            }
            if(item.trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED&&state==AP_STATE_DISABLED){
                activate();
            }
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
