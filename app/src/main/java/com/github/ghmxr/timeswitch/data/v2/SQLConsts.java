package com.github.ghmxr.timeswitch.data.v2;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class SQLConsts {
	
	/**
	 * ���ݿ��ļ�����
	 */
	public static final String SQL_DATABASE_NAME							= "sqlite.db";
	
	/**
	 * ���ݿ�汾
	 */
	public static final int SQL_DATABASE_VERSION							= 3;
	
	/**
	 * ���ݿ�Ĭ�������б��table����
	 */	
	public static final String SQL_DATABASE_DEFAULT_TABLE_NAME				= "tasks_default";
	
	/**
	 * ���ݿ����ǰ׺���������
	 */
	public static final String SQL_DATABASE_TABLE_NAME_FONT 				= "tasks_";

	/**
	 * ����--����ID
	 */
	public static final String SQL_TASK_COLUMN_ID = "_id";

	/**
	 * ����--��������
	 */
	public static final String SQL_TASK_COLUMN_NAME = "name";

	/**
	 * ����--�����Ƿ񼤻�(0--false 1--true)
	 */
	public static final String SQL_TASK_COLUMN_ENABLED = "enabled";

	/**
	 * ����--��������
	 */
	public static final String SQL_TASK_COLUMN_TYPE 						= "trigger_type";

	/**
	 * �洢�������Ͷ�Ӧ�Ĳ���;
	 * 0����һ�Σ�--{time};
	 * 1��ָ��ʱ�䳤���ظ��� --{time,period_type,value};
	 * 2(���ظ�) --{time,SUNDAY,MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY};
	 * 3(��ص�������ĳֵ)  --{percent};
	 * 4(��ص�������ĳֵ)  --{percent};
	 * 5(����¶ȵ���ĳֵ)  --{temp*10};
	 * 6(����¶ȸ���ĳֵ)  --{temp*10};
	 */
	public static final String SQL_TASK_COLUMN_TRIGGER_VALUES 				="trigger_values";

	/**
	 * ����--����
	 */
	public static final String SQL_TASK_COLUMN_EXCEPTIONS ="exceptions";

	/**
	 * ����--��Ϊ
	 */
	public static final String SQL_TASK_COLUMN_ACTIONS 						= "actions";

	/**
	 * ����--����ѡ��Ƿ��Զ�ɾ�����Ƿ��Զ��رյȣ�
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
     * ��ȡJson File ��Ӧ��Ӧ�õ�VersionCode
     */
    public static final String JSON_HEAD_VERSION_CODE="version_code";
}
