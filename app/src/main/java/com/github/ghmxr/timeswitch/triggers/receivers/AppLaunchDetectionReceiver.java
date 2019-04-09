package com.github.ghmxr.timeswitch.triggers.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.services.AppLaunchingDetectionService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class AppLaunchDetectionReceiver extends BaseBroadcastReceiver{
    static LinkedList<AppLaunchDetectionReceiver> app_detecting_receivers=new LinkedList<>();
    private Map<String,Boolean> packageLock=new HashMap<>();
    public AppLaunchDetectionReceiver(Context context, TaskItem item) {
        super(context,item);
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
                        if(if_launched) runActions(packagename);
                        else packageLock.put(packagename,false);
                    }
                }
            }

            if(item.trigger_type==PublicConsts.TRIGGER_TYPE_APP_CLOSED){
                for(String packagename:packageNames){
                    if(packagename.equals(package_name)){
                        if(!if_launched) runActions(packagename);
                        else packageLock.put(packagename,false);
                    }
                }
            }
        }
    }

    @Override
    public void activate() {
        try{
            if(!app_detecting_receivers.contains(this)) app_detecting_receivers.add(this);
            AppLaunchingDetectionService.startService(context);
        }catch (Exception e){e.printStackTrace();}
        try{
            context.registerReceiver(this,new IntentFilter(AppLaunchingDetectionService.ACTION_LAUNCH_INFO_CHANGED));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        try{
            if(app_detecting_receivers.contains(this)) app_detecting_receivers.remove(this);
            if(app_detecting_receivers.size()==0) AppLaunchingDetectionService.queue.getLast().stopDetecting();
        }catch (Exception e){e.printStackTrace();}
    }

    private void runActions(String package_name){
        if(!packageLock.get(package_name)){
            packageLock.put(package_name,true);
            runProcessTask();
        }
    }

}
