package com.github.ghmxr.timeswitch.timers;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import android.util.SparseBooleanArray;

import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class CustomTimerTask{

    private Timer timer;
    private MyTimerTask timertask;
    Context context;
    static final String TAG="TimerTask(Executor)";
    TaskItem item;
    public static SparseBooleanArray timerTaskStatus;

    private PowerManager.WakeLock pm_lock=TimeSwitchService.wakelock;

    public CustomTimerTask(Context context,TaskItem item) {
        super();
        this.context=context;
        this.item=item;
    }

    public CustomTimerTask activateTimerTask(){
        clearTimer();
        if(item.trigger_type == PublicConsts.TRIGGER_TYPE_SINGLE){   //仅触发一次
            long triggerTime=item.time;
            Date date=new Date();
            date.setTime(triggerTime);
            if(item.isenabled){
                if(triggerTime>System.currentTimeMillis()) {
                    try{
                        timer=new Timer();
                        timertask=new MyTimerTask();
                        timer.schedule(timertask,date);
                        acquireWakeLock();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }

        if(item.trigger_type ==PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME||item .trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_WEEK){
            setTimerOfRepeatingType(item.trigger_type);
        }

        return this;
    }

    /**
     * 取消本Timer
     */
    public void cancelTimer(){
        //Log.i(TAG,"cancel timer called");
        clearTimer();
        if(timerTaskStatus!=null) timerTaskStatus.put(item.id,false);
        refreshIfNeedWakeLock();
    }

    /**
     * @param type 1 or 2
     */
    private void setTimerOfRepeatingType(int type){
        clearTimer();
        if(type==PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME){   //按照指定间隔时间触发
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
                    timer=new Timer();
                    timertask=new MyTimerTask();
                    timer.schedule(timertask,date);
                    acquireWakeLock();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        if(type==PublicConsts.TRIGGER_TYPE_LOOP_WEEK){  //按照周重复触发
            long triggerTime=item.time;
            while (triggerTime<System.currentTimeMillis()){
                triggerTime+=24*60*60*1000;
            }
            Date date  = new Date();
            date.setTime(triggerTime);
            if(item.isenabled) {
                try{
                    timer=new Timer();
                    timertask=new MyTimerTask();
                    timer.schedule(timertask,date);
                    acquireWakeLock();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void clearTimer(){
        try{
            if(timer!=null){
                timer.cancel();
            }
            if(timertask!=null){
                timertask.cancel();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            timer=null;
            timertask=null;
        }
    }

    private void refreshIfNeedWakeLock(){
       // Log.i(TAG,"refreshIfNeedWakeLock called and timerTaskStatus isnull?"+Boolean.valueOf(timerTaskStatus==null));
        if(timerTaskStatus==null){
            if(pm_lock!=null){
                pm_lock.release();
                pm_lock=null;
               // Log.i(TAG,"java timer status array is all false and wakelock is released");
            }
        }else{
           // Log.i(TAG,"timerTaskStatus index true is "+timerTaskStatus.indexOfValue(true));
            if(timerTaskStatus.indexOfValue(true)<0&&pm_lock!=null){
                pm_lock.release();
                pm_lock=null;
               // Log.i(TAG,"java timer status array is all false and wakelock is released");
            }
        }
    }

    private void acquireWakeLock(){
        if(pm_lock==null) {
            pm_lock=getWakeLockInstance(PowerManager.PARTIAL_WAKE_LOCK,"Java Timer wake lock "+item.id);
            if(pm_lock!=null) pm_lock.acquire();
        }
        if(timerTaskStatus==null) timerTaskStatus=new SparseBooleanArray();
        timerTaskStatus.put(item.id,true);
    }

    private PowerManager.WakeLock getWakeLockInstance(int levelAndFlags,String tag){
        PowerManager pm=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
        if(pm==null) return null;
        return pm.newWakeLock(levelAndFlags,tag);
    }

    private class MyTimerTask extends TimerTask{
        @Override
        public void run(){
            Log.i(TAG,"Java Timer received and the task id is "+item.id);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    new ProcessTaskItem(context,item).activateTaskItem();
                }
            }).start();
            if(item.trigger_type ==PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME||item .trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_WEEK){
                setTimerOfRepeatingType(item.trigger_type);
            }else{
                if(timerTaskStatus!=null) timerTaskStatus.put(item.id,false);
                refreshIfNeedWakeLock();
            }

        }
    }

}
