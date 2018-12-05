package com.github.ghmxr.timeswitch.adapters;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.activities.Main;
import com.github.ghmxr.timeswitch.activities.Triggers;
import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.ui.ActionDisplayValue;
import com.github.ghmxr.timeswitch.utils.DisplayDensity;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.util.Calendar;
import java.util.List;

import static com.github.ghmxr.timeswitch.activities.Triggers.getWeekLoopDisplayValue;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class MainListAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    private List<TaskItem> list;
    SwitchChangedListener mlistener;
    boolean isMultiSelectMode=false;
    boolean [] isSelected;
    private View[] views;
    //private static final int ICON_COUNT_LIMIT=7;
    private int taskCount;


    public MainListAdapter (Context context, @NonNull List<TaskItem> list){
        this.context=context;
        inflater=LayoutInflater.from(context);
        this.list= list;
        taskCount=list.size();
        isSelected=new boolean[taskCount];
        views=new View[taskCount];
    }

    @Override
    public int getCount() {
        return taskCount+2;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if(i>=list.size()){
            View blank_view=inflater.inflate(R.layout.item_task,viewGroup,false);
            blank_view.setVisibility(View.INVISIBLE);
            return blank_view;
        }

        TaskItem item=null;
        try{
            item=list.get(i);
        }catch (Exception e){
            e.printStackTrace();
        }

        if(item==null) return null;

        //ViewHolder holder;

        if(views[i]==null){
            views[i]=inflater.inflate(R.layout.item_task,viewGroup,false);
            /*holder=new ViewHolder();
            holder.root=views[i].findViewById(R.id.item_task_root);
            holder.img=views[i].findViewById(R.id.item_task_trigger_icon);
            holder.trigger_value =views[i].findViewById(R.id.item_task_trigger_value);
            holder.task_name =views[i].findViewById(R.id.item_task_name);
            holder.icon_wifi_on=views[i].findViewById(R.id.item_task_actions_wifi_on);
            holder.icon_wifi_off=views[i].findViewById(R.id.item_task_actions_wifi_off);
            holder.icon_bluetooth_on=views[i].findViewById(R.id.item_task_actions_bluetooth_on);
            holder.icon_bluetooth_off=views[i].findViewById(R.id.item_task_actions_bluetooth_off);
            holder.icon_net_on=views[i].findViewById(R.id.item_task_actions_net_on);
            holder.icon_net_off=views[i].findViewById(R.id.item_task_actions_net_off);
            holder.icon_ring_vibrate=views[i].findViewById(R.id.item_task_actions_ring_vibrate);
            holder.icon_ring_off=views[i].findViewById(R.id.item_task_actions_ring_off);
            holder.icon_ring_normal=views[i].findViewById(R.id.item_task_actions_ring_normal);
            holder.icon_ring_volume=views[i].findViewById(R.id.item_task_actions_ring_volume);
            holder.icon_ring_selection=views[i].findViewById(R.id.item_task_actions_ring_selection);
            holder.icon_notification=views[i].findViewById(R.id.item_task_actions_notification);
            holder.icon_toast=views[i].findViewById(R.id.item_task_actions_toast);
            holder.icon_wallpaper=views[i].findViewById(R.id.item_task_actions_wallpaper);
            holder.icon_sms=views[i].findViewById(R.id.item_task_actions_sms);
            holder.icon_vibrate=views[i].findViewById(R.id.item_task_actions_vibrate);
            holder.icon_device_reboot=views[i].findViewById(R.id.item_task_actions_device_reboot);
            holder.icon_device_shutdown=views[i].findViewById(R.id.item_task_actions_device_shutdown);
            holder.icon_brightness_auto=views[i].findViewById(R.id.item_task_actions_brightness_auto);
            holder.icon_brightness_manual=views[i].findViewById(R.id.item_task_actions_brightness_manual);
            holder.icon_gps_on=views[i].findViewById(R.id.item_task_actions_gps_on);
            holder.icon_gps_off=views[i].findViewById(R.id.item_task_actions_gps_off);
            holder.icon_airplane_mode_on=views[i].findViewById(R.id.item_task_actions_airplane_mode_on);
            holder.icon_airplane_mode_off=views[i].findViewById(R.id.item_task_actions_airplane_mode_off);
            holder.icon_enable=views[i].findViewById(R.id.item_task_actions_enable);
            holder.icon_disable=views[i].findViewById(R.id.item_task_actions_disable);
            holder.tv_more =views[i].findViewById(R.id.item_task_actions_more);
            holder.aSwitch=views[i].findViewById(R.id.item_task_switch);
            holder.checkbox=views[i].findViewById(R.id.item_task_checkbox);
            views[i].setTag(holder);*/
        }

        views[i].setVisibility(View.VISIBLE);

        try{
            TextView tv_name=views[i].findViewById(R.id.item_task_name);
            tv_name.setText(item.name);
            int color=Color.parseColor(item.addition_title_color);
            ((views[i].findViewById(R.id.item_task_title))).setBackgroundColor(color);
            //if(color>125*125*125) tv_name.setTextColor(context.getResources().getColor(R.color.color_black));

        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            switch(item.trigger_type){
                default:break;
                case PublicConsts.TRIGGER_TYPE_SINGLE:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_repeat_single);
                    Calendar calendar=Calendar.getInstance();
                    calendar.setTimeInMillis(item.time);
                    int month=calendar.get(Calendar.MONTH)+1;
                    getTriggerTextView(i).setText( calendar.get(Calendar.YEAR)
                            +"/"+ValueUtils.format(month)+"/"+ValueUtils.format(calendar.get(Calendar.DAY_OF_MONTH))+"/"+ValueUtils.getDayOfWeek(list.get(i).time)+"/"
                            +ValueUtils.format(calendar.get(Calendar.HOUR_OF_DAY))+":"+ ValueUtils.format(calendar.get(Calendar.MINUTE)));
                }
                break;
                case PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_repeat_percertaintime);
                    refreshAllCertainTimeTaskItems();
                }
                break;
                case PublicConsts.TRIGGER_TYPE_LOOP_WEEK:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_repeat_weekloop);
                    Calendar calendar=Calendar.getInstance();
                    calendar.setTimeInMillis(item.time);
                    getTriggerTextView(i).setText(getWeekLoopDisplayValue(context,list.get(i).week_repeat,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE)));
                    //holder.trigger_value.setText();
                }
                break;
                case PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_battery_high);
                    getTriggerTextView(i).setText(context.getResources().getString(R.string.more_than)+item.battery_percentage+"%");
                }
                break;
                case PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_battery_low);
                    getTriggerTextView(i).setText(context.getResources().getString(R.string.less_than)+item.battery_percentage+"%");
                }
                break;
                case PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_temperature);
                    getTriggerTextView(i).setText(context.getResources().getString(R.string.higher_than)+item.battery_temperature+"°Ê");
                }
                break;
                case PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_temperature);
                    getTriggerTextView(i).setText(context.getResources().getString(R.string.lower_than)+item.battery_temperature+"°Ê");
                }
                break;
                case PublicConsts.TRIGGER_TYPE_RECEIVED_BROADCAST:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_broadcast);
                    getTriggerTextView(i).setText(item.selectedAction);
                }
                break;
                case PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_wifi_connected);
                    //holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                    String ssidinfo= Triggers.getWifiConnectionDisplayValue(context,item.wifiIds);
                    //if(ssidinfo.length()>16) ssidinfo=ssidinfo.substring(0,16)+"...";
                    getTriggerTextView(i).setText(ssidinfo);
                }
                break;
                case PublicConsts.TRIGGER_TYPE_WIFI_DISCONNECTED:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_wifi_disconnected);
                    //holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                    String ssidinfo= Triggers.getWifiConnectionDisplayValue(context,item.wifiIds);
                    //if(ssidinfo.length()>16) ssidinfo=ssidinfo.substring(0,16)+"...";
                    getTriggerTextView(i).setText(ssidinfo);
                }
                break;
                case PublicConsts.TRIGGER_TYPE_APP_LAUNCHED:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_app_launch);
                    String names=Triggers.getAppNameDisplayValue(context,item.package_names);
                    //if(names.length()>16) names=names.substring(0,16);
                   getTriggerTextView(i).setText(names);
                }
                break;
                case PublicConsts.TRIGGER_TYPE_APP_CLOSED:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_app_stop);
                    String names=Triggers.getAppNameDisplayValue(context,item.package_names);
                    //if(names.length()>16) names=names.substring(0,16);
                    getTriggerTextView(i).setText(names);
                }
                break;
                case PublicConsts.TRIGGER_TYPE_SCREEN_ON:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_screen_on);
                    getTriggerTextView(i).setText(context.getResources().getString(R.string.activity_triggers_screen_on));
                }
                break;
                case PublicConsts.TRIGGER_TYPE_SCREEN_OFF:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_screen_off);
                    getTriggerTextView(i).setText(context.getResources().getString(R.string.activity_triggers_screen_off));
                }
                break;
                case PublicConsts.TRIGGER_TYPE_POWER_CONNECTED:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_power_connected);
                    getTriggerTextView(i).setText(context.getResources().getString(R.string.activity_triggers_power_connected));
                }
                break;
                case PublicConsts.TRIGGER_TYPE_POWER_DISCONNECTED:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_power_disconnected);
                    getTriggerTextView(i).setText(context.getResources().getString(R.string.activity_triggers_power_disconnected));
                }
                break;
                case PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_ON:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_wifi_on);
                    getTriggerTextView(i).setText(Triggers.getWidgetDisplayValue(context,item.trigger_type));
                }
                break;
                case PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_wifi_off);
                    getTriggerTextView(i).setText(Triggers.getWidgetDisplayValue(context,item.trigger_type));
                }
                break;
                case PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_bluetooth_on);
                    getTriggerTextView(i).setText(Triggers.getWidgetDisplayValue(context,item.trigger_type));
                }
                break;
                case PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_bluetooth_off);
                    getTriggerTextView(i).setText(Triggers.getWidgetDisplayValue(context,item.trigger_type));
                }
                break;
                case PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_ring_off);
                    getTriggerTextView(i).setText(Triggers.getWidgetDisplayValue(context,item.trigger_type));
                }
                break;
                case PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_ring_vibrate);
                    getTriggerTextView(i).setText(Triggers.getWidgetDisplayValue(context,item.trigger_type));
                }
                break;
                case PublicConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_ring_normal);
                    getTriggerTextView(i).setText(Triggers.getWidgetDisplayValue(context,item.trigger_type));
                }
                break;
                case PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_airplanemode_on);
                    getTriggerTextView(i).setText(Triggers.getWidgetDisplayValue(context,item.trigger_type));
                }
                break;
                case PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_airplanemode_off);
                    getTriggerTextView(i).setText(Triggers.getWidgetDisplayValue(context,item.trigger_type));
                }
                break;
                case PublicConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_ap_on);
                    getTriggerTextView(i).setText(Triggers.getWidgetDisplayValue(context,item.trigger_type));
                }
                break;
                case PublicConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_ap_off);
                    getTriggerTextView(i).setText(Triggers.getWidgetDisplayValue(context,item.trigger_type));
                }
                break;
                case PublicConsts.TRIGGER_TYPE_NET_ON:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_cellular_on);
                    getTriggerTextView(i).setText(Triggers.getWidgetDisplayValue(context,item.trigger_type));
                }
                break;
                case PublicConsts.TRIGGER_TYPE_NET_OFF:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_cellular_off);
                    getTriggerTextView(i).setText(Triggers.getWidgetDisplayValue(context,item.trigger_type));
                }
                break;
                case PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_IN:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_headset);
                    getTriggerTextView(i).setText(context.getResources().getString(R.string.activity_trigger_headset_plug_in));
                }
                break;
                case PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT:{
                    getTriggerImageView(i).setImageResource(R.drawable.icon_headset);
                    getTriggerTextView(i).setText(context.getResources().getString(R.string.activity_trigger_headset_plug_out));
                }
                break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            ((TextView)views[i].findViewById(R.id.item_task_exception)).setText(getExceptionValue(item));
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            ((TextView)views[i].findViewById(R.id.item_task_action)).setText(getActionValue(item));
        }catch (Exception e){
            e.printStackTrace();
        }

        String [] actions =item.actions;
        /*try{
            int showCount=0,actionNum=0;
            if(Integer.parseInt(actions[PublicConsts.ACTION_WIFI_LOCALE])==PublicConsts.ACTION_OPEN){
                //holder.actions.addView(this.newActionIcon(R.drawable.icon_wifi_on));
                holder.icon_wifi_on.setVisibility(View.VISIBLE);
                showCount++;
                actionNum++;
                holder.icon_wifi_off.setVisibility(View.GONE);
            }else if(Integer.parseInt(actions[PublicConsts.ACTION_WIFI_LOCALE])==PublicConsts.ACTION_CLOSE){
                //  holder.actions.addView(this.newActionIcon(R.drawable.icon_wifi_off));
                holder.icon_wifi_on.setVisibility(View.GONE);
                holder.icon_wifi_off.setVisibility(View.VISIBLE);
                showCount++;
                actionNum++;
            }else{
                holder.icon_wifi_on.setVisibility(View.GONE);
                holder.icon_wifi_off.setVisibility(View.GONE);
            }

            if(Integer.parseInt(actions[PublicConsts.ACTION_BLUETOOTH_LOCALE])==PublicConsts.ACTION_OPEN){
                //holder.actions.addView(this.newActionIcon(R.drawable.icon_bluetooth_on));
                holder.icon_bluetooth_on.setVisibility(View.VISIBLE);
                showCount++;actionNum++;
                holder.icon_bluetooth_off.setVisibility(View.GONE);
            }else if(Integer.parseInt(actions[PublicConsts.ACTION_BLUETOOTH_LOCALE])==PublicConsts.ACTION_CLOSE){
                // holder.actions.addView(this.newActionIcon(R.drawable.icon_bluetooth_off));
                holder.icon_bluetooth_on.setVisibility(View.GONE);
                holder.icon_bluetooth_off.setVisibility(View.VISIBLE);
                showCount++;actionNum++;
            }else{
                holder.icon_bluetooth_on.setVisibility(View.GONE);
                holder.icon_bluetooth_off.setVisibility(View.GONE);
            }

            if(Integer.parseInt(actions[PublicConsts.ACTION_RING_MODE_LOCALE])==PublicConsts.ACTION_RING_VIBRATE){
                // holder.actions.addView(this.newActionIcon(R.drawable.icon_ring_vibrate));
                holder.icon_ring_vibrate.setVisibility(View.VISIBLE);
                showCount++;actionNum++;
                holder.icon_ring_off.setVisibility(View.GONE);
                holder.icon_ring_normal.setVisibility(View.GONE);
            }else if(Integer.parseInt(actions[PublicConsts.ACTION_RING_MODE_LOCALE])==PublicConsts.ACTION_RING_OFF){
                // holder.actions.addView(this.newActionIcon(R.drawable.icon_ring_off));
                holder.icon_ring_vibrate.setVisibility(View.GONE);
                holder.icon_ring_off.setVisibility(View.VISIBLE);
                showCount++;actionNum++;
                holder.icon_ring_normal.setVisibility(View.GONE);
            }else if(Integer.parseInt(actions[PublicConsts.ACTION_RING_MODE_LOCALE])==PublicConsts.ACTION_RING_NORMAL){
                // holder.actions.addView(this.newActionIcon(R.drawable.icon_ring_normal));
                holder.icon_ring_vibrate.setVisibility(View.GONE);
                holder.icon_ring_off.setVisibility(View.GONE);
                holder.icon_ring_normal.setVisibility(View.VISIBLE);
                showCount++;actionNum++;
            }else{
                holder.icon_ring_vibrate.setVisibility(View.GONE);
                holder.icon_ring_off.setVisibility(View.GONE);
                holder.icon_ring_normal.setVisibility(View.GONE);
            }

            if(Integer.parseInt(actions[PublicConsts.ACTION_BRIGHTNESS_LOCALE])==PublicConsts.ACTION_BRIGHTNESS_AUTO){
                holder.icon_brightness_auto.setVisibility(View.VISIBLE);
                showCount++;actionNum++;
                holder.icon_brightness_manual.setVisibility(View.GONE);
            }else if(Integer.parseInt(actions[PublicConsts.ACTION_BRIGHTNESS_LOCALE])>=0&&Integer.parseInt(actions[PublicConsts.ACTION_BRIGHTNESS_LOCALE])<=PublicConsts.BRIGHTNESS_MAX){
                holder.icon_brightness_auto.setVisibility(View.GONE);
                holder.icon_brightness_manual.setVisibility(View.VISIBLE);
                showCount++;actionNum++;
            }else{
                holder.icon_brightness_auto.setVisibility(View.GONE);
                holder.icon_brightness_manual.setVisibility(View.GONE);
            }


            String[] ring_volumes=actions[PublicConsts.ACTION_RING_VOLUME_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            if(Integer.parseInt(ring_volumes[PublicConsts.VOLUME_RING_LOCALE])>=0||Integer.parseInt(ring_volumes[PublicConsts.VOLUME_MEDIA_LOCALE])>=0
                   ||Integer.parseInt(ring_volumes[PublicConsts.VOLUME_NOTIFICATION_LOCALE])>=0||Integer.parseInt(ring_volumes[PublicConsts.VOLUME_ALARM_LOCALE])>=0){

                holder.icon_ring_volume.setVisibility(View.VISIBLE);
                showCount++;actionNum++;
            }
            else holder.icon_ring_volume.setVisibility(View.GONE);

            String [] ring_selections=actions[PublicConsts.ACTION_RING_SELECTION_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            if(Integer.parseInt(ring_selections[PublicConsts.RING_SELECTION_NOTIFICATION_TYPE_LOCALE])>=0||Integer.parseInt(ring_selections[PublicConsts.RING_SELECTION_CALL_TYPE_LOCALE])>=0){
                actionNum++;
                if(showCount<ICON_COUNT_LIMIT){
                    holder.icon_ring_selection.setVisibility(View.VISIBLE);
                    showCount++;
                }
            }
            else holder.icon_ring_selection.setVisibility(View.GONE);

            String[] wallpaper_values=actions[PublicConsts.ACTION_SET_WALL_PAPER_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            if(Integer.parseInt(wallpaper_values[0])>=0){
                actionNum++;
                if(showCount<ICON_COUNT_LIMIT){
                    holder.icon_wallpaper.setVisibility(View.VISIBLE);
                    showCount++;
                }

            }else {
                holder.icon_wallpaper.setVisibility(View.GONE);
            }

            String[] notification_values=actions[PublicConsts.ACTION_NOTIFICATION_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            if(Integer.parseInt(notification_values[PublicConsts.NOTIFICATION_TYPE_LOCALE])>=0){
                actionNum++;
                if(showCount<ICON_COUNT_LIMIT){
                    holder.icon_notification.setVisibility(View.VISIBLE);
                    showCount++;
                }
            }else {
                holder.icon_notification.setVisibility(View.GONE);
            }

            String[] toast_values=actions[PublicConsts.ACTION_TOAST_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            if(Integer.parseInt(toast_values[PublicConsts.TOAST_TYPE_LOCALE])>=0){
                actionNum++;
                if(showCount<ICON_COUNT_LIMIT){
                    holder.icon_toast.setVisibility(View.VISIBLE);
                    showCount++;
                }
            }
            else{
                holder.icon_toast.setVisibility(View.GONE);
            }

            String sms_values[] = actions[PublicConsts.ACTION_SMS_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL);
            if(Integer.parseInt(sms_values[PublicConsts.SMS_ENABLED_LOCALE])>=0){
                actionNum++;
                if(showCount<ICON_COUNT_LIMIT){
                    holder.icon_sms.setVisibility(View.VISIBLE);
                    showCount++;
                }
                else holder.icon_sms.setVisibility(View.GONE);
            }

            String[] vibrate_values=actions[PublicConsts.ACTION_VIBRATE_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            if(Integer.parseInt(vibrate_values[PublicConsts.VIBRATE_FREQUENCY_LOCALE])>=0){
                actionNum++;
                if(showCount<ICON_COUNT_LIMIT){
                    holder.icon_vibrate.setVisibility(View.VISIBLE);
                    showCount++;
                }
            }else {
                holder.icon_vibrate.setVisibility(View.GONE);
            }

            if(Integer.parseInt(actions[PublicConsts.ACTION_GPS_LOCALE])==PublicConsts.ACTION_OPEN){
                actionNum++;
                if(showCount<ICON_COUNT_LIMIT){
                    holder.icon_gps_on.setVisibility(View.VISIBLE);
                    showCount++;
                }
                holder.icon_gps_off.setVisibility(View.GONE);
            }else if(Integer.parseInt(actions[PublicConsts.ACTION_GPS_LOCALE])==PublicConsts.ACTION_CLOSE){
                holder.icon_gps_on.setVisibility(View.GONE);
                actionNum++;
                if(showCount<ICON_COUNT_LIMIT){
                    holder.icon_gps_off.setVisibility(View.VISIBLE);
                    showCount++;
                }
            }else{
                holder.icon_gps_on.setVisibility(View.GONE);
                holder.icon_gps_off.setVisibility(View.GONE);
            }

            if(Integer.parseInt(actions[PublicConsts.ACTION_AIRPLANE_MODE_LOCALE])==PublicConsts.ACTION_OPEN){
                actionNum++;
                if(showCount<ICON_COUNT_LIMIT){
                    holder.icon_airplane_mode_on.setVisibility(View.VISIBLE);
                    showCount++;
                }
                holder.icon_airplane_mode_off.setVisibility(View.GONE);
            }else if(Integer.parseInt(actions[PublicConsts.ACTION_AIRPLANE_MODE_LOCALE])==PublicConsts.ACTION_CLOSE){
                actionNum++;
                holder.icon_airplane_mode_on.setVisibility(View.GONE);
                if(showCount<ICON_COUNT_LIMIT){
                    holder.icon_airplane_mode_off.setVisibility(View.VISIBLE);
                    showCount++;
                }
            }else{
                holder.icon_airplane_mode_on.setVisibility(View.GONE);
                holder.icon_airplane_mode_off.setVisibility(View.GONE);
            }

            if(Integer.parseInt(actions[PublicConsts.ACTION_NET_LOCALE])==PublicConsts.ACTION_OPEN){
                actionNum++;
                //holder.actions.addView(this.newActionIcon(R.drawable.icon_celluar_on));
                if(showCount<ICON_COUNT_LIMIT){
                    holder.icon_net_on.setVisibility(View.VISIBLE);
                    showCount++;
                }
                holder.icon_net_off.setVisibility(View.GONE);
            }else if(Integer.parseInt(actions[PublicConsts.ACTION_NET_LOCALE])==PublicConsts.ACTION_CLOSE){
                actionNum++;
                //holder.actions.addView(this.newActionIcon(R.drawable.icon_cellular_off));
                holder.icon_net_on.setVisibility(View.GONE);
                if(showCount<ICON_COUNT_LIMIT){
                    holder.icon_net_off.setVisibility(View.VISIBLE);
                    showCount++;
                }

            }else {
                holder.icon_net_on.setVisibility(View.GONE);
                holder.icon_net_off.setVisibility(View.GONE);
            }

            if(Integer.parseInt(actions[PublicConsts.ACTION_DEVICECONTROL_LOCALE])==PublicConsts.ACTION_DEVICECONTROL_REBOOT){
                actionNum++;
                // holder.actions.addView(this.newActionIcon(R.drawable.icon_reboot));
                if(showCount<=ICON_COUNT_LIMIT){
                    holder.icon_device_reboot.setVisibility(View.VISIBLE);
                    showCount++;
                }
                holder.icon_device_shutdown.setVisibility(View.GONE);
            }else if(Integer.parseInt(actions[PublicConsts.ACTION_DEVICECONTROL_LOCALE])==PublicConsts.ACTION_DEVICECONTROL_SHUTDOWN){
                actionNum++;
                // holder.actions.addView(this.newActionIcon(R.drawable.icon_power));
                holder.icon_device_reboot.setVisibility(View.GONE);
                if(showCount<ICON_COUNT_LIMIT){
                    holder.icon_device_shutdown.setVisibility(View.VISIBLE);
                    showCount++;
                }
            }else{
                holder.icon_device_reboot.setVisibility(View.GONE);
                holder.icon_device_shutdown.setVisibility(View.GONE);
            }

            if(Integer.parseInt(actions[PublicConsts.ACTION_ENABLE_TASKS_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL)[0])>=0){
                actionNum++;
                if(showCount<ICON_COUNT_LIMIT){
                    holder.icon_enable.setVisibility(View.VISIBLE);
                    showCount++;
                }else{
                    holder.icon_disable.setVisibility(View.GONE);
                }
            }

            if(Integer.parseInt(actions[PublicConsts.ACTION_DISABLE_TASKS_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL)[0])>=0){
                actionNum++;
                if(showCount<ICON_COUNT_LIMIT){
                    holder.icon_disable.setVisibility(View.VISIBLE);
                    showCount++;
                }else {
                    holder.icon_disable.setVisibility(View.GONE);
                }
            }

            if(actionNum>ICON_COUNT_LIMIT) holder.tv_more.setVisibility(View.VISIBLE);
            else  holder.tv_more.setVisibility(View.GONE);

            //Log.d("ADAPTER_ICON_COUNT",""+showCount);
           // Log.d("ADAPTER_ACTION_NUM",""+actionNum);
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.putExceptionLog(context,e);
        }  */

        views[i].findViewById(R.id.item_task_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(views[i].findViewById(R.id.item_task_info).getVisibility()!=View.VISIBLE){
                    views[i].findViewById(R.id.item_task_info).setVisibility(View.VISIBLE);
                    (views[i].findViewById(R.id.item_task_title_arrow)).setRotation(90);
                }else {
                    views[i].findViewById(R.id.item_task_info).setVisibility(View.GONE);
                    (views[i].findViewById(R.id.item_task_title_arrow)).setRotation(0);
                }
            }
        });

        CheckBox cb=views[i].findViewById(R.id.item_task_checkbox);
        SwitchCompat switchCompat=views[i].findViewById(R.id.item_task_switch);
        if(isMultiSelectMode) {
            switchCompat.setVisibility(View.GONE);
            cb.setVisibility(View.VISIBLE);
            cb.setOnCheckedChangeListener(null);
            cb.setChecked(isSelected[i]);
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    isSelected[i]=isChecked;
                    //notifyDataSetChanged();
                }
            });
            views[i].findViewById(R.id.item_task_title).setOnLongClickListener(null);
        }else{
            switchCompat.setVisibility(View.VISIBLE);
            cb.setVisibility(View.GONE);
            switchCompat.setOnCheckedChangeListener(null);
            switchCompat.setChecked(item.isenabled);
            switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(mlistener!=null) mlistener.onCheckedChanged(i,b);
                    refreshAllCertainTimeTaskItems();
                }
            });
            views[i].findViewById(R.id.item_task_title).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Message msg=new Message();
                    msg.what= Main.MESSAGE_OPEN_MULTI_SELECT_MODE;
                    msg.obj=i;
                    Main.sendMessage(msg);
                    return true;
                }
            });
        }

        return views[i];
    }

    private ImageView getTriggerImageView(int position){
        return ((ImageView)views[position].findViewById(R.id.item_task_trigger_icon));
    }

    private TextView getTriggerTextView(int position){
        return  ((TextView)views[position].findViewById(R.id.item_task_trigger_value));
    }

    public void onDataSetChanged(@NonNull List<TaskItem> list){
        this.list=list;
        taskCount=list.size();
        this.isSelected=new boolean[taskCount];
        this.views=new View[taskCount];
        this.notifyDataSetChanged();
    }

    public void refreshAllCertainTimeTaskItems(){
        if(views==null) return;
        for(int i=0;i<views.length;i++){
            //if(i>=list.size()) break;
            if(views[i]==null) continue;
            TextView tv=views[i].findViewById(R.id.item_task_trigger_value);
            TaskItem item=null;
            //if(i>=list.size()) continue;
            try{
                item=list.get(i);
            }catch (Exception e){
                e.printStackTrace();
                //LogUtil.putExceptionLog(context,e);
            }
            if(item==null) continue;
            if(item.trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME){
                long remaining=list.get(i).getNextTriggeringTime()-System.currentTimeMillis();
                if(remaining<=0) remaining=0;
                int day=(int)(remaining/(1000*60*60*24));
                int hour=(int)((remaining%(1000*60*60*24))/(1000*60*60));
                int minute=(int)((remaining%(1000*60*60))/(1000*60));
                int second=(int)((remaining%(1000*60))/1000);
                String display;
                if(day>0){
                    display=day+":"+ValueUtils.format(hour)+":"+ValueUtils.format(minute)+":"+ValueUtils.format(second);
                }else if(hour>0){
                    display=ValueUtils.format(hour)+":"+ValueUtils.format(minute)+":"+ValueUtils.format(second);
                }else if(minute>0){
                    display=ValueUtils.format(minute)+":"+ValueUtils.format(second);
                }else{
                    display=ValueUtils.format(second)+"s";
                }
                if(item.isenabled) {
                    tv.setText(display);
                }
                else tv.setText("Off");
            }
        }

    }

    public void onMultiSelectModeItemClicked(int position){
        if(position>=isSelected.length||position<0) return;
        isSelected[position]=!isSelected[position];
        this.notifyDataSetChanged();
    }

    public boolean[] getIsSelected(){
        return this.isSelected;
    }

    public void setOnSwitchChangedListener(SwitchChangedListener listener){
        this.mlistener=listener;
    }

    public void openMultiSelecteMode(int longclickposition){
        this.isMultiSelectMode=true;
        isSelected=new boolean[list.size()];
        isSelected[longclickposition]=true;
        this.notifyDataSetChanged();
    }

    public void closeMultiSelectMode(){
        this.isMultiSelectMode=false;
        this.notifyDataSetChanged();
    }

    public void selectAll(){
        for(int i=0;i<isSelected.length;i++){
            isSelected[i]=true;
        }
        this.notifyDataSetChanged();
    }

    public void deselectAll(){
        for(int i=0;i<isSelected.length;i++){
            isSelected[i]=false;
        }
        this.notifyDataSetChanged();
    }

    private String getExceptionValue(TaskItem item){
        try{
            StringBuilder builder=new StringBuilder("");
            String [] exceptions=item.exceptions;
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_LOCKEDSCREEN])==1){
                builder.append(context.getResources().getString(R.string.activity_taskgui_exception_screen_locked));
            }
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_UNLOCKEDSCREEN])==1){
                builder.append(context.getResources().getString(R.string.activity_taskgui_exception_screen_unlocked));
            }
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_WIFI_ENABLED])==1){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.activity_taskgui_exception_wifi_enabled));
            }
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_WIFI_DISABLED])==1){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.activity_taskgui_exception_wifi_disabled));
            }

            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BLUETOOTH_ENABLED])==1){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.activity_taskgui_exception_bluetooth_enabled));
            }
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BLUETOOTH_DISABLED])==1){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.activity_taskgui_exception_bluetooth_disabled));
            }
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_RING_VIBRATE])==1){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.activity_taskgui_exception_ring_vibrate));
            }
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_RING_OFF])==1){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.activity_taskgui_exception_ring_off));
            }
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_RING_NORMAL])==1){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.activity_taskgui_exception_ring_normal));
            }
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_NET_ENABLED])==1){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.activity_taskgui_exception_net_enabled));
            }
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_NET_DISABLED])==1){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.activity_taskgui_exception_net_disabled));
            }
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_GPS_ENABLED])==1){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.activity_taskgui_exception_gps_on));
            }
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_GPS_DISABLED])==1){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.activity_taskgui_exception_gps_off));
            }
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_AIRPLANE_MODE_ENABLED])==1){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.activity_taskgui_exception_airplanemode_on));
            }
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_AIRPLANE_MODE_DISABLED])==1){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.activity_taskgui_exception_airplanemode_off));
            }
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_MONDAY])==1){
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
            }
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_START_TIME])>=0&&Integer.parseInt(exceptions[PublicConsts.EXCEPTION_END_TIME])>=0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.log_exceptions_period));
                builder.append(ValueUtils.timePeriodFormatValue(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_START_TIME]),Integer.parseInt(exceptions[PublicConsts.EXCEPTION_END_TIME])));
            }
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE])>=0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.log_exceptions_battery_percentage_less_than));
                builder.append(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]));
                builder.append("%");
            }
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE])>=0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.log_exceptions_battery_percentage_more_than));
                builder.append(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]));
                builder.append("%");
            }
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE])>=0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.log_exceptions_battery_temperature_higher_than));
                builder.append(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]));
                builder.append(context.getResources().getString(R.string.degree_celsius));
            }
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE])>=0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.log_exceptions_battery_temperature_lower_than));
                builder.append(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]));
                builder.append(context.getResources().getString(R.string.degree_celsius));
            }

            String returnValue=builder.toString();
            if(returnValue.equals("")) return context.getResources().getString(R.string.word_nothing);
            return returnValue;
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    private String getActionValue(TaskItem item){
        try{
            String[]actions=item.actions;
            StringBuilder builder=new StringBuilder("");

            /*int action_wifi=Integer.parseInt(actions[PublicConsts.ACTION_WIFI_LOCALE]);
            if(action_wifi>=0){
                if(action_wifi==0) builder.append(context.getResources().getString(R.string.action_wifi_open));
                if(action_wifi==1) builder.append(context.getResources().getString(R.string.action_wifi_close));
            }*/
            builder.append(ActionDisplayValue.ActionDisplayValueOfAdapter.getWifiDisplayValue(context,actions[PublicConsts.ACTION_WIFI_LOCALE]));

            /*int action_bluetooth=Integer.parseInt(actions[PublicConsts.ACTION_BLUETOOTH_LOCALE]);
            if(action_bluetooth>=0){
                if(builder.toString().length()>0)builder.append(",");
                if(action_bluetooth==0) builder.append(context.getResources().getString(R.string.action_bluetooth_close));
                if(action_bluetooth==1) builder.append(context.getResources().getString(R.string.action_bluetooth_open));
            }*/
            String value_bluetooth=ActionDisplayValue.ActionDisplayValueOfAdapter.getBluetoothDisplayValue(context,actions[PublicConsts.ACTION_BLUETOOTH_LOCALE]);
            if(value_bluetooth.length()>0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(value_bluetooth);
            }


            /*int action_ring=Integer.parseInt(actions[PublicConsts.ACTION_RING_MODE_LOCALE]);
            if(action_ring>=0){
                if(builder.toString().length()>0) builder.append(",");
                if(action_ring==PublicConsts.ACTION_RING_NORMAL) builder.append(context.getResources().getString(R.string.action_ring_mode_normal));
                if(action_ring==PublicConsts.ACTION_RING_VIBRATE)builder.append(context.getResources().getString(R.string.action_ring_mode_vibrate));
                if(action_ring==PublicConsts.ACTION_RING_OFF) builder.append(context.getResources().getString(R.string.action_ring_mode_off));
            }*/
            String value_ringmode=ActionDisplayValue.ActionDisplayValueOfAdapter.getRingModeDisplayValue(context,actions[PublicConsts.ACTION_RING_MODE_LOCALE]);
            if(value_ringmode.length()>0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(value_ringmode);
            }


            /*String [] action_volumes=actions[PublicConsts.ACTION_RING_VOLUME_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL);
            AudioManager audioManager=(AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            int volume_call=Integer.parseInt(action_volumes[PublicConsts.VOLUME_RING_LOCALE]);
            int volume_notification=Integer.parseInt(action_volumes[PublicConsts.VOLUME_NOTIFICATION_LOCALE]);
            int volume_media=Integer.parseInt(action_volumes[PublicConsts.VOLUME_MEDIA_LOCALE]);
            int volume_alarm=Integer.parseInt(action_volumes[PublicConsts.VOLUME_ALARM_LOCALE]);
            if(volume_call>=0) {
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.activity_taskgui_actions_ring_volume_ring));
                builder.append((int)(((double)volume_call/audioManager.getStreamMaxVolume(AudioManager.STREAM_RING))*100));
                builder.append(context.getResources().getString(R.string.percentage));
            }
            if(volume_media>=0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.activity_taskgui_actions_ring_volume_media));
                builder.append((int)(((double)volume_media/audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))*100));
                builder.append(context.getResources().getString(R.string.percentage));
            }
            if(volume_notification>=0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.activity_taskgui_actions_ring_volume_notification));
                builder.append((int)(((double)volume_notification/audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION))*100));
                builder.append(context.getResources().getString(R.string.percentage));
            }
            if(volume_alarm>=0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.activity_taskgui_actions_ring_volume_alarm));
                builder.append((int)(((double)volume_alarm/audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM))*100));
                builder.append(context.getResources().getString(R.string.percentage));
            }*/
            String value_volume=ActionDisplayValue.ActionDisplayValueOfAdapter.getRingVolumeDisplayValue(context,actions[PublicConsts.ACTION_RING_VOLUME_LOCALE]);
            if(value_volume.length()>0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(value_volume);
            }


            /*String[] action_ring_selections=actions[PublicConsts.ACTION_RING_SELECTION_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL);
            int action_ring_selection_call=Integer.parseInt(action_ring_selections[PublicConsts.RING_SELECTION_CALL_TYPE_LOCALE]);
            int action_ring_selection_notification=Integer.parseInt(action_ring_selections[PublicConsts.RING_SELECTION_NOTIFICATION_TYPE_LOCALE]);
            //RingtoneManager ringtoneManager=RingtoneManager.
            if(action_ring_selection_notification>=0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.action_set_ringtone_notification));
                builder.append(RingtoneManager.getRingtone(context, Uri.parse(item.uri_ring_notification)).getTitle(context));
            }
            if(action_ring_selection_call>=0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.action_set_ringtone_phone));
                builder.append(RingtoneManager.getRingtone(context, Uri.parse(item.uri_ring_call)).getTitle(context));
            }*/
            String value_ringselect=ActionDisplayValue.ActionDisplayValueOfAdapter.getRingSelectionDisplayValue(context,item);
            if(value_ringselect.length()>0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(value_ringselect);
            }

            /*String [] action_vibrate_values=actions[PublicConsts.ACTION_VIBRATE_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL);
            int vibrate_frequency=Integer.parseInt(action_vibrate_values[PublicConsts.VIBRATE_FREQUENCY_LOCALE]);
            if(vibrate_frequency>0){
                int vibrate_duration=Integer.parseInt(action_vibrate_values[PublicConsts.VIBRATE_DURATION_LOCALE]);
                int vibrate_interval=Integer.parseInt(action_vibrate_values[PublicConsts.VIBRATE_INTERVAL_LOCALE]);
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.adapter_action_vibrate));
                builder.append(vibrate_frequency);
                builder.append(context.getResources().getString(R.string.dialog_actions_vibrate_frequency_measure));
                builder.append(",");
                builder.append(vibrate_duration);
                builder.append(context.getResources().getString(R.string.dialog_actions_vibrate_duration_measure));
                builder.append(",");
                builder.append(vibrate_interval);
                builder.append(context.getResources().getString(R.string.dialog_actions_vibrate_interval_measure));
            }*/
            String value_vibrate=ActionDisplayValue.ActionDisplayValueOfAdapter.getVibrateDisplayValue(context,actions[PublicConsts.ACTION_VIBRATE_LOCALE]);
            if(value_vibrate.length()>0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(value_vibrate);
            }

            String values_brightness= ActionDisplayValue.ActionDisplayValueOfAdapter.getBrightnessDisplayValue(context,actions[PublicConsts.ACTION_BRIGHTNESS_LOCALE]);
            if(values_brightness.length()>0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(values_brightness);
            }

            /*String  action_set_wallpaper_values=actions[PublicConsts.ACTION_SET_WALL_PAPER_LOCALE];
            int action_wallpaper=Integer.parseInt(action_set_wallpaper_values);
            if(action_wallpaper>=0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.action_set_wallpaper));
                builder.append(ValueUtils.getRealPathFromUri(context,Uri.parse(item.uri_wallpaper_desktop)));
            }*/
            String value_wallpaper=ActionDisplayValue.ActionDisplayValueOfAdapter.getWallpaperDisplayValue(context,item);
            if(value_wallpaper.length()>0){
                if(builder.toString().length()>0)builder.append(",");
                builder.append(value_wallpaper);
            }

            /*String action_sms_values[]=actions[PublicConsts.ACTION_SMS_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL);
            if(Integer.parseInt(action_sms_values[PublicConsts.SMS_ENABLED_LOCALE])>=0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(context.getResources().getString(R.string.adapter_action_sms));
                if(Build.VERSION.SDK_INT>=22){
                    SubscriptionInfo subinfo= SubscriptionManager.from(context).getActiveSubscriptionInfo(Integer.parseInt(action_sms_values[PublicConsts.SMS_SUBINFO_LOCALE]));
                    builder.append("(");
                    builder.append(subinfo.getDisplayName());
                    builder.append(":");
                    builder.append(subinfo.getNumber());
                    builder.append(")");
                }
                builder.append(context.getResources().getString(R.string.adapter_action_sms_receivers));
                builder.append(item.sms_address);
                builder.append(",");
                builder.append(context.getResources().getString(R.string.adapter_action_sms_message));
                builder.append(item.sms_message);
            }*/
            String value_sms=ActionDisplayValue.ActionDisplayValueOfAdapter.getSMSDisplayValue(context,item);
            if(value_sms.length()>0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(value_sms);
            }

            String value_toast=ActionDisplayValue.ActionDisplayValueOfAdapter.getToastDisplayValue(context,item);
            if(value_toast.length()>0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(value_toast);
            }

            String value_notification= ActionDisplayValue.ActionDisplayValueOfAdapter.getNotificationDisplayValue(context,item);
            if(value_notification.length()>0) {
                if(builder.toString().length()>0) builder.append(",");
                builder.append(value_notification);
            }

            String value_gps= ActionDisplayValue.ActionDisplayValueOfAdapter.getGpsDisplayValue(context,actions[PublicConsts.ACTION_GPS_LOCALE]);
            if(value_gps.length()>0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(value_gps);
            }

            String value_net=ActionDisplayValue.ActionDisplayValueOfAdapter.getNetDisplayValue(context,actions[PublicConsts.ACTION_NET_LOCALE]);
            if(value_net.length()>0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(value_net);
            }

            String value_airplanemode= ActionDisplayValue.ActionDisplayValueOfAdapter.getAirplaneModeDisplayValue(context,actions[PublicConsts.ACTION_AIRPLANE_MODE_LOCALE]);
            if(value_airplanemode.length()>0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(value_airplanemode);
            }

            String value_device= ActionDisplayValue.ActionDisplayValueOfAdapter.getDeviceControlDisplayValue(context,actions[PublicConsts.ACTION_DEVICECONTROL_LOCALE]);
            if(value_device.length()>0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(value_device);
            }

            String value_task_enable= ActionDisplayValue.ActionDisplayValueOfAdapter.getEnableTasksDisplayValue(context,actions[PublicConsts.ACTION_ENABLE_TASKS_LOCALE]);
            if(value_task_enable.length()>0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(value_task_enable);
            }

            String value_task_disable= ActionDisplayValue.ActionDisplayValueOfAdapter.getDisableTasksDisplayValue(context,actions[PublicConsts.ACTION_DISABLE_TASKS_LOCALE]);
            if(value_task_enable.length()>0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(value_task_disable);
            }

            String value_app_open= ActionDisplayValue.ActionDisplayValueOfAdapter.getAppLaunchDisplayValue(context,actions[PublicConsts.ACTION_LAUNCH_APP_PACKAGES]);
            if(value_app_open.length()>0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(value_app_open);
            }

            String value_app_close= ActionDisplayValue.ActionDisplayValueOfAdapter.getAppCloseDisplayValue(context,actions[PublicConsts.ACTION_STOP_APP_PACKAGES]);
            if(value_app_close.length()>0){
                if(builder.toString().length()>0) builder.append(",");
                builder.append(value_app_close);
            }

            String returnValue=builder.toString();
            if(returnValue.trim().equals("")) return context.getResources().getString(R.string.word_nothing);
            return returnValue;
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
        //return "";
    }

    private int dp2px(int dp){
        return  DisplayDensity.dip2px(this.context,dp);
    }

    /*public static final class  ViewHolder {
        public TextView trigger_value, task_name;
        public ImageView img,icon_wifi_on,icon_wifi_off,icon_bluetooth_on,icon_bluetooth_off,icon_net_on,icon_net_off,
                icon_ring_normal,icon_ring_vibrate,icon_ring_off, icon_device_reboot,icon_device_shutdown,icon_brightness_auto,icon_brightness_manual,icon_gps_on,icon_gps_off,
                icon_airplane_mode_on,icon_airplane_mode_off,icon_ring_volume,icon_ring_selection,icon_wallpaper,icon_notification,icon_vibrate,icon_sms,icon_toast,icon_enable,icon_disable;
        TextView tv_more;
        public android.support.v7.widget.SwitchCompat aSwitch;
        public CheckBox checkbox;
        public View root;
    }*/

    public interface SwitchChangedListener{
        void onCheckedChanged(int position,boolean b);
    }

    /*private String getWeekLoopDisplayValue(Context context,boolean [] week_repeat,int hourOfDay,int minute){
        if(week_repeat==null||week_repeat.length!=7) return "";
        String tv_value="";
        if(week_repeat[1]) tv_value+=context.getResources().getString(R.string.monday)+" ";//if(this.weekloop[1]) tv_value+="÷‹“ª ";
        if(week_repeat[2]) tv_value+=context.getResources().getString(R.string.tuesday)+" ";
        if(week_repeat[3]) tv_value+=context.getResources().getString(R.string.wednesday)+" ";
        if(week_repeat[4]) tv_value+=context.getResources().getString(R.string.thursday)+" ";
        if(week_repeat[5]) tv_value+=context.getResources().getString(R.string.friday)+" ";
        if(week_repeat[6]) tv_value+=context.getResources().getString(R.string.saturday)+" ";
        if(week_repeat[0]) tv_value+=context.getResources().getString(R.string.sunday);
        boolean everyday=true;
        for(int i=0;i<7;i++){
            if(!week_repeat[i]) {
                everyday=false;
                break;
            }
        }
        String time=ValueUtils.format(hourOfDay)+":"+ValueUtils.format(minute);
        if(everyday) return time+"\n"+context.getResources().getString(R.string.everyday);
        return  time+"\n"+tv_value;
    }  */

}
