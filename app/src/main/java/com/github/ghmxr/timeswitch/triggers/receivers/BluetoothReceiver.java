package com.github.ghmxr.timeswitch.triggers.receivers;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;

public class BluetoothReceiver extends BaseBroadcastReceiver{

    public BluetoothReceiver(Context context, TaskItem item) {
       super(context,item);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null||intent.getAction()==null) return;
        if(intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
            if(item==null) return;
            if(item.trigger_type== PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON&&intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,-1)==BluetoothAdapter.STATE_ON){
                runProcessTask();
            }

            if(item.trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF&&intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,-1)==BluetoothAdapter.STATE_OFF){
                runProcessTask();
            }
        }
    }

    @Override
    public void activate() {
        try{
            context.registerReceiver(this,new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * @deprecated
     */
    public void registerReceiver(){
        //if(!isRegistered){

        //    isRegistered=true;
       // }
    }

    /**
     * @deprecated
     */
    public void unRegisterReceiver(){
        try{
            context.unregisterReceiver(this);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
