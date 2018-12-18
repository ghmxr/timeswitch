package com.github.ghmxr.timeswitch.services;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.activities.Main;
import com.github.ghmxr.timeswitch.activities.Profile;
import com.github.ghmxr.timeswitch.activities.Settings;
import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.receivers.BatteryReceiver;
import com.github.ghmxr.timeswitch.receivers.HeadsetPlugReceiver;
import com.github.ghmxr.timeswitch.receivers.NetworkReceiver;
import com.github.ghmxr.timeswitch.runnables.RefreshListItems;
import com.github.ghmxr.timeswitch.timers.CustomTimerTask;
import com.github.ghmxr.timeswitch.utils.LogUtil;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class TimeSwitchService extends Service {

    public static List <TaskItem> list =new ArrayList<>();

    public static AlarmManager alarmManager;

    public static LinkedList<TimeSwitchService> service_queue=new LinkedList<>();

    public static MyHandler mHandler;

    public static final int MESSAGE_REQUEST_REFRESH_TASKS=0x00000;
    public static final int MESSAGE_REFRESH_TASKS_COMPLETE =0x00001;
    /**
     * message.obj = String.valueOf("DisplayContent");
     */
    public static final int MESSAGE_DISPLAY_TOAST=0x00002;
    /**
     * message.obj=new TimeSwitchService.CustomToast();
     */
    public static final int MESSAGE_DISPLAY_CUSTOM_TOAST=0x00003;
    public static final int MESSAGE_START_FOREGROUND=10;

    //public Thread thread_getlist;

    public RefreshListItems runnable_refreshitems;

    private BatteryReceiver batteryReceiver;

    private NetworkReceiver networkReceiver;

    private HeadsetPlugReceiver headsetPlugReceiver;

    public static  PowerManager.WakeLock wakelock;

    public static NotificationCompat.Builder notification=null;

    static boolean flag_refresh_foreground_notification=false;

    //private boolean if_start_foreground_in_onstart_method=false;

    @Override
    public void onCreate(){
        super.onCreate();
        mHandler=new MyHandler();
        if(!service_queue.contains(this)) service_queue.add(this);
        Log.i("TimeSwitchService","onCreate called and queue size is "+service_queue.size());
        if(getSharedPreferences(PublicConsts.PREFERENCES_NAME,Context.MODE_PRIVATE).getInt(PublicConsts.PREFERENCES_SERVICE_TYPE,PublicConsts.PREFERENCES_SERVICE_TYPE_DEFAULT)==PublicConsts.PREFERENCES_SERVICE_TYPE_FORGROUND){
            makeThisForeground();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("TimeSwitchService","onStartCommand called");
        if(alarmManager==null) {
            alarmManager=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
        }
        SharedPreferences settings=getSharedPreferences(PublicConsts.PREFERENCES_NAME, Activity.MODE_PRIVATE);
        if(settings.getInt(PublicConsts.PREFERENCES_API_TYPE,PublicConsts.PREFERENCES_API_TYPE_DEFAULT)==PublicConsts.API_ANDROID_ALARM_MANAGER){
           CustomTimerTask.timerTaskStatus=null;
            if(wakelock!=null){
                wakelock.release();
                wakelock=null;
            }
        }

        try{
            if(batteryReceiver!=null){
                batteryReceiver.unregisterReceiver();
            }
        }catch (Exception e){e.printStackTrace();}

        batteryReceiver=new BatteryReceiver(this,null);
        batteryReceiver.registerReceiver();

        try{
            if(networkReceiver!=null){
                networkReceiver.unregisterReceiver();
            }
        }catch (Exception e){e.printStackTrace();}

        networkReceiver=new NetworkReceiver(this,null);
        networkReceiver.registerReceiver();

        try{
            if(headsetPlugReceiver!=null){
                headsetPlugReceiver.unregisterReceiver();
            }
        }catch (Exception e){e.printStackTrace();}

        headsetPlugReceiver=new HeadsetPlugReceiver(this,null);
        headsetPlugReceiver.registerReceiver();

        refreshTaskItems();

        //startService(new Intent(this,AppLaunchingDetectionService.class));
        //new AppLaunchDetectionReceiver(this,null).registerReceiver();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

   public void refreshTaskItems(){
        if(runnable_refreshitems!=null){
            runnable_refreshitems.setInterrupted();
            //runnable_refreshitems=null;
        }
        this.runnable_refreshitems=new RefreshListItems(this);
        new Thread(runnable_refreshitems).start();
   }

   private void makeThisForeground(){
       NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
       if(Build.VERSION.SDK_INT>=26){
           final String channelID="channel_service";
           NotificationChannel channel=new NotificationChannel(channelID,"Service",NotificationManager.IMPORTANCE_DEFAULT);
           notificationManager.createNotificationChannel(channel);
           notification=new NotificationCompat.Builder(this,channelID);
       }else{
           notification=new NotificationCompat.Builder(this);
       }
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
                Main.sendEmptyMessage(Main.MESSAGE_GETLIST_COMPLETE);
                Settings.sendEmptyMessage(Settings.MESSAGE_CHANGE_API_COMPLETE);
                Profile.sendEmptyMessage(Profile.MESSAGE_REFRESH_TABLES);
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
                    if(Integer.parseInt(content.toast_value[PublicConsts.TOAST_TYPE_LOCALE])==PublicConsts.TOAST_TYPE_CUSTOM){
                        toast.setGravity(Gravity.TOP|Gravity.START,
                                Integer.parseInt(content.toast_value[PublicConsts.TOAST_LOCATION_X_OFFSET_LOCALE]),
                                Integer.parseInt(content.toast_value[PublicConsts.TOAST_LOCATION_Y_OFFSET_LOCALE]));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    LogUtil.putExceptionLog(this,e);
                }
                toast.show();
            }
            break;
            /*case MESSAGE_START_FOREGROUND:{
                NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                if(Build.VERSION.SDK_INT>=26){
                    final String channelID="channel_service";
                    NotificationChannel channel=new NotificationChannel(channelID,"Service",NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                    notification=new NotificationCompat.Builder(this,channelID);
                }else{
                    notification=new NotificationCompat.Builder(this);
                }
                startForeground(1,getRefreshedNotificationBuilder(notification).build());
                refreshForegroundNotification();
                Log.d("FOREGROUND","STARTED");
            }
            break;*/
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
        try{
            flag_refresh_foreground_notification=false;
            if(batteryReceiver!=null) {
                batteryReceiver.unregisterReceiver();
                batteryReceiver=null;
            }
            if(networkReceiver!=null){
                networkReceiver.unregisterReceiver();
                networkReceiver=null;
            }
            if(headsetPlugReceiver!=null){
                headsetPlugReceiver.unregisterReceiver();
                headsetPlugReceiver=null;
            }
            if(AppLaunchingDetectionService.queue.size()>0){
                AppLaunchingDetectionService.queue.getLast().stopSelf();
            }
        }catch (Exception e){e.printStackTrace();}
        if(service_queue.contains(this)) service_queue.remove(this);
        if(wakelock!=null) {
            wakelock.release();
            wakelock=null;
        }
        for(TaskItem i:list){
            i.cancelTrigger();
        }
        list.clear();
        Log.d("TimeSwitchService","onDestroy method called");
    }

    private NotificationCompat.Builder getRefreshedNotificationBuilder(NotificationCompat.Builder builder){
        try{
            builder.setSmallIcon(R.drawable.ic_launcher);
            builder.setContentTitle("Widget Trigger");
            builder.setContentText("Widget Trigger is running");
            RemoteViews remoteViews=new RemoteViews(PublicConsts.PACKAGE_NAME,R.layout.layout_foreground_notification);
            String title;
            if(ProcessTaskItem.last_activated_task_name.equals("")){
                title=getResources().getString(R.string.notification_title_nothing);
            }else {
                title=getResources().getString(R.string.notification_title_font)+ProcessTaskItem.last_activated_task_name;
            }
            remoteViews.setTextViewText(R.id.notification_title, title);
            remoteViews.setTextViewText(R.id.notification_battery_percentage,BatteryReceiver.Battery_percentage+"%");
            if(Build.VERSION.SDK_INT>=21){
                try{
                    BatteryManager batteryManager=(BatteryManager)getSystemService(Context.BATTERY_SERVICE);
                    int current=batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)/1000;
                    if(current<0) current=0-current;
                    remoteViews.setTextViewText(R.id.notification_battery_info,(double)BatteryReceiver.Battery_voltage/1000+"V , "+current+"mA");
                }catch (Exception e){
                    e.printStackTrace();
                    remoteViews.setTextViewText(R.id.notification_battery_info,(double)BatteryReceiver.Battery_voltage/1000+"V");
                }
            }else remoteViews.setTextViewText(R.id.notification_battery_info,(double)BatteryReceiver.Battery_voltage/1000+"V");
            remoteViews.setTextViewText(R.id.notification_battery_temp,(double)BatteryReceiver.Battery_temperature/10+getResources().getString(R.string.degree_celsius));
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
                if(service_queue.size()==0) context.startService(new Intent(context, TimeSwitchService.class));
                else {
                    service_queue.getLast().makeThisBackground();
                    service_queue.getLast().refreshTaskItems();
                }
                if(AppLaunchingDetectionService.queue.size()>0) AppLaunchingDetectionService.queue.getLast().makeThisBackground();
            }else{
                if(service_queue.size()==0){
                    if(Build.VERSION.SDK_INT>=26)context.startForegroundService(new Intent(context,TimeSwitchService.class));
                    else context.startService(new Intent(context,TimeSwitchService.class));
                }else{
                    service_queue.getLast().refreshTaskItems();
                    service_queue.getLast().makeThisForeground();
                }
                if(AppLaunchingDetectionService.queue.size()>0) AppLaunchingDetectionService.queue.getLast().makeThisForeGround();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void refreshForegroundNotification(){
        try{
            if(!flag_refresh_foreground_notification){
                flag_refresh_foreground_notification=true;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (flag_refresh_foreground_notification){
                            mHandler.postDelayed(this,2000);
                            if(notification!=null) startForeground(1,getRefreshedNotificationBuilder(notification).build());
                        }else {
                            stopForeground(true);
                        }
                    }
                });
            }

        }catch (Exception e){e.printStackTrace();}
    }

    private static class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try{
                if(service_queue.size()>0) service_queue.getLast().processMessage(msg);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static final class CustomToast{
       public String toast_value[]=new String[3];
       public String toast="";
    }
}
