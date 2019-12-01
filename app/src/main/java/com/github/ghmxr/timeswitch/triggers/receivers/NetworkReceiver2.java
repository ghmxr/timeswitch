package com.github.ghmxr.timeswitch.triggers.receivers;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.github.ghmxr.timeswitch.Global;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.triggers.Trigger;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

@TargetApi(24)
public class NetworkReceiver2 implements Trigger {
    private boolean mLock=true;
    private final Context context;
    private final MyNetworkCallback callback;
    private final ConnectivityManager connectivityManager;
    private final TaskItem taskItem;

    private ConnectedType connectedType=ConnectedType.NULL;

    private enum ConnectedType{
        WIFI,CELLULAR,NULL
    }

    public NetworkReceiver2(@NonNull Context context, @Nullable TaskItem item) {
        this.context=context;
        callback=new MyNetworkCallback();
        taskItem=item;
        connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public void activate() {
        try{
            if(connectivityManager!=null)connectivityManager.registerDefaultNetworkCallback(callback);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void cancel() {
        try {
            if(connectivityManager!=null){
                connectivityManager.unregisterNetworkCallback(callback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onWifiConnected(){
        if(taskItem==null)return;
        switch (taskItem.trigger_type){
            default:break;
            case TriggerTypeConsts.TRIGGER_TYPE_WIFI_CONNECTED:{
                try{
                    if(TextUtils.isEmpty(taskItem.wifiIds)){
                        runActions();
                        return;
                    }
                    WifiInfo wifiInfo=((WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
                    if(wifiInfo==null)return;
                    String [] ids=taskItem.wifiIds.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                    for(String s:ids){
                        if(Integer.parseInt(s)==wifiInfo.getNetworkId()){
                            runActions();
                            return;
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_WIFI_DISCONNECTED:{
                mLock=false;
            }
            break;
        }
    }

    private void onWifiDisconnected(){
        if(taskItem==null)return;
        switch (taskItem.trigger_type){
            default:break;
            case TriggerTypeConsts.TRIGGER_TYPE_WIFI_CONNECTED:{
                mLock=false;
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_WIFI_DISCONNECTED:{
                try {
                    if(taskItem.wifiIds==null||taskItem.wifiIds.trim().equals("")){
                        runActions();
                        return;
                    }else{
                        WifiInfo last= Global.NetworkReceiver.connectedWifiInfo;
                        if(last==null) {
                            return;
                        }
                        try{
                            String ids[]=taskItem.wifiIds.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                            for(String s:ids){
                                if(Integer.parseInt(s)== last.getNetworkId()){
                                    runActions();
                                    return;
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
        }
    }

    private void onCellConnected(){
        if(taskItem==null)return;
        switch (taskItem.trigger_type){
            default:break;
            case TriggerTypeConsts.TRIGGER_TYPE_NET_ON:{
                runActions();
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_NET_OFF:{
                mLock=false;
            }
            break;
        }
    }

    private void onCellDisconnected(){
        if(taskItem==null)return;
        switch (taskItem.trigger_type){
            default:break;
            case TriggerTypeConsts.TRIGGER_TYPE_NET_ON:{
                mLock=false;
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_NET_OFF:{
                runActions();
            }
            break;
        }
    }

    private void runActions(){
        if(!mLock){
            mLock=true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        ProcessTaskItem.checkExceptionsAndRunActions(context,taskItem);
                    }catch (Exception e){e.printStackTrace();}
                }
            }).start();
        }
    }

    private class MyNetworkCallback extends ConnectivityManager.NetworkCallback{
        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            if(connectivityManager==null||taskItem==null)return;
            NetworkCapabilities networkCapabilities=connectivityManager.getNetworkCapabilities(network);
            if(networkCapabilities==null)return;
            if(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
                connectedType=ConnectedType.WIFI;
                onWifiConnected();
            }else if(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            &&networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)){
                connectedType=ConnectedType.CELLULAR;
                onCellConnected();
            }
        }

        @Override
        public void onLost(Network network) {
            super.onLost(network);
            if(connectivityManager==null||taskItem==null)return;

            NetworkCapabilities networkCapabilities=connectivityManager.getNetworkCapabilities(network);

            if(networkCapabilities==null){
                if(connectedType==ConnectedType.WIFI){
                    connectedType=ConnectedType.NULL;
                    onWifiDisconnected();
                }else if(connectedType==ConnectedType.CELLULAR){
                    connectedType=ConnectedType.NULL;
                    onCellDisconnected();
                }
                return;
            }

            if(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
                onWifiDisconnected();
            }else if(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    &&networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)){
                onCellDisconnected();
            }
        }
    }
}
