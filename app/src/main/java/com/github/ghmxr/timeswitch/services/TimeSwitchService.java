package com.github.ghmxr.timeswitch.services;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.Global;
import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.activities.MainActivity;
import com.github.ghmxr.timeswitch.activities.Profile;
import com.github.ghmxr.timeswitch.activities.Settings;
import com.github.ghmxr.timeswitch.data.v2.ActionConsts;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.Global.BatteryReceiver;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.utils.LogUtil;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.util.ArrayList;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class TimeSwitchService extends Service {

    public static ArrayList <TaskItem> list =new ArrayList<>();

    public static TimeSwitchService service;

    public static MyHandler mHandler;

    public static final int MESSAGE_REQUEST_REFRESH_TASKS=0;
    public static final int MESSAGE_REFRESH_TASKS_COMPLETE =1;
    /**
     * message.obj = String.valueOf("DisplayContent");
     */
    public static final int MESSAGE_DISPLAY_TOAST=2;
    /**
     * message.obj=new TimeSwitchService.CustomToast();
     */
    public static final int MESSAGE_DISPLAY_CUSTOM_TOAST=3;

    public static NotificationCompat.Builder notification=null;

    static boolean flag_refresh_foreground_notification=false;

    private final BatteryReceiver batteryReceiver=new BatteryReceiver();
    private final Global.NetworkReceiver networkReceiver=new Global.NetworkReceiver();
    private final Global.HeadsetPlugReceiver headsetPlugReceiver=new Global.HeadsetPlugReceiver();

    @Override
    public void onCreate(){
        super.onCreate();
        if(service!=null){
            service.stopSelf();
            service=null;
        }
        if(mHandler==null) mHandler=new MyHandler();
        service=this;
        //Log.i("TimeSwitchService","onCreate called and queue size is "+service_queue.size());
        if(getSharedPreferences(PublicConsts.PREFERENCES_NAME,Context.MODE_PRIVATE).getInt(PublicConsts.PREFERENCES_SERVICE_TYPE,PublicConsts.PREFERENCES_SERVICE_TYPE_DEFAULT)==PublicConsts.PREFERENCES_SERVICE_TYPE_FORGROUND){
            makeThisForeground();
        }
        try{
            registerReceiver(batteryReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }catch (Exception e){e.printStackTrace();}
        try{
            IntentFilter filter=new IntentFilter();
            filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            filter.addAction("android.net.wifi.STATE_CHANGE");
            registerReceiver(networkReceiver,filter);
        }catch (Exception e){e.printStackTrace();}
        try{
            registerReceiver(headsetPlugReceiver,new IntentFilter("android.intent.action.HEADSET_PLUG"));
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("TimeSwitchService","onStartCommand called");
        refreshTaskItems();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

   public void refreshTaskItems(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (TimeSwitchService.class){
                    for(TaskItem item:list){
                        if(item==null) continue;
                        item.cancelTask();
                    }
                    list= Global.getTaskItemListFromDatabase(TimeSwitchService.this);
                    for(TaskItem item:list){
                        if(item==null) continue;
                        if(item.isenabled) item.activateTask(TimeSwitchService.this);
                    }
                    sendEmptyMessage(TimeSwitchService.MESSAGE_REFRESH_TASKS_COMPLETE);
                }
            }
        }).start();
   }

   private void makeThisForeground(){
       NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
       if(Build.VERSION.SDK_INT>=26){
           final String channelID="channel_service";
           NotificationChannel channel=new NotificationChannel(channelID,"OngoingService",NotificationManager.IMPORTANCE_LOW);
           notificationManager.createNotificationChannel(channel);
           notification=new NotificationCompat.Builder(this,channelID);
       }else{
           notification=new NotificationCompat.Builder(this);
       }
       notification.setContentIntent(PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT));
       notification.setSmallIcon(R.drawable.ic_launcher);
       notification.setContentTitle("Widget Trigger");
       notification.setContentText("Widget Trigger is running");
       startForeground(1,getRefreshedNotificationBuilder(notification).build());
       refreshForegroundNotification();
       Log.d("FOREGROUND","STARTED");
   }

   private void makeThisBackground(){
        flag_refresh_foreground_notification=false;
        stopForeground(true);
   }


    public void processMessage(Message msg){
        switch (msg.what){
            case MESSAGE_REQUEST_REFRESH_TASKS:{
                refreshTaskItems();
            }
            break;
            case MESSAGE_REFRESH_TASKS_COMPLETE:{
                MainActivity.sendEmptyMessage(MainActivity.MESSAGE_GETLIST_COMPLETE);
                Settings.sendEmptyMessage(Settings.MESSAGE_CHANGE_API_COMPLETE);
                Profile.sendEmptyMessage(Profile.MESSAGE_REFRESH_TABLES);

                new Thread(new RefreshListRemainingTimeTask()).start();
            }
            break;
            case MESSAGE_DISPLAY_TOAST:{
                String text=(String)msg.obj;
                Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
            }
            break;
            case MESSAGE_DISPLAY_CUSTOM_TOAST:{
                CustomToast content=(CustomToast) msg.obj;
                Toast toast=Toast.makeText(this,content.toast,Toast.LENGTH_SHORT);
                try{
                    if(Integer.parseInt(content.toast_value[ActionConsts.ActionSecondLevelLocaleConsts.TOAST_TYPE_LOCALE])== ActionConsts.ActionValueConsts.TOAST_TYPE_CUSTOM){
                        toast.setGravity(Gravity.TOP|Gravity.START,
                                Integer.parseInt(content.toast_value[ActionConsts.ActionSecondLevelLocaleConsts.TOAST_LOCATION_X_OFFSET_LOCALE]),
                                Integer.parseInt(content.toast_value[ActionConsts.ActionSecondLevelLocaleConsts.TOAST_LOCATION_Y_OFFSET_LOCALE]));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    LogUtil.putExceptionLog(this,e);
                }
                toast.show();
            }
            break;
        }
    }

    public static void sendEmptyMessage(int what){
        if(mHandler!=null) mHandler.sendEmptyMessage(what);
    }

    public static void sendMessage(Message msg){
        if(mHandler!=null) mHandler.sendMessage(msg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        flag_refresh_foreground_notification=false;

        if(AppLaunchingDetectionService.service!=null){
            AppLaunchingDetectionService.service.stopSelf();
        }

        try{
            unregisterReceiver(batteryReceiver);
        }catch (Exception e){e.printStackTrace();}

        try{
            unregisterReceiver(networkReceiver);
        }catch (Exception e){e.printStackTrace();}

        try{
            unregisterReceiver(headsetPlugReceiver);
        }catch (Exception e){e.printStackTrace();}

        service=null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (TimeSwitchService.class){
                    for(TaskItem i:list){
                        i.cancelTask();
                    }
                    list.clear();
                }
            }
        }).start();

        Log.d("TimeSwitchService","onDestroy method called");
    }

    private NotificationCompat.Builder getRefreshedNotificationBuilder(NotificationCompat.Builder builder){
        try{
            RemoteViews remoteViews=new RemoteViews(PublicConsts.PACKAGE_NAME,R.layout.layout_foreground_notification);
            String title;
            if(ProcessTaskItem.last_activated_task_name.equals("")){
                title=getResources().getString(R.string.notification_title_nothing);
            }else {
                title=getResources().getString(R.string.notification_title_font)+ProcessTaskItem.last_activated_task_name;
            }
            remoteViews.setTextViewText(R.id.notification_title, title);
            remoteViews.setTextViewText(R.id.notification_battery_percentage, BatteryReceiver.battery_percentage+"%");
            if(Build.VERSION.SDK_INT>=21){
                try{
                    BatteryManager batteryManager=(BatteryManager)getSystemService(Context.BATTERY_SERVICE);
                    int current=batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)/1000;
                    if(current<0) current=0-current;
                    remoteViews.setTextViewText(R.id.notification_battery_info,(double)BatteryReceiver.battery_voltage/1000+"V , "+current+"mA");
                }catch (Exception e){
                    e.printStackTrace();
                    remoteViews.setTextViewText(R.id.notification_battery_info,(double)BatteryReceiver.battery_voltage/1000+"V");
                }
            }else remoteViews.setTextViewText(R.id.notification_battery_info,(double)BatteryReceiver.battery_voltage/1000+"V");
            remoteViews.setTextViewText(R.id.notification_battery_temp,(double)BatteryReceiver.battery_temperature/10+getResources().getString(R.string.degree_celsius));
            builder.setCustomContentView(remoteViews);
        }catch (Exception e){
            e.printStackTrace();
        }
        return builder;
    }

    public static void startService(@NonNull Context context){
        try{
            //if(service_queue.size()>0) service_queue.getLast().stopSelf();
            boolean isBackground=context.getSharedPreferences(PublicConsts.PREFERENCES_NAME,Activity.MODE_PRIVATE)
                    .getInt(PublicConsts.PREFERENCES_SERVICE_TYPE,PublicConsts.PREFERENCES_SERVICE_TYPE_DEFAULT)==PublicConsts.PREFERENCES_SERVICE_TYPE_BACKGROUND;
            if(isBackground){
                if(service==null) context.startService(new Intent(context, TimeSwitchService.class));
                else {
                    service.makeThisBackground();
                    service.refreshTaskItems();
                }
                if(AppLaunchingDetectionService.service!=null) AppLaunchingDetectionService.service.makeThisBackground();
            }else{
                if(service==null){
                    if(Build.VERSION.SDK_INT>=26)context.startForegroundService(new Intent(context,TimeSwitchService.class));
                    else context.startService(new Intent(context,TimeSwitchService.class));
                }else{
                    service.refreshTaskItems();
                    service.makeThisForeground();
                }
                if(AppLaunchingDetectionService.service!=null) AppLaunchingDetectionService.service.makeThisForeGround();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void refreshForegroundNotification(){
        if(!flag_refresh_foreground_notification){
            flag_refresh_foreground_notification=true;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (flag_refresh_foreground_notification){
                        try{
                            mHandler.postDelayed(this,2000);
                            if(notification!=null) startForeground(1,getRefreshedNotificationBuilder(notification).build());
                        }catch (Exception e){e.printStackTrace();}
                    }else {
                        try{stopForeground(true);}catch (Exception e){e.printStackTrace();}
                    }
                }
            });
        }
    }

    private static class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try{
                if(service!=null) service.processMessage(msg);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static final class CustomToast{
       public String toast_value[]=new String[3];
       public String toast="";
    }


    private static class RefreshListRemainingTimeTask implements Runnable{
        private boolean active=true;
        private final ArrayList<TaskItem> list;
        private RefreshListRemainingTimeTask(){
            this.list=TimeSwitchService.list;
        }
        @Override
        public void run() {
           while (active){
               if(list!=TimeSwitchService.list){
                   active=false;
                   //list=null;
                   Log.d("TList","The global list has been changed and this runnable returns!! "+Runtime.getRuntime().freeMemory());
                   return;
               }
               synchronized (list){
                   try{
                       for(TaskItem item:TimeSwitchService.list){
                           if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME){
                               long remaining=item.getRemainingTimeOfTypeLoopByCertainTime();
                               if(remaining<=0) remaining=0;
                               int day=(int)(remaining/(1000*60*60*24));
                               int hour=(int)((remaining%(1000*60*60*24))/(1000*60*60));
                               int minute=(int)((remaining%(1000*60*60))/(1000*60));
                               int second=(int)((remaining%(1000*60))/1000);
                               String display;
                               if(day>0){
                                   display=day+":"+ ValueUtils.format(hour)+":"+ValueUtils.format(minute)+":"+ValueUtils.format(second);
                               }else if(hour>0){
                                   display=ValueUtils.format(hour)+":"+ValueUtils.format(minute)+":"+ValueUtils.format(second);
                               }else if(minute>0){
                                   display=ValueUtils.format(minute)+":"+ValueUtils.format(second);
                               }else{
                                   display=ValueUtils.format(second)+"s";
                               }
                               if(item.isenabled) {
                                   item.display_trigger=display;
                               }
                               else item.display_trigger="Off";
                           }
                       }
                       Thread.sleep(500);
                       //Log.d("Thread_REFRESH_A","Thread sleep!!!");
                   }catch (Exception e){e.printStackTrace();}
               }
           }
        }
    }
}
