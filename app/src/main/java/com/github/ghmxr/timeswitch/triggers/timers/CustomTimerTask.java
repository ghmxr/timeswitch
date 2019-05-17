package com.github.ghmxr.timeswitch.triggers.timers;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.triggers.Trigger;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class CustomTimerTask implements Trigger{
    private static Timer timer=new Timer();
    private MyTimerTask timer_task;
    private Context context;
    static final String TAG="TimerTask(Executor)";
    private TaskItem item;
    private static PowerManager pm;
    private PowerManager.WakeLock wakeLock;

    public CustomTimerTask(Context context,TaskItem item) {
        this.context=context;
        this.item=item;
        if(pm==null){
            try{
                pm=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
            }catch (Exception e){e.printStackTrace();}
        }
    }

    @Override
    public synchronized void activate() {
        if(item.trigger_type == TriggerTypeConsts.TRIGGER_TYPE_SINGLE){   //仅触发一次
            long triggerTime=item.time;
            Date date=new Date();
            date.setTime(triggerTime);
            if(triggerTime>System.currentTimeMillis()) {
                try{
                    if(timer_task!=null) timer_task.cancel();
                    timer_task =new MyTimerTask();
                    timer.schedule(timer_task,date);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        if(item.trigger_type == TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME||item .trigger_type== TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK){
            setTimerOfRepeatingType(item.trigger_type);
        }

        //acquire wake lock
        try{
            try{if(wakeLock!=null) wakeLock.release();}catch (Exception e){}
            wakeLock=pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,String.valueOf(item.id));
            wakeLock.acquire();
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public synchronized void cancel() {
        timer_task.cancel();
        try{
            wakeLock.release();
            wakeLock=null;
        }catch (Exception e){e.printStackTrace();}
    }

    /**
     * @param type 1 or 2
     */
    private void setTimerOfRepeatingType(int type){
        if(type== TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME){   //按照指定间隔时间触发
            long triggerTime=item.time;
            long millis = item.interval_milliseconds;
            if(millis<=0) return;
            while (triggerTime<System.currentTimeMillis()){
                triggerTime+=millis;
            }
            Date date =new Date();
            date.setTime(triggerTime);
            if(item.isenabled) {
                try{
                    timer_task =new MyTimerTask();
                    timer.schedule(timer_task,date);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        if(type== TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK){  //按照周重复触发
            long triggerTime=item.time;
            while (triggerTime<System.currentTimeMillis()){
                triggerTime+=24*60*60*1000;
            }
            Date date  = new Date();
            date.setTime(triggerTime);
            if(item.isenabled) {
                try{
                    timer_task =new MyTimerTask();
                    timer.schedule(timer_task,date);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private class MyTimerTask extends TimerTask{
        @Override
        public void run(){
            Log.i(TAG,"Java Timer received and the task id is "+item.id);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        new ProcessTaskItem(context,item).checkExceptionsAndRunActions();
                    }catch (Exception e){e.printStackTrace();}
                }
            }).start();
            if(item.trigger_type == TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME||item .trigger_type== TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK){
                setTimerOfRepeatingType(item.trigger_type);
            }else{
                try{
                    wakeLock.release();
                    wakeLock=null;
                }catch (Exception e){e.printStackTrace();}
            }
        }
    }

}
