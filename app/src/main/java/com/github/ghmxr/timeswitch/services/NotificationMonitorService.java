package com.github.ghmxr.timeswitch.services;

import android.annotation.TargetApi;

import android.content.Context;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.LinkedList;

@TargetApi(18)
public class NotificationMonitorService extends NotificationListenerService{

    private static final LinkedList<NotificationListener> callbacks=new LinkedList<>();
    private static NotificationMonitorService service;
    public interface NotificationListener{
        void onNotificationPosted(StatusBarNotification sbn);
        void onNotificationRemoved(StatusBarNotification sbn);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        service=this;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        service=null;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        for(NotificationListener listener:callbacks){
            listener.onNotificationPosted(sbn);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        for(NotificationListener listener:callbacks){
            listener.onNotificationRemoved(sbn);
        }
    }

    public static void registerNotificationReceiver(Context context, NotificationListener listener){
        if(!callbacks.contains(listener)) callbacks.addLast(listener);
        if(service==null)context.startService(new Intent(context,NotificationMonitorService.class));
    }

    public static void unregisterNotificationReceiver(NotificationListener listener){
        if(callbacks.contains(listener)) callbacks.remove(listener);
        if(callbacks.size()==0&&service!=null) service.stopSelf();
    }
}
