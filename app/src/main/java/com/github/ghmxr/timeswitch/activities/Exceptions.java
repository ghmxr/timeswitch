package com.github.ghmxr.timeswitch.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.ui.BottomDialogForBattery;
import com.github.ghmxr.timeswitch.ui.BottomDialogForPeriod;
import com.github.ghmxr.timeswitch.utils.LogUtil;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.util.Arrays;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class Exceptions extends BaseActivity implements View.OnClickListener {

    public static final String INTENT_EXTRA_EXCEPTIONS="exceptions";
    public static final String INTENT_EXTRA_TRIGGER_TYPE="trigger_type";

    public static final int EXCEPTIONS_RESULT_CANCEL=0;
    public static final int EXCEPTIONS_RESULT_SUCCESS=1;

    RelativeLayout area_screen_locked,area_screen_unlocked,area_wifi_enabled,area_wifi_disabled,area_bluetooth_enabled,area_bluetooth_disabled,area_ring_vibrate,
    area_ring_off,area_ring_normal,area_net_enabled,area_net_disabled,area_gps_enabled,area_gps_disabled,area_airplane_mode_on,area_airplane_mode_off,
    area_battery_percentage,area_battery_temperature,area_day_of_week,area_period;

    CheckBox cb_screen_locked,cb_screen_unlocked,cb_wifi_enabled,cb_wifi_disabled,cb_bluetooth_enabled,cb_bluetooth_disabled,cb_ring_vibrate,cb_ring_off,
    cb_ring_normal,cb_net_enabled,cb_net_disabled,cb_gps_enabled,cb_gps_disabled,cb_airplane_mode_on,cb_airplane_mode_off;

    private String exceptions[]=new String[PublicConsts.EXCEPTION_LENTH];
    //private int trigger_type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.exceptions=this.getIntent().getStringArrayExtra(INTENT_EXTRA_EXCEPTIONS);
        int trigger_type=this.getIntent().getIntExtra(INTENT_EXTRA_TRIGGER_TYPE,-1);

        setContentView(R.layout.layout_exceptions);

        Toolbar toolbar=findViewById(R.id.exceptions_toolbar);
        setSupportActionBar(toolbar);

        try{
            this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception e){
            e.printStackTrace();
        }
        setToolBarAndStatusBarColor(toolbar,getIntent().getStringExtra(EXTRA_TITLE_COLOR));

        area_screen_locked=findViewById(R.id.exceptions_screen_locked);
        area_screen_unlocked=findViewById(R.id.exceptions_screen_unlocked);
        area_wifi_enabled=findViewById(R.id.exceptions_wifi_enabled);
        area_wifi_disabled=findViewById(R.id.exceptions_wifi_disabled);
        area_bluetooth_enabled=findViewById(R.id.exceptions_bluetooth_enabled);
        area_bluetooth_disabled=findViewById(R.id.exceptions_bluetooth_disabled);
        area_ring_vibrate=findViewById(R.id.exceptions_ring_vibrate);
        area_ring_off=findViewById(R.id.exceptions_ring_off);
        area_ring_normal=findViewById(R.id.exceptions_ring_normal);
        area_net_enabled=findViewById(R.id.exceptions_net_enabled);
        area_net_disabled=findViewById(R.id.exceptions_net_disabled);
        area_gps_enabled=findViewById(R.id.exceptions_gps_on);
        area_gps_disabled=findViewById(R.id.exceptions_gps_off);
        area_airplane_mode_on=findViewById(R.id.exceptions_airplanemode_on);
        area_airplane_mode_off=findViewById(R.id.exceptions_airplanemode_off);
        area_battery_percentage=findViewById(R.id.exceptions_battery_percentage);
        area_battery_temperature=findViewById(R.id.exceptions_battery_temperature);
        area_day_of_week=findViewById(R.id.exceptions_day_of_week);
        area_period=findViewById(R.id.exceptions_period);

        cb_screen_locked=findViewById(R.id.exceptions_screen_locked_cb);
        cb_screen_unlocked=findViewById(R.id.exceptions_screen_unlocked_cb);
        cb_wifi_enabled=findViewById(R.id.exceptions_wifi_enabled_cb);
        cb_wifi_disabled=findViewById(R.id.exceptions_wifi_disabled_cb);
        cb_bluetooth_enabled=findViewById(R.id.exceptions_bluetooth_enabled_cb);
        cb_bluetooth_disabled=findViewById(R.id.exceptions_bluetooth_disabled_cb);
        cb_ring_vibrate=findViewById(R.id.exceptions_ring_vibrate_cb);
        cb_ring_normal=findViewById(R.id.exceptions_ring_normal_cb);
        cb_ring_off=findViewById(R.id.exceptions_ring_off_cb);
        cb_net_enabled=findViewById(R.id.exceptions_net_enabled_cb);
        cb_net_disabled=findViewById(R.id.exceptions_net_disabled_cb);
        cb_gps_enabled=findViewById(R.id.exceptions_net_gps_on_cb);
        cb_gps_disabled=findViewById(R.id.exceptions_net_gps_off_cb);
        cb_airplane_mode_on=findViewById(R.id.exceptions_airplanemode_on_cb);
        cb_airplane_mode_off=findViewById(R.id.exceptions_airplanemode_off_cb);

        if(trigger_type==PublicConsts.TRIGGER_TYPE_SINGLE||trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_WEEK){
            area_period.setVisibility(View.GONE);
            area_day_of_week.setVisibility(View.GONE);
        }
        if(trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE||trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE){
            area_battery_temperature.setVisibility(View.GONE);
        }
        if(trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE||trigger_type==PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE){
            area_battery_percentage.setVisibility(View.GONE);
        }

        area_screen_locked.setOnClickListener(this);
        area_screen_unlocked.setOnClickListener(this);
        area_wifi_enabled.setOnClickListener(this);
        area_wifi_disabled.setOnClickListener(this);
        area_bluetooth_enabled.setOnClickListener(this);
        area_bluetooth_disabled.setOnClickListener(this);
        area_ring_vibrate.setOnClickListener(this);
        area_ring_off.setOnClickListener(this);
        area_ring_normal.setOnClickListener(this);
        area_net_enabled.setOnClickListener(this);
        area_net_disabled.setOnClickListener(this);
        area_gps_enabled.setOnClickListener(this);
        area_gps_disabled.setOnClickListener(this);
        area_airplane_mode_on.setOnClickListener(this);
        area_airplane_mode_off.setOnClickListener(this);
        area_battery_percentage.setOnClickListener(this);
        area_battery_temperature.setOnClickListener(this);
        area_day_of_week.setOnClickListener(this);
        area_period.setOnClickListener(this);


        try{
            cb_screen_locked.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_LOCKEDSCREEN])==1);
            cb_screen_unlocked.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_UNLOCKEDSCREEN])==1);
            cb_wifi_enabled.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_WIFI_ENABLED])==1);
            cb_wifi_disabled.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_WIFI_DISABLED])==1);
            cb_bluetooth_enabled.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BLUETOOTH_ENABLED])==1);
            cb_bluetooth_disabled.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BLUETOOTH_DISABLED])==1);
            cb_ring_vibrate.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_RING_VIBRATE])==1);
            cb_ring_normal.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_RING_NORMAL])==1);
            cb_ring_off.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_RING_OFF])==1);
            cb_net_enabled.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_NET_ENABLED])==1);
            cb_net_disabled.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_NET_DISABLED])==1);
            cb_gps_enabled.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_GPS_ENABLED])==1);
            cb_gps_disabled.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_GPS_DISABLED])==1);
            cb_airplane_mode_on.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_AIRPLANE_MODE_ENABLED])==1);
            cb_airplane_mode_off.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_AIRPLANE_MODE_DISABLED])==1);
        }catch (NumberFormatException ne){
            ne.printStackTrace();
            LogUtil.putExceptionLog(this,ne);
        }

        refreshBatteryPercentageView();
        refreshBatteryTemperatureView();
        refreshDayOfWeekView();
        refreshPeriodView();
    }

    @Override
    public void processMessage(Message msg){}

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            default:break;
            case R.id.exceptions_screen_locked:{
                cb_screen_locked.toggle();
                if(cb_screen_locked.isChecked()) cb_screen_unlocked.setChecked(false);
            }
            break;
            case R.id.exceptions_screen_unlocked:{
                cb_screen_unlocked.toggle();
                if(cb_screen_unlocked.isChecked()) cb_screen_locked.setChecked(false);
            }
            break;
            case R.id.exceptions_wifi_enabled:{
                cb_wifi_enabled.toggle();
                if(cb_wifi_enabled.isChecked()) cb_wifi_disabled.setChecked(false);
            }
            break;
            case R.id.exceptions_wifi_disabled:{
                cb_wifi_disabled.toggle();
                if(cb_wifi_disabled.isChecked()) cb_wifi_enabled.setChecked(false);
            }
            break;
            case R.id.exceptions_bluetooth_enabled:{
                cb_bluetooth_enabled.toggle();
                if(cb_bluetooth_enabled.isChecked()) cb_bluetooth_disabled.setChecked(false);
            }
            break;
            case R.id.exceptions_bluetooth_disabled:{
                cb_bluetooth_disabled.toggle();
                if(cb_bluetooth_disabled.isChecked()) cb_bluetooth_enabled.setChecked(false);
            }
            break;
            case R.id.exceptions_ring_vibrate:{
                cb_ring_vibrate.toggle();
                if(cb_ring_vibrate.isChecked()) {cb_ring_off.setChecked(false);cb_ring_normal.setChecked(false);}
            }
            break;
            case R.id.exceptions_ring_off:{
                cb_ring_off.toggle();
                if(cb_ring_off.isChecked()) {cb_ring_vibrate.setChecked(false);cb_ring_normal.setChecked(false);}
            }
            break;
            case R.id.exceptions_ring_normal:{
                cb_ring_normal.toggle();
                if(cb_ring_normal.isChecked()) {cb_ring_vibrate.setChecked(false);cb_ring_off.setChecked(false);}
            }
            break;
            case R.id.exceptions_net_enabled:{
                cb_net_enabled.toggle();
                if(cb_net_enabled.isChecked()) cb_net_disabled.setChecked(false);
            }
            break;
            case R.id.exceptions_net_disabled:{
                cb_net_disabled.toggle();
                if(cb_net_disabled.isChecked()) cb_net_enabled.setChecked(false);
            }
            break;
            case R.id.exceptions_gps_on:{
                cb_gps_enabled.toggle();
                if(cb_gps_enabled.isChecked()) cb_gps_disabled.setChecked(false);
            }
            break;
            case R.id.exceptions_gps_off:{
                cb_gps_disabled.toggle();
                if(cb_gps_disabled.isChecked()) cb_gps_enabled.setChecked(false);
            }
            break;
            case R.id.exceptions_airplanemode_on:{
                cb_airplane_mode_on.toggle();
                if(cb_airplane_mode_on.isChecked()) cb_airplane_mode_off.setChecked(false);
            }
            break;
            case R.id.exceptions_airplanemode_off:{
                cb_airplane_mode_off.toggle();
                if(cb_airplane_mode_off.isChecked()) cb_airplane_mode_on.setChecked(false);
            }
            break;
            case R.id.exceptions_battery_percentage:{
                final BottomDialogForBattery dialog=new BottomDialogForBattery(this);
                dialog.textview_title.setText("电池电量");
                dialog.textview_second_description.setText("%");
                String [] percentage=new String[99];
                for(int i=0;i<percentage.length;i++){int a=i+1;percentage[i]=String.valueOf(a);}
                dialog.wheelview_first.setItems(Arrays.asList(this.getResources().getString(R.string.dialog_battery_compare_more_than),this.getResources().getString(R.string.dialog_battery_compare_less_than)));
                dialog.wheelview_second.setItems(Arrays.asList(percentage));
                dialog.checkbox_enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        dialog.wheelview_first.setEnabled(b);
                        dialog.wheelview_second.setEnabled(b);
                    }
                });

                try{
                    dialog.checkbox_enable.setChecked((Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE])>=0)||
                            Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE])>=0);
                    dialog.wheelview_first.setEnabled(dialog.checkbox_enable.isChecked());
                    dialog.wheelview_second.setEnabled(dialog.checkbox_enable.isChecked());
                    dialog.wheelview_first.setSeletion(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE])>=0?0:
                            (Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE])>=0?1:0));
                    dialog.wheelview_second.setSeletion(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE])>=0?
                            (Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE])-1):
                            (Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE])>=0?
                                    (Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE])-1):
                                    49));
                }catch (NumberFormatException ne){
                    ne.printStackTrace();
                    LogUtil.putExceptionLog(this,ne);
                }

                dialog.textview_confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!dialog.checkbox_enable.isChecked()){
                            exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]=String.valueOf(-1);
                            exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]=String.valueOf(-1);
                        }
                        else{
                            if(dialog.wheelview_first.getSeletedIndex()==0){
                                exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]=dialog.wheelview_second.getSeletedItem();
                                exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]=String.valueOf(-1);
                            }else if(dialog.wheelview_first.getSeletedIndex()==1){
                                exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]=String.valueOf(-1);
                                exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]=dialog.wheelview_second.getSeletedItem();
                            }
                        }
                        dialog.cancel();
                        refreshBatteryPercentageView();
                    }
                });

                dialog.show();
            }
            break;

            case R.id.exceptions_battery_temperature:{
                final BottomDialogForBattery dialog=new BottomDialogForBattery(this);
                dialog.textview_title.setText("电池温度");
                dialog.textview_second_description.setText("℃");
                String [] temperature=new String[66];
                for(int i=0;i<temperature.length;i++) temperature[i]=String.valueOf(i);
                dialog.wheelview_first.setItems(Arrays.asList(this.getResources().getString(R.string.dialog_battery_compare_higher_than),this.getResources().getString(R.string.dialog_battery_compare_lower_than)));
                dialog.wheelview_second.setItems(Arrays.asList(temperature));
                dialog.checkbox_enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        dialog.wheelview_first.setEnabled(b);
                        dialog.wheelview_second.setEnabled(b);
                    }
                });
                try{
                    dialog.checkbox_enable.setChecked((Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE])>=0)||
                            Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE])>=0);
                    dialog.wheelview_first.setEnabled(dialog.checkbox_enable.isChecked());
                    dialog.wheelview_second.setEnabled(dialog.checkbox_enable.isChecked());
                    dialog.wheelview_first.setSeletion(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE])>=0?0:
                            (Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE])>=0?1:0));
                    dialog.wheelview_second.setSeletion(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE])>=0?
                            Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]):
                            (Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE])>=0?
                            Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]):40));
                }catch (NumberFormatException ne){
                    ne.printStackTrace();
                    LogUtil.putExceptionLog(this,ne);
                }

                dialog.textview_confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!dialog.checkbox_enable.isChecked()){
                            exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]=String.valueOf(-1);
                            exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]=String.valueOf(-1);
                        }
                        else{
                            if(dialog.wheelview_first.getSeletedIndex()==0){
                                exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]=dialog.wheelview_second.getSeletedItem();
                                exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]=String.valueOf(-1);
                            }else if(dialog.wheelview_first.getSeletedIndex()==1){
                                exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]=String.valueOf(-1);
                                exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]=dialog.wheelview_second.getSeletedItem();
                            }
                        }
                        dialog.cancel();
                        refreshBatteryTemperatureView();
                    }
                });

                dialog.show();
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
                    cb_mon.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_MONDAY])==1);
                    cb_tue.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_TUESDAY])==1);
                    cb_wed.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_WEDNESDAY])==1);
                    cb_thu.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_THURSDAY])==1);
                    cb_fri.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_FRIDAY])==1);
                    cb_sat.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_SATURDAY])==1);
                    cb_sun.setChecked(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_SUNDAY])==1);
                }catch (NumberFormatException ne){
                    ne.printStackTrace();
                    LogUtil.putExceptionLog(this,ne);
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
                            exceptions[PublicConsts.EXCEPTION_MONDAY]=cb_mon.isChecked()?String.valueOf(1):String.valueOf(0);
                            exceptions[PublicConsts.EXCEPTION_TUESDAY]=cb_tue.isChecked()?String.valueOf(1):String.valueOf(0);
                            exceptions[PublicConsts.EXCEPTION_WEDNESDAY]=cb_wed.isChecked()?String.valueOf(1):String.valueOf(0);
                            exceptions[PublicConsts.EXCEPTION_THURSDAY]=cb_thu.isChecked()?String.valueOf(1):String.valueOf(0);
                            exceptions[PublicConsts.EXCEPTION_FRIDAY]=cb_fri.isChecked()?String.valueOf(1):String.valueOf(0);
                            exceptions[PublicConsts.EXCEPTION_SATURDAY]=cb_sat.isChecked()?String.valueOf(1):String.valueOf(0);
                            exceptions[PublicConsts.EXCEPTION_SUNDAY]=cb_sun.isChecked()?String.valueOf(1):String.valueOf(0);
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
                    isEnabled=Integer.parseInt(exceptions[PublicConsts.EXCEPTION_START_TIME])!=-1&&Integer.parseInt(exceptions[PublicConsts.EXCEPTION_END_TIME])!=-1;
                    startHour=isEnabled?Integer.parseInt(exceptions[PublicConsts.EXCEPTION_START_TIME])/60:18;
                    startMin=isEnabled?Integer.parseInt(exceptions[PublicConsts.EXCEPTION_START_TIME])%60:0;
                    endHour=isEnabled?Integer.parseInt(exceptions[PublicConsts.EXCEPTION_END_TIME])/60:8;
                    endMin=isEnabled?Integer.parseInt(exceptions[PublicConsts.EXCEPTION_END_TIME])%60:0;
                }catch (NumberFormatException ne){
                    ne.printStackTrace();
                    LogUtil.putExceptionLog(this,ne);
                }
                dialog.setVariables(isEnabled,startHour,startMin,endHour,endMin);
                dialog.setTitle(getResources().getString(R.string.dialog_exceptions_period_title));
                dialog.show();
                dialog.setOnDialogConfirmedListener(new BottomDialogForPeriod.OnDialogConfirmedListener() {
                    @Override
                    public void onConfirmed(boolean isEnabled, int start_hour, int start_minute, int end_hour, int end_minute) {
                        exceptions[PublicConsts.EXCEPTION_START_TIME]=isEnabled?String.valueOf(start_hour*60+start_minute):String.valueOf(-1);
                        exceptions[PublicConsts.EXCEPTION_END_TIME]=isEnabled?String.valueOf(end_hour*60+end_minute):String.valueOf(-1);
                        refreshPeriodView();
                    }
                });

            }
            break;
        }
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            setResult(EXCEPTIONS_RESULT_CANCEL);
            finish();
        }
        return super.onKeyDown(keyCode, event);
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
                setResult(EXCEPTIONS_RESULT_CANCEL);
                finish();
            }
            break;
            case R.id.actions_exceptions_confirm:{
                exceptions[PublicConsts.EXCEPTION_LOCKEDSCREEN]=(cb_screen_locked.isChecked()?String.valueOf(1):String.valueOf(0));
                exceptions[PublicConsts.EXCEPTION_UNLOCKEDSCREEN]=(cb_screen_unlocked.isChecked()?String.valueOf(1):String.valueOf(0));
                exceptions[PublicConsts.EXCEPTION_WIFI_ENABLED]=(cb_wifi_enabled.isChecked()?String.valueOf(1):String.valueOf(0));
                exceptions[PublicConsts.EXCEPTION_WIFI_DISABLED]=(cb_wifi_disabled.isChecked()?String.valueOf(1):String.valueOf(0));
                exceptions[PublicConsts.EXCEPTION_BLUETOOTH_ENABLED]=(cb_bluetooth_enabled.isChecked()?String.valueOf(1):String.valueOf(0));
                exceptions[PublicConsts.EXCEPTION_BLUETOOTH_DISABLED]=(cb_bluetooth_disabled.isChecked()?String.valueOf(1):String.valueOf(0));
                exceptions[PublicConsts.EXCEPTION_RING_VIBRATE]=(cb_ring_vibrate.isChecked()?String.valueOf(1):String.valueOf(0));
                exceptions[PublicConsts.EXCEPTION_RING_OFF]=(cb_ring_off.isChecked()?String.valueOf(1):String.valueOf(0));
                exceptions[PublicConsts.EXCEPTION_RING_NORMAL]=(cb_ring_normal.isChecked()?String.valueOf(1):String.valueOf(0));
                exceptions[PublicConsts.EXCEPTION_NET_ENABLED]=(cb_net_enabled.isChecked()?String.valueOf(1):String.valueOf(0));
                exceptions[PublicConsts.EXCEPTION_NET_DISABLED]=(cb_net_disabled.isChecked()?String.valueOf(1):String.valueOf(0));
                exceptions[PublicConsts.EXCEPTION_GPS_ENABLED]=(cb_gps_enabled.isChecked()?String.valueOf(1):String.valueOf(0));
                exceptions[PublicConsts.EXCEPTION_GPS_DISABLED]=(cb_gps_disabled.isChecked()?String.valueOf(1):String.valueOf(0));
                exceptions[PublicConsts.EXCEPTION_AIRPLANE_MODE_ENABLED]=(cb_airplane_mode_on.isChecked()?String.valueOf(1):String.valueOf(0));
                exceptions[PublicConsts.EXCEPTION_AIRPLANE_MODE_DISABLED]=(cb_airplane_mode_off.isChecked()?String.valueOf(1):String.valueOf(0));
                Intent i=new Intent();
                i.putExtra(INTENT_EXTRA_EXCEPTIONS,this.exceptions);
                setResult(EXCEPTIONS_RESULT_SUCCESS,i);
                finish();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshBatteryPercentageView(){
        TextView value=this.findViewById(R.id.exceptions_battery_percentage_value);
        try{
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE])>=0){
                value.setText(this.getResources().getString(R.string.dialog_battery_compare_less_than)+exceptions[PublicConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]+"%");
            }else if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE])>=0){
                value.setText(this.getResources().getString(R.string.dialog_battery_compare_more_than)+exceptions[PublicConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]+"%");
            }else {
                value.setText(this.getResources().getString(R.string.dialog_battery_not_enabled));
            }
        }catch (NumberFormatException ne){
            ne.printStackTrace();
            LogUtil.putExceptionLog(this,ne);
        }

    }

    private void refreshBatteryTemperatureView(){
        TextView value=this.findViewById(R.id.exceptions_battery_temperature_value);
        try{
            if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE])>=0){
                value.setText(this.getResources().getString(R.string.dialog_battery_compare_lower_than)+exceptions[PublicConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]+"℃");
            }else if(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE])>=0){
                value.setText(this.getResources().getString(R.string.dialog_battery_compare_higher_than)+exceptions[PublicConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]+"℃");
            }else {
                value.setText(this.getResources().getString(R.string.dialog_battery_not_enabled));
            }
        }catch (NumberFormatException ne){
            ne.printStackTrace();
            LogUtil.putExceptionLog(this,ne);
        }

    }

    private void refreshDayOfWeekView(){
        TextView tv_value=findViewById(R.id.exceptions_day_of_week_value);
        StringBuilder value=new StringBuilder("");
        try{
            value.append(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_MONDAY])==1?getResources().getString(R.string.monday)+" ":"");
            value.append(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_TUESDAY])==1?getResources().getString(R.string.tuesday)+" ":"");
            value.append(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_WEDNESDAY])==1?getResources().getString(R.string.wednesday)+" ":"");
            value.append(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_THURSDAY])==1?getResources().getString(R.string.thursday)+" ":"");
            value.append(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_FRIDAY])==1?getResources().getString(R.string.friday)+" ":"");
            value.append(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_SATURDAY])==1?getResources().getString(R.string.saturday)+' ':"");
            value.append(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_SUNDAY])==1?getResources().getString(R.string.sunday)+" ":"");
            tv_value.setText(value.toString().equals("")?getResources().getString(R.string.not_activated):value.toString());
        }catch (NumberFormatException ne){
            ne.printStackTrace();
            LogUtil.putExceptionLog(this,ne);
        }

    }

    private void refreshPeriodView(){
        TextView tv_value=findViewById(R.id.exceptions_period_value);
        try{
            tv_value.setText((Integer.parseInt(exceptions[PublicConsts.EXCEPTION_START_TIME])!=-1&&Integer.parseInt(exceptions[PublicConsts.EXCEPTION_END_TIME])!=-1)?
                    ValueUtils.format(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_START_TIME])/60)+":"+ ValueUtils.format(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_START_TIME])%60)+"~"+ ValueUtils.format(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_END_TIME])/60)+":"+ ValueUtils.format(Integer.parseInt(exceptions[PublicConsts.EXCEPTION_END_TIME])%60)
                    :getResources().getString(R.string.not_activated));
        }catch (NumberFormatException ne){
            ne.printStackTrace();
            LogUtil.putExceptionLog(this,ne);
        }

    }

}
