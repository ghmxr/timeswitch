package com.github.ghmxr.timeswitch.triggers.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.TriggerTypeConsts;

public class RingModeReceiver extends BaseBroadcastReceiver{
    private boolean mLock=true;

    public RingModeReceiver(Context context,TaskItem item) {
        super(context,item);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null||intent.getAction()==null) return;
        if(item==null) return;
        if(intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION)){
            switch (item.trigger_type){
                default:break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF:{
                    if(intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE,-1)==AudioManager.RINGER_MODE_SILENT){
                        runActions();
                    }else{
                        mLock=false;
                    }
                }
                break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE:{
                    if(intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE,-1)==AudioManager.RINGER_MODE_VIBRATE){
                        runActions();
                    }else {
                        mLock=false;
                    }
                }
                break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL:{
                    if(intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE,-1)==AudioManager.RINGER_MODE_NORMAL){
                        runActions();
                    }else {
                        mLock=false;
                    }
                }
                break;
            }
        }
    }

    @Override
    public void activate() {
        try{
            context.registerReceiver(this,new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void runActions(){
        if(!mLock){
            mLock=true;
            runProcessTask();
        }

    }
}
