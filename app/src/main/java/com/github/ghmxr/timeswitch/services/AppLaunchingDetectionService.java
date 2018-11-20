package com.github.ghmxr.timeswitch.services;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.github.ghmxr.timeswitch.data.PublicConsts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppLaunchingDetectionService extends Service implements Runnable {
    private boolean flag=true;
    private Map<String,Boolean> map=new HashMap<>();
    private String currentRunningPackageName="";
    public static String ACTION_LAUNCH_INFO_CHANGED= PublicConsts.PACKAGE_NAME+"action.PACKAGE_LAUNCH_INFO_CHANGED";
    /**
     * get a string value indicating the package name;
     */
    public static String EXTRA_PACKAGE_NAME="package_name";
    /**
     * get a boolean value indicating if is the package is launched;
     */
    public static String EXTRA_IF_RUNNING="if_is_running";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(this).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        flag=false;
        super.onDestroy();
    }

    @Override
    public void run() {
        synchronized (AppLaunchingDetectionService.class){
            if(Build.VERSION.SDK_INT>=23){
                UsageStatsManager manager=(UsageStatsManager)getSystemService(Context.USAGE_STATS_SERVICE);
                if(manager==null) return;
                long queryTime=System.currentTimeMillis();
                while(flag){
                    try{
                        long startTime=queryTime-1000;
                        if(startTime<0) startTime=0;
                        UsageEvents events=manager.queryEvents(startTime,System.currentTimeMillis());
                        queryTime=System.currentTimeMillis();
                        while(events.hasNextEvent()){
                            UsageEvents.Event event=new UsageEvents.Event();
                            events.getNextEvent(event);
                            if(event.getEventType()==UsageEvents.Event.MOVE_TO_FOREGROUND){
                                //Log.d("AppStatusChanged",event.getPackageName()+" : FOREGROUND");
                                sendAppLaunchedBroadcast(event.getPackageName(),true);
                            }
                            if(event.getEventType()==UsageEvents.Event.MOVE_TO_BACKGROUND){
                                //Log.d("AppStatusChanged",event.getPackageName()+" : BACKGROUND");
                                sendAppLaunchedBroadcast(event.getPackageName(),false);
                            }

                        }
                        Thread.sleep(1000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }else if(Build.VERSION.SDK_INT>=21){
                ActivityManager manager=(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                if(manager==null) return;
                List<ActivityManager.RunningAppProcessInfo> infos=manager.getRunningAppProcesses();
                while (flag){
                    for(ActivityManager.RunningAppProcessInfo info:infos){
                       // Log.d("AppStatus",info.processName);
                        if(info.importance== ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                            Log.d("AppStatus",info.processName+" FOREGROUND");
                        }
                    }
                    SystemClock.sleep(1000);
                }
            }else if(Build.VERSION.SDK_INT>=14){
                ActivityManager manager=(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                if(manager==null) return;
                while (flag){
                    List<ActivityManager.RunningTaskInfo> infos=manager.getRunningTasks(1);
                    for(ActivityManager.RunningTaskInfo info:infos){
                        String package_name=info.topActivity.getPackageName();
                        //Log.d("RunningInfo",package_name);
                        if(!package_name.equals(currentRunningPackageName)){
                            if(!currentRunningPackageName.equals("")) {
                                sendAppLaunchedBroadcast(currentRunningPackageName,false);
                                Log.d("AppStatus",currentRunningPackageName+" is CLOSED");
                            }
                            currentRunningPackageName=package_name;
                            sendAppLaunchedBroadcast(package_name,true);
                            Log.d("AppStatus",package_name+" is LAUNCHED");
                        }
                    }
                    SystemClock.sleep(1000);
                }

            }


        }

    }

    private void sendAppLaunchedBroadcast(String packageName,boolean isLaunched){
        Intent i=new Intent();
        i.setAction(ACTION_LAUNCH_INFO_CHANGED);
        i.putExtra(EXTRA_PACKAGE_NAME,packageName);
        i.putExtra(EXTRA_IF_RUNNING,isLaunched);
        sendBroadcast(i);
    }
}
