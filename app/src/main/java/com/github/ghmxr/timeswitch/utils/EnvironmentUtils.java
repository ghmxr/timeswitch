package com.github.ghmxr.timeswitch.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telecom.TelecomManager;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.Global;
import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.receivers.SMSReceiver;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 运行改变系统设置或者执行一些操作以及获取一些权限、设备状态静态方法的类，
 * 其中执行操作的一些方法可能会向上抛出异常，获得状态参数的一些方法不会向上抛出异常
 */
public class EnvironmentUtils {

    public static class SpecialPermissionCheckUtil{

        public static boolean isWriteSettingsPermissionGranted(Context context){
            try{
                return Build.VERSION.SDK_INT<23||Settings.System.canWrite(context);
            }catch (Exception e){e.printStackTrace();}
            return false;
        }

        public static boolean isNotificationPolicyGranted(Context context){
            try{
                return Build.VERSION.SDK_INT<23||((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).isNotificationPolicyAccessGranted();
            }catch (Exception e){e.printStackTrace();}
            return false;
        }

        public static boolean isAppUsagePermissionGranted(Context context){
            try{
                return Build.VERSION.SDK_INT<19||((AppOpsManager)context.getSystemService(Context.APP_OPS_SERVICE)).checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(),context.getPackageName())==AppOpsManager.MODE_ALLOWED;
            }catch (Exception e){e.printStackTrace();}
            return false;
        }

