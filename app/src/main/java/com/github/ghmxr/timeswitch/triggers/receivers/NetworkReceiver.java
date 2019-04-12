package com.github.ghmxr.timeswitch.triggers.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ghmxr.timeswitch.Global;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.utils.LogUtil;

public class NetworkReceiver extends BaseBroadcastReceiver{
    boolean mLock=true;


    public NetworkReceiver(@NonNull Context context, @Nullable TaskItem item) {
        super(context,item);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null||intent.getAction()==null||item==null) return;

        int type=item.trigger_type;

        if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            NetworkInfo info=intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            WifiInfo wifiInfo=null;
            WifiManager wifiManager=(WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if(wifiManager!=null) wifiInfo=wifiManager.getConnectionInfo();

            if(type== TriggerTypeConsts.TRIGGER_TYPE_WIFI_CONNECTED){

                if(info.getDetailedState().equals(NetworkInfo.DetailedState.CONNECTED)
                        &&info.getType()==ConnectivityManager.TYPE_WIFI
                        &&info.isConnected()){

                    if(item.wifiIds==null||item.wifiIds.trim().equals("")){
                        runActions();
                        return;
                    }else{
                        if(wifiInfo==null) return;
                        try{
                            String [] ids=item.wifiIds.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                            for(String s:ids){
                                if(Integer.parseInt(s)==wifiInfo.getNetworkId()){
                                    runActions();
                                    return;
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                            LogUtil.putExceptionLog(context,e);
                        }
                    }
                }

                if(info.getDetailedState().equals(NetworkInfo.DetailedState.DISCONNECTED)
                        &&info.getType()==ConnectivityManager.TYPE_WIFI
                        &&!info.isConnected()){
                    mLock=false;
                    return;
                }

            }

            if(type== TriggerTypeConsts.TRIGGER_TYPE_WIFI_DISCONNECTED){

                if(info.getDetailedState().equals(NetworkInfo.DetailedState.DISCONNECTED)
                        &&info.getType()==ConnectivityManager.TYPE_WIFI
                        &&!info.isConnected()){
                    if(item.wifiIds==null||item.wifiIds.trim().equals("")){
                        runActions();
                        return;
                    }else{

                        if(Global.NetworkReceiver.connectedWifiInfo==null) {

                            return;
                        }
                        try{
                            String ids[]=item.wifiIds.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);

                            for(String s:ids){
                                if(Integer.parseInt(s)== Global.NetworkReceiver.connectedWifiInfo.getNetworkId()){
                                    runActions();
                                    return;
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                            LogUtil.putExceptionLog(context,e);
                        }
                    }
                }

                if(info.getDetailedState().equals(NetworkInfo.DetailedState.CONNECTED)
                        &&info.getType()==ConnectivityManager.TYPE_WIFI
                        &&info.isConnected()){
                    mLock=false;
                    return;
                }

            }
        }

        if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
            ConnectivityManager manager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager==null) return;
            NetworkInfo info=manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if(info==null) return;
            if(type== TriggerTypeConsts.TRIGGER_TYPE_NET_ON){
                if(info.isConnected()){
                    runActions();
                    return;
                }
                if(!info.isConnected()){
                    mLock=false;
                    return;
                }
            }

            if(type== TriggerTypeConsts.TRIGGER_TYPE_NET_OFF){
                if(!info.isConnected()){
                    runActions();
                    return;
                }
                if(info.isConnected()){
                    mLock=false;
                    return;
                }
            }
        }

        if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
            //Log.d("wifi state ",""+intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1));
            if(type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_ON){
                if(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1)==(WifiManager.WIFI_STATE_ENABLED)){
                    runActions();
                    return;
                }
                if(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1)==WifiManager.WIFI_STATE_DISABLED){
                    mLock=false;
                    return;
                }
            }

            if(type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF){
                if(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1)==WifiManager.WIFI_STATE_DISABLED){
                    runActions();
                    return;

                }
                if(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1)==WifiManager.WIFI_STATE_ENABLED){
                    mLock=false;
                    return;
                }
            }
        }

    }

    @Override
    public void activate() {
        try{
            IntentFilter filter=new IntentFilter();
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(this,filter);
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
