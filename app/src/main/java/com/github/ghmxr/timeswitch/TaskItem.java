package com.github.ghmxr.timeswitch;

import android.app.Service;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.ghmxr.timeswitch.data.v2.ActionConsts;
import com.github.ghmxr.timeswitch.data.v2.ExceptionConsts;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.triggers.Trigger;
import com.github.ghmxr.timeswitch.triggers.TriggerUtil;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class TaskItem implements Comparable<TaskItem>,Serializable{
	
	/**
	 * 控制排序的变量值。
	 */
	public int order=0;

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
	public int trigger_type = TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK;

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

	/**
	 * 环境光线亮度，触发器参数
	 */
	public int light_brightness=0;

	public String selectedAction="android.intent.action.ANSWER";
	public String[] package_names=new String[0];

	/**
	 * 触发例外
	 */
	public String [] exceptions=new String[ExceptionConsts.EXCEPTION_LENGTH];

	/**
	 * 任务的动作
	 */
	public String [] actions=new String[ActionConsts.ACTION_LENGTH];

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
	public boolean notify=false;

	/**
	 * 任务执行完成后是否自动删除
	 */
	public boolean autodelete=false;

	/**
	 * 任务执行后是否自动关闭
	 */
	public boolean autoclose=false;

	public String addition_exception_connector=String.valueOf(-1);

	public String addition_title_color="#16a085";

	public boolean addition_isFolded=false;

	public transient Trigger trigger;

	/**
	 * 触发器显示内容，如果触发类型为指定时间重复时，会被TimeSwitchService的一个线程每隔500毫秒刷新一次倒计时值
	 */
	public transient String display_trigger="--";
	public transient int display_trigger_icon_res=-1;
	public transient String display_exception="";
	public transient String display_actions="";
	public transient String display_additions="";

	/**
     * 基本构造方法，初始化变量
     */
	public TaskItem(){
        for(int i=0;i<week_repeat.length;i++){
           week_repeat[i]=true;
        }

        for(int i=0;i<35;i++){
        	exceptions[i]=String.valueOf(0);
		}

        exceptions[ExceptionConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]=String.valueOf(-1);
        exceptions[ExceptionConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]=String.valueOf(-1);
        exceptions[ExceptionConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]=String.valueOf(-1);
        exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]=String.valueOf(-1);
        exceptions[ExceptionConsts.EXCEPTION_START_TIME]=String.valueOf(-1);
        exceptions[ExceptionConsts.EXCEPTION_END_TIME]=String.valueOf(-1);

        for(int i=35;i<exceptions.length;i++){
        	exceptions[i]=String.valueOf(-1);
		}

        for(int i=0;i<actions.length;i++){
            actions[i]=String.valueOf(-1);
        }

        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_VOLUME_LOCALE]=String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(-1);
        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_SELECTION_LOCALE]=String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(-1);
    	//actions[PublicConsts.ACTION_SET_WALL_PAPER_LOCALE]=String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL+String.valueOf(" ");
    	actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_VIBRATE_LOCALE]=String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(-1);
		actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NOTIFICATION_LOCALE]=String.valueOf(ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_UNSELECTED)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_CONTENT_DEFAULT);
		actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_TOAST_LOCALE]=String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(0)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(0);
		actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SMS_LOCALE]=String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(-1)+PublicConsts.SEPARATOR_SECOND_LEVEL +String.valueOf(0);
	}

	@Override
	public String toString() {
		return "TaskItem{" +
				"order=" + order +
				", id=" + id +
				", name='" + name + '\'' +
				", isenabled=" + isenabled +
				", time=" + time +
				", wifiIds='" + wifiIds + '\'' +
				", trigger_type=" + trigger_type +
				", week_repeat=" + Arrays.toString(week_repeat) +
				", interval_milliseconds=" + interval_milliseconds +
				", battery_percentage=" + battery_percentage +
				", battery_temperature=" + battery_temperature +
				", selectedAction='" + selectedAction + '\'' +
				", package_names=" + Arrays.toString(package_names) +
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
				", addition_exception_connector='" + addition_exception_connector + '\'' +
				", addition_title_color='" + addition_title_color + '\'' +
				", addition_isFolded=" + addition_isFolded +
				'}';
	}

	/**
	 * 启用此触发任务
	 * @param context 传入对应的service来维持运行状态
	 */
	public void activateTask(Context context){
		if(trigger!=null) trigger.cancel();
		if(!(context instanceof Service)){
			Log.e("TaskItemWarn","The context sent to this TaskItem is not a service instance and some functions may not work!!!!");
		}
		trigger= TriggerUtil.getTriggerInstanceForTaskItem(context,this);
		if(trigger!=null) trigger.activate();
	}

	public void cancelTask(){
		if(trigger!=null) {
			trigger.cancel();
			trigger=null;
		}
	}

	/**
	 * 用于获取指定循环触发类型任务的剩余触发时间
	 * @return 剩余的时间，毫秒
	 */
	public long getRemainingTimeOfTypeLoopByCertainTime(){
		if(trigger_type== TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME){
			long triggerTime=time;
			long current=System.currentTimeMillis();
			while (triggerTime<current){
				triggerTime+=interval_milliseconds;
			}
			return triggerTime-current;
		}
		return -1;
	}

	@Override
	public int compareTo(@NonNull TaskItem o) {
		// TODO Auto-generated method stub
		if(this.order<o.order) return -1;
		if(this.order>o.order) return 1;
		return 0;
	}

}

