package com.github.ghmxr.timeswitch.adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ghmxr.timeswitch.Global;
import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.util.Calendar;

public class ContentAdapter {
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
                if(Global.NetworkReceiver.wifiList==null|| Global.NetworkReceiver.wifiList.size()<=0) return context.getResources().getString(R.string.activity_trigger_wifi_assigned_ssid);
                StringBuilder display=new StringBuilder("");
                //List<WifiConfiguration> list=wifiManager.getConfiguredNetworks();
               // if(list==null||list.size()<=0) return "";
                String ssid_array [] =ssids.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                for(String s:ssid_array){
                    for(Global.NetworkReceiver.WifiConfigInfo w: Global.NetworkReceiver.wifiList){
                        try{
                           if(Integer.parseInt(s)==w.networkID){
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
        }
    }


}
