package com.github.ghmxr.timeswitch.services;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.github.ghmxr.timeswitch.data.v2.PublicConsts;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 此服务中的轮询线程会在接收到屏幕关闭的广播后停止，屏幕开启的广播时创建新线程并开始
 * @author mxremail@qq.com
 */
public class AppLaunchingDetectionService extends Service{

    public static AppLaunchingDetectionService service;

    private BroadcastReceiver screen_receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent==null||intent.getAction()==null) return;
            if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
                startDetecting();
            }
            if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                stopDetecting();
            }
        }
    };

    public static String ACTION_LAUNCH_INFO_CHANGED= PublicConsts.PACKAGE_NAME+"action.PACKAGE_LAUNCH_INFO_CHANGED";
    /**
     * get a string value indicating the package name;
     */
    public static String EXTRA_PACKAGE_NAME="package_name";
    /**
     * get a boolean value indicating if is the package is launched;
     */
    public static String EXTRA_IF_RUNNING="if_is_running";

    private DetectingLoopingTask task;

    @Override
    public void onCreate(){
        super.onCreate();
        if(service!=null) service.stopSelf();
        service=this;
        if(getSharedPreferences(PublicConsts.PREFERENCES_NAME,Context.MODE_PRIVATE).getInt(PublicConsts.PREFERENCES_SERVICE_TYPE,PublicConsts.PREFERENCES_SERVICE_TYPE_DEFAULT)==PublicConsts.PREFERENCES_SERVICE_TYPE_FORGROUND){
            makeThisForeGround();
        }

        try{
            IntentFilter filter=new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(screen_receiver,filter);
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startDetecting();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startDetecting(){
        if(task!=null) task.setFlag(false);
        task=new DetectingLoopingTask();
        task.setFlag(true);
        new Thread(task).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopDetecting();
        service=null;
        try{
            unregisterReceiver(screen_receiver);
        }catch (Exception e){e.printStackTrace();}
        super.onDestroy();
    }

    public void makeThisBackground(){
        stopForeground(true);
        Log.d("AppLDService","Gone to background");
    }

    public void makeThisForeGround(){
        try{
            NotificationCompat.Builder notification=TimeSwitchService.notification;
            if(notification==null){
                NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                if(Build.VERSION.SDK_INT>=26){
                    final String channelID="channel_service";
                    NotificationChannel channel=new NotificationChannel(channelID,"Service",NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                    notification=new NotificationCompat.Builder(this,channelID);
                }else{
                    notification=new NotificationCompat.Builder(this);
                }
            }
            startForeground(1,notification.build());
            Log.d("AppLDService","Came to foreground");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void startService(Context context){
        try{
            boolean isBackground=context.getSharedPreferences(PublicConsts.PREFERENCES_NAME, Activity.MODE_PRIVATE)
                    .getInt(PublicConsts.PREFERENCES_SERVICE_TYPE,PublicConsts.PREFERENCES_SERVICE_TYPE_DEFAULT)==PublicConsts.PREFERENCES_SERVICE_TYPE_BACKGROUND;
            if(isBackground){
                if(service==null) context.startService(new Intent(context,AppLaunchingDetectionService.class));
                else {
                    service.makeThisBackground();
                }
            }else{
                if(service==null) {
                    if(Build.VERSION.SDK_INT>=26) context.startForegroundService(new Intent(context,AppLaunchingDetectionService.class));
                    else  context.startService(new Intent(context,AppLaunchingDetectionService.class));
                }else {
                    service.makeThisForeGround();
                }
            }
        }catch (Exception e){e.printStackTrace();}
    }



    private void stopDetecting(){
        if(task!=null) {
            task.setFlag(false);
            task=null;
        }
    }

    private void sendAppLaunchedBroadcast(String packageName,boolean isLaunched){
        Intent i=new Intent();
        i.setAction(ACTION_LAUNCH_INFO_CHANGED);
        i.putExtra(EXTRA_PACKAGE_NAME,packageName);
        i.putExtra(EXTRA_IF_RUNNING,isLaunched);
        sendBroadcast(i);
        Log.d("AppStatus",packageName+" is "+(isLaunched?"LAUNCHED":"CLOSED"));
    }

    private class DetectingLoopingTask implements Runnable{
        private boolean flag=true;
        private void setFlag(boolean b){
            flag=b;
        }
        @Override
        public void run() {
            synchronized (AppLaunchingDetectionService.class){
                if(Build.VERSION.SDK_INT>=23){
                    UsageStatsManager manager=(UsageStatsManager)getSystemService(Context.USAGE_STATS_SERVICE);
                    if(manager==null) return;
                    long queryTime=System.currentTimeMillis();
                    String lastPackageName="";
                    //String lastClosedPackageName="";
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
                                    if(!event.getPackageName().equals(lastPackageName)){
                                        if(!lastPackageName.equals(""))sendAppLaunchedBroadcast(lastPackageName,false);
                                        lastPackageName=event.getPackageName();
                                        sendAppLaunchedBroadcast(event.getPackageName(),true);
                                    }
                                }
                            /*if(event.getEventType()==UsageEvents.Event.MOVE_TO_BACKGROUND){
                                if(!event.getPackageName().equals(lastPackageName)){
                                    //lastPackageName="";
                                    sendAppLaunchedBroadcast(event.getPackageName(),false);
                                    lastPackageName=event.getPackageName();
                                }
                            }*/

                            }
                            //Log.d("AppLDService","Loop sleep!!!!!!");
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
                            //Log.d("AppLDService","Loop sleep!!!!!!");
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
                            //Log.d("AppLDService","Loop sleep!!!!!!");
                            Thread.sleep(1000);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                }

            }

        }
    }
}
