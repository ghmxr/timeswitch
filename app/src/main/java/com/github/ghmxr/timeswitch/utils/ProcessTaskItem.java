package com.github.ghmxr.timeswitch.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.PermissionChecker;
import android.telecom.TelecomManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.Global;
import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.activities.MainActivity;
import com.github.ghmxr.timeswitch.data.v2.ActionConsts;
import com.github.ghmxr.timeswitch.data.v2.AdditionConsts;
import com.github.ghmxr.timeswitch.data.v2.ExceptionConsts;
import com.github.ghmxr.timeswitch.data.v2.MySQLiteOpenHelper;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.data.v2.SQLConsts;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 处理并执行TaskItem中的actions和一些附加选项的类
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class ProcessTaskItem {

    private TaskItem item;
    private Context context;
    //private boolean canTrigger;
    static final String TAG="ProcessTaskItem";
    private StringBuilder log_taskitem=new StringBuilder("");
    public static String last_activated_task_name="";
    public static int notification_id=2;

    public ProcessTaskItem(@NonNull Context context, @Nullable TaskItem item){
        this.item=item;
        this.context=context;
        //this.canTrigger=true;
    }

    public void activateTaskItem(){
        if(this.item==null) return;
        //StringBuilder log_taskitem=new StringBuilder("");
        //log_taskitem.append(context.getResources().getString(R.string.log_taskname));
        log_taskitem.append(item.name);
        log_taskitem.append(":");

        SQLiteDatabase database= MySQLiteOpenHelper.getInstance(this.context).getWritableDatabase();


        if(item.trigger_type == TriggerTypeConsts.TRIGGER_TYPE_SINGLE){
            //do close the item
            ContentValues values=new ContentValues();
            values.put(SQLConsts.SQL_TASK_COLUMN_ENABLED,0);
            database.update(MySQLiteOpenHelper.getCurrentTableName(this.context),values,SQLConsts.SQL_TASK_COLUMN_ID +"="+item.id,null);

            //do close the single task and refresh the list in Main;
            int position=getPosition(item.id);
            if(TimeSwitchService.list!=null&&position>=0&&position<TimeSwitchService.list.size()) TimeSwitchService.list.get(position).isenabled=false;
            MainActivity.sendEmptyMessage(MainActivity.MESSAGE_REQUEST_UPDATE_LIST);
        }

        /*if(item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE||item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE
                ||item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE||item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE){

        }*/

        if(item.trigger_type == TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK){
            Calendar c=Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            int dayofweek=c.get(Calendar.DAY_OF_WEEK);
            switch (dayofweek){
                default:break;
                case Calendar.MONDAY:if(!item.week_repeat[PublicConsts.WEEK_MONDAY]){
                    return;
                }
                break;
                case Calendar.TUESDAY:if(!item.week_repeat[PublicConsts.WEEK_TUESDAY]) {
                    return;
                }
                break;
                case Calendar.WEDNESDAY:if(!item.week_repeat[PublicConsts.WEEK_WEDNESDAY]){
                    return;
                }
                break;
                case Calendar.THURSDAY:if(!item.week_repeat[PublicConsts.WEEK_THURSDAY]){
                    return;
                }
                break;
                case Calendar.FRIDAY:if(!item.week_repeat[PublicConsts.WEEK_FRIDAY]) {
                    return;
                }
                break;
                case Calendar.SATURDAY:if(!item.week_repeat[PublicConsts.WEEK_SATURDAY]){
                    return;
                }
                break;
                case Calendar.SUNDAY:if(!item.week_repeat[PublicConsts.WEEK_SUNDAY]) {
                    return;
                }
                break;
            }
        }

        StringBuilder log_exception=new StringBuilder("");
        boolean canTrigger= true;
        int exception_connector=-1;
        try {
            exception_connector=Integer.parseInt(item.addition_exception_connector);
            //Log.d("ExceptionCon",""+exception_connector);
            if(exception_connector>=0){
                canTrigger=processExceptions(ExceptionConsts.EXCEPTION_CONNECTOR_AND,log_exception);
            }else{
                canTrigger=processExceptions(ExceptionConsts.EXCEPTION_CONNECTOR_OR,log_exception);
            }
            Log.d("CanTrigger",""+canTrigger);
        }catch (Exception e){
            e.printStackTrace();
        }


        if(canTrigger){
            log_taskitem.append(context.getResources().getString(R.string.log_exceptions_activate));
            if(log_exception.toString().trim().length()>0){
                log_taskitem.append(context.getResources().getString(R.string.log_exceptions_unsatisfied));
                log_taskitem.append(log_exception.toString());
                log_taskitem.append(" ");
            }
        }else {
            log_taskitem.append(context.getResources().getString(R.string.log_exceptions));
            log_taskitem.append(" ");
            log_taskitem.append(log_exception.toString());
            log_taskitem.append(" ");
        }

        if(item.autoclose&&canTrigger){
            item.cancelTask();
            item.isenabled=false;
            try{
                ContentValues values=new ContentValues();
                values.put(SQLConsts.SQL_TASK_COLUMN_ENABLED,0);
                database.update(MySQLiteOpenHelper.getCurrentTableName(this.context),values,SQLConsts.SQL_TASK_COLUMN_ID +"="+item.id,null);
                MainActivity.sendEmptyMessage(MainActivity.MESSAGE_REQUEST_UPDATE_LIST);
            }catch (Exception e){
                e.printStackTrace();
                LogUtil.putExceptionLog(context,e);
            }
        }
        //do if delete this taskitem
        if(item.autodelete&&canTrigger){
            try{
                item.cancelTask();
                int rows=database.delete(MySQLiteOpenHelper.getCurrentTableName(this.context),SQLConsts.SQL_TASK_COLUMN_ID +"="+item.id,null);
                Log.i(TAG,"receiver deleted "+rows+" rows");
                int position=getPosition(item.id);
                if(TimeSwitchService.list!=null&&position>=0&&position<TimeSwitchService.list.size()) {
                    TimeSwitchService.list.remove(position);
                }
                MainActivity.sendEmptyMessage(MainActivity.MESSAGE_REQUEST_UPDATE_LIST);
            }catch (Exception e){
                e.printStackTrace();
                LogUtil.putExceptionLog(context,e);
            }
        }

        if(canTrigger){
           log_taskitem.append(" ");
           last_activated_task_name=item.name;
           int action_wifi=-1;
           int action_bluetooth=-1;
           int action_ring_mode=-1;
           int screen_brightness=-1;
           int action_net=-1;
           int action_gps=-1;
           int action_airplanemode=-1;
           int action_device_control=-1;
           try{
               action_wifi=Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_WIFI_LOCALE]);
               action_bluetooth=Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BLUETOOTH_LOCALE]);
               action_ring_mode=Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_MODE_LOCALE]);
               screen_brightness=Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BRIGHTNESS_LOCALE]);
               action_net=Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NET_LOCALE]);
               action_gps=Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_GPS_LOCALE]);
               action_airplanemode=Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AIRPLANE_MODE_LOCALE]);
               action_device_control=Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DEVICECONTROL_LOCALE]);
           }catch (NumberFormatException ne){
               ne.printStackTrace();
               Toast.makeText(context,ne.toString(),Toast.LENGTH_SHORT).show();
               log_taskitem.append(ne.toString());
               log_taskitem.append("\n");
           }
            activateActionOfWifi(action_wifi);
            activateActionOfBluetooth(action_bluetooth);
            activateActionOfRingMode(action_ring_mode);
            activateActionOfVolume(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_VOLUME_LOCALE]);
            activateActionOfSettingRingtone(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_SELECTION_LOCALE]);
            activateActionOfBrightness(screen_brightness);
            activateActionOfWallpaper(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SET_WALL_PAPER_LOCALE]);
            activateActionOfVibrate(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_VIBRATE_LOCALE]);
            activateActionOfToast(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_TOAST_LOCALE]);
            activateActionOfSMS(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SMS_LOCALE]);
            launchAppsByPackageName(context,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_LAUNCH_APP_PACKAGES]);
            stopAppsByPackageName(context,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_STOP_APP_PACKAGES]);
            switchTasks(0);
            switchTasks(1);

            SharedPreferences settings=context.getSharedPreferences(PublicConsts.PREFERENCES_NAME,Activity.MODE_PRIVATE);
            boolean isRoot=settings.getBoolean(PublicConsts.PREFERENCES_IS_SUPERUSER_MODE,PublicConsts.PREFERENCES_IS_SUPERUSER_MODE_DEFAULT);
            if(isRoot){
                activateActionOfNet(action_net);
                activateActionOfGps(action_gps);
                activateActionOfAirplaneMode(action_airplanemode);
                activateActionOfDeviceControl(action_device_control);
            }
            activateActionOfNotification(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NOTIFICATION_LOCALE]);
        }
        LogUtil.putLog(context,log_taskitem.toString());
        com.github.ghmxr.timeswitch.activities.Log.sendEmptyMessage(com.github.ghmxr.timeswitch.activities.Log.MESSAGE_REQUEST_REFRESH);
    }

    /**
     * process exceptions and judge if can trigger this task
     * @param processType process type,-1 for "or" and  0 for "and"
     * @param log_exception the log builder need to append
     * @return true for can trigger this task ,or false
     */
    private boolean processExceptions(int processType, StringBuilder log_exception){
        final int TYPE_OR=-1;
        final int TYPE_AND=0;
        boolean type_and_if_has_exception=false;
        try{
            KeyguardManager mKeyguardManager=(KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_LOCKEDSCREEN])==1){
                type_and_if_has_exception=true;
                if(mKeyguardManager.inKeyguardRestrictedInputMode()) {
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_screen_locked));
                        log_exception.append(" ");
                        return false;
                    }
                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_screen_locked));
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                        log_exception.append(" ");
                        return true;
                    }
                }
            }
            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_UNLOCKEDSCREEN])==1){
                type_and_if_has_exception=true;
                if(!mKeyguardManager.inKeyguardRestrictedInputMode()) {
                    if(processType==TYPE_OR) {
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_screen_unlocked));
                        log_exception.append(" ");
                        return false;
                    }
                }
                else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_screen_unlocked));
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                        log_exception.append(" ");
                        return true;
                    }
                }

            }
        }catch (Exception e){
            e.printStackTrace();
            log_exception.append(e);
            log_exception.append("\n");
        }

        try{
            WifiManager mWifiManager=(WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_WIFI_ENABLED])==1){
                type_and_if_has_exception=true;
                if(mWifiManager.getWifiState()== WifiManager.WIFI_STATE_ENABLED) {
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_wifi_enabled));
                        log_exception.append(" ");
                        return false;
                    }
                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_wifi_enabled));
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                        log_exception.append(" ");
                        return true;
                    }
                }
            }
            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_WIFI_DISABLED])==1){
                type_and_if_has_exception=true;
                if(mWifiManager.getWifiState()==WifiManager.WIFI_STATE_DISABLED) {
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_wifi_disabled));
                        log_exception.append(" ");
                        return false;
                    }
                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_wifi_disabled));
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                        log_exception.append(" ");
                        return true;
                    }
                }
            }
        }catch (NumberFormatException ne){
            ne.printStackTrace();
            log_exception.append(ne.toString());
            log_exception.append("\n");
        }

        /*try{
            if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_WIFI_CONNECTED])==1){
                type_and_if_has_exception=true;
                if(isWifiConnected()){
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_wifi_connected));
                        log_exception.append(" ");
                        return false;
                    }
                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_wifi_connected));
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.log_exceptions_unsatisfied));
                        log_exception.append(" ");
                        return true;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }*/

        /*try{
            if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_WIFI_DISCONNECTED])==1){
                type_and_if_has_exception=true;
                if(!isWifiConnected()){
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_wifi_disconnected));
                        log_exception.append(" ");
                        return false;
                    }
                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_wifi_disconnected));
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.log_exceptions_unsatisfied));
                        log_exception.append(" ");
                        return true;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }*/

        try{
            BluetoothAdapter mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BLUETOOTH_ENABLED])==1){
                type_and_if_has_exception=true;
                if(mBluetoothAdapter.getState()== BluetoothAdapter.STATE_ON) {
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_bluetooth_enabled));
                        log_exception.append(" ");
                        return false;
                    }
                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_bluetooth_enabled));
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                        log_exception.append(" ");
                        return true;
                    }
                }
            }
            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BLUETOOTH_DISABLED])==1){
                type_and_if_has_exception=true;
                if(mBluetoothAdapter.getState()==BluetoothAdapter.STATE_OFF) {
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_bluetooth_disabled));
                        log_exception.append(" ");
                        return false;
                    }
                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_bluetooth_disabled));
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                        log_exception.append(" ");
                        return true;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            log_exception.append(e.toString());
            log_exception.append("\n");
        }

        try{
            AudioManager mAudioManager=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_RING_VIBRATE])==1){
                type_and_if_has_exception=true;
                if(mAudioManager.getRingerMode()== AudioManager.RINGER_MODE_VIBRATE) {
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_ring_vibrate));
                        log_exception.append(" ");
                        return false;
                    }
                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_ring_vibrate));
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                        log_exception.append(" ");
                        return true;
                    }
                }
            }

            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_RING_OFF])==1){
                type_and_if_has_exception=true;
                if(mAudioManager.getRingerMode()==AudioManager.RINGER_MODE_SILENT) {
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_ring_off));
                        log_exception.append(" ");
                        return false;
                    }
                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_ring_off));
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                        log_exception.append(" ");
                        return true;
                    }
                }
            }

            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_RING_NORMAL])==1){
                type_and_if_has_exception=true;
                if(mAudioManager.getRingerMode()==AudioManager.RINGER_MODE_NORMAL) {
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_ring_normal));
                        log_exception.append(" ");
                        return false;
                    }
                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_ring_normal));
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                        log_exception.append(" ");
                        return true;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            log_taskitem.append(e.toString());
            log_taskitem.append("\n");
        }

        try{
            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_NET_ENABLED])==1){
                type_and_if_has_exception=true;
                if(isCellarNetworkEnabled()) {
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_net_enabled));
                        log_exception.append(" ");
                        return false;
                    }
                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_net_enabled));
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                        log_exception.append(" ");
                        return true;
                    }
                }
            }

            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_NET_DISABLED])==1){
                type_and_if_has_exception=true;
                if(!isCellarNetworkEnabled()) {
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_net_disabled));
                        log_exception.append(" ");
                        return false;
                    }
                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_net_disabled));
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                        log_exception.append(" ");
                        return true;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_GPS_ENABLED])==1){
                type_and_if_has_exception=true;
                if(isLocationServiceEnabled()) {
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_gps_on));
                        log_exception.append(" ");
                        return false;
                    }
                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_gps_on));
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                        log_exception.append(" ");
                        return true;
                    }
                }
            }

            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_GPS_DISABLED])==1){
                type_and_if_has_exception=true;
                if(!isLocationServiceEnabled()) {
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_gps_off));
                        log_exception.append(" ");
                        return false;
                    }
                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_gps_off));
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                        log_exception.append(" ");
                        return true;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_AIRPLANE_MODE_ENABLED])==1){
                type_and_if_has_exception=true;
                if(isAirplaneModeOn()) {
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_airplanemode_on));
                        log_exception.append(" ");
                        return false;
                    }
                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_airplanemode_on));
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                        log_exception.append(" ");
                        return true;
                    }
                }
            }

            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_AIRPLANE_MODE_DISABLED])==1){
                type_and_if_has_exception=true;
                if(!isAirplaneModeOn()) {
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_airplanemode_off));
                        log_exception.append(" ");
                        return false;
                    }

                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_airplanemode_off));
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                        log_exception.append(" ");
                        return true;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE])!=-1){
                type_and_if_has_exception=true;
                // Log.e(TAG,"isInstant is "+BatteryReceiver.isInstant+" currentTemp is "+BatteryReceiver.Battery_temperature+" item set is "+item.battery_temperature);
                int currentTemp=Global.BatteryReceiver.battery_temperature;
                if(currentTemp>Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE])*10) {
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.log_exceptions_battery_temperature_higher_than));
                        log_exception.append(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]);
                        log_exception.append("℃,current:");
                        log_exception.append((double)currentTemp/10);
                        log_exception.append("℃");
                        log_exception.append(" ");
                        return false;
                    }
                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.log_exceptions_battery_temperature_higher_than));
                        log_exception.append(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]);
                        log_exception.append("℃,current:");
                        log_exception.append((double)currentTemp/10);
                        log_exception.append("℃");
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                        return true;
                    }
                }
            }

            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE])!=-1){
                type_and_if_has_exception=true;
                int currentTemp=Global.BatteryReceiver.battery_temperature;
                if(currentTemp<Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE])*10) {
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.log_exceptions_battery_temperature_lower_than));
                        log_exception.append(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]);
                        log_exception.append("℃,current:");
                        log_exception.append((double)currentTemp/10);
                        log_exception.append("℃");
                        log_exception.append(" ");
                        return false;
                    }
                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.log_exceptions_battery_temperature_lower_than));
                        log_exception.append(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]);
                        log_exception.append("℃,current:");
                        log_exception.append((double)currentTemp/10);
                        log_exception.append("℃");
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                        return true;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE])!=-1) {
                type_and_if_has_exception=true;
                int currentLevel=Global.BatteryReceiver.battery_percentage;
                if(currentLevel>Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE])) {
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.log_exceptions_battery_percentage_more_than));
                        log_exception.append(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]);
                        log_exception.append("%,current:");
                        log_exception.append(currentLevel);
                        log_exception.append("%");
                        log_exception.append(" ");
                        return false;
                    }
                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.log_exceptions_battery_percentage_more_than));
                        log_exception.append(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]);
                        log_exception.append("%,current:");
                        log_exception.append(currentLevel);
                        log_exception.append("%");
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                        return true;
                    }
                }
            }

            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE])!=-1){
                type_and_if_has_exception=true;
                int currentLevel=Global.BatteryReceiver.battery_percentage;
                if(currentLevel<Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE])) {
                    if(processType==TYPE_OR){
                        log_exception.append(context.getResources().getString(R.string.log_exceptions_battery_percentage_less_than));
                        log_exception.append(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]);
                        log_exception.append("%,current:");
                        log_exception.append(currentLevel);
                        log_exception.append("%");
                        log_exception.append(" ");
                        return false;
                    }

                }else{
                    if(processType==TYPE_AND){
                        log_exception.append(context.getResources().getString(R.string.log_exceptions_battery_percentage_less_than));
                        log_exception.append(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]);
                        log_exception.append("%,current:");
                        log_exception.append(currentLevel);
                        log_exception.append("%");
                        log_exception.append(" ");
                        log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                        return true;
                    }
                }
            }

            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_HEADSET_STATUS])>0){
                type_and_if_has_exception=true;
                int value=Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_HEADSET_STATUS]);
                if(value== ExceptionConsts.EXCEPTION_HEADSET_PLUG_OUT){
                    if(!Global.HeadsetPlugReceiver.isHeadsetPluggedIn()){
                        if(processType==TYPE_OR){
                            log_exception.append(context.getResources().getString(R.string.log_exceptions_headset));
                            log_exception.append(context.getResources().getString(R.string.log_exceptions_headset_out));
                            log_exception.append(" ");
                            return false;
                        }
                    }else{
                        if(processType==TYPE_AND){
                            log_exception.append(context.getResources().getString(R.string.log_exceptions_headset));
                            log_exception.append(context.getResources().getString(R.string.log_exceptions_headset_in));
                            log_exception.append(" ");
                            return true;
                        }
                    }
                }

                if(value== ExceptionConsts.EXCEPTION_HEADSET_PLUG_IN){
                    if(Global.HeadsetPlugReceiver.isHeadsetPluggedIn()){
                        if(processType==TYPE_OR){
                            log_exception.append(context.getResources().getString(R.string.log_exceptions_headset));
                            log_exception.append(context.getResources().getString(R.string.log_exceptions_headset_in));
                            log_exception.append(" ");
                            return false;
                        }
                    }else{
                        if(processType==TYPE_AND){
                            log_exception.append(context.getResources().getString(R.string.log_exceptions_headset));
                            log_exception.append(context.getResources().getString(R.string.log_exceptions_headset_out));
                            log_exception.append(" ");
                            return true;
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            Calendar calendar=Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int dayofweek=calendar.get(Calendar.DAY_OF_WEEK);
            if(processType==TYPE_OR){
                switch (dayofweek){
                    default:break;
                    case Calendar.SUNDAY:{
                        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_SUNDAY])==1) {
                            log_exception.append(context.getResources().getString(R.string.sunday));
                            log_exception.append(" ");
                            return false;
                        }
                    }
                    break;
                    case Calendar.MONDAY:{
                        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_MONDAY])==1){
                            log_exception.append(context.getResources().getString(R.string.monday));
                            log_exception.append(" ");
                            return false;
                        }
                    }
                    break;
                    case Calendar.TUESDAY:{
                        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_TUESDAY])==1){
                            log_exception.append(context.getResources().getString(R.string.tuesday));
                            log_exception.append(" ");
                            return false;
                        }
                    }
                    break;
                    case Calendar.WEDNESDAY:{
                        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_WEDNESDAY])==1){
                            log_exception.append(context.getResources().getString(R.string.wednesday));
                            log_exception.append(" ");
                            return false;
                        }
                    }
                    break;
                    case Calendar.THURSDAY:{
                        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_THURSDAY])==1){
                            log_exception.append(context.getResources().getString(R.string.thursday));
                            log_exception.append(" ");
                            return false;
                        }
                    }
                    break;
                    case Calendar.FRIDAY:{
                        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_FRIDAY])==1){
                            log_exception.append(context.getResources().getString(R.string.friday));
                            log_exception.append(" ");
                            return false;
                        }
                    }
                    break;
                    case Calendar.SATURDAY:{
                        if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_SATURDAY])==1){
                            log_exception.append(context.getResources().getString(R.string.saturday));
                            log_exception.append(" ");
                            return false;
                        }
                    }
                    break;

                }
            }

            if(processType==TYPE_AND){
                List<Integer> selected_dayofweek=new ArrayList<>();
                if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_SUNDAY])==1) selected_dayofweek.add(Calendar.SUNDAY);
                if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_MONDAY])==1) selected_dayofweek.add(Calendar.MONDAY);
                if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_TUESDAY])==1) selected_dayofweek.add(Calendar.TUESDAY);
                if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_WEDNESDAY])==1) selected_dayofweek.add(Calendar.WEDNESDAY);
                if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_THURSDAY])==1) selected_dayofweek.add(Calendar.THURSDAY);
                if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_FRIDAY])==1) selected_dayofweek.add(Calendar.FRIDAY);
                if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_SATURDAY])==1) selected_dayofweek.add(Calendar.SATURDAY);
                if(selected_dayofweek.size()>0){
                    type_and_if_has_exception=true;
                    switch(dayofweek){
                        default:break;
                        case Calendar.SUNDAY:{
                            if(!selected_dayofweek.contains(Calendar.SUNDAY)){
                                log_exception.append(context.getResources().getString(R.string.sunday));
                                log_exception.append(" ");
                                log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                                log_exception.append(" ");
                                return true;
                            }
                        }
                        break;
                        case Calendar.MONDAY:{
                            if(!selected_dayofweek.contains(Calendar.MONDAY)){
                                log_exception.append(context.getResources().getString(R.string.monday));
                                log_exception.append(" ");
                                log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                                log_exception.append(" ");
                                return true;
                            }
                        }
                        break;
                        case Calendar.TUESDAY:{
                            if(!selected_dayofweek.contains(Calendar.TUESDAY)){
                                log_exception.append(context.getResources().getString(R.string.tuesday));
                                log_exception.append(" ");
                                log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                                log_exception.append(" ");
                                return true;
                            }
                        }
                        break;
                        case Calendar.WEDNESDAY:{
                            if(!selected_dayofweek.contains(Calendar.WEDNESDAY)){
                                log_exception.append(context.getResources().getString(R.string.wednesday));
                                log_exception.append(" ");
                                log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                                log_exception.append(" ");
                                return true;
                            }
                        }
                        break;
                        case Calendar.THURSDAY:{
                            if(!selected_dayofweek.contains(Calendar.THURSDAY)){
                                log_exception.append(context.getResources().getString(R.string.thursday));
                                log_exception.append(" ");
                                log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                                log_exception.append(" ");
                                return true;
                            }
                        }
                        break;
                        case Calendar.FRIDAY:{
                            if(!selected_dayofweek.contains(Calendar.FRIDAY)){
                                log_exception.append(context.getResources().getString(R.string.friday));
                                log_exception.append(" ");
                                log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                                log_exception.append(" ");
                                return true;
                            }
                        }
                        break;
                        case Calendar.SATURDAY:{
                            if(!selected_dayofweek.contains(Calendar.SATURDAY)){
                                log_exception.append(context.getResources().getString(R.string.saturday));
                                log_exception.append(" ");
                                log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                                log_exception.append(" ");
                                return true;
                            }
                        }
                        break;
                    }
                }
            }


            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_START_TIME])!=-1&&Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_END_TIME])!=-1){
                int minuteOfDay=calendar.get(Calendar.HOUR_OF_DAY)*60+calendar.get(Calendar.MINUTE);
                type_and_if_has_exception=true;
                //Log.i("Minute of day now",""+minuteOfDay);
                //Log.i("startTime",""+item.exceptions[PublicConsts.EXCEPTION_START_TIME]);
                //Log.i("endTime",""+item.exceptions[PublicConsts.EXCEPTION_END_TIME]);
                int startTime=Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_START_TIME]);
                int endTime=Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_END_TIME]);
                if(endTime-startTime>0){
                    if((minuteOfDay>=startTime)&&minuteOfDay<=endTime) {
                        if(processType==TYPE_OR){
                            log_exception.append(context.getResources().getString(R.string.log_exceptions_period));
                            log_exception.append(ValueUtils.format(startTime/60));
                            log_exception.append(":");
                            log_exception.append(ValueUtils.format(startTime%60));
                            log_exception.append("~");
                            log_exception.append(ValueUtils.format(endTime/60));
                            log_exception.append(":");
                            log_exception.append(ValueUtils.format(endTime%60));
                            log_exception.append(" ");
                            return false;
                        }
                    }else{
                        if(processType==TYPE_AND){
                            log_exception.append(context.getResources().getString(R.string.log_exceptions_period));
                            log_exception.append(ValueUtils.format(startTime/60));
                            log_exception.append(":");
                            log_exception.append(ValueUtils.format(startTime%60));
                            log_exception.append("~");
                            log_exception.append(ValueUtils.format(endTime/60));
                            log_exception.append(":");
                            log_exception.append(ValueUtils.format(endTime%60));
                            log_exception.append(" ");
                            log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                            return true;
                        }
                    }
                }else{
                    if(minuteOfDay>=startTime||minuteOfDay<=endTime) {
                        if(processType==TYPE_OR){
                            log_exception.append(context.getResources().getString(R.string.log_exceptions_period));
                            log_exception.append(ValueUtils.format(startTime/60));
                            log_exception.append(":");
                            log_exception.append(ValueUtils.format(startTime%60));
                            log_exception.append("~");
                            log_exception.append(ValueUtils.format(endTime/60));
                            log_exception.append(":");
                            log_exception.append(ValueUtils.format(endTime%60));
                            log_exception.append(" ");
                            return false;
                        }
                    }else{
                        if(processType==TYPE_AND){
                            log_exception.append(context.getResources().getString(R.string.log_exceptions_period));
                            log_exception.append(ValueUtils.format(startTime/60));
                            log_exception.append(":");
                            log_exception.append(ValueUtils.format(startTime%60));
                            log_exception.append("~");
                            log_exception.append(ValueUtils.format(endTime/60));
                            log_exception.append(":");
                            log_exception.append(ValueUtils.format(endTime%60));
                            log_exception.append(" ");
                            log_exception.append(context.getResources().getString(R.string.word_unsatisfied));
                            return true;
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if(processType==TYPE_AND){
            if(type_and_if_has_exception) return false;
        }

        return true;
    }

    private void activateActionOfWifi(int action_wifi){
        try{
            WifiManager mWifiManager=(WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            switch (action_wifi){
                default:break;
                case ActionConsts.ActionValueConsts.ACTION_OPEN:{
                    boolean action_wifi_result=mWifiManager.setWifiEnabled(true);
                    Log.i(TAG,"Try to enable wifi...");
                    log_taskitem.append(context.getResources().getString(R.string.action_wifi_open));
                    log_taskitem.append(":");
                    log_taskitem.append(action_wifi_result ? context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                    log_taskitem.append(" ");
                }
                break;
                case ActionConsts.ActionValueConsts.ACTION_CLOSE:{
                    boolean action_wifi_result=mWifiManager.setWifiEnabled(false);
                    Log.i(TAG,"Try to disable wifi...");
                    log_taskitem.append(context.getResources().getString(R.string.action_wifi_close));
                    log_taskitem.append(":");
                    log_taskitem.append(action_wifi_result ? context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                    log_taskitem.append(" ");
                }
                break;
            }
        }catch (Exception e){
            e.printStackTrace();
            log_taskitem.append(e.toString());
            log_taskitem.append(" ");
        }
    }

    private void activateActionOfBluetooth(int action_bluetooth){
        try{
            BluetoothAdapter mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
            if(mBluetoothAdapter==null) return;
            switch (action_bluetooth){
                default:break;
                case ActionConsts.ActionValueConsts.ACTION_OPEN:{
                    boolean action_bluetooth_result=mBluetoothAdapter.enable();
                    Log.i(TAG,"try to enable bluetooth...");
                    log_taskitem.append(context.getResources().getString(R.string.action_bluetooth_open));
                    log_taskitem.append(":");
                    log_taskitem.append(action_bluetooth_result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                    log_taskitem.append(" ");
                }
                break;
                case ActionConsts.ActionValueConsts.ACTION_CLOSE:{
                    boolean action_bluetooth_result=mBluetoothAdapter.disable();
                    Log.i(TAG,"Try to disable bluetooth...");
                    log_taskitem.append(context.getResources().getString(R.string.action_bluetooth_close));
                    log_taskitem.append(":");
                    log_taskitem.append(action_bluetooth_result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                    log_taskitem.append(" ");
                }
                break;
            }
        }catch (Exception e){
            e.printStackTrace();
            log_taskitem.append(e.toString());
            log_taskitem.append(" ");
        }

    }

    private void activateActionOfRingMode(int action_ring_mode){
        AudioManager mAudioManager=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        boolean action_ring_mode_result=true;
        if(mAudioManager==null) return ;
        switch (action_ring_mode){
            default:break;
            case ActionConsts.ActionValueConsts.ACTION_RING_VIBRATE:{
                try{
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);Log.i(TAG,"Try to set audio vibrate...");
                }catch (Exception e){
                    e.printStackTrace();
                    LogUtil.putExceptionLog(context,e);
                    action_ring_mode_result=false;
                }
                // if(mAudioManager.getRingerMode()!=AudioManager.RINGER_MODE_VIBRATE) action_ring_mode_result=false;
                log_taskitem.append(context.getResources().getString(R.string.action_ring_mode_vibrate));
                log_taskitem.append(":");
                log_taskitem.append(action_ring_mode_result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                log_taskitem.append(" ");
                break;
            }
            case ActionConsts.ActionValueConsts.ACTION_RING_OFF:{
                //boolean action_ring_result=fa
                try{
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);Log.i(TAG,"Try to set audio silent...");
                }catch (Exception e){
                    e.printStackTrace();
                    LogUtil.putExceptionLog(context,e);
                    action_ring_mode_result=false;
                }
                // if(mAudioManager.getRingerMode()!=AudioManager.RINGER_MODE_SILENT)  action_ring_mode_result=false;
                log_taskitem.append(context.getResources().getString(R.string.action_ring_mode_off));
                log_taskitem.append(":");
                log_taskitem.append(action_ring_mode_result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                log_taskitem.append(" ");
                break;
            }
            case ActionConsts.ActionValueConsts.ACTION_RING_NORMAL:{
                try{
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);Log.i(TAG,"Try to set audio normal...");
                }catch (Exception e){
                    e.printStackTrace();
                    LogUtil.putExceptionLog(context,e);
                    action_ring_mode_result=false;
                }
                //if(mAudioManager.getRingerMode()!=AudioManager.RINGER_MODE_NORMAL) action_ring_mode_result=false;
                log_taskitem.append(context.getResources().getString(R.string.action_ring_mode_normal));
                log_taskitem.append(":");
                log_taskitem.append(action_ring_mode_result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                log_taskitem.append(" ");
                break;
            }
        }
    }

    private void activateActionOfVolume(String values){
        try{
            String [] volumes=values.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            AudioManager mAudioManager=(AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int volume_ring=Integer.parseInt(volumes[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_RING_LOCALE]);
            int volume_media=Integer.parseInt(volumes[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_MEDIA_LOCALE]);
            int volume_notification=Integer.parseInt(volumes[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_NOTIFICATION_LOCALE]);
            int volume_alarm=Integer.parseInt(volumes[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_ALARM_LOCALE]);
            if(volume_ring>=0) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_RING,volume_ring,AudioManager.FLAG_SHOW_UI);
                log_taskitem.append(context.getResources().getString(R.string.dialog_actions_ring_volume_ring));
                log_taskitem.append((int)(((double)volume_ring/mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING))*100));
                log_taskitem.append("% ");
            }
            if(volume_media>=0) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,volume_media,AudioManager.FLAG_SHOW_UI);
                log_taskitem.append(context.getResources().getString(R.string.dialog_actions_ring_volume_media));
                log_taskitem.append((int)(((double)volume_media/mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))*100));
                log_taskitem.append("% ");
            }
            if(volume_notification>=0){
                mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,volume_notification,AudioManager.FLAG_SHOW_UI);
                log_taskitem.append(context.getResources().getString(R.string.dialog_actions_ring_volume_notification));
                log_taskitem.append((int)(((double)volume_notification/mAudioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION))*100));
                log_taskitem.append("% ");
            }
            if(volume_alarm>=0) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM,volume_alarm,AudioManager.FLAG_SHOW_UI);
                log_taskitem.append(context.getResources().getString(R.string.dialog_actions_ring_volume_alarmclock));
                log_taskitem.append((int)(((double)volume_alarm/mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM))*100));
                log_taskitem.append("% ");
            }
            // log_taskitem.append(context.getResources().getString(R.string.action_ring_volume));
            //log_taskitem.append(" ");
        }catch (Exception e){
            e.printStackTrace();
            log_taskitem.append(e.toString());
            log_taskitem.append("\n");
        }
    }

    private void activateActionOfSettingRingtone(String values){
        try{
            // RingtoneManager ringtoneManager=new RingtoneManager(context);
            String ring_selection_values[]=values.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            int ring_notification_selection= Integer.parseInt(ring_selection_values[ActionConsts.ActionSecondLevelLocaleConsts.RING_SELECTION_NOTIFICATION_TYPE_LOCALE]);
            int ring_phone_selection=Integer.parseInt(ring_selection_values[ActionConsts.ActionSecondLevelLocaleConsts.RING_SELECTION_CALL_TYPE_LOCALE]);
            //String ring_value_notification=ring_selection_values[PublicConsts.RING_SELECTION_NOTIFICATION_VALUE_LOCALE];
            //String ring_value_phone=ring_selection_values[PublicConsts.RING_SELECTION_PHONE_VALUE_LOCALE];
            if(ring_notification_selection== ActionConsts.ActionValueConsts.RING_TYPE_FROM_SYSTEM ||ring_notification_selection== ActionConsts.ActionValueConsts.RING_TYPE_FROM_MEDIA)
            {
                //RingtoneManager.setActualDefaultRingtoneUri(context,RingtoneManager.TYPE_NOTIFICATION ,Uri.parse(ring_value_notification));
                RingtoneManager.setActualDefaultRingtoneUri(context,RingtoneManager.TYPE_NOTIFICATION ,Uri.parse(item.uri_ring_notification));
                log_taskitem.append(context.getResources().getString(R.string.action_set_ringtone_notification));
                log_taskitem.append(context.getResources().getString(R.string.log_result_success));
                log_taskitem.append(" ");
            }
            if(ring_phone_selection== ActionConsts.ActionValueConsts.RING_TYPE_FROM_SYSTEM ||ring_phone_selection== ActionConsts.ActionValueConsts.RING_TYPE_FROM_MEDIA)
            {
                //RingtoneManager.setActualDefaultRingtoneUri(context,RingtoneManager.TYPE_RINGTONE,Uri.parse(ring_value_phone));
                RingtoneManager.setActualDefaultRingtoneUri(context,RingtoneManager.TYPE_RINGTONE,Uri.parse(item.uri_ring_call));
                log_taskitem.append(context.getResources().getString(R.string.action_set_ringtone_phone));
                log_taskitem.append(context.getResources().getString(R.string.log_result_success));
                log_taskitem.append(" ");
            }
        }catch (Exception e){
            e.printStackTrace();
            log_taskitem.append(e.toString());
            log_taskitem.append("\n");
        }
    }

    private void activateActionOfBrightness(int screen_brightness){
        if(screen_brightness!=-1){
            if(screen_brightness== ActionConsts.ActionValueConsts.ACTION_BRIGHTNESS_AUTO){
                try{
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                }catch (Exception e){
                    e.printStackTrace();
                }
                log_taskitem.append(context.getResources().getString(R.string.action_brightness_auto));
                log_taskitem.append(" ");
            }else if(screen_brightness>=0&&screen_brightness<=PublicConsts.BRIGHTNESS_MAX) {
                try{
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, screen_brightness);
                }catch (Exception e){
                    e.printStackTrace();
                }
                log_taskitem.append(context.getResources().getString(R.string.action_brightness_manual));
                log_taskitem.append((int)(((double)screen_brightness/PublicConsts.BRIGHTNESS_MAX)*100));
                log_taskitem.append(" ");
            }

        }
    }

    private void activateActionOfWallpaper(String values){
        try{
            int value=Integer.parseInt(values);
            if(value>=0){
                log_taskitem.append(context.getResources().getString(R.string.action_set_wallpaper));
                Message msg=new Message();
                msg.what=TimeSwitchService.MESSAGE_DISPLAY_TOAST;
                msg.obj=String.valueOf(context.getResources().getString(R.string.att_change_wallpaper));
                TimeSwitchService.sendMessage(msg);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            WallpaperManager wallpaperManager=WallpaperManager.getInstance(context);
                            //DisplayMetrics displayMetrics=context.getResources().getDisplayMetrics();
                            //use file path instead of uri
                            //Bitmap bitmap= BitmapFactory.decodeFile(item.uri_wallpaper_desktop);//ValueUtils.getDecodedBitmapFromFile(item.uri_wallpaper_desktop,1920*1080);
                            //this is for the task already set from old versions and can execute successfully
                            //if(bitmap==null) bitmap= MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(item.uri_wallpaper_desktop));
                            wallpaperManager.setStream(new FileInputStream(new File(item.uri_wallpaper_desktop)));
                            //wallpaperManager.setBitmap(bitmap,null,WallpaperManager.FLAG_LOCK);
                        }catch (Exception e){
                            e.printStackTrace();
                            LogUtil.putExceptionLog(context,e);
                        }
                    }
                }).start();
                //log_taskitem.append(context.getResources().getString(R.string.log_result_success));
                log_taskitem.append(" ");
            }
        }catch (Exception e){
            e.printStackTrace();
            log_taskitem.append(e.toString());
            log_taskitem.append("\n");
        }
    }

    private void activateActionOfVibrate(String values){
        try{
            String vibrate_values[]=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
            long frequency=Long.parseLong(vibrate_values[ActionConsts.ActionSecondLevelLocaleConsts.VIBRATE_FREQUENCY_LOCALE]);
            long duration=Long.parseLong(vibrate_values[ActionConsts.ActionSecondLevelLocaleConsts.VIBRATE_DURATION_LOCALE]);
            long interval=Long.parseLong(vibrate_values[ActionConsts.ActionSecondLevelLocaleConsts.VIBRATE_INTERVAL_LOCALE]);
            if(frequency>0){
                Vibrator vibrator=(Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if(vibrator==null) return;
                long[] vibrate_array=new long[(int)(frequency*2)+1];
                vibrate_array[0]=0;
                for(int i=1;i<vibrate_array.length;i++){
                    vibrate_array[i]=(i%2==0?interval:duration);
                }
                vibrator.vibrate(vibrate_array,-1);
                log_taskitem.append(context.getResources().getString(R.string.activity_taskgui_actions_vibrate));
                log_taskitem.append(" ");
            }
        }catch (Exception e){
            e.printStackTrace();
            log_taskitem.append(e.toString());
            log_taskitem.append("\n");
        }
    }

    private void activateActionOfToast(String values){
        try{
            String [] toast_values=values.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            int type=Integer.parseInt(toast_values[ActionConsts.ActionSecondLevelLocaleConsts.TOAST_TYPE_LOCALE]);
            if(type>=0){
                  /* Toast toast=Toast.makeText(context,item.toast,Toast.LENGTH_SHORT);
                   if(type==PublicConsts.TOAST_TYPE_CUSTOM) toast.setGravity(Gravity.TOP|Gravity.START,
                           Integer.parseInt(toast_values[PublicConsts.TOAST_LOCATION_X_OFFSET_LOCALE]),
                           Integer.parseInt(toast_values[PublicConsts.TOAST_LOCATION_Y_OFFSET_LOCALE]));*/
                TimeSwitchService.CustomToast content=new TimeSwitchService.CustomToast();
                content.toast_value=toast_values;
                content.toast=item.toast;
                Message message=new Message();
                message.what=TimeSwitchService.MESSAGE_DISPLAY_CUSTOM_TOAST;
                message.obj=content;
                TimeSwitchService.sendMessage(message);
                // toast.show();
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.putExceptionLog(context,e);
        }
    }

    private void activateActionOfAutorotation(String value){
        try{
           int a=Integer.parseInt(value);
           if(a<0) return;
           android.provider.Settings.System.putInt(context.getContentResolver(),Settings.System.ACCELEROMETER_ROTATION,a);
        }catch (Exception e){e.printStackTrace();}
    }

    private void activateActionOfSMS(String values){
        try{
            String sms_values[]=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
            if(Integer.parseInt(sms_values[ActionConsts.ActionSecondLevelLocaleConsts.SMS_ENABLED_LOCALE])>=0){
                SmsManager manager;
                int subscriptionId=Integer.parseInt(sms_values[ActionConsts.ActionSecondLevelLocaleConsts.SMS_SUBINFO_LOCALE]);
                if(Build.VERSION.SDK_INT>=22&&subscriptionId>=0){
                    manager=SmsManager.getSmsManagerForSubscriptionId(subscriptionId);
                }else {
                    manager=SmsManager.getDefault();
                }
                List<String> messages=manager.divideMessage(item.sms_message);
                String[] addresses=item.sms_address.split(PublicConsts.SEPARATOR_SMS_RECEIVERS);
                for(String address:addresses){
                    for(String message:messages){
                        try{
                            manager.sendTextMessage(address,null,message,null,null);
                        }catch (IllegalArgumentException le){
                            le.printStackTrace();
                            LogUtil.putExceptionLog(context,le);
                        }

                    }
                }
                log_taskitem.append(context.getResources().getString(R.string.action_sms_send));
                log_taskitem.append(":");
                for(String log_address:addresses){
                    log_taskitem.append(log_address);
                    log_taskitem.append(PublicConsts.SEPARATOR_SMS_RECEIVERS);
                }
                log_taskitem.append(" ");
            }
        }catch (Exception e){
            e.printStackTrace();
            log_taskitem.append(e.toString());
            log_taskitem.append("\n");
            //LogUtil.putExceptionLog(context,e);
        }
    }

    private void activateActionOfNet(int action_net){
        switch (action_net){
            default:break;
            case ActionConsts.ActionValueConsts.ACTION_OPEN:{
                boolean result=RootUtils.executeCommand(RootUtils.COMMAND_ENABLE_CELLUAR_NETWORK)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
                log_taskitem.append(context.getResources().getString(R.string.action_net_on));
                log_taskitem.append(":");
                log_taskitem.append(result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                log_taskitem.append(" ");
            }
            break;
            case ActionConsts.ActionValueConsts.ACTION_CLOSE:{
                boolean result=RootUtils.executeCommand(RootUtils.COMMAND_DISABLE_CELLUAR_NETWORK)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
                log_taskitem.append(context.getResources().getString(R.string.action_net_off));
                log_taskitem.append(":");
                log_taskitem.append(result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                log_taskitem.append(" ");
            }
            break;
        }
    }

    private void activateActionOfGps(int action_gps){
        Log.d(TAG,"BEGIN TO OPERATE GPS!!!!!!");
        switch (action_gps){
            default:break;
            case ActionConsts.ActionValueConsts.ACTION_OPEN:{
                String command=RootUtils.COMMAND_ENABLE_GPS;
                if(Build.VERSION.SDK_INT>=23) command=RootUtils.COMMAND_ENABLE_GPS_API23;
                final String runCommand=command;

                RootUtils.executeCommand(runCommand);
                log_taskitem.append(context.getResources().getString(R.string.action_gps_on));
                //log_taskitem.append(":");
                //log_taskitem.append(result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                log_taskitem.append(" ");
                //Log.i(TAG,"Root command of enable gps 's result is "+result);
            }
            break;
            case ActionConsts.ActionValueConsts.ACTION_CLOSE:{
                String command=RootUtils.COMMAND_DISABLE_GPS;
                if(Build.VERSION.SDK_INT>=23) command=RootUtils.COMMAND_DISABLE_GPS_API23;
                final String runCommand=command;
                RootUtils.executeCommand(runCommand);
                log_taskitem.append(context.getResources().getString(R.string.action_gps_off));
                //log_taskitem.append(":");
                //log_taskitem.append(result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                log_taskitem.append(" ");
                //Log.i(TAG,"Root command of disable gps "+result);
            }
            break;
        }
    }

    private void activateActionOfAirplaneMode(int action_airplanemode){
        switch (action_airplanemode){
            default:break;
            case ActionConsts.ActionValueConsts.ACTION_OPEN:{
                boolean result=RootUtils.executeCommand(RootUtils.COMMAND_ENABLE_AIRPLANE_MODE)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
                log_taskitem.append(context.getResources().getString(R.string.action_airplane_mode_on));
                log_taskitem.append(":");
                log_taskitem.append(result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                log_taskitem.append(" ");
            }
            break;
            case ActionConsts.ActionValueConsts.ACTION_CLOSE:{
                boolean result=RootUtils.executeCommand(RootUtils.COMMAND_DISABLE_AIRPLANE_MODE)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
                log_taskitem.append(context.getResources().getString(R.string.action_airplane_mode_off));
                log_taskitem.append(":");
                log_taskitem.append(result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                log_taskitem.append(" ");
            }
            break;
        }
    }

    private void activateActionOfDeviceControl(int action_device_control){
        switch (action_device_control){
            default:break;
            case ActionConsts.ActionValueConsts.ACTION_DEVICECONTROL_SHUTDOWN:{
                boolean result=RootUtils.executeCommand(RootUtils.COMMAND_SHUTDOWN)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
                log_taskitem.append(context.getResources().getString(R.string.action_device_shutdown));
                log_taskitem.append(":");
                log_taskitem.append(result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                log_taskitem.append(" ");
            }
            break;
            case ActionConsts.ActionValueConsts.ACTION_DEVICECONTROL_REBOOT:{
                boolean result=RootUtils.executeCommand(RootUtils.COMMAND_REBOOT)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
                log_taskitem.append(context.getResources().getString(R.string.action_device_reboot));
                log_taskitem.append(":");
                log_taskitem.append(result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                log_taskitem.append(" ");

            }break;
        }
    }

    private void activateActionOfNotification(String values){
        try{
            String notification_values[]=values.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            int type=Integer.parseInt(notification_values[ActionConsts.ActionSecondLevelLocaleConsts.NOTIFICATION_TYPE_LOCALE]);
            if(type== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_NOT_OVERRIDE ||type== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_OVERRIDE_LAST){
                NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder builder;
                if(Build.VERSION.SDK_INT>=26){
                    String channel_id="channel_tasks";
                    NotificationChannel channel=new NotificationChannel(channel_id,"Tasks", NotificationManager.IMPORTANCE_DEFAULT);
                    manager.createNotificationChannel(channel);
                    builder=new NotificationCompat.Builder(context,channel_id);
                }else{
                    builder=new NotificationCompat.Builder(context);
                }
                builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                builder.setSmallIcon(R.drawable.ic_launcher);
                builder.setContentTitle(context.getResources().getString(R.string.notification_task_activated_title));
                builder.setContentText(log_taskitem.toString());
                if(Integer.parseInt(notification_values[ActionConsts.ActionSecondLevelLocaleConsts.NOTIFICATION_TYPE_IF_CUSTOM_LOCALE])== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_CONTENT_CUSTOM){
                    builder.setContentTitle(item.notification_title);
                    builder.setContentText(item.notification_message);
                }
                PendingIntent pi =PendingIntent.getActivity(context,1,new Intent(),PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pi);
                builder.setAutoCancel(true);
                builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                builder.setFullScreenIntent(pi,false);
                if(type== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_NOT_OVERRIDE){
                    if(notification_id<Integer.MAX_VALUE) notification_id++;//Can user make notification shows like that total?
                    else notification_id=2;
                }
                manager.notify(notification_id,builder.build());
                log_taskitem.append(context.getResources().getString(R.string.activity_taskgui_actions_notification));
                log_taskitem.append(" ");
            }
        }catch (Exception e){
            e.printStackTrace();
            log_taskitem.append(e.toString());
            log_taskitem.append("\n");
        }
    }

    private void launchAppsByPackageName(Context context,String values){
        try{
            String packageNames[]=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
            PackageManager manager=context.getPackageManager();
            if(manager==null) return;
            if(packageNames.length==0) return;
            try{if(Integer.parseInt(values)==-1) return;}catch (Exception e){}
            for(String s:packageNames){
                if(s==null) continue;
                try{
                    Intent i=manager.getLaunchIntentForPackage(s);
                    context.startActivity(i);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void stopAppsByPackageName(Context context,String values){
        try{
            ActivityManager manager=(ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if(manager==null) return;
            String [] packageNames=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
            for(String s:packageNames){
                manager.killBackgroundProcesses(s);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * ROOT Required
     * @param values package names split by ":"
     */
    private void forceStopAppsByPackageName(String values){
        //android.permission.FORCE_STOP_PACKAGES
        try{
            String [] packageNames=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
            for(String s:packageNames){
                RootUtils.executeCommand(RootUtils.COMMAND_FORCE_STOP_PACKAGE+" "+s);
            }
            /*ActivityManager manager=(ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            Method method=ActivityManager.class.getMethod("forceStopPackage",String.class);
            for(String s:packageNames){
                method.invoke(manager,s);
            }*/
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean isCellarNetworkEnabled(){
        ConnectivityManager connectivityManager=(ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager==null) return  false;
        else {
            NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
            if(networkInfo==null) return false;
            if(networkInfo.getType()==ConnectivityManager.TYPE_MOBILE) return true;
            if(networkInfo.getType()==ConnectivityManager.TYPE_WIFI){
                try{
                    Method getMobileDataEnabledMethod = ConnectivityManager.class.getDeclaredMethod("getMobileDataEnabled");
                    getMobileDataEnabledMethod.setAccessible(true);
                    return (Boolean) getMobileDataEnabledMethod.invoke(connectivityManager);
                }catch (Exception e){
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * get if wifi is connected
     * @return returns true if wifi is connected
     */
    private boolean isWifiConnected(@Nullable Integer[] ssids){
        try{
            ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(connectivityManager==null) return false;
            if(connectivityManager.getActiveNetworkInfo().getType()==ConnectivityManager.TYPE_WIFI) return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 获取系统GPS是否开启
     * @return GPS是否开启
     */
    private boolean isLocationServiceEnabled(){
        LocationManager locationManager=(LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager!=null&&(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }

    private boolean isAirplaneModeOn(){
        return Settings.System.getInt(context.getContentResolver(),Settings.ACTION_AIRPLANE_MODE_SETTINGS,0)==1;
    }

    private boolean isInCall(){
        if(Build.VERSION.SDK_INT>=21){
            try{
                TelecomManager manager=(TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                if(PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)==PermissionChecker.PERMISSION_GRANTED) return manager.isInCall();
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            try{
                TelephonyManager manager=(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                //manager.getCallState()

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return false;
    }


    /**
     * 通过TaskItem中的ID获取该TaskItem在list中的位置；
     * 如果查询不到该ID，则返回-1
     * @param id TaskItem中的id
     * @return TaskItem所在list的position
     */
    public static int getPosition(int id){
        if(TimeSwitchService.list==null) return -1;
        try{
            for(int i=0;i< TimeSwitchService.list.size();i++){
                if(id==TimeSwitchService.list.get(i).id) return i;
            }
        }catch (Exception e){e.printStackTrace();}
        return -1;
    }

    /**
     * 启用或者关闭指定任务
     * @param enableOrDisable 0--enable,1--disable
     */
    private void switchTasks(int enableOrDisable){
        SQLiteDatabase database=MySQLiteOpenHelper.getInstance(context).getWritableDatabase();
        try{
            String[] switch_values=item.actions[enableOrDisable==0? ActionConsts.ActionFirstLevelLocaleConsts.ACTION_ENABLE_TASKS_LOCALE: ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DISABLE_TASKS_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL);
            if(Integer.parseInt(switch_values[0])>=0){
                for(String s:switch_values){
                    int id=Integer.parseInt(s);
                    Cursor cursor=database.rawQuery("select * from "+ MySQLiteOpenHelper.getCurrentTableName(context)+" where "+SQLConsts.SQL_TASK_COLUMN_ID+"="+id,null);
                    if(cursor.getCount()>0){
                        //int position=getPosition(id);
                        setTaskEnabled(context,id,enableOrDisable==0);
                    }
                    cursor.close();
                }
                MainActivity.sendEmptyMessage(MainActivity.MESSAGE_REQUEST_UPDATE_LIST);
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.putExceptionLog(context,e);
        }
    }

    /**
     * 启用或者关闭指定任务，更新list，刷新数据库
     * @param id 任务id
     */
    public static void setTaskEnabled(Context context,int id,boolean enabled){
        if(TimeSwitchService.list==null) return;
        int position=getPosition(id);
        if(position<0||position>=TimeSwitchService.list.size()) return;
        try{
            if(TimeSwitchService.list.get(position).trigger_type== TriggerTypeConsts.TRIGGER_TYPE_SINGLE&&TimeSwitchService.list.get(position).time<=System.currentTimeMillis()) return;
            TimeSwitchService.list.get(position).isenabled=enabled;
            SQLiteDatabase database=MySQLiteOpenHelper.getInstance(context).getWritableDatabase();
            database.execSQL("update "+ MySQLiteOpenHelper.getCurrentTableName(context)
                    +" set "+SQLConsts.SQL_TASK_COLUMN_ENABLED +"="+(enabled?1:0)+
                    " where "+SQLConsts.SQL_TASK_COLUMN_ID +"="+id);

            if(TimeSwitchService.list.get(position).trigger_type== TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME&&enabled){
                TimeSwitchService.list.get(position).time=System.currentTimeMillis();
                Cursor cursor=database.rawQuery("select * from "+ MySQLiteOpenHelper.getCurrentTableName(context)+" where "+SQLConsts.SQL_TASK_COLUMN_ID+"="+id,null);
                if(cursor.moveToFirst()){
                    long[] values_read=ValueUtils.string2longArray(cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES)));
                    if(values_read.length==2){
                        long interval_read=values_read[1];
                        long values_put[]=new long[2];
                        values_put[0]=System.currentTimeMillis();
                        values_put[1]=interval_read;
                        ContentValues contentValues=new ContentValues();
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES,ValueUtils.longArray2String(values_put));
                        database.update(MySQLiteOpenHelper.getCurrentTableName(context),contentValues,SQLConsts.SQL_TASK_COLUMN_ID+"="+id,null);
                    }
                }
                cursor.close();
            }
            if(enabled) TimeSwitchService.list.get(position).activateTask(context); else TimeSwitchService.list.get(position).cancelTask();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void setTaskFolded(final Context context, int id , boolean isFolded){
        try{
            SQLiteDatabase database=MySQLiteOpenHelper.getInstance(context).getWritableDatabase();
            final String table_name= MySQLiteOpenHelper.getCurrentTableName(context);
            Cursor cursor=database.rawQuery("select * from "+table_name+" where "+SQLConsts.SQL_TASK_COLUMN_ID+"="+id,null);
            if(cursor.moveToFirst()){
                String additions_read[]=ValueUtils.string2StringArray(cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ADDITIONS)));
                String additions[]=new String[AdditionConsts.ADDITION_LENGTH];
                for(int i=0;i<additions.length;i++){additions[i]=String.valueOf(-1);}
                additions[AdditionConsts.ADDITION_TITLE_COLOR_LOCALE]=new TaskItem().addition_title_color;
                System.arraycopy(additions_read,0,additions,0,additions_read.length);
                additions[AdditionConsts.ADDITION_TITLE_FOLDED_VALUE_LOCALE]=isFolded?String.valueOf(0):String.valueOf(-1);
                ContentValues content=new ContentValues();
                content.put(SQLConsts.SQL_TASK_COLUMN_ADDITIONS,ValueUtils.stringArray2String(additions));
                database.update(table_name,content,SQLConsts.SQL_TASK_COLUMN_ID+"="+id,null);
            }
            cursor.close();
            TimeSwitchService.list.get(getPosition(id)).addition_isFolded=isFolded;
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
