package com.github.ghmxr.timeswitch.services;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;

import com.github.ghmxr.timeswitch.utils.EnvironmentUtils;

import java.util.LinkedList;

@TargetApi(18)
public class NotificationMonitorService extends NotificationListenerService{

    private static NotificationMonitorService service;
    private static final LinkedList<NotificationListener> callbacks=new LinkedList<>();
    //private static final LinkedList<StatusBarNotification> sbns=new LinkedList<>();

    public interface NotificationListener{
        void onNotificationPosted(StatusBarNotification sbn);
        void onNotificationRemoved(StatusBarNotification sbn);
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        service=this;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        //sbns.add(sbn);
        synchronized (callbacks){
            for(NotificationListener listener:callbacks){
                listener.onNotificationPosted(sbn);
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        //sbns.remove(sbn);
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
        if(notificationMonitorService==null)return;
        StatusBarNotification[] exists=notificationMonitorService.getActiveNotifications();
        if(exists==null)return;
        for(StatusBarNotification sbn:exists){
            if(sbn.getPackageName().equals(package_name))notificationMonitorService.cancelNotification(sbn.getKey());
        }
    }

    public static synchronized void removeAllRemovableNotification(){
        NotificationMonitorService notificationMonitorService=service;
        if(notificationMonitorService==null)return;
        notificationMonitorService.cancelAllNotifications();
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        service=null;
    }

    public static void checkAndRestartService(@NonNull Context context){
        try{
            if(EnvironmentUtils.SpecialPermissionCheckUtil.isReadingNotificationPermissionGranted(context)){
                ComponentName componentName=new ComponentName(context,NotificationMonitorService.class);
                PackageManager packageManager=context.getPackageManager();
                packageManager.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
                packageManager.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
            }
        }catch (Exception e){e.printStackTrace();}
    }
}
