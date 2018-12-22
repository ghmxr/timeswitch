package com.github.ghmxr.timeswitch.runnables;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.activities.Triggers;
import com.github.ghmxr.timeswitch.adapters.MainListAdapter;
import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.SQLConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.services.AppLaunchingDetectionService;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.utils.MySQLiteOpenHelper;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.util.Calendar;
import java.util.List;

import static com.github.ghmxr.timeswitch.activities.Triggers.getWeekLoopDisplayValue;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class RefreshListItems implements Runnable {

    boolean isInterrupted;
    Context context;
    static final String TAG="Runnable_refresh_list";
    List<TaskItem> list=TimeSwitchService.list;

    public RefreshListItems(Context context){
        this.isInterrupted=false;
        this.context=context;
    }

    /**
     *读取数据库并刷新任务至list
     */
    @Override
    public void run() {
        synchronized (RefreshListItems.class){
            if(list!=null){
                for(int i=0;i<list.size();i++){
                    if(isInterrupted) break;
                    list.get(i).cancelTrigger();
                }
            }

            SQLiteDatabase database = MySQLiteOpenHelper.getInstance(context).getWritableDatabase();
            Cursor cursor=database.rawQuery("select * from "+ SQLConsts.getCurrentTableName(context),null);

            if(!isInterrupted) list.clear();
            boolean applaunching_service=false;
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
                    if(item.trigger_type== PublicConsts.TRIGGER_TYPE_SINGLE){   //0（仅一次）--{trigger_value};
                        item.time=Long.parseLong(trigger_values[0]);
                    }
                    if(item.trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME){ //1（指定时间长度重复） --{trigger_value,period_type,value};
                        item.time=Long.parseLong(trigger_values[0]);
                        item.interval_milliseconds=Long.parseLong(trigger_values[1]);
                    }
                    if(item.trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_WEEK){//2(周重复) --{trigger_value,SUNDAY,MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY};
                        item.time=Long.parseLong(trigger_values[0]);
                        for(int i=0;i<item.week_repeat.length;i++){
                            item.week_repeat[i]=(Long.parseLong(trigger_values[i+1])==1);
                        }
                    }
                /*if(item.trigger_type==PublicConsts.TRIGGER_TYPE_SINGLE||item.trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME||item.trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_WEEK){  //初始化item 中的hour 和 minute
                    calendar.setTimeInMillis(item.trigger_value);
                    item.hour=calendar.get(Calendar.HOUR_OF_DAY);
                    item.minute=calendar.get(Calendar.MINUTE);
                } */
                    if(item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE||item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE){   //3||4 (电池电量低于或高于某值)  --{percent};
                        item.battery_percentage = Integer.parseInt(trigger_values[0]);
                    }
                    if(item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE||item.trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE){
                        item.battery_temperature=Integer.parseInt(trigger_values[0]);
                    }
                    if(item.trigger_type==PublicConsts.TRIGGER_TYPE_RECEIVED_BROADCAST){
                        item.selectedAction=String.valueOf(trigger_values[0]);
                    }

                    if(item.trigger_type==PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED||item.trigger_type==PublicConsts.TRIGGER_TYPE_WIFI_DISCONNECTED){
                        if(trigger_values==null||trigger_values.length<1){
                            item.wifiIds="";
                        }else item.wifiIds=String.valueOf(trigger_values[0]);
                    }
                    if(item.trigger_type==PublicConsts.TRIGGER_TYPE_APP_LAUNCHED||item.trigger_type==PublicConsts.TRIGGER_TYPE_APP_CLOSED){
                        item.package_names=trigger_values;
                        if(item.isenabled) applaunching_service=true;
                    }
                    //exceptions ，应用预留长度>=数据库读取返回的数组长度，即 read_exceptions.length<=item.exceptions.length
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
                    String initial_additions[]=new String[PublicConsts.ADDITION_LENGTH];
                    for(int i=0;i<initial_additions.length;i++) initial_additions[i]=String.valueOf(-1);

                    String read_additions[] =ValueUtils.string2StringArray(cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ADDITIONS)));
                    System.arraycopy(read_additions,0,initial_additions,0,read_additions.length);

                    item.notify=(Integer.parseInt(initial_additions[PublicConsts.ADDITION_NOTIFY])==1);
                    item.autodelete=(Integer.parseInt(initial_additions[PublicConsts.ADDITION_AUTO_DELETE])==1);
                    item.autoclose=(Integer.parseInt(initial_additions[PublicConsts.ADDITION_AUTO_CLOSE])==1);
                    item.addition_isFolded=(Integer.parseInt(initial_additions[PublicConsts.ADDITION_TITLE_FOLDED_VALUE_LOCALE])>=0);
                    if(!initial_additions[PublicConsts.ADDITION_TITLE_COLOR_LOCALE].equals(String.valueOf(-1))){
                        item.addition_title_color=initial_additions[PublicConsts.ADDITION_TITLE_COLOR_LOCALE];
                    }
                    item.addition_exception_connector=initial_additions[PublicConsts.ADDITION_EXCEPTION_CONNECTOR_LOCALE];

                    //initial display values
                    switch (item.trigger_type){
                        default:break;
                        case PublicConsts.TRIGGER_TYPE_SINGLE: {
                            item.display_trigger_icon_res=R.drawable.icon_repeat_single;
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(item.time);
                            int month = calendar.get(Calendar.MONTH) + 1;
                            item.display_trigger=calendar.get(Calendar.YEAR)
                                    + "/" + ValueUtils.format(month) + "/" + ValueUtils.format(calendar.get(Calendar.DAY_OF_MONTH)) + "/" + ValueUtils.getDayOfWeek(item.time) + "/"
                                    + ValueUtils.format(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + ValueUtils.format(calendar.get(Calendar.MINUTE));
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME: {
                            item.display_trigger_icon_res=R.drawable.icon_repeat_percertaintime;
                            //refreshAllCertainTimeTaskItems();
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_LOOP_WEEK: {
                            item.display_trigger_icon_res=R.drawable.icon_repeat_weekloop;
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(item.time);
                            item.display_trigger=getWeekLoopDisplayValue(context, item.week_repeat, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                            //holder.trigger_value.setText();
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE: {
                            item.display_trigger_icon_res=R.drawable.icon_battery_high;
                            item.display_trigger=context.getResources().getString(R.string.more_than) + item.battery_percentage + "%";
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE: {
                            item.display_trigger_icon_res=R.drawable.icon_battery_low;
                            item.display_trigger=context.getResources().getString(R.string.less_than) + item.battery_percentage + "%";
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE: {
                            item.display_trigger_icon_res=R.drawable.icon_temperature;
                            item.display_trigger=context.getResources().getString(R.string.higher_than) + item.battery_temperature + "℃";
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE: {
                            item.display_trigger_icon_res=R.drawable.icon_temperature;
                            item.display_trigger=context.getResources().getString(R.string.lower_than) + item.battery_temperature + "℃";
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_RECEIVED_BROADCAST: {
                            item.display_trigger_icon_res=R.drawable.icon_broadcast;
                            item.display_trigger=item.selectedAction;
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED: {
                            item.display_trigger_icon_res=R.drawable.icon_wifi_connected;
                            //holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                            String ssidinfo = Triggers.getWifiConnectionDisplayValue(context, item.wifiIds);
                            //if(ssidinfo.length()>16) ssidinfo=ssidinfo.substring(0,16)+"...";
                            item.display_trigger=ssidinfo;
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_WIFI_DISCONNECTED: {
                            item.display_trigger_icon_res=R.drawable.icon_wifi_disconnected;
                            //holder.trigger_value.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                            String ssidinfo = Triggers.getWifiConnectionDisplayValue(context, item.wifiIds);
                            //if(ssidinfo.length()>16) ssidinfo=ssidinfo.substring(0,16)+"...";
                            item.display_trigger=ssidinfo;
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_APP_LAUNCHED: {
                            item.display_trigger_icon_res=R.drawable.icon_app_launch;
                            String names = Triggers.getAppNameDisplayValue(context, item.package_names);
                            //if(names.length()>16) names=names.substring(0,16);
                            item.display_trigger=names;
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_APP_CLOSED: {
                            item.display_trigger_icon_res=R.drawable.icon_app_stop;
                            String names = Triggers.getAppNameDisplayValue(context, item.package_names);
                            //if(names.length()>16) names=names.substring(0,16);
                            item.display_trigger=names;
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_SCREEN_ON: {
                            item.display_trigger_icon_res=R.drawable.icon_screen_on;
                            item.display_trigger=context.getResources().getString(R.string.activity_triggers_screen_on);
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_SCREEN_OFF: {
                            item.display_trigger_icon_res=R.drawable.icon_screen_off;
                            item.display_trigger=context.getResources().getString(R.string.activity_triggers_screen_off);
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_POWER_CONNECTED: {
                            item.display_trigger_icon_res=R.drawable.icon_power_connected;
                            item.display_trigger=context.getResources().getString(R.string.activity_triggers_power_connected);
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_POWER_DISCONNECTED: {
                            item.display_trigger_icon_res=R.drawable.icon_power_disconnected;
                            item.display_trigger=context.getResources().getString(R.string.activity_triggers_power_disconnected);
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_ON: {
                            item.display_trigger_icon_res=R.drawable.icon_wifi_on;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF: {
                            item.display_trigger_icon_res=R.drawable.icon_wifi_off;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON: {
                            item.display_trigger_icon_res=R.drawable.icon_bluetooth_on;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF: {
                            item.display_trigger_icon_res=R.drawable.icon_bluetooth_off;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF: {
                            item.display_trigger_icon_res=R.drawable.icon_ring_off;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE: {
                            item.display_trigger_icon_res=R.drawable.icon_ring_vibrate;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL: {
                            item.display_trigger_icon_res=R.drawable.icon_ring_normal;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON: {
                            item.display_trigger_icon_res=R.drawable.icon_airplanemode_on;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF: {
                            item.display_trigger_icon_res=R.drawable.icon_airplanemode_off;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED: {
                            item.display_trigger_icon_res=R.drawable.icon_ap_on;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED: {
                            item.display_trigger_icon_res=R.drawable.icon_ap_off;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_NET_ON: {
                            item.display_trigger_icon_res=R.drawable.icon_cellular_on;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_NET_OFF: {
                            item.display_trigger_icon_res=R.drawable.icon_cellular_off;
                            item.display_trigger=Triggers.getWidgetDisplayValue(context, item.trigger_type);
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_IN: {
                            item.display_trigger_icon_res=R.drawable.icon_headset;
                            item.display_trigger=context.getResources().getString(R.string.activity_trigger_headset_plug_in);
                        }
                        break;
                        case PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT: {
                            item.display_trigger_icon_res=R.drawable.icon_headset;
                            item.display_trigger=context.getResources().getString(R.string.activity_trigger_headset_plug_out);
                        }
                        break;

                    }
                    item.display_exception= MainListAdapter.getExceptionValue(context,item);
                    item.display_actions=MainListAdapter.getActionValue(context,item);
                    item.display_additions=MainListAdapter.getAdditionValue(context,item);
                }catch(Exception e){
                    e.printStackTrace();
                }

                if(item.isenabled) item.activateTrigger(context);
                list.add(item);
            }
            if(applaunching_service) AppLaunchingDetectionService.startService(context);
            else if(AppLaunchingDetectionService.queue.size()>0) AppLaunchingDetectionService.queue.getLast().stopSelf();
            cursor.close();
            if(!isInterrupted) TimeSwitchService.sendEmptyMessage(TimeSwitchService.MESSAGE_REFRESH_TASKS_COMPLETE);
        }
    }

    public void setInterrupted(){
        this.isInterrupted=true;
    }
}
