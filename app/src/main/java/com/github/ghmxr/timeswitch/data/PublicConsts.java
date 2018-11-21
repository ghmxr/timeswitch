package com.github.ghmxr.timeswitch.data;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */

public class PublicConsts {

	//Following variables are written to storage and can not be changed
	public static final String PACKAGE_NAME="com.github.ghmxr.timeswitch";
	public static final String SEPARATOR_FIRST_LEVEL =";";
	public static final String SEPARATOR_SECOND_LEVEL =":";
	public static final String SPLIT_SEPARATOR_FIRST_LEVEL =SEPARATOR_FIRST_LEVEL;
	public static final String SPLIT_SEPARATOR_SECOND_LEVEL = SEPARATOR_SECOND_LEVEL;
	public static final String SEPARATOR_SMS_RECEIVERS =",";

	//internal variables ,can changed for other uses.
	public static final String ACTION_SMS_SENT=PACKAGE_NAME+".action.sms_sent";
	public static final String ACTION_SMS_DELIVERED=PACKAGE_NAME+".action.sms_delivered";

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

	public static final int TRIGGER_TYPE_SCREEN_ON						=		201;
	public static final int TRIGGER_TYPE_SCREEN_OFF						=		202;
	public static final int TRIGGER_TYPE_POWER_CONNECTED				=		203;
	public static final int TRIGGER_TYPE_POWER_DISCONNECTED				=		204;




	public static final int WEEK_SUNDAY=0;
	public static final int WEEK_MONDAY=1;
	public static final int WEEK_TUESDAY=2;
	public static final int WEEK_WEDNESDAY=3;
	public static final int WEEK_THURSDAY=4;
	public static final int WEEK_FRIDAY=5;
	public static final int WEEK_SATURDAY=6;


	public static final int EXCEPTION_LENTH=35;
	/**
	 * 0--false ,1--true
	 */
	public static final int EXCEPTION_LOCKEDSCREEN									=		0;
	public static final int EXCEPTION_UNLOCKEDSCREEN								=		1;
	public static final int EXCEPTION_WIFI_ENABLED									=		2;
	public static final int EXCEPTION_WIFI_DISABLED									=		3;
	public static final int EXCEPTION_WIFI_CONNECTED								=		4;
	public static final int EXCEPTION_WIFI_DISCONNECTED								=		5;
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


	public static final int ACTION_LENGTH =20;
	public static final int ACTION_WIFI_LOCALE											=		0;
	public static final int ACTION_BLUETOOTH_LOCALE										=		1;	
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
	 * 未启用值为 "-1>-1>-1"
	 */
	public static final int ACTION_VIBRATE_LOCALE										=		6;
	public static final int ACTION_SET_WALL_PAPER_LOCALE								=		7;
	/**
	 * int(enabled):int(resultToast)
	 */
	public static final int ACTION_SMS_LOCALE											=		8;
	/**
	 * type(vibrate)>type2(if_custom)>title>message
	 */
	public static final int ACTION_NOTIFICATION_LOCALE									=		9;
	public static final int ACTION_TOAST_LOCALE											=		10;
	public static final int ACTION_NET_LOCALE											=		11;
	public static final int ACTION_GPS_LOCALE											=		12;
	public static final int ACTION_AIRPLANE_MODE_LOCALE									=		13;
	public static final int ACTION_DEVICECONTROL_LOCALE									=		14;
	public static final int ACTION_ENABLE_TASKS_LOCALE									=		15;
	public static final int ACTION_DISABLE_TASKS_LOCALE									=		16;

	public static final int VOLUME_RING_LOCALE=0;
	public static final int VOLUME_MEDIA_LOCALE=1;
	public static final int VOLUME_NOTIFICATION_LOCALE=2;
	public static final int VOLUME_ALARM_LOCALE=3;

	public static final int RING_SELECTION_NOTIFICATION_TYPE_LOCALE=0;
	public static final int RING_SELECTION_CALL_TYPE_LOCALE =1;

