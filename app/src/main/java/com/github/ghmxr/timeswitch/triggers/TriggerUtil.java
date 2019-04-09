package com.github.ghmxr.timeswitch.triggers;

import android.content.Context;
import android.content.Intent;

import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.triggers.receivers.APReceiver;
import com.github.ghmxr.timeswitch.triggers.receivers.AirplaneModeReceiver;
import com.github.ghmxr.timeswitch.triggers.receivers.AppLaunchDetectionReceiver;
import com.github.ghmxr.timeswitch.triggers.receivers.BatteryReceiver;
import com.github.ghmxr.timeswitch.triggers.receivers.BluetoothReceiver;
import com.github.ghmxr.timeswitch.triggers.receivers.CustomBroadcastReceiver;
import com.github.ghmxr.timeswitch.triggers.receivers.HeadsetPlugReceiver;
import com.github.ghmxr.timeswitch.triggers.receivers.NetworkReceiver;
import com.github.ghmxr.timeswitch.triggers.receivers.RingModeReceiver;
import com.github.ghmxr.timeswitch.triggers.timers.CustomAlarmReceiver;

public class TriggerUtil {

    /**
     * 通过TaskItem参数获取一个Trigger实例
     * @param context context
     * @param item TaskItem 任务项
     * @return  根据TaskItem项参数创建的Trigger实例
     */
    public static Trigger getTrigger(Context context, TaskItem item){
        if(item==null) return null;
        switch (item.trigger_type){
            default:break;

            case PublicConsts.TRIGGER_TYPE_SINGLE: case PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME:
            case PublicConsts.TRIGGER_TYPE_LOOP_WEEK:{
                boolean api_alarm=context.getSharedPreferences(PublicConsts.PREFERENCES_NAME,Context.MODE_PRIVATE)
                        .getInt(PublicConsts.PREFERENCES_API_TYPE,PublicConsts.PREFERENCES_API_TYPE_DEFAULT)==PublicConsts.API_ANDROID_ALARM_MANAGER;
                if(api_alarm) return new CustomAlarmReceiver(context,item);
            }

            case PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE: case PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE:
            case PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE: case PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE:{
                return new BatteryReceiver(context,item);
            }

            case PublicConsts.TRIGGER_TYPE_RECEIVED_BROADCAST: return new CustomBroadcastReceiver(context,item.selectedAction,item);

            case PublicConsts.TRIGGER_TYPE_SCREEN_ON: return new CustomBroadcastReceiver(context, Intent.ACTION_SCREEN_ON,item);

            case PublicConsts.TRIGGER_TYPE_SCREEN_OFF: return new CustomBroadcastReceiver(context,Intent.ACTION_SCREEN_OFF,item);

            case PublicConsts.TRIGGER_TYPE_POWER_CONNECTED: return new CustomBroadcastReceiver(context, Intent.ACTION_POWER_CONNECTED,item);

            case PublicConsts.TRIGGER_TYPE_POWER_DISCONNECTED: return new CustomBroadcastReceiver(context,Intent.ACTION_POWER_DISCONNECTED,item);

            case PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED: case PublicConsts.TRIGGER_TYPE_WIFI_DISCONNECTED:
            case PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_ON: case PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF:
            case PublicConsts.TRIGGER_TYPE_NET_ON: case PublicConsts.TRIGGER_TYPE_NET_OFF:{
                return new NetworkReceiver(context,item);
            }

            case PublicConsts.TRIGGER_TYPE_APP_LAUNCHED: case PublicConsts.TRIGGER_TYPE_APP_CLOSED:{
                return new AppLaunchDetectionReceiver(context,item);
            }

            case PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON: case PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF:{
                return new BluetoothReceiver(context,item);
            }

            case PublicConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL: case PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE:
            case PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF:{
                return new RingModeReceiver(context,item);
            }

            case PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON: case PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF:{
                return new AirplaneModeReceiver(context,item);
            }

            case PublicConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED: case PublicConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED:{
                return new APReceiver(context,item);
            }

            case PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_IN: case PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT:{
                return new HeadsetPlugReceiver(context,item);
            }

        }
        return null;
    }



}
