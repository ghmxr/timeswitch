package com.github.ghmxr.timeswitch.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class SMSReceiver extends BroadcastReceiver {
    static String TAG="SMSReceiver";
    public static final String EXTRA_IF_SHOW_RECEIPT_TOAST="receipt_toast";
    public static final String EXTRA_SENT_ADDRESS ="address";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null||intent.getAction()==null||!intent.getAction().equals(PublicConsts.ACTION_SMS_DELIVERED)) return;
        try{
            if(intent.getBooleanExtra(EXTRA_IF_SHOW_RECEIPT_TOAST,false)){
                Toast.makeText(context,intent.getStringExtra(EXTRA_SENT_ADDRESS)+context.getResources().getString(R.string.log_sms_delivered),Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){e.printStackTrace();}
    }
}
