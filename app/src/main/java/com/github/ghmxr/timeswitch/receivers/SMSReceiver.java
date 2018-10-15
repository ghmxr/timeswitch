package com.github.ghmxr.timeswitch.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.activities.SmsActivity;
import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.utils.LogUtil;

import android.app.Activity;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class SMSReceiver extends BroadcastReceiver {
    static String TAG="SMSReceiver";
    public static final String EXTRA_SMS_TASK_NAME="task_name";
    public static final String EXTRA_SMS_SUB_INFO="subscriptionId";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null) return;
        if(intent.getAction()==null) return;
        if(intent.getAction().equals(PublicConsts.ACTION_SMS_SENT)){
            String sent_log_name=intent.getStringExtra(EXTRA_SMS_TASK_NAME)+":";
           switch (getResultCode()){
               default:break;
               case Activity.RESULT_OK:{
                   try{
                       String sent_log=sent_log_name;
                       if(Build.VERSION.SDK_INT>=22){
                           SubscriptionInfo subscriptionInfo= SubscriptionManager.from(context)
                                   .getActiveSubscriptionInfo(intent.getIntExtra(EXTRA_SMS_SUB_INFO,-1));
                           if(subscriptionInfo!=null) sent_log+="(Sim:"+subscriptionInfo.getDisplayName()+" ) ";
                       }
                       sent_log+=context.getResources().getString(R.string.log_sms_sent)+intent.getStringExtra(SmsActivity.EXTRA_SMS_ADDRESS)+"\n"
                               +context.getResources().getString(R.string.log_sms_sent_message)+intent.getStringExtra(SmsActivity.EXTRA_SMS_MESSAGE);
                       LogUtil.putLog(context,sent_log);
                       //Toast.makeText(context,sent_log,Toast.LENGTH_SHORT).show();
                   }catch (Exception e){
                       e.printStackTrace();
                   }
               }
               break;
               case SmsManager.RESULT_ERROR_GENERIC_FAILURE:{
                   Log.e(TAG,"RESULT_ERROR_GENERIC_FAILURE");
                    LogUtil.putLog(context,sent_log_name+context.getResources().getString(R.string.log_sms_sent_error)+"RESULT_ERROR_GENERIC_FAILURE");
               }
               break;
               case SmsManager.RESULT_ERROR_LIMIT_EXCEEDED:{
                   Log.e(TAG,"RESULT_ERROR_LIMIT_EXCEEDED");
                   LogUtil.putLog(context,sent_log_name+context.getResources().getString(R.string.log_sms_sent_error)+"RESULT_ERROR_LIMIT_EXCEEDED");
               }
               break;
               case SmsManager.RESULT_ERROR_NO_SERVICE:{
                   Log.e(TAG,"RESULT_ERROR_NO_SERVICE");
                   LogUtil.putLog(context,sent_log_name+context.getResources().getString(R.string.log_sms_sent_error)+"RESULT_ERROR_NO_SERVICE");
               }
               break;
               case SmsManager.RESULT_ERROR_RADIO_OFF:{
                   Log.e(TAG,"RESULT_ERROR_RADIO_OFF");
                   LogUtil.putLog(context,sent_log_name+context.getResources().getString(R.string.log_sms_sent_error)+"RESULT_ERROR_RADIO_OFF");
               }
               break;
               case SmsManager.RESULT_ERROR_NULL_PDU:{
                   Log.e(TAG,"RESULT_ERROR_NULL_PDU");
                   LogUtil.putLog(context,sent_log_name+context.getResources().getString(R.string.log_sms_sent_error)+"RESULT_ERROR_NULL_PDU");
               }
               break;
               case SmsManager.RESULT_ERROR_SHORT_CODE_NEVER_ALLOWED:{
                   Log.e(TAG,"RESULT_ERROR_SHORT_CODE_NEVER_ALLOWED");
                   LogUtil.putLog(context,sent_log_name+context.getResources().getString(R.string.log_sms_sent_error)+"RESULT_ERROR_SHORT_CODE_NEVER_ALLOWED");
               }
               break;
               case SmsManager.RESULT_ERROR_SHORT_CODE_NOT_ALLOWED:{
                   Log.e(TAG,"RESULT_ERROR_SHORT_CODE_NOT_ALLOWED");
                   LogUtil.putLog(context,sent_log_name+context.getResources().getString(R.string.log_sms_sent_error)+"RESULT_ERROR_SHORT_CODE_NOT_ALLOWED");
               }
               break;
           }


        }
    }

    public static class SMSReceiptReceiver extends BroadcastReceiver{
        public static final String EXTRA_IF_SHOW_TOAST="if_show_receipt_toast";
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent==null) return;
            if(intent.getAction()==null) return;
            if(intent.getAction().equals(PublicConsts.ACTION_SMS_DELIVERED)){
                try{
                    String delivered_log=intent.getStringExtra(EXTRA_SMS_TASK_NAME)+":"
                            +intent.getStringExtra(SmsActivity.EXTRA_SMS_ADDRESS)+context.getResources().getString(R.string.log_sms_delivered);
                    LogUtil.putLog(context,delivered_log);
                    if(intent.getBooleanExtra(EXTRA_IF_SHOW_TOAST,false)) Toast.makeText(context,delivered_log,Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
