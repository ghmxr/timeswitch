package com.github.ghmxr.timeswitch.data.v2;

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


	public static final int WEEK_SUNDAY=0;
	public static final int WEEK_MONDAY=1;
	public static final int WEEK_TUESDAY=2;
	public static final int WEEK_WEDNESDAY=3;
	public static final int WEEK_THURSDAY=4;
	public static final int WEEK_FRIDAY=5;
	public static final int WEEK_SATURDAY=6;



	public static final int BRIGHTNESS_MAX=255;

	public static final String PREFERENCES_NAME  				=  	"settings";

	public static final String PREFERENCES_CURRENT_TABLE_NAME	="current_table";

	public static final String PREFERENCES_IS_SUPERUSER_MODE	=	"superuser";
	public static final boolean PREFERENCES_IS_SUPERUSER_MODE_DEFAULT=false;

	public static final String PREFERENCES_AUTO_START			=	"auto_start";
	public static final boolean PREFERENCES_AUTO_START_DEFAULT	=	false;

	public static final String PREFERENCES_MAINPAGE_INDICATOR	=	"main_indicator";
	public static final boolean PREFERENCES_MAINPAGE_INDICATOR_DEFAULT	=	false;

	public static final String PREFERENCES_THEME_COLOR="theme_color";
	public static final String PREFERENCES_THEME_COLOR_DEFAULT="#16a085";

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
	 * 定义应用服务运行于前台还是后台
	 */
	public static final String PREFERENCES_SERVICE_TYPE="service_type";
	public static final int PREFERENCES_SERVICE_TYPE_FORGROUND=1;
	public static final int PREFERENCES_SERVICE_TYPE_BACKGROUND=0;
	public static final int PREFERENCES_SERVICE_TYPE_DEFAULT=PREFERENCES_SERVICE_TYPE_BACKGROUND;

	/**
	 * 获取Json File 对应的应用的VersionCode
	 */
	public static final String JSON_HEAD_VERSION_CODE="version_code";

}
