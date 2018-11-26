package com.github.ghmxr.timeswitch.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.services.AppLaunchingDetectionService;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

import java.util.HashMap;
import java.util.Map;

public class AppLaunchDetectionReceiver extends BroadcastReceiver implements Runnable {
    private TaskItem item;
    private Context context;
    private Map<String,Boolean> packageLock=new HashMap<>();
    public AppLaunchDetectionReceiver(Context context, TaskItem item) {
        this.context=context;
        this.item=item;
        if(item==null||item.package_names==null||item.package_names.length==0) return;
        for(String name:item.package_names){
            if(name.trim().equals("")) continue;
            packageLock.put(name,false);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null) return;
        if(intent.getAction()==null) return;
        if(intent.getAction().equals(AppLaunchingDetectionService.ACTION_LAUNCH_INFO_CHANGED)){
            String package_name=intent.getStringExtra(AppLaunchingDetectionService.EXTRA_PACKAGE_NAME);
            boolean if_launched=intent.getBooleanExtra(AppLaunchingDetectionService.EXTRA_IF_RUNNING,false);
            if(package_name==null) return;
            //Log.d("AppReceiver",package_name+" is "+(if_launched?"LAUNCHED":"CLOSED"));
            String [] packageNames=item.package_names;
            if(packageNames==null||packageNames.length==0) return;
            if(item.trigger_type== PublicConsts.TRIGGER_TYPE_APP_LAUNCHED){
                for(String packagename:packageNames){
                    if(packagename.equals(package_name)){
                        if(if_launched) activate(packagename);
                        else packageLock.put(packagename,false);
                    }
                }
            }

            if(item.trigger_type==PublicConsts.TRIGGER_TYPE_APP_CLOSED){
                for(String packagename:packageNames){
                    if(packagename.equals(package_name)){
                        if(!if_launched) activate(packagename);
                        else packageLock.put(packagename,false);
                    }
                }
            }
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

    private void activate(String package_name){
        if(!packageLock.get(package_name)){
            packageLock.put(package_name,true);
            new Thread(this).start();
        }
    }

    @Override
    public void run() {
        new ProcessTaskItem(context,item).activateTaskItem();
    }
}
