package com.github.ghmxr.timeswitch.triggers.receivers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.utils.LogUtil;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.util.ArrayList;
import java.util.List;

public class NetworkReceiver extends BaseBroadcastReceiver{
    boolean mLock=true;

    public static WifiInfo connectedWifiInfo;

    public static final List<WifiConfigInfo> wifiList=new ArrayList<>();

    public NetworkReceiver(@NonNull Context context, @Nullable TaskItem item) {
        super(context,item);
    }

    /**
     * @deprecated
     */
    public void registerReceiver(){
        //if(!isregistered) {

           // isregistered=true;
       // }
    }

    /**
     * @deprecated
     */
    public void unregisterReceiver(){
       // if(isregistered) {
        try{
            context.unregisterReceiver(this);
        }catch (Exception e){
            e.printStackTrace();
        }
        //    isregistered=false;
       // }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent==null ) return;
        if(intent.getAction()==null) return;

        if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
            if(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1)==WifiManager.WIFI_STATE_ENABLED){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (wifiList){
                            try{
                                wifiList.clear();
                                final WifiManager wifiManager=(WifiManager) NetworkReceiver.this.context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                for(WifiConfiguration w:wifiManager.getConfiguredNetworks()){
                                    WifiConfigInfo wifi_info = new WifiConfigInfo();
                                    wifi_info.networkID=w.networkId;
                                    wifi_info.SSID= ValueUtils.toDisplaySSIDString(w.SSID);
                                    wifiList.add(wifi_info);
                                }
                            }catch (Exception e){e.printStackTrace();}
                        }
                    }
                }).start();
            }
        }

        if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            NetworkInfo info=intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

            WifiInfo wifiInfo=null;
            WifiManager wifiManager=(WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if(wifiManager!=null) wifiInfo=wifiManager.getConnectionInfo();

            //initialize the connected wifiInfo;
            if(info.getDetailedState().equals(NetworkInfo.DetailedState.CONNECTED)
                    &&info.getType()==ConnectivityManager.TYPE_WIFI
                    &&info.isConnected()
                    &&wifiInfo!=null
                    &&wifiInfo.getNetworkId()>=0){
                connectedWifiInfo=wifiInfo;
            }
        }

        //Do the taskitem tasks

        if(item==null) return;

        int type=item.trigger_type;

        if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            NetworkInfo info=intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            WifiInfo wifiInfo=null;
            WifiManager wifiManager=(WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if(wifiManager!=null) wifiInfo=wifiManager.getConnectionInfo();

            if(type== PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED){

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

            if(type==PublicConsts.TRIGGER_TYPE_WIFI_DISCONNECTED){

                if(info.getDetailedState().equals(NetworkInfo.DetailedState.DISCONNECTED)
                        &&info.getType()==ConnectivityManager.TYPE_WIFI
                        &&!info.isConnected()){
                    if(item.wifiIds==null||item.wifiIds.trim().equals("")){
                        runActions();
                        return;
                    }else{
                        //Log.d("NetworkReceiver","Beginning judgement");
                        if(connectedWifiInfo==null) {
                            //Log.e("NetworkReceiver","WifiInfo is null!!!!");
                            return;
                        }
                        try{
                            String ids[]=item.wifiIds.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                            //Log.d("WWWIIIIIFFFFFIIIIids", Arrays.toString(ids));
                            //Log.d("WIFI Current id",""+connectedWifiInfo.getNetworkId());
                            for(String s:ids){
                                if(Integer.parseInt(s)==connectedWifiInfo.getNetworkId()){
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
            if(type==PublicConsts.TRIGGER_TYPE_NET_ON){
                if(info.isConnected()){
                    runActions();
                    return;
                }
                if(!info.isConnected()){
                    mLock=false;
                    return;
                }
            }

            if(type==PublicConsts.TRIGGER_TYPE_NET_OFF){
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
            if(type==PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_ON){
                if(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1)==(WifiManager.WIFI_STATE_ENABLED)){
                    runActions();
                    return;
                }
                if(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1)==WifiManager.WIFI_STATE_DISABLED){
                    mLock=false;
                    return;
                }
            }

            if(type==PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF){
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

    public static class WifiConfigInfo{
        public int networkID=0;
        public String SSID="";
    }

}
