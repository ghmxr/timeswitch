package com.github.ghmxr.timeswitch.receivers;

import android.content.BroadcastReceiver;
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
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.util.ArrayList;
import java.util.List;

public class NetworkReceiver extends BroadcastReceiver implements Runnable {
    TaskItem item;
    Context context;
    //boolean isregistered=false;
    boolean mLock=true;

    public static WifiInfo connectedWifiInfo;

    public static List<WifiConfigInfo> wifiList=null;

    public NetworkReceiver(@NonNull Context context, @Nullable TaskItem item) {
        this.context=context;
        this.item=item;
    }

    public void registerReceiver(){
        //if(!isregistered) {
        try{
            IntentFilter filter=new IntentFilter();
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(this,filter);
        }catch (Exception e){
            e.printStackTrace();
        }
           // isregistered=true;
       // }
    }

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

        //Do refresh static variables
        if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
            //refresh wifi lists
            if(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1)==WifiManager.WIFI_STATE_ENABLED){
                final WifiManager wifiManager=(WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if(wifiManager!=null) {
                   // new Thread(new Runnable() {
                       // @Override
                       // public synchronized void run() {
                            //synchronized(this){
                                wifiList=new ArrayList<>();
                                for(WifiConfiguration w:wifiManager.getConfiguredNetworks()){
                                    WifiConfigInfo wifi_info = new WifiConfigInfo();
                                    wifi_info.networkID=w.networkId;
                                    wifi_info.SSID= ValueUtils.toDisplaySSIDString(w.SSID);
                                    wifiList.add(wifi_info);
                                }
                            //}

                       // }
                   // }).start();
                }
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

            //initialize the connected wifiInfo;
            /*if(info.getDetailedState().equals(NetworkInfo.DetailedState.CONNECTED)
                    &&info.getType()==ConnectivityManager.TYPE_WIFI
                    &&info.isConnected()
                    &&wifiInfo!=null
                    &&wifiInfo.getNetworkId()>=0){
                connectedWifiInfo=wifiInfo;
            }  */

            /*WifiManager wifiManager=(WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo=wifiManager.getConnectionInfo();
            Log.d("Wifi State ",info.getState().toString());
            Log.d("wifi detailed state ",info.getDetailedState().toString());
            Log.d("Wifi Extra info ",info.getExtraInfo());
            Log.d("Wifi subtype name " ,info.getSubtypeName());
            Log.d("Wifi  sup state",""+wifiInfo.getSupplicantState().toString());
            Log.d("wifi is available ",""+info.isAvailable());
            Log.d("wifi is connected ",""+info.isConnected());
            Log.d("WifiReceiver","Wifi connected "+wifiInfo.getSSID()+" type "+info.getTypeName());  */


            if(type== PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED){

                if(info.getDetailedState().equals(NetworkInfo.DetailedState.CONNECTED)
                        &&info.getType()==ConnectivityManager.TYPE_WIFI
                        &&info.isConnected()){

                    if(item.wifiIds==null||item.wifiIds.trim().equals("")){
                        activate();
                        return;
                    }else{
                        if(wifiInfo==null) return;
                        try{
                            String [] ids=item.wifiIds.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                            for(String s:ids){
                                if(Integer.parseInt(s)==wifiInfo.getNetworkId()){
                                    activate();
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
                        activate();
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
                                    activate();
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
                    activate();
                    return;
                }
                if(!info.isConnected()){
                    mLock=false;
                    return;
                }
            }

            if(type==PublicConsts.TRIGGER_TYPE_NET_OFF){
                if(!info.isConnected()){
                    activate();
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
                    activate();
                    return;
                }
                if(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1)==WifiManager.WIFI_STATE_DISABLED){
                    mLock=false;
                    return;
                }
            }

            if(type==PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF){
                if(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1)==WifiManager.WIFI_STATE_DISABLED){
                    activate();
                    return;

                }
                if(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1)==WifiManager.WIFI_STATE_ENABLED){
                    mLock=false;
                    return;
                }
            }
        }

    }

    private void activate(){
        if(!mLock){
            mLock=true;
            new Thread(this).start();
        }
    }

    @Override
    public void run() {
        new ProcessTaskItem(context,item).activateTaskItem();
    }

    public static class WifiConfigInfo{
        public int networkID=0;
        public String SSID="";
    }

}
