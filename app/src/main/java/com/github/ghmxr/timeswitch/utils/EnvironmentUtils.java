package com.github.ghmxr.timeswitch.utils;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.Global;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.receivers.SMSReceiver;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * �����еķ������������׳��쳣
 */
public class EnvironmentUtils {

    /**
     * �򿪡��ر��豸����WiFi
     * @param context context
     * @param enabled true �򿪣�false �ر�
     * @return true �ɹ���false ʧ��
     */
    public static boolean setWifiEnabled(Context context,boolean enabled){
        try{
            WifiManager manager=(WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            return manager.setWifiEnabled(enabled);
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * �򿪡��ر��豸����
     * @param context context
     * @param enabled true �򿪣�false �ر�
     * @return true �ɹ�, false ʧ��
     */
    public static boolean setBluetoothEnabled(Context context,boolean enabled){
        try{
            BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
            return enabled?adapter.enable():adapter.disable();
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * ��������ģʽ
     * @param context context
     * @param mode  ����AudioManager����ȡ����
     * @return ִ�н��
     */
    public static boolean setRingerMode(Context context, int mode){
        try{
            AudioManager manager=(AudioManager)context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            manager.setRingerMode(mode);
            return true;
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * ������������
     * @param context context
     * @param type �������ͣ�ͨ��AudioManager��ȡSTREAMʵ��
     * @param volume ������С
     * @return ���
     */
    public static boolean setRingerVolume(Context context, int type, int volume){
        try{
           AudioManager manager=(AudioManager)context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
           manager.setStreamVolume(type,volume,AudioManager.FLAG_SHOW_UI);
           return true;
        }catch (Exception e){e.printStackTrace();}
        return false;
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
     * @return ���
     */
    public static boolean setRingtone(Context context , int type,String uri){
        try{
            RingtoneManager.setActualDefaultRingtoneUri(context,type, Uri.parse(uri));
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * ������Ļ����
     * @param context context
     * @param auto_brightness �Ƿ�Ϊ�Զ�����
     * @param brightness ����ֵ(0~255)
     * @return ִ�н��
     */
    public static boolean setBrightness(Context context, boolean auto_brightness , int brightness){
        try{
            if(auto_brightness){
                Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE,Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            }else{
                Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE,Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
            return Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,brightness);
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * ����ָ��path�趨��ֽ����Ϊ��ʱ��������
     * @param context context
     * @param path �ļ�path
     * @return ִ�н��
     */
    public static boolean setWallPaper(Context context,String path){
        try{
            WallpaperManager wallpaperManager=WallpaperManager.getInstance(context);
            //Bitmap bitmap= BitmapFactory.decodeFile(path);
            //if(bitmap==null) return false;
            //wallpaperManager.setBitmap(bitmap);
            wallpaperManager.setStream(new FileInputStream(new File(path)));
            return true;
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * ����
     * @param context context
     * @param frequency ����
     * @param duration ����ʱ�䣨���룩
     * @param interval ���ʱ��(����)
     */
    public static void vibrate(Context context,int frequency,long duration,long interval){
        try{
            Vibrator vibrator=(Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
            long[] vibrate_array=new long[(frequency*2)+1];
            vibrate_array[0]=0;
            for(int i=1;i<vibrate_array.length;i++){
                vibrate_array[i]=(i%2==0?interval:duration);
            }
            vibrator.vibrate(vibrate_array,-1);
        }catch (Exception e){e.printStackTrace();}
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
    public static void setIfAutorotation(Context context , boolean b){
        try{
            android.provider.Settings.System.putInt(context.getContentResolver(),Settings.System.ACCELEROMETER_ROTATION,b?1:0);
        }catch (Exception e){e.printStackTrace();}
    }

    /**
     * ���Ͷ��ŵ�ָ��address
     * @param context context
     * @param subscriptionId ָ�����͵�Sim����null ���ʾĬ�Ͽ���API 21�����°汾�˲�������Ч
     * @param addresses  ��ַ
     * @param message ���ݣ����������Զ���ַ���
     * @param if_need_receipt �Ƿ���ʾ���ͻ�ִtoast
     * @return ִ�н��
     */
    public static boolean sendSMSMessage(Context context,@Nullable Integer subscriptionId,String[]addresses,String message,boolean if_need_receipt){
        try{
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
                for(String s:msgs){
                    manager.sendTextMessage(address,null,s,null,pi_receipt);
                }
            }
            return true;
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * �趨�ƶ�����Ĵ���رգ�ͨ��shellִ��
     * @param b true ��
     * @return ִ�н��
     */
    public static boolean setGprsNetworkEnabled(boolean b){
        try{
            return RootUtils.executeCommand(b?RootUtils.COMMAND_ENABLE_CELLUAR_NETWORK:RootUtils.COMMAND_DISABLE_CELLUAR_NETWORK)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * �趨GPS�Ĵ���رգ�ͨ��shell����
     * @param b true ��
     * @return ���
     */
    public static boolean setGpsEnabled(boolean b){
        try{
            if(Build.VERSION.SDK_INT>=23){
                return RootUtils.executeCommand(b?RootUtils.COMMAND_ENABLE_GPS_API23:RootUtils.COMMAND_DISABLE_GPS_API23)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
            }else{
                return RootUtils.executeCommand(b?RootUtils.COMMAND_DISABLE_GPS:RootUtils.COMMAND_DISABLE_GPS)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
            }
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * ִ��SU����
     * @param command ���ִ������Զ�����
     * @return �Ƿ���������
     */
    public static boolean runSUCommand(String command){
        try{
            return RootUtils.executeCommand(command)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * ͨ��ָ����������APP
     * @param context context
     * @param package_name ����
     */
    public static void launchAppByPackageName(Context context , String package_name){
        try{
            Intent i = context.getPackageManager().getLaunchIntentForPackage(package_name);
            context.startActivity(i);
        }catch (Exception e){e.printStackTrace();}
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
        try{
            ActivityManager manager=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            manager.killBackgroundProcesses(package_name);
        }catch (Exception e){e.printStackTrace();}
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
        try{
            return RootUtils.executeCommand(RootUtils.COMMAND_FORCE_STOP_PACKAGE+" "+package_name)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
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
                    Method getMobileDataEnabledMethod = ConnectivityManager.class.getDeclaredMethod("getMobileDataEnabled");
                    getMobileDataEnabledMethod.setAccessible(true);
                    return (Boolean) getMobileDataEnabledMethod.invoke(connectivityManager);
                }catch (Exception e){e.printStackTrace();}
            }
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * �жϵ�ǰ�Ƿ�����Wifi����
     * @param context context
     * @return true ����
     */
    public static boolean isWifiConnected(Context context){
        try{
            return ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo().getType()==ConnectivityManager.TYPE_WIFI;
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

}
