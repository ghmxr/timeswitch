package com.github.ghmxr.timeswitch.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.adapters.AppListAdapter;
import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.receivers.NetworkReceiver;
import com.github.ghmxr.timeswitch.ui.BottomDialogForBattery;
import com.github.ghmxr.timeswitch.ui.BottomDialogForInterval;
import com.github.ghmxr.timeswitch.ui.CustomTimePicker;
import com.github.ghmxr.timeswitch.utils.LogUtil;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

public class Triggers extends BaseActivity implements View.OnClickListener,TimePicker.OnTimeChangedListener{
    public static final String EXTRA_TRIGGER_TYPE="trigger_type";
    public static final String EXTRA_TRIGGER_VALUES="trigger_values";

    private static final int MESSAGE_GET_SSID_COMPLETE=0x00001;
    private static final int MESSAGE_GET_APPLIST_COMPLETE_OPEN =0x00002;
    private static final int MESSAGE_GET_APPLIST_COMPLETE_CLOSE=0x00003;

    private int trigger_type=0;
   // private long time=0;
    private boolean[] week_repeat=new boolean[]{true,true,true,true,true,true,true};
    private long interval=60*60*1000;
    private int battery_percentage=50,battery_temperature=35;
    private String wifi_ssidinfo ="";
    private String [] package_names=new String[0];
    private String broadcast_intent_action="android.intent.ANSWER";
    /**
     * @deprecated
     */
    CustomTimePicker timePicker;
    Calendar calendar;

    private String checkString="";
    private long first_clicked=0;

