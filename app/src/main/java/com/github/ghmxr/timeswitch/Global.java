package com.github.ghmxr.timeswitch;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.ghmxr.timeswitch.adapters.ContentAdapter;
import com.github.ghmxr.timeswitch.data.v2.AdditionConsts;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.data.v2.SQLConsts;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.data.v2.MySQLiteOpenHelper;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Global {
    public static Handler handler=new Handler(Looper.getMainLooper());

    /**
     * 通过SQLite数据库指定的表获取TaskItem列表信息（不激活TaskItem），此方法为耗时操作，可能会阻塞线程
     * @param context 传入context
     * @param table_name 指定读取的表名称，传入null则自动读取当前已设定的表名
     * @return  对应的TaskItemList
     */
    public static @NonNull ArrayList<TaskItem> getTaskItemListFromDatabase(Context context, @Nullable String table_name){
        try{
            if(table_name==null) table_name=MySQLiteOpenHelper.getCurrentTableName(context);
            SQLiteDatabase database = MySQLiteOpenHelper.getInstance(context).getWritableDatabase();
            Cursor cursor=database.rawQuery("select * from "+ table_name,null);
            ArrayList<TaskItem> list=new ArrayList<>();
            while (cursor.moveToNext()){
                try{
                    TaskItem item=new TaskItem();

                    item.id = cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ID));
                    item.order=cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ORDER));
                    item.name = cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_NAME));
                    item.isenabled=(cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ENABLED))==1);
                    item.trigger_type = cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_TYPE));

                    String[] trigger_values= ValueUtils.string2StringArray(PublicConsts.SPLIT_SEPARATOR_FIRST_LEVEL,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES)));
                    if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_SINGLE){   //0（仅一次）--{trigger_value};
                        item.time=Long.parseLong(trigger_values[0]);
                    }
                    if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME){ //1（指定时间长度重复） --{trigger_value,period_type,value};
                        item.time=Long.parseLong(trigger_values[0]);
                        item.interval_milliseconds=Long.parseLong(trigger_values[1]);
                    }
                    if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK){//2(周重复) --{trigger_value,SUNDAY,MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY};
                        item.time=Long.parseLong(trigger_values[0]);
                        for(int i=0;i<item.week_repeat.length;i++){
                            item.week_repeat[i]=(Long.parseLong(trigger_values[i+1])==1);
                        }
                    }

                    if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE||item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE){   //3||4 (电池电量低于或高于某值)  --{percent};
                        item.battery_percentage = Integer.parseInt(trigger_values[0]);
                    }
                    if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE||item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE){
                        item.battery_temperature=Integer.parseInt(trigger_values[0]);
                    }
                    if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_RECEIVED_BROADCAST){
                        item.selectedAction=String.valueOf(trigger_values[0]);
                    }

                    if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_WIFI_CONNECTED||item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_WIFI_DISCONNECTED){
                        if(trigger_values==null||trigger_values.length<1){
                            item.wifiIds="";
                        }else item.wifiIds=String.valueOf(trigger_values[0]);
                    }
                    if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_APP_LAUNCHED||item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_APP_CLOSED||item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_RECEIVED_NOTIFICATION){
                        item.package_names=trigger_values;
                    }
                    if(item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_LIGHT_SENSOR_HIGHER_THAN||item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_LIGHT_SENSOR_LOWER_THAN){
                        item.light_brightness=Integer.parseInt(trigger_values[0]);
                    }

                    String [] read_exceptions=ValueUtils.string2StringArray(PublicConsts.SPLIT_SEPARATOR_FIRST_LEVEL,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_EXCEPTIONS)));
                    System.arraycopy(read_exceptions,0,item.exceptions,0,read_exceptions.length);

                    String [] read_actions=ValueUtils.string2StringArray(PublicConsts.SPLIT_SEPARATOR_FIRST_LEVEL,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ACTIONS)));
                    System.arraycopy(read_actions,0,item.actions,0,read_actions.length);

                    item.uri_ring_notification=cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_URI_RING_NOTIFICATION));
                    item.uri_ring_call =cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_URI_RING_CALL));
                    item.uri_wallpaper_desktop=cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_URI_WALLPAPER_DESKTOP));
                    item.notification_title=cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_TITLE));
                    item.notification_message=cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_MESSAGE));
                    item.toast=cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_TOAST));
                    item.sms_address=cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_PHONE_NUMBERS));
                    item.sms_message=cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_MESSAGE));

                    //additions
                    String initial_additions[]=new String[AdditionConsts.ADDITION_LENGTH];
                    for(int i=0;i<initial_additions.length;i++) initial_additions[i]=String.valueOf(-1);

                    String read_additions[] =ValueUtils.string2StringArray(PublicConsts.SPLIT_SEPARATOR_FIRST_LEVEL,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ADDITIONS)));
                    System.arraycopy(read_additions,0,initial_additions,0,read_additions.length);

                    item.notify=(Integer.parseInt(initial_additions[AdditionConsts.ADDITION_NOTIFY])==1);
                    item.autodelete=(Integer.parseInt(initial_additions[AdditionConsts.ADDITION_AUTO_DELETE])==1);
                    item.autoclose=(Integer.parseInt(initial_additions[AdditionConsts.ADDITION_AUTO_CLOSE])==1);
                    item.addition_isFolded=(Integer.parseInt(initial_additions[AdditionConsts.ADDITION_TITLE_FOLDED_VALUE_LOCALE])>=0);
                    if(!initial_additions[AdditionConsts.ADDITION_TITLE_COLOR_LOCALE].equals(String.valueOf(-1))){
                        item.addition_title_color=initial_additions[AdditionConsts.ADDITION_TITLE_COLOR_LOCALE];
                    }
                    item.addition_exception_connector=initial_additions[AdditionConsts.ADDITION_EXCEPTION_CONNECTOR_LOCALE];

                    //initial display values
                    try{
                        item.display_trigger_icon_res=(Integer) ContentAdapter.TriggerContentAdapter.getContentForTriggerType(context,ContentAdapter.TriggerContentAdapter.CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID,item);
                        item.display_trigger=(String)ContentAdapter.TriggerContentAdapter.getContentForTriggerType(context,ContentAdapter.TriggerContentAdapter.CONTENT_TYPE_DISPLAY_STRING_CONTENT,item);
                    }catch (Exception e){e.printStackTrace();}


                    item.display_exception= ContentAdapter.ExceptionContentAdapter.getExceptionValue(context,item);
                    item.display_actions= ContentAdapter.ActionContentAdapter.ActionDisplayValuesOfMainPage.getActionValue(context,item);
                    item.display_additions= ContentAdapter.getAdditionDisplayValue(context,item);

                    list.add(item);
                }catch (Exception e){e.printStackTrace();}
            }
            cursor.close();
            database.close();
            Collections.sort(list);
            return list;
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 根据list中各item的位置刷新item中的order并更新至数据库，由TimeSwitchService.class同步
     * @param list 已排好序的list
     */
    public static void refreshTaskItemListOrders(final Context context,final List<TaskItem> list){
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (TimeSwitchService.class){
                    try{
                        if(list==null) return;
                        for(int i=0;i<list.size();i++){
                            list.get(i).order=i;
                        }
                        SQLiteDatabase database=MySQLiteOpenHelper.getInstance(context).getWritableDatabase();
                        final String table_name=MySQLiteOpenHelper.getCurrentTableName(context);
                        for(TaskItem item:list){
                            ContentValues contentValues=new ContentValues();
                            contentValues.put(SQLConsts.SQL_TASK_COLUMN_ORDER,item.order);
                            database.update(table_name,contentValues,SQLConsts.SQL_TASK_COLUMN_ID+"="+item.id,null);
                        }
                        database.close();
                    }catch (Exception e){e.printStackTrace();}
                }
            }
        }).start();
    }

    public static class BatteryReceiver extends BroadcastReceiver{
        /**
         * 百分比（0~100）
         */
        public static int battery_percentage =-1;
        /**
         * 单位 毫伏（mV）
         */
        public static int battery_voltage =-1;
        /**
         * 单位 0.1摄氏度
         */
        public static int battery_temperature =-100;

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent==null||intent.getAction()==null||!intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) return;
            battery_percentage =intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
            battery_voltage =intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,-1);
            battery_temperature =intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-100);
        }
    }

    /**
     * this receiver is registered by TimeSwitchService
     */
    public static class NetworkReceiver extends BroadcastReceiver{

        public static final ArrayList<WifiConfiguration> wifiList2=new ArrayList<>();
        public static WifiInfo connectedWifiInfo;

        @Override
        public void onReceive(final Context context, Intent intent) {
            if(intent==null||intent.getAction()==null) return;
            if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)&&intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1)==WifiManager.WIFI_STATE_ENABLED){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (wifiList2){
                            try{
                                final WifiManager wifiManager=(WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                List<WifiConfiguration> temp=wifiManager.getConfiguredNetworks();
                                wifiList2.clear();
                                wifiList2.addAll(temp);
                            }catch (Exception e){e.printStackTrace();}
                        }
                    }
                }).start();
            }

            if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                NetworkInfo info=intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                WifiInfo wifiInfo=null;
                WifiManager wifiManager=(WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if(wifiManager!=null) wifiInfo=wifiManager.getConnectionInfo();

                //initialize the connected wifiInfo;
                if(info.getDetailedState().equals(NetworkInfo.DetailedState.CONNECTED)
                        &&info.getType()== ConnectivityManager.TYPE_WIFI
                        &&info.isConnected()
                        &&wifiInfo!=null
                        &&wifiInfo.getNetworkId()>=0){
                    connectedWifiInfo=wifiInfo;
                }
            }
        }
    }

    public static class HeadsetPlugReceiver extends BroadcastReceiver{
        public static boolean isHeadsetPlugIn =false;

        public static boolean isHeadsetPluggedIn(){
            return isHeadsetPlugIn;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent==null||intent.getAction()==null||!intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) return;
            if(intent.getIntExtra("state",-1)==0) {
                isHeadsetPlugIn =false;
                Log.i("HeadSet","HeadSet is unplugged");
            }
            if(intent.getIntExtra("state",-1)==1) {
                isHeadsetPlugIn =true;
                Log.i("HeadSet","HeadSet is plugged");
            }
        }
    }

}
