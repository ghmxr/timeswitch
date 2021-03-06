package com.github.ghmxr.timeswitch.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.Global;
import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.adapters.ContentAdapter;
import com.github.ghmxr.timeswitch.data.v2.ExceptionConsts;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.ui.DialogForWifiInfoSelection;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForBatteryPercentageWithEnabledSelection;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForBatteryTemperatureWithEnabledSelection;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForPeriod;
import com.github.ghmxr.timeswitch.ui.DialogConfirmedCallBack;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForWifiStatus;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class ExceptionActivity extends BaseActivity implements View.OnClickListener {
    private TaskItem item;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        item=(TaskItem) getIntent().getSerializableExtra(EXTRA_SERIALIZED_TASKITEM);

        setContentView(R.layout.layout_exceptions);

        Toolbar toolbar=findViewById(R.id.exceptions_toolbar);
        setSupportActionBar(toolbar);

        try{
            this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception e){
            e.printStackTrace();
        }
        setToolBarAndStatusBarColor(toolbar,getIntent().getStringExtra(EXTRA_TITLE_COLOR));

        findViewById(R.id.exceptions_connector).setOnClickListener(this);
        findViewById(R.id.exceptions_screen_locked).setOnClickListener(this);
        findViewById(R.id.exceptions_screen_unlocked).setOnClickListener(this);
        findViewById(R.id.exceptions_wifi_enabled).setOnClickListener(this);
        findViewById(R.id.exceptions_wifi_disabled).setOnClickListener(this);
        findViewById(R.id.exceptions_bluetooth_enabled).setOnClickListener(this);
        findViewById(R.id.exceptions_bluetooth_disabled).setOnClickListener(this);
        findViewById(R.id.exceptions_ring_vibrate).setOnClickListener(this);
        findViewById(R.id.exceptions_ring_off).setOnClickListener(this);
        findViewById(R.id.exceptions_ring_normal).setOnClickListener(this);
        findViewById(R.id.exceptions_net_enabled).setOnClickListener(this);
        findViewById(R.id.exceptions_net_disabled).setOnClickListener(this);
        findViewById(R.id.exceptions_gps_on).setOnClickListener(this);
        findViewById(R.id.exceptions_gps_off).setOnClickListener(this);
        findViewById(R.id.exceptions_airplanemode_on).setOnClickListener(this);
        findViewById(R.id.exceptions_airplanemode_off).setOnClickListener(this);
        findViewById(R.id.exceptions_battery_percentage).setOnClickListener(this);
        findViewById(R.id.exceptions_battery_temperature).setOnClickListener(this);
        findViewById(R.id.exceptions_day_of_week).setOnClickListener(this);
        findViewById(R.id.exceptions_period).setOnClickListener(this);
        findViewById(R.id.exceptions_headset).setOnClickListener(this);
        findViewById(R.id.exceptions_wifi_status).setOnClickListener(this);
        findViewById(R.id.exceptions_incall_coming).setOnClickListener(this);
        findViewById(R.id.exceptions_incall_connected).setOnClickListener(this);
        findViewById(R.id.exceptions_incall_finished).setOnClickListener(this);
        /*if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_SINGLE||item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK){
            findViewById(R.id.exceptions_period).setVisibility(View.GONE);
            findViewById(R.id.exceptions_day_of_week).setVisibility(View.GONE);
        }
        if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE||item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE){
            findViewById(R.id.exceptions_battery_temperature).setVisibility(View.GONE);
        }
        if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE||item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE){
            findViewById(R.id.exceptions_battery_percentage).setVisibility(View.GONE);
        }*/

        try{
            ((CheckBox)findViewById(R.id.exceptions_screen_locked_cb)).setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_LOCKEDSCREEN])==1);
            ((CheckBox)findViewById(R.id.exceptions_screen_unlocked_cb)).setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_UNLOCKEDSCREEN])==1);
            ((CheckBox)findViewById(R.id.exceptions_wifi_enabled_cb)).setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_WIFI_ENABLED])==1);
            ((CheckBox)findViewById(R.id.exceptions_wifi_disabled_cb)).setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_WIFI_DISABLED])==1);
            ((CheckBox)findViewById(R.id.exceptions_bluetooth_enabled_cb)).setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BLUETOOTH_ENABLED])==1);
            ((CheckBox)findViewById(R.id.exceptions_bluetooth_disabled_cb)).setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BLUETOOTH_DISABLED])==1);
            ((CheckBox)findViewById(R.id.exceptions_ring_vibrate_cb)).setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_RING_VIBRATE])==1);
            ((CheckBox)findViewById(R.id.exceptions_ring_normal_cb)).setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_RING_NORMAL])==1);
            ((CheckBox)findViewById(R.id.exceptions_ring_off_cb)).setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_RING_OFF])==1);
            ((CheckBox)findViewById(R.id.exceptions_net_enabled_cb)).setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_NET_ENABLED])==1);
            ((CheckBox)findViewById(R.id.exceptions_net_disabled_cb)).setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_NET_DISABLED])==1);
            ((CheckBox)findViewById(R.id.exceptions_gps_on_cb)).setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_GPS_ENABLED])==1);
            ((CheckBox)findViewById(R.id.exceptions_gps_off_cb)).setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_GPS_DISABLED])==1);
            ((CheckBox)findViewById(R.id.exceptions_airplanemode_on_cb)).setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_AIRPLANE_MODE_ENABLED])==1);
            ((CheckBox)findViewById(R.id.exceptions_airplanemode_off_cb)).setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_AIRPLANE_MODE_DISABLED])==1);
            ((CheckBox)findViewById(R.id.exceptions_incall_coming_cb)).setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_IS_IN_CALL_COMING_STATE])==1);
            ((CheckBox)findViewById(R.id.exceptions_incall_connected_cb)).setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_IS_IN_CALL_CONNECTED_STATE])==1);
            ((CheckBox)findViewById(R.id.exceptions_incall_finished_cb)).setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_IS_NOT_IN_CALL_STATE])==1);
        }catch (Exception e){
            e.printStackTrace();
        }
        refreshExceptionConnectorValue();
        refreshBatteryPercentageView();
        refreshBatteryTemperatureView();
        refreshDayOfWeekView();
        refreshPeriodView();
        refreshHeadsetStatusView();
        refreshWifiStatusView();
    }

    @Override
    public void processMessage(Message msg){}

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            default:break;
            case R.id.exceptions_connector:{
                final AlertDialog dialog=new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.activity_taskgui_exception_connector))
                        .setView(LayoutInflater.from(this).inflate(R.layout.layout_dialog_with_two_single_choices,null))
                        .show();
                int exception_connector=Integer.parseInt(item.addition_exception_connector);
                ((RadioButton)dialog.findViewById(R.id.dialog_choice_first)).setChecked(exception_connector== ExceptionConsts.EXCEPTION_CONNECTOR_OR);
                ((RadioButton)dialog.findViewById(R.id.dialog_choice_second)).setChecked(exception_connector== ExceptionConsts.EXCEPTION_CONNECTOR_AND);
                ((RadioButton)dialog.findViewById(R.id.dialog_choice_first)).setText(getResources().getString(R.string.activity_taskgui_exception_connector_or));
                ((RadioButton)dialog.findViewById(R.id.dialog_choice_second)).setText(getResources().getString(R.string.activity_taskgui_exception_connector_and));
                (dialog.findViewById(R.id.dialog_choice_first)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        item.addition_exception_connector= String.valueOf(ExceptionConsts.EXCEPTION_CONNECTOR_OR);
                        refreshExceptionConnectorValue();
                    }
                });
                (dialog.findViewById(R.id.dialog_choice_second)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        item.addition_exception_connector= String.valueOf(ExceptionConsts.EXCEPTION_CONNECTOR_AND);
                        refreshExceptionConnectorValue();
                    }
                });
            }
            break;
            case R.id.exceptions_screen_locked:{
                CheckBox cb_screen_locked=findViewById(R.id.exceptions_screen_locked_cb);
                CheckBox cb_screen_unlocked=findViewById(R.id.exceptions_screen_unlocked_cb);
                cb_screen_locked.toggle();
                if(cb_screen_locked.isChecked()) cb_screen_unlocked.setChecked(false);
                item.exceptions[ExceptionConsts.EXCEPTION_LOCKEDSCREEN]=cb_screen_locked.isChecked()?String.valueOf(1):String.valueOf(0);
                item.exceptions[ExceptionConsts.EXCEPTION_UNLOCKEDSCREEN]=(cb_screen_unlocked.isChecked()?String.valueOf(1):String.valueOf(0));
            }
            break;
            case R.id.exceptions_screen_unlocked:{
                CheckBox cb_screen_locked=findViewById(R.id.exceptions_screen_locked_cb);
                CheckBox cb_screen_unlocked=findViewById(R.id.exceptions_screen_unlocked_cb);
                cb_screen_unlocked.toggle();
                if(cb_screen_unlocked.isChecked()) cb_screen_locked.setChecked(false);
                item.exceptions[ExceptionConsts.EXCEPTION_LOCKEDSCREEN]=cb_screen_locked.isChecked()?String.valueOf(1):String.valueOf(0);
                item.exceptions[ExceptionConsts.EXCEPTION_UNLOCKEDSCREEN]=(cb_screen_unlocked.isChecked()?String.valueOf(1):String.valueOf(0));
            }
            break;
            case R.id.exceptions_wifi_enabled:{
                CheckBox cb_wifi_enabled=findViewById(R.id.exceptions_wifi_enabled_cb);
                CheckBox cb_wifi_disabled=findViewById(R.id.exceptions_wifi_disabled_cb);
                cb_wifi_enabled.toggle();
                if(cb_wifi_enabled.isChecked()) cb_wifi_disabled.setChecked(false);
                item.exceptions[ExceptionConsts.EXCEPTION_WIFI_ENABLED]=(cb_wifi_enabled.isChecked()?String.valueOf(1):String.valueOf(0));
                item.exceptions[ExceptionConsts.EXCEPTION_WIFI_DISABLED]=(cb_wifi_disabled.isChecked()?String.valueOf(1):String.valueOf(0));
            }
            break;
            case R.id.exceptions_wifi_disabled:{
                CheckBox cb_wifi_enabled=findViewById(R.id.exceptions_wifi_enabled_cb);
                CheckBox cb_wifi_disabled=findViewById(R.id.exceptions_wifi_disabled_cb);
                cb_wifi_disabled.toggle();
                if(cb_wifi_disabled.isChecked()) cb_wifi_enabled.setChecked(false);
                item.exceptions[ExceptionConsts.EXCEPTION_WIFI_ENABLED]=(cb_wifi_enabled.isChecked()?String.valueOf(1):String.valueOf(0));
                item.exceptions[ExceptionConsts.EXCEPTION_WIFI_DISABLED]=(cb_wifi_disabled.isChecked()?String.valueOf(1):String.valueOf(0));
            }
            break;
            case R.id.exceptions_bluetooth_enabled:{
                CheckBox cb_bluetooth_enabled=findViewById(R.id.exceptions_bluetooth_enabled_cb);
                CheckBox cb_bluetooth_disabled=findViewById(R.id.exceptions_bluetooth_disabled_cb);
                cb_bluetooth_enabled.toggle();
                if(cb_bluetooth_enabled.isChecked()) cb_bluetooth_disabled.setChecked(false);
                item.exceptions[ExceptionConsts.EXCEPTION_BLUETOOTH_ENABLED]=(cb_bluetooth_enabled.isChecked()?String.valueOf(1):String.valueOf(0));
                item.exceptions[ExceptionConsts.EXCEPTION_BLUETOOTH_DISABLED]=(cb_bluetooth_disabled.isChecked()?String.valueOf(1):String.valueOf(0));
            }
            break;
            case R.id.exceptions_bluetooth_disabled:{
                CheckBox cb_bluetooth_enabled=findViewById(R.id.exceptions_bluetooth_enabled_cb);
                CheckBox cb_bluetooth_disabled=findViewById(R.id.exceptions_bluetooth_disabled_cb);
                cb_bluetooth_disabled.toggle();
                if(cb_bluetooth_disabled.isChecked()) cb_bluetooth_enabled.setChecked(false);
                item.exceptions[ExceptionConsts.EXCEPTION_BLUETOOTH_ENABLED]=(cb_bluetooth_enabled.isChecked()?String.valueOf(1):String.valueOf(0));
                item.exceptions[ExceptionConsts.EXCEPTION_BLUETOOTH_DISABLED]=(cb_bluetooth_disabled.isChecked()?String.valueOf(1):String.valueOf(0));
            }
            break;
            case R.id.exceptions_ring_vibrate:{
                CheckBox cb_ring_vibrate=findViewById(R.id.exceptions_ring_vibrate_cb);
                //CheckBox cb_ring_off=findViewById(R.id.exceptions_ring_off_cb);
                //CheckBox cb_ring_normal=findViewById(R.id.exceptions_ring_normal_cb);
                cb_ring_vibrate.toggle();
                //if(cb_ring_vibrate.isChecked()) {cb_ring_off.setChecked(false);cb_ring_normal.setChecked(false);}
                item.exceptions[ExceptionConsts.EXCEPTION_RING_VIBRATE]=(cb_ring_vibrate.isChecked()?String.valueOf(1):String.valueOf(0));
                //exceptions[PublicConsts.EXCEPTION_RING_OFF]=(cb_ring_off.isChecked()?String.valueOf(1):String.valueOf(0));
                //exceptions[PublicConsts.EXCEPTION_RING_NORMAL]=(cb_ring_normal.isChecked()?String.valueOf(1):String.valueOf(0));
            }
            break;
            case R.id.exceptions_ring_off:{
                //CheckBox cb_ring_vibrate=findViewById(R.id.exceptions_ring_vibrate_cb);
                CheckBox cb_ring_off=findViewById(R.id.exceptions_ring_off_cb);
                //CheckBox cb_ring_normal=findViewById(R.id.exceptions_ring_normal_cb);
                cb_ring_off.toggle();
                //if(cb_ring_off.isChecked()) {cb_ring_vibrate.setChecked(false);cb_ring_normal.setChecked(false);}
                //exceptions[PublicConsts.EXCEPTION_RING_VIBRATE]=(cb_ring_vibrate.isChecked()?String.valueOf(1):String.valueOf(0));
                item.exceptions[ExceptionConsts.EXCEPTION_RING_OFF]=(cb_ring_off.isChecked()?String.valueOf(1):String.valueOf(0));
                //exceptions[PublicConsts.EXCEPTION_RING_NORMAL]=(cb_ring_normal.isChecked()?String.valueOf(1):String.valueOf(0));
            }
            break;
            case R.id.exceptions_ring_normal:{
                //CheckBox cb_ring_vibrate=findViewById(R.id.exceptions_ring_vibrate_cb);
                //CheckBox cb_ring_off=findViewById(R.id.exceptions_ring_off_cb);
                CheckBox cb_ring_normal=findViewById(R.id.exceptions_ring_normal_cb);
                cb_ring_normal.toggle();
                //if(cb_ring_normal.isChecked()) {cb_ring_vibrate.setChecked(false);cb_ring_off.setChecked(false);}
                //exceptions[PublicConsts.EXCEPTION_RING_VIBRATE]=(cb_ring_vibrate.isChecked()?String.valueOf(1):String.valueOf(0));
                //exceptions[PublicConsts.EXCEPTION_RING_OFF]=(cb_ring_off.isChecked()?String.valueOf(1):String.valueOf(0));
                item.exceptions[ExceptionConsts.EXCEPTION_RING_NORMAL]=(cb_ring_normal.isChecked()?String.valueOf(1):String.valueOf(0));
            }
            break;
            case R.id.exceptions_net_enabled:{
                CheckBox cb_net_enabled=findViewById(R.id.exceptions_net_enabled_cb);
                CheckBox cb_net_disabled=findViewById(R.id.exceptions_net_disabled_cb);
                cb_net_enabled.toggle();
                if(cb_net_enabled.isChecked()) cb_net_disabled.setChecked(false);
                item.exceptions[ExceptionConsts.EXCEPTION_NET_ENABLED]=(cb_net_enabled.isChecked()?String.valueOf(1):String.valueOf(0));
                item.exceptions[ExceptionConsts.EXCEPTION_NET_DISABLED]=(cb_net_disabled.isChecked()?String.valueOf(1):String.valueOf(0));
            }
            break;
            case R.id.exceptions_net_disabled:{
                CheckBox cb_net_enabled=findViewById(R.id.exceptions_net_enabled_cb);
                CheckBox cb_net_disabled=findViewById(R.id.exceptions_net_disabled_cb);
                cb_net_disabled.toggle();
                if(cb_net_disabled.isChecked()) cb_net_enabled.setChecked(false);
                item.exceptions[ExceptionConsts.EXCEPTION_NET_ENABLED]=(cb_net_enabled.isChecked()?String.valueOf(1):String.valueOf(0));
                item.exceptions[ExceptionConsts.EXCEPTION_NET_DISABLED]=(cb_net_disabled.isChecked()?String.valueOf(1):String.valueOf(0));
            }
            break;
            case R.id.exceptions_gps_on:{
                CheckBox cb_gps_enabled=findViewById(R.id.exceptions_gps_on_cb);
                CheckBox cb_gps_disabled=findViewById(R.id.exceptions_gps_off_cb);
                cb_gps_enabled.toggle();
                if(cb_gps_enabled.isChecked()) cb_gps_disabled.setChecked(false);
                item.exceptions[ExceptionConsts.EXCEPTION_GPS_ENABLED]=(cb_gps_enabled.isChecked()?String.valueOf(1):String.valueOf(0));
                item.exceptions[ExceptionConsts.EXCEPTION_GPS_DISABLED]=(cb_gps_disabled.isChecked()?String.valueOf(1):String.valueOf(0));
            }
            break;
            case R.id.exceptions_gps_off:{
                CheckBox cb_gps_enabled=findViewById(R.id.exceptions_gps_on_cb);
                CheckBox cb_gps_disabled=findViewById(R.id.exceptions_gps_off_cb);
                cb_gps_disabled.toggle();
                if(cb_gps_disabled.isChecked()) cb_gps_enabled.setChecked(false);
                item.exceptions[ExceptionConsts.EXCEPTION_GPS_ENABLED]=(cb_gps_enabled.isChecked()?String.valueOf(1):String.valueOf(0));
                item.exceptions[ExceptionConsts.EXCEPTION_GPS_DISABLED]=(cb_gps_disabled.isChecked()?String.valueOf(1):String.valueOf(0));
            }
            break;
            case R.id.exceptions_airplanemode_on:{
                CheckBox cb_airplane_mode_on=findViewById(R.id.exceptions_airplanemode_on_cb);
                CheckBox cb_airplane_mode_off=findViewById(R.id.exceptions_airplanemode_off_cb);
                cb_airplane_mode_on.toggle();
                if(cb_airplane_mode_on.isChecked()) cb_airplane_mode_off.setChecked(false);
                item.exceptions[ExceptionConsts.EXCEPTION_AIRPLANE_MODE_ENABLED]=(cb_airplane_mode_on.isChecked()?String.valueOf(1):String.valueOf(0));
                item.exceptions[ExceptionConsts.EXCEPTION_AIRPLANE_MODE_DISABLED]=(cb_airplane_mode_off.isChecked()?String.valueOf(1):String.valueOf(0));
            }
            break;
            case R.id.exceptions_airplanemode_off:{
                CheckBox cb_airplane_mode_on=findViewById(R.id.exceptions_airplanemode_on_cb);
                CheckBox cb_airplane_mode_off=findViewById(R.id.exceptions_airplanemode_off_cb);
                cb_airplane_mode_off.toggle();
                if(cb_airplane_mode_off.isChecked()) cb_airplane_mode_on.setChecked(false);
                item.exceptions[ExceptionConsts.EXCEPTION_AIRPLANE_MODE_ENABLED]=(cb_airplane_mode_on.isChecked()?String.valueOf(1):String.valueOf(0));
                item.exceptions[ExceptionConsts.EXCEPTION_AIRPLANE_MODE_DISABLED]=(cb_airplane_mode_off.isChecked()?String.valueOf(1):String.valueOf(0));
            }
            break;
            case R.id.exceptions_incall_coming:{
                CheckBox cb_incall_coming=findViewById(R.id.exceptions_incall_coming_cb);
                //CheckBox cb_incall_connected=findViewById(R.id.exceptions_incall_connected_cb);
                //CheckBox cb_incall_finished=findViewById(R.id.exceptions_incall_finished_cb);
                cb_incall_coming.toggle();
                /*if(cb_incall_coming.isChecked()){
                    cb_incall_connected.setChecked(false);
                    cb_incall_finished.setChecked(false);
                    item.exceptions[ExceptionConsts.EXCEPTION_IS_NOT_IN_CALL_STATE]=String.valueOf(0);
                    item.exceptions[ExceptionConsts.EXCEPTION_IS_IN_CALL_CONNECTED_STATE]=String.valueOf(0);
                }*/
                item.exceptions[ExceptionConsts.EXCEPTION_IS_IN_CALL_COMING_STATE]=cb_incall_coming.isChecked()?String.valueOf(1):String.valueOf(0);
            }
            break;
            case R.id.exceptions_incall_connected:{
                //CheckBox cb_incall_coming=findViewById(R.id.exceptions_incall_coming_cb);
                CheckBox cb_incall_connected=findViewById(R.id.exceptions_incall_connected_cb);
                //CheckBox cb_incall_finished=findViewById(R.id.exceptions_incall_finished_cb);
                cb_incall_connected.toggle();
                /*if(cb_incall_connected.isChecked()){
                    cb_incall_coming.setChecked(false);
                    cb_incall_finished.setChecked(false);
                    item.exceptions[ExceptionConsts.EXCEPTION_IS_IN_CALL_COMING_STATE]=String.valueOf(0);
                    item.exceptions[ExceptionConsts.EXCEPTION_IS_NOT_IN_CALL_STATE]=String.valueOf(0);
                }*/
                item.exceptions[ExceptionConsts.EXCEPTION_IS_IN_CALL_CONNECTED_STATE]=cb_incall_connected.isChecked()?String.valueOf(1):String.valueOf(0);
            }
            break;
            case R.id.exceptions_incall_finished:{
                //CheckBox cb_incall_coming=findViewById(R.id.exceptions_incall_coming_cb);
                //CheckBox cb_incall_connected=findViewById(R.id.exceptions_incall_connected_cb);
                CheckBox cb_incall_finished=findViewById(R.id.exceptions_incall_finished_cb);
                cb_incall_finished.toggle();
                /*if(cb_incall_finished.isChecked()){
                    cb_incall_coming.setChecked(false);
                    cb_incall_connected.setChecked(false);
                    item.exceptions[ExceptionConsts.EXCEPTION_IS_IN_CALL_COMING_STATE]=String.valueOf(0);
                    item.exceptions[ExceptionConsts.EXCEPTION_IS_IN_CALL_CONNECTED_STATE]=String.valueOf(0);
                }*/
                item.exceptions[ExceptionConsts.EXCEPTION_IS_NOT_IN_CALL_STATE]=cb_incall_finished.isChecked()?String.valueOf(1):String.valueOf(0);
            }
            break;
            case R.id.exceptions_battery_percentage:{
                int ex_more_than=Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]);
                int ex_less_than=Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]);
                int selection_first=0;
                if(ex_less_than>=0) selection_first=1;
                int selection_second=50;
                if(ex_more_than>=0) selection_second=ex_more_than;
                else if(ex_less_than>=0) selection_second=ex_less_than;
                final BottomDialogForBatteryPercentageWithEnabledSelection dialog=new BottomDialogForBatteryPercentageWithEnabledSelection(this,(ex_more_than>=0||ex_less_than>=0),selection_first,selection_second);
                dialog.show();
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        if(result.equals("-1")){
                            item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]=String.valueOf(-1);
                            item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]=String.valueOf(-1);
                        }else{
                            //String[]results=result.split(",");
                            //int selection=Integer.parseInt(results[0]);
                            item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]=dialog.getFirstSelectionValue()==0?String.valueOf(dialog.getSecondSelectionValue()):String.valueOf(-1);
                            item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]=dialog.getFirstSelectionValue()==1?String.valueOf(dialog.getSecondSelectionValue()):String.valueOf(-1);
                        }
                        refreshBatteryPercentageView();
                    }
                });
            }
            break;

            case R.id.exceptions_battery_temperature:{
                int ex_higher_than=Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]);
                int ex_lower_than=Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]);
                int selection_first=0;
                if(ex_lower_than>=0) selection_first=1;
                int selection_second=45;
                if(ex_higher_than>=0) selection_second=ex_higher_than;
                else if(ex_lower_than>=0) selection_second=ex_lower_than;
                final BottomDialogForBatteryTemperatureWithEnabledSelection dialog=new BottomDialogForBatteryTemperatureWithEnabledSelection(this,(ex_lower_than>=0||ex_higher_than>=0),selection_first,selection_second);
                dialog.show();
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        if(result.equals("-1")){
                            item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]=String.valueOf(-1);
                            item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]=String.valueOf(-1);
                        }else {
                            //String[]results=result.split(",");
                            //int selection=Integer.parseInt(results[0]);
                            item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]=dialog.getFirstSelectionValue()==0?String.valueOf(dialog.getSecondSelectionValue()):String.valueOf(-1);
                            item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]=dialog.getFirstSelectionValue()==1?String.valueOf(dialog.getSecondSelectionValue()):String.valueOf(-1);
                        }
                        refreshBatteryTemperatureView();
                    }
                });
            }
            break;

            case R.id.exceptions_day_of_week:{
                View dialogview=LayoutInflater.from(this).inflate(R.layout.layout_dialog_weekloop,null);
                final CheckBox cb_mon=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_mon);
                final CheckBox cb_tue=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_tue);
                final CheckBox cb_wed=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_wed);
                final CheckBox cb_thu=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_thu);
                final CheckBox cb_fri=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_fri);
                final CheckBox cb_sat=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_sat);
                final CheckBox cb_sun=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_sun);

                try{
                    cb_mon.setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_MONDAY])==1);
                    cb_tue.setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_TUESDAY])==1);
                    cb_wed.setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_WEDNESDAY])==1);
                    cb_thu.setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_THURSDAY])==1);
                    cb_fri.setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_FRIDAY])==1);
                    cb_sat.setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_SATURDAY])==1);
                    cb_sun.setChecked(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_SUNDAY])==1);
                }catch (NumberFormatException ne){
                    ne.printStackTrace();
                }

                final AlertDialog dialog=new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_exceptions_day_of_week_title))
                        .setView(dialogview)
                        .setPositiveButton(getResources().getString(R.string.dialog_button_positive), null)
                        .setNegativeButton(getResources().getString(R.string.dialog_button_negative), null).create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!cb_mon.isChecked()||!cb_tue.isChecked()||!cb_wed.isChecked()
                                ||!cb_thu.isChecked()||!cb_fri.isChecked()||!cb_sat.isChecked()
                                ||!cb_sun.isChecked()){
                            item.exceptions[ExceptionConsts.EXCEPTION_MONDAY]=cb_mon.isChecked()?String.valueOf(1):String.valueOf(0);
                            item.exceptions[ExceptionConsts.EXCEPTION_TUESDAY]=cb_tue.isChecked()?String.valueOf(1):String.valueOf(0);
                            item.exceptions[ExceptionConsts.EXCEPTION_WEDNESDAY]=cb_wed.isChecked()?String.valueOf(1):String.valueOf(0);
                            item.exceptions[ExceptionConsts.EXCEPTION_THURSDAY]=cb_thu.isChecked()?String.valueOf(1):String.valueOf(0);
                            item.exceptions[ExceptionConsts.EXCEPTION_FRIDAY]=cb_fri.isChecked()?String.valueOf(1):String.valueOf(0);
                            item.exceptions[ExceptionConsts.EXCEPTION_SATURDAY]=cb_sat.isChecked()?String.valueOf(1):String.valueOf(0);
                            item.exceptions[ExceptionConsts.EXCEPTION_SUNDAY]=cb_sun.isChecked()?String.valueOf(1):String.valueOf(0);
                            dialog.cancel();
                            refreshDayOfWeekView();
                        }
                        else{
                            Snackbar.make(view,getResources().getString(R.string.dialog_exceptions_day_of_week_all_selected),Snackbar.LENGTH_SHORT).show();
                            return;
                        }

                    }
                });
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });


            }
            break;

            case R.id.exceptions_period:{//0~1439
                BottomDialogForPeriod dialog=new BottomDialogForPeriod(this);
                boolean isEnabled=false;
                int startHour=0;
                int startMin=0;
                int endHour=0;
                int endMin=0;
                try{
                    isEnabled=Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_START_TIME])!=-1&&Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_END_TIME])!=-1;
                    startHour=isEnabled?Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_START_TIME])/60:18;
                    startMin=isEnabled?Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_START_TIME])%60:0;
                    endHour=isEnabled?Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_END_TIME])/60:8;
                    endMin=isEnabled?Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_END_TIME])%60:0;
                }catch (NumberFormatException ne){
                    ne.printStackTrace();
                }
                dialog.setVariables(isEnabled,startHour,startMin,endHour,endMin);
                dialog.setTitle(getResources().getString(R.string.dialog_exceptions_period_title));
                dialog.show();
                dialog.setOnDialogConfirmedListener(new BottomDialogForPeriod.OnDialogConfirmedListener() {
                    @Override
                    public void onConfirmed(boolean isEnabled, int start_hour, int start_minute, int end_hour, int end_minute) {
                        item.exceptions[ExceptionConsts.EXCEPTION_START_TIME]=isEnabled?String.valueOf(start_hour*60+start_minute):String.valueOf(-1);
                        item.exceptions[ExceptionConsts.EXCEPTION_END_TIME]=isEnabled?String.valueOf(end_hour*60+end_minute):String.valueOf(-1);
                        refreshPeriodView();
                    }
                });

            }
            break;
            case R.id.exceptions_headset:{
                try{
                    int headset_selection=Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_HEADSET_STATUS]);
                    final AlertDialog dialog=new AlertDialog.Builder(this)
                            .setTitle(getResources().getString(R.string.activity_taskgui_exception_headset))
                            .setIcon(R.drawable.icon_headset)
                            .setView(LayoutInflater.from(this).inflate(R.layout.layout_dialog_with_three_single_choices,null))
                            .show();
                    RadioButton button_unselected=dialog.findViewById(R.id.dialog3_choice_first);
                    RadioButton button_plugged=dialog.findViewById(R.id.dialog3_choice_second);
                    RadioButton button_unplugged=dialog.findViewById(R.id.dialog3_choice_third);
                    button_unselected.setText(getResources().getString(R.string.word_unselected));
                    button_plugged.setText(getResources().getString(R.string.activity_taskgui_exception_headset_in));
                    button_unplugged.setText(getResources().getString(R.string.activity_taskgui_exception_headset_out));
                    button_unselected.setChecked(headset_selection==0);
                    button_plugged.setChecked(headset_selection== ExceptionConsts.EXCEPTION_HEADSET_PLUG_IN);
                    button_unplugged.setChecked(headset_selection== ExceptionConsts.EXCEPTION_HEADSET_PLUG_OUT);
                    button_unselected.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            item.exceptions[ExceptionConsts.EXCEPTION_HEADSET_STATUS]=String.valueOf(0);
                            dialog.cancel();
                            refreshHeadsetStatusView();
                        }
                    });
                    button_unplugged.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            item.exceptions[ExceptionConsts.EXCEPTION_HEADSET_STATUS]=String.valueOf(ExceptionConsts.EXCEPTION_HEADSET_PLUG_OUT);
                            dialog.cancel();
                            refreshHeadsetStatusView();
                        }
                    });
                    button_plugged.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            item.exceptions[ExceptionConsts.EXCEPTION_HEADSET_STATUS]=String.valueOf(ExceptionConsts.EXCEPTION_HEADSET_PLUG_IN);
                            dialog.cancel();
                            refreshHeadsetStatusView();
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
                }

            }
            break;
            case R.id.exceptions_wifi_status:{
                final int []values=ValueUtils.string2intArray(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL,item.exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS]);
                BottomDialogForWifiStatus dialog=new BottomDialogForWifiStatus(this,values.length>0?values[0]:-1);
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        int value=Integer.parseInt(result);
                        switch (value){
                            default:break;
                            case -1:{
                                item.exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS]=String.valueOf(-1);
                                refreshWifiStatusView();
                            }
                            break;
                            case ExceptionConsts.EXCEPTION_WIFI_VALUE_DISCONNECTED:{
                                item.exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS]=String.valueOf(ExceptionConsts.EXCEPTION_WIFI_VALUE_DISCONNECTED);
                                refreshWifiStatusView();
                            }
                            break;
                            case ExceptionConsts.EXCEPTION_WIFI_VALUE_CONNECTED_TO_RANDOM_SSID:{
                                item.exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS]=String.valueOf(ExceptionConsts.EXCEPTION_WIFI_VALUE_CONNECTED_TO_RANDOM_SSID);
                                refreshWifiStatusView();
                                if(Global.NetworkReceiver.wifiList2.size()==0){
                                    Snackbar.make(findViewById(android.R.id.content),getResources().getString(R.string.activity_trigger_wifi_open_att),Snackbar.LENGTH_SHORT).show();
                                    return;
                                }
                                DialogForWifiInfoSelection dialog1=new DialogForWifiInfoSelection(ExceptionActivity.this,values);
                                dialog1.setOnDialogConfirmedListener(new DialogForWifiInfoSelection.DialogConfirmedListener() {
                                    @Override
                                    public void onDialogConfirmed(int[] ids) {
                                        if(ids==null||ids.length==0) {
                                            item.exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS]=String.valueOf(ExceptionConsts.EXCEPTION_WIFI_VALUE_CONNECTED_TO_RANDOM_SSID);
                                        }else item.exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS]=ValueUtils.intArray2String(PublicConsts.SEPARATOR_SECOND_LEVEL,ids);
                                        refreshWifiStatusView();
                                    }
                                });
                                dialog1.show();
                            }
                            break;
                        }
                    }
                });
                dialog.show();
            }
            break;
        }
    }

    @Override
    public void finish(){
        Intent i=new Intent();
        i.putExtra(EXTRA_SERIALIZED_TASKITEM,item);
        setResult(RESULT_OK,i);
        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.exceptions,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id =item.getItemId();
        switch (id){
            default:break;
            case android.R.id.home:{
                finish();
            }
            break;
            case R.id.actions_exceptions_confirm:{
                finish();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshExceptionConnectorValue(){
        TextView tv=findViewById(R.id.exceptions_connector_value);
        tv.setText(Integer.parseInt(item.addition_exception_connector)>=0?getResources().getString(R.string.activity_taskgui_exception_connector_and):getResources().getString(R.string.activity_taskgui_exception_connector_or));
    }

    private void refreshBatteryPercentageView(){
        TextView value=this.findViewById(R.id.exceptions_battery_percentage_value);
        try{
            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE])>=0){
                value.setText(this.getResources().getString(R.string.dialog_battery_compare_less_than)+item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]+"%");
            }else if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE])>=0){
                value.setText(this.getResources().getString(R.string.dialog_battery_compare_more_than)+item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]+"%");
            }else {
                value.setText(this.getResources().getString(R.string.dialog_battery_not_enabled));
            }
        }catch (NumberFormatException ne){
            ne.printStackTrace();
        }

    }

    private void refreshHeadsetStatusView(){
        try{
            TextView tv=findViewById(R.id.exceptions_headset_value);
            int selection=Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_HEADSET_STATUS]);
            switch (selection){
                default:break;
                case 0:tv.setText(getResources().getString(R.string.word_unselected));break;
                case ExceptionConsts.EXCEPTION_HEADSET_PLUG_OUT:tv.setText(getResources().getString(R.string.activity_taskgui_exception_headset_out));break;
                case ExceptionConsts.EXCEPTION_HEADSET_PLUG_IN:tv.setText(getResources().getString(R.string.activity_taskgui_exception_headset_in));break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void refreshBatteryTemperatureView(){
        TextView value=this.findViewById(R.id.exceptions_battery_temperature_value);
        try{
            if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE])>=0){
                value.setText(this.getResources().getString(R.string.dialog_battery_compare_lower_than)+item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]+"��");
            }else if(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE])>=0){
                value.setText(this.getResources().getString(R.string.dialog_battery_compare_higher_than)+item.exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]+"��");
            }else {
                value.setText(this.getResources().getString(R.string.dialog_battery_not_enabled));
            }
        }catch (NumberFormatException ne){
            ne.printStackTrace();
        }

    }

    private void refreshDayOfWeekView(){
        TextView tv_value=findViewById(R.id.exceptions_day_of_week_value);
        StringBuilder value=new StringBuilder("");
        try{
            value.append(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_MONDAY])==1?getResources().getString(R.string.monday)+" ":"");
            value.append(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_TUESDAY])==1?getResources().getString(R.string.tuesday)+" ":"");
            value.append(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_WEDNESDAY])==1?getResources().getString(R.string.wednesday)+" ":"");
            value.append(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_THURSDAY])==1?getResources().getString(R.string.thursday)+" ":"");
            value.append(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_FRIDAY])==1?getResources().getString(R.string.friday)+" ":"");
            value.append(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_SATURDAY])==1?getResources().getString(R.string.saturday)+' ':"");
            value.append(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_SUNDAY])==1?getResources().getString(R.string.sunday)+" ":"");
            tv_value.setText(value.toString().equals("")?getResources().getString(R.string.not_activated):value.toString());
        }catch (NumberFormatException ne){
            ne.printStackTrace();
        }

    }

    private void refreshPeriodView(){
        TextView tv_value=findViewById(R.id.exceptions_period_value);
        try{
            tv_value.setText((Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_START_TIME])!=-1&&Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_END_TIME])!=-1)?
                    ValueUtils.format(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_START_TIME])/60)+":"+ ValueUtils.format(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_START_TIME])%60)+"~"+ ValueUtils.format(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_END_TIME])/60)+":"+ ValueUtils.format(Integer.parseInt(item.exceptions[ExceptionConsts.EXCEPTION_END_TIME])%60)
                    :getResources().getString(R.string.not_activated));
        }catch (NumberFormatException ne){
            ne.printStackTrace();
        }

    }

    private void refreshWifiStatusView(){
        ((TextView)findViewById(R.id.exceptions_wifi_status_value)).setText(ContentAdapter.ExceptionContentAdapter.getExceptionValueOfWifiStatus(ExceptionActivity.this,item.exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS]));
    }

}
