package com.github.ghmxr.timeswitch.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;

public class RootUtils {

    public static final String COMMAND_GRANT_SECURE_PERMISSION="pm grant com.mxrapp.timeswitch android.permission.WRITE_SECURE_SETTINGS \n";
    public static final String COMMAND_ENABLE_CELLUAR_NETWORK="svc data enable \n";
    public static final String COMMAND_DISABLE_CELLUAR_NETWORK="svc data disable \n";
    public static final String COMMAND_ENABLE_GPS="settings put secure location_providers_allowed gps,network";
    public static final String COMMAND_ENABLE_GPS_API23="settings put secure location_providers_allowed +gps,-network \n";
    public static final String COMMAND_DISABLE_GPS="settings put secure location_providers_allowed off \n";
    public static final String COMMAND_ENABLE_AIRPLANE_MODE="settings put global airplane_mode_on 1\n" +
            "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true \n";
    public static final String COMMAND_DISABLE_AIRPLANE_MODE="settings put global airplane_mode_on 0\n" +
            "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false \n";
    public static final String COMMAND_REBOOT="reboot \n";
    public static final String COMMAND_SHUTDOWN="reboot -p \n";

    //public static final int ROOT_COMMAND_RESULT_REFUSE=1;
    public static final int ROOT_COMMAND_RESULT_SUCCESS=0;

    private static final String COMMAND_SU="su";

    public static int executeCommand(final String command){
        try{
            Process su = Runtime.getRuntime().exec(COMMAND_SU);

            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.writeBytes(command);
            outputStream.flush();

            outputStream.writeBytes("exit\n");
            outputStream.flush();

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
