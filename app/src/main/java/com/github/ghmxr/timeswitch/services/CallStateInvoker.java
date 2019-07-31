package com.github.ghmxr.timeswitch.services;

import android.content.Context;
import android.support.annotation.NonNull;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.github.ghmxr.timeswitch.Global;

import java.util.LinkedList;

public class CallStateInvoker extends PhoneStateListener {

    private static CallStateInvoker invoker;
    private static TelephonyManager manager=null;
    private static final LinkedList<CallStateChangedCallback>callbacks=new LinkedList<>();

    public interface CallStateChangedCallback{
        void onCallStateChanged(int state,String phoneNumber);
    }

    /**
     * 激活电话状态监听器
     */
    public static void activate(final Context context){
        if(invoker!=null)return ;
        synchronized (CallStateInvoker.class){
            if(invoker==null){
                Global.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        invoker=new CallStateInvoker(context);
                    }
                });
            }
        }
    }

    private CallStateInvoker(@NonNull Context context){
        if(manager==null){
            manager=(TelephonyManager)context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        }
        if(manager==null)return;
        manager.listen(this,PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onCallStateChanged(int state, String phoneNumber) {
        super.onCallStateChanged(state, phoneNumber);
        Log.e("callcall",""+state+"  "+phoneNumber);//来电1，接听2，挂断0，电话号码为连续数字例如13011121113
        for(CallStateChangedCallback callback:callbacks){
            callback.onCallStateChanged(state,phoneNumber);
        }
    }

    public static synchronized void registerCallback(CallStateChangedCallback callback){
        if(!callbacks.contains(callback))callbacks.add(callback);
    }

    public static synchronized void unregisterCallback(CallStateChangedCallback callback){
        callbacks.remove(callback);
    }
}
