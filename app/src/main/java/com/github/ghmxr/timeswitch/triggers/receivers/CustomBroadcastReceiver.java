package com.github.ghmxr.timeswitch.triggers.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.triggers.Trigger;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class CustomBroadcastReceiver extends BaseBroadcastReceiver{
    String action;
    public CustomBroadcastReceiver(Context context, String action, TaskItem item) {
        super(context,item);
        this.action=action;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if(intent==null) return;
        if(intent.getAction()==null) return;
        if(intent.getAction().equals(action)){
            runProcessTask();
        }
    }

    @Override
    public void activate() {
        try{
            context.registerReceiver(this,new IntentFilter(action));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * @deprecated
     */
    public void registerReceiver(){
       // if(!isRegistered){
           // if(item.isenabled) {

               // this.isRegistered=true;
           // }
       // }
    }

    /**
     * @deprecated
     */
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
