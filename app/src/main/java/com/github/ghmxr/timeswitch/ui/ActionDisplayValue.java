package com.github.ghmxr.timeswitch.ui;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.PublicConsts;
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
            }else if(Integer.parseInt(notification_values[PublicConsts.NOTIFICATION_TYPE_LOCALE])==PublicConsts.NOTIFICATION_TYPE_VIBRATE){
                builder.append(context.getResources().getString(R.string.activity_taskgui_actions_notification_vibrate));
                builder.append(":");
                if(Integer.parseInt(notification_values[PublicConsts.NOTIFICATION_TYPE_IF_CUSTOM_LOCALE])==PublicConsts.NOTIFICATION_TYPE_DEFAULT){
                    builder.append(context.getResources().getString(R.string.activity_taskgui_actions_notification_type_default));
                }else if(Integer.parseInt(notification_values[PublicConsts.NOTIFICATION_TYPE_IF_CUSTOM_LOCALE])==PublicConsts.NOTIFICATION_TYPE_CUSTOM){
                    builder.append(context.getResources().getString(R.string.activity_taskgui_actions_notification_type_custom));
                }
            }else if(Integer.parseInt(notification_values[PublicConsts.NOTIFICATION_TYPE_LOCALE])==PublicConsts.NOTIFICATION_TYPE_NO_VIBRATE){
                builder.append(context.getResources().getString(R.string.activity_taskgui_actions_notification_no_vibrate));
                builder.append(":");
                if(Integer.parseInt(notification_values[PublicConsts.NOTIFICATION_TYPE_IF_CUSTOM_LOCALE])==PublicConsts.NOTIFICATION_TYPE_DEFAULT){
                    builder.append(context.getResources().getString(R.string.activity_taskgui_actions_notification_type_default));
                }else if(Integer.parseInt(notification_values[PublicConsts.NOTIFICATION_TYPE_IF_CUSTOM_LOCALE])==PublicConsts.NOTIFICATION_TYPE_CUSTOM){
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

    public static boolean isGeneralItemVisible(String value){
        try{
            return Integer.parseInt(value)>=0;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

}
