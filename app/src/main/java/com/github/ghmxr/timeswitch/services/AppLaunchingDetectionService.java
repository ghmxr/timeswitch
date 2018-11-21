package com.github.ghmxr.timeswitch.services;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.github.ghmxr.timeswitch.data.PublicConsts;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class AppLaunchingDetectionService extends Service implements Runnable {
    private boolean flag=true;
    public static List<AppLaunchingDetectionService> queue=new LinkedList<>();
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
        flag=true;
        if(!queue.contains(this)) queue.add(this);
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
        if(queue.contains(this)) queue.remove(this);
        super.onDestroy();
    }

    @Override
    public void run() {
        synchronized (AppLaunchingDetectionService.class){
            if(Build.VERSION.SDK_INT>=23){
                UsageStatsManager manager=(UsageStatsManager)getSystemService(Context.USAGE_STATS_SERVICE);
                if(manager==null) return;
                long queryTime=System.currentTimeMillis();
                String lastLaunchedPackageName="";
                String lastClosedPackageName="";
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
                                if(!event.getPackageName().equals(lastLaunchedPackageName)){
                                    if(lastClosedPackageName.equals(event.getPackageName())) lastClosedPackageName="";
                                    sendAppLaunchedBroadcast(event.getPackageName(),true);
                                    lastLaunchedPackageName=event.getPackageName();
                                }
                            }
                            if(event.getEventType()==UsageEvents.Event.MOVE_TO_BACKGROUND){
                                if(!event.getPackageName().equals(lastClosedPackageName)){
                                    if(lastLaunchedPackageName.equals(event.getPackageName())) lastLaunchedPackageName="";
                                    sendAppLaunchedBroadcast(event.getPackageName(),false);
                                    lastClosedPackageName=event.getPackageName();
                                }
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
                String currentRunningPackageName="";
                while (flag){
                    try{
                        List<ActivityManager.RunningAppProcessInfo> infos=manager.getRunningAppProcesses();
                        Field processStateField=ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
                        for(ActivityManager.RunningAppProcessInfo info:infos){
                            if(info.importance== ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND&&processStateField.getInt(info)==2){
                                //Log.d("AppStatus",info.processName+" FOREGROUND");
                                if(!currentRunningPackageName.equals(info.pkgList[0])){
                                    if(!currentRunningPackageName.equals("")){
                                        sendAppLaunchedBroadcast(currentRunningPackageName,false);
                                    }
                                    currentRunningPackageName=info.pkgList[0];
                                    sendAppLaunchedBroadcast(info.pkgList[0],true);
                                }
                                break;
                            }
                        }
                        Thread.sleep(1000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }else if(Build.VERSION.SDK_INT>=14){
                ActivityManager manager=(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                if(manager==null) return;
                String currentRunningPackageName="";
                while (flag){
                    try{
                        List<ActivityManager.RunningTaskInfo> infos=manager.getRunningTasks(1);
                        for(ActivityManager.RunningTaskInfo info:infos){
                            String package_name=info.topActivity.getPackageName();
                            //Log.d("RunningInfo",package_name);
                            if(!package_name.equals(currentRunningPackageName)){
                                if(!currentRunningPackageName.equals("")) {
                                    sendAppLaunchedBroadcast(currentRunningPackageName,false);
                                    //Log.d("AppStatus",currentRunningPackageName+" is CLOSED");
                                }
                                currentRunningPackageName=package_name;
                                sendAppLaunchedBroadcast(package_name,true);
                                //Log.d("AppStatus",package_name+" is LAUNCHED");
                            }
                        }
                        Thread.sleep(1000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }

        }

    }

    public void stopDececting(){
        flag=false;
    }

    private void sendAppLaunchedBroadcast(String packageName,boolean isLaunched){
        Intent i=new Intent();
        i.setAction(ACTION_LAUNCH_INFO_CHANGED);
        i.putExtra(EXTRA_PACKAGE_NAME,packageName);
        i.putExtra(EXTRA_IF_RUNNING,isLaunched);
        sendBroadcast(i);
        //Log.d("AppStatus",packageName+" is "+(isLaunched?"LAUNCHED":"CLOSED"));
    }
}
