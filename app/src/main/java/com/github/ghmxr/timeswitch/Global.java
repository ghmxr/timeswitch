package com.github.ghmxr.timeswitch;

import android.content.BroadcastReceiver;
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
import android.util.Log;

import com.github.ghmxr.timeswitch.activities.Triggers;
import com.github.ghmxr.timeswitch.adapters.MainListAdapter;
import com.github.ghmxr.timeswitch.data.v2.AdditionConsts;
import com.github.ghmxr.timeswitch.data.v2.SQLConsts;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.data.v2.MySQLiteOpenHelper;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.util.ArrayList;
import java.util.Calendar;

import static com.github.ghmxr.timeswitch.activities.Triggers.getWeekLoopDisplayValue;

public class Global {
    public static Handler handler=new Handler(Looper.getMainLooper());

    /**
     * 通过SQLite数据库指定的表获取TaskItem列表信息（不激活TaskItem），此方法为耗时操作，可能会阻塞线程
     * @param context 传入context
     * @return  对应的TaskItemList
     */
    public static @NonNull ArrayList<TaskItem> getTaskItemListFromDatabase(Context context){
        try{
            SQLiteDatabase database = MySQLiteOpenHelper.getInstance(context).getWritableDatabase();
            Cursor cursor=database.rawQuery("select * from "+ MySQLiteOpenHelper.getCurrentTableName(context),null);
            ArrayList<TaskItem> list=new ArrayList<>();
            while (cursor.moveToNext()){
                try{
                    TaskItem item=new TaskItem();

                    item.id = cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ID));
                    item.name = cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_NAME));
                    item.isenabled=(cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ENABLED))==1);
                    item.trigger_type = cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_TYPE));

                    String[] trigger_values= ValueUtils.string2StringArray(cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES)));
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
                    if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_APP_LAUNCHED||item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_APP_CLOSED){
                        item.package_names=trigger_values;
                    }

                    String [] read_exceptions=ValueUtils.string2StringArray(cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_EXCEPTIONS)));
                    System.arraycopy(read_exceptions,0,item.exceptions,0,read_exceptions.length);

                    String [] read_actions=ValueUtils.string2StringArray(cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ACTIONS)));
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

                    String read_additions[] =ValueUtils.string2StringArray(cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ADDITIONS)));
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
                    switch (item.trigger_type){
                        default:break;
                        case TriggerTypeConsts.TRIGGER_TYPE_SINGLE: {
                            item.display_trigger_icon_res=R.drawable.icon_repeat_single;
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(item.time);
                            int month = calendar.get(Calendar.MONTH) + 1;
                            item.display_trigger=calendar.get(Calendar.YEAR)
                                    + "/" + ValueUtils.format(month) + "/" + ValueUtils.format(calendar.get(Calendar.DAY_OF_MONTH)) + "/" + ValueUtils.getDayOfWeek(item.time) + "/"
                                    + ValueUtils.format(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + ValueUtils.format(calendar.get(Calendar.MINUTE));
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME: {
                            item.display_trigger_icon_res=R.drawable.icon_repeat_percertaintime;
                            //refreshAllCertainTimeTaskItems();
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK: {
                            item.display_trigger_icon_res=R.drawable.icon_repeat_weekloop;
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(item.time);
                            item.display_trigger=getWeekLoopDisplayValue(context, item.week_repeat, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                            //holder.trigger_value.setText();
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE: {
                            item.display_trigger_icon_res=R.drawable.icon_battery_high;
                            item.display_trigger=context.getResources().getString(R.string.more_than) + item.battery_percentage + "%";
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE: {
                            item.display_trigger_icon_res=R.drawable.icon_battery_low;
                            item.display_trigger=context.getResources().getString(R.string.less_than) + item.battery_percentage + "%";
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE: {
                            item.display_trigger_icon_res=R.drawable.icon_temperature;
                            item.display_trigger=context.getResources().getString(R.string.higher_than) + item.battery_temperature + "℃";
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE: {
                            item.display_trigger_icon_res=R.drawable.icon_temperature;
                            item.display_trigger=context.getResources().getString(R.string.lower_than) + item.battery_temperature + "℃";
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_RECEIVED_BROADCAST: {
                            item.display_trigger_icon_res=R.drawable.icon_broadcast;
                            item.display_trigger=item.selectedAction;
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_WIFI_CONNECTED: {
                            item.display_trigger_icon_res=R.drawable.icon_wifi_connected;
                            //holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                            String ssidinfo = Triggers.getWifiConnectionDisplayValue(context, item.wifiIds);
                            //if(ssidinfo.length()>16) ssidinfo=ssidinfo.substring(0,16)+"...";
                            item.display_trigger=ssidinfo;
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_WIFI_DISCONNECTED: {
                            item.display_trigger_icon_res=R.drawable.icon_wifi_disconnected;
                            //holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                            String ssidinfo = Triggers.getWifiConnectionDisplayValue(context, item.wifiIds);
                            //if(ssidinfo.length()>16) ssidinfo=ssidinfo.substring(0,16)+"...";
                            item.display_trigger=ssidinfo;
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_APP_LAUNCHED: {
                            item.display_trigger_icon_res=R.drawable.icon_app_launch;
                            String names = Triggers.getAppNameDisplayValue(context, item.package_names);
                            //if(names.length()>16) names=names.substring(0,16);
                            item.display_trigger=names;
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_APP_CLOSED: {
                            item.display_trigger_icon_res=R.drawable.icon_app_stop;
                            String names = Triggers.getAppNameDisplayValue(context, item.package_names);
                            //if(names.length()>16) names=names.substring(0,16);
                            item.display_trigger=names;
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_SCREEN_ON: {
                            item.display_trigger_icon_res=R.drawable.icon_screen_on;
                            item.display_trigger=context.getResources().getString(R.string.activity_triggers_screen_on);
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_SCREEN_OFF: {
                            item.display_trigger_icon_res=R.drawable.icon_screen_off;
                            item.display_trigger=context.getResources().getString(R.string.activity_triggers_screen_off);
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_POWER_CONNECTED: {
                            item.display_trigger_icon_res=R.drawable.icon_power_connected;
                            item.display_trigger=context.getResources().getString(R.string.activity_triggers_power_connected);
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_POWER_DISCONNECTED: {
                            item.display_trigger_icon_res=R.drawable.icon_power_disconnected;
                            item.display_trigger=context.getResources().getString(R.string.activity_triggers_power_disconnected);
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_ON: {
                            item.display_trigger_icon_res=R.drawable.icon_wifi_on;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF: {
                            item.display_trigger_icon_res=R.drawable.icon_wifi_off;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON: {
                            item.display_trigger_icon_res=R.drawable.icon_bluetooth_on;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF: {
                            item.display_trigger_icon_res=R.drawable.icon_bluetooth_off;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF: {
                            item.display_trigger_icon_res=R.drawable.icon_ring_off;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE: {
                            item.display_trigger_icon_res=R.drawable.icon_ring_vibrate;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL: {
                            item.display_trigger_icon_res=R.drawable.icon_ring_normal;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON: {
                            item.display_trigger_icon_res=R.drawable.icon_airplanemode_on;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF: {
                            item.display_trigger_icon_res=R.drawable.icon_airplanemode_off;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED: {
                            item.display_trigger_icon_res=R.drawable.icon_ap_on;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED: {
                            item.display_trigger_icon_res=R.drawable.icon_ap_off;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_NET_ON: {
                            item.display_trigger_icon_res=R.drawable.icon_cellular_on;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_NET_OFF: {
                            item.display_trigger_icon_res=R.drawable.icon_cellular_off;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_IN: {
                            item.display_trigger_icon_res=R.drawable.icon_headset;
                            item.display_trigger=context.getResources().getString(R.string.activity_trigger_headset_plug_in);
                        }
                        break;
                        case TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT: {
                            item.display_trigger_icon_res=R.drawable.icon_headset;
                            item.display_trigger=context.getResources().getString(R.string.activity_trigger_headset_plug_out);
                        }
                        break;

                    }

                    item.display_exception= MainListAdapter.getExceptionValue(context,item);
                    item.display_actions=MainListAdapter.getActionValue(context,item);
                    item.display_additions=MainListAdapter.getAdditionValue(context,item);

                    list.add(item);
                }catch (Exception e){e.printStackTrace();}
            }
            cursor.close();
            database.close();
            return list;
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
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

    public static class NetworkReceiver extends BroadcastReceiver{

        public static final ArrayList<WifiConfigInfo> wifiList=new ArrayList<>();
        public static WifiInfo connectedWifiInfo;

        @Override
        public void onReceive(final Context context, Intent intent) {
            if(intent==null||intent.getAction()==null) return;
            if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)&&intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1)==WifiManager.WIFI_STATE_ENABLED){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (wifiList){
                            try{
                                wifiList.clear();
                                final WifiManager wifiManager=(WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                for(WifiConfiguration w:wifiManager.getConfiguredNetworks()){
                                    WifiConfigInfo wifi_info = new WifiConfigInfo();
                                    wifi_info.networkID=w.networkId;
                                    wifi_info.SSID= ValueUtils.toDisplaySSIDString(w.SSID);
                                    wifiList.add(wifi_info);
                                }
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

        public static class WifiConfigInfo{
            public int networkID=0;
            public String SSID="";
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
