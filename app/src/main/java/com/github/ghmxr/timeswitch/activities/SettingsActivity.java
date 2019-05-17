package com.github.ghmxr.timeswitch.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
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
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.ui.DialogForColor;
import com.github.ghmxr.timeswitch.utils.RootUtils;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class SettingsActivity extends BaseActivity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener{

    SharedPreferences settings;

    public static final int RESULT_CHANGED_INDICATOR_STATE =0x00010;

    boolean flag_restart_main=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_settings);
        Toolbar toolbar=findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolBarAndStatusBarColor(toolbar,getIntent().getStringExtra(EXTRA_TITLE_COLOR));
        settings=getSharedPreferences(PublicConsts.PREFERENCES_NAME, Activity.MODE_PRIVATE);
        //editor=settings.edit();

        findViewById(R.id.settings_api).setOnClickListener(this);
        findViewById(R.id.settings_autostart).setOnClickListener(this);
        findViewById(R.id.settings_indicator).setOnClickListener(this);
        findViewById(R.id.settings_animator).setOnClickListener(this);
        findViewById(R.id.settings_log).setOnClickListener(this);
        findViewById(R.id.settings_privilege).setOnClickListener(this);
        findViewById(R.id.settings_superuser).setOnClickListener(this);
        findViewById(R.id.settings_about).setOnClickListener(this);
        findViewById(R.id.settings_color).setOnClickListener(this);
        findViewById(R.id.settings_service_type).setOnClickListener(this);

        CheckBox cb_autostart=findViewById(R.id.settings_autostart_cb);
        CheckBox cb_indicator=findViewById(R.id.settings_indicator_cb);
        CheckBox cb_superuser=findViewById(R.id.settings_superuser_cb);
        CheckBox cb_animator=findViewById(R.id.settings_animator_cb);

        cb_autostart.setChecked(settings.getBoolean(PublicConsts.PREFERENCES_AUTO_START,PublicConsts.PREFERENCES_AUTO_START_DEFAULT));
        cb_indicator.setChecked(settings.getBoolean(PublicConsts.PREFERENCES_MAINPAGE_INDICATOR,PublicConsts.PREFERENCES_MAINPAGE_INDICATOR_DEFAULT));
        cb_superuser.setChecked(settings.getBoolean(PublicConsts.PREFERENCES_IS_SUPERUSER_MODE,PublicConsts.PREFERENCES_IS_SUPERUSER_MODE_DEFAULT));
        cb_animator.setChecked(settings.getBoolean(PublicConsts.PREFERENCE_DISABLE_ANIMATION_EFFECTS,PublicConsts.PREFERENCE_DISABLE_ANIMATION_EFFECTS_DEFAULT));

        ((TextView)findViewById(R.id.settings_color_value)).setText(settings.getString(PublicConsts.PREFERENCES_THEME_COLOR,PublicConsts.PREFERENCES_THEME_COLOR_DEFAULT));
        try{
            ((TextView)findViewById(R.id.settings_color_value)).setTextColor(Color.parseColor(settings.getString(PublicConsts.PREFERENCES_THEME_COLOR,PublicConsts.PREFERENCES_THEME_COLOR_DEFAULT)));
        }catch (Exception e){e.printStackTrace();}
        cb_autostart.setOnCheckedChangeListener(this);
        cb_indicator.setOnCheckedChangeListener(this);
        cb_superuser.setOnCheckedChangeListener(this);
        cb_animator.setOnCheckedChangeListener(this);
        refreshAPIValue();
        refreshServiceTypeValue();
    }

    @Override
    public void processMessage(Message msg){}

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
                        TimeSwitchService.sendEmptyMessage(TimeSwitchService.MESSAGE_REQUEST_REFRESH_TASKS);
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
                setResult(RESULT_CHANGED_INDICATOR_STATE);
            }
            break;
            case R.id.settings_animator:{
                ((CheckBox)findViewById(R.id.settings_animator_cb)).toggle();
            }
            break;
            case R.id.settings_log:{
                Intent i=new Intent(this, LogActivity.class);
                i.putExtra(EXTRA_TITLE_COLOR,getIntent().getStringExtra(EXTRA_TITLE_COLOR));
                startActivity(i);
            }
            break;
            case R.id.settings_privilege:{
                Intent i=new Intent(this,PrivilegeTestActivity.class);
                i.putExtra(EXTRA_TITLE_COLOR,getIntent().getStringExtra(EXTRA_TITLE_COLOR));
                startActivity(i);
            }
            break;
            case R.id.settings_superuser:{
                CheckBox cb_superuser=findViewById(R.id.settings_superuser_cb);
                cb_superuser.toggle();
            }
            break;
            case R.id.settings_about:{
                Intent i=new Intent(this,About.class);
                i.putExtra(EXTRA_TITLE_COLOR,getIntent().getStringExtra(EXTRA_TITLE_COLOR));
                startActivity(i);
            }
            break;
            case R.id.settings_color:{
                DialogForColor dialog=new DialogForColor(this,settings.getString(PublicConsts.PREFERENCES_THEME_COLOR,PublicConsts.PREFERENCES_THEME_COLOR_DEFAULT));
                dialog.setTitle(getResources().getString(R.string.activity_settings_color_att));
                dialog.show();
                dialog.setOnDialogConfirmListener(new DialogForColor.OnDialogForColorConfirmedListener() {
                    @Override
                    public void onConfirmed(String color) {
                        try{
                            editor.putString(PublicConsts.PREFERENCES_THEME_COLOR,color);
                            editor.apply();
                            ((TextView)findViewById(R.id.settings_color_value)).setText(color);
                            ((TextView)findViewById(R.id.settings_color_value)).setTextColor(Color.parseColor(color));
                            setToolBarAndStatusBarColor(findViewById(R.id.settings_toolbar),color);
                            getIntent().putExtra(EXTRA_TITLE_COLOR,color);
                            flag_restart_main=true;
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
            break;
            case R.id.settings_service_type:{
                final AlertDialog dialog=new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.activity_settings_service_type))
                        .setView(LayoutInflater.from(this).inflate(R.layout.layout_dialog_with_two_single_choices,null))
                        .show();
                RadioButton ra_background=dialog.findViewById(R.id.dialog_choice_first);
                RadioButton ra_foreground=dialog.findViewById(R.id.dialog_choice_second);
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                    TextView tv=dialog.findViewById(R.id.dialog_two_choices_att);
                    tv.setVisibility(View.VISIBLE);
                    tv.setTextColor(getResources().getColor(R.color.color_activity_taskgui_att));
                    tv.setText(getResources().getString(R.string.activity_settings_service_type_att));
                    tv.setClickable(true);
                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i=new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            i.setData(Uri.fromParts("package", getApplication().getPackageName(), null));
                            startActivity(i);
                        }
                    });
                }
                ra_background.setText(getResources().getString(R.string.activity_settings_service_type_background));
                ra_foreground.setText(getResources().getString(R.string.activity_settings_service_type_foreground));
                ra_background.setChecked(settings.getInt(PublicConsts.PREFERENCES_SERVICE_TYPE,PublicConsts.PREFERENCES_SERVICE_TYPE_DEFAULT)==PublicConsts.PREFERENCES_SERVICE_TYPE_BACKGROUND);
                ra_foreground.setChecked(settings.getInt(PublicConsts.PREFERENCES_SERVICE_TYPE,PublicConsts.PREFERENCES_SERVICE_TYPE_DEFAULT)==PublicConsts.PREFERENCES_SERVICE_TYPE_FOREGROUND);
                ra_background.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editor.putInt(PublicConsts.PREFERENCES_SERVICE_TYPE,PublicConsts.PREFERENCES_SERVICE_TYPE_BACKGROUND);
                        editor.apply();
                        dialog.cancel();
                        refreshServiceTypeValue();
                        TimeSwitchService.sendEmptyMessage(TimeSwitchService.MESSAGE_REQUEST_REFRESH_TASKS);
                    }
                });
                ra_foreground.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editor.putInt(PublicConsts.PREFERENCES_SERVICE_TYPE,PublicConsts.PREFERENCES_SERVICE_TYPE_FOREGROUND);
                        editor.apply();
                        dialog.cancel();
                        refreshServiceTypeValue();
                        TimeSwitchService.sendEmptyMessage(TimeSwitchService.MESSAGE_REQUEST_REFRESH_TASKS);
                    }
                });

            }
            break;
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
                MainActivity.sendEmptyMessage(b? MainActivity.MESSAGE_SHOW_INDICATOR: MainActivity.MESSAGE_HIDE_INDICATOR);
                editor.putBoolean(PublicConsts.PREFERENCES_MAINPAGE_INDICATOR,b);
                editor.apply();
            }
            break;
            case R.id.settings_superuser_cb:{
                final CheckBox cb_superuser=SettingsActivity.this.findViewById(R.id.settings_superuser_cb);
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
                            int result=RootUtils.executeCommand("\n");
                            Log.i("RootCommand","result is "+result);
                            if(result==RootUtils.ROOT_COMMAND_RESULT_SUCCESS){
                                editor.putBoolean(PublicConsts.PREFERENCES_IS_SUPERUSER_MODE,true);
                                editor.apply();
                                cb_superuser.setChecked(true);
                                Toast.makeText(SettingsActivity.this,getResources().getString(R.string.activity_settings_toast_get_superuser_success),Toast.LENGTH_SHORT).show();
                            }else{
                                cb_superuser.setChecked(false);
                                Toast.makeText(SettingsActivity.this,getResources().getString(R.string.activity_settings_toast_get_superuser_fail),Toast.LENGTH_SHORT).show();
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
            case R.id.settings_animator_cb:{
                editor.putBoolean(PublicConsts.PREFERENCE_DISABLE_ANIMATION_EFFECTS, b);
                editor.apply();
            }
            break;
        }
    }

    private void refreshAPIValue(){
        TextView tv_api_value=findViewById(R.id.settings_api_value);
        int api_type=settings.getInt(PublicConsts.PREFERENCES_API_TYPE,PublicConsts.PREFERENCES_API_TYPE_DEFAULT);
        tv_api_value.setText(api_type==PublicConsts.API_ANDROID_ALARM_MANAGER?"Android Alarm Manager":(api_type==PublicConsts.API_JAVA_TIMER?"Java Timer":""));
    }

    private void refreshServiceTypeValue(){
        TextView tv=((TextView)findViewById(R.id.settings_service_type_value));
        int type=getSharedPreferences(PublicConsts.PREFERENCES_NAME,Activity.MODE_PRIVATE).getInt(PublicConsts.PREFERENCES_SERVICE_TYPE,PublicConsts.PREFERENCES_SERVICE_TYPE_DEFAULT);
        if(type==PublicConsts.PREFERENCES_SERVICE_TYPE_BACKGROUND){
            tv.setText(getResources().getString(R.string.activity_settings_service_type_background));
        }
        if(type==PublicConsts.PREFERENCES_SERVICE_TYPE_FOREGROUND){
            tv.setText(getResources().getString(R.string.activity_settings_service_type_foreground));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id =item.getItemId();
        if(id==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish(){
        super.finish();
        if(flag_restart_main){
            while (BaseActivity.queue!=null&&BaseActivity.queue.size()>0) BaseActivity.queue.getLast().finish();
            startActivity(new Intent(this,MainActivity.class));
        }
    }

}
