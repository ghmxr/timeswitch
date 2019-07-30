package com.github.ghmxr.timeswitch.services;

import android.annotation.TargetApi;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;

import java.util.LinkedList;

@TargetApi(18)
public class NotificationMonitorService extends NotificationListenerService{

    private static NotificationMonitorService service;
    private static final LinkedList<NotificationListener> callbacks=new LinkedList<>();
    private static final LinkedList<StatusBarNotification> sbns=new LinkedList<>();

    public interface NotificationListener{
        void onNotificationPosted(StatusBarNotification sbn);
        void onNotificationRemoved(StatusBarNotification sbn);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        service=this;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        sbns.add(sbn);
        synchronized (callbacks){
            for(NotificationListener listener:callbacks){
                listener.onNotificationPosted(sbn);
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        sbns.remove(sbn);
        synchronized (callbacks){
            for(NotificationListener listener:callbacks){
                listener.onNotificationRemoved(sbn);
            }
        }
    }

    public static void registerNotificationReceiver(NotificationListener listener){
        synchronized (callbacks){
            if(!callbacks.contains(listener)) callbacks.addLast(listener);
        }
    }

    public static void unregisterNotificationReceiver(NotificationListener listener){
        synchronized (callbacks){
            if(callbacks.contains(listener)) callbacks.remove(listener);
        }
    }

    @TargetApi(21)
    public static synchronized void removeNotification(@NonNull String package_name){
        NotificationMonitorService notificationMonitorService=service;
        if(service==null)return;
        for(StatusBarNotification sbn:sbns){
            if(sbn.getPackageName().equals(package_name))notificationMonitorService.cancelNotification(sbn.getKey());
        }
    }

    public static synchronized void removeAllRemovableNotification(){
        NotificationMonitorService notificationMonitorService=service;
        if(service==null)return;
        service.cancelAllNotifications();
    }

    @Override
    public void onDestroy(){
        service=null;
    }
}