        public static boolean isReadingNotificationPermissionGranted(Context context){
            try{
                /*String pkgName = context.getPackageName();
                final String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
                if (!TextUtils.isEmpty(flat)) {
                    final String[] names = flat.split(":");
                    for (int i = 0; i < names.length; i++) {
                        final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                        if (cn != null) {
                            if (TextUtils.equals(pkgName, cn.getPackageName())) {
                                return true;
                            }
                        }
                    }
                }
                return false;*/
                return NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.getPackageName());
            }catch (Exception e){
                e.printStackTrace();
            }
            return false;
        }
    }

    public static class PermissionRequestUtil{

        /**
         * 检查获取使用权限，如果没有获得权限则向activity发送一个snackbar申请授权并返回false，否则返回true
         * @param activity 此方法会向activity发送snackbar
         * @param att snackbar的提示
         * @param action snackbar的action的提示
         * @return true-权限已获得，false-权限没有获得并显示一个UI去申请
         */
        public static boolean checkAndShowRequestUsageStatusPermissionSnackbar(final Activity activity, String att, String action){
            if(Build.VERSION.SDK_INT<21) return true;
            if(SpecialPermissionCheckUtil.isAppUsagePermissionGranted(activity)) return true;
            Snackbar snackbar=Snackbar.make(activity.findViewById(android.R.id.content),att,Snackbar.LENGTH_SHORT);
            snackbar.setAction(action, new View.OnClickListener() {
                @Override
                @TargetApi(21)
                public void onClick(View v) {
                    activity.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                }
            });
            snackbar.show();
            return false;
        }

        /**
         * 检查写入设置权限，如果没有获得权限则向activity发送一个snackbar申请授权并返回false，否则返回true
         * @param activity 此方法会向activity发送snackbar
         * @param att snackbar的提示
         * @param action snackbar的action的提示
         * @return true-权限已获得，false-权限没有获得并显示一个UI去申请
         */
        public static boolean checkAndShowWriteSettingsPermissionRequestSnackbar(final Activity activity, String att, String action){
            if(Build.VERSION.SDK_INT<23)return true;
            if(SpecialPermissionCheckUtil.isWriteSettingsPermissionGranted(activity)) return true;
            Snackbar snackbar=Snackbar.make(activity.findViewById(android.R.id.content),att,Snackbar.LENGTH_SHORT);
            snackbar.setAction(action, new View.OnClickListener() {
                @Override
                @TargetApi(23)
                public void onClick(View v) {
                    Intent i=new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    i.setData(Uri.parse("package:"+activity.getPackageName()));
                    activity.startActivity(i);
                }
            });
            snackbar.show();
            return false;
        }

        /**
         * 检查勿扰模式权限，如果没有获得权限则向activity发送一个snackbar申请授权并返回false，否则返回true
         * @param activity 此方法会向activity发送snackbar
         * @param att snackbar的提示
         * @param action snackbar的action的提示
         * @return true-权限已获得，false-权限没有获得并显示一个UI去申请
         */
        public static boolean checkAndShowNotificationPolicyRequestSnackbar(final Activity activity,String att,String action){
            if(Build.VERSION.SDK_INT<24) return true;
            if(SpecialPermissionCheckUtil.isNotificationPolicyGranted(activity)) return true;
            Snackbar snackbar=Snackbar.make(activity.findViewById(android.R.id.content),att,Snackbar.LENGTH_SHORT);
            snackbar.setAction(action, new View.OnClickListener() {
                @Override
                @TargetApi(23)
                public void onClick(View v) {
                    activity.startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
                    Toast.makeText(activity,activity.getResources().getString(R.string.permission_request_notification_policy_toast),Toast.LENGTH_SHORT).show();
                }
            });
            snackbar.show();
            return false;
        }

        public static boolean checkAndShowNotificationReadingRequestSnackbar(final Activity activity,String att,String action){
            if(Build.VERSION.SDK_INT<18)return false;
            if(SpecialPermissionCheckUtil.isReadingNotificationPermissionGranted(activity)) return true;
            Snackbar snackbar=Snackbar.make(activity.findViewById(android.R.id.content),att,Snackbar.LENGTH_SHORT);
            snackbar.setAction(action, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    Toast.makeText(activity,activity.getResources().getString(R.string.permission_request_reading_notification_toast),Toast.LENGTH_SHORT).show();
                }
            });
            snackbar.show();
            return false;
        }

        /**
         * 跳转到本应用的详情页
         * @param context context
         */
        public static void showAppDetailPageOfThisApplication(Context context){
            Intent i = new Intent();
            i.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            i.setData(Uri.fromParts("package", context.getPackageName(), null));
            context.startActivity(i);
        }

        /**
         * 显示一个可跳转至本应用详情页的snackbar
         * @param activity 需要显示snackbar的activity
         */
        public static void showSnackbarWithActionOfAppdetailPage(final Activity activity,String att,String action){
            Snackbar snackbar=Snackbar.make(activity.findViewById(android.R.id.content),att,Snackbar.LENGTH_SHORT);
            snackbar.setAction(action, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAppDetailPageOfThisApplication(activity);
                }
            });
            snackbar.show();
        }

    }

    /**
     * 打开、关闭设备无线WiFi
     * @param context context
     * @param enabled true 打开，false 关闭
     */
    public static boolean setWifiEnabled(Context context,boolean enabled){
        return ((WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(enabled);
    }

    /**
     * 是否能获得WifiManager实例
     * @param context context
     * @return true-能够获得WifiManager实例
     */
    public static boolean isWifiSupported(Context context){
        return context.getApplicationContext().getSystemService(Context.WIFI_SERVICE)!=null;
    }

    /**
     * 打开、关闭设备蓝牙
     * @param enabled true 打开，false 关闭
     * @return true 成功, false 失败
     */
    public static boolean setBluetoothEnabled(boolean enabled){
        BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
        return enabled?adapter.enable():adapter.disable();
    }

    /**
     * 设备是否支持蓝牙
     * @return true-能够得到蓝牙实例
     */
    public static boolean isBluetoothSupported(){
        return BluetoothAdapter.getDefaultAdapter()!=null;
    }

    /**
     * 设置铃声模式
     * @param context context
     * @param mode  调用AudioManager来获取参数
     */
    public static void setRingerMode(Context context, int mode){
        ((AudioManager)context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE)).setRingerMode(mode);
    }

    /**
     * 设置铃声音量
     * @param context context
     * @param type 铃声类型，通过AudioManager获取STREAM实例
     * @param volume 铃声大小
     */
    public static void setRingerVolume(Context context, int type, int volume){
        ((AudioManager)context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE)).setStreamVolume(type,volume,AudioManager.FLAG_SHOW_UI);
    }

    /**
     * 获取铃声最大音量值
     * @param context context
     * @param type 铃声类型
     * @return 最大铃声音量值
     */
    public static int getRingerMaxVolume(Context context, int type){
        try{
            AudioManager manager=(AudioManager)context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            return manager.getStreamMaxVolume(type);
        }catch (Exception e){e.printStackTrace();}
        return 0;
    }

    /**
     * 设置铃声
     * @param context context
     * @param type 从RingtoneManager获取静态参数
     * @param uri uri
     */
    public static void setRingtone(Context context , int type,String uri){
        RingtoneManager.setActualDefaultRingtoneUri(context,type, Uri.parse(uri));
    }

    /**
     * 调整屏幕亮度
     * @param context context
     * @param auto_brightness 是否为自动亮度
     * @param brightness 亮度值(0~255)，如果第二个参数已传入true的话此参数随意传入一个值即可
     * @return 执行结果
     */
    public static boolean setBrightness(Context context, boolean auto_brightness , int brightness){
        if(auto_brightness){
            return Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE,Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        }
        return Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE,Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                &Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,brightness);
    }

    /**
     * 根据指定path设定壁纸，此为耗时操作方法
     * @param context context
     * @param path 文件path
     */
    public static void setWallPaper(Context context,String path) throws Exception{
        WallpaperManager.getInstance(context).setStream(new FileInputStream(new File(path)));
    }

    /**
     * 振动器
     * @param context context
     * @param frequency 次数
     * @param duration 持续时间（毫秒）
     * @param interval 间隔时间(毫秒)
     */
    public static void vibrate(Context context,int frequency,long duration,long interval){
        Vibrator vibrator=(Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] vibrate_array=new long[(frequency*2)+1];
        vibrate_array[0]=0;
        for(int i=1;i<vibrate_array.length;i++){
            vibrate_array[i]=(i%2==0?interval:duration);
        }
        vibrator.vibrate(vibrate_array,-1);
    }

    /**
     * 显示一个Toast，非UI线程也可调用
     * @param context context
     * @param offsets new int{x,y} 偏移量，不需要时可传入null
     * @param content 文字内容
     */
    public static void showToast(final Context context, @Nullable final int[]offsets, final String content){
        Global.handler.post(new Runnable() {
            @Override
            public void run() {
                try{
                    Toast toast=Toast.makeText(context,content,Toast.LENGTH_SHORT);
                    if(offsets!=null){
                        toast.setGravity(Gravity.TOP|Gravity.START,offsets[0],offsets[1]);
                    }
                    toast.show();
                }catch (Exception e){e.printStackTrace();}

            }
        });
    }

    /**
     * 设置是否自动转屏
     * @param context context
     * @param b true 打开自动转屏，false 关闭自动转屏
     */
    public static boolean setIfAutorotation(Context context , boolean b){
        return android.provider.Settings.System.putInt(context.getContentResolver(),Settings.System.ACCELEROMETER_ROTATION,b?1:0);
    }

    /**
     * 打开闪光灯并持续相应的毫秒数，执行完成前保持阻塞不会返回
     * @param milliseconds 持续的毫秒数
     */
    public static synchronized void setTorch(Context context,long milliseconds) {
        if(milliseconds<20) milliseconds=20;
        if(Build.VERSION.SDK_INT<23){
            Camera camera=null;
            try{
                camera=Camera.open();
                Camera.Parameters parameters=camera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
                //SystemClock.sleep(milliseconds);
                Thread.sleep(milliseconds);
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try{
                    Camera.Parameters parameters=camera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(parameters);
                }catch (Exception e){}
                try{camera.release();}catch (Exception e){}
            }
        }else{
            CameraManager manager=null;
            try{
                manager=(CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
                manager.setTorchMode("0",true);
                //SystemClock.sleep(milliseconds);
                Thread.sleep(milliseconds);
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try{
                    manager.setTorchMode("0",false);
                }catch (Exception e){e.printStackTrace();}
            }
        }
    }

    /**
     * 使闪光灯按照保持、关闭的循环点亮（闪烁），方法执行完成前不会返回，需要在子线程执行
     * 此方法不会向上抛异常
     * @param arrays 执行参数
     */
    public static synchronized void setTorch(Context context,long[]arrays){
        if(arrays==null||arrays.length==0) return;
        if(Build.VERSION.SDK_INT<23){
            Camera camera=null;
            try{
                camera=Camera.open();
                Camera.Parameters parameters;
                for(int i=0;i<arrays.length;i++){
                    if(i%2==0){
                        //open the torch and hold the milliseconds
                        parameters=camera.getParameters();
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        camera.setParameters(parameters);
                        //hold on
                        //SystemClock.sleep(arrays[i]<20?20:arrays[i]);
                        Thread.sleep(arrays[i]<20?20:arrays[i]);
                    }
                    else {
                        //close and hold on
                        parameters=camera.getParameters();
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        camera.setParameters(parameters);
                        //SystemClock.sleep(arrays[i]<20?20:arrays[i]);
                        Thread.sleep(arrays[i]<20?20:arrays[i]);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try{
                    Camera.Parameters parameters=camera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(parameters);
                }catch (Exception e){}
                try{camera.release();}catch (Exception e){}
            }
        }else{
            CameraManager manager=null;
            try{
                manager=(CameraManager)context.getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
                for(int i=0;i<arrays.length;i++){
                    if(i%2==0){
                        manager.setTorchMode("0",true);
                        //hold on
                        //SystemClock.sleep(arrays[i]<20?20:arrays[i]);
                        Thread.sleep(arrays[i]<20?20:arrays[i]);
                    }else{
                        manager.setTorchMode("0",false);
                        //hold on
                        //SystemClock.sleep(arrays[i]<20?20:arrays[i]);
                        Thread.sleep(arrays[i]<20?20:arrays[i]);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try{
                    manager.setTorchMode("0",false);
                }catch (Exception e){e.printStackTrace();}
            }
        }
    }

    /**
     * 发送短信到指定address
     * @param context context
     * @param subscriptionId 指定发送的Sim卡，null 则表示默认卡，API 21及以下版本此参数不生效
     * @param addresses  地址
     * @param message 内容，过长将会自动拆分发送
     * @param if_need_receipt 是否显示发送回执toast
     */
    public static void sendSMSMessage(Context context,@Nullable Integer subscriptionId,String[]addresses,String message,boolean if_need_receipt){
        SmsManager manager;
        if(subscriptionId!=null&& Build.VERSION.SDK_INT>=22){
            manager=SmsManager.getSmsManagerForSubscriptionId(subscriptionId);
        }else{
            manager=SmsManager.getDefault();
        }
        ArrayList<String> msgs=manager.divideMessage(message);
        for(String address:addresses){
            Intent i_delivered=new Intent(PublicConsts.ACTION_SMS_DELIVERED);
            i_delivered.putExtra(SMSReceiver.EXTRA_IF_SHOW_RECEIPT_TOAST,if_need_receipt);
            i_delivered.putExtra(SMSReceiver.EXTRA_SENT_ADDRESS,address);
            PendingIntent pi_receipt=PendingIntent.getBroadcast(context,0,i_delivered,PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent pi_sent=PendingIntent.getBroadcast(context,0,new Intent(PublicConsts.ACTION_SMS_SENT),PendingIntent.FLAG_UPDATE_CURRENT);
            for(String s:msgs){
                manager.sendTextMessage(address,null,s,pi_sent,pi_receipt);
            }
        }
    }

    /**
     * 向系统通知栏发送一条通知
     * @param context context
     * @param id notification id
     * @param title 通知标题
     * @param message 通知内容
     */
    public static void sendNotification(Context context, int id, @NonNull String title, @NonNull String message){
        NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;
        if(Build.VERSION.SDK_INT>=26){
            String channel_id="channel_tasks";
            NotificationChannel channel=new NotificationChannel(channel_id,"Tasks", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
            builder=new NotificationCompat.Builder(context,channel_id);
        }else{
            builder=new NotificationCompat.Builder(context);
        }
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle(title);
        builder.setContentText(message);
        PendingIntent pi =PendingIntent.getActivity(context,1,new Intent(),PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);
        builder.setAutoCancel(true);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        builder.setFullScreenIntent(pi,false);
        manager.notify(id,builder.build());
    }

    /**
     * 设定移动网络的打开与关闭，通过shell执行
     * @param b true 打开
     * @return 执行结果
     */
    public static boolean setGprsNetworkEnabled(boolean b){
        return RootUtils.executeCommand(b?RootUtils.COMMAND_ENABLE_CELLUAR_NETWORK:RootUtils.COMMAND_DISABLE_CELLUAR_NETWORK)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
    }

    /**
     * 设定GPS的打开与关闭，通过shell运行
     * @param b true 打开
     * @return 结果
     */
    public static boolean setGpsEnabled(boolean b){
        if(Build.VERSION.SDK_INT>=23){
            return RootUtils.executeCommand(b?RootUtils.COMMAND_ENABLE_GPS_API23:RootUtils.COMMAND_DISABLE_GPS_API23)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
        }else{
            return RootUtils.executeCommand(b?RootUtils.COMMAND_ENABLE_GPS:RootUtils.COMMAND_DISABLE_GPS)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
        }
    }

    /**
     * 设定飞行模式的打开与关闭，通过shell执行
     * @param b true 打开
     * @return 执行结果
     */
    public static boolean setAirplaneModeEnabled(boolean b){
        return RootUtils.executeCommand(b?RootUtils.COMMAND_ENABLE_AIRPLANE_MODE:RootUtils.COMMAND_DISABLE_AIRPLANE_MODE)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
    }

    /**
     * 通过shell命令关机
     * @return 执行结果
     */
    public static boolean shutdownDevice(){
        return RootUtils.executeCommand(RootUtils.COMMAND_SHUTDOWN)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
    }

    /**
     * 通过shell命令重启
     * @return 执行结果
     */
    public static boolean restartDevice(){
        return RootUtils.executeCommand(RootUtils.COMMAND_REBOOT)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
    }

    /**
     * 执行SU命令
     * @param command 命令，执行完后自动换行
     * @return 是否正常结束
     */
    public static boolean runSUCommand(String command){
        return RootUtils.executeCommand(command)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
    }

    /**
     * 通过指定包名启动APP
     * @param context context
     * @param package_name 包名
     */
    public static void launchAppByPackageName(Context context , String package_name){
        Intent i = context.getPackageManager().getLaunchIntentForPackage(package_name);
        context.startActivity(i);
    }

    /**
     * 尝试启动多个包
     * @param context context
     * @param package_names 包名数组
     */
    public static void launchAppByPackageName(Context context,String [] package_names){
        if(package_names==null||package_names.length==0) return;
        for(String s: package_names){
            launchAppByPackageName(context,s);
        }
    }

    /**
     * 关闭指定应用（对前台或者正在使用的应用无效）
     * @param context context
     * @param package_name 包名
     */
    public static void stopAppByPackageName(Context context,String package_name){
        ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).killBackgroundProcesses(package_name);
    }

    /**
     * 关闭指定应用（对前台或者正在使用的应用无效）
     * @param context context
     * @param package_names 包名数组
     */
    public static void stopAppByPackageName(Context context,String[] package_names){
        if(package_names==null||package_names.length==0) return;
        for(String s:package_names){
            stopAppByPackageName(context,s);
        }
    }

    /**
     * 通过su强制结束指定包名进程
     * @param package_name 包名
     * @return 是否正常结束
     */
    public static boolean forceStopAppByPackageName(String package_name){
        return RootUtils.executeCommand(RootUtils.COMMAND_FORCE_STOP_PACKAGE+package_name)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
    }

    /**
     * 测试本机是否支持光线传感器
     * @return true-能够获取光线传感器实例
     */
    public static boolean isLightSensorSupported(Context context){
        try{
            return ((SensorManager)context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE)).getDefaultSensor(Sensor.TYPE_LIGHT)!=null;
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * 移动数据网络是否打开
     * @param context context
     * @return true 打开， false 关闭或者无法读取到
     */
    public static boolean isGprsNetworkEnabled(Context context){
        try{
            ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
            if(networkInfo.getType()==ConnectivityManager.TYPE_MOBILE) return true;
            if(networkInfo.getType()==ConnectivityManager.TYPE_WIFI){
                try{
                    /*if(Build.VERSION.SDK_INT>=26){
                        TelephonyManager manager=(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                        return manager.isDataEnabled();
                    }*/
                    Method getMobileDataEnabledMethod = ConnectivityManager.class.getDeclaredMethod("getMobileDataEnabled");
                    getMobileDataEnabledMethod.setAccessible(true);
                    return (Boolean) getMobileDataEnabledMethod.invoke(connectivityManager);
                }catch (Exception e){e.printStackTrace();}
            }
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * Wifi是否已启用
     * @return true-已启用
     */
    public static boolean isWifiEnabled(Context context){
        try {
            return ((WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).isWifiEnabled();
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * 判断当前是否连接Wifi网络或是否连接至指定Wifi网络
     * @param id 如果传入null，则连接至任一Wifi网络时则返回true，或者传入一个大于等于0的network id，则当连接到这个id的网络时才返回true
     * @return true 连接到了指定wifi
     */
    public static boolean isWifiConnected(Context context,@Nullable Integer id){
        try{
            if(id==null) return ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo().getType()==ConnectivityManager.TYPE_WIFI;
            WifiInfo info=Global.NetworkReceiver.connectedWifiInfo;
            return ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo().getType()==ConnectivityManager.TYPE_WIFI
                    &&info!=null&&id.equals(info.getNetworkId());
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * 蓝牙是否开启
     */
    public static boolean isBluetoothEnabled(Context context){
        try{
            return BluetoothAdapter.getDefaultAdapter().isEnabled();
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * 获取当前铃声模式，参考值通过AudioManager获取实例
     * @return 当前铃声模式
     */
    public static int getRingerMode(Context context){
        try{
            return ((AudioManager)context.getSystemService(Context.AUDIO_SERVICE)).getRingerMode();
        }catch (Exception e){e.printStackTrace();}
        return -1;
    }

    /**
     * 判断当前系统键盘是否为锁定状态
     * @return true-锁屏状态
     */
    public static boolean isScreenLockedByKeyGuardManager(Context context){
        try{
            return ((KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode();
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * 获取当前gps是否开启
     * @return true-gps开启
     */
    public static boolean isGpsEnabled(Context context){
        try{
            return ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * 获取当前是否已开启飞行模式
     * @return true-当前已开启飞行模式
     */
    public static boolean isAirplaneModeOpen(Context context){
        try{
            return Settings.System.getInt(context.getContentResolver(),Settings.ACTION_AIRPLANE_MODE_SETTINGS,0)==1;
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * 获取当前是否为通话状态,API21及以上生效，否则返回默认值false
     * @return true-当前为通话状态
     */
    public static boolean isInCall(Context context){
        try{
            if(Build.VERSION.SDK_INT<21) return false;
            return ((TelecomManager) context.getSystemService(Context.TELECOM_SERVICE)).isInCall();
        }catch (SecurityException se){
            return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static List<SubscriptionInfo> getAvailableSubscribtionInfos(Context context){
        try{
            if(Build.VERSION.SDK_INT<22) return null;
            return SubscriptionManager.from(context).getActiveSubscriptionInfoList();
        }catch (Exception e){e.printStackTrace();}
        return null;
    }

}
