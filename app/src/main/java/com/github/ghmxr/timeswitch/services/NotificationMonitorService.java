package com.github.ghmxr.timeswitch.services;

import android.annotation.TargetApi;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.LinkedList;

@TargetApi(18)
public class NotificationMonitorService extends NotificationListenerService{

    private static final LinkedList<NotificationListener> callbacks=new LinkedList<>();

    public interface NotificationListener{
        void onNotificationPosted(StatusBarNotification sbn);
        void onNotificationRemoved(StatusBarNotification sbn);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        synchronized (callbacks){
            for(NotificationListener listener:callbacks){
                listener.onNotificationPosted(sbn);
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
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
}