	public static final int RING_TYPE_FROM_SYSTEM =0;
	public static final int RING_TYPE_FROM_MEDIA =1;
	public static final int RING_TYPE_FROM_PATH =2;

	public static final int VIBRATE_FREQUENCY_LOCALE =0;
	public static final int VIBRATE_DURATION_LOCALE =1;
	public static final int VIBRATE_INTERVAL_LOCALE =2;

	public static final int NOTIFICATION_TYPE_LOCALE =0;
	public static final int NOTIFICATION_TYPE_IF_CUSTOM_LOCALE =1;

	public static final int SMS_ENABLED_LOCALE=0;
	public static final int SMS_SUBINFO_LOCALE=1;
	public static final int SMS_RESULT_TOAST_LOCALE=2;

	public static final int NOTIFICATION_TYPE_UNSELECTED=-1;
	public static final int NOTIFICATION_TYPE_VIBRATE=1;
	public static final int NOTIFICATION_TYPE_NO_VIBRATE=0;
	public static final int NOTIFICATION_TYPE_DEFAULT=0;
	public static final int NOTIFICATION_TYPE_CUSTOM=1;

	public static final int TOAST_TYPE_LOCALE=0;
	public static final int TOAST_LOCATION_X_OFFSET_LOCALE=1;
	public static final int TOAST_LOCATION_Y_OFFSET_LOCALE=2;
	public static final int TOAST_UNSELECTED=-1;
	public static final int TOAST_TYPE_DEFAULT=0;
	public static final int TOAST_TYPE_CUSTOM=1;
	
	public static final int ACTION_OPEN				=	1;
	public static final int ACTION_CLOSE			=	0;
	public static final int ACTION_UNSELECTED 		= 	-1;
	public static final int ACTION_RING_VIBRATE		=	1;
	public static final int ACTION_RING_OFF			=	2;
	public static final int ACTION_RING_NORMAL		=	0;
	public static final int ACTION_RING_UNSELECTED	=	-1;
	public static final int ACTION_DEVICECONTROL_REBOOT		=0;
	public static final int ACTION_DEVICECONTROL_SHUTDOWN	=1;
	public static final int ACTION_DEVICECONSTROL_NONE		=-1;
	public static final int ACTION_BRIGHTNESS_UNSELECTED	=-1;
	public static final int ACTION_BRIGHTNESS_AUTO			=256;

	/**
	 * @deprecated
	 */
	public static final int ADDITION_NOTIFY=0;
	public static final int ADDITION_AUTO_DELETE=1;
	public static final int ADDITION_AUTO_CLOSE=2;

	public static final int BRIGHTNESS_MAX=255;



	public static final String PREFERENCES_NAME  				=  	"settings";

	public static final String PREFERENCES_CURRENT_TABLE_NAME	="current_table";

	public static final String PREFERENCES_IS_SUPERUSER_MODE	=	"superuser";
	public static final boolean PREFERENCES_IS_SUPERUSER_MODE_DEFAULT=false;

	public static final String PREFERENCES_AUTO_START			=	"auto_start";
	public static final boolean PREFERENCES_AUTO_START_DEFAULT	=	false;

	public static final String PREFERENCES_MAINPAGE_INDICATOR	=	"main_indicator";
	public static final boolean PREFERENCES_MAINPAGE_INDICATOR_DEFAULT	=	false;

	public static final int API_ANDROID_ALARM_MANAGER=0;
	public static final int API_JAVA_TIMER=1;

	/**
	 * 定义定时任务使用Android AlarmManager接口还是Java Timer 接口；
	 * 0 -- Android AlarmManager; 1-- Java Timer;
	 */
	public static final String PREFERENCES_API_TYPE  ="timer_api";
	public static final int PREFERENCES_API_TYPE_DEFAULT=API_ANDROID_ALARM_MANAGER;

	public static final String PREFERENCES_LOGS_NAME	="Logs";

	/**
	 * 获取Json File 对应的应用的VersionCode
	 */
	public static final String JSON_HEAD_VERSION_CODE="version_code";

}
