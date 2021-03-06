package com.github.ghmxr.timeswitch.utils;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.github.ghmxr.timeswitch.Global;
import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.activities.MainActivity;
import com.github.ghmxr.timeswitch.adapters.ContentAdapter;
import com.github.ghmxr.timeswitch.data.v2.ActionConsts;
import com.github.ghmxr.timeswitch.data.v2.AdditionConsts;
import com.github.ghmxr.timeswitch.data.v2.ExceptionConsts;
import com.github.ghmxr.timeswitch.data.v2.MySQLiteOpenHelper;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.data.v2.SQLConsts;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.services.NotificationMonitorService;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理并执行TaskItem中的exceptions,actions和一些附加选项的类
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class ProcessTaskItem {

    static final String TAG="ProcessTaskItem";
    public static String last_activated_task_name="";
    private static int notification_id=2;

    private static Thread flash_light_thread;
    private static final Map<Integer,Thread> delayeds=new HashMap<>();

    public static synchronized void checkExceptionsAndRunActions(@NonNull Context context,@NonNull TaskItem item){
        if(!(context instanceof TimeSwitchService)){
            Log.e("ProcessTaskItem","The instance of context is not a TimeSwitchService instance!!");
        }
        SQLiteDatabase database= MySQLiteOpenHelper.getInstance(context).getWritableDatabase();

        if(item.trigger_type == TriggerTypeConsts.TRIGGER_TYPE_SINGLE){
            //do close the item
            item.isenabled=false;
            ContentValues values=new ContentValues();
            values.put(SQLConsts.SQL_TASK_COLUMN_ENABLED,0);
            database.update(MySQLiteOpenHelper.getCurrentTableName(context),values,SQLConsts.SQL_TASK_COLUMN_ID +"="+item.id,null);

            //refresh the list in Main;
            MainActivity.sendEmptyMessage(MainActivity.MESSAGE_REQUEST_UPDATE_LIST);
        }

        if(item.trigger_type == TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK){
            Calendar c=Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            int day_of_week=c.get(Calendar.DAY_OF_WEEK);
            switch (day_of_week){
                default:break;
                case Calendar.MONDAY:if(!item.week_repeat[PublicConsts.WEEK_MONDAY]) return; break;
                case Calendar.TUESDAY:if(!item.week_repeat[PublicConsts.WEEK_TUESDAY]) return; break;
                case Calendar.WEDNESDAY:if(!item.week_repeat[PublicConsts.WEEK_WEDNESDAY]) return; break;
                case Calendar.THURSDAY:if(!item.week_repeat[PublicConsts.WEEK_THURSDAY]) return; break;
                case Calendar.FRIDAY:if(!item.week_repeat[PublicConsts.WEEK_FRIDAY]) return; break;
                case Calendar.SATURDAY:if(!item.week_repeat[PublicConsts.WEEK_SATURDAY]) return; break;
                case Calendar.SUNDAY:if(!item.week_repeat[PublicConsts.WEEK_SUNDAY]) return; break;
            }
        }

        boolean canTrigger= true;
        try {
            canTrigger=processExceptionOfTaskItem(context,item);
            Log.d("processType",item.addition_exception_connector.equals("-1")?"OR":"AND");
            Log.d("CanTrigger",""+canTrigger);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(item.autoclose&&canTrigger){
            item.cancelTask();
            item.isenabled=false;
            try{
                ContentValues values=new ContentValues();
                values.put(SQLConsts.SQL_TASK_COLUMN_ENABLED,0);
                database.update(MySQLiteOpenHelper.getCurrentTableName(context),values,SQLConsts.SQL_TASK_COLUMN_ID +"="+item.id,null);
                MainActivity.sendEmptyMessage(MainActivity.MESSAGE_REQUEST_UPDATE_LIST);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        //do if delete this taskitem
        if(item.autodelete&&canTrigger){
            try{
                item.cancelTask();
                //int rows=database.delete(MySQLiteOpenHelper.getCurrentTableName(this.context),SQLConsts.SQL_TASK_COLUMN_ID +"="+item.id,null);
                MySQLiteOpenHelper.deleteRow(context,MySQLiteOpenHelper.getCurrentTableName(context),item.id);
                //Log.i(TAG,"receiver deleted "+rows+" rows");
            }catch (Exception e){
                e.printStackTrace();
            }
            synchronized (TimeSwitchService.class){
                try{
                    TimeSwitchService.list.remove(item);
                }catch (Exception e){e.printStackTrace();}
            }
            MainActivity.sendEmptyMessage(MainActivity.MESSAGE_REQUEST_UPDATE_LIST);
        }

        if(item.delayed&&canTrigger){
            int id=notification_id+1;
            delayeds.put(id,Thread.currentThread());
            NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder;
            if(Build.VERSION.SDK_INT>=26){
                String channel_id="channel_tasks";
                NotificationChannel channel=new NotificationChannel(channel_id,
                        context.getResources().getString(R.string.notification_channel_task),
                        NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
                builder=new NotificationCompat.Builder(context,channel_id);
            }else{
                builder=new NotificationCompat.Builder(context);
            }
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            builder.setSmallIcon(R.drawable.ic_launcher);

            builder.setContentTitle(context.getResources().getString(R.string.notification_delayed_title)+item.name);
            builder.setContentText(context.getResources().getString(R.string.notification_delayed_message));

            Intent cancelIntent=new Intent(context,CancelTaskReceiver.class);
            cancelIntent.putExtra("threadId",id);


            builder.setContentIntent(PendingIntent.getActivity(context,1,new Intent(context,MainActivity.class),
                    PendingIntent.FLAG_UPDATE_CURRENT));
            builder.setAutoCancel(true);

            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            builder.setFullScreenIntent(PendingIntent.getActivity(context,2,new Intent(),PendingIntent.FLAG_UPDATE_CURRENT)
                    ,true);

            RemoteViews remoteViews=new RemoteViews(context.getPackageName(),R.layout.layout_notification_delay);

            remoteViews.setOnClickPendingIntent(R.id.notification_task_cancel,PendingIntent.getBroadcast(context,
                   id,cancelIntent,PendingIntent.FLAG_UPDATE_CURRENT));
            remoteViews.setTextViewText(R.id.notification_task_title,context.getResources().getString(R.string.notification_delayed_title)+item.name);
            remoteViews.setTextViewText(R.id.notification_task_message,context.getResources().getString(R.string.notification_delayed_message));
            remoteViews.setImageViewResource(R.id.notification_task_icon, (Integer) ContentAdapter.TriggerContentAdapter
                    .getContentForTriggerType(context
                            ,ContentAdapter.TriggerContentAdapter.CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID
                            ,item));
            builder.setCustomContentView(remoteViews);
            builder.setGroupSummary(false);
            builder.setGroup("IncomingTask");
            manager.notify(id,builder.build());
            try{
                Thread.sleep(5000);
                manager.cancel(id);
                delayeds.values().remove(Thread.currentThread());
            }catch (InterruptedException ie){
                Log.d("Canceled",notification_id+" 任务取消执行");
                return;
            }
        }

        if(canTrigger){
            last_activated_task_name=item.name;
            try{activateActionOfWifi(context,Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_WIFI_LOCALE]));}catch (Exception e){e.printStackTrace();}
            try{activateActionOfBluetooth(context,Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BLUETOOTH_LOCALE]));}catch (Exception e){e.printStackTrace();}
            try{activateActionOfRingMode(context,Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_MODE_LOCALE]));}catch (Exception e){e.printStackTrace();}
            try{activateActionOfVolume(context,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_VOLUME_LOCALE]);}catch (Exception e){e.printStackTrace();}
            try{activateActionOfSettingRingtone(context,item);}catch (Exception e){e.printStackTrace();}
            try{activateActionOfBrightness(context,Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BRIGHTNESS_LOCALE]));}catch (Exception e){e.printStackTrace();}
            try{activateActionOfWallpaper(context,item);}catch (Exception e){e.printStackTrace();}
            try{activateActionOfFlashlight(context,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FLASHLIGHT]);}catch (Exception e){e.printStackTrace();}
            try{activateActionOfVibrate(context,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_VIBRATE_LOCALE]);}catch (Exception e){e.printStackTrace();}
            try{activateActionOfToast(context,item);}catch (Exception e){e.printStackTrace();}
            try{activateActionOfPlay(context,item);}catch (Exception e){e.printStackTrace();}
            try{activateActionOfCleaningNotification(item);}catch (Exception e){e.printStackTrace();}
            try{activateActionOfAutorotation(context,Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AUTOROTATION]));}catch (Exception e){e.printStackTrace();}
            try{activateActionOfSMS(context,item);}catch (Exception e){e.printStackTrace();}
            try{launchAppsByPackageName(context,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_LAUNCH_APP_PACKAGES]);}catch (Exception e){e.printStackTrace();}
            try{stopAppsByPackageName(context,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_STOP_APP_PACKAGES]);}catch (Exception e){e.printStackTrace();}
            try{switchTasks(context,item);}catch (Exception e){e.printStackTrace();}
            try{forceStopAppsByPackageName(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FORCE_STOP_APP_PACKAGES]);}catch (Exception e){e.printStackTrace();}

            if(context.getSharedPreferences(PublicConsts.PREFERENCES_NAME,Activity.MODE_PRIVATE).getBoolean(PublicConsts.PREFERENCES_IS_SUPERUSER_MODE,PublicConsts.PREFERENCES_IS_SUPERUSER_MODE_DEFAULT)){
                try{activateActionOfNet(Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NET_LOCALE]));}catch (Exception e){e.printStackTrace();}
                try{activateActionOfGps(Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_GPS_LOCALE]));}catch (Exception e){e.printStackTrace();}
                try{activateActionOfAirplaneMode(Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AIRPLANE_MODE_LOCALE]));}catch (Exception e){e.printStackTrace();}
                try{activateActionOfDeviceControl(Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DEVICE_CONTROL_LOCALE]));}catch (Exception e){e.printStackTrace();}
            }

            activateActionOfNotification(context,item);
            LogUtil.putLog(context,context.getResources().getString(R.string.notification_task_activated_title)+":"+item.name);
        }

    }

    /**
     * 分析TaskItem中的exceptions并返回此TaskItem中的Actions是否可以被执行
     * @return true-可以执行,false-不能执行
     */
    private static boolean processExceptionOfTaskItem(@NonNull Context context,@NonNull TaskItem item){
        int process_type=Integer.parseInt(item.addition_exception_connector);
        boolean flag=false;

        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_LOCKEDSCREEN])==1){
            flag=true;
            boolean b=EnvironmentUtils.isScreenLockedByKeyGuardManager(context);
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&b)return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&!b)return true;
        }

        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_UNLOCKEDSCREEN])==1){
            flag=true;
            boolean b=EnvironmentUtils.isScreenLockedByKeyGuardManager(context);
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&!b)return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&b)return true;
        }

        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_WIFI_ENABLED])==1){
            flag=true;
            boolean b =EnvironmentUtils.isWifiEnabled(context);
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&b)return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&!b)return true;
        }

        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_WIFI_DISABLED])==1){
            flag=true;
            boolean b=EnvironmentUtils.isWifiEnabled(context);
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&!b)return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&b)return true;
        }

        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_HEADSET_STATUS])==ExceptionConsts.EXCEPTION_HEADSET_PLUG_OUT){
            flag=true;
            boolean isPlugged=Global.HeadsetPlugReceiver.isHeadsetPluggedIn();
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&!isPlugged) return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&isPlugged) return true;
        }else if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_HEADSET_STATUS])==ExceptionConsts.EXCEPTION_HEADSET_PLUG_IN){
            flag=true;
            boolean isPlugged=Global.HeadsetPlugReceiver.isHeadsetPluggedIn();
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&isPlugged) return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&!isPlugged) return true;
        }

        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BLUETOOTH_ENABLED])==1){
            flag=true;
            boolean b=EnvironmentUtils.isBluetoothEnabled();
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&b) return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&!b) return true;
        }

        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BLUETOOTH_DISABLED])==1){
            flag=true;
            boolean b=EnvironmentUtils.isBluetoothEnabled();
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&!b) return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&b) return true;
        }

        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_RING_VIBRATE])==1){
            flag=true;
            boolean is_vibrate_mode= EnvironmentUtils.getRingerMode(context)==AudioManager.RINGER_MODE_VIBRATE;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&is_vibrate_mode)return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&!is_vibrate_mode)return true;
        }

        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_RING_OFF])==1){
            flag=true;
            boolean b= EnvironmentUtils.getRingerMode(context)==AudioManager.RINGER_MODE_SILENT;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&b) return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&!b) return true;
        }

        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_RING_NORMAL])==1){
            flag=true;
            boolean b=EnvironmentUtils.getRingerMode(context)==AudioManager.RINGER_MODE_NORMAL;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&b) return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&!b) return true;
        }

        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_NET_ENABLED])==1){
            flag=true;
            boolean b=EnvironmentUtils.isGprsNetworkEnabled(context);
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&b) return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&!b) return true;
        }

        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_NET_DISABLED])==1){
            flag=true;
            boolean b=EnvironmentUtils.isGprsNetworkEnabled(context);
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&!b) return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&b) return true;
        }

        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_GPS_ENABLED])==1){
            flag=true;
            boolean b=EnvironmentUtils.isGpsEnabled(context);
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&b) return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&!b) return true;
        }

        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_GPS_DISABLED])==1){
            flag=true;
            boolean b=EnvironmentUtils.isGpsEnabled(context);
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&!b) return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&b) return true;
        }

        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_AIRPLANE_MODE_ENABLED])==1){
            flag=true;
            boolean b=EnvironmentUtils.isAirplaneModeOpen(context);
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&b) return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&!b) return true;
        }

        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_AIRPLANE_MODE_DISABLED])==1){
            flag=true;
            boolean b=EnvironmentUtils.isAirplaneModeOpen(context);
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&!b) return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&b) return true;
        }

        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_IS_IN_CALL_COMING_STATE])==1){
            flag=true;
            boolean b= TimeSwitchService.CallStateInvoker.getCallState()== TelephonyManager.CALL_STATE_RINGING;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&b) return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&!b)return true;
        }

        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_IS_IN_CALL_CONNECTED_STATE])==1){
            flag=true;
            boolean b=TimeSwitchService.CallStateInvoker.getCallState()==TelephonyManager.CALL_STATE_OFFHOOK;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&b)return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&!b)return true;
        }

        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_IS_NOT_IN_CALL_STATE])==1){
            flag=true;
            boolean b= TimeSwitchService.CallStateInvoker.getCallState()==TelephonyManager.CALL_STATE_IDLE;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR&&b)return false;
            if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&!b)return true;
        }

        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int day_of_week=calendar.get(Calendar.DAY_OF_WEEK);
        List<Integer> selected_day_of_week=new ArrayList<>();
        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_MONDAY])==1) selected_day_of_week.add(Calendar.MONDAY);
        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_TUESDAY])==1) selected_day_of_week.add(Calendar.TUESDAY);
        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_WEDNESDAY])==1) selected_day_of_week.add(Calendar.WEDNESDAY);
        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_THURSDAY])==1) selected_day_of_week.add(Calendar.THURSDAY);
        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_FRIDAY])==1) selected_day_of_week.add(Calendar.FRIDAY);
        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_SATURDAY])==1) selected_day_of_week.add(Calendar.SATURDAY);
        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_SUNDAY])==1) selected_day_of_week.add(Calendar.SUNDAY);

        if(selected_day_of_week.size()>0){
            flag=true;
            boolean is_today_in_selected_days=selected_day_of_week.contains(day_of_week);
            if(is_today_in_selected_days&&process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR) return false;
            if(!is_today_in_selected_days&&process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND) return true;
        }

        int start_time=Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_START_TIME]);
        int end_time=Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_END_TIME]);
        int current_min=calendar.get(Calendar.HOUR_OF_DAY)*60+calendar.get(Calendar.MINUTE);
        if(start_time>=0&&end_time>=0){
            flag=true;
            boolean b;
            if(end_time>start_time) b=current_min>=start_time&&current_min<=end_time;
            else b=current_min<=start_time&&current_min>=end_time;
            if(b&&process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR) return false;
            if(!b&&process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND) return true;
        }

        int battery_less_than_percentage=Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]);
        if(battery_less_than_percentage>=0){
            flag=true;
            boolean b=battery_less_than_percentage<Global.BatteryReceiver.battery_percentage;
            if(b&&process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR) return false;
            if(!b&&process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND) return true;
        }

        int battery_more_than_percentage=Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]);
        if(battery_more_than_percentage>=0){
            flag=true;
            boolean b=battery_more_than_percentage>Global.BatteryReceiver.battery_percentage;
            if(b&&process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR) return false;
            if(!b&&process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND) return true;
        }

        int battery_lower_than_temperature=Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]);
        if(battery_lower_than_temperature>=0){
            flag=true;
            boolean b=battery_lower_than_temperature<Global.BatteryReceiver.battery_temperature/10;
            if(b&&process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR) return false;
            if(!b&&process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND) return true;
        }

        int battery_higher_than_temperature=Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]);
        if(battery_higher_than_temperature>=0){
            flag=true;
            boolean b=battery_higher_than_temperature>Global.BatteryReceiver.battery_temperature/10;
            if(b&&process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR) return false;
            if(!b&&process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND) return true;
        }

        int ex_wifi_status=Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[0]);
        if(ex_wifi_status==ExceptionConsts.EXCEPTION_WIFI_VALUE_DISCONNECTED){
            flag=true;
            boolean b=EnvironmentUtils.isWifiConnected(context,null);
            if(!b&&process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR) return false;
            if(b&&process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND) return true;
        }else if(ex_wifi_status==ExceptionConsts.EXCEPTION_WIFI_VALUE_CONNECTED_TO_RANDOM_SSID){
            flag=true;
            boolean b=EnvironmentUtils.isWifiConnected(context,null);
            if(b&&process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR) return false;
            if(!b&&process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND)return true;
        }else if(ex_wifi_status>=0){
            flag=true;
            boolean b=false;
            int[]ids=ValueUtils.string2intArray(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL,item.exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS]);
            for(int id:ids) if(EnvironmentUtils.isWifiConnected(context,id)){
                b=true;
                break;
            }
            if(b&&process_type==ExceptionConsts.EXCEPTION_CONNECTOR_OR) return false;
            if(!b&&process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND) return true;
        }

        if(process_type==ExceptionConsts.EXCEPTION_CONNECTOR_AND&&flag) return false;
        return true;
    }

    private static void activateActionOfWifi(Context context,int action_wifi){
        switch (action_wifi){
            default:break;
            case ActionConsts.ActionValueConsts.ACTION_OPEN:{
                EnvironmentUtils.setWifiEnabled(context,true);
                Log.i(TAG,"Try to enable wifi...");
            }
            break;
            case ActionConsts.ActionValueConsts.ACTION_CLOSE:{
                EnvironmentUtils.setWifiEnabled(context,false);
                Log.i(TAG,"Try to disable wifi...");
            }
            break;
        }
    }

    private static void activateActionOfBluetooth(Context context,int action_bluetooth){
        switch (action_bluetooth){
            default:break;
            case ActionConsts.ActionValueConsts.ACTION_OPEN:{
                EnvironmentUtils.setBluetoothEnabled(true);
                Log.i(TAG,"try to enable bluetooth...");
            }
            break;
            case ActionConsts.ActionValueConsts.ACTION_CLOSE:{
                EnvironmentUtils.setBluetoothEnabled(false);
                Log.i(TAG,"Try to disable bluetooth...");
            }
            break;
        }

    }

    private static void activateActionOfRingMode(Context context,int action_ring_mode){
        switch (action_ring_mode){
            default:break;
            case ActionConsts.ActionValueConsts.ACTION_RING_VIBRATE:{
                EnvironmentUtils.setRingerMode(context,AudioManager.RINGER_MODE_VIBRATE);
                break;
            }
            case ActionConsts.ActionValueConsts.ACTION_RING_OFF:{
                EnvironmentUtils.setRingerMode(context,AudioManager.RINGER_MODE_SILENT);
                break;
            }
            case ActionConsts.ActionValueConsts.ACTION_RING_NORMAL:{
                EnvironmentUtils.setRingerMode(context,AudioManager.RINGER_MODE_NORMAL);
                break;
            }
        }
    }

    private static void activateActionOfVolume(Context context,String values){
        String [] volumes=values.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
        int volume_ring=Integer.parseInt(volumes[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_RING_LOCALE]);
        int volume_media=Integer.parseInt(volumes[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_MEDIA_LOCALE]);
        int volume_notification=Integer.parseInt(volumes[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_NOTIFICATION_LOCALE]);
        int volume_alarm=Integer.parseInt(volumes[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_ALARM_LOCALE]);
        if(volume_ring>=0) {
            EnvironmentUtils.setRingerVolume(context,AudioManager.STREAM_RING,volume_ring);
        }
        if(volume_media>=0) {
            EnvironmentUtils.setRingerVolume(context,AudioManager.STREAM_MUSIC,volume_media);
        }
        if(volume_notification>=0){
            EnvironmentUtils.setRingerVolume(context,AudioManager.STREAM_NOTIFICATION,volume_notification);
        }
        if(volume_alarm>=0) {
            EnvironmentUtils.setRingerVolume(context,AudioManager.STREAM_ALARM,volume_alarm);
        }
    }

    private static void activateActionOfSettingRingtone(Context context,TaskItem item){
        String ring_selection_values[]=item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_SELECTION_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
        int ring_notification_selection= Integer.parseInt(ring_selection_values[ActionConsts.ActionSecondLevelLocaleConsts.RING_SELECTION_NOTIFICATION_TYPE_LOCALE]);
        int ring_phone_selection=Integer.parseInt(ring_selection_values[ActionConsts.ActionSecondLevelLocaleConsts.RING_SELECTION_CALL_TYPE_LOCALE]);

        if(ring_notification_selection== ActionConsts.ActionValueConsts.RING_TYPE_FROM_SYSTEM ||ring_notification_selection== ActionConsts.ActionValueConsts.RING_TYPE_FROM_MEDIA)
        {
            EnvironmentUtils.setRingtone(context,RingtoneManager.TYPE_NOTIFICATION,item.uri_ring_notification);
            //RingtoneManager.setActualDefaultRingtoneUri(context,RingtoneManager.TYPE_NOTIFICATION ,Uri.parse(item.uri_ring_notification));
        }
        if(ring_phone_selection== ActionConsts.ActionValueConsts.RING_TYPE_FROM_SYSTEM ||ring_phone_selection== ActionConsts.ActionValueConsts.RING_TYPE_FROM_MEDIA)
        {
            EnvironmentUtils.setRingtone(context,RingtoneManager.TYPE_RINGTONE,item.uri_ring_call);
            //RingtoneManager.setActualDefaultRingtoneUri(context,RingtoneManager.TYPE_RINGTONE,Uri.parse(item.uri_ring_call));
        }
    }

    private static void activateActionOfBrightness(Context context,int screen_brightness){
        if(screen_brightness!=-1){
            if(screen_brightness== ActionConsts.ActionValueConsts.ACTION_BRIGHTNESS_AUTO){
                EnvironmentUtils.setBrightness(context,true,0);
            }else if(screen_brightness>=0&&screen_brightness<=PublicConsts.BRIGHTNESS_MAX) {
                EnvironmentUtils.setBrightness(context,false,screen_brightness);
            }

        }
    }

    private static void activateActionOfWallpaper(final Context context, final TaskItem item){
        int value=Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SET_WALL_PAPER_LOCALE]);
        if(value>=0){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        EnvironmentUtils.setWallPaper(context,item.uri_wallpaper_desktop);
                    }catch (Exception e){e.printStackTrace();}
                }
            }).start();
        }
    }

    public static class FlashlightReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context,Intent intent){
            if(flash_light_thread!=null){
                Log.d("Noti","inter");
                flash_light_thread.interrupt();
                flash_light_thread=null;
            }
        }
    }

    private static void activateActionOfFlashlight(final Context context,String values){
        final String []array=values.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
        final int type=Integer.parseInt(array[0]);
        if(type<0)return;
        synchronized (ProcessTaskItem.class){
            if(flash_light_thread!=null) {
                flash_light_thread.interrupt();
                flash_light_thread=null;
                SystemClock.sleep(1000);
            }
            final NotificationManager manager=(NotificationManager)context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if(manager==null)return;
            NotificationCompat.Builder builder;
            if(Build.VERSION.SDK_INT>=26){
                NotificationChannel channel=new NotificationChannel("channel_flashlight","FlashLight", NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
                builder=new NotificationCompat.Builder(context,"channel_flashlight");
            }else{
                builder=new NotificationCompat.Builder(context);
            }
            builder.setSmallIcon(R.drawable.icon_flashlight);
            builder.setContentTitle(context.getResources().getString(R.string.notification_flashlight_title));
            builder.setContentText(context.getResources().getString(R.string.notification_flashlight_message));
            builder.setOngoing(true);
            builder.setContentIntent(PendingIntent.getBroadcast(context,1,new Intent(context,FlashlightReceiver.class),PendingIntent.FLAG_UPDATE_CURRENT));
            //builder.setFullScreenIntent(PendingIntent.getActivity(context,1,new Intent(),PendingIntent.FLAG_UPDATE_CURRENT),false);
            manager.notify(110,builder.build());
            flash_light_thread=new Thread(new Runnable() {
                @Override
                public void run() {
                    if(type==ActionConsts.ActionValueConsts.ACTION_FLASHLIGHT_TYPE_HOLD)EnvironmentUtils.setTorch(context,Integer.parseInt(array[1])*1000);
                    else if(type==ActionConsts.ActionValueConsts.ACTION_FLASHLIGHT_TYPE_CUSTOM){
                        long[] vars=new long[array.length-1];
                        for(int i=0;i<vars.length;i++){
                            vars[i]=Long.parseLong(array[i+1]);
                        }
                        EnvironmentUtils.setTorch(context,vars);
                    }
                    manager.cancel(110);
                }
            });
            flash_light_thread.start();
        }

    }

    private static void activateActionOfVibrate(Context context,String values){
        String vibrate_values[]=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
        int frequency=Integer.parseInt(vibrate_values[ActionConsts.ActionSecondLevelLocaleConsts.VIBRATE_FREQUENCY_LOCALE]);
        long duration=Long.parseLong(vibrate_values[ActionConsts.ActionSecondLevelLocaleConsts.VIBRATE_DURATION_LOCALE]);
        long interval=Long.parseLong(vibrate_values[ActionConsts.ActionSecondLevelLocaleConsts.VIBRATE_INTERVAL_LOCALE]);
        if(frequency>0) EnvironmentUtils.vibrate(context,frequency,duration,interval);
    }

    private static void activateActionOfToast(Context context,TaskItem item){
        String []toast_values=item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_TOAST_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
        int type=Integer.parseInt(toast_values[ActionConsts.ActionSecondLevelLocaleConsts.TOAST_TYPE_LOCALE]);
        if(type>=0){
            int[] offsets=null;
            if(type==ActionConsts.ActionValueConsts.TOAST_TYPE_CUSTOM){
                offsets=new int[2];
                offsets[0]=Integer.parseInt(toast_values[ActionConsts.ActionSecondLevelLocaleConsts.TOAST_LOCATION_X_OFFSET_LOCALE]);
                offsets[1]=Integer.parseInt(toast_values[ActionConsts.ActionSecondLevelLocaleConsts.TOAST_LOCATION_Y_OFFSET_LOCALE]);
            }
            EnvironmentUtils.showToast(context,offsets,item.toast);
        }
    }

    private static void activateActionOfPlay(Context context,TaskItem item){
        int selection=Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_PLAY_AUDIO]);
        if(selection>=0){
            EnvironmentUtils.playAudioFileFromUri(context, Uri.parse(item.uri_play));
        }
    }

    private static void activateActionOfCleaningNotification(TaskItem item){
        int selection=0;
        String value=item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_CLEAN_NOTIFICATION];
        String []  package_names=null;
        try{
            selection=Integer.parseInt(value);
            if(selection<0)return;
        }catch (NumberFormatException ne){
            try{
                package_names=value.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            }catch (Exception e){}
        }
        if(selection== ActionConsts.ActionValueConsts.ACTION_CLEAN_NOTIFICATION_ALL){
            NotificationMonitorService.removeAllRemovableNotification();
            return;
        }
        if(package_names==null)return;
        for(String s:package_names){
            NotificationMonitorService.removeNotification(s);
        }
    }

    private static void activateActionOfAutorotation(Context context,int value){
        if(value>=0) EnvironmentUtils.setIfAutorotation(context,value==1);
    }

    private static void activateActionOfSMS(Context context,TaskItem item){
        String sms_values[]=item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SMS_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL);
        if(Integer.parseInt(sms_values[ActionConsts.ActionSecondLevelLocaleConsts.SMS_ENABLED_LOCALE])>=0){
            Integer subid=null;
            int subid_int=Integer.parseInt(sms_values[ActionConsts.ActionSecondLevelLocaleConsts.SMS_SUBINFO_LOCALE]);
            if(Build.VERSION.SDK_INT>=22&&subid_int>=0) subid=subid_int;
            EnvironmentUtils.sendSMSMessage(context,subid,item.sms_address.split(PublicConsts.SEPARATOR_SMS_RECEIVERS),item.sms_message
                    ,Integer.parseInt(sms_values[ActionConsts.ActionSecondLevelLocaleConsts.SMS_RESULT_TOAST_LOCALE])>=0);

        }
    }

    private static void activateActionOfNet(int action_net){
        switch (action_net){
            default:break;
            case ActionConsts.ActionValueConsts.ACTION_OPEN: EnvironmentUtils.setGprsNetworkEnabled(true);break;
            case ActionConsts.ActionValueConsts.ACTION_CLOSE: EnvironmentUtils.setGprsNetworkEnabled(false);break;
        }
    }

    private static void activateActionOfGps(int action_gps){
        switch (action_gps){
            default:break;
            case ActionConsts.ActionValueConsts.ACTION_OPEN: {
                EnvironmentUtils.setGpsEnabled(true);
            }
            break;
            case ActionConsts.ActionValueConsts.ACTION_CLOSE:{
                EnvironmentUtils.setGpsEnabled(false);
            }
            break;
        }
    }

    private static void activateActionOfAirplaneMode(int action_airplanemode){
        switch (action_airplanemode){
            default:break;
            case ActionConsts.ActionValueConsts.ACTION_OPEN:{
                EnvironmentUtils.setAirplaneModeEnabled(true);
            }
            break;
            case ActionConsts.ActionValueConsts.ACTION_CLOSE:{
                EnvironmentUtils.setAirplaneModeEnabled(false);
            }
            break;
        }
    }

    private static void activateActionOfDeviceControl(int action_device_control){
        switch (action_device_control){
            default:break;
            case ActionConsts.ActionValueConsts.ACTION_DEVICE_CONTROL_SHUTDOWN:{
                EnvironmentUtils.shutdownDevice();
            }
            break;
            case ActionConsts.ActionValueConsts.ACTION_DEVICE_CONTROL_REBOOT:{
                EnvironmentUtils.restartDevice();
            }break;
        }
    }

    private static void activateActionOfNotification(Context context,TaskItem item){
        String notification_values[]=item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NOTIFICATION_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
        int type=Integer.parseInt(notification_values[ActionConsts.ActionSecondLevelLocaleConsts.NOTIFICATION_TYPE_LOCALE]);
        if(type==-1)return;
        int if_custom=Integer.parseInt(notification_values[ActionConsts.ActionSecondLevelLocaleConsts.NOTIFICATION_TYPE_IF_CUSTOM_LOCALE]);
        if(type== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_NOT_OVERRIDE){
            if(notification_id<Integer.MAX_VALUE-1) notification_id++;
            else notification_id=2;
        }
        String title,message;
        if(if_custom==ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_CONTENT_CUSTOM){
            title=item.notification_title;
            message=item.notification_message;
        }else {
            title=context.getResources().getString(R.string.notification_task_activated_title);
            message=item.name;
        }
        try {
            EnvironmentUtils.sendNotification(context,
                    notification_id,
                    (Integer) ContentAdapter.TriggerContentAdapter
                            .getContentForTriggerType(context
                                    ,ContentAdapter.TriggerContentAdapter.CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID
                                    ,item),
                    title,message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void launchAppsByPackageName(Context context,String values){
        if(values.equals(String.valueOf(-1)))return;
        String packageNames[]=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
        EnvironmentUtils.launchAppByPackageName(context,packageNames);
    }

    private static void stopAppsByPackageName(Context context,String values){
        if(values.equals(String.valueOf(-1)))return;
        String packageNames[]=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
        EnvironmentUtils.stopAppByPackageName(context,packageNames);
    }

    private static void forceStopAppsByPackageName(String values){
        if(values.equals(String.valueOf(-1)))return;
        String packageNames[]=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
        for(String s:packageNames) EnvironmentUtils.forceStopAppByPackageName(s);
    }

    /**
     * 启用或者关闭指定任务
     */
    private static void switchTasks(Context context,TaskItem item){
        SQLiteDatabase database=MySQLiteOpenHelper.getInstance(context).getReadableDatabase();
        String[] enable_ids=item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_ENABLE_TASKS_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
        String[] disable_ids=item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DISABLE_TASKS_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
        if(Integer.parseInt(enable_ids[0])>=0){
            for(String s:enable_ids){
                int id=Integer.parseInt(s);
                Cursor cursor=database.rawQuery("select * from "+ MySQLiteOpenHelper.getCurrentTableName(context)+" where "+SQLConsts.SQL_TASK_COLUMN_ID+"="+id,null);
                if(cursor.moveToFirst()){
                    //int position=getPosition(id);
                    synchronized (TimeSwitchService.class){
                        setTaskEnabled(context,getTaskItemOfId(TimeSwitchService.list
                                ,cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ID)))
                                ,true,MySQLiteOpenHelper.getCurrentTableName(context));
                    }
                }
                cursor.close();
            }
        }
        if(Integer.parseInt(disable_ids[0])>=0){
            for(String s:disable_ids){
                int id=Integer.parseInt(s);
                Cursor cursor=database.rawQuery("select * from "+ MySQLiteOpenHelper.getCurrentTableName(context)+" where "+SQLConsts.SQL_TASK_COLUMN_ID+"="+id,null);
                if(cursor.moveToFirst()){
                    synchronized (TimeSwitchService.class){
                        setTaskEnabled(context
                                ,getTaskItemOfId(TimeSwitchService.list,cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ID)))
                                ,false,MySQLiteOpenHelper.getCurrentTableName(context));
                    }
                }
                cursor.close();
            }
        }
        MainActivity.sendEmptyMessage(MainActivity.MESSAGE_REQUEST_UPDATE_LIST);
    }

    /**
     * 根据指定ID返回list中的TaskItem
     * @param list 要查询的list
     * @param id 要获得的TaskItem的ID
     * @return TaskItem，如果没有对应ID的则返回null
     */
    public static @Nullable TaskItem getTaskItemOfId(List<TaskItem>list,int id){
        try{
            for(TaskItem item:list) if(item.id==id) return item;
        }catch (Exception e){e.printStackTrace();}
        return null;
    }


    /**
     * 启用或者关闭指定任务，更新list，刷新数据库
     * @param item 指定的任务项
     * @param table_name 指定表的名称，传入null则不对数据表做更改，或者将对指定table_name中TaskItem.id的row做出修改
     */
    public static void setTaskEnabled(@NonNull Context context,@NonNull TaskItem item,boolean enabled,@Nullable String table_name){
        if(!(context instanceof TimeSwitchService)) {
            Log.e("invalid","an invalid type of context ,must be a type of TimeSwitchService instance!!");
            return;
        }
        if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_SINGLE&&item.time<=System.currentTimeMillis()) return;
        item.isenabled=enabled;

        long currentTime=System.currentTimeMillis();
        SQLiteDatabase database=MySQLiteOpenHelper.getInstance(context).getWritableDatabase();

        if(table_name!=null){
            database.execSQL("update "+ table_name
                    +" set "+SQLConsts.SQL_TASK_COLUMN_ENABLED +"="+(enabled?1:0)+
                    " where "+SQLConsts.SQL_TASK_COLUMN_ID +"="+item.id);
        }
        if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME&&enabled){
            item.time=currentTime;
            if(table_name!=null){
                Cursor cursor=database.rawQuery("select * from "+ MySQLiteOpenHelper.getCurrentTableName(context)+" where "+SQLConsts.SQL_TASK_COLUMN_ID+"="+item.id,null);
                if(cursor.moveToNext()){
                    long[] values_read=ValueUtils.string2longArray(PublicConsts.SPLIT_SEPARATOR_FIRST_LEVEL,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES)));
                    if(values_read.length==2){
                        long interval_read=values_read[1];
                        long values_put[]=new long[2];
                        values_put[0]=currentTime;
                        values_put[1]=interval_read;
                        ContentValues contentValues=new ContentValues();
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES,ValueUtils.longArray2String(PublicConsts.SEPARATOR_FIRST_LEVEL,values_put));
                        database.update(MySQLiteOpenHelper.getCurrentTableName(context),contentValues,SQLConsts.SQL_TASK_COLUMN_ID+"="+item.id,null);
                    }
                }
                cursor.close();
            }
        }

        database.close();
        if(enabled) item.activateTask(context); else item.cancelTask();
    }

    /**
     * 设置指定项的任务的折叠状态，传入非null的table_name时将按照TaskItem的id值更新table_name的指定row
     * @param item 指定的任务项
     * @param isFolded 是否折叠
     * @param table_name 指定表的名称，如果传入null则不对数据表中TaskItem.id的row做修改
     */
    public static void setTaskFolded(@NonNull Context context, @NonNull TaskItem item , boolean isFolded,@Nullable String table_name){
        if(table_name!=null){
            SQLiteDatabase database=MySQLiteOpenHelper.getInstance(context).getWritableDatabase();
            //final String table_name= MySQLiteOpenHelper.getCurrentTableName(context);
            Cursor cursor=database.rawQuery("select * from "+table_name+" where "+SQLConsts.SQL_TASK_COLUMN_ID+"="+item.id,null);
            if(cursor.moveToFirst()){
                String additions_read[]=ValueUtils.string2StringArray(PublicConsts.SPLIT_SEPARATOR_FIRST_LEVEL,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ADDITIONS)));
                String additions[]=new String[AdditionConsts.ADDITION_LENGTH];
                for(int i=0;i<additions.length;i++){additions[i]=String.valueOf(-1);}
                additions[AdditionConsts.ADDITION_TITLE_COLOR_LOCALE]=new TaskItem().addition_title_color;
                System.arraycopy(additions_read,0,additions,0,additions_read.length);
                additions[AdditionConsts.ADDITION_TITLE_FOLDED_VALUE_LOCALE]=isFolded?String.valueOf(0):String.valueOf(-1);
                ContentValues content=new ContentValues();
                content.put(SQLConsts.SQL_TASK_COLUMN_ADDITIONS,ValueUtils.stringArray2String(PublicConsts.SEPARATOR_FIRST_LEVEL,additions));
                database.update(table_name,content,SQLConsts.SQL_TASK_COLUMN_ID+"="+item.id,null);
            }
            cursor.close();
            database.close();
        }
        item.addition_isFolded=isFolded;
    }

    public static class CancelTaskReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            try{
                int notiId=intent.getIntExtra("threadId",-1);
                Thread thread=delayeds.get(notiId);
                if(thread!=null)thread.interrupt();
                ((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(notiId);
                delayeds.remove(notiId);
            }catch (Exception e){e.printStackTrace();}
        }
    }

}
