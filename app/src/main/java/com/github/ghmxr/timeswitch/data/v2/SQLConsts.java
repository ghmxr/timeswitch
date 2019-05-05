package com.github.ghmxr.timeswitch.data.v2;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class SQLConsts {
	
	/**
	 * 数据库文件名称
	 */
	public static final String SQL_DATABASE_NAME							= "sqlite.db";
	
	/**
	 * 数据库版本
	 */
	public static final int SQL_DATABASE_VERSION							= 3;
	
	/**
	 * 数据库默认任务列表的table名称
	 */	
	public static final String SQL_DATABASE_DEFAULT_TABLE_NAME				= "tasks_default";
	
	/**
	 * 数据库表名前缀，后跟数字
	 */
	public static final String SQL_DATABASE_TABLE_NAME_FONT 				= "tasks_";

	/**
	 * 列名--任务ID
	 */
	public static final String SQL_TASK_COLUMN_ID = "_id";

	/**
	 * 列名--任务名称
	 */
	public static final String SQL_TASK_COLUMN_NAME = "name";

	/**
	 * 列名--任务是否激活(0--false 1--true)
	 */
	public static final String SQL_TASK_COLUMN_ENABLED = "enabled";

	/**
	 * 列名--任务类型
	 */
	public static final String SQL_TASK_COLUMN_TYPE 						= "trigger_type";

	/**
	 * 存储触发类型对应的参数;
	 * 0（仅一次）--{time};
	 * 1（指定时间长度重复） --{time,period_type,value};
	 * 2(周重复) --{time,SUNDAY,MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY};
	 * 3(电池电量低于某值)  --{percent};
	 * 4(电池电量高于某值)  --{percent};
	 * 5(电池温度低于某值)  --{temp*10};
	 * 6(电池温度高于某值)  --{temp*10};
	 */
	public static final String SQL_TASK_COLUMN_TRIGGER_VALUES 				="trigger_values";

	/**
	 * 列名--例外
	 */
	public static final String SQL_TASK_COLUMN_EXCEPTIONS ="exceptions";

	/**
	 * 列名--行为
	 */
	public static final String SQL_TASK_COLUMN_ACTIONS 						= "actions";

	/**
	 * 列名--附加选项（是否自动删除，是否自动关闭等）
	 */
	public static final String SQL_TASK_COLUMN_ADDITIONS						=	"additions";

	public static final String SQL_TASK_COLUMN_URI_RING_NOTIFICATION	="uri_audio1";

	public static final String SQL_TASK_COLUMN_URI_RING_CALL 			="uri_audio2";

	public static final String SQL_TASK_COLUMN_URI_WALLPAPER_DESKTOP		="uri_img1";

	public static final String SQL_TASK_COLUMN_SMS_SEND_PHONE_NUMBERS		="sms_send_address";

	public static final String SQL_TASK_COLUMN_SMS_SEND_MESSAGE				="sms_send_message";

	public static final String SQL_TASK_COLUMN_NOTIFICATION_TITLE			="notification_title";

	public static final String SQL_TASK_COLUMN_NOTIFICATION_MESSAGE			="notification_message";

	public static final String SQL_TASK_COLUMN_TOAST						="toast";

	public static final String SQL_TASK_COLUMN_ORDER						="task_order";

    /**
     * 获取Json File 对应的应用的VersionCode
     */
    public static final String JSON_HEAD_VERSION_CODE="version_code";
}
