package com.github.ghmxr.timeswitch.adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import com.github.ghmxr.timeswitch.Global;
import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.ActionConsts;
import com.github.ghmxr.timeswitch.data.v2.ExceptionConsts;
import com.github.ghmxr.timeswitch.data.v2.MySQLiteOpenHelper;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.data.v2.SQLConsts;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.io.File;
import java.util.Calendar;

public class ContentAdapter {
    public static String getAdditionDisplayValue(Context context, TaskItem item){
        try{
            if(item.autodelete) return context.getResources().getString(R.string.activity_taskgui_additional_autodelete_cb);
            if(item.autoclose) return context.getResources().getString(R.string.activity_taskgui_additional_autoclose_cb);
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public static class TriggerContentAdapter{
        /**
         * 传入此参数来获取触发器图标R值(Integer)
         */
        public static final int CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID =0;
        /**
         * 传入此参数来获取触发器标题(String)
         */
        public static final int CONTENT_TYPE_DISPLAY_STRING_TITLE =1;
        /**
         * 传入此参数来获取触发器内容(String)
         */
        public static final int CONTENT_TYPE_DISPLAY_STRING_CONTENT=2;
        /**
         * 根据type返回一个Resource，type值参考TriggerTypeConsts
         * @param context 传入context来获取系统resource资源
         * @param content_type 要获取的内容类型，CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID, CONTENT_TYPE_DISPLAY_STRING_TITLE, CONTENT_TYPE_DISPLAY_STRING_CONTENT中的一个值
         * @param item 传入一个TaskItem，本方法会通过里边的参数返回相应的值，不能为空，否则会向上抛空指针
         */
        public static @Nullable Object getContentForTriggerType(Context context, int content_type, @NonNull TaskItem item){
            switch(item.trigger_type){
                default:break;
                case TriggerTypeConsts.TRIGGER_TYPE_SINGLE: {
                    if(content_type== CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_repeat_single;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE) return context.getResources().getString(R.string.activity_taskgui_condition_single_att);
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return TriggerDisplayStrings.getSingleTimeDisplayValue(context,item.time);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_repeat_percertaintime;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE) return context.getResources().getString(R.string.activity_taskgui_condition_percertaintime_att);
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return TriggerDisplayStrings.getCertainLoopTimeDisplayValue(context,item.interval_milliseconds);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK: {
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_repeat_weekloop;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE) return context.getResources().getString(R.string.activity_taskgui_condition_weekloop_att);
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return TriggerDisplayStrings.getWeekLoopDisplayValue2(context,item.week_repeat,item.time);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE: {
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_temperature;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE) return context.getResources().getString(R.string.activity_taskgui_condition_battery_temperature_att);
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return TriggerDisplayStrings.getBatteryTemperatureDisplayValue(context,item.trigger_type,item.battery_temperature);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE: {
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_temperature;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE) return context.getResources().getString(R.string.activity_taskgui_condition_battery_temperature_att);
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return TriggerContentAdapter.TriggerDisplayStrings.getBatteryTemperatureDisplayValue(context,item.trigger_type,item.battery_temperature);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE: {
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_battery_high;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE) return context.getResources().getString(R.string.activity_taskgui_condition_battery_percentage_att);
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return TriggerContentAdapter.TriggerDisplayStrings.getBatteryPercentageDisplayValue(context,item.trigger_type,item.battery_percentage);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE: {
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_battery_low;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE) return context.getResources().getString(R.string.activity_taskgui_condition_battery_percentage_att);
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return TriggerDisplayStrings.getBatteryPercentageDisplayValue(context,item.trigger_type,item.battery_percentage);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_RECEIVED_BROADCAST: {
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_broadcast;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE) return context.getResources().getString(R.string.activity_taskgui_condition_received_broadcast_att);
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return item.selectedAction;
                }

                case TriggerTypeConsts.TRIGGER_TYPE_WIFI_CONNECTED: {
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_wifi_connected;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE) return context.getResources().getString(R.string.activity_taskgui_condition_wifi_connected);
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return TriggerDisplayStrings.getWifiConnectionDisplayValue(context,item.wifiIds);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_WIFI_DISCONNECTED:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID)return R.drawable.icon_wifi_disconnected;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE) return context.getResources().getString(R.string.activity_taskgui_condition_wifi_disconnected);
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return TriggerDisplayStrings.getWifiConnectionDisplayValue(context,item.wifiIds);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_SCREEN_ON: {
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_screen_on;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE||content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return context.getResources().getString(R.string.activity_triggers_screen_on);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_SCREEN_OFF: {
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_screen_off;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE||content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return context.getResources().getString(R.string.activity_triggers_screen_off);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_POWER_CONNECTED: {
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_power_connected;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE||content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return context.getResources().getString(R.string.activity_triggers_power_connected);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_POWER_DISCONNECTED: {
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID)return R.drawable.icon_power_disconnected;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE||content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return context.getResources().getString(R.string.activity_triggers_power_disconnected);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_ON: {
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_wifi_on;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE||content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return context.getResources().getString(R.string.dialog_triggers_widget_wifi_on);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_wifi_off;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE||content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return context.getResources().getString(R.string.dialog_triggers_widget_wifi_off);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID)return R.drawable.icon_bluetooth_on;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE||content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return context.getResources().getString(R.string.dialog_triggers_widget_bluetooth_on);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID)return R.drawable.icon_bluetooth_off;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE||content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return context.getResources().getString(R.string.dialog_triggers_widget_bluetooth_off);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID)return R.drawable.icon_ring_off;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE||content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return context.getResources().getString(R.string.dialog_triggers_widget_ring_mode_off);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID)return R.drawable.icon_ring_vibrate;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE||content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return context.getResources().getString(R.string.dialog_triggers_widget_ring_mode_vibrate);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_ring_normal;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE||content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return context.getResources().getString(R.string.dialog_triggers_widget_ring_mode_normal);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_airplanemode_off;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE||content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return context.getResources().getString(R.string.dialog_triggers_widget_airplane_mode_off);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID)return R.drawable.icon_airplanemode_on;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE||content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return context.getResources().getString(R.string.dialog_triggers_widget_airplane_mode_on);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID)return R.drawable.icon_ap_on;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE||content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return context.getResources().getString(R.string.dialog_triggers_widget_ap_on);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_ap_off;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE||content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return context.getResources().getString(R.string.dialog_triggers_widget_ap_off);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_NET_ON:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID)return R.drawable.icon_cellular_on;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE||content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return context.getResources().getString(R.string.dialog_triggers_widget_net_on);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_NET_OFF:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID)return R.drawable.icon_cellular_off;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE||content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return context.getResources().getString(R.string.dialog_triggers_widget_net_off);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_APP_LAUNCHED:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_app_launch;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE) return context.getResources().getString(R.string.activity_trigger_app_opened);
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return TriggerDisplayStrings.getAppNameDisplayValue(context,item.package_names);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_APP_CLOSED:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_app_stop;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE) return context.getResources().getString(R.string.activity_trigger_app_closed);
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return TriggerDisplayStrings.getAppNameDisplayValue(context,item.package_names);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_IN:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID)return R.drawable.icon_headset;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE) return context.getResources().getString(R.string.activity_trigger_headset);
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return context.getResources().getString(R.string.activity_trigger_headset_plug_in);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID)return R.drawable.icon_headset;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE) return context.getResources().getString(R.string.activity_trigger_headset);
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return context.getResources().getString(R.string.activity_trigger_headset_plug_out);
                }

                case TriggerTypeConsts.TRIGGER_TYPE_LIGHT_SENSOR_HIGHER_THAN: case TriggerTypeConsts.TRIGGER_TYPE_LIGHT_SENSOR_LOWER_THAN:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID) return R.drawable.icon_brightness;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE) return context.getResources().getString(R.string.activity_trigger_brightness);
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return TriggerDisplayStrings.getBrightnessTriggerDisplayValue(context,item.trigger_type,item.light_brightness);
                }
                break;
                case TriggerTypeConsts.TRIGGER_TYPE_RECEIVED_NOTIFICATION:{
                    if(content_type==CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID)return R.drawable.icon_notification;
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_TITLE) return context.getResources().getString(R.string.activity_trigger_received_notification);
                    if(content_type==CONTENT_TYPE_DISPLAY_STRING_CONTENT) return TriggerDisplayStrings.getAppNameDisplayValue(context,item.package_names);
                }
                break;
            }
            return null;
        }

        public static class TriggerDisplayStrings{

            public static String getSingleTimeDisplayValue(@NonNull Context context, long millis){
               // TextView tv_condition_single_value=findViewById(R.id.trigger_single_value);
                Calendar calendar=Calendar.getInstance();
                calendar.setTimeInMillis(millis);
                int month=calendar.get(Calendar.MONTH)+1;
                return context.getResources().getString(R.string.activity_taskgui_condition_single_value)+ ValueUtils.format(calendar.get(Calendar.YEAR))+"/"+ ValueUtils.format(month)+"/"+ ValueUtils.format(calendar.get(Calendar.DAY_OF_MONTH))+"("+ValueUtils.getDayOfWeek(calendar.getTimeInMillis())+")/"+ ValueUtils.format(calendar.get(Calendar.HOUR_OF_DAY))+":"+ ValueUtils.format(calendar.get(Calendar.MINUTE));
            }

            public static String getCertainLoopTimeDisplayValue(Context context, long loopmillis){
                //TextView tv_condition_percertaintime_value=findViewById(R.id.trigger_percertaintime_value);
                return context.getResources().getString(R.string.adapter_per)+ ValueUtils.getFormatTime(context,loopmillis)+context.getResources().getString(R.string.adapter_trigger);
            }

            /**
             * @deprecated
             */
            public static String getWeekLoopDisplayValue(Context context, boolean week_repeat[], int hourOfDay, int minute){
                if(context==null||week_repeat==null||week_repeat.length!=7) return "";
                String tv_value="";
                //TextView tv_condition_weekloop_value=findViewById(R.id.layout_taskgui_area_condition_weekloop_value);
                if(week_repeat[1]) tv_value+=context.getResources().getString(R.string.monday)+" ";//if(this.weekloop[1]) tv_value+="周一 ";
                if(week_repeat[2]) tv_value+=context.getResources().getString(R.string.tuesday)+" ";
                if(week_repeat[3]) tv_value+=context.getResources().getString(R.string.wednesday)+" ";
                if(week_repeat[4]) tv_value+=context.getResources().getString(R.string.thursday)+" ";
                if(week_repeat[5]) tv_value+=context.getResources().getString(R.string.friday)+" ";
                if(week_repeat[6]) tv_value+=context.getResources().getString(R.string.saturday)+" ";
                if(week_repeat[0]) tv_value+=context.getResources().getString(R.string.sunday);

                boolean everyday=true;
                for(int i=0;i<7;i++){
                    if(!week_repeat[i]) {  //if(!this.weekloop[i]) {
                        everyday=false;
                        break;
                    }
                }

                String time=ValueUtils.format(hourOfDay)+":"+ValueUtils.format(minute);
                if(everyday) return context.getResources().getString(R.string.everyday)+" "+time;
                return  tv_value+" "+time;
            }

            public static String getWeekLoopDisplayValue2(Context context,boolean week_repeat[],long time){
                if(context==null||week_repeat==null||week_repeat.length!=7) return "";
                String tv_value="";
                boolean everyday_flag=true;
                if(week_repeat[PublicConsts.WEEK_MONDAY]) tv_value+=context.getResources().getString(R.string.monday)+" ";else everyday_flag=false;
                if(week_repeat[PublicConsts.WEEK_TUESDAY]) tv_value+=context.getResources().getString(R.string.tuesday)+" ";else everyday_flag=false;
                if(week_repeat[PublicConsts.WEEK_WEDNESDAY]) tv_value+=context.getResources().getString(R.string.wednesday)+" ";else everyday_flag=false;
                if(week_repeat[PublicConsts.WEEK_THURSDAY]) tv_value+=context.getResources().getString(R.string.thursday)+" ";else everyday_flag=false;
                if(week_repeat[PublicConsts.WEEK_FRIDAY]) tv_value+=context.getResources().getString(R.string.friday)+" ";else everyday_flag=false;
                if(week_repeat[PublicConsts.WEEK_SATURDAY]) tv_value+=context.getResources().getString(R.string.saturday)+" ";else everyday_flag=false;
                if(week_repeat[PublicConsts.WEEK_SUNDAY]) tv_value+=context.getResources().getString(R.string.sunday);else everyday_flag=false;

                if(everyday_flag) tv_value=context.getResources().getString(R.string.everyday);

                Calendar calendar=Calendar.getInstance();
                calendar.setTimeInMillis(time);
                return tv_value+" "+ValueUtils.format(calendar.get(Calendar.HOUR_OF_DAY))+":"+ValueUtils.format(calendar.get(Calendar.MINUTE));

            }

            public static String getBatteryPercentageDisplayValue(Context context, int trigger_type, int percentage){
                StringBuilder value=new StringBuilder("");
                if(trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE){
                    value.append(context.getResources().getString(R.string.more_than));
                    value.append(" ");
                    value.append(percentage);
                    value.append("%");
                }else if(trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE){
                    value.append(context.getResources().getString(R.string.less_than));
                    value.append(" ");
                    value.append(percentage);
                    value.append("%");
                }
                 return value.toString();
            }

            public static String getBatteryTemperatureDisplayValue(Context context, int trigger_type, int battery_temperature){
                StringBuilder value=new StringBuilder("");
                if(trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE){
                    value.append(context.getResources().getString(R.string.lower_than));
                    value.append(" ");
                    value.append(battery_temperature);
                    value.append("℃");
                }else if(trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE){
                    value.append(context.getResources().getString(R.string.higher_than));
                    value.append(" ");
                    value.append(battery_temperature);
                    value.append("℃");
                }
                return value.toString();
            }

            /**
             * @deprecated
             */
            public static String getBroadcastDisplayValue(String intent_action){
               // TextView tv_broadcast=findViewById(R.id.layout_taskgui_area_condition_received_broadcast_value);
               // if(trigger_type==PublicConsts.TRIGGER_TYPE_RECEIVED_BROADCAST){
                //    tv_broadcast.setText(taskitem.selectedAction);
               // }
                return  intent_action;
            }

            public static String getWifiConnectionDisplayValue(Context context, String ssids){
                if(context==null||ssids==null) return "";
                if(ssids.length()==0||ssids.trim().equals("")) return context.getResources().getString(R.string.activity_trigger_wifi_no_ssid_assigned);
               // WifiManager wifiManager=(WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if(Global.NetworkReceiver.wifiList2.size()==0) return context.getResources().getString(R.string.activity_trigger_wifi_assigned_ssid);
                StringBuilder display=new StringBuilder("");
                //List<WifiConfiguration> list=wifiManager.getConfiguredNetworks();
               // if(list==null||list.size()<=0) return "";
                String ssid_array [] =ssids.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                for(String s:ssid_array){
                    for(WifiConfiguration w: Global.NetworkReceiver.wifiList2){
                        try{
                           if(Integer.parseInt(s)==w.networkId){
                               if(!display.toString().equals("")) display.append(" , ");
                               display.append(w.SSID);
                           }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }

                return display.toString();
            }

            /**
             * @deprecated
             */
            public static String getWidgetDisplayValue(Context context, int triggerType){
                switch (triggerType){
                    default:break;
                    case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_ON: return context.getResources().getString(R.string.dialog_triggers_widget_wifi_on);
                    case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF:return context.getResources().getString(R.string.dialog_triggers_widget_wifi_off);
                    case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON: return context.getResources().getString(R.string.dialog_triggers_widget_bluetooth_on);
                    case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF: return context.getResources().getString(R.string.dialog_triggers_widget_bluetooth_off);
                    case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF: return context.getResources().getString(R.string.dialog_triggers_widget_ring_mode_off);
                    case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE: return context.getResources().getString(R.string.dialog_triggers_widget_ring_mode_vibrate);
                    case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL: return context.getResources().getString(R.string.dialog_triggers_widget_ring_mode_normal);
                    case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED: return context.getResources().getString(R.string.dialog_triggers_widget_ap_on);
                    case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED: return context.getResources().getString(R.string.dialog_triggers_widget_ap_off);
                    case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON: return context.getResources().getString(R.string.dialog_triggers_widget_airplane_mode_on);
                    case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF: return context.getResources().getString(R.string.dialog_triggers_widget_airplane_mode_off);
                    case TriggerTypeConsts.TRIGGER_TYPE_NET_ON: return context.getResources().getString(R.string.dialog_triggers_widget_net_on);
                    case TriggerTypeConsts.TRIGGER_TYPE_NET_OFF: return context.getResources().getString(R.string.dialog_triggers_widget_net_off);
                }
                return "";
            }

            public static String getAppNameDisplayValue(Context context, String[] packageNames){
                if(packageNames==null||packageNames.length==0) return "";
                StringBuilder builder=new StringBuilder("");
                PackageManager manager=context.getPackageManager();
                for(int i=0;i<packageNames.length;i++){
                    String packageName=packageNames[i];
                    try{
                        builder.append(manager.getApplicationLabel(manager.getApplicationInfo(packageNames[i],PackageManager.GET_META_DATA)));
                        if(packageName.length()>1&&i<packageNames.length-1) builder.append(",");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                return builder.toString();
            }

            public static String getBrightnessTriggerDisplayValue(Context context,int trigger_type,int value){
                Resources resources=context.getResources();
                if(trigger_type==TriggerTypeConsts.TRIGGER_TYPE_LIGHT_SENSOR_HIGHER_THAN) return resources.getString(R.string.trigger_brightness_higher_than)+value+resources.getString(R.string.trigger_brightness_unit);
                if (trigger_type==TriggerTypeConsts.TRIGGER_TYPE_LIGHT_SENSOR_LOWER_THAN) return resources.getString(R.string.trigger_brightness_lower_than)+value+resources.getString(R.string.trigger_brightness_unit);
                return "";
            }
        }
    }

    public static class ExceptionContentAdapter{

        public static String getExceptionValue(Context context, TaskItem item){
            try{
                StringBuilder builder=new StringBuilder("");
                String [] exceptions=item.exceptions;
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_LOCKEDSCREEN])==1){
                    builder.append(context.getResources().getString(R.string.activity_taskgui_exception_screen_locked));
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_UNLOCKEDSCREEN])==1){
                    builder.append(context.getResources().getString(R.string.activity_taskgui_exception_screen_unlocked));
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_WIFI_ENABLED])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.activity_taskgui_exception_wifi_enabled));
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_WIFI_DISABLED])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.activity_taskgui_exception_wifi_disabled));
                }

                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_BLUETOOTH_ENABLED])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.activity_taskgui_exception_bluetooth_enabled));
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_BLUETOOTH_DISABLED])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.activity_taskgui_exception_bluetooth_disabled));
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_RING_VIBRATE])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.activity_taskgui_exception_ring_vibrate));
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_RING_OFF])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.activity_taskgui_exception_ring_off));
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_RING_NORMAL])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.activity_taskgui_exception_ring_normal));
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_NET_ENABLED])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.activity_taskgui_exception_net_enabled));
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_NET_DISABLED])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.activity_taskgui_exception_net_disabled));
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_GPS_ENABLED])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.activity_taskgui_exception_gps_on));
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_GPS_DISABLED])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.activity_taskgui_exception_gps_off));
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_AIRPLANE_MODE_ENABLED])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.activity_taskgui_exception_airplanemode_on));
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_AIRPLANE_MODE_DISABLED])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.activity_taskgui_exception_airplanemode_off));
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_HEADSET_STATUS])>0){
                    if(builder.toString().length()>0) builder.append(",");
                    if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_HEADSET_STATUS])== ExceptionConsts.EXCEPTION_HEADSET_PLUG_OUT){
                        builder.append(context.getResources().getString(R.string.activity_taskgui_exception_headset));
                        builder.append(context.getResources().getString(R.string.activity_taskgui_exception_headset_out));
                    }
                    if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_HEADSET_STATUS])== ExceptionConsts.EXCEPTION_HEADSET_PLUG_IN){
                        builder.append(context.getResources().getString(R.string.activity_taskgui_exception_headset));
                        builder.append(context.getResources().getString(R.string.activity_taskgui_exception_headset_in));
                    }
                }

                String day_of_week="";
                String day_of_week_values="";
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_MONDAY])==1) {
                    day_of_week_values+=context.getResources().getString(R.string.monday);
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_TUESDAY])==1){
                    if(day_of_week_values.trim().length()>0)day_of_week_values+=" ";
                    day_of_week_values+=context.getResources().getString(R.string.tuesday);
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_WEDNESDAY])==1){
                    if(day_of_week_values.trim().length()>0)day_of_week_values+=" ";
                    day_of_week_values+=context.getResources().getString(R.string.wednesday);
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_THURSDAY])==1){
                    if(day_of_week_values.trim().length()>0)day_of_week_values+=" ";
                    day_of_week_values+=context.getResources().getString(R.string.thursday);
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_FRIDAY])==1){
                    if(day_of_week_values.trim().length()>0)day_of_week_values+=" ";
                    day_of_week_values+=context.getResources().getString(R.string.friday);
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_SATURDAY])==1){
                    if(day_of_week_values.trim().length()>0)day_of_week_values+=" ";
                    day_of_week_values+=context.getResources().getString(R.string.saturday);
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_SUNDAY])==1){
                    if(day_of_week_values.trim().length()>0)day_of_week_values+=" ";
                    day_of_week_values+=context.getResources().getString(R.string.sunday);
                }
                if(day_of_week_values.trim().length()>0){
                    day_of_week+=(context.getResources().getString(R.string.adapter_exception_day_of_week_head));
                    day_of_week+=day_of_week_values;
                }
                if(builder.toString().length()>0&&day_of_week.length()>0) builder.append(",");
                builder.append(day_of_week);
                /*if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_MONDAY])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.monday));
                }
                if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_TUESDAY])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.tuesday));
                }
                if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_WEDNESDAY])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.wednesday));
                }
                if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_THURSDAY])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.thursday));
                }
                if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_FRIDAY])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.friday));
                }
                if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_SATURDAY])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.saturday));
                }
                if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_SUNDAY])==1){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.sunday));
                }*/


                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_START_TIME])>=0&&Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_END_TIME])>=0){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.log_exceptions_period));
                    builder.append(ValueUtils.timePeriodFormatValue(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_START_TIME]),Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_END_TIME])));
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE])>=0){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.log_exceptions_battery_percentage_less_than));
                    builder.append(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]));
                    builder.append("%");
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE])>=0){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.log_exceptions_battery_percentage_more_than));
                    builder.append(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]));
                    builder.append("%");
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE])>=0){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.log_exceptions_battery_temperature_higher_than));
                    builder.append(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]));
                    builder.append(context.getResources().getString(R.string.degree_celsius));
                }
                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE])>=0){
                    if(builder.toString().length()>0) builder.append(",");
                    builder.append(context.getResources().getString(R.string.log_exceptions_battery_temperature_lower_than));
                    builder.append(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]));
                    builder.append(context.getResources().getString(R.string.degree_celsius));
                }

                if(Integer.parseInt(exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[0])!=-1){
                    if(builder.toString().length()>0)builder.append(",");
                    builder.append(context.getResources().getString(R.string.exception_wifi_status));
                    builder.append(":");
                    builder.append(getExceptionValueOfWifiStatus(context,exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS]));
                }

                String returnValue=builder.toString();
                if(returnValue.equals("")) return context.getResources().getString(R.string.word_nothing);

                String exception_connector;
                if(Integer.parseInt(item.addition_exception_connector)==0) exception_connector=context.getResources().getString(R.string.adapter_exception_type_and);
                else exception_connector=context.getResources().getString(R.string.adapter_exception_type_or);

                return exception_connector+returnValue;
            }catch (Exception e){
                e.printStackTrace();
            }
            return "";
        }

        public static String getExceptionValueOfWifiStatus(Context context,String values){
            try{
                StringBuilder builder=new StringBuilder("");
                String ssid_array [] =values.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                int head = Integer.parseInt(ssid_array[0]);
                if(head==-1) return context.getResources().getString(R.string.unselected);
                if(head==ExceptionConsts.EXCEPTION_WIFI_VALUE_DISCONNECTED) return context.getResources().getString(R.string.exception_wifi_status_disconnected);
                if(head==ExceptionConsts.EXCEPTION_WIFI_VALUE_CONNECTED_TO_RANDOM_SSID) return context.getResources().getString(R.string.activity_trigger_wifi_no_ssid_assigned);
                for(String s:ssid_array){
                    for(WifiConfiguration w: Global.NetworkReceiver.wifiList2){
                        try{
                            if(Integer.parseInt(s)==w.networkId){
                                if(!builder.toString().equals("")) builder.append(" , ");
                                builder.append(w.SSID);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                if(builder.toString().equals("")) return context.getResources().getString(R.string.activity_trigger_wifi_assigned_ssid);
                return context.getResources().getString(R.string.activity_trigger_wifi_assigned_ssid)+":"+builder.toString();
            }catch (Exception e){e.printStackTrace();}
            return "";
        }
    }

    public static class ActionContentAdapter{

        /**
         * 常规型Action，打开，关闭和未选择三个值。
         * @param context context
         * @param value action value
         * @return display string
         */
        @NonNull
        public static String getGeneralDisplayValue(@NonNull Context context, String value){
            try{
                int action_value=Integer.parseInt(value);
                if(action_value== ActionConsts.ActionValueConsts.ACTION_OPEN) return context.getResources().getString(R.string.open);
                else if(action_value== ActionConsts.ActionValueConsts.ACTION_CLOSE) return  context.getResources().getString(R.string.close);
                else if(action_value== ActionConsts.ActionValueConsts.ACTION_UNSELECTED) return "";
            }catch (Exception e){
                e.printStackTrace();
            }
            return "";
        }

        @NonNull
        public static String getDeviceControlDisplayValue(@NonNull Context context, String value){
            try{
                int action_device=Integer.parseInt(value);
                if(action_device== ActionConsts.ActionValueConsts.ACTION_DEVICE_CONTROL_REBOOT)  return context.getResources().getString(R.string.reboot);
                else if(action_device== ActionConsts.ActionValueConsts.ACTION_DEVICE_CONTROL_SHUTDOWN) return context.getResources().getString(R.string.shut_down);
                else if(action_device== ActionConsts.ActionValueConsts.ACTION_DEVICE_CONTROL_NONE) return "";
            }catch (Exception e){
                e.printStackTrace();
            }
            return "";
        }

        @NonNull
        public static String getRingModeDisplayValue(@NonNull Context context, String value){
            try{
                int action_ring_mode=Integer.parseInt(value);
                if(action_ring_mode== ActionConsts.ActionValueConsts.ACTION_RING_VIBRATE)  return context.getResources().getString(R.string.vibrate);
                else if(action_ring_mode== ActionConsts.ActionValueConsts.ACTION_RING_OFF) return context.getResources().getString(R.string.silent);
                else if(action_ring_mode== ActionConsts.ActionValueConsts.ACTION_RING_NORMAL) return context.getResources().getString(R.string.ring_normal);
                else if(action_ring_mode== ActionConsts.ActionValueConsts.ACTION_RING_UNSELECTED) return "";
            }catch (Exception e){
                e.printStackTrace();
            }
            return "";
        }

        public static String getRingVolumeDisplayValue(@NonNull Context context, @NonNull String values){
            try{
                AudioManager manager=(AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                String[] volume_values=values.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                int volume_ring=Integer.parseInt(volume_values[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_RING_LOCALE]);
                int volume_media=Integer.parseInt(volume_values[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_MEDIA_LOCALE]);
                int volume_notification=Integer.parseInt(volume_values[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_NOTIFICATION_LOCALE]);
                int volume_alarm=Integer.parseInt(volume_values[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_ALARM_LOCALE]);
                StringBuilder displayBuilder=new StringBuilder("");
                if(volume_ring>=0){
                    displayBuilder.append(context.getResources().getString(R.string.activity_taskgui_actions_ring_volume_ring));
                    displayBuilder.append((int)(((double)volume_ring/manager.getStreamMaxVolume(AudioManager.STREAM_RING))*100));
                    displayBuilder.append("%");
                    displayBuilder.append(" ");
                }
                if(volume_media>=0){
                    displayBuilder.append(context.getResources().getString(R.string.activity_taskgui_actions_ring_volume_media));
                    displayBuilder.append((int)(((double)volume_media/manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))*100));
                    displayBuilder.append("%");
                    displayBuilder.append(" ");
                }
                if(volume_notification>=0){
                    displayBuilder.append(context.getResources().getString(R.string.activity_taskgui_actions_ring_volume_notification));
                    displayBuilder.append((int)(((double)volume_notification/manager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION))*100));
                    displayBuilder.append("%");
                    displayBuilder.append(" ");
                }
                if(volume_alarm>=0){
                    displayBuilder.append(context.getResources().getString(R.string.activity_taskgui_actions_ring_volume_alarm));
                    displayBuilder.append((int)(((double)volume_alarm/manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))*100));
                    displayBuilder.append("%");
                }
                return displayBuilder.toString();
            }catch (Exception e){
                e.printStackTrace();
            }
            return "";
        }

        public static String getBrightnessDisplayValue(@NonNull Context context, String value){
            try{
                int action_brightness=Integer.parseInt(value);
                if(action_brightness== ActionConsts.ActionValueConsts.ACTION_BRIGHTNESS_AUTO)  return context.getResources().getString(R.string.action_brightness_auto);
                else if(action_brightness>=0&&action_brightness<=PublicConsts.BRIGHTNESS_MAX)
                    return context.getResources().getString(R.string.action_brightness_manual)+(int)((float)action_brightness/PublicConsts.BRIGHTNESS_MAX*100)+"%";
            }catch (Exception e){
                e.printStackTrace();
            }
            return "";
        }

        public static String getRingSelectionDisplayValue(@NonNull Context context, @NonNull String values){
            try{
                String ring_selection_values[]=values.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                int ring_notification_selection= Integer.parseInt(ring_selection_values[ActionConsts.ActionSecondLevelLocaleConsts.RING_SELECTION_NOTIFICATION_TYPE_LOCALE]);
                int ring_phone_selection=Integer.parseInt(ring_selection_values[ActionConsts.ActionSecondLevelLocaleConsts.RING_SELECTION_CALL_TYPE_LOCALE]);
                StringBuilder displayBuilder=new StringBuilder("");
                if(ring_notification_selection== ActionConsts.ActionValueConsts.RING_TYPE_FROM_SYSTEM ||ring_notification_selection== ActionConsts.ActionValueConsts.RING_TYPE_FROM_MEDIA){
                    String ringOfNotification=context.getResources().getString(R.string.activity_taskgui_actions_ring_selection_notification);
                    displayBuilder.append(ringOfNotification);
                    displayBuilder.append(" ");
                }
                if(ring_phone_selection== ActionConsts.ActionValueConsts.RING_TYPE_FROM_SYSTEM ||ring_phone_selection== ActionConsts.ActionValueConsts.RING_TYPE_FROM_MEDIA){
                    String ringOfPhone=context.getResources().getString(R.string.activity_taskgui_actions_ring_selection_phone);
                    displayBuilder.append(ringOfPhone);
                }
               return displayBuilder.toString();
            }catch (Exception e){
                e.printStackTrace();
            }
            return "";
        }

        public static String getWallpaperDisplayValue(@NonNull Context context, @NonNull String value, @NonNull String uri){
            try{
                if(Integer.parseInt(value)==-1) return "";
                else {
                    //String filename=new File(ValueUtils.getRealPathFromUri(context, Uri.parse(uri))).getName();//wallpaper_values[1]
                    //if(filename.length()>25) filename=filename.substring(0,25)+"...";
                    return new File(ValueUtils.getRealPathFromUri(context, Uri.parse(uri))).getName();
                }
            }catch (Exception e){
                e.printStackTrace();
                //LogUtil.putExceptionLog(context,e);
            }
            return "";
        }

        public static String getVibrateDisplayValue(@NonNull Context context, @NonNull String value){
            try{
                String[] vibrate_values=value.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                StringBuilder displayValue=new StringBuilder("");
                int frequency=Integer.parseInt(vibrate_values[ActionConsts.ActionSecondLevelLocaleConsts.VIBRATE_FREQUENCY_LOCALE]);
                int duration=Integer.parseInt(vibrate_values[ActionConsts.ActionSecondLevelLocaleConsts.VIBRATE_DURATION_LOCALE]);
                int interval=Integer.parseInt(vibrate_values[ActionConsts.ActionSecondLevelLocaleConsts.VIBRATE_INTERVAL_LOCALE]);
                if(frequency>0){
                    //displayValue.append(getResources().getString(R.string.dialog_actions_vibrate_frequency));
                    //displayValue.append(":");
                    displayValue.append(frequency);
                    displayValue.append(context.getResources().getString(R.string.dialog_actions_vibrate_frequency_measure));
                    displayValue.append(",");
                    //displayValue.append(getResources().getString(R.string.dialog_actions_vibrate_duration));
                    //displayValue.append(":");
                    displayValue.append(duration);
                    displayValue.append("ms");//getResources().getString(R.string.dialog_actions_vibrate_duration_measure));
                    displayValue.append(",");
                    //displayValue.append(getResources().getString(R.string.dialog_actions_vibrate_interval));
                    //displayValue.append(":");
                    displayValue.append(interval);
                    displayValue.append("ms");//getResources().getString(R.string.dialog_actions_vibrate_interval_measure));
                }
                return displayValue.toString();
            }catch (Exception e){
                e.printStackTrace();
            }
            return "";
        }

        public static String getNotificationDisplayValue(@NonNull Context context, String value){
            try{
                StringBuilder builder=new StringBuilder("");
                String[] notification_values=value.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                if(Integer.parseInt(notification_values[ActionConsts.ActionSecondLevelLocaleConsts.NOTIFICATION_TYPE_LOCALE])== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_UNSELECTED){
                    builder.append("");
                }else if(Integer.parseInt(notification_values[ActionConsts.ActionSecondLevelLocaleConsts.NOTIFICATION_TYPE_LOCALE])== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_NOT_OVERRIDE){
                    builder.append(context.getResources().getString(R.string.activity_taskgui_actions_notification_not_override));
                    builder.append(":");
                    if(Integer.parseInt(notification_values[ActionConsts.ActionSecondLevelLocaleConsts.NOTIFICATION_TYPE_IF_CUSTOM_LOCALE])== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_CONTENT_DEFAULT){
                        builder.append(context.getResources().getString(R.string.activity_taskgui_actions_notification_type_default));
                    }else if(Integer.parseInt(notification_values[ActionConsts.ActionSecondLevelLocaleConsts.NOTIFICATION_TYPE_IF_CUSTOM_LOCALE])== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_CONTENT_CUSTOM){
                        builder.append(context.getResources().getString(R.string.activity_taskgui_actions_notification_type_custom));
                    }
                }else if(Integer.parseInt(notification_values[ActionConsts.ActionSecondLevelLocaleConsts.NOTIFICATION_TYPE_LOCALE])== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_OVERRIDE_LAST){
                    builder.append(context.getResources().getString(R.string.activity_taskgui_actions_notification_override_last));
                    builder.append(":");
                    if(Integer.parseInt(notification_values[ActionConsts.ActionSecondLevelLocaleConsts.NOTIFICATION_TYPE_IF_CUSTOM_LOCALE])== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_CONTENT_DEFAULT){
                        builder.append(context.getResources().getString(R.string.activity_taskgui_actions_notification_type_default));
                    }else if(Integer.parseInt(notification_values[ActionConsts.ActionSecondLevelLocaleConsts.NOTIFICATION_TYPE_IF_CUSTOM_LOCALE])== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_CONTENT_CUSTOM){
                        builder.append(context.getResources().getString(R.string.activity_taskgui_actions_notification_type_custom));
                    }
                }
                return builder.toString();
            }catch (Exception e){
                e.printStackTrace();
            }
            return "";
        }

        public static String getSMSDisplayValue(Context context, String value){
            try{
                String sms_values[]=value.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                StringBuilder builder=new StringBuilder("");
                if(Integer.parseInt(sms_values[ActionConsts.ActionSecondLevelLocaleConsts.SMS_ENABLED_LOCALE])>=0 ){
                    builder.append(context.getResources().getString(R.string.activity_taskgui_actions_sms_enabled));
                    if(Integer.parseInt(sms_values[ActionConsts.ActionSecondLevelLocaleConsts.SMS_RESULT_TOAST_LOCALE])>=0){
                        builder.append(":");
                        builder.append(context.getResources().getString(R.string.activity_taskgui_actions_sms_receipt));
                    }
                }
                return builder.toString();
            }catch (Exception e){
                e.printStackTrace();
            }
            return "";
        }

        public static String getToastDisplayValue(@NonNull String value, @NonNull String toast){
            try{
                String toast_values[]=value.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                if(Integer.parseInt(toast_values[0])>=0) {
                    //if(toast.length()>15) toast=toast.substring(0,15)+"...";
                    return toast;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return "";
        }

        public static String getTaskSwitchDisplayValue(@NonNull String value){
            try{
                String[] values=value.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
                if(Integer.parseInt(values[0])>=0) {
                    StringBuilder builder=new StringBuilder("");
                    for (String id:values){
                        /*int position= ProcessTaskItem.getPosition(Integer.parseInt(id));
                        if(position>=0&& TimeSwitchService.list!=null&&position<TimeSwitchService.list.size()){
                            builder.append(TimeSwitchService.list.get(position).name);
                            builder.append(" ");
                        }*/
                        TaskItem item=ProcessTaskItem.getTaskItemOfId(TimeSwitchService.list,Integer.parseInt(id));
                        if(item==null) continue;
                        builder.append(item.name);
                        builder.append(" ");
                    }
                    //String displayValue=builder.toString();
                    //if(displayValue.length()>15) displayValue=displayValue.substring(0,15)+"...";
                    return builder.toString();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return "";
        }

        public static String getAppNameDisplayValue(Context context, String value){
            if(value==null) return "";
            String[] packageNames;
            try{
                packageNames=value.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
            }catch (Exception e){
                e.printStackTrace();
                return "";
            }
            StringBuilder builder=new StringBuilder("");
            try{
                try{
                    if(Integer.parseInt(packageNames[0])<0) return "";
                }catch (Exception e){
                    //e.printStackTrace();
                }
                PackageManager manager=context.getPackageManager();
                for(int i=0;i<packageNames.length;i++){
                    builder.append(manager.getApplicationLabel(manager.getApplicationInfo(packageNames[i],PackageManager.GET_META_DATA)));
                    if(packageNames.length>1&&i<packageNames.length-1) builder.append(",");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            //String return_value=builder.toString();
            //if(return_value.length()>15) return_value=return_value.substring(0,15)+"...";
            return builder.toString();
        }

        public static String getFlashlightDisplayValue(Context context,String values){
            try{
                String [] array=values.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                int type=Integer.parseInt(array[0]);
                if(type==-1) return "";
                if(type==ActionConsts.ActionValueConsts.ACTION_FLASHLIGHT_TYPE_HOLD){
                    return context.getResources().getString(R.string.action_flashlight_hold)+":"+array[1];
                }
                if(type== ActionConsts.ActionValueConsts.ACTION_FLASHLIGHT_TYPE_CUSTOM) {
                    String set="";
                    for(int i=1;i<array.length;i++) {
                        set+=array[i];
                        if(i<array.length-1) set+=",";
                    }
                    return context.getResources().getString(R.string.action_flashlight_custom)+":"+set;
                }
            }catch (Exception e){e.printStackTrace();}
            return "";
        }

        public static boolean isGeneralItemVisible(String value){
            try{
                return Integer.parseInt(value)>=0;
            }catch (Exception e){
                e.printStackTrace();
            }
            return false;
        }

        public static class ActionDisplayValuesOfMainPage {

            public static String getWifiDisplayValue(Context context,String value){
                try{
                    int action_wifi=Integer.parseInt(value);
                    if(action_wifi>=0){
                        if(action_wifi==1) return context.getResources().getString(R.string.action_wifi_open);
                        if(action_wifi==0) return context.getResources().getString(R.string.action_wifi_close);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getBluetoothDisplayValue(Context context,String value){
                try{
                    int action_bluetooth=Integer.parseInt(value);
                    if(action_bluetooth==0) return context.getResources().getString(R.string.action_bluetooth_close);
                    if(action_bluetooth==1) return context.getResources().getString(R.string.action_bluetooth_open);
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getRingModeDisplayValue(Context context,String value){
                try {
                    int action_ring=Integer.parseInt(value);
                    if(action_ring== ActionConsts.ActionValueConsts.ACTION_RING_NORMAL) return context.getResources().getString(R.string.action_ring_mode_normal);
                    if(action_ring== ActionConsts.ActionValueConsts.ACTION_RING_VIBRATE) return (context.getResources().getString(R.string.action_ring_mode_vibrate));
                    if(action_ring== ActionConsts.ActionValueConsts.ACTION_RING_OFF) return (context.getResources().getString(R.string.action_ring_mode_off));
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getRingVolumeDisplayValue(Context context,String values){
                try{
                    String [] action_volumes=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
                    StringBuilder builder=new StringBuilder("");
                    AudioManager audioManager=(AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    int volume_call=Integer.parseInt(action_volumes[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_RING_LOCALE]);
                    int volume_notification=Integer.parseInt(action_volumes[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_NOTIFICATION_LOCALE]);
                    int volume_media=Integer.parseInt(action_volumes[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_MEDIA_LOCALE]);
                    int volume_alarm=Integer.parseInt(action_volumes[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_ALARM_LOCALE]);
                    if(volume_call>=0) {
                        builder.append(context.getResources().getString(R.string.adapter_action_volume_call));
                        builder.append((int)(((double)volume_call/audioManager.getStreamMaxVolume(AudioManager.STREAM_RING))*100));
                        builder.append(context.getResources().getString(R.string.percentage));
                    }
                    if(volume_media>=0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(context.getResources().getString(R.string.adapter_action_volume_music));
                        builder.append((int)(((double)volume_media/audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))*100));
                        builder.append(context.getResources().getString(R.string.percentage));
                    }
                    if(volume_notification>=0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(context.getResources().getString(R.string.adapter_action_volume_notification));
                        builder.append((int)(((double)volume_notification/audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION))*100));
                        builder.append(context.getResources().getString(R.string.percentage));
                    }
                    if(volume_alarm>=0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(context.getResources().getString(R.string.adapter_action_volume_alarm));
                        builder.append((int)(((double)volume_alarm/audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM))*100));
                        builder.append(context.getResources().getString(R.string.percentage));
                    }
                    return builder.toString();
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getRingSelectionDisplayValue(Context context,String values,String uri_notification,String uri_call){
                try{
                    String[] action_ring_selections=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
                    StringBuilder builder=new StringBuilder("");
                    int action_ring_selection_call=Integer.parseInt(action_ring_selections[ActionConsts.ActionSecondLevelLocaleConsts.RING_SELECTION_CALL_TYPE_LOCALE]);
                    int action_ring_selection_notification=Integer.parseInt(action_ring_selections[ActionConsts.ActionSecondLevelLocaleConsts.RING_SELECTION_NOTIFICATION_TYPE_LOCALE]);
                    if(action_ring_selection_notification>=0){
                        builder.append(context.getResources().getString(R.string.action_set_ringtone_notification));
                        builder.append(RingtoneManager.getRingtone(context, Uri.parse(uri_notification)).getTitle(context));
                    }
                    if(action_ring_selection_call>=0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(context.getResources().getString(R.string.action_set_ringtone_phone));
                        builder.append(RingtoneManager.getRingtone(context, Uri.parse(uri_call)).getTitle(context));
                    }
                    return builder.toString();
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getRingSelectionDisplayValue(Context context, TaskItem item){
                return getRingSelectionDisplayValue(context,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_SELECTION_LOCALE],item.uri_ring_notification,item.uri_ring_call);
            }

            public static String getVibrateDisplayValue(Context context,String values){
                try{
                    String [] action_vibrate_values=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
                    StringBuilder builder=new StringBuilder("");
                    int vibrate_frequency=Integer.parseInt(action_vibrate_values[ActionConsts.ActionSecondLevelLocaleConsts.VIBRATE_FREQUENCY_LOCALE]);
                    if(vibrate_frequency>0){
                        int vibrate_duration=Integer.parseInt(action_vibrate_values[ActionConsts.ActionSecondLevelLocaleConsts.VIBRATE_DURATION_LOCALE]);
                        int vibrate_interval=Integer.parseInt(action_vibrate_values[ActionConsts.ActionSecondLevelLocaleConsts.VIBRATE_INTERVAL_LOCALE]);
                        builder.append(context.getResources().getString(R.string.adapter_action_vibrate));
                        builder.append(vibrate_frequency);
                        builder.append(context.getResources().getString(R.string.dialog_actions_vibrate_frequency_measure));
                        builder.append(",");
                        builder.append(vibrate_duration);
                        builder.append(context.getResources().getString(R.string.dialog_actions_vibrate_duration_measure));
                        builder.append(",");
                        builder.append(vibrate_interval);
                        builder.append(context.getResources().getString(R.string.dialog_actions_vibrate_interval_measure));
                        return builder.toString();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getBrightnessDisplayValue(Context context,String value){
                try{
                    int action_brightness=Integer.parseInt(value);
                    if(action_brightness>=0){
                        StringBuilder builder=new StringBuilder("");
                        builder.append(context.getResources().getString(R.string.adapter_action_brightness));
                        if(action_brightness== ActionConsts.ActionValueConsts.ACTION_BRIGHTNESS_AUTO) builder.append(context.getResources().getString(R.string.adapter_action_brightness_auto));
                        else {
                            builder.append((int)(((double)action_brightness/PublicConsts.BRIGHTNESS_MAX)*100));
                            builder.append(context.getResources().getString(R.string.percentage));
                        }
                        return builder.toString();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getWallpaperDisplayValue(Context context, String value, String uri_wallpaper){
                try{
                    StringBuilder builder=new StringBuilder("");
                    int action_wallpaper=Integer.parseInt(value);
                    if(action_wallpaper>=0){
                        builder.append(context.getResources().getString(R.string.action_set_wallpaper));
                        builder.append(ValueUtils.getRealPathFromUri(context,Uri.parse(uri_wallpaper)));
                    }
                    return builder.toString();
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getWallpaperDisplayValue(Context context,TaskItem item){
                return getWallpaperDisplayValue(context,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SET_WALL_PAPER_LOCALE],item.uri_wallpaper_desktop);
            }

            public static String getSMSDisplayValue(Context context,String values,String addresses,String message){
                try{
                    String action_sms_values[]=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
                    StringBuilder builder=new StringBuilder("");
                    if(Integer.parseInt(action_sms_values[ActionConsts.ActionSecondLevelLocaleConsts.SMS_ENABLED_LOCALE])>=0){
                        builder.append(context.getResources().getString(R.string.adapter_action_sms));
                        if(Build.VERSION.SDK_INT>=22){
                            SubscriptionInfo subinfo= null;
                            try{
                                subinfo= SubscriptionManager.from(context).getActiveSubscriptionInfo(Integer.parseInt(action_sms_values[ActionConsts.ActionSecondLevelLocaleConsts.SMS_SUBINFO_LOCALE]));
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            if(subinfo!=null){
                                builder.append("(");
                                try{
                                    builder.append(subinfo.getDisplayName());
                                    builder.append(":");
                                    builder.append(subinfo.getNumber());
                                }catch (Exception e){
                                    e.printStackTrace();
                                    builder.append("");
                                }
                                builder.append(")");
                            }
                        }
                        builder.append(context.getResources().getString(R.string.adapter_action_sms_receivers));
                        builder.append(addresses);
                        builder.append(",");
                        builder.append(context.getResources().getString(R.string.adapter_action_sms_message));
                        builder.append(message);
                        return builder.toString();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getSMSDisplayValue(Context context,TaskItem item){
                return getSMSDisplayValue(context,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SMS_LOCALE],item.sms_address,item.sms_message);
            }

            public static String getToastDisplayValue(Context context,String values,String toast){
                try{
                    String []toast_values=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
                    int toast_type=Integer.parseInt(toast_values[0]);
                    if(toast_type>=0){
                        StringBuilder builder=new StringBuilder("");
                        builder.append(context.getResources().getString(R.string.adapter_action_toast));
                        if(toast_type== ActionConsts.ActionValueConsts.TOAST_TYPE_CUSTOM){
                            int toast_x_offset=Integer.parseInt(toast_values[ActionConsts.ActionSecondLevelLocaleConsts.TOAST_LOCATION_X_OFFSET_LOCALE]);
                            int toast_y_offset=Integer.parseInt(toast_values[ActionConsts.ActionSecondLevelLocaleConsts.TOAST_LOCATION_Y_OFFSET_LOCALE]);
                            builder.append(context.getResources().getString(R.string.toast_location_custom));
                            builder.append(":");
                            builder.append(toast_x_offset);
                            builder.append(",");
                            builder.append(toast_y_offset);
                            builder.append(",");
                        }
                        builder.append(context.getResources().getString(R.string.adapter_action_toast_message));
                        builder.append(toast);
                        return builder.toString();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getToastDisplayValue(Context context,TaskItem item){
                return getToastDisplayValue(context,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_TOAST_LOCALE],item.toast);
            }

            public static String getGpsDisplayValue(Context context,String value){
                try{
                    int action_gps=Integer.parseInt(value);
                    if(action_gps==0) return context.getResources().getString(R.string.action_gps_off);
                    if(action_gps==1) return context.getResources().getString(R.string.action_gps_on);
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getNetDisplayValue(Context context,String value){
                try{
                  int action_net=Integer.parseInt(value);
                  if(action_net==0) return context.getResources().getString(R.string.action_net_off);
                  if(action_net==1) return context.getResources().getString(R.string.action_net_on);
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getAirplaneModeDisplayValue(Context context,String value){
                try{
                    int action_airplanemode=Integer.parseInt(value);
                    if(action_airplanemode==0) return context.getResources().getString(R.string.action_airplane_mode_off);
                    if(action_airplanemode==1) return context.getResources().getString(R.string.action_airplane_mode_on);
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getDeviceControlDisplayValue(Context context,String value){
                try {
                    int action_device=Integer.parseInt(value);
                    if(action_device== ActionConsts.ActionValueConsts.ACTION_DEVICE_CONTROL_SHUTDOWN) return context.getResources().getString(R.string.action_device_shutdown);
                    if(action_device== ActionConsts.ActionValueConsts.ACTION_DEVICE_CONTROL_REBOOT) return context.getResources().getString(R.string.action_device_reboot);
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getNotificationDisplayValue(Context context,String values,String title,String message){
                try{
                    String notification_values[]=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
                    if(Integer.parseInt(notification_values[0])>=0){
                        StringBuilder builder=new StringBuilder("");
                        if(Integer.parseInt(notification_values[0])==0){
                            builder.append(context.getResources().getString(R.string.adapter_action_notification_override_last));
                        }else if (Integer.parseInt(notification_values[0])==1){
                            builder.append(context.getResources().getString(R.string.adapter_action_notification_not_override));
                        }
                        if(Integer.parseInt(notification_values[1])==0) builder.append(context.getResources().getString(R.string.word_default));
                        if(Integer.parseInt(notification_values[1])==1) {
                            builder.append(context.getResources().getString(R.string.adapter_action_notification_title));
                            builder.append(title);
                            builder.append(",");
                            builder.append(context.getResources().getString(R.string.adapter_action_notification_message));
                            builder.append(message);
                        }
                        return builder.toString();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getNotificationDisplayValue(Context context,TaskItem item){
                return getNotificationDisplayValue(context,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NOTIFICATION_LOCALE],item.notification_title,item.notification_message);
            }

            public static String getTaskNamesDisplayValue(Context context,String values){
                try{
                    String [] ids=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
                    if(Integer.parseInt(ids[0])<0) return "";
                    StringBuilder builder=new StringBuilder("");
                    SQLiteDatabase database= MySQLiteOpenHelper.getInstance(context).getReadableDatabase();
                    for(int i=0;i<ids.length;i++){
                        //TaskItem item= ProcessTaskItem.getTaskItemOfId(TimeSwitchService.list,Integer.parseInt(ids[i]));
                        int id=Integer.parseInt(ids[i]);
                        if(id<0)continue;
                        Cursor cursor=database.rawQuery("select * from "+MySQLiteOpenHelper.getCurrentTableName(context)+" where "+ SQLConsts.SQL_TASK_COLUMN_ID+"="+id,null);
                        cursor.moveToFirst();
                        builder.append(cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_NAME)));
                        if(i<ids.length-1) builder.append(",");
                        cursor.close();
                    }
                    database.close();
                    return builder.toString();
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getEnableTasksDisplayValue(Context context,String values){
                try{
                    String tasknames=getTaskNamesDisplayValue(context,values);
                    if(tasknames.length()>0){
                        String displayValue="";
                        displayValue+=context.getResources().getString(R.string.adapter_action_task_enable);
                        displayValue+=tasknames;
                        return displayValue;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getDisableTasksDisplayValue(Context context,String values){
                try{
                    String tasknames=getTaskNamesDisplayValue(context,values);
                    if(tasknames.length()>0){
                        String displayValue="";
                        displayValue+=context.getResources().getString(R.string.adapter_action_task_disable);
                        displayValue+=tasknames;
                        return displayValue;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getAppPackagesDisplayValue(Context context,String values){
                try{
                    StringBuilder builder=new StringBuilder("");
                    String [] package_names=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
                    try{
                        if(Integer.parseInt(package_names[0])<0) return "";
                    }catch (Exception e){
                        //e.printStackTrace();
                    }
                    //builder.append(context.getResources().getString(R.string.adapter_action_app_open));
                    PackageManager manager=context.getApplicationContext().getPackageManager();
                    for(int i=0;i<package_names.length;i++){
                        builder.append(manager.getApplicationLabel(manager.getApplicationInfo(package_names[i],PackageManager.GET_META_DATA)));
                        if(i<package_names.length-1) builder.append(",");
                    }
                    return builder.toString();
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getAppLaunchDisplayValue(Context context,String values){
                try{
                    String apps=getAppPackagesDisplayValue(context,values);
                    if(apps.length()>0){
                        String displayValue="";
                        displayValue+=(context.getResources().getString(R.string.adapter_action_app_open));
                        displayValue+=(apps);
                        return displayValue;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getAppCloseDisplayValue(Context context,String values){
                try{
                    String apps=getAppPackagesDisplayValue(context,values);
                    if(apps.length()>0){
                        String displayValue="";
                        displayValue+=context.getResources().getString(R.string.adapter_action_app_close);
                        displayValue+=apps;
                        return displayValue;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getAppForceCloseValue(Context context ,String values){
                try{
                    String apps=getAppPackagesDisplayValue(context,values);
                    if(apps.length()>0){
                        return context.getResources().getString(R.string.adapter_action_app_force_close)+apps;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

            public static String getActionValue(Context context, TaskItem item){
                try{
                    String[]actions=item.actions;
                    StringBuilder builder=new StringBuilder("");

                    builder.append(getWifiDisplayValue(context,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_WIFI_LOCALE]));

                    String value_bluetooth= getBluetoothDisplayValue(context,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BLUETOOTH_LOCALE]);
                    if(value_bluetooth.length()>0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(value_bluetooth);
                    }


                    String value_ringmode= getRingModeDisplayValue(context,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_MODE_LOCALE]);
                    if(value_ringmode.length()>0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(value_ringmode);
                    }

                    String value_volume= getRingVolumeDisplayValue(context,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_VOLUME_LOCALE]);
                    if(value_volume.length()>0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(value_volume);
                    }


                    String value_ringselect= getRingSelectionDisplayValue(context,item);
                    if(value_ringselect.length()>0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(value_ringselect);
                    }

                    String value_vibrate= getVibrateDisplayValue(context,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_VIBRATE_LOCALE]);
                    if(value_vibrate.length()>0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(value_vibrate);
                    }

                    String value_autorotation=getGeneralDisplayValue(context,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AUTOROTATION]);
                    if(value_autorotation.length()>0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(value_autorotation);
                    }

                    String values_brightness= getBrightnessDisplayValue(context,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BRIGHTNESS_LOCALE]);
                    if(values_brightness.length()>0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(values_brightness);
                    }

                    String value_wallpaper= getWallpaperDisplayValue(context,item);
                    if(value_wallpaper.length()>0){
                        if(builder.toString().length()>0)builder.append(",");
                        builder.append(value_wallpaper);
                    }

                    String value_flashlight=getFlashlightDisplayValue(context,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FLASHLIGHT]);
                    if(value_flashlight.length()>0){
                        if(builder.toString().length()>0)builder.append(",");
                        builder.append(context.getResources().getString(R.string.action_flashlight));
                        builder.append(":");
                        builder.append(value_flashlight);
                    }

                    String value_sms= getSMSDisplayValue(context,item);
                    if(value_sms.length()>0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(value_sms);
                    }

                    String value_toast= getToastDisplayValue(context,item);
                    if(value_toast.length()>0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(value_toast);
                    }

                    String value_notification= getNotificationDisplayValue(context,item);
                    if(value_notification.length()>0) {
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(value_notification);
                    }

                    String value_gps= getGpsDisplayValue(context,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_GPS_LOCALE]);
                    if(value_gps.length()>0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(value_gps);
                    }

                    String value_net= getNetDisplayValue(context,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NET_LOCALE]);
                    if(value_net.length()>0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(value_net);
                    }

                    String value_airplanemode= getAirplaneModeDisplayValue(context,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AIRPLANE_MODE_LOCALE]);
                    if(value_airplanemode.length()>0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(value_airplanemode);
                    }

                    String value_device= getDeviceControlDisplayValue(context,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DEVICE_CONTROL_LOCALE]);
                    if(value_device.length()>0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(value_device);
                    }

                    String value_task_enable= getEnableTasksDisplayValue(context,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_ENABLE_TASKS_LOCALE]);
                    if(value_task_enable.length()>0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(value_task_enable);
                    }

                    String value_task_disable= getDisableTasksDisplayValue(context,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DISABLE_TASKS_LOCALE]);
                    if(value_task_disable.length()>0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(value_task_disable);
                    }

                    String value_app_open= getAppLaunchDisplayValue(context,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_LAUNCH_APP_PACKAGES]);
                    if(value_app_open.length()>0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(value_app_open);
                    }

                    String value_app_close= getAppCloseDisplayValue(context,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_STOP_APP_PACKAGES]);
                    if(value_app_close.length()>0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(value_app_close);
                    }

                    String value_app_force_close=getAppForceCloseValue(context,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FORCE_STOP_APP_PACKAGES]);
                    if(value_app_force_close.length()>0){
                        if(builder.toString().length()>0) builder.append(",");
                        builder.append(value_app_force_close);
                    }

                    String returnValue=builder.toString();
                    if(returnValue.trim().equals("")) return context.getResources().getString(R.string.word_nothing);
                    return returnValue;
                }catch (Exception e){
                    e.printStackTrace();
                    return "";
                }
            }
        }
    }

}
