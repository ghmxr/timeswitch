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
	 * ��������ı���ֵ��
	 */
	public int order=0;

	/**
	 * ����id
	 *
	 */
	public int id=0;
	
	/**
	 * ÿ����ʱ�趨��ָ�������ơ�
	 *
	 */
	public String name="";

	/**
	 * ����ʱ�����Ƿ񼤻�
	 */
	public boolean isenabled=true;

	/**
	 * ʱ��--����ʱ�䡣����single �� weekloop
	 */
	public long time=0;

	/**
	 * ������������/�Ͽ�ָ��Wifi����Ϣ(networkid)
	 */
	public String wifiIds="";

	/**
	 * ��������
	 */
	public int trigger_type = TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK;

	/**
	 * ÿ���ظ��Ĳ���ֵ����
	 */
	public boolean [] week_repeat=new boolean[7];


	/**
	 * ָ���ظ��ļ��ʱ�䣬��λ����
	 */
	public long interval_milliseconds=1000*60*60;

	/**
	 * ��ذٷֱȲ���ֵ
	 */
	public int battery_percentage =50;

	/**
	 * ����¶Ȳ���ֵ����λΪ���϶�
	 */
	public int battery_temperature=35;

	/**
	 * �����������ȣ�����������
	 */
	public int light_brightness=0;

	public String selectedAction="android.intent.action.ANSWER";
	public String[] package_names=new String[0];

	/**
	 * ��������
	 */
	public String [] exceptions=new String[ExceptionConsts.EXCEPTION_LENGTH];

	/**
	 * ����Ķ���
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
	 * �����Ƿ���֪ͨ������֪ͨ��
	 */
	public boolean notify=false;

	/**
	 * ����ִ����ɺ��Ƿ��Զ�ɾ��
	 */
	public boolean autodelete=false;

	/**
	 * ����ִ�к��Ƿ��Զ��ر�
	 */
	public boolean autoclose=false;

	public String addition_exception_connector=String.valueOf(-1);

	public String addition_title_color="#16a085";

	public boolean addition_isFolded=false;

	public transient Trigger trigger;

	/**
	 * ��������ʾ���ݣ������������Ϊָ��ʱ���ظ�ʱ���ᱻTimeSwitchService��һ���߳�ÿ��500����ˢ��һ�ε���ʱֵ
	 */
	public transient String display_trigger="--";
	public transient int display_trigger_icon_res=-1;
	public transient String display_exception="";
	public transient String display_actions="";
	public transient String display_additions="";

	/**
     * �������췽������ʼ������
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
	 * ���ô˴�������
	 * @param context �����Ӧ��service��ά������״̬
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
	 * ���ڻ�ȡָ��ѭ���������������ʣ�ഥ��ʱ��
	 * @return ʣ���ʱ�䣬����
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

