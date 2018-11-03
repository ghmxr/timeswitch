package com.github.ghmxr.timeswitch.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.RingtoneManager;

import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

public class RingModeReceiver extends BroadcastReceiver implements Runnable {

    private Context context;
    private TaskItem item;
    private boolean isRegistered=false;
    private boolean mLock=true;

    public RingModeReceiver(Context context,TaskItem item) {
        this.context=context;
        this.item=item;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null||intent.getAction()==null) return;
        if(item==null) return;
        if(intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION)){
            switch (item.trigger_type){
                default:break;
                case PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF:{
                    if(intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE,-1)==AudioManager.RINGER_MODE_SILENT){
                        activate();
                    }else{
                        mLock=false;
                    }
                }
                break;
                case PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE:{
                    if(intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE,-1)==AudioManager.RINGER_MODE_VIBRATE){
                        activate();
                    }else {
                        mLock=false;
                    }
                }
                break;
                case PublicConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL:{
                    if(intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE,-1)==AudioManager.RINGER_MODE_NORMAL){
                        activate();
                    }else {
                        mLock=false;
                    }
                }
                break;
            }
        }
    }

    public void registerReceiver(){
        if(!isRegistered){
            context.registerReceiver(this,new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION));
            isRegistered=true;
        }
    }

    public void unRegisterReceiver(){
        if(isRegistered){
            context.unregisterReceiver(this);
            isRegistered=false;
        }
    }

    private void activate(){
        if(!mLock){
            new Thread(this).start();
            mLock=true;
        }

    }

    @Override
    public void run() {
        new ProcessTaskItem(context,item).activateTaskItem();
    }
}
