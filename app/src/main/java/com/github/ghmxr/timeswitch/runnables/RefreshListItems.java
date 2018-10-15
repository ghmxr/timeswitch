package com.github.ghmxr.timeswitch.runnables;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.SQLConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.utils.MySQLiteOpenHelper;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.util.List;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class RefreshListItems implements Runnable {

    boolean isInterrupted;
    Context context;
    static final String TAG="Runnable_refresh_list";
    List<TaskItem> list;

    public RefreshListItems(Context context){
        this.isInterrupted=false;
        this.context=context;
        //Main.list=new ArrayList<TaskItem>();
        this.list= TimeSwitchService.list;
    }

    /**
     *��ȡ���ݿⲢˢ��������list
     */
    @Override
    public void run() {

        if(list!=null){
            for(int i=0;i<list.size();i++){
                if(isInterrupted) break;
                list.get(i).cancelTrigger();
            }
        }

        SQLiteDatabase database = MySQLiteOpenHelper.getInstance(context).getWritableDatabase();
        Cursor cursor=database.rawQuery("select * from "+ SQLConsts.getCurrentTableName(context),null);

        if(!isInterrupted) list.clear();

        while (cursor.moveToNext()) {
            if(isInterrupted){
                Log.i(TAG,"Refresh list items is interrupted!!");
                break;
            }
            TaskItem item = new TaskItem();
            try{
                item.id = cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ID));
                item.name = cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_NAME));
               // if(cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ENABLED))==0) item.isenabled=false;
                //else item.isenabled=true;
                item.isenabled=(cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ENABLED))==1);
                item.trigger_type = cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_TYPE));

                String[] trigger_values=ValueUtils.string2StringArray(cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES)));

               // Calendar calendar=Calendar.getInstance();
                if(item.trigger_type== PublicConsts.TRIGGER_TYPE_SINGLE){   //0����һ�Σ�--{trigger_value};
                    item.time=Long.parseLong(trigger_values[0]);
                }
                if(item.trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME){ //1��ָ��ʱ�䳤���ظ��� --{trigger_value,period_type,value};
                    item.time=Long.parseLong(trigger_values[0]);
                    item.interval_milliseconds=Long.parseLong(trigger_values[1]);
                }
                if(item.trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_WEEK){//2(���ظ�) --{trigger_value,SUNDAY,MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY};
                    item.time=Long.parseLong(trigger_values[0]);
                    for(int i=0;i<item.week_repeat.length;i++){
                        item.week_repeat[i]=(Long.parseLong(trigger_values[i+1])==1);
                    }
                }
                /*if(item.trigger_type==PublicConsts.TRIGGER_TYPE_SINGLE||item.trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME||item.trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_WEEK){  //��ʼ��item �е�hour �� minute
                    calendar.setTimeInMillis(item.trigger_value);
                    item.hour=calendar.get(Calendar.HOUR_OF_DAY);
                    item.minute=calendar.get(Calendar.MINUTE);
                } */
                if(item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE||item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE){   //3||4 (��ص������ڻ����ĳֵ)  --{percent};
                    item.battery_percentage = Integer.parseInt(trigger_values[0]);
                }
                if(item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE||item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE){
                    item.battery_temperature=Integer.parseInt(trigger_values[0]);
                }
                if(item.trigger_type==PublicConsts.TRIGGER_TYPE_RECEIVED_BROADTCAST){
                    item.selectedAction=String.valueOf(trigger_values[0]);
                }
                //exceptions ��Ӧ��Ԥ������>=���ݿ��ȡ���ص����鳤�ȣ��� read_exceptions.length<=item.exceptions.length
                String [] read_exceptions=ValueUtils.string2StringArray(cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_EXCEPTIONS)));
                //for(int i=0;i<read_exceptions.length;i++){
                //   item.exceptions[i]=read_exceptions[i];
                // }
                System.arraycopy(read_exceptions,0,item.exceptions,0,read_exceptions.length);
                //actions , read_exceptions.length<=item.actions.length
                String [] read_actions=ValueUtils.string2StringArray(cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ACTIONS)));
                //for(int i=0;i<read_actions.length;i++){
                //    item.actions[i]=read_actions[i];
                //}
                System.arraycopy(read_actions,0,item.actions,0,read_actions.length);
                item.uri_ring_notification=cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_URI_RING_NOTIFICATION));
                item.uri_ring_call =cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_URI_RING_CALL));
                item.uri_wallpaper_desktop=cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_URI_WALLPAPER_DESKTOP));
                item.notification_title=cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_TITLE));
                item.notification_message=cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_MESSAGE));
                item.toast=cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_TOAST));
                item.sms_address=cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_PHONE_NUMBERS));
                item.sms_message=cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_MESSAGE));
                //additions
                int read_additions[] =ValueUtils.string2intArray(cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ADDITIONS)));
                item.notify=(read_additions[PublicConsts.ADDITION_NOTIFY]==1);
                item.autodelete=(read_additions[PublicConsts.ADDITION_AUTO_DELETE]==1);
                item.autoclose=(read_additions[PublicConsts.ADDITION_AUTO_CLOSE]==1);

                /*
                item.actions = ValueUtils.string2intArray(cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ACTIONS)));
                item.trigger_value = cursor.getLong(cursor.getColumnIndex(SQLConsts.SQL_TASK_TIME));
                item.hour = cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_HOUR));
                item.minute = cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_MIN));

                item.repeat_time = cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_INTERVAL_CERTAIN));
                item.repeat_time_type = cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_INTERVAL_CERTAIN_TYPE));
                item.week_repeat = ValueUtils.string2booleanArray(cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_INTERVAL_WEEK)));  */

               // item.exception = ValueUtils.string2booleanArray(cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_EXCEPTIONS)));

               /* int notify=cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_NOTIFY));
                if(notify==1) item.notify=true; else item.notify=false;
                int autodelete =cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_AUTODEL));
                if(autodelete==1) item.autodelete=true; else item.autodelete=false;   */

            }catch(Exception e){
                e.printStackTrace();
            }

            if(item.isenabled) item.activateTrigger(context);
            list.add(item);
        }

        cursor.close();
        if(!isInterrupted) TimeSwitchService.sendEmptyMessage(TimeSwitchService.MESSAGE_REFRESH_TASKS_COMPLETE);
    }

    public void setInterrupted(){
        this.isInterrupted=true;
    }
}
