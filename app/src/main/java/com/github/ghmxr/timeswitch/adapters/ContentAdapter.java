package com.github.ghmxr.timeswitch.adapters;

import android.content.res.Resources;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;

public class ContentAdapter {

    public static final int CONTENT_TYPE_RESOURCE_DRAWABLE_ID=0;
    public static final int CONTENT_TYPE_DISPLAY_STRING =1;

    /**
     * 根据type返回一个Resource，type值参考TriggerTypeConsts
     */
    public static Object getContentForTriggerType(Resources resources,int content_type, int trigger_type){
        switch(trigger_type){
            default:break;
            case TriggerTypeConsts.TRIGGER_TYPE_SINGLE: {
                if(content_type==CONTENT_TYPE_RESOURCE_DRAWABLE_ID) return R.drawable.icon_repeat_single;
                //if(content_type==CONTENT_TYPE_DISPLAY_STRING) return;
            }

            case TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME:return R.drawable.icon_repeat_percertaintime;

            case TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK: return R.drawable.icon_repeat_weekloop;

            case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE: return R.drawable.icon_temperature;

            case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE: return R.drawable.icon_temperature;

            case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE: return R.drawable.icon_battery_high;

            case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE: return R.drawable.icon_battery_low;

            case TriggerTypeConsts.TRIGGER_TYPE_RECEIVED_BROADCAST: return R.drawable.icon_broadcast;

            case TriggerTypeConsts.TRIGGER_TYPE_WIFI_CONNECTED: return R.drawable.icon_wifi_connected;

            case TriggerTypeConsts.TRIGGER_TYPE_WIFI_DISCONNECTED: return R.drawable.icon_wifi_disconnected;

            case TriggerTypeConsts.TRIGGER_TYPE_SCREEN_ON: return R.drawable.icon_screen_on;

            case TriggerTypeConsts.TRIGGER_TYPE_SCREEN_OFF: return R.drawable.icon_screen_off;

            case TriggerTypeConsts.TRIGGER_TYPE_POWER_CONNECTED: return R.drawable.icon_power_connected;

            case TriggerTypeConsts.TRIGGER_TYPE_POWER_DISCONNECTED: return R.drawable.icon_power_disconnected;

            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_ON: return R.drawable.icon_wifi_on;

            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF:return R.drawable.icon_wifi_off;

            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON:return R.drawable.icon_bluetooth_on;

            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF:return R.drawable.icon_bluetooth_off;

            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF:return R.drawable.icon_ring_off;

            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE:return R.drawable.icon_ring_vibrate;

            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL:return R.drawable.icon_ring_normal;

            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF:return R.drawable.icon_airplanemode_off;

            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON:return R.drawable.icon_airplanemode_on;

            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED:return R.drawable.icon_ap_on;

            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED:return R.drawable.icon_ap_off;

            case TriggerTypeConsts.TRIGGER_TYPE_NET_ON:return R.drawable.icon_cellular_on;

            case TriggerTypeConsts.TRIGGER_TYPE_NET_OFF:return R.drawable.icon_cellular_off;

            case TriggerTypeConsts.TRIGGER_TYPE_APP_LAUNCHED:return R.drawable.icon_app_launch;

            case TriggerTypeConsts.TRIGGER_TYPE_APP_CLOSED:return R.drawable.icon_app_stop;

            case TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_IN:return R.drawable.icon_headset;

            case TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT:return R.drawable.icon_headset;
        }
        return null;
    }
}
