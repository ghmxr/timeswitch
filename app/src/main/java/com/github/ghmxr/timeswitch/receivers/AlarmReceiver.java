package com.github.ghmxr.timeswitch.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class AlarmReceiver extends BroadcastReceiver implements Runnable{
    public static final String TAG_TASKITEM_ID="task_id";
    int id;
    //Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        id=intent.getIntExtra(TAG_TASKITEM_ID,-1);
        //this.context=context;
        new Thread(this).start();
    }

    @Override
    public void run() {
        Log.i("AlarmReceiver","AlarmReceiver received and the id is "+id);
        int position=ProcessTaskItem.getPosition(id);
        if(position>=0) {
            final TaskItem item=TimeSwitchService.list.get(position);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(TimeSwitchService.service_queue!=null&&TimeSwitchService.service_queue.size()>0) new ProcessTaskItem(TimeSwitchService.service_queue.getLast(),item).activateTaskItem();
                }
            }).start();
            if((item.trigger_type== PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME||item.trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_WEEK)&&(item.triggerObject instanceof PendingIntent)){
                item.activateTriggerOfAlarmManager((PendingIntent)item.triggerObject);
                Log.i("AlarmReceiver","continue the alarm and the id is "+item.id);
            }
        }
    }

}
