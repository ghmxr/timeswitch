package com.github.ghmxr.timeswitch.triggers.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.triggers.Trigger;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

public abstract class BaseBroadcastReceiver extends BroadcastReceiver implements Trigger {
    Context context;
    TaskItem item;
    public BaseBroadcastReceiver(@NonNull Context context, @Nullable TaskItem item){
        this.context=context;
        this.item=item;
    }

    @Override
    public abstract void onReceive(Context context, Intent intent);

    @Override
    public abstract void activate();

    @Override
    public void cancel() {
        try{
            context.unregisterReceiver(this);
        }catch (Exception e){e.printStackTrace();}
    }

    public void runProcessTask(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    //new ProcessTaskItem(context,item).checkExceptionsAndRunActions();
                    ProcessTaskItem.checkExceptionsAndRunActions(context,item);
                }catch (Exception e){e.printStackTrace();}
            }
        }).start();
    }
}
