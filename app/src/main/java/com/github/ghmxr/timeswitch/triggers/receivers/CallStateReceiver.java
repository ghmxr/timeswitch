package com.github.ghmxr.timeswitch.triggers.receivers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;

import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.triggers.Trigger;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

public class CallStateReceiver implements Trigger , TimeSwitchService.CallStateInvoker.CallStateChangedCallback {

    private Context context;
    private TaskItem item;
    private boolean mLock=false;


    public CallStateReceiver(@NonNull Context context, @NonNull TaskItem item) {
        this.context=context;
        this.item=item;
    }


    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        //Log.e("触发器收到",""+state+" "+incomingNumber);
        switch (state){
            default:break;
            case TelephonyManager.CALL_STATE_RINGING:{//响铃
                if(item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_CALL_STATE_FINISHED||item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_CALL_STATE_CONNECTED){
                    mLock=false;
                    return;
                }
                if(checkIfRunThisTaskItem(item.call_state_numbers,incomingNumber))runProcessTaskItem();
            }
            break;
            case TelephonyManager.CALL_STATE_OFFHOOK:{//接通
                if(item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_CALL_STATE_FINISHED||item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_CALL_STATE_INCOMING){
                    mLock=false;
                    return;
                }
                if(checkIfRunThisTaskItem(item.call_state_numbers,incomingNumber))runProcessTaskItem();
            }
            break;
            case TelephonyManager.CALL_STATE_IDLE:{//挂断
                if(item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_CALL_STATE_INCOMING||item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_CALL_STATE_CONNECTED){
                    mLock=false;
                    return;
                }
                if(checkIfRunThisTaskItem(item.call_state_numbers,incomingNumber))runProcessTaskItem();
            }
            break;
        }
    }

    private void runProcessTaskItem(){
        if(!mLock){
            mLock=true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        ProcessTaskItem.checkExceptionsAndRunActions(context,item);
                    }catch (Exception e){e.printStackTrace();}
                }
            }).start();
        }
    }

    private static boolean checkIfRunThisTaskItem(@Nullable String[] array, @Nullable String inComingNumber){
        if(array==null||array.length==0)return true;
        if(inComingNumber==null) return false;
        return compare(array,inComingNumber);
    }

    private static boolean compare(@NonNull String [] number_array,@NonNull String inComingNumber){
        for(String s:number_array){
            if(getFormatPhoneNumberString(s).equals(getFormatPhoneNumberString(inComingNumber))){
                return true;
            }
        }
        return false;
    }

    private static String getFormatPhoneNumberString(@Nullable String number_info){
        if(number_info==null)return "";
        return number_info.trim().replace("+","")
                .replace("-","")
                .replace(" ","")
                .toLowerCase();
    }

    @Override
    public void activate() {
        try{
            TimeSwitchService.CallStateInvoker.registerCallback(this);
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public void cancel() {
        try{
            TimeSwitchService.CallStateInvoker.unregisterCallback(this);
        }catch (Exception e){e.printStackTrace();}
    }

}
