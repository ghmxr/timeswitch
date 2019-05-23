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
 * ���иı�ϵͳ���û���ִ��һЩ�����Լ���ȡһЩȨ�ޡ��豸״̬��̬�������࣬
 * ����ִ�в�����һЩ�������ܻ������׳��쳣�����״̬������һЩ�������������׳��쳣
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
         * ����ȡʹ��Ȩ�ޣ����û�л��Ȩ������activity����һ��snackbar������Ȩ������false�����򷵻�true
         * @param activity �˷�������activity����snackbar
         * @param att snackbar����ʾ
         * @param action snackbar��action����ʾ
         * @return true-Ȩ���ѻ�ã�false-Ȩ��û�л�ò���ʾһ��UIȥ����
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
         * ���д������Ȩ�ޣ����û�л��Ȩ������activity����һ��snackbar������Ȩ������false�����򷵻�true
         * @param activity �˷�������activity����snackbar
         * @param att snackbar����ʾ
         * @param action snackbar��action����ʾ
         * @return true-Ȩ���ѻ�ã�false-Ȩ��û�л�ò���ʾһ��UIȥ����
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
         * �������ģʽȨ�ޣ����û�л��Ȩ������activity����һ��snackbar������Ȩ������false�����򷵻�true
         * @param activity �˷�������activity����snackbar
         * @param att snackbar����ʾ
         * @param action snackbar��action����ʾ
         * @return true-Ȩ���ѻ�ã�false-Ȩ��û�л�ò���ʾһ��UIȥ����
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
         * ��ת����Ӧ�õ�����ҳ
         * @param context context
         */
        public static void showAppDetailPageOfThisApplication(Context context){
            Intent i = new Intent();
            i.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            i.setData(Uri.fromParts("package", context.getPackageName(), null));
            context.startActivity(i);
        }

        /**
         * ��ʾһ������ת����Ӧ������ҳ��snackbar
         * @param activity ��Ҫ��ʾsnackbar��activity
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
     * �򿪡��ر��豸����WiFi
     * @param context context
     * @param enabled true �򿪣�false �ر�
     */
    public static boolean setWifiEnabled(Context context,boolean enabled){
        return ((WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(enabled);
    }

    /**
     * �Ƿ��ܻ��WifiManagerʵ��
     * @param context context
     * @return true-�ܹ����WifiManagerʵ��
     */
    public static boolean isWifiSupported(Context context){
        return context.getApplicationContext().getSystemService(Context.WIFI_SERVICE)!=null;
    }

    /**
     * �򿪡��ر��豸����
     * @param enabled true �򿪣�false �ر�
     * @return true �ɹ�, false ʧ��
     */
    public static boolean setBluetoothEnabled(boolean enabled){
        BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
        return enabled?adapter.enable():adapter.disable();
    }

    /**
     * �豸�Ƿ�֧������
     * @return true-�ܹ��õ�����ʵ��
     */
    public static boolean isBluetoothSupported(){
        return BluetoothAdapter.getDefaultAdapter()!=null;
    }

    /**
     * ��������ģʽ
     * @param context context
     * @param mode  ����AudioManager����ȡ����
     */
    public static void setRingerMode(Context context, int mode){
        ((AudioManager)context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE)).setRingerMode(mode);
    }

    /**
     * ������������
     * @param context context
     * @param type �������ͣ�ͨ��AudioManager��ȡSTREAMʵ��
     * @param volume ������С
     */
    public static void setRingerVolume(Context context, int type, int volume){
        ((AudioManager)context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE)).setStreamVolume(type,volume,AudioManager.FLAG_SHOW_UI);
    }

    /**
     * ��ȡ�����������ֵ
     * @param context context
     * @param type ��������
     * @return �����������ֵ
     */
    public static int getRingerMaxVolume(Context context, int type){
        try{
            AudioManager manager=(AudioManager)context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            return manager.getStreamMaxVolume(type);
        }catch (Exception e){e.printStackTrace();}
        return 0;
    }

    /**
     * ��������
     * @param context context
     * @param type ��RingtoneManager��ȡ��̬����
     * @param uri uri
     */
    public static void setRingtone(Context context , int type,String uri){
        RingtoneManager.setActualDefaultRingtoneUri(context,type, Uri.parse(uri));
    }

    /**
     * ������Ļ����
     * @param context context
     * @param auto_brightness �Ƿ�Ϊ�Զ�����
     * @param brightness ����ֵ(0~255)������ڶ��������Ѵ���true�Ļ��˲������⴫��һ��ֵ����
     * @return ִ�н��
     */
    public static boolean setBrightness(Context context, boolean auto_brightness , int brightness){
        if(auto_brightness){
            return Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE,Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        }
        return Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE,Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                &Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,brightness);
    }

    /**
     * ����ָ��path�趨��ֽ����Ϊ��ʱ��������
     * @param context context
     * @param path �ļ�path
     */
    public static void setWallPaper(Context context,String path) throws Exception{
        WallpaperManager.getInstance(context).setStream(new FileInputStream(new File(path)));
    }

    /**
     * ����
     * @param context context
     * @param frequency ����
     * @param duration ����ʱ�䣨���룩
     * @param interval ���ʱ��(����)
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
     * ��ʾһ��Toast����UI�߳�Ҳ�ɵ���
     * @param context context
     * @param offsets new int{x,y} ƫ����������Ҫʱ�ɴ���null
     * @param content ��������
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
     * �����Ƿ��Զ�ת��
     * @param context context
     * @param b true ���Զ�ת����false �ر��Զ�ת��
     */
    public static boolean setIfAutorotation(Context context , boolean b){
        return android.provider.Settings.System.putInt(context.getContentResolver(),Settings.System.ACCELEROMETER_ROTATION,b?1:0);
    }

    /**
     * ������Ʋ�������Ӧ�ĺ�������ִ�����ǰ�����������᷵��
     * @param milliseconds �����ĺ�����
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
     * ʹ����ư��ձ��֡��رյ�ѭ����������˸��������ִ�����ǰ���᷵�أ���Ҫ�����߳�ִ��
     * �˷��������������쳣
     * @param arrays ִ�в���
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
     * ���Ͷ��ŵ�ָ��address
     * @param context context
     * @param subscriptionId ָ�����͵�Sim����null ���ʾĬ�Ͽ���API 21�����°汾�˲�������Ч
     * @param addresses  ��ַ
     * @param message ���ݣ����������Զ���ַ���
     * @param if_need_receipt �Ƿ���ʾ���ͻ�ִtoast
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
     * ��ϵͳ֪ͨ������һ��֪ͨ
     * @param context context
     * @param id notification id
     * @param title ֪ͨ����
     * @param message ֪ͨ����
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
     * �趨�ƶ�����Ĵ���رգ�ͨ��shellִ��
     * @param b true ��
     * @return ִ�н��
     */
    public static boolean setGprsNetworkEnabled(boolean b){
        return RootUtils.executeCommand(b?RootUtils.COMMAND_ENABLE_CELLUAR_NETWORK:RootUtils.COMMAND_DISABLE_CELLUAR_NETWORK)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
    }

    /**
     * �趨GPS�Ĵ���رգ�ͨ��shell����
     * @param b true ��
     * @return ���
     */
    public static boolean setGpsEnabled(boolean b){
        if(Build.VERSION.SDK_INT>=23){
            return RootUtils.executeCommand(b?RootUtils.COMMAND_ENABLE_GPS_API23:RootUtils.COMMAND_DISABLE_GPS_API23)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
        }else{
            return RootUtils.executeCommand(b?RootUtils.COMMAND_ENABLE_GPS:RootUtils.COMMAND_DISABLE_GPS)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
        }
    }

    /**
     * �趨����ģʽ�Ĵ���رգ�ͨ��shellִ��
     * @param b true ��
     * @return ִ�н��
     */
    public static boolean setAirplaneModeEnabled(boolean b){
        return RootUtils.executeCommand(b?RootUtils.COMMAND_ENABLE_AIRPLANE_MODE:RootUtils.COMMAND_DISABLE_AIRPLANE_MODE)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
    }

    /**
     * ͨ��shell����ػ�
     * @return ִ�н��
     */
    public static boolean shutdownDevice(){
        return RootUtils.executeCommand(RootUtils.COMMAND_SHUTDOWN)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
    }

    /**
     * ͨ��shell��������
     * @return ִ�н��
     */
    public static boolean restartDevice(){
        return RootUtils.executeCommand(RootUtils.COMMAND_REBOOT)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
    }

    /**
     * ִ��SU����
     * @param command ���ִ������Զ�����
     * @return �Ƿ���������
     */
    public static boolean runSUCommand(String command){
        return RootUtils.executeCommand(command)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
    }

    /**
     * ͨ��ָ����������APP
     * @param context context
     * @param package_name ����
     */
    public static void launchAppByPackageName(Context context , String package_name){
        Intent i = context.getPackageManager().getLaunchIntentForPackage(package_name);
        context.startActivity(i);
    }

    /**
     * �������������
     * @param context context
     * @param package_names ��������
     */
    public static void launchAppByPackageName(Context context,String [] package_names){
        if(package_names==null||package_names.length==0) return;
        for(String s: package_names){
            launchAppByPackageName(context,s);
        }
    }

    /**
     * �ر�ָ��Ӧ�ã���ǰ̨��������ʹ�õ�Ӧ����Ч��
     * @param context context
     * @param package_name ����
     */
    public static void stopAppByPackageName(Context context,String package_name){
        ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).killBackgroundProcesses(package_name);
    }

    /**
     * �ر�ָ��Ӧ�ã���ǰ̨��������ʹ�õ�Ӧ����Ч��
     * @param context context
     * @param package_names ��������
     */
    public static void stopAppByPackageName(Context context,String[] package_names){
        if(package_names==null||package_names.length==0) return;
        for(String s:package_names){
            stopAppByPackageName(context,s);
        }
    }

    /**
     * ͨ��suǿ�ƽ���ָ����������
     * @param package_name ����
     * @return �Ƿ���������
     */
    public static boolean forceStopAppByPackageName(String package_name){
        return RootUtils.executeCommand(RootUtils.COMMAND_FORCE_STOP_PACKAGE+package_name)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
    }

    /**
     * ���Ա����Ƿ�֧�ֹ��ߴ�����
     * @return true-�ܹ���ȡ���ߴ�����ʵ��
     */
    public static boolean isLightSensorSupported(Context context){
        try{
            return ((SensorManager)context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE)).getDefaultSensor(Sensor.TYPE_LIGHT)!=null;
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * �ƶ����������Ƿ��
     * @param context context
     * @return true �򿪣� false �رջ����޷���ȡ��
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
     * Wifi�Ƿ�������
     * @return true-������
     */
    public static boolean isWifiEnabled(Context context){
        try {
            return ((WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).isWifiEnabled();
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * �жϵ�ǰ�Ƿ�����Wifi������Ƿ�������ָ��Wifi����
     * @param id �������null������������һWifi����ʱ�򷵻�true�����ߴ���һ�����ڵ���0��network id�������ӵ����id������ʱ�ŷ���true
     * @return true ���ӵ���ָ��wifi
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
     * �����Ƿ���
     */
    public static boolean isBluetoothEnabled(Context context){
        try{
            return BluetoothAdapter.getDefaultAdapter().isEnabled();
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * ��ȡ��ǰ����ģʽ���ο�ֵͨ��AudioManager��ȡʵ��
     * @return ��ǰ����ģʽ
     */
    public static int getRingerMode(Context context){
        try{
            return ((AudioManager)context.getSystemService(Context.AUDIO_SERVICE)).getRingerMode();
        }catch (Exception e){e.printStackTrace();}
        return -1;
    }

    /**
     * �жϵ�ǰϵͳ�����Ƿ�Ϊ����״̬
     * @return true-����״̬
     */
    public static boolean isScreenLockedByKeyGuardManager(Context context){
        try{
            return ((KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode();
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * ��ȡ��ǰgps�Ƿ���
     * @return true-gps����
     */
    public static boolean isGpsEnabled(Context context){
        try{
            return ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * ��ȡ��ǰ�Ƿ��ѿ�������ģʽ
     * @return true-��ǰ�ѿ�������ģʽ
     */
    public static boolean isAirplaneModeOpen(Context context){
        try{
            return Settings.System.getInt(context.getContentResolver(),Settings.ACTION_AIRPLANE_MODE_SETTINGS,0)==1;
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * ��ȡ��ǰ�Ƿ�Ϊͨ��״̬,API21��������Ч�����򷵻�Ĭ��ֵfalse
     * @return true-��ǰΪͨ��״̬
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
