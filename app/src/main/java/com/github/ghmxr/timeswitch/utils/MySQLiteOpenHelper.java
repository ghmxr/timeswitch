package com.github.ghmxr.timeswitch.utils;

import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.SQLConsts;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper{

	private static MySQLiteOpenHelper helper;
	private Context context;
	
	private MySQLiteOpenHelper(Context context){
		super(context, SQLConsts.SQL_DATABASE_NAME,null, SQLConsts.SQL_DATABASE_VERSION);
		this.context=context.getApplicationContext();
	}

	public static MySQLiteOpenHelper getInstance(Context context){
		if(helper==null){
			helper=new MySQLiteOpenHelper(context.getApplicationContext());
		}
		return helper;
	}

	public static void clearCurrentInstance(){
		helper=null;
	}

	/**
     * 动态获取当前使用的数据库表名。
     * @param context 调用此方法的activity 或者 context
     * @return 当前使用的数据库的表名
     */
    public static String getCurrentTableName(Context context){
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
				+SQLConsts.SQL_TASK_COLUMN_SMS_SEND_MESSAGE+" text);";
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.i("MySqliteOpenHelper","oldVersion is "+oldVersion+" , newVersion is "+newVersion);
		if(newVersion==oldVersion) return;
		switch (oldVersion){
			default:break;
			case 0: case 1:{
				String sql_lookup_table_names="select name from "+ "sqlite_master"+" where type='table' order by name";
				Cursor cursor=db.rawQuery(sql_lookup_table_names,null);
				while (cursor.moveToNext()){
					String tablename=cursor.getString(0);
					if(tablename.contains(SQLConsts.SQL_DATABASE_TABLE_NAME_FONT)){
						try{
							db.execSQL("alter table "+tablename+" add column "+SQLConsts.SQL_TASK_COLUMN_URI_RING_NOTIFICATION+" text;");
						}catch (SQLiteException se){
							se.printStackTrace();
							LogUtil.putExceptionLog(context,se);
						}

						try{
							db.execSQL("alter table "+tablename+" add column "+SQLConsts.SQL_TASK_COLUMN_URI_RING_CALL +" text;");
						}catch (SQLiteException se){
							se.printStackTrace();
							LogUtil.putExceptionLog(context,se);
						}

						try{
							db.execSQL("alter table "+tablename+" add column "+SQLConsts.SQL_TASK_COLUMN_URI_WALLPAPER_DESKTOP+" text;");
						}catch (SQLiteException se){
							se.printStackTrace();
							LogUtil.putExceptionLog(context,se);
						}

						try{
							db.execSQL("alter table "+tablename+" add column "+SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_TITLE+" text;");
						}catch (SQLiteException se){
							se.printStackTrace();
							LogUtil.putExceptionLog(context,se);
						}

						try{
							db.execSQL("alter table "+tablename+" add column "+SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_MESSAGE+" text;");
						}catch (SQLiteException se){
							se.printStackTrace();
							LogUtil.putExceptionLog(context,se);
						}

						try{
							db.execSQL("alter table "+tablename+" add column "+SQLConsts.SQL_TASK_COLUMN_TOAST+" text;");
						}catch (SQLiteException se){
							se.printStackTrace();
							LogUtil.putExceptionLog(context,se);
						}

						try{
							db.execSQL("alter table "+tablename+" add column "+SQLConsts.SQL_TASK_COLUMN_SMS_SEND_PHONE_NUMBERS+" text;");
						}catch (SQLiteException se){
							se.printStackTrace();
							LogUtil.putExceptionLog(context,se);
						}

						try{
							db.execSQL("alter table "+tablename+" add column "+SQLConsts.SQL_TASK_COLUMN_SMS_SEND_MESSAGE+" text;");
						}catch (SQLiteException se){
							se.printStackTrace();
							LogUtil.putExceptionLog(context,se);
						}

					}
				}
				cursor.close();
			}

		}
	}

}
