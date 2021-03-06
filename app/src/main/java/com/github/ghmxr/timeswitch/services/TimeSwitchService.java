package com.github.ghmxr.timeswitch.services;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.github.ghmxr.timeswitch.Global;
import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.activities.MainActivity;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.Global.BatteryReceiver;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.receivers.SMSReceiver;
import com.github.ghmxr.timeswitch.triggers.receivers.APReceiver;
import com.github.ghmxr.timeswitch.utils.EnvironmentUtils;
import com.github.ghmxr.timeswitch.utils.LogUtil;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class TimeSwitchService extends Service {

    public static ArrayList <TaskItem> list =new ArrayList<>();//Guarded by TimeSwitchService.class

    public static TimeSwitchService service;

    public static MyHandler mHandler;

    public static final int MESSAGE_REQUEST_REFRESH_TASKS=0;

    public static NotificationCompat.Builder notification=null;

    static boolean flag_refresh_foreground_notification=false;

    private final BatteryReceiver batteryReceiver=new BatteryReceiver();
    private final Global.NetworkReceiver networkReceiver=new Global.NetworkReceiver();
    private final Global.HeadsetPlugReceiver headsetPlugReceiver=new Global.HeadsetPlugReceiver();

    private final BroadcastReceiver log_receiver=new BroadcastReceiver() {
        private boolean lock_network=false;
        private boolean lock_gps=false;
        private boolean lock_wifi_status=false;
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent==null||intent.getAction()==null)return;
            switch (intent.getAction()){
                default:break;
                case WifiManager.WIFI_STATE_CHANGED_ACTION:{
                    int state=intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1);
                    if(state==WifiManager.WIFI_STATE_ENABLED) LogUtil.putLog(TimeSwitchService.this,getResources().getString(R.string.log_wifi_enabled));
                    else if(state==WifiManager.WIFI_STATE_DISABLED) LogUtil.putLog(TimeSwitchService.this,getResources().getString(R.string.log_wifi_disabled));
                }
                break;
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:{
                    WifiInfo info=intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                    if(info!=null&&info.getNetworkId()>=0&&!lock_wifi_status){
                        lock_wifi_status=true;
                        LogUtil.putLog(TimeSwitchService.this,getResources().getString(R.string.log_wifi_connected)+"(SSID:"+info.getSSID()+")");
                    }
                    if((info==null||info.getNetworkId()<0)&&lock_wifi_status){
                        lock_wifi_status=false;
                        LogUtil.putLog(TimeSwitchService.this,getResources().getString(R.string.log_wifi_disconnected));
                    }
                }
                break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:{
                    int state=intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,-1);
                    if(state==BluetoothAdapter.STATE_ON) LogUtil.putLog(TimeSwitchService.this,getResources().getString(R.string.log_bluetooth_enabled));
                    else if(state==BluetoothAdapter.STATE_OFF) LogUtil.putLog(TimeSwitchService.this,getResources().getString(R.string.log_bluetooth_disabled));
                }
                break;
                case AudioManager.RINGER_MODE_CHANGED_ACTION:{
                    int state=intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE,-1);
                    if(state==AudioManager.RINGER_MODE_NORMAL) LogUtil.putLog(TimeSwitchService.this,getResources().getString(R.string.log_audio_mode_normal));
                    else if(state==AudioManager.RINGER_MODE_SILENT) LogUtil.putLog(TimeSwitchService.this,getResources().getString(R.string.log_audio_mode_silent));
                    else if(state==AudioManager.RINGER_MODE_VIBRATE) LogUtil.putLog(TimeSwitchService.this,getResources().getString(R.string.log_audio_mode_vibrate));
                }
                break;
                case ConnectivityManager.CONNECTIVITY_ACTION:{
                    ConnectivityManager manager=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
                    if(manager==null)return;
                    NetworkInfo networkInfo=manager.getActiveNetworkInfo();
                    boolean isMobileNetworkConnected=networkInfo!=null&&networkInfo.getType()==ConnectivityManager.TYPE_MOBILE;
                    if(isMobileNetworkConnected&&!lock_network)
                    {
                        lock_network=true;
                        LogUtil.putLog(TimeSwitchService.this,getResources().getString(R.string.log_network_on));return;
                    }
                    if(!isMobileNetworkConnected&&lock_network)
                    {
                        lock_network=false;
                        LogUtil.putLog(TimeSwitchService.this,getResources().getString(R.string.log_network_off));
                    }
                }
                break;
                case PublicConsts.ACTION_SMS_SENT:{
                    String address=intent.getStringExtra(SMSReceiver.EXTRA_SENT_ADDRESS);
                    LogUtil.putLog(TimeSwitchService.this,getResources().getString(R.string.log_sms_sent2)+" "+address);
                }
                break;
                case Intent.ACTION_AIRPLANE_MODE_CHANGED:{
                    boolean enabled=intent.getBooleanExtra("state",false);
                    LogUtil.putLog(TimeSwitchService.this,getResources().getString(enabled?R.string.log_airplane_mode_on:R.string.log_airplane_mode_off));
                }
                break;
                case "android.location.MODE_CHANGED":{
                    boolean enabled=EnvironmentUtils.isGpsEnabled(context);
                    if(enabled&&!lock_gps){
                        lock_gps=true;
                        LogUtil.putLog(TimeSwitchService.this,getResources().getString(R.string.log_gps_enabled));
                    }
                    else if(!enabled&&lock_gps){
                        lock_gps=false;
                        LogUtil.putLog(TimeSwitchService.this,getResources().getString(R.string.log_gps_disabled));
                    }
                }
                break;
                case APReceiver.ACTION_AP_STATE_CHANGED:{
                    int state=intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1);
                    if(state==APReceiver.AP_STATE_ENABLED)LogUtil.putLog(TimeSwitchService.this,getResources().getString(R.string.log_ap_enabled));
                    else if(state==APReceiver.AP_STATE_DISABLED)LogUtil.putLog(TimeSwitchService.this,getResources().getString(R.string.log_ap_disabled));
                }
                break;
            }
        }
    };

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
        if(getSharedPreferences(PublicConsts.PREFERENCES_NAME,Context.MODE_PRIVATE).getInt(PublicConsts.PREFERENCES_SERVICE_TYPE,PublicConsts.PREFERENCES_SERVICE_TYPE_DEFAULT)==PublicConsts.PREFERENCES_SERVICE_TYPE_FOREGROUND){
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
        try{
            IntentFilter filter=new IntentFilter();
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            filter.addAction(PublicConsts.ACTION_SMS_SENT);
            filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            filter.addAction("android.location.MODE_CHANGED");
            filter.addAction(APReceiver.ACTION_AP_STATE_CHANGED);
            registerReceiver(log_receiver,filter);
        }catch (Exception e){e.printStackTrace();}
        CallStateInvoker.activate(this);
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
                    list= Global.getTaskItemListFromDatabase(TimeSwitchService.this,null);
                    for(TaskItem item:list){
                        if(item==null) continue;
                        if(item.isenabled) item.activateTask(TimeSwitchService.this);
                    }
                    //sendEmptyMessage(TimeSwitchService.MESSAGE_REFRESH_TASKS_COMPLETE);
                    MainActivity.sendEmptyMessage(MainActivity.MESSAGE_GETLIST_COMPLETE);
                    //SettingsActivity.sendEmptyMessage(SettingsActivity.MESSAGE_CHANGE_API_COMPLETE);
                    //Profile.sendEmptyMessage(Profile.MESSAGE_REFRESH_TABLES);

                    new Thread(new RefreshListRemainingTimeTask()).start();
                }
            }
        }).start();
   }

   private void makeThisForeground(){
       NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
       if(Build.VERSION.SDK_INT>=26){
           final String channelID="channel_service";
           NotificationChannel channel=new NotificationChannel(channelID,
                   getResources().getString(R.string.notification_channel_ongoing),
                   NotificationManager.IMPORTANCE_NONE);
           notificationManager.createNotificationChannel(channel);
           notification=new NotificationCompat.Builder(this,channelID);
       }else{
           notification=new NotificationCompat.Builder(this);
       }
       notification.setPriority(NotificationCompat.PRIORITY_MIN);
       notification.setContentIntent(PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT));
       notification.setSmallIcon(R.drawable.ic_launcher);
       notification.setContentTitle("Widget Trigger");
       notification.setContentText("Widget Trigger is running");
       notification.setGroup("OngoingService");
       notification.setGroupSummary(false);
       startForeground(1,getRefreshedNotificationBuilder(notification).build());
       refreshForegroundNotification();
       //Log.d("FOREGROUND","STARTED");
   }

   private void makeThisBackground(){
        flag_refresh_foreground_notification=false;
        stopForeground(true);
   }


    public void processMessage(Message msg){
        switch (msg.what){
            case MESSAGE_REQUEST_REFRESH_TASKS:{
                refreshTaskItems();
                if(getSharedPreferences(PublicConsts.PREFERENCES_NAME,Context.MODE_PRIVATE).getInt(PublicConsts.PREFERENCES_SERVICE_TYPE,PublicConsts.PREFERENCES_SERVICE_TYPE_DEFAULT)==PublicConsts.PREFERENCES_SERVICE_TYPE_FOREGROUND){
                    makeThisForeground();
                }else{
                    makeThisBackground();
                }
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

        try{
            unregisterReceiver(log_receiver);
        }catch (Exception e){e.printStackTrace();}
        CallStateInvoker.stop();

        mHandler=null;
        service=null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (TimeSwitchService.class){
                    for(TaskItem i:list){
                        i.cancelTask();
                    }
                    //other places may user the values of this list
                    //list.clear();
                    System.gc();
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

    public static void stopService(){
        if(service!=null) service.stopSelf();
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

    public static class CallStateInvoker extends PhoneStateListener {

        private static CallStateInvoker invoker;
        private final TelephonyManager manager;
        private static final LinkedList<CallStateChangedCallback> callbacks=new LinkedList<>();
        private static int state=0;

        public interface CallStateChangedCallback{
            void onCallStateChanged(int state, String phoneNumber);
        }

        /**
         * 激活电话状态监听回调器
         * 此方法必须在主UI线程调用
         */
        private static void activate(final Context context){
            if(invoker!=null) return;
            invoker=new CallStateInvoker(context);
        }

        /**
         * 停止电话状态监听回调器
         * 此方法须在主UI线程调用
         */
        private static void stop(){
            callbacks.clear();
            if(invoker==null)return;
            invoker.removeVariables();
            invoker=null;
        }

        private CallStateInvoker(@NonNull Context context){
            manager=(TelephonyManager)context.getApplicationContext().getSystemService(TELEPHONY_SERVICE);
            if(manager==null)return;
            manager.listen(this,PhoneStateListener.LISTEN_CALL_STATE);
        }

        private void removeVariables(){
            if(manager==null)return;
            manager.listen(this,PhoneStateListener.LISTEN_NONE);
        }

        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            super.onCallStateChanged(state, phoneNumber);
            CallStateInvoker.state=state;
            //Log.e("callcall",""+state+"  "+phoneNumber);//来电1，接听2，挂断0，电话号码为连续数字例如13011121113
            for(CallStateChangedCallback callback:callbacks){
                callback.onCallStateChanged(state,phoneNumber);
            }
        }

        /**
         * 注册电话状态监听回调，此回调器在TimeswitchService生命周期内生效
         * @param callback 回调接口
         */
        public static synchronized void registerCallback(CallStateChangedCallback callback){
            if(!callbacks.contains(callback))callbacks.add(callback);
        }

        /**
         * 取消注册电话状态监听回调
         * @param callback 要取消的回调接口
         */
        public static synchronized void unregisterCallback(CallStateChangedCallback callback){
            callbacks.remove(callback);
        }

        /**
         * 获取当前电话状态。
         * @return 电话状态，来电1，接听2，挂断0
         */
        public static int getCallState(){
            return state;
        }
    }
}
