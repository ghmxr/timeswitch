package com.github.ghmxr.timeswitch.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;

import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

public class HeadsetPlugReceiver extends BroadcastReceiver implements Runnable {
    Context context;
    TaskItem item;

    public HeadsetPlugReceiver(Context context, TaskItem item) {
        this.context=context;
        this.item=item;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null||intent.getAction()==null) return;
        if(intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)){
            if(item==null) return;
            if(item.trigger_type== PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_IN&&intent.getIntExtra("state",-1)==1){
                Log.d("HEADSET","is PLUG IN");
                activate();
            }
            if(item.trigger_type==PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT&&intent.getIntExtra("state",-1)==0){
                Log.d("HEADSET","is PLUG OUT");
                activate();
            }
        }
    }

    public void registerReceiver(){
        try{
            context.registerReceiver(this,new IntentFilter(Intent.ACTION_HEADSET_PLUG));
            //context.registerReceiver(this,new IntentFilter(AudioManager.ACTION_HEADSET_PLUG));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void activate(){
        new Thread(this).start();
    }

    @Override
    public void run(){
        new ProcessTaskItem(context,item).activateTaskItem();
    }
}
