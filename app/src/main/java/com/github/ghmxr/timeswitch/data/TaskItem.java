package com.github.ghmxr.timeswitch.data;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.github.ghmxr.timeswitch.receivers.APReceiver;
import com.github.ghmxr.timeswitch.receivers.AirplaneModeReceiver;
import com.github.ghmxr.timeswitch.receivers.AlarmReceiver;
import com.github.ghmxr.timeswitch.receivers.AppLaunchDetectionReceiver;
import com.github.ghmxr.timeswitch.receivers.BatteryReceiver;
import com.github.ghmxr.timeswitch.receivers.BluetoothReceiver;
import com.github.ghmxr.timeswitch.receivers.CustomBroadcastReceiver;
import com.github.ghmxr.timeswitch.receivers.HeadsetPlugReceiver;
import com.github.ghmxr.timeswitch.receivers.NetworkReceiver;
import com.github.ghmxr.timeswitch.receivers.RingModeReceiver;
import com.github.ghmxr.timeswitch.services.AppLaunchingDetectionService;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.timers.CustomTimerTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class TaskItem implements Comparable<TaskItem>{
	
	/**
	 * 控制排序的静态变量值。
	 */
	public static int SortConfig=0;

	/**
	 * 任务id
	 *
	 */
	public int id=0;
	
	/**
	 * 每个定时设定的指定的名称。
	 *
	 */
	public String name="";

	/**
	 * 本定时任务是否激活
	 */
	public boolean isenabled=true;

	/**
	 * 时间--绝对时间。用于single 和 weekloop
	 */
	public long time=0;

	/**
	 * 触发条件连接/断开指定Wifi的信息(networkid)
	 */
	public String wifiIds="";

	/**
	 * 触发类型
	 */
	public int trigger_type =0;

	/**
	 * 每周重复的布尔值数组
	 */
	public boolean [] week_repeat=new boolean[7];


	/**
	 * 指定重复的间隔时间，单位毫秒
	 */
	public long interval_milliseconds=1000*60*60;

	/**
	 * 电池百分比参数值
	 */
	public int battery_percentage =50;

	/**
	 * 电池温度参数值，单位为摄氏度
	 */
	public int battery_temperature=35;

	public String selectedAction="android.intent.action.ANSWER";
	public String[] package_names=new String[0];

	/**
	 * 触发例外
	 */
	public String [] exceptions=new String[PublicConsts.EXCEPTION_LENTH];

	/**
	 * 任务的动作
	 */
	public String [] actions=new String[PublicConsts.ACTION_LENGTH];

	public String uri_ring_notification="";
	public String uri_ring_call ="";
	public String uri_wallpaper_desktop="";
	public String sms_address="";
	public String sms_message="";
	public String notification_title="";
	public String notification_message="";
	public String toast="";

	/**
	 * @deprecated
	 * 任务是否在通知栏推送通知。
	 */
	public boolean notify=true;

	/**
	 * 任务执行完成后是否自动删除
	 */
	public boolean autodelete=false;

	/**
	 * 任务执行后是否自动关闭
	 */
	public boolean autoclose=false;

	/**
	 *任务的触发器
	 */
	public Object triggerObject=null;

    /**
     * 基本构造方法，初始化变量
     */
	public TaskItem(){
        for(int i=0;i<week_repeat.length;i++){
           week_repeat[i]=true;
        }

        for(int i=0;i<exceptions.length;i++){
        	exceptions[i]=String.valueOf(0);
		}

        exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]=String.valueOf(-1);
        exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]=String.valueOf(-1);
        exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]=String.valueOf(-1);
        exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]=String.valueOf(-1);
        exceptions[PublicConsts.EXCEPTION_START_TIME]=String.valueOf(-1);
        exceptions[PublicConsts.EXCEPTION_END_TIME]=String.valueOf(-1);

        for(int i=0;i<actions.length;i++){
            actions[i]=String.valueOf(-1);
        }

        actions[PublicConsts.ACTION_RING_VOLUME_LOCALE]=String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(-1);
        actions[PublicConsts.ACTION_RING_SELECTION_LOCALE]=String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(-1);
    	//actions[PublicConsts.ACTION_SET_WALL_PAPER_LOCALE]=String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL+String.valueOf(" ");
    	actions[PublicConsts.ACTION_VIBRATE_LOCALE]=String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(-1);
		actions[PublicConsts.ACTION_NOTIFICATION_LOCALE]=String.valueOf(PublicConsts.NOTIFICATION_TYPE_UNSELECTED)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(PublicConsts.NOTIFICATION_TYPE_DEFAULT);
		actions[PublicConsts.ACTION_TOAST_LOCALE]=String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(0)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(0);
		actions[PublicConsts.ACTION_SMS_LOCALE]=String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(0);
	}

    public TaskItem(TaskItem item){
		this.id=item.id;
		this.name=item.name;
		this.isenabled=item.isenabled;
		this.time=item.time;
		//this.hour=item.hour;
		//this.minute=item.minute;
		this.trigger_type=item.trigger_type;
		this.week_repeat=new boolean[item.week_repeat.length];
		for(int i=0;i<this.week_repeat.length;i++) this.week_repeat[i]=item.week_repeat[i];
		//System.arraycopy(item.week_repeat,0,this.week_repeat,item.week_repeat.length);
		this.interval_milliseconds=item.interval_milliseconds;
		this.battery_percentage=item.battery_percentage;
		this.battery_temperature=item.battery_temperature;
		this.package_names=new String[item.package_names.length];
		System.arraycopy(item.package_names,0,this.package_names,0,item.package_names.length);
		//this.exceptions=item.exceptions;
		this.exceptions=new String[item.exceptions.length];
		//for(int i=0;i<exceptions.length;i++) this.exceptions[i]=item.exceptions[i];
		System.arraycopy(item.exceptions,0,this.exceptions,0,item.exceptions.length);
		//this.actions=item.actions;
		this.actions=new String[item.actions.length];
		//for(int i=0;i<item.actions.length;i++) this.actions[i]=item.actions[i];
		System.arraycopy(item.actions,0,this.actions,0,item.actions.length);
		uri_ring_notification=new String(item.uri_ring_notification);
		uri_ring_call =new String(item.uri_ring_call);
		uri_wallpaper_desktop=new String(item.uri_wallpaper_desktop);
		sms_address=new String(item.sms_address);
		sms_message=new String(item.sms_message);
		notification_title=new String(item.notification_title);
		notification_message=new String(item.notification_message);
		toast=new String(item.toast);
		selectedAction=new String(item.selectedAction);
		wifiIds=new String(item.wifiIds);
		this.notify=item.notify;
		this.autodelete=item.autodelete;
		this.autoclose=item.autoclose;
	}

	@Override
	public String toString() {
		return "TaskItem{" +
				"id=" + id +
				", name='" + name + '\'' +
				", trigger_value=" + time +
				", trigger_type=" + trigger_type +
				", week_repeat=" + Arrays.toString(week_repeat) +
				", interval_milliseconds=" + interval_milliseconds +
				", battery_percentage=" + battery_percentage +
				", battery_temperature=" + battery_temperature +
				", wifiIds=" + wifiIds +
				", package_names="+ Arrays.toString(package_names)+
				", exceptions=" + Arrays.toString(exceptions) +
				", actions=" + Arrays.toString(actions) +
				", uri_ring_notification='" + uri_ring_notification + '\'' +
				", uri_ring_call='" + uri_ring_call + '\'' +
				", uri_wallpaper_desktop='" + uri_wallpaper_desktop + '\'' +
				", sms_address='" + sms_address + '\'' +
				", sms_message='" + sms_message + '\'' +
				", notification_title='" + notification_title + '\'' +
				", notification_message='" + notification_message + '\'' +
				", toast='" + toast + '\'' +
				", notify=" + notify +
				", autodelete=" + autodelete +
				", autoclose=" + autoclose +
				'}';
	}

	public void activateTrigger(Context context){
		SharedPreferences settings=context.getSharedPreferences(PublicConsts.PREFERENCES_NAME, Activity.MODE_PRIVATE);
		if(settings.getInt(PublicConsts.PREFERENCES_API_TYPE,PublicConsts.PREFERENCES_API_TYPE_DEFAULT)==PublicConsts.API_JAVA_TIMER){
			if (this.trigger_type == PublicConsts.TRIGGER_TYPE_SINGLE || this.trigger_type == PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME
					|| this.trigger_type == PublicConsts.TRIGGER_TYPE_LOOP_WEEK) {
				triggerObject=new CustomTimerTask(context,this).activateTimerTask();
			}
		}else if(settings.getInt(PublicConsts.PREFERENCES_API_TYPE,PublicConsts.PREFERENCES_API_TYPE_DEFAULT)==PublicConsts.API_ANDROID_ALARM_MANAGER){
			if (this.trigger_type == PublicConsts.TRIGGER_TYPE_SINGLE || this.trigger_type == PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME
					|| this.trigger_type == PublicConsts.TRIGGER_TYPE_LOOP_WEEK){
				Intent intent = new Intent();
				intent.setClass(context, AlarmReceiver.class);
				intent.putExtra(AlarmReceiver.TAG_TASKITEM_ID,id);
				triggerObject= PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				activateTriggerOfAlarmManager((PendingIntent)triggerObject);
			}
		}

		if(trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE||trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE
				||trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE||trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE){
			triggerObject=new BatteryReceiver(context,this);
			((BatteryReceiver)triggerObject).registerReceiver();
		}

		if(trigger_type==PublicConsts.TRIGGER_TYPE_RECEIVED_BROADCAST){
			triggerObject=new CustomBroadcastReceiver(context,selectedAction,this);
			((CustomBroadcastReceiver)triggerObject).registerReceiver();
		}

		if(trigger_type==PublicConsts.TRIGGER_TYPE_SCREEN_ON){
			triggerObject=new CustomBroadcastReceiver(context,Intent.ACTION_SCREEN_ON,this);
			((CustomBroadcastReceiver)triggerObject).registerReceiver();
		}

		if(trigger_type==PublicConsts.TRIGGER_TYPE_SCREEN_OFF){
			triggerObject=new CustomBroadcastReceiver(context,Intent.ACTION_SCREEN_OFF,this);
			((CustomBroadcastReceiver)triggerObject).registerReceiver();
		}

		if(trigger_type==PublicConsts.TRIGGER_TYPE_POWER_CONNECTED){
			triggerObject=new CustomBroadcastReceiver(context,Intent.ACTION_POWER_CONNECTED,this);
			((CustomBroadcastReceiver)triggerObject).registerReceiver();
		}

		if(trigger_type==PublicConsts.TRIGGER_TYPE_POWER_DISCONNECTED){
			triggerObject=new CustomBroadcastReceiver(context,Intent.ACTION_POWER_DISCONNECTED,this);
			((CustomBroadcastReceiver)triggerObject).registerReceiver();
		}

		if(trigger_type==PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED||trigger_type==PublicConsts.TRIGGER_TYPE_WIFI_DISCONNECTED
				||trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_ON||trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF
				||trigger_type==PublicConsts.TRIGGER_TYPE_NET_ON||trigger_type==PublicConsts.TRIGGER_TYPE_NET_OFF){
			triggerObject=new NetworkReceiver(context,this);
			((NetworkReceiver)triggerObject).registerReceiver();
		}

		if(trigger_type==PublicConsts.TRIGGER_TYPE_APP_LAUNCHED||trigger_type==PublicConsts.TRIGGER_TYPE_APP_CLOSED){
			triggerObject=new AppLaunchDetectionReceiver(context,this);
			if(AppLaunchingDetectionService.queue==null||AppLaunchingDetectionService.queue.size()<=0) context.startService(new Intent(context,AppLaunchingDetectionService.class));
			((AppLaunchDetectionReceiver)triggerObject).registerReceiver();
		}

		//if(trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_ON||trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF){
		//	triggerObject=new NetworkReceiver(context,this);
		//	((NetworkReceiver)triggerObject).registerReceiver();
		//}

		if(trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON||trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF){
			triggerObject=new BluetoothReceiver(context,this);
			((BluetoothReceiver)triggerObject).registerReceiver();
		}

		if(trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL||trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE
				||trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF){
			triggerObject=new RingModeReceiver(context,this);
			((RingModeReceiver)triggerObject).registerReceiver();
		}

		if(trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON||trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF){
			triggerObject=new AirplaneModeReceiver(context,this);
			((AirplaneModeReceiver)triggerObject).registerReceiver();
		}

		if(trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED||trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED){
			triggerObject=new APReceiver(context,this);
			((APReceiver)triggerObject).registerReceiver();
		}

		if(trigger_type==PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_IN||trigger_type==PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT){
			triggerObject=new HeadsetPlugReceiver(context,this);
			((HeadsetPlugReceiver)triggerObject).registerReceiver();
		}



	}

	public void cancelTrigger(){
		if(triggerObject instanceof PendingIntent){
			if(TimeSwitchService.alarmManager!=null) {
				TimeSwitchService.alarmManager.cancel((PendingIntent) triggerObject);
			}

		}else if(triggerObject instanceof CustomTimerTask){
			((CustomTimerTask) triggerObject).cancelTimer();
		}else if(triggerObject instanceof BatteryReceiver){
			((BatteryReceiver) triggerObject).unregisterReceiver();
		}else if(triggerObject instanceof CustomBroadcastReceiver){
			((CustomBroadcastReceiver)triggerObject).unRegisterReceiver();
		}else if(triggerObject instanceof NetworkReceiver){
			((NetworkReceiver)triggerObject).unregisterReceiver();
		}else if(triggerObject instanceof BluetoothReceiver){
			((BluetoothReceiver)triggerObject).unRegisterReceiver();
		}else if(triggerObject instanceof RingModeReceiver){
			((RingModeReceiver)triggerObject).unRegisterReceiver();
		}else if(triggerObject instanceof APReceiver){
			((APReceiver)triggerObject).unRegisterReceiver();
		}else if(triggerObject instanceof AirplaneModeReceiver){
			((AirplaneModeReceiver)triggerObject).unRegisterReceiver();
		}else if(triggerObject instanceof AppLaunchDetectionReceiver){
			((AppLaunchDetectionReceiver)triggerObject).unregisterReceiver();
		}else if(triggerObject instanceof HeadsetPlugReceiver){
			((HeadsetPlugReceiver)triggerObject).unregisterReceiver();
		}

	}

	public void activateTriggerOfAlarmManager(PendingIntent pendingIntent){
		AlarmManager alarmManager=TimeSwitchService.alarmManager;
		if(alarmManager!=null){
			if (trigger_type == PublicConsts.TRIGGER_TYPE_SINGLE) {  //触发仅一次的模式
				if (Build.VERSION.SDK_INT >= 19) {
					if (isenabled&&time>System.currentTimeMillis()) {
						alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
						//Log.e(TAG,"Alarm of exact set and i="+i);
					}
					else alarmManager.cancel(pendingIntent);
				} else {
					if (isenabled&&time>System.currentTimeMillis())
						alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
					else alarmManager.cancel(pendingIntent);
				}
			}
			if (trigger_type == PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME) {  //按照指定时间重复
				long millis=interval_milliseconds;
				if(millis<=0) return;
				long triggerTime=time;
				while (triggerTime<System.currentTimeMillis()){
					triggerTime+=millis;
				}
				if (Build.VERSION.SDK_INT >= 19) {
					if (isenabled) {
						alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
						//Log.e(TAG,"Alarm of exact set and i="+i);
					}
					else alarmManager.cancel(pendingIntent);
				} else {
					if (isenabled) {
						alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
					}
					else alarmManager.cancel(pendingIntent);
				}
			}

			if(trigger_type ==PublicConsts.TRIGGER_TYPE_LOOP_WEEK){  //按照每周重复
				long triggerTime=time;
				while(triggerTime<System.currentTimeMillis()){
					triggerTime+=24*60*60*1000;
				}
				if (Build.VERSION.SDK_INT >= 19) {
					if (isenabled) {
						alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
						//Log.e(TAG,"Alarm of exact set and i="+i);
					}
					else alarmManager.cancel(pendingIntent);
				} else {
					if (isenabled&&time>System.currentTimeMillis())
						alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
					else alarmManager.cancel(pendingIntent);
				}
			}
		}
	}

	/**
	 * 只用于间隔指定时间使用
	 * @return 下次触发的绝对时间
	 */
	public long getNextTriggeringTime(){
		if(trigger_type==PublicConsts.TRIGGER_TYPE_SINGLE){
			if(time-System.currentTimeMillis()<0){
				return -1;
			}
			else return time;
		}else if(trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME){
			long triggerTime=time;
			while (triggerTime<System.currentTimeMillis()){
				triggerTime+=interval_milliseconds;
			}
			return triggerTime;
		}else if(trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_WEEK){
			//not used
		}

		return -1;
	}

	@Override
	public int compareTo(TaskItem o) {
		// TODO Auto-generated method stub
		return 0;
	}

}

