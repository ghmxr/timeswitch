package com.github.ghmxr.timeswitch.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import com.github.ghmxr.timeswitch.Global;
import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.adapters.ContentAdapter;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.ui.ActionDisplayValue;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForBatteryPercentageWithEnabledSelection;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForBatteryTemperatureWithEnabledSelection;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForInterval;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.DialogConfirmedCallBack;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.DialogForAppSelection;
import com.github.ghmxr.timeswitch.utils.EnvironmentUtils;
import com.github.ghmxr.timeswitch.utils.LogUtil;

public class TriggerActivity extends BaseActivity implements View.OnClickListener{
    private TaskItem item;
    Calendar calendar=Calendar.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_triggers);
        Toolbar toolbar=findViewById(R.id.triggers_toolbar);
        setSupportActionBar(toolbar);

        try{getSupportActionBar().setDisplayHomeAsUpEnabled(true);}catch (Exception e){e.printStackTrace();}
        setToolBarAndStatusBarColor(toolbar,getIntent().getStringExtra(EXTRA_TITLE_COLOR));

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
        item=(TaskItem) getIntent().getSerializableExtra(EXTRA_SERIALIZED_TASKITEM);
        calendar.setTimeInMillis(item.time);
        calendar.set(Calendar.SECOND,0);
        Log.d("TaskItem",item.toString());

        //set the views

        activateTriggerType(item.trigger_type);
    }

    private void activateTriggerType(int type){
        item.trigger_type=type;
        refreshTriggerDisplayValues(type);
        switch (type){
            default:break;
            case TriggerTypeConsts.TRIGGER_TYPE_SINGLE:{
                ((TextView)findViewById(R.id.trigger_single_value)).setText(ContentAdapter.TriggerContentAdapter.TriggerDisplayStrings.getSingleTimeDisplayValue(this,calendar.getTimeInMillis()));
                //timePicker.setVisibility(View.VISIBLE);
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME:{
                ((TextView)findViewById(R.id.trigger_percertaintime_value)).setText(ContentAdapter.TriggerContentAdapter.TriggerDisplayStrings.getCertainLoopTimeDisplayValue(this,item.interval_milliseconds));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK:{
                ((TextView)findViewById(R.id.trigger_weekloop_value)).setText(ContentAdapter.TriggerContentAdapter.TriggerDisplayStrings.getWeekLoopDisplayValue(this,item.week_repeat,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE)));
                //timePicker.setVisibility(View.VISIBLE);
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE: case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE:{
                ((TextView)findViewById(R.id.trigger_battery_temperature_value)).setText(ContentAdapter.TriggerContentAdapter.TriggerDisplayStrings.getBatteryTemperatureDisplayValue(this,type,item.battery_temperature));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE: case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE:{
                ((TextView)findViewById(R.id.trigger_battery_percentage_value)).setText(ContentAdapter.TriggerContentAdapter.TriggerDisplayStrings.getBatteryPercentageDisplayValue(this,type,item.battery_percentage));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_RECEIVED_BROADCAST:{
                ((TextView)findViewById(R.id.trigger_received_broadcast_value)).setText(ContentAdapter.TriggerContentAdapter.TriggerDisplayStrings.getBroadcastDisplayValue(item.selectedAction));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_WIFI_CONNECTED:{
                ((TextView)findViewById(R.id.trigger_wifi_connected_value)).setText(ContentAdapter.TriggerContentAdapter.TriggerDisplayStrings.getWifiConnectionDisplayValue(this,item.wifiIds));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_WIFI_DISCONNECTED:{
                ((TextView)findViewById(R.id.trigger_wifi_disconnected_value)).setText(ContentAdapter.TriggerContentAdapter.TriggerDisplayStrings.getWifiConnectionDisplayValue(this,item.wifiIds));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_APP_LAUNCHED: {
                ((TextView)findViewById(R.id.trigger_app_opened_value)).setText(ContentAdapter.TriggerContentAdapter.TriggerDisplayStrings.getAppNameDisplayValue(this,item.package_names));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_APP_CLOSED:{
                ((TextView)findViewById(R.id.trigger_app_closed_value)).setText(ContentAdapter.TriggerContentAdapter.TriggerDisplayStrings.getAppNameDisplayValue(this,item.package_names));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_IN:{
                ((TextView)findViewById(R.id.trigger_headset_value)).setText(getResources().getString(R.string.activity_trigger_headset_plug_in));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT:{
                ((TextView)findViewById(R.id.trigger_headset_value)).setText(getResources().getString(R.string.activity_trigger_headset_plug_out));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_SCREEN_ON:{
                ((TextView)findViewById(R.id.trigger_screen_on_value)).setText(getResources().getString(R.string.activity_triggers_screen_on));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_SCREEN_OFF:{
                ((TextView)findViewById(R.id.trigger_screen_on_value)).setText(getResources().getString(R.string.activity_triggers_screen_off));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_POWER_CONNECTED:{
                ((TextView)findViewById(R.id.trigger_power_connected_value)).setText(getResources().getString(R.string.activity_triggers_power_connected));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_POWER_DISCONNECTED:{
                ((TextView)findViewById(R.id.trigger_power_connected_value)).setText(getResources().getString(R.string.activity_triggers_power_disconnected));
                //timePicker.setVisibility(View.GONE);
            }
            break;
            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_ON: case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF: case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON:
            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF: case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF: case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE:
            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL: case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON: case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF:
            case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED: case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED: case TriggerTypeConsts.TRIGGER_TYPE_NET_ON:
            case TriggerTypeConsts.TRIGGER_TYPE_NET_OFF:{
                ((TextView)findViewById(R.id.trigger_widget_changed_value)).setText(ContentAdapter.TriggerContentAdapter.TriggerDisplayStrings.getWidgetDisplayValue(this,type));
                //timePicker.setVisibility(View.GONE);
            }
            break;
        }
    }

    @Override
    public void processMessage(Message msg) {
        switch (msg.what){
            default:break;
        }
    }

    @Override
    public void onClick(View v){
        final int v_id=v.getId();
        switch(v_id){
            default:break;
            case R.id.trigger_single:{
                activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_SINGLE);
                new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, final int year, final int month, final int dayOfMonth) {

                        new TimePickerDialog(TriggerActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                calendar.set(Calendar.YEAR,year);
                                calendar.set(Calendar.MONTH,month);
                                calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);

                                calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                calendar.set(Calendar.MINUTE,minute);
                                calendar.set(Calendar.SECOND,0);
                                item.time=calendar.getTimeInMillis();
                                activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_SINGLE);
                            }
                        },calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true).show();

                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
            break;
            case R.id.trigger_percertaintime:{
                activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME);
                BottomDialogForInterval dialog=new BottomDialogForInterval(this);
                dialog.setVariables((int)(item.interval_milliseconds/(1000*60*60*24)),
                        (int)((item.interval_milliseconds%(1000*60*60*24))/(1000*60*60)),
                        (int)((item.interval_milliseconds%(1000*60*60))/(1000*60)));
                dialog.setTitle(getResources().getString(R.string.dialog_setinterval_title));
                dialog.show();
                dialog.setOnDialogConfirmedListener(new BottomDialogForInterval.OnDialogConfirmedListener() {
                    @Override
                    public void onDialogConfirmed(long millis) {
                        item.interval_milliseconds=millis;
                        ((TextView)findViewById(R.id.trigger_percertaintime_value)).setText(ContentAdapter.TriggerContentAdapter.TriggerDisplayStrings.getCertainLoopTimeDisplayValue(TriggerActivity.this,millis));
                        activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME);
                    }
                });
            }
            break;
            case R.id.trigger_weekloop:{
                activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK);
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

                cb_sun.setChecked(item.week_repeat[PublicConsts.WEEK_SUNDAY]);
                cb_mon.setChecked(item.week_repeat[PublicConsts.WEEK_MONDAY]);
                cb_tue.setChecked(item.week_repeat[PublicConsts.WEEK_TUESDAY]);
                cb_wed.setChecked(item.week_repeat[PublicConsts.WEEK_WEDNESDAY]);
                cb_thu.setChecked(item.week_repeat[PublicConsts.WEEK_THURSDAY]);
                cb_fri.setChecked(item.week_repeat[PublicConsts.WEEK_FRIDAY]);
                cb_sat.setChecked(item.week_repeat[PublicConsts.WEEK_SATURDAY]);

                dialog_weekloop.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        dialog_weekloop.cancel();

                        new TimePickerDialog(TriggerActivity.this, new TimePickerDialog.OnTimeSetListener() {
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
                                    activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_SINGLE);
                                    //dialog_weekloop.cancel();
                                    return;
                                }
                                //dialog_weekloop.cancel();
                                TriggerActivity.this.item.week_repeat=week_repeat;
                                calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                calendar.set(Calendar.MINUTE,minute);
                                calendar.set(Calendar.SECOND,0);
                                item.time=calendar.getTimeInMillis();
                                activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK);
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
                if(item.trigger_type!= TriggerTypeConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE&&item.trigger_type!= TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE){
                    activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE);
                }else{
                    activateTriggerType(item.trigger_type);
                }
                int selection_first=0;
                if(item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE) selection_first=0;
                else if(item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE) selection_first=1;
                final BottomDialogForBatteryPercentageWithEnabledSelection.BottomDialogForBatteryPercentageWithoutEnabledSelection dialog=
                        new BottomDialogForBatteryPercentageWithEnabledSelection.BottomDialogForBatteryPercentageWithoutEnabledSelection(this,selection_first,item.battery_percentage);
                dialog.show();
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                       // String[] results= result.split(",");
                        item.battery_percentage=dialog.getSecondSelectionValue();
                        if(dialog.getFirstSelectionValue()== BottomDialogForBatteryPercentageWithEnabledSelection.BottomDialogForBatteryPercentageWithoutEnabledSelection.SELECTION_VALUE_MORE_THAN) {
                            activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE);
                        }else if(dialog.getFirstSelectionValue()== BottomDialogForBatteryPercentageWithEnabledSelection.BottomDialogForBatteryPercentageWithoutEnabledSelection.SELECTION_VALUE_LESS_THAN){
                            activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE);
                        }
                    }
                });
            }
            break;
            case R.id.trigger_battery_temperature:{
                if(item.trigger_type!= TriggerTypeConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE &&item.trigger_type!= TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE){
                    activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE);
                }else{
                    activateTriggerType(item.trigger_type);
                }
                int selection_first=0;
                if(item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE) selection_first=0;
                else if(item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE) selection_first=1;
                final BottomDialogForBatteryTemperatureWithEnabledSelection.BottomDialogForBatteryTemperatureWithoutEnabledSelection dialog=
                        new BottomDialogForBatteryTemperatureWithEnabledSelection.BottomDialogForBatteryTemperatureWithoutEnabledSelection(this,selection_first,item.battery_percentage);
                dialog.show();
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        // String[] results= result.split(",");
                        item.battery_temperature=dialog.getSecondSelectionValue();
                        if(dialog.getFirstSelectionValue()== 0) {
                            activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE);
                        }else if(dialog.getFirstSelectionValue()== 1){
                            activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE);
                        }
                    }
                });
            }
            break;
            case R.id.trigger_received_broadcast:{
                activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_RECEIVED_BROADCAST);
                View dialogView=LayoutInflater.from(this).inflate(R.layout.layout_dialog_with_listview,null);
                final BroadcastSelectionAdapter adapter=new BroadcastSelectionAdapter(item.selectedAction);
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
                        item.selectedAction=adapter.getSelectedAction();
                        activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_RECEIVED_BROADCAST);
                        ((TextView)findViewById(R.id.trigger_received_broadcast_value)).setText(ContentAdapter.TriggerContentAdapter.TriggerDisplayStrings.getBroadcastDisplayValue(item.selectedAction));
                        dialog.cancel();
                    }
                });
            }
            break;
            case R.id.trigger_wifi_connected: case R.id.trigger_wifi_disconnected:{
                //trigger_type=PublicConsts.TRIGGER_TYPE_WIFI_CONNECTED;
                if(v_id==R.id.trigger_wifi_connected) activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_WIFI_CONNECTED);
                else if(v_id==R.id.trigger_wifi_disconnected) activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_WIFI_DISCONNECTED);

                final WifiManager wifiManager=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if(wifiManager==null){
                    Log.e("Triggers","WifiManager is null !!");
                    ((TextView)findViewById(R.id.trigger_wifi_connected_value)).setText(ContentAdapter.TriggerContentAdapter.TriggerDisplayStrings.getWifiConnectionDisplayValue(TriggerActivity.this,item.wifiIds));
                    return;
                }

                if(Global.NetworkReceiver.wifiList==null){
                    Snackbar snackbar=Snackbar.make(findViewById(R.id.trigger_root),getResources().getString(R.string.activity_trigger_wifi_open_att),Snackbar.LENGTH_SHORT);
                    if(v_id==R.id.trigger_wifi_connected)((TextView)findViewById(R.id.trigger_wifi_connected_value)).setText(ContentAdapter.TriggerContentAdapter.TriggerDisplayStrings.getWifiConnectionDisplayValue(TriggerActivity.this,item.wifiIds));
                    else if(v_id==R.id.trigger_wifi_disconnected)((TextView)findViewById(R.id.trigger_wifi_disconnected_value)).setText(ContentAdapter.TriggerContentAdapter.TriggerDisplayStrings.getWifiConnectionDisplayValue(TriggerActivity.this,item.wifiIds));
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
                final WifiInfoListAdapter adapter=new WifiInfoListAdapter(Global.NetworkReceiver.wifiList, item.wifiIds);
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
                                item.wifiIds =adapter.getSelectedIDs();
                                if(v_id==R.id.trigger_wifi_connected) {
                                    activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_WIFI_CONNECTED);
                                    ((TextView)findViewById(R.id.trigger_wifi_connected_value)).setText(ContentAdapter.TriggerContentAdapter.TriggerDisplayStrings.getWifiConnectionDisplayValue(TriggerActivity.this,item.wifiIds));
                                }
                                else if(v_id==R.id.trigger_wifi_disconnected) {
                                    activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_WIFI_DISCONNECTED);
                                    ((TextView)findViewById(R.id.trigger_wifi_disconnected_value)).setText(ContentAdapter.TriggerContentAdapter.TriggerDisplayStrings.getWifiConnectionDisplayValue(TriggerActivity.this,item.wifiIds));
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
                if(!EnvironmentUtils.PermissionRequestUtil.checkAndShowRequestUsageStatusPermissionSnackbar(this,
                        getResources().getString(R.string.activity_trigger_app_usage_att)
                        ,getResources().getString(R.string.permission_grant_action_att))) return;

                DialogForAppSelection dialog=new DialogForAppSelection(this,v_id==R.id.trigger_app_opened?getResources().getString(R.string.activity_trigger_app_opened)
                        :getResources().getString(R.string.activity_trigger_app_closed),item.package_names,v_id==R.id.trigger_app_opened?null:"#55e74c3c","");
                dialog.setOnDialogConfirmedCallBack(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        try{
                            if(result.equals("-1")) return;
                            item.package_names=result.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                            refreshTriggerDisplayValues(v_id==R.id.trigger_app_opened?TriggerTypeConsts.TRIGGER_TYPE_APP_LAUNCHED:TriggerTypeConsts.TRIGGER_TYPE_APP_CLOSED);
                            //if(v_id==R.id.trigger_app_opened){
                            ((TextView)findViewById(v_id==R.id.trigger_app_opened?R.id.trigger_app_opened_value:R.id.trigger_app_closed_value)).setText(ActionDisplayValue.getAppNameDisplayValue(TriggerActivity.this,result));
                            //}
                        }catch (Exception e){item.package_names=new String[0];}

                    }
                });
                dialog.show();
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
                ra_plug_in.setChecked(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_IN);
                ra_plug_out.setChecked(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT);
                ra_plug_in.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        item.trigger_type= TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_IN;
                        dialog.cancel();
                        activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_IN);
                    }
                });
                ra_plug_out.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        item.trigger_type= TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT;
                        dialog.cancel();
                        activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT);
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
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_wifi_on_ra)).setChecked(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_ON);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_wifi_off_ra)).setChecked(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_bluetooth_on_ra)).setChecked(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_bluetooth_off_ra)).setChecked(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_ring_off_ra)).setChecked(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_ring_vibrate_ra)).setChecked(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_ring_normal_ra)).setChecked(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_airplane_mode_on_ra)).setChecked(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_airplane_mode_off_ra)).setChecked(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_ap_on_ra)).setChecked(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_ap_off_ra)).setChecked(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_net_on_ra)).setChecked(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_NET_ON);
                ((RadioButton)dialogView.findViewById(R.id.triggers_widget_net_off_ra)).setChecked(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_NET_OFF);
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
                        int triggerType= TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_ON;
                        switch (v.getId()){
                            default:break;
                            case R.id.triggers_widget_wifi_on:{
                                triggerType= TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_ON;
                            }
                            break;
                            case R.id.triggers_widget_wifi_off:{
                                triggerType= TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF;
                            }
                            break;
                            case R.id.triggers_widget_bluetooth_on:{
                                triggerType= TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON;
                            }
                            break;
                            case R.id.triggers_widget_bluetooth_off:{
                                triggerType= TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF;
                            }
                            break;
                            case R.id.triggers_widget_ring_off:{
                                triggerType= TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF;
                            }
                            break;
                            case R.id.triggers_widget_ring_vibrate:{
                                triggerType= TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE;
                            }
                            break;
                            case R.id.triggers_widget_ring_normal:{
                                triggerType= TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL;
                            }
                            break;
                            case R.id.triggers_widget_airplane_mode_on:{
                                triggerType= TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON;
                            }
                            break;
                            case R.id.triggers_widget_airplane_mode_off:{
                                triggerType= TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF;
                            }
                            break;
                            case R.id.triggers_widget_ap_on:{
                                triggerType= TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED;
                            }
                            break;
                            case R.id.triggers_widget_ap_off:{
                                triggerType= TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED;
                            }
                            break;
                            case R.id.triggers_widget_net_on:{
                                triggerType= TriggerTypeConsts.TRIGGER_TYPE_NET_ON;
                            }
                            break;
                            case R.id.triggers_widget_net_off:{
                                triggerType= TriggerTypeConsts.TRIGGER_TYPE_NET_OFF;
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
                ((RadioButton)dialog.findViewById(R.id.dialog_choice_first)).setChecked(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_SCREEN_ON);
                ((RadioButton)dialog.findViewById(R.id.dialog_choice_second)).setChecked(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_SCREEN_OFF);
                (dialog.findViewById(R.id.dialog_choice_first)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_SCREEN_ON);
                    }
                });
                dialog.findViewById(R.id.dialog_choice_second).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_SCREEN_OFF);
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
                ((RadioButton)dialog.findViewById(R.id.dialog_choice_first)).setChecked(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_POWER_CONNECTED);
                ((RadioButton)dialog.findViewById(R.id.dialog_choice_second)).setChecked(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_POWER_DISCONNECTED);
                (dialog.findViewById(R.id.dialog_choice_first)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_POWER_CONNECTED);
                    }
                });
                dialog.findViewById(R.id.dialog_choice_second).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        activateTriggerType(TriggerTypeConsts.TRIGGER_TYPE_POWER_DISCONNECTED);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.triggers,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch(menuItem.getItemId()){
            default:break;
            case R.id.action_triggers_confirm:case android.R.id.home:{
                finish();
            }
            break;
        }
        return super.onOptionsItemSelected(menuItem);
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

        ((RadioButton)findViewById(R.id.trigger_single_ra)).setChecked(type== TriggerTypeConsts.TRIGGER_TYPE_SINGLE);
        ((RadioButton)findViewById(R.id.trigger_percertaintime_ra)).setChecked(type== TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME);
        ((RadioButton)findViewById(R.id.trigger_weekloop_ra)).setChecked(type== TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK);
        ((RadioButton)findViewById(R.id.trigger_battery_percentage_ra)).setChecked(type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE||type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE);
        ((RadioButton)findViewById(R.id.trigger_battery_temperature_ra)).setChecked(type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE||type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE);
        ((RadioButton)findViewById(R.id.trigger_received_broadcast_ra)).setChecked(type== TriggerTypeConsts.TRIGGER_TYPE_RECEIVED_BROADCAST);
        ((RadioButton)findViewById(R.id.trigger_wifi_connected_ra)).setChecked(type== TriggerTypeConsts.TRIGGER_TYPE_WIFI_CONNECTED);
        ((RadioButton)findViewById(R.id.trigger_wifi_disconnected_ra)).setChecked(type== TriggerTypeConsts.TRIGGER_TYPE_WIFI_DISCONNECTED);
        ((RadioButton)findViewById(R.id.trigger_widget_changed_ra)).setChecked(
                type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_ON||type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF
        ||type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON||type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF
        ||type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF||type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE
        ||type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL||type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON
        ||type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF||type== TriggerTypeConsts.TRIGGER_TYPE_NET_ON
        ||type== TriggerTypeConsts.TRIGGER_TYPE_NET_OFF||type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED||type== TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED);

        ((RadioButton)findViewById(R.id.trigger_screen_on_ra)).setChecked(type== TriggerTypeConsts.TRIGGER_TYPE_SCREEN_ON||type== TriggerTypeConsts.TRIGGER_TYPE_SCREEN_OFF);
        //((RadioButton)findViewById(R.id.trigger_screen_off_ra)).setChecked(type==PublicConsts.TRIGGER_TYPE_SCREEN_OFF);
        ((RadioButton)findViewById(R.id.trigger_power_connected_ra)).setChecked(type== TriggerTypeConsts.TRIGGER_TYPE_POWER_CONNECTED||type== TriggerTypeConsts.TRIGGER_TYPE_POWER_DISCONNECTED);
        //((RadioButton)findViewById(R.id.trigger_power_disconnected_ra)).setChecked(type==PublicConsts.TRIGGER_TYPE_POWER_DISCONNECTED);
        ((RadioButton)findViewById(R.id.trigger_app_opened_ra)).setChecked(type== TriggerTypeConsts.TRIGGER_TYPE_APP_LAUNCHED);
        ((RadioButton)findViewById(R.id.trigger_app_closed_ra)).setChecked(type== TriggerTypeConsts.TRIGGER_TYPE_APP_CLOSED);
        ((RadioButton)findViewById(R.id.trigger_headset_ra)).setChecked(type== TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_IN||type== TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT);

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

    @Override
    public void finish(){
        Intent intent=new Intent();
        intent.putExtra(EXTRA_SERIALIZED_TASKITEM,item);
        setResult(RESULT_OK,intent);
        Log.d("taskitem",item.toString());
        super.finish();
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
                view=LayoutInflater.from(TriggerActivity.this).inflate(R.layout.item_broadcast_intent,viewGroup,false);
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
        private List<Global.NetworkReceiver.WifiConfigInfo> list;
        private boolean[] isSelected;
        public WifiInfoListAdapter(List<Global.NetworkReceiver.WifiConfigInfo> list, String selected_ids) {
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
                LogUtil.putExceptionLog(TriggerActivity.this,e);
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
                view=LayoutInflater.from(TriggerActivity.this).inflate(R.layout.item_wifiinfo,viewGroup,false);
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

}
