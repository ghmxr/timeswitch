package com.github.ghmxr.timeswitch.data.v2;

public class ActionConsts {

    public static final int ACTION_LENGTH =24;

    public static class ActionFirstLevelLocaleConsts{

        public static final int ACTION_WIFI_LOCALE											=		0;
        public static final int ACTION_BLUETOOTH_LOCALE									=		1;
        public static final int ACTION_RING_MODE_LOCALE 									=		2;
        /**
         * "int:int:int"
         * "铃声音量:媒体音量:Alarm音量"
         */
        public static final int ACTION_RING_VOLUME_LOCALE									=		3;
        /**
         * "int:int"
         */
        public static final int ACTION_RING_SELECTION_LOCALE								=		4;
        public static final int ACTION_BRIGHTNESS_LOCALE 									=		5;
        /**
         * "frequency:duration:interval"
         * 未启用值为 "-1:-1:-1"
         */
        public static final int ACTION_VIBRATE_LOCALE										=		6;
        public static final int ACTION_SET_WALL_PAPER_LOCALE								=		7;
        /**
         * int(enabled):int(resultToast)
         */
        public static final int ACTION_SMS_LOCALE											=		8;
        /**
         * type(vibrate):type2(if_custom)
         */
        public static final int ACTION_NOTIFICATION_LOCALE									=		9;
        public static final int ACTION_TOAST_LOCALE										=		10;
        public static final int ACTION_NET_LOCALE											=		11;
        public static final int ACTION_GPS_LOCALE											=		12;
        public static final int ACTION_AIRPLANE_MODE_LOCALE								=		13;
        public static final int ACTION_DEVICE_CONTROL_LOCALE                              =		14;
        public static final int ACTION_ENABLE_TASKS_LOCALE									=		15;
        public static final int ACTION_DISABLE_TASKS_LOCALE								=		16;
        public static final int ACTION_LAUNCH_APP_PACKAGES									=		17;
        public static final int ACTION_STOP_APP_PACKAGES									=		18;
        public static final int ACTION_FORCE_STOP_APP_PACKAGES								=		19;
        public static final int ACTION_AUTOROTATION										=		20;
        public static final int ACTION_FLASHLIGHT                                         =       21;
        public static final int ACTION_PLAY_AUDIO                                         =       22;
        public static final int ACTION_CLEAN_NOTIFICATION                                 =       23;
    }

    public static class ActionSecondLevelLocaleConsts{

        public static final int VOLUME_RING_LOCALE                                          =0;
        public static final int VOLUME_MEDIA_LOCALE                                         =1;
        public static final int VOLUME_NOTIFICATION_LOCALE                                  =2;
        public static final int VOLUME_ALARM_LOCALE                                         =3;
        public static final int RING_SELECTION_NOTIFICATION_TYPE_LOCALE                     =0;
        public static final int RING_SELECTION_CALL_TYPE_LOCALE                             =1;
        public static final int VIBRATE_FREQUENCY_LOCALE                                    =0;
        public static final int VIBRATE_DURATION_LOCALE                                     =1;
        public static final int VIBRATE_INTERVAL_LOCALE                                     =2;
        public static final int NOTIFICATION_TYPE_LOCALE                                    =0;
        public static final int NOTIFICATION_TYPE_IF_CUSTOM_LOCALE                          =1;
        public static final int SMS_ENABLED_LOCALE                                          =0;
        public static final int SMS_SUBINFO_LOCALE                                          =1;
        public static final int SMS_RESULT_TOAST_LOCALE                                     =2;
        public static final int TOAST_TYPE_LOCALE                                           =0;
        public static final int TOAST_LOCATION_X_OFFSET_LOCALE                              =1;
        public static final int TOAST_LOCATION_Y_OFFSET_LOCALE                              =2;
        public static final int FLASHLIGHT_TYPE_LOCALE                                      =0;
    }

    public static class ActionValueConsts{

        public static final int RING_TYPE_FROM_SYSTEM                                       =0;
        public static final int RING_TYPE_FROM_MEDIA                                        =1;
        public static final int RING_TYPE_FROM_PATH                                         =2;
        public static final int NOTIFICATION_TYPE_UNSELECTED                                =-1;
        public static final int NOTIFICATION_TYPE_NOT_OVERRIDE                              =1;
        public static final int NOTIFICATION_TYPE_OVERRIDE_LAST                             =0;
        public static final int NOTIFICATION_TYPE_CONTENT_DEFAULT                           =0;
        public static final int NOTIFICATION_TYPE_CONTENT_CUSTOM                            =1;
        public static final int TOAST_UNSELECTED                                            =-1;
        public static final int TOAST_TYPE_DEFAULT                                          =0;
        public static final int TOAST_TYPE_CUSTOM                                           =1;
        public static final int ACTION_OPEN				                                  =	1;
        public static final int ACTION_CLOSE			                                      =	0;
        public static final int ACTION_UNSELECTED 		                                      = -1;
        public static final int ACTION_RING_VIBRATE		                                  =	1;
        public static final int ACTION_RING_OFF			                                  =	2;
        public static final int ACTION_RING_NORMAL		                                      =	0;
        public static final int ACTION_RING_UNSELECTED	                                      =	-1;
        public static final int ACTION_DEVICE_CONTROL_REBOOT                                 =0;
        public static final int ACTION_DEVICE_CONTROL_SHUTDOWN                               =1;
        public static final int ACTION_DEVICE_CONTROL_NONE                                   =-1;
        public static final int ACTION_BRIGHTNESS_UNSELECTED	                               =-1;
        public static final int ACTION_BRIGHTNESS_AUTO			                               =256;
        public static final int ACTION_FLASHLIGHT_TYPE_HOLD                                  =0;
        public static final int ACTION_FLASHLIGHT_TYPE_CUSTOM                                =1;
        public static final int ACTION_CLEAN_NOTIFICATION_ALL                                =1;
    }

}
