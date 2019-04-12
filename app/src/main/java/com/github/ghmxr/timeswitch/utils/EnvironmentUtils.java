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
 * 此类中的方法不会向上抛出异常
 */
public class EnvironmentUtils {

    /**
     * 打开、关闭设备无线WiFi
     * @param context context
     * @param enabled true 打开，false 关闭
     * @return true 成功，false 失败
     */
    public static boolean setWifiEnabled(Context context,boolean enabled){
        try{
            WifiManager manager=(WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            return manager.setWifiEnabled(enabled);
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * 打开、关闭设备蓝牙
     * @param context context
     * @param enabled true 打开，false 关闭
     * @return true 成功, false 失败
     */
    public static boolean setBluetoothEnabled(Context context,boolean enabled){
        try{
            BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
            return enabled?adapter.enable():adapter.disable();
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * 设置铃声模式
     * @param context context
     * @param mode  调用AudioManager来获取参数
     * @return 执行结果
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
     * 设置铃声音量
     * @param context context
     * @param type 铃声类型，通过AudioManager获取STREAM实例
     * @param volume 铃声大小
     * @return 结果
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
     * @return 结果
     */
    public static boolean setRingtone(Context context , int type,String uri){
        try{
            RingtoneManager.setActualDefaultRingtoneUri(context,type, Uri.parse(uri));
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * 调整屏幕亮度
     * @param context context
     * @param auto_brightness 是否为自动亮度
     * @param brightness 亮度值(0~255)
     * @return 执行结果
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
     * 根据指定path设定壁纸，此为耗时操作方法
     * @param context context
     * @param path 文件path
     * @return 执行结果
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
     * 振动器
     * @param context context
     * @param frequency 次数
     * @param duration 持续时间（毫秒）
     * @param interval 间隔时间(毫秒)
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
    public static void setIfAutorotation(Context context , boolean b){
        try{
            android.provider.Settings.System.putInt(context.getContentResolver(),Settings.System.ACCELEROMETER_ROTATION,b?1:0);
        }catch (Exception e){e.printStackTrace();}
    }

    /**
     * 发送短信到指定address
     * @param context context
     * @param subscriptionId 指定发送的Sim卡，null 则表示默认卡，API 21及以下版本此参数不生效
     * @param addresses  地址
     * @param message 内容，过长将会自动拆分发送
     * @param if_need_receipt 是否显示发送回执toast
     * @return 执行结果
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
     * 设定移动网络的打开与关闭，通过shell执行
     * @param b true 打开
     * @return 执行结果
     */
    public static boolean setGprsNetworkEnabled(boolean b){
        try{
            return RootUtils.executeCommand(b?RootUtils.COMMAND_ENABLE_CELLUAR_NETWORK:RootUtils.COMMAND_DISABLE_CELLUAR_NETWORK)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * 设定GPS的打开与关闭，通过shell运行
     * @param b true 打开
     * @return 结果
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
     * 执行SU命令
     * @param command 命令，执行完后自动换行
     * @return 是否正常结束
     */
    public static boolean runSUCommand(String command){
        try{
            return RootUtils.executeCommand(command)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * 通过指定包名启动APP
     * @param context context
     * @param package_name 包名
     */
    public static void launchAppByPackageName(Context context , String package_name){
        try{
            Intent i = context.getPackageManager().getLaunchIntentForPackage(package_name);
            context.startActivity(i);
        }catch (Exception e){e.printStackTrace();}
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
        try{
            ActivityManager manager=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            manager.killBackgroundProcesses(package_name);
        }catch (Exception e){e.printStackTrace();}
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
        try{
            return RootUtils.executeCommand(RootUtils.COMMAND_FORCE_STOP_PACKAGE+" "+package_name)==RootUtils.ROOT_COMMAND_RESULT_SUCCESS;
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
                    Method getMobileDataEnabledMethod = ConnectivityManager.class.getDeclaredMethod("getMobileDataEnabled");
                    getMobileDataEnabledMethod.setAccessible(true);
                    return (Boolean) getMobileDataEnabledMethod.invoke(connectivityManager);
                }catch (Exception e){e.printStackTrace();}
            }
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

    /**
     * 判断当前是否连接Wifi网络
     * @param context context
     * @return true 连接
     */
    public static boolean isWifiConnected(Context context){
        try{
            return ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo().getType()==ConnectivityManager.TYPE_WIFI;
        }catch (Exception e){e.printStackTrace();}
        return false;
    }

}
