package com.github.ghmxr.timeswitch.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

public class HeadsetPlugReceiver extends BroadcastReceiver implements Runnable {
    Context context;
    TaskItem item;
    private boolean mLock=true;
    public static boolean isHeadsetPlugIn =false;

    public HeadsetPlugReceiver(Context context, @Nullable TaskItem item) {
        this.context=context;
        this.item=item;
    }

    public static boolean isHeadsetPluggedIn(){
        return isHeadsetPlugIn;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null||intent.getAction()==null) return;
        if(intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)){
            if(item==null){
                if(intent.getIntExtra("state",-1)==0) {
                    isHeadsetPlugIn =false;
                    Log.i("HeadSet","HeadSet is unplugged");
                }
                if(intent.getIntExtra("state",-1)==1) {
                    isHeadsetPlugIn =true;
                    Log.i("HeadSet","HeadSet is plugged");
                }
                return;
            }
            if(item.trigger_type== PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_IN){
                if(intent.getIntExtra("state",-1)==1){
                    Log.d("HEADSET","is PLUG IN");
                    activate();
                    return;
                }
                if(intent.getIntExtra("state",-1)==0){
                    mLock=false;
                    return;
                }
            }
            if(item.trigger_type==PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT){
                if(intent.getIntExtra("state",-1)==0){
                    Log.d("HEADSET","is PLUG OUT");
                    activate();
                    return;
                }
                if(intent.getIntExtra("state",-1)==1){
                    mLock=false;
                    //return;
                }
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

    public void unregisterReceiver(){
        try{
            context.unregisterReceiver(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void activate(){
        if(!mLock){
            mLock=true;
            new Thread(this).start();
        }

    }

    @Override
    public void run(){
        new ProcessTaskItem(context,item).activateTaskItem();
    }
}
