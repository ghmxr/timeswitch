package com.github.ghmxr.timeswitch.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class CustomBroadcastReceiver extends BroadcastReceiver {
    String action;
    private Context context;
    private TaskItem item;
   // private boolean isRegistered=false;
    public CustomBroadcastReceiver(Context context, String action, TaskItem item) {
        super();
        this.context=context;
        this.action=action;
        this.item=item;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if(intent==null) return;
        if(intent.getAction()==null) return;
        if(intent.getAction().equals(action)){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //new ProcessTaskItem(context,item).activateTaskItem();
                    if(TimeSwitchService.service_queue!=null&&TimeSwitchService.service_queue.size()>0) new ProcessTaskItem(TimeSwitchService.service_queue.getLast(),item).activateTaskItem();
                }
            }).start();
        }
    }

    public void registerReceiver(){
       // if(!isRegistered){
           // if(item.isenabled) {
                try{
                    context.registerReceiver(this,new IntentFilter(action));
                }catch (Exception e){
                    e.printStackTrace();
                }
               // this.isRegistered=true;
           // }
       // }
    }

    public void unRegisterReceiver(){
       // if(isRegistered){
        try{
            context.unregisterReceiver(this);
        }catch (Exception e){
            e.printStackTrace();
        }
        //    this.isRegistered=false;
       // }
    }
}
