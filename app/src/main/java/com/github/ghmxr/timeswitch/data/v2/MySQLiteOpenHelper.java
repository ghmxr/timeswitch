package com.github.ghmxr.timeswitch.data.v2;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper{

	private Context context;
	//private static MySQLiteOpenHelper helper;
	
	private MySQLiteOpenHelper(Context context){
		super(context, SQLConsts.SQL_DATABASE_NAME,null, SQLConsts.SQL_DATABASE_VERSION);
		this.context=context;
	}

	/**
	 * get an instance
	 */
	public static MySQLiteOpenHelper getInstance(Context context){
		/*if(helper==null){
			synchronized (MySQLiteOpenHelper.class){
				if(helper==null){
					helper=new MySQLiteOpenHelper(context.getApplicationContext());
				}
			}
		}*/
		return new MySQLiteOpenHelper(context.getApplicationContext());
	}

	/**
     * 动态获取当前使用的数据库表名。
     * @param context 调用此方法的activity 或者 context
     * @return 当前使用的数据库的表名
     */
    public static String getCurrentTableName(@NonNull Context context){
         SharedPreferences settings = context.getSharedPreferences(PublicConsts.PREFERENCES_NAME, Activity.MODE_PRIVATE);
         return settings.getString(PublicConsts.PREFERENCES_CURRENT_TABLE_NAME, SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(getCreateTableSQLCommand(getCurrentTableName(context)));
	}

	public static String getCreateTableSQLCommand(String tableName){
		return "create table if not exists "+ tableName + " ("
				+SQLConsts.SQL_TASK_COLUMN_ID +" integer primary key autoincrement not null,"
				+SQLConsts.SQL_TASK_COLUMN_NAME +" text , "
				+SQLConsts.SQL_TASK_COLUMN_ENABLED +" integer not null default 1,"
				+SQLConsts.SQL_TASK_COLUMN_TYPE +"  integer not null default 0,"
				+SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES+" text ,"
				+SQLConsts.SQL_TASK_COLUMN_EXCEPTIONS +" text ,"
				+SQLConsts.SQL_TASK_COLUMN_ACTIONS +" text ,"
				+SQLConsts.SQL_TASK_COLUMN_ADDITIONS+" text ,"
				+SQLConsts.SQL_TASK_COLUMN_URI_RING_NOTIFICATION+" text ,"
				+SQLConsts.SQL_TASK_COLUMN_URI_RING_CALL +" text ,"
				+SQLConsts.SQL_TASK_COLUMN_URI_WALLPAPER_DESKTOP+" text ,"
				+SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_TITLE+" text ,"
				+SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_MESSAGE+" text ,"
				+SQLConsts.SQL_TASK_COLUMN_TOAST+" text ,"
				+SQLConsts.SQL_TASK_COLUMN_SMS_SEND_PHONE_NUMBERS+" text ,"
				+SQLConsts.SQL_TASK_COLUMN_SMS_SEND_MESSAGE+" text ,"
				+ SQLConsts.SQL_TASK_COLUMN_ORDER+" integer not null default 0);";
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.i("MySqliteOpenHelper","oldVersion is "+oldVersion+" , newVersion is "+newVersion);
		if(newVersion==oldVersion) return;
		switch (oldVersion){
			default:break;
			case 2:{   //添加order字段，并将关键字id值从0开始排列，id值初始等于order
				String sql_lookup_table_names="select name from "+ "sqlite_master"+" where type='table' order by name";
				Cursor cursor=db.rawQuery(sql_lookup_table_names,null);
				while (cursor.moveToNext()){
					String table_name=cursor.getString(0);
					if(table_name.contains(SQLConsts.SQL_DATABASE_TABLE_NAME_FONT)) {
						db.execSQL("alter table "+table_name +" add column "+SQLConsts.SQL_TASK_COLUMN_ORDER+" integer not null default 0");
						Cursor cursor1=db.rawQuery("select * from "+table_name,null);
						int order=0;
						while (cursor1.moveToNext()){
							final int id_this_row=cursor1.getInt(cursor1.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ID));
							ContentValues values=new ContentValues();
							if(id_this_row!=order)values.put(SQLConsts.SQL_TASK_COLUMN_ID,order);
							values.put(SQLConsts.SQL_TASK_COLUMN_ORDER,order++);
							db.update(table_name,values,SQLConsts.SQL_TASK_COLUMN_ID+"="+id_this_row,null);
						}
						cursor1.close();
					}
				}
				cursor.close();
			}

		}
	}

	/**
	 * 插入或者更新TaskItem到数据库表中
	 * @param activity 执行数据操作的activity(会向这个activity发送错误信息的SnackBar)
	 * @param taskitem 要插入或者更新的item，不能为null
	 * @param id 要更新的item的id，如果为null则会创建新行
	 * @return 插入的行数或者更新的行数，-1则表示有错误
	 */
	public static long insertOrUpdateRow(@NonNull Activity activity, @NonNull TaskItem taskitem, @Nullable Integer id){
		boolean isNull=true;
		for(int i=0;i<taskitem.actions.length;i++){//for(int i=0;i<actions.length;i++){
			if(i== ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_VOLUME_LOCALE){
				String volume_values[]=taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_VOLUME_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL);
				if(Integer.parseInt(volume_values[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_RING_LOCALE])>=0||Integer.parseInt(volume_values[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_MEDIA_LOCALE])>=0
						||Integer.parseInt(volume_values[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_NOTIFICATION_LOCALE])>=0||Integer.parseInt(volume_values[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_ALARM_LOCALE])>=0)
					isNull=false;
			}
			else if(i== ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_SELECTION_LOCALE){
				if(Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_SELECTION_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL)[ActionConsts.ActionSecondLevelLocaleConsts.RING_SELECTION_NOTIFICATION_TYPE_LOCALE])>=0
						||Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_SELECTION_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL)[ActionConsts.ActionSecondLevelLocaleConsts.RING_SELECTION_CALL_TYPE_LOCALE])>=0) isNull=false;
			}
			else if(i== ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SET_WALL_PAPER_LOCALE){
				if(Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SET_WALL_PAPER_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[0])>=0) isNull=false;
			}
			else if(i== ActionConsts.ActionFirstLevelLocaleConsts.ACTION_VIBRATE_LOCALE){
				if(Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_VIBRATE_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[ActionConsts.ActionSecondLevelLocaleConsts.VIBRATE_FREQUENCY_LOCALE])>0) isNull=false;
			}
			else if(i== ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NOTIFICATION_LOCALE){
				if(Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NOTIFICATION_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[ActionConsts.ActionSecondLevelLocaleConsts.NOTIFICATION_TYPE_LOCALE])!= ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_UNSELECTED) isNull=false;
			}
			else if(i== ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SMS_LOCALE){
				if(Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SMS_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[0])>=0) isNull=false;
			}
			else if(i== ActionConsts.ActionFirstLevelLocaleConsts.ACTION_TOAST_LOCALE){
				if(Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_TOAST_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[ActionConsts.ActionSecondLevelLocaleConsts.TOAST_TYPE_LOCALE])>=0) isNull=false;
			}
			else if(i== ActionConsts.ActionFirstLevelLocaleConsts.ACTION_ENABLE_TASKS_LOCALE){
				if(Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_ENABLE_TASKS_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL)[0])>=0) isNull=false;
			}
			else if(i== ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DISABLE_TASKS_LOCALE){
				if(Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DISABLE_TASKS_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL)[0])>=0) isNull=false;
			}else if(i== ActionConsts.ActionFirstLevelLocaleConsts.ACTION_LAUNCH_APP_PACKAGES){
				if(!taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_LAUNCH_APP_PACKAGES].equals(String.valueOf(-1))) isNull=false;
			}else if(i== ActionConsts.ActionFirstLevelLocaleConsts.ACTION_STOP_APP_PACKAGES){
				if(!taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_STOP_APP_PACKAGES].equals(String.valueOf(-1))) isNull=false;
			}
			else if(Integer.parseInt(taskitem.actions[i])!= ActionConsts.ActionValueConsts.ACTION_UNSELECTED) isNull=false;
		}
		if(isNull) {
			Snackbar.make(activity.findViewById(android.R.id.content),activity.getResources().getString(R.string.activity_taskgui_toast_no_actions),Snackbar.LENGTH_SHORT).show();
			return -1;
		}
		if(taskitem.trigger_type == TriggerTypeConsts.TRIGGER_TYPE_SINGLE&&taskitem.time<System.currentTimeMillis()){
			Snackbar.make(activity.findViewById(android.R.id.content),activity.getResources().getString(R.string.activity_taskgui_toast_time_invalid),Snackbar.LENGTH_SHORT).show();
			return -1;
		}
		SQLiteDatabase db= MySQLiteOpenHelper.getInstance(activity).getWritableDatabase();
		ContentValues values=new ContentValues();

		values.put(SQLConsts.SQL_TASK_COLUMN_NAME,taskitem.name);

		String[] triggerValues=new String[1];
		if(taskitem.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_SINGLE){//0（仅一次）--{time};
			triggerValues=new String[1];
			triggerValues[0]=String.valueOf(taskitem.time);
		}
		if(taskitem.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME){//1（指定时间长度重复） --{time,period};
			triggerValues=new String[2];
			triggerValues[0]=String.valueOf(System.currentTimeMillis());
			triggerValues[1]=String.valueOf(taskitem.interval_milliseconds);
		}
		if(taskitem.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK){//2(周重复) --{time,SUNDAY,MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY};
			triggerValues=new String[8];
			triggerValues[0]=String.valueOf(taskitem.time);
			for(int i=1;i<triggerValues.length;i++){
				triggerValues[i]=String.valueOf(taskitem.week_repeat[i-1]?1:0);
			}
		}
		if(taskitem.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE||taskitem.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE){ //3,4(电池电量低于/高于某值)  --{percent}
			triggerValues=new String[1];
			triggerValues[0]=String.valueOf(taskitem.battery_percentage);
		}
		if(taskitem.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE||taskitem.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE){  //5,6(电池温度低于/高于某值) --{temperature}
			triggerValues=new String[1];
			triggerValues[0]=String.valueOf(taskitem.battery_temperature);
		}
		if(taskitem.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_RECEIVED_BROADCAST){
			triggerValues=new String[1];
			triggerValues[0]=String.valueOf(taskitem.selectedAction);
		}
		if(taskitem.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_WIFI_CONNECTED||taskitem.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_WIFI_DISCONNECTED){
			triggerValues=new String[1];
			triggerValues[0]=String.valueOf(taskitem.wifiIds);
		}
		if(taskitem.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_APP_LAUNCHED||taskitem.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_APP_CLOSED){
			triggerValues=taskitem.package_names;
			if(triggerValues==null||triggerValues.length==0) triggerValues=new String[1];
		}

		values.put(SQLConsts.SQL_TASK_COLUMN_ENABLED,taskitem.isenabled?1:0);
		values.put(SQLConsts.SQL_TASK_COLUMN_TYPE,taskitem.trigger_type);
		values.put(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES, ValueUtils.stringArray2String(triggerValues));
		values.put(SQLConsts.SQL_TASK_COLUMN_EXCEPTIONS,ValueUtils.stringArray2String(taskitem.exceptions));
		values.put(SQLConsts.SQL_TASK_COLUMN_ACTIONS,ValueUtils.stringArray2String(taskitem.actions));
		values.put(SQLConsts.SQL_TASK_COLUMN_URI_RING_NOTIFICATION,taskitem.uri_ring_notification);
		values.put(SQLConsts.SQL_TASK_COLUMN_URI_RING_CALL,taskitem.uri_ring_call);
		values.put(SQLConsts.SQL_TASK_COLUMN_URI_WALLPAPER_DESKTOP,taskitem.uri_wallpaper_desktop);
		values.put(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_TITLE,taskitem.notification_title);
		values.put(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_MESSAGE,taskitem.notification_message);
		values.put(SQLConsts.SQL_TASK_COLUMN_TOAST,taskitem.toast);
		values.put(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_PHONE_NUMBERS,taskitem.sms_address);
		values.put(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_MESSAGE,taskitem.sms_message);
		String [] additions=new String[AdditionConsts.ADDITION_LENGTH];
		for(int i=0;i<additions.length;i++) additions[i]=String.valueOf(-1);
		additions[AdditionConsts.ADDITION_NOTIFY]=taskitem.notify?String.valueOf(1):String.valueOf(0);
		additions[AdditionConsts.ADDITION_AUTO_DELETE]=taskitem.autodelete?String.valueOf(1):String.valueOf(0);
		additions[AdditionConsts.ADDITION_AUTO_CLOSE]=taskitem.autoclose?String.valueOf(1):String.valueOf(0);
		additions[AdditionConsts.ADDITION_TITLE_COLOR_LOCALE]=taskitem.addition_title_color;
		additions[AdditionConsts.ADDITION_EXCEPTION_CONNECTOR_LOCALE]=taskitem.addition_exception_connector;
		additions[AdditionConsts.ADDITION_TITLE_FOLDED_VALUE_LOCALE]=taskitem.addition_isFolded?String.valueOf(0):String.valueOf(-1);
		values.put(SQLConsts.SQL_TASK_COLUMN_ADDITIONS,ValueUtils.stringArray2String(additions));
		long result;
		if(id==null) {
			try{
				int id_insert=getInsertId(db,getCurrentTableName(activity));
				values.put(SQLConsts.SQL_TASK_COLUMN_ID,id_insert);
				values.put(SQLConsts.SQL_TASK_COLUMN_ORDER,getNewInsertRowTaskOrder(db,MySQLiteOpenHelper.getCurrentTableName(activity)));
			}catch (Exception e){
				e.printStackTrace();
				Snackbar.make(activity.findViewById(android.R.id.content),e.toString(),Snackbar.LENGTH_SHORT).show();
				return -1;
			}
			result= db.insert(MySQLiteOpenHelper.getCurrentTableName(activity),null,values);
		}
		else{
			Log.d("UPDATE","id is "+id);
			result= db.update(MySQLiteOpenHelper.getCurrentTableName(activity),values,SQLConsts.SQL_TASK_COLUMN_ID +"="+id,null);
		}
		db.close();
		return result;
	}

	public static int getInsertId(SQLiteDatabase db,String table_name) throws Exception{
		Cursor cursor=db.rawQuery("select * from "+table_name,null);
		int id_not_used=0;
		boolean look_flag=true;
		while (look_flag){
			boolean need_next_loop=false;
			while (cursor.moveToNext()){
				if(cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ID))==id_not_used) {
					id_not_used++;
					if(id_not_used>=Integer.MAX_VALUE) throw new Exception("The task num has reached the integer max value");
					need_next_loop=true;
					cursor.moveToFirst();
					break;
				}
			}
			if(!need_next_loop) look_flag=false;
		}
		cursor.close();
		Log.d("InsertID","The id get can be used is "+id_not_used);
		return id_not_used;
	}

	private static int getNewInsertRowTaskOrder(SQLiteDatabase database,String table_name){
		Cursor cursor=database.rawQuery("select * from "+table_name,null);
		if(cursor.getCount()==0) {
			cursor.close();
			return 0;
		}
		int task_order_max=0;
		while (cursor.moveToNext()){
			int task_order_this_row=cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ORDER));
			if(task_order_this_row>task_order_max) task_order_max=task_order_this_row;
		}
		cursor.close();
		return task_order_max+1;
	}

	/**
	 * 删除指定表的指定行
	 * @param db 数据库实例
	 * @param table_name 要操作的数据表
	 * @param id 要删除的行
	 */
	public static void deleteRow(final SQLiteDatabase db,final String table_name,final int id) {
		Cursor cursor_looked=db.rawQuery("select * from "+table_name+" where "+SQLConsts.SQL_TASK_COLUMN_ID+" = "+id,null);
		cursor_looked.moveToNext();
		int order_of_delete_row=cursor_looked.getInt(cursor_looked.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ORDER));
		cursor_looked.close();

		//do delete the row
		db.delete(table_name,SQLConsts.SQL_TASK_COLUMN_ID+" = "+id,null);

		Cursor cursor=db.rawQuery("select * from "+table_name+" where "+SQLConsts.SQL_TASK_COLUMN_ORDER+" >= "+order_of_delete_row,null);
		while (cursor.moveToNext()){
			int id_this_row=cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ID));
			int order_this_row=cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ORDER));
			ContentValues contentValues=new ContentValues();
			contentValues.put(SQLConsts.SQL_TASK_COLUMN_ORDER,order_this_row-1);
			db.update(table_name,contentValues,SQLConsts.SQL_TASK_COLUMN_ID+" = "+id_this_row,null);
		}
		cursor.close();
	}
	public static class SqlTableItem{
		public String table_name="";
		public String table_display_name="";
		public int task_num=0;
	}

	/**
	 * 将一个数据表写成一个json文件到path，此方法为耗时操作
	 * @param table 要导出的表名
	 * @param path 要写入的path（须包含文件名）
	 * @throws Exception 运行过程中可能会抛出的异常
	 */
	public static void saveTable2File(Context context, String table,String path) throws Exception{
		SQLiteDatabase database=MySQLiteOpenHelper.getInstance(context).getWritableDatabase();
		JSONObject head=new JSONObject();
		head.put(SQLConsts.JSON_HEAD_VERSION_CODE,context.getPackageManager().getPackageInfo(context.getApplicationInfo().packageName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT).versionCode);
		JSONArray jsonArray=new JSONArray();
		Cursor cursor=database.rawQuery("select * from "+table+" ;",null);
		while (cursor.moveToNext()){
			JSONObject jsonObject=new JSONObject();
			jsonObject.put(SQLConsts.SQL_TASK_COLUMN_ID,cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ID)));
			jsonObject.put(SQLConsts.SQL_TASK_COLUMN_NAME,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_NAME)));
			jsonObject.put(SQLConsts.SQL_TASK_COLUMN_ENABLED,cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ENABLED)));
			jsonObject.put(SQLConsts.SQL_TASK_COLUMN_TYPE,cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_TYPE)));
			jsonObject.put(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES)));
			jsonObject.put(SQLConsts.SQL_TASK_COLUMN_EXCEPTIONS,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_EXCEPTIONS)));
			jsonObject.put(SQLConsts.SQL_TASK_COLUMN_ACTIONS,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ACTIONS)));
			jsonObject.put(SQLConsts.SQL_TASK_COLUMN_URI_RING_NOTIFICATION,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_URI_RING_NOTIFICATION)));
			jsonObject.put(SQLConsts.SQL_TASK_COLUMN_URI_RING_CALL,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_URI_RING_CALL)));
			jsonObject.put(SQLConsts.SQL_TASK_COLUMN_URI_WALLPAPER_DESKTOP,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_URI_WALLPAPER_DESKTOP)));
			jsonObject.put(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_TITLE,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_TITLE)));
			jsonObject.put(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_MESSAGE,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_MESSAGE)));
			jsonObject.put(SQLConsts.SQL_TASK_COLUMN_TOAST,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_TOAST)));
			jsonObject.put(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_PHONE_NUMBERS,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_PHONE_NUMBERS)));
			jsonObject.put(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_MESSAGE,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_MESSAGE)));
			jsonObject.put(SQLConsts.SQL_TASK_COLUMN_ADDITIONS,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ADDITIONS)));
			jsonObject.put(SQLConsts.SQL_TASK_COLUMN_ORDER,cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ID)));
			jsonArray.put(jsonObject);
		}
		cursor.close();
		OutputStream out=new FileOutputStream(new File(path));
		Writer writer=new OutputStreamWriter(out);
		writer.write(head.toString()+"\n"+jsonArray.toString());
		writer.flush();
		writer.close();
	}

	/**
	 * 读取数据库所有表的信息并通过SqlTableItem的list集返回回来
	 * @param context context
	 * @return 表的SqlTableItem的list
	 */
	public static @NonNull List<SqlTableItem> getTableListFromDatabase(Context context){
		try{
			List<SqlTableItem> list=new ArrayList<>();
			SQLiteDatabase database= MySQLiteOpenHelper.getInstance(context).getWritableDatabase();

			SqlTableItem default_item=new SqlTableItem();
			default_item.table_name=SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME;
			default_item.table_display_name=context.getResources().getString(R.string.word_default);
			Cursor cursor_default=database.rawQuery("select * from "+SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME,null);
			default_item.task_num=cursor_default.getCount();
			cursor_default.close();
			list.add(default_item);

			SharedPreferences settings=context.getSharedPreferences(PublicConsts.PREFERENCES_NAME, Activity.MODE_PRIVATE);
			final String sql="select name from "+ "sqlite_master"+" where type='table' order by name";
			Cursor cursor=database.rawQuery(sql,null);
			while (cursor.moveToNext()){
				SqlTableItem item=new SqlTableItem();
				item.table_name=cursor.getString(0);
				item.table_display_name=settings.getString(item.table_name,"");
				if((item.table_name.contains(SQLConsts.SQL_DATABASE_TABLE_NAME_FONT))&&!item.table_name.equals(SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME)) {
					Cursor insideCursor=database.rawQuery("select * from "+item.table_name+"",null);
					item.task_num=insideCursor.getCount();
					insideCursor.close();
					list.add(item);
				}
			}
			cursor.close();
			return list;
		}catch (Exception e){e.printStackTrace();}
		return new ArrayList<>();
	}

	/**
	 * 删除库中的指定表
	 * @param table_name 要删除的表的名称
	 */
	public static void deleteTable(Context context,final String table_name){
		SharedPreferences settings=context.getSharedPreferences(PublicConsts.PREFERENCES_NAME,Activity.MODE_PRIVATE);
		final SharedPreferences.Editor editor=settings.edit();
		if(settings.getString(PublicConsts.PREFERENCES_CURRENT_TABLE_NAME,SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME).equals(table_name)){
			editor.putString(PublicConsts.PREFERENCES_CURRENT_TABLE_NAME,SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME);
			editor.apply();
		}
		SQLiteDatabase database=getInstance(context).getWritableDatabase();
		if(!table_name.equals(SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME)) {
			database.execSQL("drop table "+table_name+" ;");
			editor.remove(table_name);
			editor.apply();
		}
	}

	/**
	 * 插入一个新的表
	 * @param display_name 这个表要显示的名称
	 */
	public static void addTable(Context context,String display_name){
		final String table_name=SQLConsts.SQL_DATABASE_TABLE_NAME_FONT+ getIdForNewTable(context);
		SQLiteDatabase database=MySQLiteOpenHelper.getInstance(context).getWritableDatabase();
		database.execSQL(MySQLiteOpenHelper.getCreateTableSQLCommand(table_name));
		SharedPreferences settings=context.getSharedPreferences(PublicConsts.PREFERENCES_NAME,Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor=settings.edit();
		editor.putString(table_name,display_name);
		editor.apply();
	}

	/**
	 * 从一个文件读取并将内容导入至一个新的table
	 * @param context context
	 * @param file 指定要读取的文件
	 * @throws Exception 可能抛出的异常
	 */
	public static void readFile2Table(Context context,File file) throws Exception{
		SQLiteDatabase database=MySQLiteOpenHelper.getInstance(context).getWritableDatabase();
		SharedPreferences.Editor editor=context.getSharedPreferences(PublicConsts.PREFERENCES_NAME,Activity.MODE_PRIVATE).edit();
		StringBuilder builder=new StringBuilder("");
		InputStream in=new FileInputStream(file);
		BufferedReader reader=new BufferedReader(new InputStreamReader(in));
		String line;
		while((line=reader.readLine())!=null){
			builder.append(line);
		}
		JSONTokener jsonTokener=new JSONTokener(builder.toString());
		Object object=jsonTokener.nextValue();
		if(object instanceof JSONObject){
			int version_code_read=((JSONObject) object).getInt(SQLConsts.JSON_HEAD_VERSION_CODE);
			int version_current=context.getPackageManager().getPackageInfo(context.getPackageName(),PackageManager.COMPONENT_ENABLED_STATE_DEFAULT).versionCode;
			if(version_code_read>version_current){
				throw new Exception("The target file "+file.getName()+" was generated by a new version of this application:(the current application version is :"
						+version_current
						+", the target file was generated by the newer version "+version_code_read+" of this application)");
			}
			object=jsonTokener.nextValue();
		}
		JSONArray jsonarray = (JSONArray) object;
		String newTableName=SQLConsts.SQL_DATABASE_TABLE_NAME_FONT+ getIdForNewTable(context);
		database.execSQL(MySQLiteOpenHelper.getCreateTableSQLCommand(newTableName));
		String display_name=file.getName().substring(0,file.getName().lastIndexOf("."));
		editor.putString(newTableName,display_name);
		editor.apply();
		for(int j=0;j<jsonarray.length();j++){
			JSONObject jsonObject=(JSONObject)jsonarray.get(j);
			ContentValues contentValues=new ContentValues();
			contentValues.put(SQLConsts.SQL_TASK_COLUMN_ID,jsonObject.getInt(SQLConsts.SQL_TASK_COLUMN_ID));
			contentValues.put(SQLConsts.SQL_TASK_COLUMN_NAME,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_NAME));
			contentValues.put(SQLConsts.SQL_TASK_COLUMN_ENABLED,jsonObject.getInt(SQLConsts.SQL_TASK_COLUMN_ENABLED));
			contentValues.put(SQLConsts.SQL_TASK_COLUMN_TYPE,jsonObject.getInt(SQLConsts.SQL_TASK_COLUMN_TYPE));
			contentValues.put(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES));
			contentValues.put(SQLConsts.SQL_TASK_COLUMN_EXCEPTIONS,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_EXCEPTIONS));
			contentValues.put(SQLConsts.SQL_TASK_COLUMN_ACTIONS,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_ACTIONS));
			contentValues.put(SQLConsts.SQL_TASK_COLUMN_URI_RING_NOTIFICATION,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_URI_RING_NOTIFICATION));
			contentValues.put(SQLConsts.SQL_TASK_COLUMN_URI_RING_CALL,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_URI_RING_CALL));
			contentValues.put(SQLConsts.SQL_TASK_COLUMN_URI_WALLPAPER_DESKTOP,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_URI_WALLPAPER_DESKTOP));
			contentValues.put(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_TITLE,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_TITLE));
			contentValues.put(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_MESSAGE,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_MESSAGE));
			contentValues.put(SQLConsts.SQL_TASK_COLUMN_TOAST,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_TOAST));
			contentValues.put(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_PHONE_NUMBERS,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_PHONE_NUMBERS));
			contentValues.put(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_MESSAGE,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_MESSAGE));
			contentValues.put(SQLConsts.SQL_TASK_COLUMN_ADDITIONS,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_ADDITIONS));
			contentValues.put(SQLConsts.SQL_TASK_COLUMN_ORDER,j);
			try{
				contentValues.put(SQLConsts.SQL_TASK_COLUMN_ORDER,jsonObject.getInt(SQLConsts.SQL_TASK_COLUMN_ORDER));
			}catch (JSONException e){
				e.printStackTrace();
				Log.e("TaskOrder","May be this Json Array does not contain the order value");
			}
			database.insert(newTableName,null,contentValues);
		}
	}

	/**
	 * 遍历数据库中所有的表并返回一个可用的最小的id值来作为表名使用
	 * @param context context
	 * @return 最小的可用的id值
	 */
	public static int getIdForNewTable(Context context){
		List<SqlTableItem> list=getTableListFromDatabase(context);
		int selectedID;
		for (selectedID=0;selectedID<list.size();selectedID++){
			boolean ifcontains=false;
			for(int h=0;h<list.size();h++){
				if(list.get(h).table_name.equals(SQLConsts.SQL_DATABASE_TABLE_NAME_FONT+selectedID)){
					ifcontains=true;
					break;
				}
			}
			if(!ifcontains) break;
		}
		return selectedID;
	}

}
