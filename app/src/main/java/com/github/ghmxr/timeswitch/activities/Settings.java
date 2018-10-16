package com.github.ghmxr.timeswitch.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.utils.RootUtils;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class Settings extends BaseActivity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener{

    SharedPreferences settings;
    //SharedPreferences.Editor editor;
    public static final int MESSAGE_CHANGE_API_COMPLETE=0x10000;

    public static final int RESULT_CHAGED_INDICATOR_STATE=0x00010;

    private AlertDialog waitDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_settings);
        Toolbar toolbar=findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settings=getSharedPreferences(PublicConsts.PREFERENCES_NAME, Activity.MODE_PRIVATE);
        //editor=settings.edit();

        findViewById(R.id.settings_api).setOnClickListener(this);
        findViewById(R.id.settings_autostart).setOnClickListener(this);
        findViewById(R.id.settings_indicator).setOnClickListener(this);
        findViewById(R.id.settings_log).setOnClickListener(this);
        findViewById(R.id.settings_superuser).setOnClickListener(this);
        findViewById(R.id.settings_about).setOnClickListener(this);

        CheckBox cb_autostart=findViewById(R.id.settings_autostart_cb);
        CheckBox cb_indicator=findViewById(R.id.settings_indicator_cb);
        CheckBox cb_superuser=findViewById(R.id.settings_superuser_cb);

        cb_autostart.setChecked(settings.getBoolean(PublicConsts.PREFERENCES_AUTO_START,PublicConsts.PREFERENCES_AUTO_START_DEFAULT));
        cb_indicator.setChecked(settings.getBoolean(PublicConsts.PREFERENCES_MAINPAGE_INDICATOR,PublicConsts.PREFERENCES_MAINPAGE_INDICATOR_DEFAULT));
        cb_superuser.setChecked(settings.getBoolean(PublicConsts.PREFERENCES_IS_SUPERUSER_MODE,PublicConsts.PREFERENCES_IS_SUPERUSER_MODE_DEFAULT));

        cb_autostart.setOnCheckedChangeListener(this);
        cb_indicator.setOnCheckedChangeListener(this);
        cb_superuser.setOnCheckedChangeListener(this);

        refreshAPIValue();
    }

    @Override
    public void processMessage(Message msg){
        switch(msg.what){
            default:break;
            case MESSAGE_CHANGE_API_COMPLETE:{
                if(waitDialog!=null){
                    waitDialog.cancel();
                    waitDialog=null;
                }
            }
            break;
        }
    }

    @Override
    public void onClick(View v){
        final SharedPreferences.Editor editor=settings.edit();
        switch (v.getId()){
            default:break;
            case R.id.settings_api:{
                View dialogview=LayoutInflater.from(this).inflate(R.layout.layout_dialog_api,null);
                final AlertDialog dialog=new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_api_title))
                        .setView(dialogview)
                        .setPositiveButton(getResources().getString(R.string.dialog_button_positive),null)
                        .setNegativeButton(getResources().getString(R.string.dialog_button_negative),null)
                        .show();
                final RadioButton ra_alarm=dialogview.findViewById(R.id.dialog_api_alarm_manager_ra);
                final RadioButton ra_java_timer=dialogview.findViewById(R.id.dialog_api_java_timer_ra);
                int api=settings.getInt(PublicConsts.PREFERENCES_API_TYPE,PublicConsts.PREFERENCES_API_TYPE_DEFAULT);
                ra_alarm.setChecked(api==PublicConsts.API_ANDROID_ALARM_MANAGER);
                ra_java_timer.setChecked(api==PublicConsts.API_JAVA_TIMER);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int selected_api=0;
                        if(ra_alarm.isChecked()) selected_api=PublicConsts.API_ANDROID_ALARM_MANAGER;
                        else if(ra_java_timer.isChecked()) selected_api=PublicConsts.API_JAVA_TIMER;
                        editor.putInt(PublicConsts.PREFERENCES_API_TYPE,selected_api);
                        editor.apply();

                        dialog.cancel();
                        refreshAPIValue();
                        waitDialog=new AlertDialog.Builder(Settings.this).setView(LayoutInflater.from(Settings.this).inflate(R.layout.layout_dialog_wait,null))
                                .setCancelable(false)
                                .create();
                        waitDialog.show();

                        //if(Main.queue.size()>0) Main.queue.getLast().startService2Refresh();
                        if(TimeSwitchService.service_queue.size()>0) TimeSwitchService.service_queue.getLast().refreshTaskItems();
                        else Settings.this.startService(new Intent(Settings.this,TimeSwitchService.class));
                    }
                });
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        dialog.cancel();
                    }
                });
            }
            break;
            case R.id.settings_autostart:{
                CheckBox cb=findViewById(R.id.settings_autostart_cb);
                cb.toggle();
            }
            break;
            case R.id.settings_indicator:{
                CheckBox cb_indicator=findViewById(R.id.settings_indicator_cb);
                cb_indicator.toggle();
                setResult(RESULT_CHAGED_INDICATOR_STATE);
            }
            break;
            case R.id.settings_log:{
                startActivity(new Intent(this, com.github.ghmxr.timeswitch.activities.Log.class));
            }
            break;
            case R.id.settings_superuser:{
                CheckBox cb_superuser=findViewById(R.id.settings_superuser_cb);
                cb_superuser.toggle();
            }
            break;
            case R.id.settings_about:{
                startActivity(new Intent(this,About.class));
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        final SharedPreferences.Editor editor=settings.edit();
        switch (compoundButton.getId()){
            default:break;
            case R.id.settings_autostart_cb:{
                editor.putBoolean(PublicConsts.PREFERENCES_AUTO_START,b);
                editor.apply();
            }
            break;
            case R.id.settings_indicator_cb:{
                Main.sendEmptyMessage(b?Main.MESSAGE_SHOW_INDICATOR:Main.MESSAGE_HIDE_INDICATOR);
                editor.putBoolean(PublicConsts.PREFERENCES_MAINPAGE_INDICATOR,b);
                editor.apply();
            }
            break;
            case R.id.settings_superuser_cb:{
                final CheckBox cb_superuser=Settings.this.findViewById(R.id.settings_superuser_cb);
                if(b&&RootUtils.isDeviceRooted()){
                    final AlertDialog dialog=new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle(getResources().getString(R.string.dialog_get_superuser_att))
                            .setMessage(getResources().getString(R.string.dialog_get_superuser_message))
                            .setPositiveButton(getResources().getString(R.string.dialog_get_superuser_button_confirm),null)
                            .setNegativeButton(getResources().getString(R.string.dialog_button_negative),null)
                            .show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                            int result=RootUtils.executeCommand(RootUtils.COMMAND_GRANT_SECURE_PERMISSION);
                            Log.i("RootCommand","result is "+result);
                            if(result==RootUtils.ROOT_COMMAND_RESULT_SUCCESS){
                                editor.putBoolean(PublicConsts.PREFERENCES_IS_SUPERUSER_MODE,true);
                                editor.apply();
                                cb_superuser.setChecked(true);
                                Toast.makeText(Settings.this,getResources().getString(R.string.activity_settings_toast_get_superuser_success),Toast.LENGTH_SHORT).show();
                            }else{
                                cb_superuser.setChecked(false);
                                Toast.makeText(Settings.this,getResources().getString(R.string.activity_settings_toast_get_superuser_fail),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            cb_superuser.setChecked(false);
                            dialog.cancel();
                        }
                    });
                }else if(b&&!RootUtils.isDeviceRooted()){
                    cb_superuser.setChecked(false);
                    Snackbar.make(findViewById(R.id.settings_root),getResources().getString(R.string.activity_settings_superuser_not_available),Snackbar.LENGTH_SHORT).show();
                }else {
                    editor.putBoolean(PublicConsts.PREFERENCES_IS_SUPERUSER_MODE,false);
                    editor.apply();
                }
            }
            break;
        }
    }

    private void refreshAPIValue(){
        TextView tv_api_value=findViewById(R.id.settings_api_value);
        int api_type=settings.getInt(PublicConsts.PREFERENCES_API_TYPE,PublicConsts.PREFERENCES_API_TYPE_DEFAULT);
        tv_api_value.setText(api_type==PublicConsts.API_ANDROID_ALARM_MANAGER?"Android Alarm Manager":(api_type==PublicConsts.API_JAVA_TIMER?"Java Timer":""));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id =item.getItemId();
        if(id==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
