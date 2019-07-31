package com.github.ghmxr.timeswitch.triggers.receivers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.services.CallStateInvoker;
import com.github.ghmxr.timeswitch.triggers.Trigger;

public class CallStateReceiver implements Trigger , CallStateInvoker.CallStateChangedCallback {

    private Context context;
    private TaskItem item;


    public CallStateReceiver(@NonNull Context context, @NonNull TaskItem item) {
        this.context=context;
        this.item=item;
    }


    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        Log.e("触发器收到",""+state+" "+incomingNumber);
        switch (state){
            default:break;
            case TelephonyManager.CALL_STATE_RINGING:{//响铃

            }
            break;
            case TelephonyManager.CALL_STATE_OFFHOOK:{//接通

            }
            break;
            case TelephonyManager.CALL_STATE_IDLE:{//挂断

            }
            break;
        }
    }

    @Override
    public void activate() {
        try{
            CallStateInvoker.registerCallback(this);
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public void cancel() {
        try{
            CallStateInvoker.unregisterCallback(this);
        }catch (Exception e){e.printStackTrace();}
    }

}
