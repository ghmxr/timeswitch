package com.github.ghmxr.timeswitch.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.services.AppLaunchingDetectionService;

public class AppLaunchDetectionReceiver extends BroadcastReceiver implements Runnable {
    private TaskItem item;
    private Context context;

    public AppLaunchDetectionReceiver(Context context, TaskItem item) {
        this.context=context;
        this.item=item;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null) return;
        if(intent.getAction()==null) return;
        if(intent.getAction().equals(AppLaunchingDetectionService.ACTION_LAUNCH_INFO_CHANGED)){
            String package_name=intent.getStringExtra(AppLaunchingDetectionService.EXTRA_PACKAGE_NAME);
            boolean if_launched=intent.getBooleanExtra(AppLaunchingDetectionService.EXTRA_IF_RUNNING,false);
            if(package_name==null) return;
            Log.d("AppReceiver",package_name+" is "+(if_launched?"LAUNCHED":"CLOSED"));
        }
    }

    public void registerReceiver(){
        try{
            context.registerReceiver(this,new IntentFilter(AppLaunchingDetectionService.ACTION_LAUNCH_INFO_CHANGED));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void unregisterReceiver(){
        try{
            context.unregisterReceiver(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

    }
}
