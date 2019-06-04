package com.github.ghmxr.timeswitch.triggers.receivers;

import android.annotation.TargetApi;
import android.content.Context;
import android.service.notification.StatusBarNotification;

import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.services.NotificationMonitorService;
import com.github.ghmxr.timeswitch.triggers.Trigger;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

import java.util.Arrays;

public class NotificationReceiver implements Trigger,NotificationMonitorService.NotificationListener {
    private Context context;
    private TaskItem item;

    public NotificationReceiver(Context context,TaskItem item){
        this.context=context;
        this.item=item;
    }

    @Override
    @TargetApi(18)
    public void onNotificationPosted(StatusBarNotification sbn) {
        if(item==null)return;
        if(Arrays.asList(item.package_names).contains(sbn.getPackageName())) runProcessTaskItem();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {}

    @Override
    public void activate() {
        NotificationMonitorService.registerNotificationReceiver(this);
    }

    @Override
    public void cancel() {
        NotificationMonitorService.unregisterNotificationReceiver(this);
    }

    private void runProcessTaskItem(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    //new ProcessTaskItem(context,item).checkExceptionsAndRunActions();
                    ProcessTaskItem.checkExceptionsAndRunActions(context,item);
                }catch (Exception e){e.printStackTrace();}
            }
        }).start();
    }

}
