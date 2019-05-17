package com.github.ghmxr.timeswitch.triggers.timers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.triggers.Trigger;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

public class CustomAlarmReceiver implements Trigger {
    private static AlarmManager alarmManager;
    private Context context;
    private TaskItem item;
    private static final SparseArray<CustomAlarmReceiver> map_alarm_receivers =new SparseArray<>();
    private final PendingIntent pi;
    private static final String ACTION ="com.github.ghmxr.timeswitch.alarm";
    private static final String EXTRA_TASK_ID="task_id";

    public CustomAlarmReceiver(Context context, @NonNull TaskItem item){
       this.item=item;
       this.context=context;
       Intent i=new Intent(context,AlarmReceiver.class);
       i.setAction(ACTION);
       i.putExtra(EXTRA_TASK_ID,item.id);
       pi=PendingIntent.getBroadcast(context,item.id,i,PendingIntent.FLAG_UPDATE_CURRENT);
       map_alarm_receivers.put(item.id,this);
       if(alarmManager==null){
           synchronized (CustomAlarmReceiver.class){
               if(alarmManager==null){
                   try{
                       alarmManager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                   }catch (Exception e){e.printStackTrace();}
               }
           }
       }
    }


    @Override
    public void activate() {
        if(alarmManager==null) return;
        if (item.trigger_type == TriggerTypeConsts.TRIGGER_TYPE_SINGLE) {  //触发仅一次的模式
            if (Build.VERSION.SDK_INT >= 19) {
                if (item.time>System.currentTimeMillis()) alarmManager.setExact(AlarmManager.RTC_WAKEUP, item.time, pi);
            } else {
                if (item.time>System.currentTimeMillis()) alarmManager.set(AlarmManager.RTC_WAKEUP, item.time, pi);
            }
        }
        if (item.trigger_type == TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME) {  //按照指定时间重复
            long millis=item.interval_milliseconds;
            if(millis<=0) return;
            long triggerTime=item.time;
            while (triggerTime<System.currentTimeMillis()){
                triggerTime+=millis;
            }
            if (Build.VERSION.SDK_INT >= 19) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pi);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pi);
            }
        }

        if(item.trigger_type == TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK){  //按照每周重复
            long triggerTime=item.time;
            while(triggerTime<System.currentTimeMillis()){
                triggerTime+=24*60*60*1000;
            }
            if (Build.VERSION.SDK_INT >= 19) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pi);
                //Log.e(TAG,"Alarm of exact set and i="+i);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pi);
            }
        }
    }

    @Override
    public void cancel() {
        try{
            alarmManager.cancel(pi);
            map_alarm_receivers.remove(item.id);
        }catch (Exception e){e.printStackTrace();}
    }

    public static class AlarmReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context,Intent intent) {
            if(intent==null||intent.getAction()==null||!intent.getAction().equals(ACTION)) return;
            try{
                final CustomAlarmReceiver receiver= map_alarm_receivers.get(intent.getIntExtra(EXTRA_TASK_ID,-1));
                if((receiver.item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME||receiver.item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK)){
                    receiver.activate();
                    Log.i("AlarmReceiver","continue the alarm and the id is "+receiver.item.id);
                }else {
                    map_alarm_receivers.remove(receiver.item.id);
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            new ProcessTaskItem(receiver.context,receiver.item).checkExceptionsAndRunActions();
                        }catch (Exception e){e.printStackTrace();}
                    }
                }).start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
