package com.github.ghmxr.timeswitch.triggers.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;

public class HeadsetPlugReceiver extends BaseBroadcastReceiver{
    private boolean mLock=true;

    public HeadsetPlugReceiver(Context context, TaskItem item) {
        super(context,item);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null||intent.getAction()==null) return;
        if(intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)){
            if(item==null) return;
            if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_IN){
                if(intent.getIntExtra("state",-1)==1){
                    Log.d("HEADSET","is PLUG IN");
                    runActions();
                    return;
                }
                if(intent.getIntExtra("state",-1)==0){
                    mLock=false;
                    return;
                }
            }
            if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT){
                if(intent.getIntExtra("state",-1)==0){
                    Log.d("HEADSET","is PLUG OUT");
                    runActions();
                    return;
                }
                if(intent.getIntExtra("state",-1)==1){
                    mLock=false;
                    //return;
                }
            }
        }
    }

    @Override
    public void activate() {
        try{
            context.registerReceiver(this,new IntentFilter(Intent.ACTION_HEADSET_PLUG));
            //context.registerReceiver(this,new IntentFilter(AudioManager.ACTION_HEADSET_PLUG));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void cancel() {
        super.cancel();
    }

    private void runActions(){
        if(!mLock){
            mLock=true;
            runProcessTask();
        }

    }
}
