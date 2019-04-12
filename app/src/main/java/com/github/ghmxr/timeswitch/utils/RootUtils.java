package com.github.ghmxr.timeswitch.utils;

import com.github.ghmxr.timeswitch.data.v2.PublicConsts;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;

public class RootUtils {

    public static final String COMMAND_GRANT_SECURE_PERMISSION="pm grant "+ PublicConsts.PACKAGE_NAME+" android.permission.WRITE_SECURE_SETTINGS \n";
    public static final String COMMAND_FORCE_STOP_PACKAGE="am force-stop ";
    public static final String COMMAND_ENABLE_CELLUAR_NETWORK="svc data enable";
    public static final String COMMAND_DISABLE_CELLUAR_NETWORK="svc data disable";
    public static final String COMMAND_ENABLE_GPS="settings put secure location_providers_allowed gps";
    public static final String COMMAND_ENABLE_GPS_API23="settings put secure location_providers_allowed +gps";
    public static final String COMMAND_DISABLE_GPS="settings put secure location_providers_allowed off";
    public static final String COMMAND_DISABLE_GPS_API23="settings put secure location_providers_allowed -gps";
    public static final String COMMAND_ENABLE_AIRPLANE_MODE="settings put global airplane_mode_on 1\n" +
            "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true \n";
    public static final String COMMAND_DISABLE_AIRPLANE_MODE="settings put global airplane_mode_on 0\n" +
            "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false \n";
    public static final String COMMAND_REBOOT="reboot";
    public static final String COMMAND_SHUTDOWN="reboot -p";

    //public static final int ROOT_COMMAND_RESULT_REFUSE=1;
    public static final int ROOT_COMMAND_RESULT_SUCCESS=0;

    private static final String COMMAND_SU="su";

    public static int executeCommand(final String command){
        try{
            Process su = Runtime.getRuntime().exec(COMMAND_SU);

            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.writeBytes(command+" \n");
            outputStream.flush();

            outputStream.writeBytes("exit\n");
            outputStream.flush();

            try{su.getInputStream().close();}catch (Exception e){}
            try{su.getOutputStream().close();}catch (Exception e){}
            try{su.getErrorStream().close();}catch (Exception e){}

            return su.waitFor();
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }

    }

    public static boolean isDeviceRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
    }

    private static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkRootMethod2() {
        String[] paths = { "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private static boolean checkRootMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[] { "/system/xbin/which", "su" });
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) return true;
            return false;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }


}
