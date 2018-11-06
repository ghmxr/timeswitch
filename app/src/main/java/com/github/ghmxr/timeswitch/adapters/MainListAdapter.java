package com.github.ghmxr.timeswitch.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.activities.Triggers;
import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.utils.DisplayDensity;
import com.github.ghmxr.timeswitch.utils.LogUtil;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.util.Calendar;
import java.util.List;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class MainListAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    public List<TaskItem> list;
    SwitchChangedListener mlistener;
    boolean isMultiSelectMode=false;
    boolean [] isSelected;
    //Calendar calendar;
    private View[] views;
    //private List<TextView> repeat_textviews=new ArrayList<>();
    private static final int ICON_COUNT_LIMIT=7;


    public MainListAdapter (Context context,List<TaskItem> list){
        this.context=context;
        inflater=LayoutInflater.from(context);
        this.list= list;
        isSelected=new boolean[list.size()];
        //calendar=Calendar.getInstance();
        views=new View[list.size()];
    }


    @Override
    public int getCount() {
        return this.list.size();
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
        ViewHolder holder;
        if(views[i]==null){
            views[i]=inflater.inflate(R.layout.item_task,viewGroup,false);
            holder=new ViewHolder();
            holder.img=views[i].findViewById(R.id.item_task_img);
            holder.trigger_value =views[i].findViewById(R.id.item_task_time);
            holder.task_name =views[i].findViewById(R.id.item_task_repeat);
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
            views[i].setTag(holder);
        }
        else{
            holder=(ViewHolder) views[i].getTag();
        }

        if(this.list.get(i).trigger_type ==PublicConsts.TRIGGER_TYPE_SINGLE){
            holder.img.setImageResource(R.drawable.icon_repeat_single);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
            Calendar calendar=Calendar.getInstance();
            calendar.setTimeInMillis(list.get(i).time);
            int month=calendar.get(Calendar.MONTH)+1;
            holder.trigger_value.setText( calendar.get(Calendar.YEAR)+"/"+ValueUtils.format(month)+"/"+ValueUtils.format(calendar.get(Calendar.DAY_OF_MONTH))+"/"+ValueUtils.getDayOfWeek(list.get(i).time)+"/"+
                    "\n"+ValueUtils.format(calendar.get(Calendar.HOUR_OF_DAY))+":"+ ValueUtils.format(calendar.get(Calendar.MINUTE)));
        }else if(this.list.get(i).trigger_type ==PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME){
            holder.img.setImageResource(R.drawable.icon_repeat_percertaintime);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
            refreshAllCertainTimeTaskItems();
        }else if(this.list.get(i).trigger_type ==PublicConsts.TRIGGER_TYPE_LOOP_WEEK){
            holder.img.setImageResource(R.drawable.icon_repeat_weekloop);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
            Calendar calendar=Calendar.getInstance();
            calendar.setTimeInMillis(list.get(i).time);
            holder.trigger_value.setText( ValueUtils.format(calendar.get(Calendar.HOUR_OF_DAY))+":"+ ValueUtils.format(calendar.get(Calendar.MINUTE)));
        }else if(this.list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE){
            holder.img.setImageResource(R.drawable.icon_battery_high);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        }else if(this.list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE){
            holder.img.setImageResource(R.drawable.icon_battery_low);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
            //holder.trigger_value.setText(context.getResources().getString(R.string.more_than)+this.list.get(i).battery_percentage+"%");
            holder.trigger_value.setText(context.getResources().getString(R.string.less_than)+this.list.get(i).battery_percentage+"%");
        }else if(this.list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE||this.list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE){
            holder.img.setImageResource(R.drawable.icon_temperature);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
            holder.trigger_value.setText(context.getResources().getString(R.string.higher_than)+this.list.get(i).battery_temperature+"¡æ");
        }else if(this.list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE) {
            holder.img.setImageResource(R.drawable.icon_temperature);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
            holder.trigger_value.setText(context.getResources().getString(R.string.lower_than)+this.list.get(i).battery_temperature+"¡æ");
        }else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_RECEIVED_BROADTCAST){
            holder.img.setImageResource(R.drawable.icon_broadcast);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
            String action=list.get(i).selectedAction;
            if(action.length()>16) action=action.substring(0,16)+"...";
            holder.trigger_value.setText(action);
        }else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED){
            holder.img.setImageResource(R.drawable.icon_wifi_connected);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
            String ssidinfo= Triggers.getWifiConnectionDisplayValue(context,list.get(i).wifiIds);
            if(ssidinfo.length()>16) ssidinfo=ssidinfo.substring(0,16)+"...";
            holder.trigger_value.setText(ssidinfo);
        }else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_WIFI_DISCONNECTED){
            holder.img.setImageResource(R.drawable.icon_wifi_disconnected);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
            String ssidinfo= Triggers.getWifiConnectionDisplayValue(context,list.get(i).wifiIds);
            if(ssidinfo.length()>16) ssidinfo=ssidinfo.substring(0,16)+"...";
            holder.trigger_value.setText(ssidinfo);
        }


        else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_SCREEN_ON){
            holder.img.setImageResource(R.drawable.icon_screen_unlocked);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            holder.trigger_value.setText(context.getResources().getString(R.string.activity_triggers_screen_on));
        }
        else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_SCREEN_OFF){
            holder.img.setImageResource(R.drawable.icon_screen_locked);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            holder.trigger_value.setText(context.getResources().getString(R.string.activity_triggers_screen_off));
        }
        else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_POWER_CONNECTED){
            holder.img.setImageResource(R.drawable.icon_power_connected);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            holder.trigger_value.setText(context.getResources().getString(R.string.activity_triggers_power_connected));
        }
        else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_POWER_DISCONNECTED){
            holder.img.setImageResource(R.drawable.icon_power_disconnected);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            holder.trigger_value.setText(context.getResources().getString(R.string.activity_triggers_power_disconnected));
        }


        else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_ON){
            holder.img.setImageResource(R.drawable.icon_wifi_on);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            holder.trigger_value.setText(Triggers.getWidgetDisplayValue(context,list.get(i).trigger_type));
        }else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF){
            holder.img.setImageResource(R.drawable.icon_wifi_off);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            holder.trigger_value.setText(Triggers.getWidgetDisplayValue(context,list.get(i).trigger_type));
        }else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON){
            holder.img.setImageResource(R.drawable.icon_bluetooth_on);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            holder.trigger_value.setText(Triggers.getWidgetDisplayValue(context,list.get(i).trigger_type));
        }else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF){
            holder.img.setImageResource(R.drawable.icon_bluetooth_off);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            holder.trigger_value.setText(Triggers.getWidgetDisplayValue(context,list.get(i).trigger_type));
        }else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF){
            holder.img.setImageResource(R.drawable.icon_ring_off);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            holder.trigger_value.setText(Triggers.getWidgetDisplayValue(context,list.get(i).trigger_type));
        }else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE){
            holder.img.setImageResource(R.drawable.icon_ring_vibrate);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            holder.trigger_value.setText(Triggers.getWidgetDisplayValue(context,list.get(i).trigger_type));
        }else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL){
            holder.img.setImageResource(R.drawable.icon_ring_normal);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            holder.trigger_value.setText(Triggers.getWidgetDisplayValue(context,list.get(i).trigger_type));
        }else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON){
            holder.img.setImageResource(R.drawable.icon_airplanemode_on);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            holder.trigger_value.setText(Triggers.getWidgetDisplayValue(context,list.get(i).trigger_type));
        }else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF){
            holder.img.setImageResource(R.drawable.icon_airplanemode_off);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            holder.trigger_value.setText(Triggers.getWidgetDisplayValue(context,list.get(i).trigger_type));
        }else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED){
            holder.img.setImageResource(R.drawable.icon_ap_on);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            holder.trigger_value.setText(Triggers.getWidgetDisplayValue(context,list.get(i).trigger_type));
        }else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED){
            holder.img.setImageResource(R.drawable.icon_ap_off);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            holder.trigger_value.setText(Triggers.getWidgetDisplayValue(context,list.get(i).trigger_type));
        }else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_NET_ON){
            holder.img.setImageResource(R.drawable.icon_cellular_on);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            holder.trigger_value.setText(Triggers.getWidgetDisplayValue(context,list.get(i).trigger_type));
        }else if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_NET_OFF){
            holder.img.setImageResource(R.drawable.icon_cellular_off);
            holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            holder.trigger_value.setText(Triggers.getWidgetDisplayValue(context,list.get(i).trigger_type));
        }

        String name=list.get(i).name;
        if(name.length()>20) name=name.substring(0,20)+"...";
        holder.task_name.setText(name);

        String [] actions =list.get(i).actions;
        try{
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

            Log.d("ADAPTER_ICON_COUNT",""+showCount);
            Log.d("ADAPTER_ACTION_NUM",""+actionNum);
        }catch (NumberFormatException ne){
            ne.printStackTrace();
            new AlertDialog.Builder(context)
                    .setTitle("NumberFormatException")
                    .setMessage(ne.toString())
                    .setPositiveButton(context.getResources().getString(R.string.dialog_button_positive), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .show();
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.putExceptionLog(context,e);
        }


        if(isMultiSelectMode) {
            holder.aSwitch.setVisibility(View.GONE);
            holder.checkbox.setVisibility(View.VISIBLE);
            holder.checkbox.setChecked(isSelected[i]);
        }else{
            holder.aSwitch.setVisibility(View.VISIBLE);
            holder.checkbox.setVisibility(View.GONE);
            holder.aSwitch.setOnCheckedChangeListener(null);
            holder.aSwitch.setChecked(list.get(i).isenabled);
            holder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    //list.get(i).isenabled=b;
                    Log.i("OnCheckedChanged","listener activated!!!");
                    if(mlistener!=null) mlistener.onCheckedChanged(i,b);
                    refreshAllCertainTimeTaskItems();
                }
            });
        }

        view=views[i];
        return view;
    }


    /**
     * @deprecated
     */
    public ImageView newActionIcon(int resID){
        ImageView icon=new ImageView(this.context);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(dp2px(20),dp2px(20));
        icon.setImageResource(resID);
        icon.setLayoutParams(params);
        return icon;
    }

    public void onDataSetChanged(List<TaskItem> list){
        this.list=list;
        this.isSelected=new boolean[this.list.size()];
        this.views=new View[this.list.size()];
        this.notifyDataSetChanged();
    }

    public void refreshAllCertainTimeTaskItems(){
        for(int i=0;i<views.length;i++){
            if(i>=list.size()) break;
            if(views[i]==null) continue;
            if(list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME){
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
                TextView tv=views[i].findViewById(R.id.item_task_time);
                if(list.get(i).isenabled) {
                    tv.setText(display);
                }
                else tv.setText("Off");
            }
        }

    }

    public void onMultiSelectModeItemClicked(int position){
        isSelected[position]=!isSelected[position];
        this.notifyDataSetChanged();
    }

    public boolean[] getIsSelected(){
        return this.isSelected;
    }

    public void setOnSwitchChangedListener(SwitchChangedListener listener){
        this.mlistener=listener;
    }

    /*public void switchOffItem(int position){
        this.list.get(position).isenabled=false;
        this.notifyDataSetChanged();
    }*/

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

    private int dp2px(int dp){
        return  DisplayDensity.dip2px(this.context,dp);
    }

    public static final class  ViewHolder {
        public TextView trigger_value, task_name;
        public ImageView img,icon_wifi_on,icon_wifi_off,icon_bluetooth_on,icon_bluetooth_off,icon_net_on,icon_net_off,
                icon_ring_normal,icon_ring_vibrate,icon_ring_off, icon_device_reboot,icon_device_shutdown,icon_brightness_auto,icon_brightness_manual,icon_gps_on,icon_gps_off,
                icon_airplane_mode_on,icon_airplane_mode_off,icon_ring_volume,icon_ring_selection,icon_wallpaper,icon_notification,icon_vibrate,icon_sms,icon_toast,icon_enable,icon_disable;
        TextView tv_more;
        public android.support.v7.widget.SwitchCompat aSwitch;
        public CheckBox checkbox;
       // public LinearLayout actions;
    }

    public interface SwitchChangedListener{
        void onCheckedChanged(int position,boolean b);
    }

}
