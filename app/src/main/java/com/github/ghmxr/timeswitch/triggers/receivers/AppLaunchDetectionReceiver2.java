package com.github.ghmxr.timeswitch.triggers.receivers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.services.AppLaunchingDetectionService;
import com.github.ghmxr.timeswitch.triggers.Trigger;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

import java.util.Arrays;

public class AppLaunchDetectionReceiver2 implements Trigger,AppLaunchingDetectionService.AppLaunchingDetectionCallback {

    private Context context;
    private TaskItem item;
    private boolean mLock=false;

    public AppLaunchDetectionReceiver2(@NonNull Context context, @NonNull TaskItem item){
        this.context=context;
        this.item=item;
    }

    @Override
    public void onAppOpened(String package_name) {
        if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_APP_LAUNCHED){
            if(Arrays.asList(item.package_names).contains(package_name)) runProcessTaskItem();
        }else if(item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_APP_CLOSED){
            if(Arrays.asList(item.package_names).contains(package_name)) mLock=false;
        }
    }

    @Override
    public void onAppClosed(String package_name) {
        if(item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_APP_CLOSED){
            if(Arrays.asList(item.package_names).contains(package_name))runProcessTaskItem();
        }else if(item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_APP_LAUNCHED){
            if(Arrays.asList(item.package_names).contains(package_name))mLock=false;
        }
    }

    @Override
    public void activate() {
        AppLaunchingDetectionService.registerCallback(context,this);
    }

    @Override
    public void cancel() {
        AppLaunchingDetectionService.unregisterCallback(this);
    }

    private void runProcessTaskItem(){
        if(!mLock){
            mLock=true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        ProcessTaskItem.checkExceptionsAndRunActions(context,item);
                    }catch (Exception e){e.printStackTrace();}
                }
            }).start();
        }
    }
}
