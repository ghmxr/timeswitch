package com.github.ghmxr.timeswitch.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.activities.Actions;
import com.github.ghmxr.timeswitch.activities.BaseActivity;
import com.github.ghmxr.timeswitch.activities.Exceptions;
import com.github.ghmxr.timeswitch.activities.Triggers;
import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.SQLConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.utils.DisplayDensity;
import com.github.ghmxr.timeswitch.utils.LogUtil;
import com.github.ghmxr.timeswitch.utils.MySQLiteOpenHelper;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public abstract class TaskGui extends BaseActivity implements View.OnClickListener{

	//public CustomTimePicker timepicker;
	/**
	 * @deprecated
	 */
	public Calendar calendar=Calendar.getInstance();
	public TaskItem taskitem=new TaskItem();
	private static final int REQUEST_CODE_TRIGGERS=0;
	private static final int REQUEST_CODE_EXCEPTIONS=1;
	private static final int REQUEST_CODE_ACTIONS=2;

	public void onCreate(Bundle mybundle){
		super.onCreate(mybundle);
		setContentView(R.layout.layout_taskgui);
		Toolbar toolbar =findViewById(R.id.taskgui_toolbar);
		setSupportActionBar(toolbar);
		//timepicker=findViewById(R.id.layout_taskgui_timepicker);

		/*Calendar current=Calendar.getInstance();
		current.setTimeInMillis(System.currentTimeMillis());
		calendar.set(current.get(Calendar.YEAR),current.get(Calendar.MONTH),current.get(Calendar.DAY_OF_MONTH),current.get(Calendar.HOUR_OF_DAY),current.get(Calendar.MINUTE));
		calendar.setTimeInMillis(calendar.getTimeInMillis()+10*60*1000);*/

		Calendar calendar=Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis()+10*60*1000);
		calendar.set(Calendar.SECOND,0);
		//timepicker.setIs24HourView(true);

		taskitem.time=calendar.getTimeInMillis();

		findViewById(R.id.layout_taskgui_area_name).setOnClickListener(this);

		findViewById(R.id.layout_taskgui_trigger).setOnClickListener(this);
		//findViewById(R.id.layout_taskgui_area_condition_single).setOnClickListener(this);
		//findViewById(R.id.layout_taskgui_area_condition_percertaintime).setOnClickListener(this);
		//findViewById(R.id.layout_taskgui_area_condition_weekloop).setOnClickListener(this);
		//findViewById(R.id.layout_taskgui_area_condition_battery_percentage).setOnClickListener(this);
		//findViewById(R.id.layout_taskgui_area_condition_battery_temperature).setOnClickListener(this);
		//findViewById(R.id.layout_taskgui_area_condition_received_broadcast).setOnClickListener(this);
		
		findViewById(R.id.layout_taskgui_area_exception_lockscreen).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_unlockscreen).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_wifi_enabled).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_wifi_disabled).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_ring_vibrate).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_ring_off).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_ring_normal).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_bluetooth_enabled).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_bluetooth_disabled).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_net_enabled).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_net_disabled).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_gps_enabled).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_gps_disabled).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_airplane_mode_enabled).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_airplane_mode_disabled).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_battery_percentage).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_battery_temperature).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_day_of_week).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_period).setOnClickListener(this);
		
		findViewById(R.id.layout_taskgui_exception_lockscreen_cancel).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_exception_unlockscreen_cancel).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_exception_wifi_enabled_cancel).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_exception_wifi_disabled_cancel).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_exception_bluetooth_enabled_cancel).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_exception_bluetooth_disabled_cancel).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_exception_ring_normal_cancel).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_exception_ring_off_cancel).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_exception_ring_vibrate_cancel).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_exception_net_enabled_cancel).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_exception_net_disabled_cancel).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_airplane_mode_enabled).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_airplane_mode_disabled).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_exception_gps_enabled_cancel).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_exception_gps_disabled_cancel).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_airplane_mode_enabled_cancel).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_airplane_mode_disabled_cancel).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_battery_percentage_cancel).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_battery_temperature_cancel).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_day_of_week_cancel).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_exception_period_cancel).setOnClickListener(this);

		findViewById(R.id.taskgui_operations_area_wifi).setOnClickListener(this);
		findViewById(R.id.taskgui_operations_area_bluetooth).setOnClickListener(this);
		findViewById(R.id.taskgui_operations_area_net).setOnClickListener(this);
		findViewById(R.id.taskgui_operations_area_ring_mode).setOnClickListener(this);
		findViewById(R.id.taskgui_operations_area_devicecontrol).setOnClickListener(this);
		findViewById(R.id.taskgui_operations_area_brightness).setOnClickListener(this);
		findViewById(R.id.taskgui_operations_area_gps).setOnClickListener(this);
		findViewById(R.id.taskgui_operations_area_airplane_mode).setOnClickListener(this);
		findViewById(R.id.taskgui_operations_area_ring_volume).setOnClickListener(this);
		findViewById(R.id.taskgui_operations_area_ring_selection).setOnClickListener(this);
		findViewById(R.id.taskgui_operations_area_wallpaper).setOnClickListener(this);
		findViewById(R.id.taskgui_operations_area_vibrate).setOnClickListener(this);
		findViewById(R.id.taskgui_operations_area_notification).setOnClickListener(this);
		findViewById(R.id.taskgui_operations_area_toast).setOnClickListener(this);
		findViewById(R.id.taskgui_operations_area_sms).setOnClickListener(this);
		findViewById(R.id.taskgui_operations_area_enable).setOnClickListener(this);
		findViewById(R.id.taskgui_operations_area_disable).setOnClickListener(this);
		findViewById(R.id.taskgui_operations_area_app_launch).setOnClickListener(this);
		findViewById(R.id.taskgui_operations_area_app_close).setOnClickListener(this);

		findViewById(R.id.layout_taskgui_area_additional_notify).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_additional_autodelete).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_additional_autoclose).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_additional_titlecolor).setOnClickListener(this);

		findViewById(R.id.layout_taskgui_area_exception_additem).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_action_additem).setOnClickListener(this);

		initialVariables();

		//do set the views of the variables.

		/*if(Build.VERSION.SDK_INT<23){
			timepicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
			timepicker.setCurrentMinute(calendar.get(Calendar.MINUTE));

		}else{
			timepicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
			timepicker.setMinute(calendar.get(Calendar.MINUTE));
		} */
		//timepicker.setOnTimeChangedListener(this);

		String taskname=taskitem.name;
		if(taskname.length()>24) taskname=taskname.substring(0,24)+"...";
		((TextView)findViewById(R.id.layout_taskgui_area_name_text)).setText(taskname);
		((CheckBox)findViewById(R.id.layout_taskgui_area_additional_autoclose_cb)).setChecked(taskitem.autoclose);
		((CheckBox)findViewById(R.id.layout_taskgui_area_additional_autodelete_cb)).setChecked(taskitem.autodelete);
		//activateTriggerType(taskitem.trigger_type);
		try{
			(findViewById(R.id.layout_taskgui_additional_titlecolor_img)).setBackgroundColor(Color.parseColor(taskitem.addition_title_color));
		}catch (Exception e){
			e.printStackTrace();
		}

		if(taskitem.trigger_type==PublicConsts.TRIGGER_TYPE_SINGLE) setAutoCloseAreaEnabled(false);
		else {
			setAutoCloseAreaEnabled(!((CheckBox)findViewById(R.id.layout_taskgui_area_additional_autodelete_cb)).isChecked());
		}

		refreshTriggerDisplayValue();
		refreshExceptionViews();
		refreshActionStatus();
		setTaskThemeColor(taskitem.addition_title_color);
	}

	public abstract void initialVariables();

	private void setTaskThemeColor(String color){
		try{ setToolBarAndStatusBarColor(findViewById(R.id.taskgui_toolbar),color); } catch (Exception e){ e.printStackTrace(); }
	}

	@Override
	public void processMessage(Message msg){}

	private void refreshActionStatus(){
		((TextView)findViewById(R.id.taskgui_operations_area_wifi_status)).setText(ActionDisplayValue.getGeneralDisplayValue(this,taskitem.actions[PublicConsts.ACTION_WIFI_LOCALE]));

		((TextView)findViewById(R.id.taskgui_operations_area_bluetooth_status)).setText(ActionDisplayValue.getGeneralDisplayValue(this,taskitem.actions[PublicConsts.ACTION_BLUETOOTH_LOCALE]));

		((TextView)findViewById(R.id.taskgui_operations_area_net_status)).setText(ActionDisplayValue.getGeneralDisplayValue(this,taskitem.actions[PublicConsts.ACTION_NET_LOCALE]));

		((TextView)findViewById(R.id.taskgui_operations_area_devicecontrol_status)).setText(ActionDisplayValue.getDeviceControlDisplayValue(this,taskitem.actions[PublicConsts.ACTION_DEVICECONTROL_LOCALE]));

		((TextView)findViewById(R.id.taskgui_operations_area_ring_mode_status)).setText(ActionDisplayValue.getRingModeDisplayValue(this,taskitem.actions[PublicConsts.ACTION_RING_MODE_LOCALE]));

		((TextView)findViewById(R.id.taskgui_operations_area_ring_volume_status)).setText(ActionDisplayValue.getRingVolumeDisplayValue(this,taskitem.actions[PublicConsts.ACTION_RING_VOLUME_LOCALE]));

		((TextView)findViewById(R.id.taskgui_operations_area_brightness_status)).setText(ActionDisplayValue.getBrightnessDisplayValue(this,taskitem.actions[PublicConsts.ACTION_BRIGHTNESS_LOCALE]));

		((TextView)findViewById(R.id.taskgui_operations_area_gps_status)).setText(ActionDisplayValue.getGeneralDisplayValue(this,taskitem.actions[PublicConsts.ACTION_GPS_LOCALE]));

		((TextView)findViewById(R.id.taskgui_operations_area_airplane_mode_status)).setText(ActionDisplayValue.getGeneralDisplayValue(this,taskitem.actions[PublicConsts.ACTION_AIRPLANE_MODE_LOCALE]));

		((TextView)findViewById(R.id.taskgui_operations_area_ring_selection_status)).setText(ActionDisplayValue.getRingSelectionDisplayValue(this,taskitem.actions[PublicConsts.ACTION_RING_SELECTION_LOCALE]));

		((TextView)findViewById(R.id.taskgui_operations_area_wallpaper_status)).setText(ActionDisplayValue.getWallpaperDisplayValue(this,taskitem.actions[PublicConsts.ACTION_SET_WALL_PAPER_LOCALE],taskitem.uri_wallpaper_desktop));

		((TextView)findViewById(R.id.taskgui_operations_area_vibrate_status)).setText(ActionDisplayValue.getVibrateDisplayValue(this,taskitem.actions[PublicConsts.ACTION_VIBRATE_LOCALE]));

		((TextView)findViewById(R.id.taskgui_operations_area_notification_status)).setText(ActionDisplayValue.getNotificationDisplayValue(this,taskitem.actions[PublicConsts.ACTION_NOTIFICATION_LOCALE]));
		((TextView)findViewById(R.id.taskgui_operations_area_toast_status)).setText(ActionDisplayValue.getToastDisplayValue(taskitem.actions[PublicConsts.ACTION_TOAST_LOCALE],taskitem.toast));
		((TextView)findViewById(R.id.taskgui_operations_area_sms_status)).setText(ActionDisplayValue.getSMSDisplayValue(this,taskitem.actions[PublicConsts.ACTION_SMS_LOCALE]));
		((TextView)findViewById(R.id.taskgui_operations_area_enable_status)).setText(ActionDisplayValue.getTaskSwitchDisplayValue(taskitem.actions[PublicConsts.ACTION_ENABLE_TASKS_LOCALE]));
		((TextView)findViewById(R.id.taskgui_operations_area_disable_status)).setText(ActionDisplayValue.getTaskSwitchDisplayValue(taskitem.actions[PublicConsts.ACTION_DISABLE_TASKS_LOCALE]));
		((TextView)findViewById(R.id.taskgui_operations_area_app_launch_status)).setText(ActionDisplayValue.getAppNameDisplayValue(this,taskitem.actions[PublicConsts.ACTION_LAUNCH_APP_PACKAGES]));
		((TextView)findViewById(R.id.taskgui_operations_area_app_close_status)).setText(ActionDisplayValue.getAppNameDisplayValue(this,taskitem.actions[PublicConsts.ACTION_STOP_APP_PACKAGES]));
		try{
			findViewById(R.id.taskgui_operations_area_wifi).setVisibility(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_WIFI_LOCALE])>=0?View.VISIBLE:View.GONE);
			findViewById(R.id.taskgui_operations_area_bluetooth).setVisibility(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_BLUETOOTH_LOCALE])>=0?View.VISIBLE:View.GONE);
			findViewById(R.id.taskgui_operations_area_ring_mode).setVisibility(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_RING_MODE_LOCALE])>=0?View.VISIBLE:View.GONE);
			String ring_volume_values[]=taskitem.actions[PublicConsts.ACTION_RING_VOLUME_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
			findViewById(R.id.taskgui_operations_area_ring_volume).setVisibility(
					(Integer.parseInt(ring_volume_values[PublicConsts.VOLUME_RING_LOCALE])>=0||Integer.parseInt(ring_volume_values[PublicConsts.VOLUME_MEDIA_LOCALE])>=0
							||Integer.parseInt(ring_volume_values[PublicConsts.VOLUME_NOTIFICATION_LOCALE])>=0||Integer.parseInt(ring_volume_values[PublicConsts.VOLUME_ALARM_LOCALE])>=0)
							?View.VISIBLE:View.GONE);
			String[] ring_selection_values=taskitem.actions[PublicConsts.ACTION_RING_SELECTION_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
			findViewById(R.id.taskgui_operations_area_ring_selection).setVisibility((Integer.parseInt(ring_selection_values[PublicConsts.RING_SELECTION_NOTIFICATION_TYPE_LOCALE])>=0||
					Integer.parseInt(ring_selection_values[PublicConsts.RING_SELECTION_CALL_TYPE_LOCALE])>=0
					)?View.VISIBLE:View.GONE);
			findViewById(R.id.taskgui_operations_area_brightness).setVisibility(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_BRIGHTNESS_LOCALE])>=0?View.VISIBLE:View.GONE);
			findViewById(R.id.taskgui_operations_area_vibrate).setVisibility(
					Integer.parseInt(taskitem.actions[PublicConsts.ACTION_VIBRATE_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[PublicConsts.VIBRATE_FREQUENCY_LOCALE])>=0?
					View.VISIBLE:View.GONE);
			findViewById(R.id.taskgui_operations_area_wallpaper).setVisibility(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_SET_WALL_PAPER_LOCALE])>=0?View.VISIBLE:View.GONE);
			findViewById(R.id.taskgui_operations_area_sms).setVisibility(
					Integer.parseInt(taskitem.actions[PublicConsts.ACTION_SMS_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[PublicConsts.SMS_ENABLED_LOCALE])>=0?View.VISIBLE:View.GONE);
			findViewById(R.id.taskgui_operations_area_notification).setVisibility(
					Integer.parseInt(taskitem.actions[PublicConsts.ACTION_NOTIFICATION_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[PublicConsts.NOTIFICATION_TYPE_LOCALE])>=0?
							View.VISIBLE:View.GONE);
			findViewById(R.id.taskgui_operations_area_toast).setVisibility(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_TOAST_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[PublicConsts.TOAST_TYPE_LOCALE])>=0?View.VISIBLE:View.GONE);
			findViewById(R.id.taskgui_operations_area_airplane_mode).setVisibility(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_AIRPLANE_MODE_LOCALE])>=0?View.VISIBLE:View.GONE);
			findViewById(R.id.taskgui_operations_area_net).setVisibility(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_NET_LOCALE])>=0?View.VISIBLE:View.GONE);
			findViewById(R.id.taskgui_operations_area_gps).setVisibility(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_GPS_LOCALE])>=0?View.VISIBLE:View.GONE);
			findViewById(R.id.taskgui_operations_area_devicecontrol).setVisibility(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_DEVICECONTROL_LOCALE])>=0?View.VISIBLE:View.GONE);
			findViewById(R.id.taskgui_operations_area_enable).setVisibility(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_ENABLE_TASKS_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL)[0])>=0?View.VISIBLE:View.GONE);
			findViewById(R.id.taskgui_operations_area_disable).setVisibility(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_DISABLE_TASKS_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL)[0])>=0?View.VISIBLE:View.GONE);
			findViewById(R.id.taskgui_operations_area_app_launch).setVisibility(taskitem.actions[PublicConsts.ACTION_LAUNCH_APP_PACKAGES].equals(String.valueOf(-1))?View.GONE:View.VISIBLE);
			findViewById(R.id.taskgui_operations_area_app_close).setVisibility(taskitem.actions[PublicConsts.ACTION_STOP_APP_PACKAGES].equals(String.valueOf(-1))?View.GONE:View.VISIBLE);
		}catch (Exception e){
			e.printStackTrace();
			LogUtil.putExceptionLog(this,e);
		}

    }

    private void refreshTriggerDisplayValue(){
		ImageView icon=findViewById(R.id.layout_taskgui_trigger_icon);
		TextView att=findViewById(R.id.layout_taskgui_trigger_att);
		TextView value=findViewById(R.id.layout_taskgui_trigger_value);
		switch(taskitem.trigger_type){
			default:break;
			case PublicConsts.TRIGGER_TYPE_SINGLE:{
				icon.setImageResource(R.drawable.icon_repeat_single);
				att.setText(getResources().getString(R.string.activity_taskgui_condition_single_att));
				value.setText(Triggers.getSingleTimeDisplayValue(this,taskitem.time));
			}
			break;
			case PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME:{
				icon.setImageResource(R.drawable.icon_repeat_percertaintime);
				att.setText(getResources().getString(R.string.activity_taskgui_condition_percertaintime_att));
				value.setText(Triggers.getCertainLoopTimeDisplayValue(this,taskitem.interval_milliseconds));
			}
			break;
			case PublicConsts.TRIGGER_TYPE_LOOP_WEEK:{
				icon.setImageResource(R.drawable.icon_repeat_weekloop);
				att.setText(getResources().getString(R.string.activity_taskgui_condition_weekloop_att));
				Calendar c=Calendar.getInstance();
				c.setTimeInMillis(taskitem.time);
				value.setText(Triggers.getWeekLoopDisplayValue(this,taskitem.week_repeat,c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE)));
			}
			break;
			case PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE:{
				icon.setImageResource(R.drawable.icon_temperature);
				att.setText(getResources().getString(R.string.activity_taskgui_condition_battery_temperature_att));
				value.setText(Triggers.getBatteryTemperatureDisplayValue(this,taskitem.trigger_type,taskitem.battery_temperature));
			}
			break;
			case PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE:{
				icon.setImageResource(R.drawable.icon_temperature);
				att.setText(getResources().getString(R.string.activity_taskgui_condition_battery_temperature_att));
				value.setText(Triggers.getBatteryTemperatureDisplayValue(this,taskitem.trigger_type,taskitem.battery_temperature));
			}
			break;
			case PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE:{
				icon.setImageResource(R.drawable.icon_battery_high);
				att.setText(getResources().getString(R.string.activity_taskgui_condition_battery_percentage_att));
				value.setText(Triggers.getBatteryPercentageDisplayValue(this,taskitem.trigger_type,taskitem.battery_percentage));
			}
			break;
			case PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE:{
				icon.setImageResource(R.drawable.icon_battery_low);
				att.setText(getResources().getString(R.string.activity_taskgui_condition_battery_percentage_att));
				value.setText(Triggers.getBatteryPercentageDisplayValue(this,taskitem.trigger_type,taskitem.battery_percentage));
			}
			break;
            case PublicConsts.TRIGGER_TYPE_RECEIVED_BROADCAST:{
                icon.setImageResource(R.drawable.icon_broadcast);
                att.setText(getResources().getString(R.string.activity_taskgui_condition_received_broadcast_att));
                value.setText(Triggers.getBroadcastDisplayValue(taskitem.selectedAction));
            }
            break;
			case PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED:{
				icon.setImageResource(R.drawable.icon_wifi_connected);
				att.setText(getResources().getString(R.string.activity_taskgui_condition_wifi_connected));
				value.setText(Triggers.getWifiConnectionDisplayValue(this,taskitem.wifiIds));
			}
			break;
			case PublicConsts.TRIGGER_TYPE_WIFI_DISCONNECTED:{
				icon.setImageResource(R.drawable.icon_wifi_disconnected);
				att.setText(getResources().getString(R.string.activity_taskgui_condition_wifi_disconnected));
				value.setText(Triggers.getWifiConnectionDisplayValue(this,taskitem.wifiIds));
			}
			break;
			case PublicConsts.TRIGGER_TYPE_SCREEN_ON:{
				icon.setImageResource(R.drawable.icon_screen_on);
				att.setText(getResources().getString(R.string.activity_triggers_screen_on));
				value.setText("");
			}
			break;
			case PublicConsts.TRIGGER_TYPE_SCREEN_OFF:{
				icon.setImageResource(R.drawable.icon_screen_off);
				att.setText(getResources().getString(R.string.activity_triggers_screen_off));
				value.setText("");
			}
			break;
			case PublicConsts.TRIGGER_TYPE_POWER_CONNECTED:{
				icon.setImageResource(R.drawable.icon_power_connected);
				att.setText(getResources().getString(R.string.activity_triggers_power_connected));
				value.setText("");
			}
			break;
			case PublicConsts.TRIGGER_TYPE_POWER_DISCONNECTED:{
				icon.setImageResource(R.drawable.icon_power_disconnected);
				att.setText(getResources().getString(R.string.activity_triggers_power_disconnected));
				value.setText("");
			}
			break;
			case PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_ON:{
				icon.setImageResource(R.drawable.icon_wifi_on);
				att.setText(Triggers.getWidgetDisplayValue(this,taskitem.trigger_type));
				value.setText("");
			}
			break;
			case PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF:{
				icon.setImageResource(R.drawable.icon_wifi_off);
				att.setText(Triggers.getWidgetDisplayValue(this,taskitem.trigger_type));
				value.setText("");
			}
			break;
			case PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON:{
				icon.setImageResource(R.drawable.icon_bluetooth_on);
				att.setText(Triggers.getWidgetDisplayValue(this,taskitem.trigger_type));
				value.setText("");
			}
			break;
            case PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF:{
                icon.setImageResource(R.drawable.icon_bluetooth_off);
                att.setText(Triggers.getWidgetDisplayValue(this,taskitem.trigger_type));
                value.setText("");
            }
            break;
			case PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF:{
				icon.setImageResource(R.drawable.icon_ring_off);
				att.setText(Triggers.getWidgetDisplayValue(this,taskitem.trigger_type));
				value.setText("");
			}
			break;
			case PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE:{
				icon.setImageResource(R.drawable.icon_ring_vibrate);
				att.setText(Triggers.getWidgetDisplayValue(this,taskitem.trigger_type));
				value.setText("");
			}
			break;
			case PublicConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL:{
				icon.setImageResource(R.drawable.icon_ring_normal);
				att.setText(Triggers.getWidgetDisplayValue(this,taskitem.trigger_type));
				value.setText("");
			}
			break;
			case PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF:{
				icon.setImageResource(R.drawable.icon_airplanemode_off);
				att.setText(Triggers.getWidgetDisplayValue(this,taskitem.trigger_type));
				value.setText("");
			}
			break;
			case PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON:{
				icon.setImageResource(R.drawable.icon_airplanemode_on);
				att.setText(Triggers.getWidgetDisplayValue(this,taskitem.trigger_type));
				value.setText("");
			}
			break;
			case PublicConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED:{
				icon.setImageResource(R.drawable.icon_ap_on);
				att.setText(Triggers.getWidgetDisplayValue(this,taskitem.trigger_type));
				value.setText("");
			}
			break;
			case PublicConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED:{
				icon.setImageResource(R.drawable.icon_ap_off);
				att.setText(Triggers.getWidgetDisplayValue(this,taskitem.trigger_type));
				value.setText("");
			}
			break;
			case PublicConsts.TRIGGER_TYPE_NET_ON:{
				icon.setImageResource(R.drawable.icon_cellular_on);
				att.setText(Triggers.getWidgetDisplayValue(this,taskitem.trigger_type));
				value.setText("");
			}
			break;
			case PublicConsts.TRIGGER_TYPE_NET_OFF:{
				icon.setImageResource(R.drawable.icon_cellular_off);
				att.setText(Triggers.getWidgetDisplayValue(this,taskitem.trigger_type));
				value.setText("");
			}
			break;
			case PublicConsts.TRIGGER_TYPE_APP_LAUNCHED:{
				icon.setImageResource(R.drawable.icon_app_launch);
				att.setText(getResources().getString(R.string.activity_trigger_app_opened));
				value.setText(Triggers.getAppNameDisplayValue(this,taskitem.package_names));
			}
			break;
			case PublicConsts.TRIGGER_TYPE_APP_CLOSED:{
				icon.setImageResource(R.drawable.icon_app_stop);
				att.setText(getResources().getString(R.string.activity_trigger_app_closed));
				value.setText(Triggers.getAppNameDisplayValue(this,taskitem.package_names));
			}
			break;
			case PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_IN:{
				icon.setImageResource(R.drawable.icon_headset);
				att.setText(getResources().getString(R.string.activity_trigger_headset));
				value.setText(getResources().getString(R.string.activity_trigger_headset_plug_in));
			}
			break;
			case PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT:{
				icon.setImageResource(R.drawable.icon_headset);
				att.setText(getResources().getString(R.string.activity_trigger_headset));
				value.setText(getResources().getString(R.string.activity_trigger_headset_plug_out));
			}
			break;
		}
	}

	/*public void activateTriggerType(int type){
		taskitem.trigger_type =type;
        refreshTriggerView(type);
		switch(type){
			default:break;
			case PublicConsts.TRIGGER_TYPE_SINGLE:{
                clearExceptionsOfTimeType();
				this.timepicker.setVisibility(View.VISIBLE);
				//refreshSingleTimeTextView();

			}
			break;

			case PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME:{
				this.timepicker.setVisibility(View.GONE);
				refreshRepeatTextView();
			}
			break;
			
			case PublicConsts.TRIGGER_TYPE_LOOP_WEEK:{
                clearExceptionsOfTimeType();
				this.timepicker.setVisibility(View.VISIBLE);
				refreshWeekLoopTextView();
			}
			break;

			case PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE:{
				clearExceptionsOfBatteryPercentage();
				timepicker.setVisibility(View.GONE);
				refreshBatteryPercentageTextView();
			}
			break;

			case PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE:{
			    clearExceptionsOfBatteryPercentage();
				timepicker.setVisibility(View.GONE);
				refreshBatteryPercentageTextView();
			}
			break;

			case PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE:{
			    clearExceptionsOfBatteryTemperature();
				timepicker.setVisibility(View.GONE);
				refreshBatteryTemperatureTextView();
			}
			break;

			case PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE:{
			    clearExceptionsOfBatteryTemperature();
				timepicker.setVisibility(View.GONE);
				refreshBatteryTemperatureTextView();
			}
			break;
			case PublicConsts.TRIGGER_TYPE_RECEIVED_BROADCAST:{
				timepicker.setVisibility(View.GONE);
				refreshBroadcastTextView();
			}
			break;
		}

		if(type==PublicConsts.TRIGGER_TYPE_SINGLE) setAutoCloseAreaEnabled(false);
		else {
			setAutoCloseAreaEnabled(!((CheckBox)findViewById(R.id.layout_taskgui_area_additional_autodelete_cb)).isChecked());
		}

	}  */

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
			default:break;
			case R.id.layout_taskgui_area_name:{
				View dialogview =LayoutInflater.from(this).inflate(R.layout.layout_dialog_name,null);
				final EditText edittext =dialogview.findViewById(R.id.dialog_edittext_name);
				edittext.setText(TaskGui.this.taskitem.name);//edittext.setText(TaskGui.this.taskname);
				final AlertDialog dialog=new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.dialog_task_name_title))
						.setView(dialogview)
						.setPositiveButton(getResources().getString(R.string.dialog_button_positive), null).create();
				dialog.show();
				dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String name=edittext.getText().toString().trim();
						if(name.length()<=0||name.equals("")){
							//Toast.makeText(TaskGui.this,"输入任务名称",Toast.LENGTH_SHORT).show();
							Snackbar.make(v,getResources().getString(R.string.dialog_task_name_invalid),Snackbar.LENGTH_SHORT).show();
							//return;
						}
						else{
							taskitem.name=name;//TaskGui.this.taskname=name;
							dialog.cancel();
							String taskname=taskitem.name;
							if(taskname.length()>24) taskname=taskname.substring(0,24)+"...";
							((TextView)findViewById(R.id.layout_taskgui_area_name_text)).setText(taskname);//TaskGui.this.clickarea_name_text.setText(TaskGui.this.taskname);
						}
					}
				});
			}
			break;
			case R.id.layout_taskgui_trigger:{
				Intent i=new Intent();
				i.setClass(this,Triggers.class);
				i.putExtra(Triggers.EXTRA_TRIGGER_TYPE,taskitem.trigger_type);
				i.putExtra(EXTRA_TITLE_COLOR,taskitem.addition_title_color);
				String trigger_values[]=new String[1];
				switch (taskitem.trigger_type){
					default:break;
					case PublicConsts.TRIGGER_TYPE_SINGLE:{
						trigger_values=new String[1];
						trigger_values[0]=String.valueOf(taskitem.time);
					}
					break;
					case PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME:{
						trigger_values=new String[1];
						trigger_values[0]=String.valueOf(taskitem.interval_milliseconds);
					}
					break;
					case PublicConsts.TRIGGER_TYPE_LOOP_WEEK:{
						trigger_values=new String[8];
						trigger_values[0]=String.valueOf(taskitem.time);
						for(int j=1;j<trigger_values.length;j++){
							trigger_values[j]=taskitem.week_repeat[j-1]?String.valueOf(1):String.valueOf(0);
						}
					}
					break;
					case PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE: case PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE: {
						trigger_values=new String[1];
						trigger_values[0]=String.valueOf(taskitem.battery_temperature);
					}
					break;
					case PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE: case PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE:{
						trigger_values=new String[1];
						trigger_values[0]=String.valueOf(taskitem.battery_percentage);
					}
					break;
					case PublicConsts.TRIGGER_TYPE_RECEIVED_BROADCAST:{
						trigger_values=new String[1];
						trigger_values[0]=String.valueOf(taskitem.selectedAction);
					}
					break;
					case PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED: case PublicConsts.TRIGGER_TYPE_WIFI_DISCONNECTED:{
						trigger_values=new String[1];
						trigger_values[0]=taskitem.wifiIds;
					}
					break;
					case PublicConsts.TRIGGER_TYPE_APP_LAUNCHED: case PublicConsts.TRIGGER_TYPE_APP_CLOSED:{
						trigger_values=new String[taskitem.package_names.length];
						System.arraycopy(taskitem.package_names,0,trigger_values,0,taskitem.package_names.length);
					}
					break;
				}
				//if(trigger_values==null) break;
				i.putExtra(Triggers.EXTRA_TRIGGER_VALUES,trigger_values);
				startActivityForResult(i,REQUEST_CODE_TRIGGERS);
			}
			break;

			case R.id.layout_taskgui_area_exception_bluetooth_disabled: case R.id.layout_taskgui_area_exception_bluetooth_enabled:
			case R.id.layout_taskgui_area_exception_lockscreen: case R.id.layout_taskgui_area_exception_unlockscreen:
			case R.id.layout_taskgui_area_exception_wifi_disabled: case R.id.layout_taskgui_area_exception_wifi_enabled:
			case R.id.layout_taskgui_area_exception_ring_normal: case R.id.layout_taskgui_area_exception_ring_off: case R.id.layout_taskgui_area_exception_ring_vibrate:
			case R.id.layout_taskgui_area_exception_net_disabled: case R.id.layout_taskgui_area_exception_net_enabled: case R.id.layout_taskgui_area_exception_additem:
			case R.id.layout_taskgui_area_exception_gps_enabled: case R.id.layout_taskgui_area_exception_gps_disabled:
			case R.id.layout_taskgui_area_exception_airplane_mode_enabled: case R.id.layout_taskgui_area_exception_airplane_mode_disabled:
			case R.id.layout_taskgui_area_exception_battery_percentage: case R.id.layout_taskgui_area_exception_battery_temperature:
			case R.id.layout_taskgui_area_exception_day_of_week: case R.id.layout_taskgui_area_exception_period:{
				Intent i=new Intent(this,Exceptions.class);
				i.putExtra(Exceptions.INTENT_EXTRA_EXCEPTIONS,taskitem.exceptions);
				i.putExtra(Exceptions.INTENT_EXTRA_TRIGGER_TYPE,taskitem.trigger_type);
				i.putExtra(EXTRA_TITLE_COLOR,taskitem.addition_title_color);
				this.startActivityForResult(i,REQUEST_CODE_EXCEPTIONS);

			}
			break;
			case R.id.layout_taskgui_exception_bluetooth_disabled_cancel:
				//taskitem.exception[PublicConsts.EXCEPTION_BLUETOOTH_DISABLED]=false;
				taskitem.exceptions[PublicConsts.EXCEPTION_BLUETOOTH_DISABLED]=String.valueOf(0);
				this.refreshExceptionViews();
				break;
			case R.id.layout_taskgui_exception_bluetooth_enabled_cancel:
				//taskitem.exception[PublicConsts.EXCEPTION_BLUETOOTH_ENABLED]=false;
				taskitem.exceptions[PublicConsts.EXCEPTION_BLUETOOTH_ENABLED]=String.valueOf(0);
				this.refreshExceptionViews();
				break;
			case R.id.layout_taskgui_exception_wifi_enabled_cancel:
				//taskitem.exception[PublicConsts.EXCEPTION_WIFI_ENABLED]=false;
				taskitem.exceptions[PublicConsts.EXCEPTION_WIFI_ENABLED]=String.valueOf(0);
				this.refreshExceptionViews();
				break;
			case R.id.layout_taskgui_exception_wifi_disabled_cancel:
				//taskitem.exception[PublicConsts.EXCEPTION_WIFI_DISABLED]=false;
				taskitem.exceptions[PublicConsts.EXCEPTION_WIFI_DISABLED]=String.valueOf(0);
				this.refreshExceptionViews();
				break;
			case R.id.layout_taskgui_exception_ring_off_cancel:
				//taskitem.exception[PublicConsts.EXCEPTION_RING_OFF]=false;
				taskitem.exceptions[PublicConsts.EXCEPTION_RING_OFF]=String.valueOf(0);
				this.refreshExceptionViews();
				break;
			case R.id.layout_taskgui_exception_ring_vibrate_cancel:
				//taskitem.exception[PublicConsts.EXCEPTION_RING_VIBRATE]=false;
				taskitem.exceptions[PublicConsts.EXCEPTION_RING_VIBRATE]=String.valueOf(0);
				this.refreshExceptionViews();
				break;
			case R.id.layout_taskgui_exception_ring_normal_cancel:
				//taskitem.exception[PublicConsts.EXCEPTION_RING_NORMAL]=false;
				taskitem.exceptions[PublicConsts.EXCEPTION_RING_NORMAL]=String.valueOf(0);
				this.refreshExceptionViews();
				break;
			case R.id.layout_taskgui_exception_net_enabled_cancel:
				//taskitem.exception[PublicConsts.EXCEPTION_NET_ENABLED]=false;
				taskitem.exceptions[PublicConsts.EXCEPTION_NET_ENABLED]=String.valueOf(0);
				this.refreshExceptionViews();
				break;
			case R.id.layout_taskgui_exception_net_disabled_cancel:
				//taskitem.exception[PublicConsts.EXCEPTION_NET_DISABLED]=false;
				taskitem.exceptions[PublicConsts.EXCEPTION_NET_DISABLED]=String.valueOf(0);
				this.refreshExceptionViews();
				break;
			case R.id.layout_taskgui_exception_lockscreen_cancel:
				//taskitem.exception[PublicConsts.EXCEPTION_LOCKEDSCREEN]=false;
				taskitem.exceptions[PublicConsts.EXCEPTION_LOCKEDSCREEN]=String.valueOf(0);
				this.refreshExceptionViews();
				break;
			case R.id.layout_taskgui_exception_unlockscreen_cancel:
				taskitem.exceptions[PublicConsts.EXCEPTION_UNLOCKEDSCREEN]=String.valueOf(0);
				this.refreshExceptionViews();
				break;
			case R.id.layout_taskgui_area_exception_battery_percentage_cancel:{
				taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]=String.valueOf(-1);
				taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]=String.valueOf(-1);
				refreshExceptionViews();
			}
			break;
			case R.id.layout_taskgui_area_exception_battery_temperature_cancel:{
				taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]=String.valueOf(-1);
				taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]=String.valueOf(-1);
				refreshExceptionViews();
			}
			break;
			case R.id.layout_taskgui_exception_gps_disabled_cancel:{
				taskitem.exceptions[PublicConsts.EXCEPTION_GPS_DISABLED]=String.valueOf(0);
				refreshExceptionViews();
			}
			break;
			case R.id.layout_taskgui_exception_gps_enabled_cancel:{
				taskitem.exceptions[PublicConsts.EXCEPTION_GPS_ENABLED]=String.valueOf(0);
				refreshExceptionViews();
			}
			break;
			case R.id.layout_taskgui_area_exception_airplane_mode_disabled_cancel:{
				taskitem.exceptions[PublicConsts.EXCEPTION_AIRPLANE_MODE_DISABLED]=String.valueOf(0);
				refreshExceptionViews();
			}
			break;
			case R.id.layout_taskgui_area_exception_airplane_mode_enabled_cancel:{
				taskitem.exceptions[PublicConsts.EXCEPTION_AIRPLANE_MODE_ENABLED]=String.valueOf(0);
				refreshExceptionViews();
			}
			break;
			case R.id.layout_taskgui_area_exception_day_of_week_cancel:{
				taskitem.exceptions[PublicConsts.EXCEPTION_MONDAY]=String.valueOf(0);
				taskitem.exceptions[PublicConsts.EXCEPTION_TUESDAY]=String.valueOf(0);
				taskitem.exceptions[PublicConsts.EXCEPTION_WEDNESDAY]=String.valueOf(0);
				taskitem.exceptions[PublicConsts.EXCEPTION_THURSDAY]=String.valueOf(0);
				taskitem.exceptions[PublicConsts.EXCEPTION_FRIDAY]=String.valueOf(0);
				taskitem.exceptions[PublicConsts.EXCEPTION_SATURDAY]=String.valueOf(0);
				taskitem.exceptions[PublicConsts.EXCEPTION_SUNDAY]=String.valueOf(0);
				refreshExceptionViews();
			}
			break;
			case R.id.layout_taskgui_area_exception_period_cancel:{
				taskitem.exceptions[PublicConsts.EXCEPTION_START_TIME]=String.valueOf(-1);
				taskitem.exceptions[PublicConsts.EXCEPTION_END_TIME]=String.valueOf(-1);
				refreshExceptionViews();
			}
			break;
			case R.id.layout_taskgui_area_action_additem:
			case R.id.taskgui_operations_area_wifi: case R.id.taskgui_operations_area_bluetooth: case R.id.taskgui_operations_area_ring_mode:
			case R.id.taskgui_operations_area_ring_volume: case R.id.taskgui_operations_area_ring_selection: case R.id.taskgui_operations_area_brightness:
			case R.id.taskgui_operations_area_vibrate: case R.id.taskgui_operations_area_wallpaper: case R.id.taskgui_operations_area_sms:
			case R.id.taskgui_operations_area_notification: case R.id.taskgui_operations_area_net: case R.id.taskgui_operations_area_gps:
			case R.id.taskgui_operations_area_airplane_mode: case R.id.taskgui_operations_area_devicecontrol: case R.id.taskgui_operations_area_toast:
			case R.id.taskgui_operations_area_enable: case R.id.taskgui_operations_area_disable: case R.id.taskgui_operations_area_app_launch:
			case R.id.taskgui_operations_area_app_close:{
				Intent i=new Intent(this,Actions.class);
				i.putExtra(Actions.EXTRA_TASK_ID,taskitem.id);
				i.putExtra(Actions.EXTRA_ACTIONS,taskitem.actions);
				i.putExtra(Actions.EXTRA_ACTION_URI_RING_NOTIFICATION,taskitem.uri_ring_notification);
				i.putExtra(Actions.EXTRA_ACTION_URI_RING_CALL,taskitem.uri_ring_call);
				i.putExtra(Actions.EXTRA_ACTION_URI_WALLPAPER_DESKTOP,taskitem.uri_wallpaper_desktop);
				i.putExtra(Actions.EXTRA_ACTION_NOTIFICATION_TITLE,taskitem.notification_title);
				i.putExtra(Actions.EXTRA_ACTION_NOTIFICATION_MESSAGE,taskitem.notification_message);
				i.putExtra(Actions.EXTRA_ACTION_TOAST,taskitem.toast);
				i.putExtra(Actions.EXTRA_ACTION_SMS_ADDRESS,taskitem.sms_address);
				i.putExtra(Actions.EXTRA_ACTION_SMS_MESSAGE,taskitem.sms_message);
				i.putExtra(EXTRA_TITLE_COLOR,taskitem.addition_title_color);
				startActivityForResult(i,REQUEST_CODE_ACTIONS);
			}
			break;

			case R.id.layout_taskgui_area_additional_autodelete:{
				CheckBox cb_autodelete=findViewById(R.id.layout_taskgui_area_additional_autodelete_cb);
				cb_autodelete.toggle();
				if(taskitem.trigger_type!=PublicConsts.TRIGGER_TYPE_SINGLE) setAutoCloseAreaEnabled(!cb_autodelete.isChecked());
				taskitem.autodelete=cb_autodelete.isChecked();
			}
			break;
			case R.id.layout_taskgui_area_additional_autoclose:{
				CheckBox cb_autoclose=findViewById(R.id.layout_taskgui_area_additional_autoclose_cb);
				if(cb_autoclose.isEnabled()) {
					cb_autoclose.toggle();
					taskitem.autoclose=cb_autoclose.isChecked();
				}
			}
			break;
			case R.id.layout_taskgui_additional_titlecolor:{
				DialogForColor dialog=new DialogForColor(this,taskitem.addition_title_color);
				dialog.show();
				dialog.setOnDialogConfirmListener(new DialogForColor.OnDialogForColorConfirmedListener() {
					@Override
					public void onConfirmed(String color) {
						taskitem.addition_title_color=color;
						try{
							findViewById(R.id.layout_taskgui_additional_titlecolor_img).setBackgroundColor(Color.parseColor(taskitem.addition_title_color));
							setTaskThemeColor(taskitem.addition_title_color);
						}catch (Exception e){
							e.printStackTrace();
						}
					}
				});
			}
			break;
		}
		
	}

	private void setAutoCloseAreaEnabled(boolean b){
		RelativeLayout rl_autoclose=findViewById(R.id.layout_taskgui_area_additional_autoclose);
		CheckBox cb_autoclose=findViewById(R.id.layout_taskgui_area_additional_autoclose_cb);
		TextView tv_autoclose=findViewById(R.id.layout_taskgui_additional_autoclose_att);
		rl_autoclose.setClickable(b);
		cb_autoclose.setEnabled(b);
		tv_autoclose.setTextColor(b?getResources().getColor(R.color.color_text_normal):getResources().getColor(R.color.color_text_disabled));
		if(!b) {
			cb_autoclose.setChecked(true);
			taskitem.autoclose=true;
		}else{
			cb_autoclose.setChecked(taskitem.autoclose);
		}
	}

	/*@Override
	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		// TODO Auto-generated method stub
		calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
		calendar.set(Calendar.MINUTE,minute);
		calendar.set(Calendar.SECOND,0);
		if(taskitem.trigger_type ==PublicConsts.TRIGGER_TYPE_SINGLE) refreshSingleTimeTextView();
		
	}  */
	
	/*@Override
	public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
		// TODO Auto-generated method stub
		this.calendar.set(year, month, dayOfMonth);
		TaskGui.this.activateTriggerType(PublicConsts.TRIGGER_TYPE_SINGLE);
	}  */
	
	
	public void refreshExceptionViews(){
		try{
			findViewById(R.id.layout_taskgui_area_exception_lockscreen).setVisibility(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_LOCKEDSCREEN])==1?View.VISIBLE:View.GONE);
			findViewById(R.id.layout_taskgui_area_exception_unlockscreen).setVisibility(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_UNLOCKEDSCREEN])==1?View.VISIBLE:View.GONE);
			findViewById(R.id.layout_taskgui_area_exception_ring_vibrate).setVisibility(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_RING_VIBRATE])==1?View.VISIBLE:View.GONE);
			findViewById(R.id.layout_taskgui_area_exception_ring_off).setVisibility(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_RING_OFF])==1?View.VISIBLE:View.GONE);
			findViewById(R.id.layout_taskgui_area_exception_ring_normal).setVisibility(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_RING_NORMAL])==1?View.VISIBLE:View.GONE);
			findViewById(R.id.layout_taskgui_area_exception_wifi_enabled).setVisibility(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_WIFI_ENABLED])==1?View.VISIBLE:View.GONE);
			findViewById(R.id.layout_taskgui_area_exception_wifi_disabled).setVisibility(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_WIFI_DISABLED])==1?View.VISIBLE:View.GONE);
			findViewById(R.id.layout_taskgui_area_exception_bluetooth_enabled).setVisibility(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_BLUETOOTH_ENABLED])==1?View.VISIBLE:View.GONE);
			findViewById(R.id.layout_taskgui_area_exception_bluetooth_disabled).setVisibility(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_BLUETOOTH_DISABLED])==1?View.VISIBLE:View.GONE);
			findViewById(R.id.layout_taskgui_area_exception_net_enabled).setVisibility(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_NET_ENABLED])==1?View.VISIBLE:View.GONE);
			findViewById(R.id.layout_taskgui_area_exception_net_disabled).setVisibility(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_NET_DISABLED])==1?View.VISIBLE:View.GONE);
			findViewById(R.id.layout_taskgui_area_exception_gps_enabled).setVisibility(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_GPS_ENABLED])==1?View.VISIBLE:View.GONE);
			findViewById(R.id.layout_taskgui_area_exception_gps_disabled).setVisibility(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_GPS_DISABLED])==1?View.VISIBLE:View.GONE);
			findViewById(R.id.layout_taskgui_area_exception_airplane_mode_enabled).setVisibility(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_AIRPLANE_MODE_ENABLED])==1?View.VISIBLE:View.GONE);
			findViewById(R.id.layout_taskgui_area_exception_airplane_mode_disabled).setVisibility(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_AIRPLANE_MODE_DISABLED])==1?View.VISIBLE:View.GONE);
			((TextView)findViewById(R.id.layout_taskgui_area_exception_battery_percentage_value)).setText(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE])!=-1?(this.getResources().getString(R.string.dialog_battery_compare_more_than)+taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]+"%"):(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE])!=-1?(this.getResources().getString(R.string.dialog_battery_compare_less_than)+taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]+"%"):""));
			((TextView)findViewById(R.id.layout_taskgui_area_exception_battery_temperature_value)).setText(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE])!=-1?(this.getResources().getString(R.string.dialog_battery_compare_higher_than)+taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]+"℃"):(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE])!=-1?(this.getResources().getString(R.string.dialog_battery_compare_lower_than)+taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]+"℃"):""));
			findViewById(R.id.layout_taskgui_area_exception_battery_percentage).setVisibility((Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE])!=-1||Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE])!=-1)?View.VISIBLE:View.GONE);
			findViewById(R.id.layout_taskgui_area_exception_battery_temperature).setVisibility((Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE])!=-1||Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE])!=-1)?View.VISIBLE:View.GONE);

			StringBuilder value=new StringBuilder("");
			value.append(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_MONDAY])==1?getResources().getString(R.string.monday)+" ":"");
			value.append(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_TUESDAY])==1?getResources().getString(R.string.tuesday)+" ":"");
			value.append(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_WEDNESDAY])==1?getResources().getString(R.string.wednesday)+" ":"");
			value.append(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_THURSDAY])==1?getResources().getString(R.string.thursday)+" ":"");
			value.append(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_FRIDAY])==1?getResources().getString(R.string.friday)+" ":"");
			value.append(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_SATURDAY])==1?getResources().getString(R.string.saturday)+' ':"");
			value.append(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_SUNDAY])==1?getResources().getString(R.string.sunday)+" ":"");
			((TextView)findViewById(R.id.layout_taskgui_area_exception_day_of_week_value)).setText(value.toString().equals("")?getResources().getString(R.string.not_activated):value.toString());
			findViewById(R.id.layout_taskgui_area_exception_day_of_week).setVisibility(value.toString().equals("")?View.GONE:View.VISIBLE);

			findViewById(R.id.layout_taskgui_area_exception_period).setVisibility((Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_START_TIME])>=0&&Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_END_TIME])>=0)?View.VISIBLE:View.GONE);
			((TextView)findViewById(R.id.layout_taskgui_area_exception_period_value)).setText((Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_START_TIME])!=-1&&Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_END_TIME])!=-1)?
					ValueUtils.format(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_START_TIME])/60)+":"+ ValueUtils.format(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_START_TIME])%60)+"~"+ ValueUtils.format(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_END_TIME])/60)+":"+ ValueUtils.format(Integer.parseInt(taskitem.exceptions[PublicConsts.EXCEPTION_END_TIME])%60)
					:getResources().getString(R.string.not_activated));
		}catch (NumberFormatException ne){
			ne.printStackTrace();
			LogUtil.putExceptionLog(TaskGui.this,ne);
		}

	}

	/**
	 * 将当前实例中的TaskItem插入到数据库中
	 * @param id 任务的ID，为空时则在数据库新建一行，不为空时则更新指定ID的行
	 * @return 插入或者更新数据的结果,插入失败时返回-1，更新失败时返回0
	 */
	public long saveTaskItem2DB(@Nullable Integer id){
		boolean isNull=true;
		for(int i=0;i<taskitem.actions.length;i++){//for(int i=0;i<actions.length;i++){
			try{
				if(i==PublicConsts.ACTION_RING_VOLUME_LOCALE){
					String volume_values[]=taskitem.actions[PublicConsts.ACTION_RING_VOLUME_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL);
					if(Integer.parseInt(volume_values[PublicConsts.VOLUME_RING_LOCALE])>=0||Integer.parseInt(volume_values[PublicConsts.VOLUME_MEDIA_LOCALE])>=0
							||Integer.parseInt(volume_values[PublicConsts.VOLUME_NOTIFICATION_LOCALE])>=0||Integer.parseInt(volume_values[PublicConsts.VOLUME_ALARM_LOCALE])>=0)
						isNull=false;
				}
				else if(i==PublicConsts.ACTION_RING_SELECTION_LOCALE){
					if(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_RING_SELECTION_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL)[PublicConsts.RING_SELECTION_NOTIFICATION_TYPE_LOCALE])>=0
							||Integer.parseInt(taskitem.actions[PublicConsts.ACTION_RING_SELECTION_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL)[PublicConsts.RING_SELECTION_CALL_TYPE_LOCALE])>=0) isNull=false;
				}
				else if(i==PublicConsts.ACTION_SET_WALL_PAPER_LOCALE){
					if(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_SET_WALL_PAPER_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[0])>=0) isNull=false;
				}
				else if(i==PublicConsts.ACTION_VIBRATE_LOCALE){
					if(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_VIBRATE_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[PublicConsts.VIBRATE_FREQUENCY_LOCALE])>0) isNull=false;
				}
				else if(i==PublicConsts.ACTION_NOTIFICATION_LOCALE){
					if(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_NOTIFICATION_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[PublicConsts.NOTIFICATION_TYPE_LOCALE])!=PublicConsts.NOTIFICATION_TYPE_UNSELECTED) isNull=false;
				}
				else if(i==PublicConsts.ACTION_SMS_LOCALE){
					if(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_SMS_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[0])>=0) isNull=false;
				}
				else if(i==PublicConsts.ACTION_TOAST_LOCALE){
					if(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_TOAST_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[PublicConsts.TOAST_TYPE_LOCALE])>=0) isNull=false;
				}
				else if(i==PublicConsts.ACTION_ENABLE_TASKS_LOCALE){
					if(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_ENABLE_TASKS_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL)[0])>=0) isNull=false;
				}
				else if(i==PublicConsts.ACTION_DISABLE_TASKS_LOCALE){
					if(Integer.parseInt(taskitem.actions[PublicConsts.ACTION_DISABLE_TASKS_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL)[0])>=0) isNull=false;
				}else if(i==PublicConsts.ACTION_LAUNCH_APP_PACKAGES){
					if(!taskitem.actions[PublicConsts.ACTION_LAUNCH_APP_PACKAGES].equals(String.valueOf(-1))) isNull=false;
				}else if(i==PublicConsts.ACTION_STOP_APP_PACKAGES){
					if(!taskitem.actions[PublicConsts.ACTION_STOP_APP_PACKAGES].equals(String.valueOf(-1))) isNull=false;
				}
				else if(Integer.parseInt(taskitem.actions[i])!= PublicConsts.ACTION_UNSELECTED) isNull=false;
			}catch (NumberFormatException ne){
				ne.printStackTrace();
				LogUtil.putExceptionLog(TaskGui.this,ne);
				return -1;
			}
		}
		if(isNull) {
			Snackbar.make(findViewById(R.id.layout_taskgui_root),getResources().getString(R.string.activity_taskgui_toast_no_actions),Snackbar.LENGTH_SHORT).show();
			return -1;
		}
		if(taskitem.trigger_type ==PublicConsts.TRIGGER_TYPE_SINGLE&&taskitem.time<System.currentTimeMillis()){
			Snackbar.make(findViewById(R.id.layout_taskgui_root),getResources().getString(R.string.activity_taskgui_toast_time_invalid),Snackbar.LENGTH_SHORT).show();
			return -1;
		}

		SQLiteDatabase db= MySQLiteOpenHelper.getInstance(this).getWritableDatabase();
		ContentValues values=new ContentValues();

		values.put(SQLConsts.SQL_TASK_COLUMN_NAME,taskitem.name);

		String[] triggerValues=new String[1];
		if(this.taskitem.trigger_type==PublicConsts.TRIGGER_TYPE_SINGLE){//0（仅一次）--{time};
			triggerValues=new String[1];
			triggerValues[0]=String.valueOf(taskitem.time);
		}
		if(this.taskitem.trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME){//1（指定时间长度重复） --{time,period};
			triggerValues=new String[2];
			triggerValues[0]=String.valueOf(System.currentTimeMillis());
			triggerValues[1]=String.valueOf(this.taskitem.interval_milliseconds);
		}
		if(this.taskitem.trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_WEEK){//2(周重复) --{time,SUNDAY,MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY};
			triggerValues=new String[8];
			triggerValues[0]=String.valueOf(taskitem.time);
			for(int i=1;i<triggerValues.length;i++){
				triggerValues[i]=String.valueOf(this.taskitem.week_repeat[i-1]?1:0);
			}
		}
		if(this.taskitem.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE||this.taskitem.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE){ //3,4(电池电量低于/高于某值)  --{percent}
			triggerValues=new String[1];
			triggerValues[0]=String.valueOf(this.taskitem.battery_percentage);
		}
		if(this.taskitem.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE||this.taskitem.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE){  //5,6(电池温度低于/高于某值) --{temperature}
			triggerValues=new String[1];
			triggerValues[0]=String.valueOf(this.taskitem.battery_temperature);
		}
		if(taskitem.trigger_type==PublicConsts.TRIGGER_TYPE_RECEIVED_BROADCAST){
			triggerValues=new String[1];
			triggerValues[0]=String.valueOf(taskitem.selectedAction);
		}
		if(taskitem.trigger_type==PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED||taskitem.trigger_type==PublicConsts.TRIGGER_TYPE_WIFI_DISCONNECTED){
			triggerValues=new String[1];
			triggerValues[0]=String.valueOf(taskitem.wifiIds);
		}
		if(taskitem.trigger_type==PublicConsts.TRIGGER_TYPE_APP_LAUNCHED||taskitem.trigger_type==PublicConsts.TRIGGER_TYPE_APP_CLOSED){
		    triggerValues=taskitem.package_names;
		    if(triggerValues==null||triggerValues.length==0) triggerValues=new String[1];
        }

		values.put(SQLConsts.SQL_TASK_COLUMN_ENABLED,taskitem.isenabled?1:0);
		values.put(SQLConsts.SQL_TASK_COLUMN_TYPE,taskitem.trigger_type);
		values.put(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES, ValueUtils.stringArray2String(triggerValues));
		values.put(SQLConsts.SQL_TASK_COLUMN_EXCEPTIONS,ValueUtils.stringArray2String(taskitem.exceptions));
		values.put(SQLConsts.SQL_TASK_COLUMN_ACTIONS,ValueUtils.stringArray2String(taskitem.actions));
		values.put(SQLConsts.SQL_TASK_COLUMN_URI_RING_NOTIFICATION,taskitem.uri_ring_notification);
		values.put(SQLConsts.SQL_TASK_COLUMN_URI_RING_CALL,taskitem.uri_ring_call);
		values.put(SQLConsts.SQL_TASK_COLUMN_URI_WALLPAPER_DESKTOP,taskitem.uri_wallpaper_desktop);
		values.put(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_TITLE,taskitem.notification_title);
		values.put(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_MESSAGE,taskitem.notification_message);
		values.put(SQLConsts.SQL_TASK_COLUMN_TOAST,taskitem.toast);
		values.put(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_PHONE_NUMBERS,taskitem.sms_address);
		values.put(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_MESSAGE,taskitem.sms_message);
		String [] additions=new String[PublicConsts.ADDITION_LENGTH];
		for(int i=0;i<additions.length;i++) additions[i]=String.valueOf(-1);
		additions[PublicConsts.ADDITION_NOTIFY]=this.taskitem.notify?String.valueOf(1):String.valueOf(0);
		additions[PublicConsts.ADDITION_AUTO_DELETE]=this.taskitem.autodelete?String.valueOf(1):String.valueOf(0);
		additions[PublicConsts.ADDITION_AUTO_CLOSE]=this.taskitem.autoclose?String.valueOf(1):String.valueOf(0);
		additions[PublicConsts.ADDITION_TITLE_COLOR_LOCALE]=taskitem.addition_title_color;
		additions[PublicConsts.ADDITION_EXCEPTION_CONNECTOR_LOCALE]=taskitem.addition_exception_connector;
		additions[PublicConsts.ADDITION_TITLE_FOLDED_VALUE_LOCALE]=taskitem.addition_isFolded?String.valueOf(0):String.valueOf(-1);
		values.put(SQLConsts.SQL_TASK_COLUMN_ADDITIONS,ValueUtils.stringArray2String(additions));
		if(id==null) return db.insert(SQLConsts.getCurrentTableName(this),null,values);
		else{
			Log.d("UPDATE","id is "+id);
			return db.update(SQLConsts.getCurrentTableName(this),values,SQLConsts.SQL_TASK_COLUMN_ID +"="+id,null);
		}
	}

	/**
	 * 将dp值转换为px
	 */
	public int dp2px(int dp){
		return DisplayDensity.dip2px(this, dp);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
			default:break;
			case REQUEST_CODE_TRIGGERS:{
				if(resultCode==RESULT_OK){
					if(data==null) return;
					taskitem.trigger_type=data.getIntExtra(Triggers.EXTRA_TRIGGER_TYPE,0);
					switch (taskitem.trigger_type){
						default:break;
						case PublicConsts.TRIGGER_TYPE_SINGLE:{
							try{
								taskitem.time=Long.parseLong(data.getStringArrayExtra(Triggers.EXTRA_TRIGGER_VALUES)[0]);
							}catch (Exception e){
								e.printStackTrace();
								LogUtil.putExceptionLog(this,e);
							}
							clearExceptionsOfTimeType();
						}
						break;
						case PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME:{
							try {
								taskitem.interval_milliseconds=Long.parseLong(data.getStringArrayExtra(Triggers.EXTRA_TRIGGER_VALUES)[0]);
							}catch (Exception e){
								e.printStackTrace();
								LogUtil.putExceptionLog(this,e);
							}
						}
						break;
						case PublicConsts.TRIGGER_TYPE_LOOP_WEEK:{
							try{
								taskitem.time=Long.parseLong(data.getStringArrayExtra(Triggers.EXTRA_TRIGGER_VALUES)[0]);
								for(int i=1;i<data.getStringArrayExtra(Triggers.EXTRA_TRIGGER_VALUES).length;i++){
									taskitem.week_repeat[i-1]=Integer.parseInt(data.getStringArrayExtra(Triggers.EXTRA_TRIGGER_VALUES)[i])==1;
								}
							}catch (Exception e){
								e.printStackTrace();
								LogUtil.putExceptionLog(this,e);
							}
							clearExceptionsOfTimeType();
						}
						break;
						case PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE: case PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE:{
							try{
								taskitem.battery_percentage=Integer.parseInt(data.getStringArrayExtra(Triggers.EXTRA_TRIGGER_VALUES)[0]);
							}catch (Exception e){
								e.printStackTrace();
								LogUtil.putExceptionLog(this,e);
							}
							clearExceptionsOfBatteryPercentage();
						}
						break;
						case PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE: case PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE:{
							try{
								taskitem.battery_temperature=Integer.parseInt(data.getStringArrayExtra(Triggers.EXTRA_TRIGGER_VALUES)[0]);
							}catch (Exception e){
								e.printStackTrace();
								LogUtil.putExceptionLog(this,e);
							}
							clearExceptionsOfBatteryTemperature();
						}
						break;
						case PublicConsts.TRIGGER_TYPE_RECEIVED_BROADCAST:{
							try{
								taskitem.selectedAction=String.valueOf(data.getStringArrayExtra(Triggers.EXTRA_TRIGGER_VALUES)[0]);
							}catch (Exception e){
								e.printStackTrace();
								LogUtil.putExceptionLog(this,e);
							}
						}
						break;
						case PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED: case PublicConsts.TRIGGER_TYPE_WIFI_DISCONNECTED:{
							//Log.d("wifi ssids ",data.getStringArrayExtra(Triggers.EXTRA_TRIGGER_VALUES)[0]);
							taskitem.wifiIds=data.getStringArrayExtra(Triggers.EXTRA_TRIGGER_VALUES)[0];
						}
						break;
						case PublicConsts.TRIGGER_TYPE_APP_LAUNCHED: case PublicConsts.TRIGGER_TYPE_APP_CLOSED:{
							taskitem.package_names=data.getStringArrayExtra(Triggers.EXTRA_TRIGGER_VALUES);
						}
						break;
					}
					refreshTriggerDisplayValue();

					if(taskitem.trigger_type==PublicConsts.TRIGGER_TYPE_SINGLE) setAutoCloseAreaEnabled(false);
					else {
						setAutoCloseAreaEnabled(!((CheckBox)findViewById(R.id.layout_taskgui_area_additional_autodelete_cb)).isChecked());
					}
				}
			}
			break;
			case REQUEST_CODE_EXCEPTIONS:{
				if(resultCode==Exceptions.EXCEPTIONS_RESULT_SUCCESS) {
					String[] result =null;
					if(data!=null) result=data.getStringArrayExtra(Exceptions.INTENT_EXTRA_EXCEPTIONS);
					if (result != null) taskitem.exceptions = result;
					refreshExceptionViews();
				}
			}
			break;
			case REQUEST_CODE_ACTIONS:{
				if(resultCode==RESULT_OK){
					if(data==null) return;
					taskitem.actions=data.getStringArrayExtra(Actions.EXTRA_ACTIONS);
					taskitem.uri_ring_notification=data.getStringExtra(Actions.EXTRA_ACTION_URI_RING_NOTIFICATION);
					taskitem.uri_ring_call=data.getStringExtra(Actions.EXTRA_ACTION_URI_RING_CALL);
					taskitem.uri_wallpaper_desktop=data.getStringExtra(Actions.EXTRA_ACTION_URI_WALLPAPER_DESKTOP);
					taskitem.sms_address=data.getStringExtra(Actions.EXTRA_ACTION_SMS_ADDRESS);
					taskitem.sms_message=data.getStringExtra(Actions.EXTRA_ACTION_SMS_MESSAGE);
					taskitem.notification_title=data.getStringExtra(Actions.EXTRA_ACTION_NOTIFICATION_TITLE);
					taskitem.notification_message=data.getStringExtra(Actions.EXTRA_ACTION_NOTIFICATION_MESSAGE);
					taskitem.toast=data.getStringExtra(Actions.EXTRA_ACTION_TOAST);
					refreshActionStatus();
				}
			}
			break;
		}
	}

	@Override
	public void finish(){
		super.finish();
		//if(linkedlist.contains(this)) linkedlist.remove(this);
	}

	private void clearExceptionsOfTimeType(){
        taskitem.exceptions[PublicConsts.EXCEPTION_START_TIME]=String.valueOf(-1);
        taskitem.exceptions[PublicConsts.EXCEPTION_END_TIME]=String.valueOf(-1);
        taskitem.exceptions[PublicConsts.EXCEPTION_MONDAY]=String.valueOf(0);
        taskitem.exceptions[PublicConsts.EXCEPTION_TUESDAY]=String.valueOf(0);
        taskitem.exceptions[PublicConsts.EXCEPTION_WEDNESDAY]=String.valueOf(0);
        taskitem.exceptions[PublicConsts.EXCEPTION_THURSDAY]=String.valueOf(0);
        taskitem.exceptions[PublicConsts.EXCEPTION_FRIDAY]=String.valueOf(0);
        taskitem.exceptions[PublicConsts.EXCEPTION_SATURDAY]=String.valueOf(0);
        taskitem.exceptions[PublicConsts.EXCEPTION_SUNDAY]=String.valueOf(0);
        refreshExceptionViews();
    }

    private void clearExceptionsOfBatteryPercentage(){
	    taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]=String.valueOf(-1);
	    taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]=String.valueOf(-1);
	    refreshExceptionViews();
    }

    private void clearExceptionsOfBatteryTemperature(){
	    taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]=String.valueOf(-1);
	    taskitem.exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]=String.valueOf(-1);
	    refreshExceptionViews();
    }

    private class BroadcastSelectionAdapter extends BaseAdapter{
		List<String> intent_list=new ArrayList<>();
		int selectedPosition=0;
		private BroadcastSelectionAdapter(@Nullable String selectedAction){
			intent_list.add(Intent.ACTION_ANSWER);
			intent_list.add(Intent.ACTION_BATTERY_LOW);
			intent_list.add(Intent.ACTION_MEDIA_BAD_REMOVAL);
			intent_list.add(Intent.ACTION_PACKAGE_REMOVED);
			intent_list.add(Intent.ACTION_POWER_CONNECTED);
			intent_list.add(Intent.ACTION_POWER_DISCONNECTED);
			intent_list.add(WifiManager.WIFI_STATE_CHANGED_ACTION);
			intent_list.add(Intent.ACTION_PACKAGE_CHANGED);
			intent_list.add(Intent.ACTION_SCREEN_OFF);
			intent_list.add(Intent.ACTION_SCREEN_ON);
			intent_list.add(Intent.ACTION_PACKAGE_REMOVED);
			intent_list.add(Intent.ACTION_PACKAGE_ADDED);
			intent_list.add(ConnectivityManager.CONNECTIVITY_ACTION);
			if(selectedAction==null) return;
			for(int i=0;i<intent_list.size();i++){
				if(selectedAction.equals(intent_list.get(i))) {
					selectedPosition=i;
					break;
				}
			}
		}
		@Override
		public int getCount() {
			return intent_list.size();
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
		public View getView(int i, View view, ViewGroup viewGroup) {
			if(view==null){
				view=LayoutInflater.from(TaskGui.this).inflate(R.layout.item_broadcast_intent,viewGroup,false);
			}
			((RadioButton)view.findViewById(R.id.item_broadcast_ra)).setText(intent_list.get(i));
			((RadioButton)view.findViewById(R.id.item_broadcast_ra)).setChecked(i==selectedPosition);
			return view;
		}

		public void onItemClicked(int position){
			selectedPosition=position;
			notifyDataSetChanged();
		}

		public String getSelectedAction(){
			return intent_list.get(selectedPosition);
		}
	}
		
}