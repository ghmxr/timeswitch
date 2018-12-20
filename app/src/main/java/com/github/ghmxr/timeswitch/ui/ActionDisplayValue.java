package com.github.ghmxr.timeswitch.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.utils.LogUtil;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.io.File;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class ActionDisplayValue {
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
            if(action_value==PublicConsts.ACTION_OPEN) return context.getResources().getString(R.string.open);
            else if(action_value==PublicConsts.ACTION_CLOSE) return  context.getResources().getString(R.string.close);
            else if(action_value==PublicConsts.ACTION_UNSELECTED) return "";
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.putExceptionLog(context,e);
        }
        return "";
    }

    @NonNull
    public static String getDeviceControlDisplayValue(@NonNull Context context,String value){
        try{
            int action_device=Integer.parseInt(value);
            if(action_device==PublicConsts.ACTION_DEVICECONTROL_REBOOT)  return context.getResources().getString(R.string.reboot);
            else if(action_device==PublicConsts.ACTION_DEVICECONTROL_SHUTDOWN) return context.getResources().getString(R.string.shut_down);
            else if(action_device==PublicConsts.ACTION_DEVICECONSTROL_NONE) return "";
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.putExceptionLog(context,e);
        }
        return "";
    }

    @NonNull
    public static String getRingModeDisplayValue(@NonNull Context context,String value){
        try{
            int action_ring_mode=Integer.parseInt(value);
            if(action_ring_mode==PublicConsts.ACTION_RING_VIBRATE)  return context.getResources().getString(R.string.vibrate);
            else if(action_ring_mode==PublicConsts.ACTION_RING_OFF) return context.getResources().getString(R.string.silent);
            else if(action_ring_mode==PublicConsts.ACTION_RING_NORMAL) return context.getResources().getString(R.string.ring_normal);
            else if(action_ring_mode==PublicConsts.ACTION_RING_UNSELECTED) return "";
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.putExceptionLog(context,e);
        }
        return "";
    }

    public static String getRingVolumeDisplayValue(@NonNull Context context,@NonNull String values){
        try{
            AudioManager manager=(AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            String[] volume_values=values.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            int volume_ring=Integer.parseInt(volume_values[PublicConsts.VOLUME_RING_LOCALE]);
            int volume_media=Integer.parseInt(volume_values[PublicConsts.VOLUME_MEDIA_LOCALE]);
            int volume_notification=Integer.parseInt(volume_values[PublicConsts.VOLUME_NOTIFICATION_LOCALE]);
            int volume_alarm=Integer.parseInt(volume_values[PublicConsts.VOLUME_ALARM_LOCALE]);
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
            LogUtil.putExceptionLog(context,e);
        }
        return "";
    }

    public static String getBrightnessDisplayValue(@NonNull Context context,String value){
        try{
            int action_brightness=Integer.parseInt(value);
            if(action_brightness==PublicConsts.ACTION_BRIGHTNESS_AUTO)  return context.getResources().getString(R.string.action_brightness_auto);
            else if(action_brightness>=0&&action_brightness<=PublicConsts.BRIGHTNESS_MAX)
                return context.getResources().getString(R.string.action_brightness_manual)+(int)((float)action_brightness/PublicConsts.BRIGHTNESS_MAX*100)+"%";
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.putExceptionLog(context,e);
        }
        return "";
    }

    public static String getRingSelectionDisplayValue(@NonNull Context context,@NonNull String values){
        try{
            String ring_selection_values[]=values.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            int ring_notification_selection= Integer.parseInt(ring_selection_values[PublicConsts.RING_SELECTION_NOTIFICATION_TYPE_LOCALE]);
            int ring_phone_selection=Integer.parseInt(ring_selection_values[PublicConsts.RING_SELECTION_CALL_TYPE_LOCALE]);
            StringBuilder displayBuilder=new StringBuilder("");
            if(ring_notification_selection==PublicConsts.RING_TYPE_FROM_SYSTEM ||ring_notification_selection==PublicConsts.RING_TYPE_FROM_MEDIA){
                String ringOfNotification=context.getResources().getString(R.string.activity_taskgui_actions_ring_selection_notification);
                displayBuilder.append(ringOfNotification);
                displayBuilder.append(" ");
            }
            if(ring_phone_selection==PublicConsts.RING_TYPE_FROM_SYSTEM ||ring_phone_selection==PublicConsts.RING_TYPE_FROM_MEDIA){
                String ringOfPhone=context.getResources().getString(R.string.activity_taskgui_actions_ring_selection_phone);
                displayBuilder.append(ringOfPhone);
            }
           return displayBuilder.toString();
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.putExceptionLog(context,e);
        }
        return "";
    }

    public static String getWallpaperDisplayValue(@NonNull Context context,@NonNull String value,@NonNull String uri){
        try{
            if(Integer.parseInt(value)==-1) return "";
            else {
                String filename=new File(ValueUtils.getRealPathFromUri(context, Uri.parse(uri))).getName();//wallpaper_values[1]
                if(filename.length()>25) filename=filename.substring(0,25)+"...";
                return filename;
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.putExceptionLog(context,e);
        }
        return "";
    }

    public static String getVibrateDisplayValue(@NonNull Context context,@NonNull String value){
        try{
            String[] vibrate_values=value.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            StringBuilder displayValue=new StringBuilder("");
            int frequency=Integer.parseInt(vibrate_values[PublicConsts.VIBRATE_FREQUENCY_LOCALE]);
            int duration=Integer.parseInt(vibrate_values[PublicConsts.VIBRATE_DURATION_LOCALE]);
            int interval=Integer.parseInt(vibrate_values[PublicConsts.VIBRATE_INTERVAL_LOCALE]);
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
            LogUtil.putExceptionLog(context,e);
        }
        return "";
    }

    public static String getNotificationDisplayValue(@NonNull Context context,String value){
        try{
            StringBuilder builder=new StringBuilder("");
            String[] notification_values=value.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            if(Integer.parseInt(notification_values[PublicConsts.NOTIFICATION_TYPE_LOCALE])==PublicConsts.NOTIFICATION_TYPE_UNSELECTED){
                builder.append("");
            }else if(Integer.parseInt(notification_values[PublicConsts.NOTIFICATION_TYPE_LOCALE])==PublicConsts.NOTIFICATION_TYPE_NOT_OVERRIDE){
                builder.append(context.getResources().getString(R.string.activity_taskgui_actions_notification_not_override));
                builder.append(":");
                if(Integer.parseInt(notification_values[PublicConsts.NOTIFICATION_TYPE_IF_CUSTOM_LOCALE])==PublicConsts.NOTIFICATION_TYPE_CONTENT_DEFAULT){
                    builder.append(context.getResources().getString(R.string.activity_taskgui_actions_notification_type_default));
                }else if(Integer.parseInt(notification_values[PublicConsts.NOTIFICATION_TYPE_IF_CUSTOM_LOCALE])==PublicConsts.NOTIFICATION_TYPE_CONTENT_CUSTOM){
                    builder.append(context.getResources().getString(R.string.activity_taskgui_actions_notification_type_custom));
                }
            }else if(Integer.parseInt(notification_values[PublicConsts.NOTIFICATION_TYPE_LOCALE])==PublicConsts.NOTIFICATION_TYPE_OVERRIDE_LAST){
                builder.append(context.getResources().getString(R.string.activity_taskgui_actions_notification_override_last));
                builder.append(":");
                if(Integer.parseInt(notification_values[PublicConsts.NOTIFICATION_TYPE_IF_CUSTOM_LOCALE])==PublicConsts.NOTIFICATION_TYPE_CONTENT_DEFAULT){
                    builder.append(context.getResources().getString(R.string.activity_taskgui_actions_notification_type_default));
                }else if(Integer.parseInt(notification_values[PublicConsts.NOTIFICATION_TYPE_IF_CUSTOM_LOCALE])==PublicConsts.NOTIFICATION_TYPE_CONTENT_CUSTOM){
                    builder.append(context.getResources().getString(R.string.activity_taskgui_actions_notification_type_custom));
                }
            }
            return builder.toString();
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.putExceptionLog(context,e);
        }
        return "";
    }

    public static String getSMSDisplayValue(Context context,String value){
        try{
            String sms_values[]=value.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            StringBuilder builder=new StringBuilder("");
            if(Integer.parseInt(sms_values[PublicConsts.SMS_ENABLED_LOCALE])>=0 ){
                builder.append(context.getResources().getString(R.string.activity_taskgui_actions_sms_enabled));
                if(Integer.parseInt(sms_values[PublicConsts.SMS_RESULT_TOAST_LOCALE])>=0){
                    builder.append(":");
                    builder.append(context.getResources().getString(R.string.activity_taskgui_actions_sms_receipt));
                }
            }
            return builder.toString();
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.putExceptionLog(context,e);
        }
        return "";
    }

    public static String getToastDisplayValue(@NonNull String value,@NonNull String toast){
        try{
            String toast_values[]=value.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            if(Integer.parseInt(toast_values[0])>=0) {
                if(toast.length()>15) toast=toast.substring(0,15)+"...";
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
                    int position= ProcessTaskItem.getPosition(Integer.parseInt(id));
                    if(position>=0&& TimeSwitchService.list!=null&&position<TimeSwitchService.list.size()){
                        builder.append(TimeSwitchService.list.get(position).name);
                        builder.append(" ");
                    }
                }
                String displayValue=builder.toString();
                if(displayValue.length()>15) displayValue=displayValue.substring(0,15)+"...";
                return displayValue;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }


    public static String getAppNameDisplayValue(Context context,String value){
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
                if(Integer.parseInt(packageNames[0])==-1) return "";
            }catch (Exception e){
                e.printStackTrace();
            }
            PackageManager manager=context.getPackageManager();
            for(int i=0;i<packageNames.length;i++){
                builder.append(manager.getApplicationLabel(manager.getApplicationInfo(packageNames[i],PackageManager.GET_META_DATA)));
                if(packageNames.length>1&&i<packageNames.length-1) builder.append(",");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        String return_value=builder.toString();
        if(return_value.length()>15) return_value=return_value.substring(0,15)+"...";
        return return_value;
    }
    public static boolean isGeneralItemVisible(String value){
        try{
            return Integer.parseInt(value)>=0;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static class ActionDisplayValueOfAdapter{

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
                if(action_ring==PublicConsts.ACTION_RING_NORMAL) return context.getResources().getString(R.string.action_ring_mode_normal);
                if(action_ring==PublicConsts.ACTION_RING_VIBRATE) return (context.getResources().getString(R.string.action_ring_mode_vibrate));
                if(action_ring==PublicConsts.ACTION_RING_OFF) return (context.getResources().getString(R.string.action_ring_mode_off));
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
                int volume_call=Integer.parseInt(action_volumes[PublicConsts.VOLUME_RING_LOCALE]);
                int volume_notification=Integer.parseInt(action_volumes[PublicConsts.VOLUME_NOTIFICATION_LOCALE]);
                int volume_media=Integer.parseInt(action_volumes[PublicConsts.VOLUME_MEDIA_LOCALE]);
                int volume_alarm=Integer.parseInt(action_volumes[PublicConsts.VOLUME_ALARM_LOCALE]);
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
                int action_ring_selection_call=Integer.parseInt(action_ring_selections[PublicConsts.RING_SELECTION_CALL_TYPE_LOCALE]);
                int action_ring_selection_notification=Integer.parseInt(action_ring_selections[PublicConsts.RING_SELECTION_NOTIFICATION_TYPE_LOCALE]);
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
            return getRingSelectionDisplayValue(context,item.actions[PublicConsts.ACTION_RING_SELECTION_LOCALE],item.uri_ring_notification,item.uri_ring_call);
        }

        public static String getVibrateDisplayValue(Context context,String values){
            try{
                String [] action_vibrate_values=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
                StringBuilder builder=new StringBuilder("");
                int vibrate_frequency=Integer.parseInt(action_vibrate_values[PublicConsts.VIBRATE_FREQUENCY_LOCALE]);
                if(vibrate_frequency>0){
                    int vibrate_duration=Integer.parseInt(action_vibrate_values[PublicConsts.VIBRATE_DURATION_LOCALE]);
                    int vibrate_interval=Integer.parseInt(action_vibrate_values[PublicConsts.VIBRATE_INTERVAL_LOCALE]);
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
                    if(action_brightness==PublicConsts.ACTION_BRIGHTNESS_AUTO) builder.append(R.string.adapter_action_brightness_auto);
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

        public static String getWallpapperDisplayValue(Context context,String value,String uri_wallpaper){
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
            return getWallpapperDisplayValue(context,item.actions[PublicConsts.ACTION_SET_WALL_PAPER_LOCALE],item.uri_wallpaper_desktop);
        }

        public static String getSMSDisplayValue(Context context,String values,String addresses,String message){
            try{
                String action_sms_values[]=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
                StringBuilder builder=new StringBuilder("");
                if(Integer.parseInt(action_sms_values[PublicConsts.SMS_ENABLED_LOCALE])>=0){
                    builder.append(context.getResources().getString(R.string.adapter_action_sms));
                    if(Build.VERSION.SDK_INT>=22){
                        SubscriptionInfo subinfo= null;
                        try{
                            subinfo=SubscriptionManager.from(context).getActiveSubscriptionInfo(Integer.parseInt(action_sms_values[PublicConsts.SMS_SUBINFO_LOCALE]));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
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
            return getSMSDisplayValue(context,item.actions[PublicConsts.ACTION_SMS_LOCALE],item.sms_address,item.sms_message);
        }

        public static String getToastDisplayValue(Context context,String values,String toast){
            try{
                String []toast_values=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
                int toast_type=Integer.parseInt(toast_values[0]);
                if(toast_type>=0){
                    StringBuilder builder=new StringBuilder("");
                    builder.append(context.getResources().getString(R.string.adapter_action_toast));
                    if(toast_type==PublicConsts.TOAST_TYPE_CUSTOM){
                        int toast_x_offset=Integer.parseInt(toast_values[PublicConsts.TOAST_LOCATION_X_OFFSET_LOCALE]);
                        int toast_y_offset=Integer.parseInt(toast_values[PublicConsts.TOAST_LOCATION_Y_OFFSET_LOCALE]);
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
            return getToastDisplayValue(context,item.actions[PublicConsts.ACTION_TOAST_LOCALE],item.toast);
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
                if(action_device==PublicConsts.ACTION_DEVICECONTROL_SHUTDOWN) return context.getResources().getString(R.string.action_device_shutdown);
                if(action_device==PublicConsts.ACTION_DEVICECONTROL_REBOOT) return context.getResources().getString(R.string.action_device_reboot);
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
                    builder.append(context.getResources().getString(R.string.adapter_action_notification));
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
            return getNotificationDisplayValue(context,item.actions[PublicConsts.ACTION_NOTIFICATION_LOCALE],item.notification_title,item.notification_message);
        }

        public static String getTaskNamesDisplayValue(String values){
            try{
                String [] ids=values.split(PublicConsts.SEPARATOR_SECOND_LEVEL);
                if(Integer.parseInt(ids[0])<0) return "";
                StringBuilder builder=new StringBuilder("");
                for(int i=0;i<ids.length;i++){
                    int position=ProcessTaskItem.getPosition(Integer.parseInt(ids[i]));
                    if(position>=0) builder.append(TimeSwitchService.list.get(position).name);
                    if(i<ids.length-1) builder.append(",");
                }
                return builder.toString();
            }catch (Exception e){
                e.printStackTrace();
            }
            return "";
        }

        public static String getEnableTasksDisplayValue(Context context,String values){
            try{
                String tasknames=getTaskNamesDisplayValue(values);
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
                String tasknames=getTaskNamesDisplayValue(values);
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
                    e.printStackTrace();
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

    }

}
