package com.github.ghmxr.timeswitch.services;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.activities.Main;
import com.github.ghmxr.timeswitch.activities.Profile;
import com.github.ghmxr.timeswitch.activities.Settings;
import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.receivers.BatteryReceiver;
import com.github.ghmxr.timeswitch.receivers.NetworkReceiver;
import com.github.ghmxr.timeswitch.runnables.RefreshListItems;
import com.github.ghmxr.timeswitch.timers.CustomTimerTask;
import com.github.ghmxr.timeswitch.utils.LogUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class TimeSwitchService extends Service {

    public static List <TaskItem> list =new ArrayList<>();

    public static AlarmManager alarmManager;

    //public static List<CustomTimerTask> timerList=new ArrayList<>();

    public static LinkedList<TimeSwitchService> service_queue=new LinkedList<>();

    //public SharedPreferences settings;

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

    public static Thread thread_getlist;
    public RefreshListItems runnable_refreshitems;

    private BatteryReceiver batteryReceiver;

    private NetworkReceiver networkReceiver;

    public static  PowerManager.WakeLock wakelock;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("TimeSwitchService","onStartCommand called and queue size is "+service_queue.size());
        if(!service_queue.contains(this)) service_queue.add(this);
        mHandler=new MyHandler();
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
        if(batteryReceiver==null){
           // Log.e("TimeSwitchService","batteryReceiver is null");
            batteryReceiver=new BatteryReceiver(this);
            batteryReceiver.registerReceiver();
        }

        if(networkReceiver==null){
            networkReceiver=new NetworkReceiver(this,null);
            networkReceiver.registerReceiver();
        }

        refreshTaskItems();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

   public void refreshTaskItems(){
        if(thread_getlist!=null){
            thread_getlist.interrupt();
            runnable_refreshitems.setInterrupted();
            thread_getlist=null;
            runnable_refreshitems=null;
        }
        this.runnable_refreshitems=new RefreshListItems(this);
        thread_getlist=new Thread(runnable_refreshitems);
        thread_getlist.start();
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
        if(batteryReceiver!=null) {
            batteryReceiver.unregisterReceiver();
            batteryReceiver=null;
        }
        if(networkReceiver!=null){
            networkReceiver.unregisterReceiver();
            networkReceiver=null;
        }
        if(service_queue.contains(this)) service_queue.remove(this);
        if(wakelock!=null) {
            wakelock.release();
            wakelock=null;
        }
        //mHandler=null;
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