    private AlertDialog dialog_appinfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_triggers);
        Toolbar toolbar=findViewById(R.id.triggers_toolbar);
        setSupportActionBar(toolbar);

        try{getSupportActionBar().setDisplayHomeAsUpEnabled(true);}catch (Exception e){e.printStackTrace();}
        setToolBarAndStatusBarColor(toolbar,getIntent().getStringExtra(EXTRA_TITLE_COLOR));
        timePicker=findViewById(R.id.trigger_timepicker);
        timePicker.setIs24HourView(true);

        calendar= Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis()+10*60*1000);
        calendar.set(Calendar.SECOND,0);

        findViewById(R.id.trigger_single).setOnClickListener(this);
        findViewById(R.id.trigger_percertaintime).setOnClickListener(this);
        findViewById(R.id.trigger_weekloop).setOnClickListener(this);
        findViewById(R.id.trigger_battery_percentage).setOnClickListener(this);
        findViewById(R.id.trigger_battery_temperature).setOnClickListener(this);
        findViewById(R.id.trigger_wifi_connected).setOnClickListener(this);
        findViewById(R.id.trigger_wifi_disconnected).setOnClickListener(this);
        findViewById(R.id.trigger_widget_changed).setOnClickListener(this);
        findViewById(R.id.trigger_received_broadcast).setOnClickListener(this);
        findViewById(R.id.trigger_screen_on).setOnClickListener(this);
        findViewById(R.id.trigger_screen_off).setOnClickListener(this);
        findViewById(R.id.trigger_power_connected).setOnClickListener(this);
        findViewById(R.id.trigger_power_disconnected).setOnClickListener(this);
        findViewById(R.id.trigger_app_opened).setOnClickListener(this);
        findViewById(R.id.trigger_app_closed).setOnClickListener(this);
        findViewById(R.id.trigger_headset).setOnClickListener(this);
        //initialize the values
        try{
            trigger_type=getIntent().getIntExtra(EXTRA_TRIGGER_TYPE,0);
            String trigger_values[]=getIntent().getStringArrayExtra(EXTRA_TRIGGER_VALUES);
            switch(trigger_type){
                default:break;
                case PublicConsts.TRIGGER_TYPE_SINGLE:{
                    //time=Long.parseLong(trigger_values[0]);
                    try{
                        calendar.setTimeInMillis(Long.parseLong(trigger_values[0]));
                        calendar.set(Calendar.SECOND,0);
                    }catch (Exception e){
                        LogUtil.putExceptionLog(this,e);
                        e.printStackTrace();
                    }
                }
                break;
                case PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME:{
                    //time=Long.parseLong(trigger_values[0]);
                    interval=Long.parseLong(trigger_values[0]);
                }
                break;
                case PublicConsts.TRIGGER_TYPE_LOOP_WEEK:{
                    //time=Long.parseLong(trigger_values[0]);
                    try{
                        calendar.setTimeInMillis(Long.parseLong(trigger_values[0]));
                        calendar.set(Calendar.SECOND,0);
                        for(int i=1;i<trigger_values.length;i++){
                            week_repeat[i-1]=Integer.parseInt(trigger_values[i])==1;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        LogUtil.putExceptionLog(this,e);
                    }

                }
                break;
                case PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE: case PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE:{
                    battery_temperature=Integer.parseInt(trigger_values[0]);
                }
                break;
                case PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE: case PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE:{
                    battery_percentage=Integer.parseInt(trigger_values[0]);
                }
                break;

                case PublicConsts.TRIGGER_TYPE_RECEIVED_BROADCAST:{
                    broadcast_intent_action=String.valueOf(trigger_values[0]);
                }
                break;

                case PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED: case PublicConsts.TRIGGER_TYPE_WIFI_DISCONNECTED:{
                    wifi_ssidinfo=String.valueOf(trigger_values[0]);
                    //Log.d("wifi ssids" ,wifi_ssidinfo);
                }
                break;
                case PublicConsts.TRIGGER_TYPE_APP_LAUNCHED: case PublicConsts.TRIGGER_TYPE_APP_CLOSED:{
                    package_names=trigger_values;
                }
                break;

            }
        }catch (Exception e){
            LogUtil.putExceptionLog(this,e);
            e.printStackTrace();
        }

        checkString=toCheckString();

        //set the views
        if(Build.VERSION.SDK_INT<23){
            timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        }else{
            timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            timePicker.setMinute(calendar.get(Calendar.MINUTE));
        }
        timePicker.setOnTimeChangedListener(this);

        activateTriggerType(trigger_type);
    }

    public String toCheckString() {
        return "Triggers{" +
                "trigger_type=" + trigger_type +
                ", week_repeat=" + Arrays.toString(week_repeat) +
                ", interval=" + interval +
                ", battery_percentage=" + battery_percentage +
                ", battery_temperature=" + battery_temperature +
                ", wifi_ssidinfo='" + wifi_ssidinfo + '\'' +
                ", package_names='" + Arrays.toString(package_names) + '\'' +
                ", broadcast_intent_action='" + broadcast_intent_action + '\'' +
                ", calendar=" + calendar.getTimeInMillis() +
                ", wifi_ssidinfo=" + wifi_ssidinfo +
                '}';
    }

    private void activateTriggerType(int type){
        trigger_type=type;
        refreshTriggerDisplayValues(type);
        switch (type){
            default:break;
            case PublicConsts.TRIGGER_TYPE_SINGLE:{
                ((TextView)findViewById(R.id.trigger_single_value)).setText(getSingleTimeDisplayValue(this,calendar.getTimeInMillis()));
                //timePicker.setVisibility(View.VISIBLE);
            }
            break;
            case PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME:{
                ((TextView)findViewById(R.id.trigger_percertaintime_value)).setText(getCertainLoopTimeDisplayValue(this,interval));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case PublicConsts.TRIGGER_TYPE_LOOP_WEEK:{
                ((TextView)findViewById(R.id.trigger_weekloop_value)).setText(getWeekLoopDisplayValue(this,week_repeat,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE)));
                //timePicker.setVisibility(View.VISIBLE);
            }
            break;
            case PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE: case PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE:{
                ((TextView)findViewById(R.id.trigger_battery_temperature_value)).setText(getBatteryTemperatureDisplayValue(this,type,battery_temperature));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE: case PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE:{
                ((TextView)findViewById(R.id.trigger_battery_percentage_value)).setText(getBatteryPercentageDisplayValue(this,type,battery_percentage));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case PublicConsts.TRIGGER_TYPE_RECEIVED_BROADCAST:{
                ((TextView)findViewById(R.id.trigger_received_broadcast_value)).setText(getBroadcastDisplayValue(broadcast_intent_action));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED:{
                ((TextView)findViewById(R.id.trigger_wifi_connected_value)).setText(getWifiConnectionDisplayValue(this,wifi_ssidinfo));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case PublicConsts.TRIGGER_TYPE_WIFI_DISCONNECTED:{
                ((TextView)findViewById(R.id.trigger_wifi_disconnected_value)).setText(getWifiConnectionDisplayValue(this,wifi_ssidinfo));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case PublicConsts.TRIGGER_TYPE_APP_LAUNCHED: {
                ((TextView)findViewById(R.id.trigger_app_opened_value)).setText(getAppNameDisplayValue(this,package_names));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case PublicConsts.TRIGGER_TYPE_APP_CLOSED:{
                ((TextView)findViewById(R.id.trigger_app_closed_value)).setText(getAppNameDisplayValue(this,package_names));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_IN:{
                ((TextView)findViewById(R.id.trigger_headset_value)).setText(getResources().getString(R.string.activity_trigger_headset_plug_in));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT:{
                ((TextView)findViewById(R.id.trigger_headset_value)).setText(getResources().getString(R.string.activity_trigger_headset_plug_out));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case PublicConsts.TRIGGER_TYPE_SCREEN_ON:{
                ((TextView)findViewById(R.id.trigger_screen_on_value)).setText(getResources().getString(R.string.activity_triggers_screen_on));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case PublicConsts.TRIGGER_TYPE_SCREEN_OFF:{
                ((TextView)findViewById(R.id.trigger_screen_on_value)).setText(getResources().getString(R.string.activity_triggers_screen_off));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case PublicConsts.TRIGGER_TYPE_POWER_CONNECTED:{
                ((TextView)findViewById(R.id.trigger_power_connected_value)).setText(getResources().getString(R.string.activity_triggers_power_connected));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case PublicConsts.TRIGGER_TYPE_POWER_DISCONNECTED:{
                ((TextView)findViewById(R.id.trigger_power_connected_value)).setText(getResources().getString(R.string.activity_triggers_power_disconnected));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_ON: case PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF: case PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON:
            case PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF: case PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF: case PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE:
            case PublicConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL: case PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON: case PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF:
            case PublicConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED: case PublicConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED: case PublicConsts.TRIGGER_TYPE_NET_ON:
            case PublicConsts.TRIGGER_TYPE_NET_OFF:{
                ((TextView)findViewById(R.id.trigger_widget_changed_value)).setText(getWidgetDisplayValue(this,type));
                //timePicker.setVisibility(View.GONE);
            }
            break;
        }
    }

    @Override
    public void processMessage(Message msg) {
        switch (msg.what){
            default:break;
            /*case MESSAGE_GET_SSID_COMPLETE:{
                if(dialog_wait!=null) dialog_wait.cancel();
                View dialogview=LayoutInflater.from(this).inflate(R.layout.layout_dialog_with_listview,null);
                ListView wifi_list=dialogview.findViewById(R.id.layout_dialog_listview);
                final WifiInfoListAdapter adapter=new WifiInfoListAdapter((List<WifiConfiguration>)msg.obj, wifi_ssidinfo);
                //Log.d("wifi ssids ",wifi_ssidinfo);
                wifi_list.setAdapter(adapter);
                wifi_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        adapter.onItemClicked(i);
                    }
                });
               new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.activity_trigger_wifi_dialog_att))
                        .setView(dialogview)
                        .setPositiveButton(getResources().getString(R.string.dialog_button_positive), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                activateTriggerType(PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED);
                                wifi_ssidinfo =adapter.getSelectedIDs();
                                ((TextView)findViewById(R.id.trigger_wifi_connected_value)).setText(getWifiConnectionDisplayValue(Triggers.this,wifi_ssidinfo));
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.dialog_button_negative), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
            break; */
            case MESSAGE_GET_APPLIST_COMPLETE_OPEN: case MESSAGE_GET_APPLIST_COMPLETE_CLOSE:{
                if(dialog_appinfo==null) break;
                final int msg_what=msg.what;
                dialog_appinfo.findViewById(R.id.dialog_app_wait).setVisibility(View.GONE);
                ListView listview=dialog_appinfo.findViewById(R.id.dialog_app_list);
                final AppListAdapter adapter=new AppListAdapter(this,(List<AppListAdapter.AppItemInfo>)msg.obj,package_names);
                listview.setAdapter(adapter);
                dialog_appinfo.findViewById(R.id.dialog_app_list_area).setVisibility(View.VISIBLE);
                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        adapter.onItemClicked(position);
                    }
                });
                dialog_appinfo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] package_names=adapter.getSelectedPackageNames();
                        if(package_names.length==0) {
                            Snackbar.make(v,getResources().getString(R.string.dialog_app_select_z_att),Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        Triggers.this.package_names=package_names;
                        int type=msg_what==MESSAGE_GET_APPLIST_COMPLETE_OPEN?PublicConsts.TRIGGER_TYPE_APP_LAUNCHED:PublicConsts.TRIGGER_TYPE_APP_CLOSED;
                        activateTriggerType(type);
                        dialog_appinfo.cancel();
                    }
                });
                dialog_appinfo.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.deselectAll();
                    }
                });
            }
            break;
        }
    }

    @Override
    public void onClick(View v){
        final int v_id=v.getId();
        switch(v_id){
            default:break;
            case R.id.trigger_single:{
                activateTriggerType(PublicConsts.TRIGGER_TYPE_SINGLE);
                new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, final int year, final int month, final int dayOfMonth) {

                        new TimePickerDialog(Triggers.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                calendar.set(Calendar.YEAR,year);
                                calendar.set(Calendar.MONTH,month);
                                calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);

                                calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                calendar.set(Calendar.MINUTE,minute);
                                calendar.set(Calendar.SECOND,0);
                                activateTriggerType(PublicConsts.TRIGGER_TYPE_SINGLE);
                            }
                        },calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true).show();

                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
            break;
            case R.id.trigger_percertaintime:{
                activateTriggerType(PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME);
                BottomDialogForInterval dialog=new BottomDialogForInterval(this);
                dialog.setVariables((int)(interval/(1000*60*60*24)),
                        (int)((interval%(1000*60*60*24))/(1000*60*60)),
                        (int)((interval%(1000*60*60))/(1000*60)));
                dialog.setTitle(getResources().getString(R.string.dialog_setinterval_title));
                dialog.show();
                dialog.setOnDialogConfirmedListener(new BottomDialogForInterval.OnDialogConfirmedListener() {
                    @Override
                    public void onDialogConfirmed(long millis) {
                        interval=millis;
                        ((TextView)findViewById(R.id.trigger_percertaintime_value)).setText(getCertainLoopTimeDisplayValue(Triggers.this,millis));
                        activateTriggerType(PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME);
                    }
                });
            }
            break;
            case R.id.trigger_weekloop:{
                activateTriggerType(PublicConsts.TRIGGER_TYPE_LOOP_WEEK);
                LayoutInflater inflater=LayoutInflater.from(this);
                View dialogview=inflater.inflate(R.layout.layout_dialog_weekloop, null);
                final AlertDialog dialog_weekloop  = new AlertDialog.Builder(this)
                        .setIcon(R.drawable.icon_repeat_weekloop)
                        .setTitle(this.getResources().getString(R.string.dialog_weekloop_title))
                        .setView(dialogview)
                        .setPositiveButton(this.getResources().getString(R.string.dialog_button_positive), null)
                        .setNegativeButton(getResources().getString(R.string.dialog_button_negative), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setCancelable(true)
                        .create();

                dialog_weekloop.show();
                final CheckBox cb_mon=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_mon);
                final CheckBox cb_tue=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_tue);
                final CheckBox cb_wed=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_wed);
                final CheckBox cb_thu=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_thu);
                final CheckBox cb_fri=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_fri);
                final CheckBox cb_sat=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_sat);
                final CheckBox cb_sun=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_sun);

                cb_sun.setChecked(week_repeat[PublicConsts.WEEK_SUNDAY]);
                cb_mon.setChecked(week_repeat[PublicConsts.WEEK_MONDAY]);
                cb_tue.setChecked(week_repeat[PublicConsts.WEEK_TUESDAY]);
                cb_wed.setChecked(week_repeat[PublicConsts.WEEK_WEDNESDAY]);
                cb_thu.setChecked(week_repeat[PublicConsts.WEEK_THURSDAY]);
                cb_fri.setChecked(week_repeat[PublicConsts.WEEK_FRIDAY]);
                cb_sat.setChecked(week_repeat[PublicConsts.WEEK_SATURDAY]);

                dialog_weekloop.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        dialog_weekloop.cancel();

                        new TimePickerDialog(Triggers.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                boolean week_repeat[]=new boolean[7];
                                week_repeat[PublicConsts.WEEK_MONDAY]=cb_mon.isChecked();
                                week_repeat[PublicConsts.WEEK_TUESDAY]=cb_tue.isChecked();
                                week_repeat[PublicConsts.WEEK_WEDNESDAY]=cb_wed.isChecked();
                                week_repeat[PublicConsts.WEEK_THURSDAY]=cb_thu.isChecked();
                                week_repeat[PublicConsts.WEEK_FRIDAY]=cb_fri.isChecked();
                                week_repeat[PublicConsts.WEEK_SATURDAY]=cb_sat.isChecked();
                                week_repeat[PublicConsts.WEEK_SUNDAY]=cb_sun.isChecked();
                                boolean allunchecked=true;
                                for (int i=0;i<7;i++){
                                    if(week_repeat[i]){
                                        allunchecked=false;
                                        break;
                                    }
                                }
                                //((TextView)findViewById(R.id.trigger_weekloop_value)).setText(getWeekLoopDisplayValue(Triggers.this,week_repeat,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE)));
                                if(allunchecked){
                                    calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                    calendar.set(Calendar.MINUTE,minute);
                                    calendar.set(Calendar.SECOND,0);
                                    activateTriggerType(PublicConsts.TRIGGER_TYPE_SINGLE);
                                    //dialog_weekloop.cancel();
                                    return;
                                }
                                //dialog_weekloop.cancel();
                                Triggers.this.week_repeat=week_repeat;
                                calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                calendar.set(Calendar.MINUTE,minute);
                                calendar.set(Calendar.SECOND,0);
                                activateTriggerType(PublicConsts.TRIGGER_TYPE_LOOP_WEEK);
                            }
                        },calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true).show();
                    }
                });

                /*dialog_weekloop.setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // TODO Auto-generated method stub
                        boolean allunchecked=true;
                        for(int i=0;i<7;i++){
                            if(week_repeat[i]){//if(TaskGui.this.weekloop[i]){
                                allunchecked=false;
                                break;
                            }
                        }
                        if(allunchecked) activateTriggerType(PublicConsts.TRIGGER_TYPE_SINGLE);
                    }
                });*/
            }
            break;
            case R.id.trigger_battery_percentage:{
                if(trigger_type!=PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE&&trigger_type!=PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE){
                    activateTriggerType(PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE);
                }else{
                    activateTriggerType(trigger_type);
                }

                final BottomDialogForBattery dialog=new BottomDialogForBattery(this);
                dialog.textview_title.setText(getResources().getString(R.string.activity_taskgui_condition_battery_percentage_att));
                dialog.checkbox_enable.setVisibility(View.GONE);
                dialog.textview_second_description.setText("%");
                String[] percentage=new String[99];
                for(int i=0;i<percentage.length;i++) {
                    int a=i+1;
                    percentage[i]=String.valueOf(a);
                }
                String[] compares={this.getResources().getString(R.string.dialog_battery_compare_more_than),this.getResources().getString(R.string.dialog_battery_compare_less_than)};
                dialog.wheelview_first.setItems(Arrays.asList(compares));
                dialog.wheelview_second.setItems(Arrays.asList(percentage));
                dialog.wheelview_first.setSeletion(trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE?0:(trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE?1:0));
                dialog.wheelview_second.setSeletion(battery_percentage-1);
                dialog.textview_confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position=dialog.wheelview_first.getSeletedIndex();
                        int trigger_type=(position==0?PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE:(position==1?PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE:PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE));
                        battery_percentage=Integer.parseInt(dialog.wheelview_second.getSeletedItem());
                        dialog.cancel();
                        activateTriggerType(trigger_type);
                        ((TextView)findViewById(R.id.trigger_battery_percentage_value)).setText(getBatteryPercentageDisplayValue(Triggers.this,trigger_type,battery_percentage));
                    }
                });
                dialog.show();
            }
            break;
            case R.id.trigger_battery_temperature:{
                if(trigger_type!=PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE &&trigger_type!=PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE){
                    activateTriggerType(PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE);
                }else{
                    activateTriggerType(trigger_type);
                }

                final BottomDialogForBattery dialog=new BottomDialogForBattery(this);
                dialog.textview_title.setText(getResources().getString(R.string.activity_taskgui_condition_battery_temperature_att));
                dialog.checkbox_enable.setVisibility(View.GONE);
                dialog.textview_second_description.setText("¡æ");
                String[] temperature=new String[66];
                for(int i=0;i<temperature.length;i++) {
                    temperature[i]=String.valueOf(i);
                }
                String[] compares={this.getResources().getString(R.string.dialog_battery_compare_higher_than),this.getResources().getString(R.string.dialog_battery_compare_lower_than)};
                dialog.wheelview_first.setItems(Arrays.asList(compares));
                dialog.wheelview_second.setItems(Arrays.asList(temperature));
                dialog.wheelview_first.setSeletion(trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE?0:(trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE?1:0));
                dialog.wheelview_second.setSeletion(battery_temperature);
                dialog.textview_confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position=dialog.wheelview_first.getSeletedIndex();
                        int trigger_type=(position==0?PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE:(position==1?PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE:PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE));
                        battery_temperature=Integer.parseInt(dialog.wheelview_second.getSeletedItem());
                        dialog.cancel();
                        activateTriggerType(trigger_type);
                        ((TextView)findViewById(R.id.trigger_battery_temperature_value)).setText(getBatteryTemperatureDisplayValue(Triggers.this,trigger_type,battery_temperature));
                    }
                });
                dialog.show();
            }
            break;
            case R.id.trigger_received_broadcast:{
                activateTriggerType(PublicConsts.TRIGGER_TYPE_RECEIVED_BROADCAST);
                View dialogView=LayoutInflater.from(this).inflate(R.layout.layout_dialog_with_listview,null);
                final BroadcastSelectionAdapter adapter=new BroadcastSelectionAdapter(broadcast_intent_action);
                ListView listView=dialogView.findViewById(R.id.layout_dialog_listview);
                listView.setDivider(null);
                (listView).setAdapter(adapter);
                final AlertDialog dialog=new AlertDialog.Builder(this)
                        .setTitle("IntentFilter")
                        .setView(dialogView)
                        .setPositiveButton(getResources().getString(R.string.dialog_button_positive),null)
                        .setNegativeButton(getResources().getString(R.string.dialog_button_negative), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        adapter.onItemClicked(i);
                    }
                });
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        broadcast_intent_action=adapter.getSelectedAction();
                        activateTriggerType(PublicConsts.TRIGGER_TYPE_RECEIVED_BROADCAST);
                        ((TextView)findViewById(R.id.trigger_received_broadcast_value)).setText(getBroadcastDisplayValue(broadcast_intent_action));
                        dialog.cancel();
                    }
                });
            }
            break;
            case R.id.trigger_wifi_connected: case R.id.trigger_wifi_disconnected:{
                //trigger_type=PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED;
                if(v_id==R.id.trigger_wifi_connected) activateTriggerType(PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED);
                else if(v_id==R.id.trigger_wifi_disconnected) activateTriggerType(PublicConsts.TRIGGER_TYPE_WIFI_DISCONNECTED);
                /*dialog_wait=new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_wait_att))
                        .setView(LayoutInflater.from(this).inflate(R.layout.layout_dialog_wait,null))
                        .setCancelable(false)
                        .show(); */
               /* new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg=new Message();
                        msg.what=MESSAGE_GET_SSID_COMPLETE;

                        WifiManager wifiManager=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                        if(wifiManager==null){
                            Log.e("Triggers","WifiManager is null !!");
                            msg.obj=new ArrayList<WifiConfiguration>();
                            sendMessage(msg);
                            return;
                        }

                        msg.obj=wifiManager.getConfiguredNetworks();

                        sendMessage(msg);
                    }
                }).start(); */

                final WifiManager wifiManager=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if(wifiManager==null){
                    Log.e("Triggers","WifiManager is null !!");
                    ((TextView)findViewById(R.id.trigger_wifi_connected_value)).setText(getWifiConnectionDisplayValue(Triggers.this,wifi_ssidinfo));
                    return;
                }

                if(NetworkReceiver.wifiList==null){
                    Snackbar snackbar=Snackbar.make(findViewById(R.id.trigger_root),getResources().getString(R.string.activity_trigger_wifi_open_att),Snackbar.LENGTH_SHORT);
                    if(v_id==R.id.trigger_wifi_connected)((TextView)findViewById(R.id.trigger_wifi_connected_value)).setText(getWifiConnectionDisplayValue(Triggers.this,wifi_ssidinfo));
                    else if(v_id==R.id.trigger_wifi_disconnected)((TextView)findViewById(R.id.trigger_wifi_disconnected_value)).setText(getWifiConnectionDisplayValue(Triggers.this,wifi_ssidinfo));
                    snackbar.setAction(getResources().getString(R.string.snackbar_action_open_wifi), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            wifiManager.setWifiEnabled(true);
                        }
                    });
                    snackbar.show();
                    return;
                }

                //List<WifiConfiguration> list=wifiManager.getConfiguredNetworks();

                View dialogview=LayoutInflater.from(this).inflate(R.layout.layout_dialog_with_listview,null);
                ListView wifi_list=dialogview.findViewById(R.id.layout_dialog_listview);
                final WifiInfoListAdapter adapter=new WifiInfoListAdapter(NetworkReceiver.wifiList, wifi_ssidinfo);
                //Log.d("wifi ssids ",wifi_ssidinfo);
                wifi_list.setAdapter(adapter);
                wifi_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        adapter.onItemClicked(i);
                    }
                });
                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.activity_trigger_wifi_dialog_att))
                        .setView(dialogview)
                        .setPositiveButton(getResources().getString(R.string.dialog_button_positive), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                wifi_ssidinfo =adapter.getSelectedIDs();
                                if(v_id==R.id.trigger_wifi_connected) {
                                    activateTriggerType(PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED);
                                    ((TextView)findViewById(R.id.trigger_wifi_connected_value)).setText(getWifiConnectionDisplayValue(Triggers.this,wifi_ssidinfo));
                                }
                                else if(v_id==R.id.trigger_wifi_disconnected) {
                                    activateTriggerType(PublicConsts.TRIGGER_TYPE_WIFI_DISCONNECTED);
                                    ((TextView)findViewById(R.id.trigger_wifi_disconnected_value)).setText(getWifiConnectionDisplayValue(Triggers.this,wifi_ssidinfo));
                                }
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.dialog_button_negative), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
            break;
            case R.id.trigger_app_opened: case R.id.trigger_app_closed:{
                if(Build.VERSION.SDK_INT>=23){
                    AppOpsManager manager=(AppOpsManager)getSystemService(Context.APP_OPS_SERVICE);
                    if(manager==null) {
                        Toast.makeText(this,"Can not get AppOpsManager instance",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean isGranted=manager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(),getPackageName())==AppOpsManager.MODE_ALLOWED;
                    if(!isGranted){
                        Snackbar snackbar=Snackbar.make(findViewById(R.id.trigger_root),getResources().getString(R.string.activity_trigger_app_usage_att),Snackbar.LENGTH_SHORT);
                        snackbar.setAction(getResources().getString(R.string.permission_grant_action_att), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                            }
                        });
                        snackbar.show();
                        return;
                    }
                }
                final int trigger_type=(v_id==R.id.trigger_app_opened?PublicConsts.TRIGGER_TYPE_APP_LAUNCHED:PublicConsts.TRIGGER_TYPE_APP_CLOSED);
                  this.dialog_appinfo=new AlertDialog.Builder(this)
                          .setTitle(trigger_type==PublicConsts.TRIGGER_TYPE_APP_LAUNCHED?getResources().getString(R.string.dialog_app_open_select_title)
                          :getResources().getString(R.string.dialog_app_close_select_title))
                          .setView(LayoutInflater.from(this).inflate(R.layout.layout_dialog_app_select,null))
                          .setPositiveButton(getResources().getString(R.string.dialog_button_positive),null)
                          .setNegativeButton(getResources().getString(R.string.action_deselectall),null)
                          .show();
                  new Thread(new Runnable() {
                      @Override
                      public void run() {
                          List<AppListAdapter.AppItemInfo> list=new ArrayList<>();
                          PackageManager manager=getPackageManager();
                          List<PackageInfo> list_get=manager.getInstalledPackages(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
                          for(PackageInfo info:list_get){
                              AppListAdapter.AppItemInfo itemInfo=new AppListAdapter.AppItemInfo();
                              itemInfo.icon=manager.getApplicationIcon(info.applicationInfo);
                              itemInfo.appname=manager.getApplicationLabel(info.applicationInfo).toString();
                              itemInfo.package_name=info.applicationInfo.packageName;
                              list.add(itemInfo);
                          }
                          Message msg=new Message();
                          msg.obj=list;
                          msg.what= trigger_type==PublicConsts.TRIGGER_TYPE_APP_LAUNCHED?MESSAGE_GET_APPLIST_COMPLETE_OPEN:MESSAGE_GET_APPLIST_COMPLETE_CLOSE;
                          sendMessage(msg);
                      }
                  }).start();
            }
            break;
            case R.id.trigger_headset:{
                final AlertDialog dialog=new AlertDialog.Builder(this)
                        .setIcon(R.drawable.icon_headset)
                        .setTitle(getResources().getString(R.string.activity_trigger_headset))
                        .setView(LayoutInflater.from(this).inflate(R.layout.layout_dialog_with_two_single_choices,null))
                        .show();
                RadioButton ra_plug_in=dialog.findViewById(R.id.dialog_choice_first);
                RadioButton ra_plug_out=dialog.findViewById(R.id.dialog_choice_second);
                ra_plug_in.setText(getResources().getString(R.string.activity_trigger_headset_plug_in));
                ra_plug_out.setText(getResources().getString(R.string.activity_trigger_headset_plug_out));
                ra_plug_in.setChecked(trigger_type==PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_IN);
                ra_plug_out.setChecked(trigger_type==PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT);
                ra_plug_in.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        trigger_type=PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_IN;
                        dialog.cancel();
                        activateTriggerType(PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_IN);
                    }
                });
                ra_plug_out.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        trigger_type=PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT;
                        dialog.cancel();
                        activateTriggerType(PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT);
                    }
                });
            }
            break;
            case R.id.trigger_widget_changed:{
                /*if(trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_ON||trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF||trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON
                        ||trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF||trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF||trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE
                        ||trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL||trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON||trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF
                        ||trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED||trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED||trigger_type==PublicConsts.TRIGGER_TYPE_NET_ON
                        ||trigger_type==PublicConsts.TRIGGER_TYPE_NET_OFF){
                    activateTriggerType(trigger_type);
                }else{
                    activateTriggerType(PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_ON);
                }*/
                View dialogView=LayoutInflater.from(this).inflate(R.layout.layout_dialog_triggers_widget,null);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_wifi_on_ra)).setChecked(trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_ON);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_wifi_off_ra)).setChecked(trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_bluetooth_on_ra)).setChecked(trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_bluetooth_off_ra)).setChecked(trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_ring_off_ra)).setChecked(trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_ring_vibrate_ra)).setChecked(trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_ring_normal_ra)).setChecked(trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_airplane_mode_on_ra)).setChecked(trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_airplane_mode_off_ra)).setChecked(trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_ap_on_ra)).setChecked(trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_ap_off_ra)).setChecked(trigger_type==PublicConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_net_on_ra)).setChecked(trigger_type==PublicConsts.TRIGGER_TYPE_NET_ON);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_net_off_ra)).setChecked(trigger_type==PublicConsts.TRIGGER_TYPE_NET_OFF);
                final AlertDialog dialog=new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.activity_triggers_widget_changed))
                        .setView(dialogView)
                        .setNegativeButton(getResources().getString(R.string.dialog_button_negative), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create();
                View.OnClickListener listener=new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int triggerType=PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_ON;
                        switch (v.getId()){
                            default:break;
                            case R.id.triggers_widget_wifi_on:{
                                triggerType=PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_ON;
                            }
                            break;
                            case R.id.triggers_widget_wifi_off:{
                                triggerType=PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF;
                            }
                            break;
                            case R.id.triggers_widget_bluetooth_on:{
                                triggerType=PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON;
                            }
                            break;
                            case R.id.triggers_widget_bluetooth_off:{
                                triggerType=PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF;
                            }
                            break;
                            case R.id.triggers_widget_ring_off:{
                                triggerType=PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF;
                            }
                            break;
                            case R.id.triggers_widget_ring_vibrate:{
                                triggerType=PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE;
                            }
                            break;
                            case R.id.triggers_widget_ring_normal:{
                                triggerType=PublicConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL;
                            }
                            break;
                            case R.id.triggers_widget_airplane_mode_on:{
                                triggerType=PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON;
                            }
                            break;
                            case R.id.triggers_widget_airplane_mode_off:{
                                triggerType=PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF;
                            }
                            break;
                            case R.id.triggers_widget_ap_on:{
                                triggerType=PublicConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED;
                            }
                            break;
                            case R.id.triggers_widget_ap_off:{
                                triggerType=PublicConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED;
                            }
                            break;
                            case R.id.triggers_widget_net_on:{
                                triggerType=PublicConsts.TRIGGER_TYPE_NET_ON;
                            }
                            break;
                            case R.id.triggers_widget_net_off:{
                                triggerType=PublicConsts.TRIGGER_TYPE_NET_OFF;
                            }
                            break;
                        }
                        activateTriggerType(triggerType);
                       // ((TextView)findViewById(R.id.trigger_widget_changed_value)).setText(getWidgetDisplayValue(Triggers.this,triggerType));
                        dialog.cancel();
                    }
                };
                dialog.show();
                dialogView.findViewById(R.id.triggers_widget_wifi_on).setOnClickListener(listener);
                dialogView.findViewById(R.id.triggers_widget_wifi_off).setOnClickListener(listener);
                dialogView.findViewById(R.id.triggers_widget_bluetooth_on).setOnClickListener(listener);
                dialogView.findViewById(R.id.triggers_widget_bluetooth_off).setOnClickListener(listener);
                dialogView.findViewById(R.id.triggers_widget_ring_off).setOnClickListener(listener);
                dialogView.findViewById(R.id.triggers_widget_ring_vibrate).setOnClickListener(listener);
                dialogView.findViewById(R.id.triggers_widget_ring_normal).setOnClickListener(listener);
                dialogView.findViewById(R.id.triggers_widget_airplane_mode_on).setOnClickListener(listener);
                dialogView.findViewById(R.id.triggers_widget_airplane_mode_off).setOnClickListener(listener);
                dialogView.findViewById(R.id.triggers_widget_ap_on).setOnClickListener(listener);
                dialogView.findViewById(R.id.triggers_widget_ap_off).setOnClickListener(listener);
                dialogView.findViewById(R.id.triggers_widget_net_on).setOnClickListener(listener);
                dialogView.findViewById(R.id.triggers_widget_net_off).setOnClickListener(listener);
            }
            break;
            case R.id.trigger_screen_on:{
                final AlertDialog dialog=new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.activity_triggers_screen))
                        .setView(LayoutInflater.from(this).inflate(R.layout.layout_dialog_with_two_single_choices,null))
                        .show();
                ((RadioButton)dialog.findViewById(R.id.dialog_choice_first)).setText(getResources().getString(R.string.activity_triggers_screen_on));
                ((RadioButton)dialog.findViewById(R.id.dialog_choice_second)).setText(getResources().getString(R.string.activity_triggers_screen_off));
                ((RadioButton)dialog.findViewById(R.id.dialog_choice_first)).setChecked(trigger_type==PublicConsts.TRIGGER_TYPE_SCREEN_ON);
                ((RadioButton)dialog.findViewById(R.id.dialog_choice_second)).setChecked(trigger_type==PublicConsts.TRIGGER_TYPE_SCREEN_OFF);
                (dialog.findViewById(R.id.dialog_choice_first)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        activateTriggerType(PublicConsts.TRIGGER_TYPE_SCREEN_ON);
                    }
                });
                dialog.findViewById(R.id.dialog_choice_second).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        activateTriggerType(PublicConsts.TRIGGER_TYPE_SCREEN_OFF);
                    }
                });
            }
            break;
            /*case R.id.trigger_screen_off:{
                activateTriggerType(PublicConsts.TRIGGER_TYPE_SCREEN_OFF);
            }
            break;*/
            case R.id.trigger_power_connected:{
                final AlertDialog dialog=new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.activity_triggers_power))
                        .setView(LayoutInflater.from(this).inflate(R.layout.layout_dialog_with_two_single_choices,null))
                        .show();
                ((RadioButton)dialog.findViewById(R.id.dialog_choice_first)).setText(getResources().getString(R.string.activity_triggers_power_connected));
                ((RadioButton)dialog.findViewById(R.id.dialog_choice_second)).setText(getResources().getString(R.string.activity_triggers_power_disconnected));
                ((RadioButton)dialog.findViewById(R.id.dialog_choice_first)).setChecked(trigger_type==PublicConsts.TRIGGER_TYPE_POWER_CONNECTED);
                ((RadioButton)dialog.findViewById(R.id.dialog_choice_second)).setChecked(trigger_type==PublicConsts.TRIGGER_TYPE_POWER_DISCONNECTED);
                (dialog.findViewById(R.id.dialog_choice_first)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        activateTriggerType(PublicConsts.TRIGGER_TYPE_POWER_CONNECTED);
                    }
                });
                dialog.findViewById(R.id.dialog_choice_second).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        activateTriggerType(PublicConsts.TRIGGER_TYPE_POWER_DISCONNECTED);
                    }
                });
            }
            break;
            /*case R.id.trigger_power_disconnected:{
                activateTriggerType(PublicConsts.TRIGGER_TYPE_POWER_DISCONNECTED);
            }
            break;*/
        }
    }

    @Override
    public void onTimeChanged(TimePicker timePicker, int hour, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,minute);
        calendar.set(Calendar.SECOND,0);
        if(trigger_type==PublicConsts.TRIGGER_TYPE_SINGLE) ((TextView)findViewById(R.id.trigger_single_value)).setText(getSingleTimeDisplayValue(this,calendar.getTimeInMillis()));
        if(trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_WEEK){
            ((TextView)findViewById(R.id.trigger_weekloop_value)).setText(getWeekLoopDisplayValue(this,week_repeat,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE)));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.triggers,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            default:break;
            case R.id.action_triggers_confirm:{
                String trigger_values []=new String[1];
                switch(trigger_type){
                    default:break;
                    case PublicConsts.TRIGGER_TYPE_SINGLE:{
                        trigger_values=new String [1];
                        trigger_values[0]=String.valueOf(calendar.getTimeInMillis());
                    }
                    break;
                    case PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME:{
                        trigger_values=new String[1];
                        trigger_values[0]=String.valueOf(interval);
                    }
                    break;
                    case PublicConsts.TRIGGER_TYPE_LOOP_WEEK:{
                        trigger_values=new String[8];
                        trigger_values[0]=String.valueOf(calendar.getTimeInMillis());
                        for(int i=1;i<trigger_values.length;i++){
                            trigger_values[i]=week_repeat[i-1]?String.valueOf(1):String.valueOf(0);
                        }
                    }
                    break;
                    case PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE: case PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE:{
                        trigger_values=new String[1];
                        trigger_values[0]=String.valueOf(battery_percentage);
                    }
                    break;
                    case PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE: case PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE:{
                        trigger_values=new String[1];
                        trigger_values[0]=String.valueOf(battery_temperature);
                    }
                    break;
                    case PublicConsts.TRIGGER_TYPE_RECEIVED_BROADCAST:{
                        trigger_values=new String[1];
                        trigger_values[0]=String.valueOf(broadcast_intent_action);
                    }
                    break;
                    case PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED: case PublicConsts.TRIGGER_TYPE_WIFI_DISCONNECTED:{
                        trigger_values=new String[1];
                        trigger_values[0]= wifi_ssidinfo;
                    }
                    break;
                    case PublicConsts.TRIGGER_TYPE_APP_LAUNCHED: case PublicConsts.TRIGGER_TYPE_APP_CLOSED:{
                        trigger_values=package_names;
                        if(trigger_values.length==0) trigger_values=new String[1];
                    }
                    break;
                }
                //if(trigger_values==null) return false;
                Intent i=new Intent();
                i.putExtra(EXTRA_TRIGGER_TYPE,trigger_type);
                i.putExtra(EXTRA_TRIGGER_VALUES,trigger_values);
                setResult(RESULT_OK,i);
                finish();
            }
            break;
            case android.R.id.home:{
                checkAndExit();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            checkAndExit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static String getSingleTimeDisplayValue(@NonNull Context context, long millis){
       // TextView tv_condition_single_value=findViewById(R.id.trigger_single_value);
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int month=calendar.get(Calendar.MONTH)+1;
        return context.getResources().getString(R.string.activity_taskgui_condition_single_value)+ ValueUtils.format(calendar.get(Calendar.YEAR))+"/"+ ValueUtils.format(month)+"/"+ ValueUtils.format(calendar.get(Calendar.DAY_OF_MONTH))+"("+ValueUtils.getDayOfWeek(calendar.getTimeInMillis())+")/"+ ValueUtils.format(calendar.get(Calendar.HOUR_OF_DAY))+":"+ ValueUtils.format(calendar.get(Calendar.MINUTE));
    }


    public static String getCertainLoopTimeDisplayValue(Context context, long loopmillis){
        //TextView tv_condition_percertaintime_value=findViewById(R.id.trigger_percertaintime_value);
        return context.getResources().getString(R.string.adapter_per)+ ValueUtils.getFormatTime(context,loopmillis)+context.getResources().getString(R.string.adapter_trigger);
    }

    public static String getWeekLoopDisplayValue(Context context,boolean week_repeat [],int hourOfDay,int minute){
        if(context==null||week_repeat==null||week_repeat.length!=7) return "";
        String tv_value="";
        //TextView tv_condition_weekloop_value=findViewById(R.id.layout_taskgui_area_condition_weekloop_value);
        if(week_repeat[1]) tv_value+=context.getResources().getString(R.string.monday)+" ";//if(this.weekloop[1]) tv_value+="ÖÜÒ» ";
        if(week_repeat[2]) tv_value+=context.getResources().getString(R.string.tuesday)+" ";
        if(week_repeat[3]) tv_value+=context.getResources().getString(R.string.wednesday)+" ";
        if(week_repeat[4]) tv_value+=context.getResources().getString(R.string.thursday)+" ";
        if(week_repeat[5]) tv_value+=context.getResources().getString(R.string.friday)+" ";
        if(week_repeat[6]) tv_value+=context.getResources().getString(R.string.saturday)+" ";
        if(week_repeat[0]) tv_value+=context.getResources().getString(R.string.sunday);

        boolean everyday=true;
        for(int i=0;i<7;i++){
            if(!week_repeat[i]) {  //if(!this.weekloop[i]) {
                everyday=false;
                break;
            }
        }

        String time=ValueUtils.format(hourOfDay)+":"+ValueUtils.format(minute);
        if(everyday) return context.getResources().getString(R.string.everyday)+" "+time;
        return  tv_value+" "+time;
    }

    public static String getBatteryPercentageDisplayValue(Context context,int trigger_type,int percentage){
       // TextView tv_battery=findViewById(R.id.layout_taskgui_area_condition_battery_percentage_value);
        StringBuilder value=new StringBuilder("");
        if(trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE){
            value.append(context.getResources().getString(R.string.more_than)+" ");
            value.append(percentage+"%");
        }else if(trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE){
            value.append(context.getResources().getString(R.string.less_than)+" ");
            value.append(percentage+"%");
        }
         return value.toString();
    }

    public static String getBatteryTemperatureDisplayValue(Context context,int trigger_type,int battery_temperature){
        //TextView tv_battery=findViewById(R.id.layout_taskgui_area_condition_battery_temperature_value);
        StringBuilder value=new StringBuilder("");
        if(trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE){
            value.append(context.getResources().getString(R.string.lower_than)+" ");
            value.append(battery_temperature+"¡æ");
        }else if(trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE){
            value.append(context.getResources().getString(R.string.higher_than)+" ");
            value.append(battery_temperature+"¡æ");
        }
        return value.toString();
    }

    public static String getBroadcastDisplayValue(String intent_action){
       // TextView tv_broadcast=findViewById(R.id.layout_taskgui_area_condition_received_broadcast_value);
       // if(trigger_type==PublicConsts.TRIGGER_TYPE_RECEIVED_BROADCAST){
        //    tv_broadcast.setText(taskitem.selectedAction);
       // }
        return  intent_action;
    }

    public static String getWifiConnectionDisplayValue(Context context, String ssids){
        if(context==null||ssids==null) return "";
        if(ssids.length()==0||ssids.trim().equals("")) return context.getResources().getString(R.string.activity_trigger_wifi_no_ssid_assigned);
       // WifiManager wifiManager=(WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(NetworkReceiver.wifiList==null||NetworkReceiver.wifiList.size()<=0) return context.getResources().getString(R.string.activity_trigger_wifi_assigned_ssid);
        StringBuilder display=new StringBuilder("");
        //List<WifiConfiguration> list=wifiManager.getConfiguredNetworks();
       // if(list==null||list.size()<=0) return "";
        String ssid_array [] =ssids.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
        for(String s:ssid_array){
            for(NetworkReceiver.WifiConfigInfo w:NetworkReceiver.wifiList){
                try{
                   if(Integer.parseInt(s)==w.networkID){
                       if(!display.toString().equals("")) display.append(" , ");
                       display.append(w.SSID);
                   }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        String displayValue=display.toString();
        if(displayValue.length()>75) displayValue=displayValue.substring(0,75)+"...";
        return displayValue;
    }

    public static String getWidgetDisplayValue(Context context,int triggerType){
        switch (triggerType){
            default:break;
            case PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_ON: return context.getResources().getString(R.string.dialog_triggers_widget_wifi_on);
            case PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF:return context.getResources().getString(R.string.dialog_triggers_widget_wifi_off);
            case PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON: return context.getResources().getString(R.string.dialog_triggers_widget_bluetooth_on);
            case PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF: return context.getResources().getString(R.string.dialog_triggers_widget_bluetooth_off);
            case PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF: return context.getResources().getString(R.string.dialog_triggers_widget_ring_mode_off);
            case PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE: return context.getResources().getString(R.string.dialog_triggers_widget_ring_mode_vibrate);
            case PublicConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL: return context.getResources().getString(R.string.dialog_triggers_widget_ring_mode_normal);
            case PublicConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED: return context.getResources().getString(R.string.dialog_triggers_widget_ap_on);
            case PublicConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED: return context.getResources().getString(R.string.dialog_triggers_widget_ap_off);
            case PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON: return context.getResources().getString(R.string.dialog_triggers_widget_airplane_mode_on);
            case PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF: return context.getResources().getString(R.string.dialog_triggers_widget_airplane_mode_off);
            case PublicConsts.TRIGGER_TYPE_NET_ON: return context.getResources().getString(R.string.dialog_triggers_widget_net_on);
            case PublicConsts.TRIGGER_TYPE_NET_OFF: return context.getResources().getString(R.string.dialog_triggers_widget_net_off);
        }
        return "";
    }

    public static String getAppNameDisplayValue(Context context,String[] packageNames){
        if(packageNames==null||packageNames.length==0) return "";
        StringBuilder builder=new StringBuilder("");
        PackageManager manager=context.getPackageManager();
        for(int i=0;i<packageNames.length;i++){
            String packageName=packageNames[i];
            try{
                builder.append(manager.getApplicationLabel(manager.getApplicationInfo(packageNames[i],PackageManager.GET_META_DATA)));
                if(packageName.length()>1&&i<packageNames.length-1) builder.append(",");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return builder.toString();
    }

    private void refreshTriggerDisplayValues(int type){
        TextView tv_condition_single_value=findViewById(R.id.trigger_single_value);
        TextView tv_condition_percertaintime_value=findViewById(R.id.trigger_percertaintime_value);
        TextView tv_condition_weekloop_value=findViewById(R.id.trigger_weekloop_value);
        TextView tv_condition_battery_percentage_value=findViewById(R.id.trigger_battery_percentage_value);
        TextView tv_condition_battery_temperature_value=findViewById(R.id.trigger_battery_temperature_value);
        TextView tv_wifi_connected=findViewById(R.id.trigger_wifi_connected_value);
        TextView tv_wifi_disconnected=findViewById(R.id.trigger_wifi_disconnected_value);
        TextView tv_widget_changed=findViewById(R.id.trigger_widget_changed_value);
        TextView tv_condition_broadcast=findViewById(R.id.trigger_received_broadcast_value);
        TextView tv_screen_on=findViewById(R.id.trigger_screen_on_value);
        TextView tv_screen_off=findViewById(R.id.trigger_screen_off_value);
        TextView tv_power_connected=findViewById(R.id.trigger_power_connected_value);
        TextView tv_power_disconnected=findViewById(R.id.trigger_power_disconnected_value);
        TextView tv_app_opened=findViewById(R.id.trigger_app_opened_value);
        TextView tv_app_closed=findViewById(R.id.trigger_app_closed_value);
        TextView tv_headset=findViewById(R.id.trigger_headset_value);

        String unchoose=this.getResources().getString(R.string.activity_taskgui_att_unchoose);

        ((RadioButton)findViewById(R.id.trigger_single_ra)).setChecked(type==PublicConsts.TRIGGER_TYPE_SINGLE);
        ((RadioButton)findViewById(R.id.trigger_percertaintime_ra)).setChecked(type==PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME);
        ((RadioButton)findViewById(R.id.trigger_weekloop_ra)).setChecked(type==PublicConsts.TRIGGER_TYPE_LOOP_WEEK);
        ((RadioButton)findViewById(R.id.trigger_battery_percentage_ra)).setChecked(type==PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE||type==PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE);
        ((RadioButton)findViewById(R.id.trigger_battery_temperature_ra)).setChecked(type==PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE||type==PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE);
        ((RadioButton)findViewById(R.id.trigger_received_broadcast_ra)).setChecked(type==PublicConsts.TRIGGER_TYPE_RECEIVED_BROADCAST);
        ((RadioButton)findViewById(R.id.trigger_wifi_connected_ra)).setChecked(type==PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED);
        ((RadioButton)findViewById(R.id.trigger_wifi_disconnected_ra)).setChecked(type==PublicConsts.TRIGGER_TYPE_WIFI_DISCONNECTED);
        ((RadioButton)findViewById(R.id.trigger_widget_changed_ra)).setChecked(
                type==PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_ON||type==PublicConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF
        ||type==PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON||type==PublicConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF
        ||type==PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF||type==PublicConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE
        ||type==PublicConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL||type==PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON
        ||type==PublicConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF||type==PublicConsts.TRIGGER_TYPE_NET_ON
        ||type==PublicConsts.TRIGGER_TYPE_NET_OFF||type==PublicConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED||type==PublicConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED);

        ((RadioButton)findViewById(R.id.trigger_screen_on_ra)).setChecked(type==PublicConsts.TRIGGER_TYPE_SCREEN_ON||type==PublicConsts.TRIGGER_TYPE_SCREEN_OFF);
        //((RadioButton)findViewById(R.id.trigger_screen_off_ra)).setChecked(type==PublicConsts.TRIGGER_TYPE_SCREEN_OFF);
        ((RadioButton)findViewById(R.id.trigger_power_connected_ra)).setChecked(type==PublicConsts.TRIGGER_TYPE_POWER_CONNECTED||type==PublicConsts.TRIGGER_TYPE_POWER_DISCONNECTED);
        //((RadioButton)findViewById(R.id.trigger_power_disconnected_ra)).setChecked(type==PublicConsts.TRIGGER_TYPE_POWER_DISCONNECTED);
        ((RadioButton)findViewById(R.id.trigger_app_opened_ra)).setChecked(type==PublicConsts.TRIGGER_TYPE_APP_LAUNCHED);
        ((RadioButton)findViewById(R.id.trigger_app_closed_ra)).setChecked(type==PublicConsts.TRIGGER_TYPE_APP_CLOSED);
        ((RadioButton)findViewById(R.id.trigger_headset_ra)).setChecked(type==PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_IN||trigger_type==PublicConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT);

        tv_condition_single_value.setText(unchoose);
        tv_condition_percertaintime_value.setText(unchoose);
        tv_condition_weekloop_value.setText(unchoose);
        tv_condition_battery_percentage_value.setText(unchoose);
        tv_condition_battery_temperature_value.setText(unchoose);
        tv_wifi_connected.setText(unchoose);
        tv_wifi_disconnected.setText(unchoose);
        tv_widget_changed.setText(unchoose);
        tv_condition_broadcast.setText(unchoose);
        tv_wifi_connected.setText(unchoose);
        tv_wifi_disconnected.setText(unchoose);
        tv_screen_on.setText(unchoose);
        tv_screen_off.setText(unchoose);
        tv_power_connected.setText(unchoose);
        tv_power_disconnected.setText(unchoose);
        tv_app_opened.setText(unchoose);
        tv_app_closed.setText(unchoose);
        tv_headset.setText(unchoose);
    }

    private void checkAndExit(){
        if(!toCheckString().equals(checkString)){
            long thisTime=System.currentTimeMillis();
            if(thisTime-first_clicked>1000){
                first_clicked=thisTime;
                Snackbar.make(findViewById(R.id.trigger_root),getResources().getString(R.string.snackbar_changes_not_saved_back),Snackbar.LENGTH_SHORT).show();
                return;
            }
            setResult(RESULT_CANCELED);
            finish();
        }else{
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private class BroadcastSelectionAdapter extends BaseAdapter {
        List<String> intent_list=new ArrayList<>();
        int selectedPosition=0;
        private BroadcastSelectionAdapter(@Nullable String selectedAction){
            intent_list.add(Intent.ACTION_ANSWER);
            intent_list.add(Intent.ACTION_BATTERY_LOW);
            intent_list.add(Intent.ACTION_MEDIA_BAD_REMOVAL);
            intent_list.add(Intent.ACTION_PACKAGE_REMOVED);
            intent_list.add(Intent.ACTION_POWER_CONNECTED);
            intent_list.add(Intent.ACTION_POWER_DISCONNECTED);
            intent_list.add(WifiManager.WIFI_STATE_CHANGED_ACTION);
            intent_list.add(Intent.ACTION_PACKAGE_CHANGED);
            intent_list.add(Intent.ACTION_SCREEN_OFF);
            intent_list.add(Intent.ACTION_SCREEN_ON);
            intent_list.add(Intent.ACTION_PACKAGE_REMOVED);
            intent_list.add(Intent.ACTION_PACKAGE_ADDED);
            intent_list.add(ConnectivityManager.CONNECTIVITY_ACTION);
            if(selectedAction==null) return;
            for(int i=0;i<intent_list.size();i++){
                if(selectedAction.equals(intent_list.get(i))) {
                    selectedPosition=i;
                    break;
                }
            }
        }
        @Override
        public int getCount() {
            return intent_list.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view==null){
                view=LayoutInflater.from(Triggers.this).inflate(R.layout.item_broadcast_intent,viewGroup,false);
            }
            ((RadioButton)view.findViewById(R.id.item_broadcast_ra)).setText(intent_list.get(i));
            ((RadioButton)view.findViewById(R.id.item_broadcast_ra)).setChecked(i==selectedPosition);
            return view;
        }

        public void onItemClicked(int position){
            selectedPosition=position;
            notifyDataSetChanged();
        }

        public String getSelectedAction(){
            return intent_list.get(selectedPosition);
        }
    }

    private class WifiInfoListAdapter extends BaseAdapter{
        private List<NetworkReceiver.WifiConfigInfo> list;
        private boolean[] isSelected;
        public WifiInfoListAdapter(List<NetworkReceiver.WifiConfigInfo> list,String selected_ids) {
            if(list==null||selected_ids==null) return;
            this.list=list;
            isSelected=new boolean[list.size()];
            if(selected_ids.equals("")) return;
            try{
                String[] ids=selected_ids.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                for(String id:ids){
                    for(int i=0;i<list.size();i++){
                        if(list.get(i).networkID==Integer.parseInt(id)) {
                            isSelected[i]=true;
                            break;
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                LogUtil.putExceptionLog(Triggers.this,e);
            }

        }

        @Override
        public int getCount() {
            return list==null?0:list.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(list==null) return null;
            if(view==null){
                view=LayoutInflater.from(Triggers.this).inflate(R.layout.item_wifiinfo,viewGroup,false);
            }

            ((TextView)view.findViewById(R.id.item_wifiinfo_ssid)).setText(list.get(i).SSID);
            ((CheckBox)view.findViewById(R.id.item_wifiinfo_cb)).setChecked(isSelected[i]);

            return view;
        }

        public void onItemClicked(int position){
            isSelected[position]=!isSelected[position];
            notifyDataSetChanged();
        }

        public String getSelectedIDs(){
            String ids="";
            for(int i=0;i<isSelected.length;i++){
                if(isSelected[i]) {
                    if(!ids.equals("")) ids+=PublicConsts.SEPARATOR_SECOND_LEVEL;
                    ids+=list.get(i).networkID;
                }
            }
            return ids;
        }

    }

    /*private class AppListAdapter extends BaseAdapter{
        List<AppItemInfo> list;
        boolean[] isSelected;
        AppListAdapter(List<AppItemInfo> list,String[] selectedPackageNames){
            this.list=list;
            isSelected=new boolean[list.size()];
            if(selectedPackageNames==null||selectedPackageNames.length==0) return;
            for(String name:selectedPackageNames){
                for(int i=0;i<list.size();i++){
                    if(name.equals(list.get(i).package_name)){
                        isSelected[i]=true;
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView==null){
                convertView=LayoutInflater.from(Triggers.this).inflate(R.layout.item_app_info,parent,false);
                holder=new ViewHolder();
                holder.icon=convertView.findViewById(R.id.item_app_icon);
                holder.tv_name=convertView.findViewById(R.id.item_app_name);
                holder.cb=convertView.findViewById(R.id.item_app_cb);
                convertView.setTag(holder);
            }else{
                holder=(ViewHolder) convertView.getTag();
            }
            holder.icon.setImageDrawable(list.get(position).icon);
            holder.tv_name.setText(list.get(position).appname);
            holder.cb.setChecked(isSelected[position]);
            return convertView;
        }

        public void onItemClicked(int position){
            if(position<0||position>=isSelected.length) return;
            isSelected[position]=!isSelected[position];
            notifyDataSetChanged();
        }

        public void deselectAll(){
            for(int i=0;i<isSelected.length;i++){
                isSelected[i]=false;
            }
            notifyDataSetChanged();
        }

        public String[] getSelectedPackageNames(){
            int selectedNum=0;
            for(int i=0;i<isSelected.length;i++){
                if(isSelected[i]) selectedNum++;
            }
            String[] names=new String[selectedNum];
            int j=0;
            for(int i=0;i<isSelected.length;i++){
                if(isSelected[i]) {
                    names[j]=list.get(i).package_name;
                    j++;
                }
            }
            return names;
        }

        private class ViewHolder{
            ImageView icon;
            TextView tv_name;
            CheckBox cb;
        }
    }

    private class AppItemInfo{
        public Drawable icon;
        public String appname="";
        public String package_name="";
    }  */

}
