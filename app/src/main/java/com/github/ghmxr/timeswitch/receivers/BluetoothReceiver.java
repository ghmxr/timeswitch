package com.github.ghmxr.timeswitch.receivers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

public class BluetoothReceiver extends BroadcastReceiver implements Runnable {
    private Context context;
    private  TaskItem item;
    private boolean isRegistered=false;

    public BluetoothReceiver(Context context, TaskItem item) {
        this.context=context;
        this.item=item;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null||intent.getAction()==null) return;
        if(intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
            if(item==null) return;
            if(item.trigger_type== PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON&&intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,-1)==BluetoothAdapter.STATE_ON){
                activate();
            }

            if(item.trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF&&intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,-1)==BluetoothAdapter.STATE_OFF){
                activate();
            }
        }
    }

    public void registerReceiver(){
        if(!isRegistered){
            context.registerReceiver(this,new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
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
        new Thread(this).start();
    }

    @Override
    public void run() {
        new ProcessTaskItem(context,item).activateTaskItem();
    }
}
