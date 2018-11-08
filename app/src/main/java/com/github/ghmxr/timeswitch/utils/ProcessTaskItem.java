package com.github.ghmxr.timeswitch.utils;

import android.app.Activity;
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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
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
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.activities.Main;
import com.github.ghmxr.timeswitch.activities.SmsActivity;
import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.SQLConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.receivers.BatteryReceiver;
import com.github.ghmxr.timeswitch.receivers.SMSReceiver;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

/**
 * 处理并执行TaskItem中的actions和一些附加选项的类
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class ProcessTaskItem {

    private TaskItem item;
    private Context context;
    private boolean canTrigger;
    static final String TAG="ProcessTaskItem";

    public ProcessTaskItem(@NonNull Context context, @Nullable TaskItem item){
        this.item=item;
        this.context=context;
        this.canTrigger=true;
    }

    public void activateTaskItem(){
        if(this.item==null) return;
        StringBuilder log_taskitem=new StringBuilder("");
        //log_taskitem.append(context.getResources().getString(R.string.log_taskname));
        log_taskitem.append(item.name);
        log_taskitem.append(":");
        WifiManager mWifiManager=(WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        KeyguardManager mKeyguardManager=(KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
        BluetoothAdapter mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        AudioManager mAudioManager=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        SQLiteDatabase database= MySQLiteOpenHelper.getInstance(this.context).getWritableDatabase();


        if(item.trigger_type ==PublicConsts.TRIGGER_TYPE_SINGLE){
            //do close the item
            ContentValues values=new ContentValues();
            values.put(SQLConsts.SQL_TASK_COLUMN_ENABLED,0);
            database.update(SQLConsts.getCurrentTableName(this.context),values,SQLConsts.SQL_TASK_COLUMN_ID +"="+item.id,null);

            //do close the single task and refresh the list in Main;
            int position=getPosition(item.id);
            if(TimeSwitchService.list!=null&&position>=0&&position<TimeSwitchService.list.size()) TimeSwitchService.list.get(position).isenabled=false;
            Main.sendEmptyMessage(Main.MESSAGE_REQUEST_UPDATE_LIST);
        }

        /*if(item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE||item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE
                ||item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE||item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE){

        }*/

        if(item.trigger_type ==PublicConsts.TRIGGER_TYPE_LOOP_WEEK){
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



        //boolean breakException=false;
        StringBuilder log_exception=new StringBuilder("");
        if(mKeyguardManager==null){
            Log.e(TAG,"KeyGuardManager is null!!");
        }else{
            try{
                if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_LOCKEDSCREEN])==1){
                    if(mKeyguardManager.inKeyguardRestrictedInputMode()) {
                        canTrigger=false;
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_screen_locked));
                        log_exception.append(" ");
                    }
                }
                if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_UNLOCKEDSCREEN])==1){
                    if(!mKeyguardManager.inKeyguardRestrictedInputMode()) {
                        canTrigger=false;
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_screen_unlocked));
                        log_exception.append(" ");
                    }
                }
            }catch (NumberFormatException ne){
                ne.printStackTrace();
                log_taskitem.append(ne);
                log_taskitem.append("\n");
            }

        }

        if(mWifiManager==null){
            //no wifi hardware support??
            Log.e(TAG,"WiFi Manger is null!!");
        }
        else {
            try{
                if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_WIFI_ENABLED])==1){
                    if(mWifiManager.getWifiState()== WifiManager.WIFI_STATE_ENABLED) {
                        canTrigger=false;
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_wifi_enabled));
                        log_exception.append(" ");
                    }
                }
                if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_WIFI_DISABLED])==1){
                    if(mWifiManager.getWifiState()==WifiManager.WIFI_STATE_DISABLED) {
                        canTrigger=false;
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_wifi_disabled));
                        log_exception.append(" ");
                    }
                }
            }catch (NumberFormatException ne){
                ne.printStackTrace();
                log_taskitem.append(ne.toString());
                log_taskitem.append("\n");
            }

        }

        if(mBluetoothAdapter==null){
            // no bluetooth support??
            Log.e(TAG,"Can not get BluetoothAdapter(is this device supported bluetooth?)");
        }
        else{
            try{
                if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_BLUETOOTH_ENABLED])==1){
                    if(mBluetoothAdapter.getState()== BluetoothAdapter.STATE_ON) {
                        canTrigger=false;
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_bluetooth_enabled));
                        log_exception.append(" ");
                    }
                }
                if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_BLUETOOTH_DISABLED])==1){
                    if(mBluetoothAdapter.getState()==BluetoothAdapter.STATE_OFF) {
                        canTrigger=false;
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_bluetooth_disabled));
                        log_exception.append(" ");
                    }
                }
            }catch (NumberFormatException ne){
                ne.printStackTrace();
                log_taskitem.append(ne.toString());
                log_taskitem.append("\n");
            }

        }

        if(mAudioManager==null){
            Log.e(TAG,"AudioManager is null!!");
        }else{
            try{
                if(Integer.parseInt(item.exceptions[PublicConsts.ACTION_RING_VIBRATE])==1){
                    if(mAudioManager.getRingerMode()== AudioManager.RINGER_MODE_VIBRATE) {
                        canTrigger=false;
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_ring_vibrate));
                        log_exception.append(" ");
                    }
                }

                if(Integer.parseInt(item.exceptions[PublicConsts.ACTION_RING_OFF])==1){
                    if(mAudioManager.getRingerMode()==AudioManager.RINGER_MODE_SILENT) {
                        canTrigger=false;
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_ring_off));
                        log_exception.append(" ");
                    }
                }

                if(Integer.parseInt(item.exceptions[PublicConsts.ACTION_RING_NORMAL])==1){
                    if(mAudioManager.getRingerMode()==AudioManager.RINGER_MODE_NORMAL) {
                        canTrigger=false;
                        log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_ring_normal));
                        log_exception.append(" ");
                    }
                }
            }catch (NumberFormatException ne){
                ne.printStackTrace();
                log_taskitem.append(ne.toString());
                log_taskitem.append("\n");
            }
        }

        try{
            if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_NET_ENABLED])==1){
                if(isCellarNetworkEnabled()) {
                    canTrigger=false;
                    log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_net_enabled));
                    log_exception.append(" ");
                }
            }

            if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_NET_DISABLED])==1){
                if(!isCellarNetworkEnabled()) {
                    canTrigger=false;
                    log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_net_disabled));
                    log_exception.append(" ");
                }
            }

            if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_GPS_ENABLED])==1){
                if(isLocationServiceEnabled()) {
                    canTrigger=false;
                    log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_gps_on));
                    log_exception.append(" ");
                }
            }

            if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_GPS_DISABLED])==1){
                if(!isLocationServiceEnabled()) {
                    canTrigger=false;
                    log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_gps_off));
                    log_exception.append(" ");
                }
            }

            if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_AIRPLANE_MODE_ENABLED])==1){
                if(isAirplaneModeOn()) {
                    canTrigger=false;
                    log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_airplanemode_on));
                    log_exception.append(" ");
                }
            }

            if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_AIRPLANE_MODE_DISABLED])==1){
                if(!isAirplaneModeOn()) {
                    canTrigger=false;
                    log_exception.append(context.getResources().getString(R.string.activity_taskgui_exception_airplanemode_off));
                    log_exception.append(" ");
                }
            }

            if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE])!=-1){
                // Log.e(TAG,"isInstant is "+BatteryReceiver.isInstant+" currentTemp is "+BatteryReceiver.Battery_temperature+" item set is "+item.battery_temperature);
                if(BatteryReceiver.isInstant){
                    int currentTemp=BatteryReceiver.Battery_temperature;
                    if(currentTemp>Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE])*10) {
                        canTrigger=false;
                        log_exception.append(context.getResources().getString(R.string.log_exceptions_battery_temperature_higher_than));
                        log_exception.append(item.exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]);
                        log_exception.append("℃,current:");
                        log_exception.append((double)currentTemp/10);
                        log_exception.append("℃");
                        log_exception.append(" ");
                    }
                }
            }

            if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE])!=-1){
                if(BatteryReceiver.isInstant){
                    int currentTemp=BatteryReceiver.Battery_temperature;
                    if(currentTemp<Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE])*10) {
                        canTrigger=false;
                        log_exception.append(context.getResources().getString(R.string.log_exceptions_battery_temperature_lower_than));
                        log_exception.append(item.exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]);
                        log_exception.append("℃,current:");
                        log_exception.append((double)currentTemp/10);
                        log_exception.append("℃");
                        log_exception.append(" ");
                    }
                }
            }

            if (Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE])!=-1) {
                int currentLevel=BatteryReceiver.Battery_percentage;
                if(BatteryReceiver.isInstant&&(currentLevel>Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]))) {
                    canTrigger=false;
                    log_exception.append(context.getResources().getString(R.string.log_exceptions_battery_percentage_more_than));
                    log_exception.append(item.exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]);
                    log_exception.append("%,current:");
                    log_exception.append(currentLevel);
                    log_exception.append("%");
                    log_exception.append(" ");
                }
            }

            if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE])!=-1){
                int currentLevel=BatteryReceiver.Battery_percentage;
                if(BatteryReceiver.isInstant&&(currentLevel<Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]))) {
                    canTrigger=false;
                    log_exception.append(context.getResources().getString(R.string.log_exceptions_battery_percentage_less_than));
                    log_exception.append(item.exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]);
                    log_exception.append("%,current:");
                    log_exception.append(currentLevel);
                    log_exception.append("%");
                    log_exception.append(" ");
                }
            }

            Calendar calendar=Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int dayofweek=calendar.get(Calendar.DAY_OF_WEEK);
            if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_MONDAY])==1&&dayofweek==Calendar.MONDAY){
                canTrigger=false;
                log_exception.append(context.getResources().getString(R.string.monday));
                log_exception.append(" ");
            }

            if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_TUESDAY])==1&&dayofweek==Calendar.TUESDAY){
                canTrigger=false;
                log_exception.append(context.getResources().getString(R.string.tuesday));
                log_exception.append(" ");
            }

            if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_WEDNESDAY])==1&&dayofweek==Calendar.WEDNESDAY){
                canTrigger=false;
                log_exception.append(context.getResources().getString(R.string.wednesday));
                log_exception.append(" ");
            }

            if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_THURSDAY])==1&&dayofweek==Calendar.THURSDAY){
                canTrigger=false;
                log_exception.append(context.getResources().getString(R.string.thursday));
                log_exception.append(" ");
            }

            if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_FRIDAY])==1&&dayofweek==Calendar.FRIDAY){
                canTrigger=false;
                log_exception.append(context.getResources().getString(R.string.friday));
                log_exception.append(" ");
            }

            if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_SATURDAY])==1&&dayofweek==Calendar.SATURDAY){
                canTrigger=false;
                log_exception.append(context.getResources().getString(R.string.saturday));
                log_exception.append(" ");
            }

            if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_SUNDAY])==1&&dayofweek==Calendar.SUNDAY){
                canTrigger=false;
                log_exception.append(context.getResources().getString(R.string.sunday));
                log_exception.append(" ");
            }

            if(Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_START_TIME])!=-1&&Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_END_TIME])!=-1){
                int minuteOfDay=calendar.get(Calendar.HOUR_OF_DAY)*60+calendar.get(Calendar.MINUTE);
                //Log.i("Minute of day now",""+minuteOfDay);
                //Log.i("startTime",""+item.exceptions[PublicConsts.EXCEPTION_START_TIME]);
                //Log.i("endTime",""+item.exceptions[PublicConsts.EXCEPTION_END_TIME]);
                int startTime=Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_START_TIME]);
                int endTime=Integer.parseInt(item.exceptions[PublicConsts.EXCEPTION_END_TIME]);
                if(endTime-startTime>0){
                    if((minuteOfDay>=startTime)&&minuteOfDay<=endTime) {
                        canTrigger=false;
                        log_exception.append(context.getResources().getString(R.string.log_exceptions_period));
                        log_exception.append(ValueUtils.format(startTime/60));
                        log_exception.append(":");
                        log_exception.append(ValueUtils.format(startTime%60));
                        log_exception.append("~");
                        log_exception.append(ValueUtils.format(endTime/60));
                        log_exception.append(":");
                        log_exception.append(ValueUtils.format(endTime%60));
                        log_exception.append(" ");
                    }
                }else{
                    if(minuteOfDay>=startTime||minuteOfDay<=endTime) {
                        canTrigger=false;
                        log_exception.append(context.getResources().getString(R.string.log_exceptions_period));
                        log_exception.append(ValueUtils.format(startTime/60));
                        log_exception.append(":");
                        log_exception.append(ValueUtils.format(startTime%60));
                        log_exception.append("~");
                        log_exception.append(ValueUtils.format(endTime/60));
                        log_exception.append(":");
                        log_exception.append(ValueUtils.format(endTime%60));
                        log_exception.append(" ");
                    }
                }
            }
        }catch (NumberFormatException ne){
            ne.printStackTrace();
            log_taskitem.append(ne.toString());
            log_taskitem.append("\n");
        }

        if(!log_exception.toString().equals("")&&log_exception.length()>0){
            log_taskitem.append(context.getResources().getString(R.string.log_exceptions));
            log_taskitem.append(log_exception.toString());
            log_taskitem.append(" ");
        }
        else{
            log_taskitem.append(" 已触发 ");
        }
        Log.i(TAG,"canTrigger is "+this.canTrigger);

        if(item.autoclose&&canTrigger){
            item.cancelTrigger();
            item.isenabled=false;
            try{
                ContentValues values=new ContentValues();
                values.put(SQLConsts.SQL_TASK_COLUMN_ENABLED,0);
                database.update(SQLConsts.getCurrentTableName(this.context),values,SQLConsts.SQL_TASK_COLUMN_ID +"="+item.id,null);
                Main.sendEmptyMessage(Main.MESSAGE_REQUEST_UPDATE_LIST);
            }catch (Exception e){
                e.printStackTrace();
                LogUtil.putExceptionLog(context,e);
            }
        }
        //do if delete this taskitem
        if(item.autodelete&&canTrigger){
            try{
                item.cancelTrigger();
                int rows=database.delete(SQLConsts.getCurrentTableName(this.context),SQLConsts.SQL_TASK_COLUMN_ID +"="+item.id,null);
                Log.i(TAG,"receiver deleted "+rows+" rows");
                int position=getPosition(item.id);
                if(TimeSwitchService.list!=null&&position>=0&&position<TimeSwitchService.list.size()) {
                    TimeSwitchService.list.remove(position);
                }
                Main.sendEmptyMessage(Main.MESSAGE_REQUEST_UPDATE_LIST);
            }catch (Exception e){
                e.printStackTrace();
                LogUtil.putExceptionLog(context,e);
            }
        }

        if(canTrigger){
           log_taskitem.append(" ");
           int action_wifi=-1;
           int action_bluetooth=-1;
           int action_ring_mode=-1;
           int screen_brightness=-1;
           int action_net=-1;
           int action_gps=-1;
           int action_airplanemode=-1;
           int action_device_control=-1;
           try{
               action_wifi=Integer.parseInt(item.actions[PublicConsts.ACTION_WIFI_LOCALE]);
               action_bluetooth=Integer.parseInt(item.actions[PublicConsts.ACTION_BLUETOOTH_LOCALE]);
               action_ring_mode=Integer.parseInt(item.actions[PublicConsts.ACTION_RING_MODE_LOCALE]);
               screen_brightness=Integer.parseInt(item.actions[PublicConsts.ACTION_BRIGHTNESS_LOCALE]);
               action_net=Integer.parseInt(item.actions[PublicConsts.ACTION_NET_LOCALE]);
               action_gps=Integer.parseInt(item.actions[PublicConsts.ACTION_GPS_LOCALE]);
               action_airplanemode=Integer.parseInt(item.actions[PublicConsts.ACTION_AIRPLANE_MODE_LOCALE]);
               action_device_control=Integer.parseInt(item.actions[PublicConsts.ACTION_DEVICECONTROL_LOCALE]);
           }catch (NumberFormatException ne){
               ne.printStackTrace();
               Toast.makeText(context,ne.toString(),Toast.LENGTH_SHORT).show();
               log_taskitem.append(ne.toString());
               log_taskitem.append("\n");
           }

            if(mWifiManager!=null){
                switch (action_wifi){
                    default:break;
                    case PublicConsts.ACTION_OPEN:{
                        boolean action_wifi_result=mWifiManager.setWifiEnabled(true);
                        Log.i(TAG,"Try to enable wifi...");
                        log_taskitem.append(context.getResources().getString(R.string.action_wifi_open));
                        log_taskitem.append(":");
                        log_taskitem.append(action_wifi_result ? context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                        log_taskitem.append(" ");
                    }
                    break;
                    case PublicConsts.ACTION_CLOSE:{
                        boolean action_wifi_result=mWifiManager.setWifiEnabled(false);
                        Log.i(TAG,"Try to disable wifi...");
                        log_taskitem.append(context.getResources().getString(R.string.action_wifi_close));
                        log_taskitem.append(":");
                        log_taskitem.append(action_wifi_result ? context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                        log_taskitem.append(" ");
                    }
                    break;
                }
            }

            if(mBluetoothAdapter!=null){
                switch (action_bluetooth){
                    default:break;
                    case PublicConsts.ACTION_OPEN:{
                        boolean action_bluetooth_result=mBluetoothAdapter.enable();
                        Log.i(TAG,"try to enable bluetooth...");
                        log_taskitem.append(context.getResources().getString(R.string.action_bluetooth_open));
                        log_taskitem.append(":");
                        log_taskitem.append(action_bluetooth_result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                        log_taskitem.append(" ");
                    }
                    break;
                    case PublicConsts.ACTION_CLOSE:{
                        boolean action_bluetooth_result=mBluetoothAdapter.disable();
                        Log.i(TAG,"Try to disable bluetooth...");
                        log_taskitem.append(context.getResources().getString(R.string.action_bluetooth_close));
                        log_taskitem.append(":");
                        log_taskitem.append(action_bluetooth_result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                        log_taskitem.append(" ");
                    }
                    break;
                }
            }

            if(mAudioManager!=null){
                boolean action_ring_mode_result=true;
                switch (action_ring_mode){
                    default:break;
                    case PublicConsts.ACTION_RING_VIBRATE:{
                        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);Log.i(TAG,"Try to set audio vibrate...");
                        if(mAudioManager.getRingerMode()!=AudioManager.RINGER_MODE_VIBRATE) action_ring_mode_result=false;
                        log_taskitem.append(context.getResources().getString(R.string.action_ring_mode_vibrate));
                        log_taskitem.append(":");
                        log_taskitem.append(action_ring_mode_result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                        log_taskitem.append(" ");
                        break;
                    }
                    case PublicConsts.ACTION_RING_OFF:{
                        //boolean action_ring_result=fa
                        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);Log.i(TAG,"Try to set audio silent...");
                        if(mAudioManager.getRingerMode()!=AudioManager.RINGER_MODE_SILENT)  action_ring_mode_result=false;
                        log_taskitem.append(context.getResources().getString(R.string.action_ring_mode_off));
                        log_taskitem.append(":");
                        log_taskitem.append(action_ring_mode_result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                        log_taskitem.append(" ");
                        break;
                    }
                    case PublicConsts.ACTION_RING_NORMAL:{
                        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);Log.i(TAG,"Try to set audio normal...");
                        if(mAudioManager.getRingerMode()!=AudioManager.RINGER_MODE_NORMAL) action_ring_mode_result=false;
                        log_taskitem.append(context.getResources().getString(R.string.action_ring_mode_normal));
                        log_taskitem.append(":");
                        log_taskitem.append(action_ring_mode_result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                        log_taskitem.append(" ");
                        break;
                    }
                }
                try{
                    String [] volumes=item.actions[PublicConsts.ACTION_RING_VOLUME_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                    int volume_ring=Integer.parseInt(volumes[PublicConsts.VOLUME_RING_LOCALE]);
                    int volume_media=Integer.parseInt(volumes[PublicConsts.VOLUME_MEDIA_LOCALE]);
                    int volume_notification=Integer.parseInt(volumes[PublicConsts.VOLUME_NOTIFICATION_LOCALE]);
                    int volume_alarm=Integer.parseInt(volumes[PublicConsts.VOLUME_ALARM_LOCALE]);
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
            try{
               // RingtoneManager ringtoneManager=new RingtoneManager(context);
               String ring_selection_values[]=item.actions[PublicConsts.ACTION_RING_SELECTION_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
               int ring_notification_selection= Integer.parseInt(ring_selection_values[PublicConsts.RING_SELECTION_NOTIFICATION_TYPE_LOCALE]);
               int ring_phone_selection=Integer.parseInt(ring_selection_values[PublicConsts.RING_SELECTION_CALL_TYPE_LOCALE]);
               //String ring_value_notification=ring_selection_values[PublicConsts.RING_SELECTION_NOTIFICATION_VALUE_LOCALE];
               //String ring_value_phone=ring_selection_values[PublicConsts.RING_SELECTION_PHONE_VALUE_LOCALE];
               if(ring_notification_selection==PublicConsts.RING_TYPE_SYSTEM_LOCALE ||ring_notification_selection==PublicConsts.RING_TYPE_MEDIA_LOCALE)
               {
                   //RingtoneManager.setActualDefaultRingtoneUri(context,RingtoneManager.TYPE_NOTIFICATION ,Uri.parse(ring_value_notification));
                   RingtoneManager.setActualDefaultRingtoneUri(context,RingtoneManager.TYPE_NOTIFICATION ,Uri.parse(item.uri_ring_notification));
                   log_taskitem.append(context.getResources().getString(R.string.action_set_ringtone_notification));
                   log_taskitem.append(context.getResources().getString(R.string.log_result_success));
                   log_taskitem.append(" ");
               }
               if(ring_phone_selection==PublicConsts.RING_TYPE_SYSTEM_LOCALE ||ring_phone_selection==PublicConsts.RING_TYPE_MEDIA_LOCALE)
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


            if(screen_brightness!=-1){
                if(screen_brightness==PublicConsts.ACTION_BRIGHTNESS_AUTO){
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                    log_taskitem.append(context.getResources().getString(R.string.action_brightness_auto));
                    log_taskitem.append(" ");
                }else if(screen_brightness>=0&&screen_brightness<=PublicConsts.BRIGHTNESS_MAX) {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, screen_brightness);
                    log_taskitem.append(context.getResources().getString(R.string.action_brightness_manual));
                    log_taskitem.append((int)(((double)screen_brightness/PublicConsts.BRIGHTNESS_MAX)*100));
                    log_taskitem.append(" ");
                }

            }

            try{
              // String wallpaper_values[]=item.actions[PublicConsts.ACTION_SET_WALL_PAPER_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
               if(Integer.parseInt(item.actions[PublicConsts.ACTION_SET_WALL_PAPER_LOCALE])>=0){
                   log_taskitem.append(context.getResources().getString(R.string.action_set_wallpaper));
                   WallpaperManager wallpaperManager=WallpaperManager.getInstance(context);
                   //Bitmap bitmap= MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(wallpaper_values[1]));
                   Bitmap bitmap= MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(item.uri_wallpaper_desktop));
                   wallpaperManager.setBitmap(bitmap);
                   //wallpaperManager.setBitmap(bitmap,null,WallpaperManager.FLAG_LOCK);
                   log_taskitem.append(context.getResources().getString(R.string.log_result_success));
                   log_taskitem.append(" ");
               }
            }catch (FileNotFoundException fe){
                fe.printStackTrace();
                log_taskitem.append(context.getResources().getString(R.string.action_set_wall_paper_file_not_found));
                log_taskitem.append(" ");
            }catch (Exception e){
               e.printStackTrace();
               log_taskitem.append(e.toString());
               log_taskitem.append("\n");
            }

            try{
               String vibrate_values[]=item.actions[PublicConsts.ACTION_VIBRATE_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
               long frequency=Long.parseLong(vibrate_values[PublicConsts.VIBRATE_FREQUENCY_LOCALE]);
               long duration=Long.parseLong(vibrate_values[PublicConsts.VIBRATE_DURATION_LOCALE]);
               long interval=Long.parseLong(vibrate_values[PublicConsts.VIBRATE_INTERVAL_LOCALE]);
               if(frequency>0){
                   Vibrator vibrator=(Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
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

            try{
               String toast_values[]=item.actions[PublicConsts.ACTION_TOAST_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
               int type=Integer.parseInt(toast_values[PublicConsts.TOAST_TYPE_LOCALE]);
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

            try{
               String sms_values[]=item.actions[PublicConsts.ACTION_SMS_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL);
               if(Integer.parseInt(sms_values[PublicConsts.SMS_ENABLED_LOCALE])>=0){
                   SmsManager manager;
                   int subscriptionId=Integer.parseInt(sms_values[PublicConsts.SMS_SUBINFO_LOCALE]);
                   if(Build.VERSION.SDK_INT>=22&&subscriptionId>=0){
                       manager=SmsManager.getSmsManagerForSubscriptionId(subscriptionId);
                   }else {
                       manager=SmsManager.getDefault();
                   }
                   List<String> messages=manager.divideMessage(item.sms_message);
                   String[] addresses=item.sms_address.split(PublicConsts.SEPARATOR_SMS_RECEIVERS);
                   for(String address:addresses){
                       for(String message:messages){
                           Intent i_sent=new Intent(PublicConsts.ACTION_SMS_SENT);
                           i_sent.putExtra(SMSReceiver.EXTRA_SMS_SUB_INFO,subscriptionId);
                           i_sent.putExtra(SMSReceiver.EXTRA_SMS_TASK_NAME,item.name);
                           i_sent.putExtra(SmsActivity.EXTRA_SMS_ADDRESS,address);
                           i_sent.putExtra(SmsActivity.EXTRA_SMS_MESSAGE,message);
                           Intent i_delivered=new Intent(PublicConsts.ACTION_SMS_DELIVERED);
                           i_delivered.putExtra(SMSReceiver.EXTRA_SMS_TASK_NAME,item.name);
                           i_delivered.putExtra(SMSReceiver.SMSReceiptReceiver.EXTRA_IF_SHOW_TOAST,Integer.parseInt(sms_values[PublicConsts.SMS_RESULT_TOAST_LOCALE])>=0);
                           i_delivered.putExtra(SmsActivity.EXTRA_SMS_ADDRESS,address);
                           PendingIntent pi_sent=PendingIntent.getBroadcast(context,0,i_sent,PendingIntent.FLAG_UPDATE_CURRENT);
                           PendingIntent pi_receipt=PendingIntent.getBroadcast(context,0,i_delivered,PendingIntent.FLAG_UPDATE_CURRENT);
                           try{
                               manager.sendTextMessage(address,null,message,pi_sent,pi_receipt);
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

            switchTasks(0);//has checked if has this action inside this method
           switchTasks(1);
            SharedPreferences settings=context.getSharedPreferences(PublicConsts.PREFERENCES_NAME,Activity.MODE_PRIVATE);
            boolean isRoot=settings.getBoolean(PublicConsts.PREFERENCES_IS_SUPERUSER_MODE,PublicConsts.PREFERENCES_IS_SUPERUSER_MODE_DEFAULT);

            if(isRoot){
                switch (action_net){
                    default:break;
                    case PublicConsts.ACTION_OPEN:{
                        boolean result=RootUtils.executeCommand(RootUtils.COMMAND_ENABLE_CELLUAR_NETWORK)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
                        log_taskitem.append(context.getResources().getString(R.string.action_net_on));
                        log_taskitem.append(":");
                        log_taskitem.append(result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                        log_taskitem.append(" ");
                    }
                    break;
                    case PublicConsts.ACTION_CLOSE:{
                        boolean result=RootUtils.executeCommand(RootUtils.COMMAND_DISABLE_CELLUAR_NETWORK)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
                        log_taskitem.append(context.getResources().getString(R.string.action_net_off));
                        log_taskitem.append(":");
                        log_taskitem.append(result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                        log_taskitem.append(" ");
                    }
                    break;
                }
                Log.e(TAG,"BEGIN TO OPERATE GPS!!!!!!");
                switch (action_gps){
                    default:break;
                    case PublicConsts.ACTION_OPEN:{
                        String command=RootUtils.COMMAND_ENABLE_GPS;
                        if(Build.VERSION.SDK_INT>=23) command=RootUtils.COMMAND_ENABLE_GPS_API23;
                        final String runCommand=command;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                RootUtils.executeCommand(runCommand);
                            }
                        }).start();

                        log_taskitem.append(context.getResources().getString(R.string.action_gps_on));
                        //log_taskitem.append(":");
                        //log_taskitem.append(result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                        log_taskitem.append(" ");
                       // Log.i(TAG,"Root command of enable gps 's result is "+result);
                    }
                    break;
                    case PublicConsts.ACTION_CLOSE:{
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                RootUtils.executeCommand(RootUtils.COMMAND_DISABLE_GPS);
                            }
                        }).start();
                        log_taskitem.append(context.getResources().getString(R.string.action_gps_off));
                        //log_taskitem.append(":");
                        //log_taskitem.append(result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                        log_taskitem.append(" ");
                        //Log.i(TAG,"Root command of disable gps "+result);
                    }
                    break;
                }
                switch (action_airplanemode){
                    default:break;
                    case PublicConsts.ACTION_OPEN:{
                        boolean result=RootUtils.executeCommand(RootUtils.COMMAND_ENABLE_AIRPLANE_MODE)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
                        log_taskitem.append(context.getResources().getString(R.string.action_airplane_mode_on));
                        log_taskitem.append(":");
                        log_taskitem.append(result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                        log_taskitem.append(" ");
                    }
                    break;
                    case PublicConsts.ACTION_CLOSE:{
                        boolean result=RootUtils.executeCommand(RootUtils.COMMAND_DISABLE_AIRPLANE_MODE)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
                        log_taskitem.append(context.getResources().getString(R.string.action_airplane_mode_off));
                        log_taskitem.append(":");
                        log_taskitem.append(result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                        log_taskitem.append(" ");
                    }
                    break;
                }
                switch (action_device_control){
                    default:break;
                    case PublicConsts.ACTION_DEVICECONTROL_SHUTDOWN:{
                        boolean result=RootUtils.executeCommand(RootUtils.COMMAND_SHUTDOWN)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
                        log_taskitem.append(context.getResources().getString(R.string.action_device_shutdown));
                        log_taskitem.append(":");
                        log_taskitem.append(result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                        log_taskitem.append(" ");
                    }
                    break;
                    case PublicConsts.ACTION_DEVICECONTROL_REBOOT:{
                        boolean result=RootUtils.executeCommand(RootUtils.COMMAND_REBOOT)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
                        log_taskitem.append(context.getResources().getString(R.string.action_device_reboot));
                        log_taskitem.append(":");
                        log_taskitem.append(result?context.getResources().getString(R.string.log_result_success):context.getResources().getString(R.string.log_result_fail));
                        log_taskitem.append(" ");

                    }break;
                }
            }

          /*if(item.notify){
              Message message=new Message();
              message.what=TimeSwitchService.MESSAGE_NOTIFICATION_TASK_ACTIVATED;
              String[] integer_actions=new String[item.actions.length];
              for(int i=0;i<item.actions.length;i++){
                  integer_actions[i]=item.actions[i];
              }
              message.obj=integer_actions;
              TimeSwitchService.sendMessage(message);
              //Log.e(TAG,"Notification Message SENT!!!!");
          }  */

            try{
                String notification_values[]=item.actions[PublicConsts.ACTION_NOTIFICATION_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                int type=Integer.parseInt(notification_values[PublicConsts.NOTIFICATION_TYPE_LOCALE]);
                if(type==PublicConsts.NOTIFICATION_TYPE_VIBRATE||type==PublicConsts.NOTIFICATION_TYPE_NO_VIBRATE){
                    NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationCompat.Builder builder;
                    if(Build.VERSION.SDK_INT>=26){
                        String channel_id="channel_default";
                        NotificationChannel channel=new NotificationChannel(channel_id,"Default", NotificationManager.IMPORTANCE_DEFAULT);
                        manager.createNotificationChannel(channel);
                        builder=new NotificationCompat.Builder(context,channel_id);
                    }else{
                        builder=new NotificationCompat.Builder(context);
                    }
                    builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    builder.setSmallIcon(R.drawable.ic_launcher);
                    builder.setContentTitle(context.getResources().getString(R.string.notification_task_activated_title));
                    builder.setContentText(log_taskitem.toString());
                    if(Integer.parseInt(notification_values[PublicConsts.NOTIFICATION_TYPE_IF_CUSTOM_LOCALE])==PublicConsts.NOTIFICATION_TYPE_CUSTOM){
                        builder.setContentTitle(item.notification_title);
                        builder.setContentText(item.notification_message);
                    }
                    PendingIntent pi =PendingIntent.getActivity(context,1,new Intent(),PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(pi);
                    builder.setAutoCancel(true);
                    builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                    builder.setFullScreenIntent(pi,false);
                    manager.notify(0,builder.build());
                    log_taskitem.append(context.getResources().getString(R.string.activity_taskgui_actions_notification));
                    log_taskitem.append(" ");
                }
            }catch (Exception e){
                e.printStackTrace();
                log_taskitem.append(e.toString());
                log_taskitem.append("\n");
            }

        }
        //Log.i(TAG,"log is "+log_taskitem);
        //SharedPreferences log=context.getSharedPreferences(PublicConsts.PREFERENCES_LOGS_NAME,Activity.MODE_PRIVATE);
        //SharedPreferences.Editor editor=log.edit();
       // editor.putString(Long.valueOf(System.currentTimeMillis()).toString(),log_taskitem.toString());
       // editor.apply();
        LogUtil.putLog(context,log_taskitem.toString());
        com.github.ghmxr.timeswitch.activities.Log.sendEmptyMessage(com.github.ghmxr.timeswitch.activities.Log.MESSAGE_REQUEST_REFRESH);
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


    /**
     * 通过TaskItem中的ID获取该TaskItem在list中的位置；
     * 如果查询不到该ID，则返回-1
     * @param id TaskItem中的id
     * @return TaskItem所在list的position
     */
    public static int getPosition(int id){
        if(TimeSwitchService.list==null) return -1;
        for(int i=0;i< TimeSwitchService.list.size();i++){
            if(id==TimeSwitchService.list.get(i).id) return i;
        }
        return -1;
    }

    /**
     * 启用或者关闭指定任务
     * @param enableOrDisable 0--enable,1--disable
     */
    private void switchTasks(int enableOrDisable){
        SQLiteDatabase database=MySQLiteOpenHelper.getInstance(context).getWritableDatabase();
        try{
            String[] switch_values=item.actions[enableOrDisable==0?PublicConsts.ACTION_ENABLE_TASKS_LOCALE:PublicConsts.ACTION_DISABLE_TASKS_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL);
            if(Integer.parseInt(switch_values[0])>=0){
                for(String s:switch_values){
                    int id=Integer.parseInt(s);
                    Cursor cursor=database.rawQuery("select * from "+ SQLConsts.getCurrentTableName(context)+" where "+SQLConsts.SQL_TASK_COLUMN_ID+"="+id,null);
                    if(cursor.getCount()>0){
                        //int position=getPosition(id);
                        setTaskEnabled(context,id,enableOrDisable==0);
                    }
                    cursor.close();
                }
                Main.sendEmptyMessage(Main.MESSAGE_REQUEST_UPDATE_LIST);
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
            if(TimeSwitchService.list.get(position).trigger_type==PublicConsts.TRIGGER_TYPE_SINGLE&&TimeSwitchService.list.get(position).time<=System.currentTimeMillis()) return;
            TimeSwitchService.list.get(position).isenabled=enabled;
            SQLiteDatabase database=MySQLiteOpenHelper.getInstance(context).getWritableDatabase();
            database.execSQL("update "+SQLConsts.getCurrentTableName(context)
                    +" set "+SQLConsts.SQL_TASK_COLUMN_ENABLED +"="+(enabled?1:0)+
                    " where "+SQLConsts.SQL_TASK_COLUMN_ID +"="+id);

            if(TimeSwitchService.list.get(position).trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME&&enabled){
                TimeSwitchService.list.get(position).time=System.currentTimeMillis();
                Cursor cursor=database.rawQuery("select * from "+ SQLConsts.getCurrentTableName(context)+" where "+SQLConsts.SQL_TASK_COLUMN_ID+"="+id,null);
                if(cursor.moveToFirst()){
                    long[] values_read=ValueUtils.string2longArray(cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES)));
                    if(values_read.length==2){
                        long interval_read=values_read[1];
                        long values_put[]=new long[2];
                        values_put[0]=System.currentTimeMillis();
                        values_put[1]=interval_read;
                        ContentValues contentValues=new ContentValues();
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES,ValueUtils.longArray2String(values_put));
                        database.update(SQLConsts.getCurrentTableName(context),contentValues,SQLConsts.SQL_TASK_COLUMN_ID+"="+id,null);
                    }
                }
                cursor.close();
            }
            if(enabled) TimeSwitchService.list.get(position).activateTrigger(context); else TimeSwitchService.list.get(position).cancelTrigger();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
