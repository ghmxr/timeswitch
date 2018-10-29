package com.github.ghmxr.timeswitch.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

public class WifiReceiver extends BroadcastReceiver implements Runnable {
    TaskItem item;
    Context context;
    boolean isregistered=false;
    //boolean mLock=true;

    public WifiReceiver(@NonNull Context context, @NonNull TaskItem item) {
        this.context=context;
        this.item=item;
    }

    public void registerReceiver(){
        if(!isregistered) {
            IntentFilter filter=new IntentFilter();
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            context.registerReceiver(this,new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
            isregistered=true;
        }
    }

    public void unregisterReceiver(){
        if(isregistered) context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null ) return;
        if(intent.getAction()==null) return;
        if(!intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)&&!intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) return;

        int type=item.trigger_type;

        NetworkInfo info=intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

        if(type== PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED){
            if(info==null) return;
            if(info.getState().equals(NetworkInfo.State.CONNECTED)){
                activate();
                return;
            }
        }

        if(type==PublicConsts.TRIGGER_TYPE_WIFI_DISCONNECTED){
            if(info==null) return;
            if(info.getState().equals(NetworkInfo.State.DISCONNECTED)){
                activate();
                return;
            }

        }

        if(type==PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_ON){
            if(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_DISABLED)==(WifiManager.WIFI_STATE_ENABLED)){
                activate();
                return;
            }
        }

        if(type==PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF){
            if(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_DISABLED)==WifiManager.WIFI_STATE_DISABLED){
                activate();
                //return;
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
