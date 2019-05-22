package com.github.ghmxr.timeswitch.data.v2;

public class ExceptionConsts {
    public static final int EXCEPTION_LENGTH =36;
    public static final int EXCEPTION_CONNECTOR_OR								=	-1;
    public static final int EXCEPTION_CONNECTOR_AND								=	0;
    /**
     * 0--false ,1--true
     */
    public static final int EXCEPTION_LOCKEDSCREEN									=		0;
    public static final int EXCEPTION_UNLOCKEDSCREEN								=		1;
    public static final int EXCEPTION_WIFI_ENABLED									=		2;
    public static final int EXCEPTION_WIFI_DISABLED									=		3;
    /**
     * 0-false, 1-disconnected,2-connected
     */
    public static final int EXCEPTION_HEADSET_STATUS								=		4;
    public static final int EXCEPTION_HEADSET_PLUG_OUT=1;
    public static final int EXCEPTION_HEADSET_PLUG_IN=2;
    public static final int EXCEPTION_NOTUSED										=		5;
    /**
     * 0--false ,1--true
     */
    public static final int EXCEPTION_BLUETOOTH_ENABLED								=		6;
    public static final int EXCEPTION_BLUETOOTH_DISABLED							=		7;
    public static final int EXCEPTION_RING_VIBRATE									=		8;
    public static final int EXCEPTION_RING_OFF										=		9;
    public static final int EXCEPTION_RING_NORMAL									=		10;
    public static final int EXCEPTION_NET_ENABLED									=		11;
    public static final int EXCEPTION_NET_DISABLED									=		12;
    public static final int EXCEPTION_GPS_ENABLED									=		13;
    public static final int EXCEPTION_GPS_DISABLED									=		14;
    public static final int EXCEPTION_AIRPLANE_MODE_ENABLED							=		15;
    public static final int EXCEPTION_AIRPLANE_MODE_DISABLED						=		16;
    /**
     * 0--false,1--true
     */
    public static final int EXCEPTION_SUNDAY										=	17;
    public static final int EXCEPTION_MONDAY										=	18;
    public static final int EXCEPTION_TUESDAY										=	19;
    public static final int EXCEPTION_WEDNESDAY										=	20;
    public static final int EXCEPTION_THURSDAY										=	21;
    public static final int EXCEPTION_FRIDAY										=	22;
    public static final int EXCEPTION_SATURDAY										=	23;
    /**
     * -1 stands for unselected;
     */
    public static final int EXCEPTION_START_TIME									=	24;
    public static final int EXCEPTION_END_TIME										=	25;
    /**
     * -1 stands for unselected
     */
    public static final int EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE					=	26;
    public static final int EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE					=	27;
    public static final int EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE				=	28;
    public static final int EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE				=	29;
    //public static final int EXCEPTION_HEADSET_STATUS								=	30;
    //the locales 30~34 is not used now and the initial value is 0.
    //the following initial values are -1.from the locale 35 and on.
    public static final int EXCEPTION_WIFI_STATUS   								=	35;
    public static final int EXCEPTION_WIFI_VALUE_DISCONNECTED                       =   -9;
    public static final int EXCEPTION_WIFI_VALUE_CONNECTED_TO_RANDOM_SSID           =   -10;
}
