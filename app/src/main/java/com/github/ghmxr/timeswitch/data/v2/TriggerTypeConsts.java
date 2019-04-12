package com.github.ghmxr.timeswitch.data.v2;

public class TriggerTypeConsts {

    //Following variables are written to storage and can not be changed
    public static final int TRIGGER_TYPE_SINGLE										=	0	;
    public static final int TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME						=	1	;
    public static final int TRIGGER_TYPE_LOOP_WEEK									=	2	;
    public static final int TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE				=	3	;
    public static final int TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE				=	4	;
    public static final int TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE             =	5;
    public static final int TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE            =	6;
    public static final int TRIGGER_TYPE_WIFI_CONNECTED								=	7;
    public static final int TRIGGER_TYPE_WIFI_DISCONNECTED							=	8;
    public static final int TRIGGER_TYPE_RECEIVED_BROADCAST 						=	9;
    public static final int TRIGGER_TYPE_APP_LAUNCHED								=	10;
    public static final int TRIGGER_TYPE_APP_CLOSED									=	11;
    public static final int TRIGGER_TYPE_WIDGET_WIFI_ON					=		101;
    public static final int TRIGGER_TYPE_WIDGET_WIFI_OFF				=		102;
    public static final int TRIGGER_TYPE_WIDGET_BLUETOOTH_ON			=		103;
    public static final int TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF			=		104;
    public static final int TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE		=		105;
    public static final int TRIGGER_TYPE_WIDGET_RING_MODE_OFF			=		106;
    public static final int TRIGGER_TYPE_WIDGET_RING_NORMAL				=		107;
    public static final int TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON		=		108;
    public static final int TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF		=		109;
    public static final int TRIGGER_TYPE_WIDGET_AP_ENABLED				=		110;
    public static final int TRIGGER_TYPE_WIDGET_AP_DISABLED				=		111;
    public static final int TRIGGER_TYPE_NET_ON							=		112;
    public static final int TRIGGER_TYPE_NET_OFF						=		113;
    public static final int TRIGGER_TYPE_HEADSET_PLUG_IN				=		114;
    public static final int TRIGGER_TYPE_HEADSET_PLUG_OUT				=		115;
    public static final int TRIGGER_TYPE_SCREEN_ON						=		201;
    public static final int TRIGGER_TYPE_SCREEN_OFF						=		202;
    public static final int TRIGGER_TYPE_POWER_CONNECTED				=		203;
    public static final int TRIGGER_TYPE_POWER_DISCONNECTED				=		204;
}
