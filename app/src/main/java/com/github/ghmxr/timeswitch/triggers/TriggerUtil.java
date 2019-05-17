package com.github.ghmxr.timeswitch.triggers;

import android.content.Context;
import android.content.Intent;

import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.triggers.receivers.APReceiver;
import com.github.ghmxr.timeswitch.triggers.receivers.AirplaneModeReceiver;
import com.github.ghmxr.timeswitch.triggers.receivers.AppLaunchDetectionReceiver;
import com.github.ghmxr.timeswitch.triggers.receivers.BatteryReceiver;
import com.github.ghmxr.timeswitch.triggers.receivers.BluetoothReceiver;
import com.github.ghmxr.timeswitch.triggers.receivers.CustomBroadcastReceiver;
import com.github.ghmxr.timeswitch.triggers.receivers.HeadsetPlugReceiver;
import com.github.ghmxr.timeswitch.triggers.receivers.NetworkReceiver;
import com.github.ghmxr.timeswitch.triggers.receivers.RingModeReceiver;
import com.github.ghmxr.timeswitch.triggers.sensors.LightSensor;
import com.github.ghmxr.timeswitch.triggers.timers.CustomAlarmReceiver;
import com.github.ghmxr.timeswitch.triggers.timers.CustomTimerTask;

public class TriggerUtil {

    /**
     * 通过TaskItem参数获取一个Trigger实例
     * @param context context，一个service实例
     * @param item TaskItem 任务项
     * @return  根据TaskItem项参数创建的Trigger实例
     */
    public static Trigger getTriggerInstanceForTaskItem(Context context, TaskItem item){
        if(item==null) return null;
        switch (item.trigger_type){
            default:break;

            case TriggerTypeConsts.TRIGGER_TYPE_SINGLE: case TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME:
            case TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK:{
                boolean api_alarm=context.getSharedPreferences(PublicConsts.PREFERENCES_NAME,Context.MODE_PRIVATE)
                        .getInt(PublicConsts.PREFERENCES_API_TYPE,PublicConsts.PREFERENCES_API_TYPE_DEFAULT)==PublicConsts.API_ANDROID_ALARM_MANAGER;
                if(api_alarm) return new CustomAlarmReceiver(context,item);
                else return new CustomTimerTask(context,item);
            }

            case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE: case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE:
            case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE: case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE:{
                return new BatteryReceiver(context,item);
            }

            case TriggerTypeConsts.TRIGGER_TYPE_RECEIVED_BROADCAST: return new CustomBroadcastReceiver(context,item.selectedAction,item);

            case TriggerTypeConsts.TRIGGER_TYPE_SCREEN_ON: return new CustomBroadcastReceiver(context, Intent.ACTION_SCREEN_ON,item);

            case TriggerTypeConsts.TRIGGER_TYPE_SCREEN_OFF: return new CustomBroadcastReceiver(context,Intent.ACTION_SCREEN_OFF,item);

            case TriggerTypeConsts.TRIGGER_TYPE_POWER_CONNECTED: return new CustomBroadcastReceiver(context, Intent.ACTION_POWER_CONNECTED,item);

            case TriggerTypeConsts.TRIGGER_TYPE_POWER_DISCONNECTED: return new CustomBroadcastReceiver(context,Intent.ACTION_POWER_DISCONNECTED,item);

            case TriggerTypeConsts.TRIGGER_TYPE_WIFI_CONNECTED: case TriggerTypeConsts.TRIGGER_TYPE_WIFI_DISCONNECTED:
            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_ON: case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF:
            case TriggerTypeConsts.TRIGGER_TYPE_NET_ON: case TriggerTypeConsts.TRIGGER_TYPE_NET_OFF:{
                return new NetworkReceiver(context,item);
            }

            case TriggerTypeConsts.TRIGGER_TYPE_APP_LAUNCHED: case TriggerTypeConsts.TRIGGER_TYPE_APP_CLOSED:{
                return new AppLaunchDetectionReceiver(context,item);
            }

            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON: case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF:{
                return new BluetoothReceiver(context,item);
            }

            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL: case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE:
            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF:{
                return new RingModeReceiver(context,item);
            }

            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON: case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF:{
                return new AirplaneModeReceiver(context,item);
            }

            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED: case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED:{
                return new APReceiver(context,item);
            }

            case TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_IN: case TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT:{
                return new HeadsetPlugReceiver(context,item);
            }
            case TriggerTypeConsts.TRIGGER_TYPE_LIGHT_SENSOR_HIGHER_THAN:case TriggerTypeConsts.TRIGGER_TYPE_LIGHT_SENSOR_LOWER_THAN:{
                return new LightSensor(context,item);
            }

        }
        return null;
    }



}
