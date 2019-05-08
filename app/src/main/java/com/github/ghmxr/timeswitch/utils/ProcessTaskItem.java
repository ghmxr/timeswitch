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
 * 处理并执行TaskItem中的exceptions,actions和一些附加选项的类
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

    public ProcessTaskItem(@NonNull Context context, TaskItem item){
        this.item=item;
        this.context=context;
        //this.canTrigger=true;
    }

    public void checkExceptionsAndRunActions(){
        if(this.item==null) return;

        log_taskitem.append(item.name);
        log_taskitem.append(":");

        SQLiteDatabase database= MySQLiteOpenHelper.getInstance(this.context).getWritableDatabase();

        if(item.trigger_type == TriggerTypeConsts.TRIGGER_TYPE_SINGLE){
            //do close the item
            item.isenabled=false;
            ContentValues values=new ContentValues();
            values.put(SQLConsts.SQL_TASK_COLUMN_ENABLED,0);
            database.update(MySQLiteOpenHelper.getCurrentTableName(this.context),values,SQLConsts.SQL_TASK_COLUMN_ID +"="+item.id,null);

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
                //int rows=database.delete(MySQLiteOpenHelper.getCurrentTableName(this.context),SQLConsts.SQL_TASK_COLUMN_ID +"="+item.id,null);
                MySQLiteOpenHelper.deleteRow(database,MySQLiteOpenHelper.getCurrentTableName(context),item.id);
                //Log.i(TAG,"receiver deleted "+rows+" rows");
            }catch (Exception e){
                e.printStackTrace();
            }
            try{
                TimeSwitchService.list.remove(item);
            }catch (Exception e){e.printStackTrace();}
            MainActivity.sendEmptyMessage(MainActivity.MESSAGE_REQUEST_UPDATE_LIST);
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
               action_device_control=Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DEVICE_CONTROL_LOCALE]);
           }catch (Exception e){
               e.printStackTrace();
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
                if(EnvironmentUtils.isGprsNetworkEnabled(context)) {
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
                if(!EnvironmentUtils.isGprsNetworkEnabled(context)) {
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
                if(EnvironmentUtils.isGpsEnabled(context)) {
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
                if(!EnvironmentUtils.isGpsEnabled(context)) {
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
                if(EnvironmentUtils.isAirplaneModeOpen(context)) {
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
                if(!EnvironmentUtils.isAirplaneModeOpen(context)) {
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

    private void activateActionOfBluetooth(int action_bluetooth){
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

    private void activateActionOfRingMode(int action_ring_mode){
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

    private void activateActionOfVolume(String values){
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

    private void activateActionOfSettingRingtone(String values){
        String ring_selection_values[]=values.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
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

    private void activateActionOfBrightness(int screen_brightness){
        if(screen_brightness!=-1){
            if(screen_brightness== ActionConsts.ActionValueConsts.ACTION_BRIGHTNESS_AUTO){
                EnvironmentUtils.setBrightness(context,true,0);
            }else if(screen_brightness>=0&&screen_brightness<=PublicConsts.BRIGHTNESS_MAX) {
                EnvironmentUtils.setBrightness(context,false,screen_brightness);
            }

        }
    }

    private void activateActionOfWallpaper(String values){
        int value=Integer.parseInt(values);
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

    private void activateActionOfVibrate(String values){
        String vibrate_values[]=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
        Integer frequency=Integer.parseInt(vibrate_values[ActionConsts.ActionSecondLevelLocaleConsts.VIBRATE_FREQUENCY_LOCALE]);
        long duration=Long.parseLong(vibrate_values[ActionConsts.ActionSecondLevelLocaleConsts.VIBRATE_DURATION_LOCALE]);
        long interval=Long.parseLong(vibrate_values[ActionConsts.ActionSecondLevelLocaleConsts.VIBRATE_INTERVAL_LOCALE]);
        if(frequency>0) EnvironmentUtils.vibrate(context,frequency,duration,interval);
    }

    private void activateActionOfToast(String values){
        String [] toast_values=values.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
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

    private void activateActionOfAutorotation(int value){
        if(value>=0) EnvironmentUtils.setIfAutorotation(context,value==1);
    }

    private void activateActionOfSMS(String values){
        String sms_values[]=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
        if(Integer.parseInt(sms_values[ActionConsts.ActionSecondLevelLocaleConsts.SMS_ENABLED_LOCALE])>=0){
            Integer subid=null;
            int subid_int=Integer.parseInt(sms_values[ActionConsts.ActionSecondLevelLocaleConsts.SMS_SUBINFO_LOCALE]);
            if(Build.VERSION.SDK_INT>=22&&subid_int>=0) subid=subid_int;
            EnvironmentUtils.sendSMSMessage(context,subid,item.sms_address.split(PublicConsts.SEPARATOR_SMS_RECEIVERS),item.sms_message
                    ,Integer.parseInt(sms_values[ActionConsts.ActionSecondLevelLocaleConsts.SMS_RESULT_TOAST_LOCALE])>=0);

        }
    }

    private void activateActionOfNet(int action_net){
        switch (action_net){
            default:break;
            case ActionConsts.ActionValueConsts.ACTION_OPEN: EnvironmentUtils.setGprsNetworkEnabled(true);break;
            case ActionConsts.ActionValueConsts.ACTION_CLOSE: EnvironmentUtils.setGprsNetworkEnabled(false);break;
        }
    }

    private void activateActionOfGps(int action_gps){
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

    private void activateActionOfAirplaneMode(int action_airplanemode){
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

    private void activateActionOfDeviceControl(int action_device_control){
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

    private void activateActionOfNotification(String values){
        String notification_values[]=values.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
        int type=Integer.parseInt(notification_values[ActionConsts.ActionSecondLevelLocaleConsts.NOTIFICATION_TYPE_LOCALE]);
        int if_custom=Integer.parseInt(notification_values[ActionConsts.ActionSecondLevelLocaleConsts.NOTIFICATION_TYPE_IF_CUSTOM_LOCALE]);
        if(type== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_NOT_OVERRIDE){
            if(notification_id<Integer.MAX_VALUE) notification_id++;//Can user make notification shows like that total?
            else notification_id=2;
        }
        String title,message;
        if(if_custom==ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_CONTENT_CUSTOM){
            title=item.notification_title;
            message=item.notification_message;
        }else {
            title=context.getResources().getString(R.string.notification_task_activated_title);
            message=log_taskitem.toString();
        }
        EnvironmentUtils.sendNotification(context,notification_id,title,message);
    }

    private void launchAppsByPackageName(Context context,String values){
        if(values.equals(String.valueOf(-1)))return;
        String packageNames[]=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
        EnvironmentUtils.launchAppByPackageName(context,packageNames);
    }

    private void stopAppsByPackageName(Context context,String values){
        if(values.equals(String.valueOf(-1)))return;
        String packageNames[]=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
        EnvironmentUtils.stopAppByPackageName(context,packageNames);
    }

    private void forceStopAppsByPackageName(String values){
        if(values.equals(String.valueOf(-1)))return;
        String packageNames[]=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
        for(String s:packageNames) EnvironmentUtils.forceStopAppByPackageName(s);
    }

    /**
     * 启用或者关闭指定任务
     * @param enableOrDisable 0--enable,1--disable
     */
    private void switchTasks(int enableOrDisable){
        SQLiteDatabase database=MySQLiteOpenHelper.getInstance(context).getWritableDatabase();
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
     * @param id 任务id
     */
    public static void setTaskEnabled(Context context,int id,boolean enabled){
        if(context==null||!(context instanceof TimeSwitchService)) {
            Log.e("invalid","an invalid type of context ,must be a type of TimeSwitchService instance!!");
            return;
        }

        TaskItem item=getTaskItemOfId(TimeSwitchService.list,id);
        if(item==null) return;

        try{
            if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_SINGLE&&item.time<=System.currentTimeMillis()) return;
            item.isenabled=enabled;
            SQLiteDatabase database=MySQLiteOpenHelper.getInstance(context).getWritableDatabase();
            database.execSQL("update "+ MySQLiteOpenHelper.getCurrentTableName(context)
                    +" set "+SQLConsts.SQL_TASK_COLUMN_ENABLED +"="+(enabled?1:0)+
                    " where "+SQLConsts.SQL_TASK_COLUMN_ID +"="+id);

            if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME&&enabled){
                long currentTime=System.currentTimeMillis();
                item.time=currentTime;
                Cursor cursor=database.rawQuery("select * from "+ MySQLiteOpenHelper.getCurrentTableName(context)+" where "+SQLConsts.SQL_TASK_COLUMN_ID+"="+id,null);
                if(cursor.moveToFirst()){
                    long[] values_read=ValueUtils.string2longArray(cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES)));
                    if(values_read.length==2){
                        long interval_read=values_read[1];
                        long values_put[]=new long[2];
                        values_put[0]=currentTime;
                        values_put[1]=interval_read;
                        ContentValues contentValues=new ContentValues();
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES,ValueUtils.longArray2String(values_put));
                        database.update(MySQLiteOpenHelper.getCurrentTableName(context),contentValues,SQLConsts.SQL_TASK_COLUMN_ID+"="+id,null);
                    }
                }
                cursor.close();
                database.close();
            }
            if(enabled) item.activateTask(context); else item.cancelTask();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void setTaskFolded(final Context context, int id , boolean isFolded){
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
        database.close();
        //TimeSwitchService.list.get(getPosition(id)).addition_isFolded=isFolded;
        TaskItem item=getTaskItemOfId(TimeSwitchService.list,id);
        if(item!=null) item.addition_isFolded=isFolded;
    }

}
