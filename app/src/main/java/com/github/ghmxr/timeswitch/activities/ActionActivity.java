package com.github.ghmxr.timeswitch.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.adapters.AppListAdapter;
import com.github.ghmxr.timeswitch.data.v2.ActionConsts;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.ui.ActionDisplayValue;
import com.github.ghmxr.timeswitch.ui.BottomDialog;
import com.github.ghmxr.timeswitch.ui.BottomDialogForBrightness;
import com.github.ghmxr.timeswitch.ui.BottomDialogForDeviceControl;
import com.github.ghmxr.timeswitch.ui.BottomDialogForRingMode;
import com.github.ghmxr.timeswitch.ui.BottomDialogForVibrate;
import com.github.ghmxr.timeswitch.ui.BottomDialogForVolume;
import com.github.ghmxr.timeswitch.ui.BottomDialogWith2Selections;
import com.github.ghmxr.timeswitch.ui.BottomDialogWith3Selections;
import com.github.ghmxr.timeswitch.ui.DialogConfirmedCallBack;
import com.github.ghmxr.timeswitch.utils.LogUtil;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */

public class ActionActivity extends BaseActivity implements View.OnClickListener{
    public static final String EXTRA_TASK_ID="taskid";
    public static final String EXTRA_ACTIONS="actions";
    public static final String EXTRA_ACTION_URI_RING_NOTIFICATION="uri_ring_notification";
    public static final String EXTRA_ACTION_URI_RING_CALL="uri_ring_call";
    public static final String EXTRA_ACTION_URI_WALLPAPER_DESKTOP ="uri_wallpaper";
    public static final String EXTRA_ACTION_NOTIFICATION_TITLE="notification_title";
    public static final String EXTRA_ACTION_NOTIFICATION_MESSAGE="notification_message";
    public static final String EXTRA_ACTION_TOAST="toast";
    public static final String EXTRA_ACTION_SMS_ADDRESS="sms_address";
    public static final String EXTRA_ACTION_SMS_MESSAGE="sms_message";
    private static final int REQUEST_CODE_RING_CHANGED=1;
    private static final int REQUEST_CODE_WALLPAPER_CHANGED=2;
    private static final int REQUEST_CODE_SMS_SET=3;
    //boolean isItemClicked=false;
    String [] actions=new String[ActionConsts.ACTION_LENGTH];
    String uri_ring_notification="",uri_ring_call="",
            uri_wallpaper="",
            notification_title="",notification_message="",toast="",
            sms_address="",sms_message="";
    String checkString ="";
    private long first_clicked_back_time=0;
    private int taskid=-1;
    private static final int TASK_ENABLE=0;
    private static final int TASK_DISABLE=1;
    private static final int MESSAGE_GET_LIST_OPEN_COMPLETE=101;
    private static final int MESSAGE_GET_LIST_CLOSE_COMPLETE=102;

    private AlertDialog dialog_app_oc;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_actions);
        Toolbar toolbar=findViewById(R.id.actions_toolbar);
        setSupportActionBar(toolbar);
        try{getSupportActionBar().setDisplayHomeAsUpEnabled(true);}
        catch (Exception e){e.printStackTrace();}
        setToolBarAndStatusBarColor(toolbar,getIntent().getStringExtra(EXTRA_TITLE_COLOR));
        findViewById(R.id.actions_wifi).setOnClickListener(this);
        findViewById(R.id.actions_bluetooth).setOnClickListener(this);
        findViewById(R.id.actions_ring_mode).setOnClickListener(this);
        findViewById(R.id.actions_ring_volume).setOnClickListener(this);
        findViewById(R.id.actions_ring_selection).setOnClickListener(this);
        findViewById(R.id.actions_brightness).setOnClickListener(this);
        findViewById(R.id.actions_wallpaper).setOnClickListener(this);
        findViewById(R.id.actions_vibrate).setOnClickListener(this);
        findViewById(R.id.actions_sms).setOnClickListener(this);
        findViewById(R.id.actions_notification).setOnClickListener(this);
        findViewById(R.id.actions_toast).setOnClickListener(this);
        findViewById(R.id.actions_net).setOnClickListener(this);
        findViewById(R.id.actions_gps).setOnClickListener(this);
        findViewById(R.id.actions_airplane_mode).setOnClickListener(this);
        findViewById(R.id.actions_devicecontrol).setOnClickListener(this);
        findViewById(R.id.actions_enable).setOnClickListener(this);
        findViewById(R.id.actions_disable).setOnClickListener(this);
        findViewById(R.id.actions_app_open).setOnClickListener(this);
        findViewById(R.id.actions_app_close).setOnClickListener(this);
        findViewById(R.id.actions_app_force_close).setOnClickListener(this);
        findViewById(R.id.actions_autorotation).setOnClickListener(this);
        try{
            Intent data=getIntent();
            actions=data.getStringArrayExtra(EXTRA_ACTIONS);
            taskid=data.getIntExtra(EXTRA_TASK_ID,-1);
            uri_ring_notification=data.getStringExtra(EXTRA_ACTION_URI_RING_NOTIFICATION);
            uri_ring_call=data.getStringExtra(EXTRA_ACTION_URI_RING_CALL);
            uri_wallpaper=data.getStringExtra(EXTRA_ACTION_URI_WALLPAPER_DESKTOP);
            notification_title=data.getStringExtra(EXTRA_ACTION_NOTIFICATION_TITLE);
            notification_message=data.getStringExtra(EXTRA_ACTION_NOTIFICATION_MESSAGE);
            toast=data.getStringExtra(EXTRA_ACTION_TOAST);
            sms_address=data.getStringExtra(EXTRA_ACTION_SMS_ADDRESS);
            sms_message=data.getStringExtra(EXTRA_ACTION_SMS_MESSAGE);
            checkString = toCheckString();
            refreshActionStatus();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void processMessage(Message msg) {
        final int msg_what=msg.what;
        switch(msg_what){
            default:break;
            case MESSAGE_GET_LIST_OPEN_COMPLETE: case MESSAGE_GET_LIST_CLOSE_COMPLETE:{
                if(dialog_app_oc==null) return;
                dialog_app_oc.findViewById(R.id.dialog_app_wait).setVisibility(View.GONE);
                dialog_app_oc.findViewById(R.id.dialog_app_list_area).setVisibility(View.VISIBLE);
                String [] selectedPackages=new String[1];
                try{
                    selectedPackages=actions[msg_what==MESSAGE_GET_LIST_OPEN_COMPLETE? ActionConsts.ActionFirstLevelLocaleConsts.ACTION_LAUNCH_APP_PACKAGES: ActionConsts.ActionFirstLevelLocaleConsts.ACTION_STOP_APP_PACKAGES].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                    if(Integer.parseInt(selectedPackages[0])<0) selectedPackages=new String[1];
                }catch (Exception e){
                    //e.printStackTrace();
                    //LogUtil.putExceptionLog(this,e);
                }
                final AppListAdapter adapter=new AppListAdapter(this,(List<AppListAdapter.AppItemInfo>)msg.obj,selectedPackages);
                ((ListView)dialog_app_oc.findViewById(R.id.dialog_app_list)).setAdapter(adapter);
                dialog_app_oc.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String [] package_names=adapter.getSelectedPackageNames();
                        StringBuilder return_value=new StringBuilder("");
                        if(package_names.length>0){
                            for(int i=0;i<package_names.length;i++){
                                return_value.append(package_names[i]);
                                if(i<(package_names.length-1)) return_value.append(PublicConsts.SEPARATOR_SECOND_LEVEL);
                            }
                        }else return_value=new StringBuilder(String.valueOf(-1));
                        actions[msg_what==MESSAGE_GET_LIST_OPEN_COMPLETE? ActionConsts.ActionFirstLevelLocaleConsts.ACTION_LAUNCH_APP_PACKAGES: ActionConsts.ActionFirstLevelLocaleConsts.ACTION_STOP_APP_PACKAGES]=return_value.toString();
                        dialog_app_oc.cancel();
                        refreshActionStatus();
                    }
                });
                dialog_app_oc.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.deselectAll();
                    }
                });
                try{
                    if(msg_what==MESSAGE_GET_LIST_OPEN_COMPLETE){
                        dialog_app_oc.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PackageManager pm=getPackageManager();
                                for(String s:adapter.getSelectedPackageNames()){
                                    try{
                                        Intent i=pm.getLaunchIntentForPackage(s);
                                        startActivity(i);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                        Toast.makeText(ActionActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                    }
                }catch (Exception e){e.printStackTrace();}
                ((ListView)dialog_app_oc.findViewById(R.id.dialog_app_list)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        adapter.onItemClicked(position);
                    }
                });
            }
            break;
        }
    }

    @TargetApi(23)
    private void showRequestWriteSettingsPermissionSnackbar(){
        Snackbar snackbar=Snackbar.make(findViewById(R.id.layout_actions_root),getResources().getString(R.string.permission_request_write_settings_message),Snackbar.LENGTH_SHORT);
        snackbar.setAction(getResources().getString(R.string.permission_grant_action_att), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS));
                Toast.makeText(ActionActivity.this,getResources().getString(R.string.permission_request_write_settings_toast),Toast.LENGTH_SHORT).show();
            }
        });
        snackbar.show();
    }

    @TargetApi(23)
    private void showRequestAccessNotificationPolicyPermissionSnackbar(){
        Snackbar snackbar=Snackbar.make(findViewById(R.id.layout_actions_root),getResources().getString(R.string.permission_request_notification_policy_message),Snackbar.LENGTH_SHORT);
        snackbar.setAction(getResources().getString(R.string.permission_grant_action_att), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
                Toast.makeText(ActionActivity.this,getResources().getString(R.string.permission_request_notification_policy_toast),Toast.LENGTH_SHORT).show();
            }
        });
        snackbar.show();
    }


    @Override
    public void onClick(View view) {
        //isItemClicked=true;
        switch (view.getId()){
           default:break;
            case R.id.actions_wifi:{
                /*if(PermissionChecker.checkSelfPermission(this,Manifest.permission.CHANGE_WIFI_STATE)!=PermissionChecker.PERMISSION_GRANTED){
                    Snackbar snackbar=Snackbar.make(findViewById(R.id.layout_actions_root),"",Snackbar.LENGTH_SHORT);
                    snackbar.setAction(getResources().getString(R.string.permission_grant_action_att), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent appdetail = new Intent();
                            appdetail.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            appdetail.setData(Uri.fromParts("package", getApplication().getPackageName(), null));
                            startActivity(appdetail);
                        }
                    });
                    snackbar.show();
                    return;
                }*/
                //showNormalBottomDialog(view.getId());
                BottomDialogWith3Selections dialog=new BottomDialogWith3Selections(this,R.drawable.icon_wifi_on
                        ,R.drawable.icon_wifi_off
                        ,Integer.parseInt(actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_WIFI_LOCALE]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_WIFI_LOCALE]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_bluetooth:{
                //showNormalBottomDialog(view.getId());
                BottomDialogWith3Selections dialog=new BottomDialogWith3Selections(this,R.drawable.icon_bluetooth_on
                        ,R.drawable.icon_bluetooth_off
                        ,Integer.parseInt(actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BLUETOOTH_LOCALE]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BLUETOOTH_LOCALE]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_ring_mode: {
                if(Build.VERSION.SDK_INT>=24){
                    NotificationManager manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                    if(manager==null) {
                        Log.e("Actions","Can not get NotificationManager instance");
                        Toast.makeText(this,"Can not get NotificationManager instance",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(!manager.isNotificationPolicyAccessGranted()){
                        showRequestAccessNotificationPolicyPermissionSnackbar();
                        return;
                    }
                }
                //showNormalBottomDialog(view.getId());
                BottomDialogForRingMode dialog=new BottomDialogForRingMode(this,Integer.parseInt(actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_MODE_LOCALE]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_MODE_LOCALE]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_net:{
                BottomDialogWith3Selections dialog=new BottomDialogWith3Selections(this,R.drawable.icon_cellular_on,R.drawable.icon_cellular_off
                ,Integer.parseInt(actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NET_LOCALE]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NET_LOCALE]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_gps: {
                BottomDialogWith3Selections dialog=new BottomDialogWith3Selections(this,R.drawable.icon_location_on,R.drawable.icon_location_off
                        ,Integer.parseInt(actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_GPS_LOCALE]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_GPS_LOCALE]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_airplane_mode:{
                BottomDialogWith3Selections dialog=new BottomDialogWith3Selections(this,R.drawable.icon_airplanemode_on,R.drawable.icon_airplanemode_off
                        ,Integer.parseInt(actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AIRPLANE_MODE_LOCALE]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AIRPLANE_MODE_LOCALE]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_devicecontrol:{
                boolean isRoot=getSharedPreferences(PublicConsts.PREFERENCES_NAME, Activity.MODE_PRIVATE).getBoolean(PublicConsts.PREFERENCES_IS_SUPERUSER_MODE,PublicConsts.PREFERENCES_IS_SUPERUSER_MODE_DEFAULT);
                if(!isRoot){
                    showSnackBarOfSuperuserRequest();
                    return;
                }
                BottomDialogForDeviceControl dialog=new BottomDialogForDeviceControl(this,Integer.parseInt(actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DEVICE_CONTROL_LOCALE]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DEVICE_CONTROL_LOCALE]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_brightness:{
                //Manifest.permission.WRITE_SETTINGS
                if(Build.VERSION.SDK_INT>=23&&!android.provider.Settings.System.canWrite(this)){
                    showRequestWriteSettingsPermissionSnackbar();
                    return;
                }
                BottomDialogForBrightness dialog=new BottomDialogForBrightness(this,Integer.parseInt(actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BRIGHTNESS_LOCALE]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BRIGHTNESS_LOCALE]=String.valueOf(result);
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_ring_volume:{
                if(Build.VERSION.SDK_INT>=24){
                    NotificationManager manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                    if(manager==null){
                        Toast.makeText(this,"Can not get NotificationManager instance",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(!manager.isNotificationPolicyAccessGranted()){
                        showRequestAccessNotificationPolicyPermissionSnackbar();
                        return;
                    }
                }
                AudioManager audioManager=(AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if(audioManager==null){
                    Toast.makeText(this,"Can not get AudioManager instance",Toast.LENGTH_SHORT).show();
                    return;
                }
                BottomDialogForVolume dialog=new BottomDialogForVolume(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_VOLUME_LOCALE]);
                dialog.show();
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_VOLUME_LOCALE]=result;
                        refreshActionStatus();
                    }
                });

            }
            break;
            case R.id.actions_ring_selection:{
                if(Build.VERSION.SDK_INT>=23&&!android.provider.Settings.System.canWrite(this)){
                    showRequestWriteSettingsPermissionSnackbar();
                    return;
                }
                Intent intent = new Intent();
                intent.setClass(this,ActionOfChangingRingtones.class);
                intent.putExtra(ActionOfChangingRingtones.EXTRA_RING_VALUES,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_SELECTION_LOCALE]);
                intent.putExtra(ActionOfChangingRingtones.EXTRA_RING_URI_NOTIFICATION,uri_ring_notification);
                intent.putExtra(ActionOfChangingRingtones.EXTRA_RING_URI_CALL,uri_ring_call);
                intent.putExtra(EXTRA_TITLE_COLOR,getIntent().getStringExtra(EXTRA_TITLE_COLOR));
                startActivityForResult(intent,REQUEST_CODE_RING_CHANGED);
            }
            break;

            case R.id.actions_wallpaper:{
                if(Build.VERSION.SDK_INT>=23&&PermissionChecker.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PermissionChecker.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    Snackbar snackbar=Snackbar.make(findViewById(R.id.layout_actions_root),getResources().getString(R.string.permission_request_read_external_storage),Snackbar.LENGTH_SHORT);
                    snackbar.setAction(getResources().getString(R.string.permission_grant_action_att), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent appdetail = new Intent();
                            appdetail.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            appdetail.setData(Uri.fromParts("package", getApplication().getPackageName(), null));
                            startActivity(appdetail);
                        }
                    });
                    snackbar.show();
                    return;
                }

                BottomDialogWith2Selections dialog=new BottomDialogWith2Selections(this,R.drawable.icon_wallpaper
                        ,getResources().getString(R.string.dialog_actions_wallpaper_select)
                        ,Integer.parseInt(actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SET_WALL_PAPER_LOCALE]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        if(result.equals("0")){
                            startActivityForResult(new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI),REQUEST_CODE_WALLPAPER_CHANGED);
                            //Toast.makeText(ActionActivity.this,getResources().getString(R.string.dialog_actions_wallpaper_att),Toast.LENGTH_SHORT).show();
                        }
                        else {
                            actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SET_WALL_PAPER_LOCALE]=String.valueOf(-1);
                            refreshActionStatus();
                        }
                    }
                });
                dialog.show();
            }
            break;

            case R.id.actions_vibrate:{
                BottomDialogForVibrate dialog=new BottomDialogForVibrate(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_VIBRATE_LOCALE]);
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_VIBRATE_LOCALE]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_notification:{
                final BottomDialog dialog=new BottomDialog(this);
                dialog.setContentView(R.layout.layout_dialog_notification);
                dialog.show();
                try{
                    String[] notification_values=actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NOTIFICATION_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                    int type=Integer.parseInt(notification_values[ActionConsts.ActionSecondLevelLocaleConsts.NOTIFICATION_TYPE_LOCALE]);
                    int type_custom=Integer.parseInt(notification_values[ActionConsts.ActionSecondLevelLocaleConsts.NOTIFICATION_TYPE_IF_CUSTOM_LOCALE]);
                    final String title=notification_title;//notification_values[PublicConsts.NOTIFICATION_TITLE_LOCALE];
                    final String message=notification_message;//notification_values[PublicConsts.NOTIFICATION_MESSAGE_LOCALE];
                    final RadioButton ra_unselected=dialog.findViewById(R.id.dialog_notification_selection_unselected_ra);
                    final RadioButton ra_not_override=dialog.findViewById(R.id.dialog_notification_selection_not_override_ra);
                    final RadioButton ra_override_last=dialog.findViewById(R.id.dialog_notification_selection_override_last_ra);
                    final RadioButton ra_default=dialog.findViewById(R.id.dialog_notification_operation_default_ra);
                    final RadioButton ra_custom=dialog.findViewById(R.id.dialog_notification_operation_custom_ra);
                    final RelativeLayout operation_area=dialog.findViewById(R.id.dialog_notification_operation_area);
                    final EditText edit_title=dialog.findViewById(R.id.dialog_notification_operation_custom_edit_title);
                    final EditText edit_message=dialog.findViewById(R.id.dialog_notification_operation_custom_edit_message);
                    edit_title.setText(title.trim());
                    edit_message.setText(message.trim());
                    (ra_unselected).setChecked(type== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_UNSELECTED);
                    (ra_not_override).setChecked(type== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_NOT_OVERRIDE);
                    (ra_override_last).setChecked(type== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_OVERRIDE_LAST);
                    (operation_area).setVisibility(type== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_UNSELECTED?View.GONE:View.VISIBLE);
                    (dialog.findViewById(R.id.dialog_notification_selection_unselected)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            (ra_unselected).setChecked(true);
                            ra_not_override.setChecked(false);
                            ra_override_last.setChecked(false);
                            (operation_area).setVisibility(View.GONE);
                        }
                    });

                    (dialog.findViewById(R.id.dialog_notification_selection_override_last)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            (ra_unselected).setChecked(false);
                            ra_not_override.setChecked(false);
                            ra_override_last.setChecked(true);
                            (operation_area).setVisibility(View.VISIBLE);

                        }
                    });

                    dialog.findViewById(R.id.dialog_notification_selection_not_override).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            (ra_unselected).setChecked(false);
                            ra_not_override.setChecked(true);
                            ra_override_last.setChecked(false);
                            (operation_area).setVisibility(View.VISIBLE);
                        }
                    });

                    ra_default.setChecked(type_custom== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_CONTENT_DEFAULT);
                    ra_custom.setChecked(type_custom== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_CONTENT_CUSTOM);
                    edit_title.setEnabled(ra_custom.isChecked());
                    edit_message.setEnabled(ra_custom.isChecked());
                    ra_custom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            edit_title.setEnabled(b);
                            edit_message.setEnabled(b);
                        }
                    });
                    (dialog.findViewById(R.id.dialog_notification_button_confirm)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                            int type=-1;
                            if(ra_unselected.isChecked()) type= ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_UNSELECTED;
                            if(ra_override_last.isChecked()) type= ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_OVERRIDE_LAST;
                            if(ra_not_override.isChecked()) type= ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_NOT_OVERRIDE;
                            int type_if_custom=ra_custom.isChecked()? ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_CONTENT_CUSTOM : ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_CONTENT_DEFAULT;
                            String custom_title=edit_title.getText().toString();
                            String custom_message=edit_message.getText().toString();
                            actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NOTIFICATION_LOCALE]=String.valueOf(type)+PublicConsts.SEPARATOR_SECOND_LEVEL
                                    +String.valueOf(type_if_custom);
                            notification_title=custom_title;
                            notification_message=custom_message;
                            refreshActionStatus();
                        }
                    });
                    (dialog.findViewById(R.id.dialog_notification_button_cancel)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                    LogUtil.putExceptionLog(this,e);
                }
            }
            break;
            case R.id.actions_toast:{
                /*final ImageView preview=new ImageView(this);
                final ViewGroup.LayoutParams layoutParams=new ViewGroup.LayoutParams(DisplayDensity.dip2px(this,25),DisplayDensity.dip2px(this,25));
                preview.setLayoutParams(layoutParams);
                preview.setVisibility(View.GONE);
                preview.setBackgroundColor(getResources().getColor(R.color.colorAccent));  */

                final BottomDialog dialog=new BottomDialog(this);
                dialog.setContentView(R.layout.layout_dialog_toast);
                dialog.show();
                final CheckBox cb_enabled=dialog.findViewById(R.id.dialog_toast_cb);
                final RadioGroup rg=dialog.findViewById(R.id.dialog_toast_rg);
                final RadioButton ra_location_default=dialog.findViewById(R.id.dialog_toast_default_ra);
                final RadioButton ra_location_custom=dialog.findViewById(R.id.dialog_toast_custom_ra);
                final SeekBar sb_x=dialog.findViewById(R.id.dialog_toast_x_sb);
                final SeekBar sb_y=dialog.findViewById(R.id.dialog_toast_y_sb);
                TextView tv_x_center=dialog.findViewById(R.id.dialog_toast_x_center);
                TextView tv_y_center=dialog.findViewById(R.id.dialog_toast_y_center);
                final EditText editText=dialog.findViewById(R.id.dialog_toast_edittext);
                final LinearLayout location_selection_area=dialog.findViewById(R.id.dialog_toast_location);
                final Button button=dialog.findViewById(R.id.dialog_toast_preview);
                try{
                    String toast_values[]=actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_TOAST_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
                    boolean isenabled=Integer.parseInt(toast_values[ActionConsts.ActionSecondLevelLocaleConsts.TOAST_TYPE_LOCALE])>=0;
                    int type=Integer.parseInt(toast_values[ActionConsts.ActionSecondLevelLocaleConsts.TOAST_TYPE_LOCALE]);
                    cb_enabled.setChecked(isenabled);
                    rg.setVisibility(isenabled?View.VISIBLE:View.GONE);
                    editText.setText(toast);
                    editText.setEnabled(isenabled);
                    button.setEnabled(isenabled);
                    ra_location_custom.setChecked(type== ActionConsts.ActionValueConsts.TOAST_TYPE_CUSTOM);
                    location_selection_area.setVisibility(isenabled?(type== ActionConsts.ActionValueConsts.TOAST_TYPE_CUSTOM?View.VISIBLE:View.GONE):View.GONE);
                    cb_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            rg.setVisibility(b?View.VISIBLE:View.GONE);
                            editText.setEnabled(b);
                            button.setEnabled(b);
                            location_selection_area.setVisibility(b?(ra_location_custom.isChecked()?View.VISIBLE:View.GONE):View.GONE);
                        }
                    });

                    ra_location_custom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            ra_location_default.setChecked(!b);
                            location_selection_area.setVisibility(b?View.VISIBLE:View.GONE);
                            sb_x.setEnabled(b);
                            sb_y.setEnabled(b);
                        }
                    });

                    final int x_max=getWindowManager().getDefaultDisplay().getWidth();
                    final int y_max=getWindowManager().getDefaultDisplay().getHeight();
                    sb_x.setMax(x_max);
                    sb_y.setMax(y_max);
                    int progress_x=Integer.parseInt(toast_values[ActionConsts.ActionSecondLevelLocaleConsts.TOAST_LOCATION_X_OFFSET_LOCALE]);
                    if(progress_x>=0&&progress_x<=x_max) sb_x.setProgress(progress_x);
                    int progress_y=Integer.parseInt(toast_values[ActionConsts.ActionSecondLevelLocaleConsts.TOAST_LOCATION_Y_OFFSET_LOCALE]);
                    if(progress_y>=0&&progress_y<=y_max) sb_y.setProgress(progress_y);
                    /*sb_x.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            preview.setX(i);
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                            dialog.setBackgroundColor("#00ffffff");
                            ((android.support.constraint.ConstraintLayout)findViewById(R.id.layout_actions_root)).addView(preview);
                            preview.setVisibility(View.VISIBLE);
                            preview.setX(seekBar.getProgress());
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            dialog.setBackgroundColor(BottomDialog.BOTTOM_DIALOG_COLOR_WHITE);
                            preview.setVisibility(View.GONE);
                            ((android.support.constraint.ConstraintLayout)findViewById(R.id.layout_actions_root)).removeView(preview);
                        }
                    });  */

                    /*sb_y.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            preview.setY(i);
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                            dialog.setBackgroundColor("#00ffffff");
                            ((android.support.constraint.ConstraintLayout)findViewById(R.id.layout_actions_root)).addView(preview);
                            preview.setVisibility(View.VISIBLE);
                            preview.setY(seekBar.getProgress());
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            dialog.setBackgroundColor(BottomDialog.BOTTOM_DIALOG_COLOR_WHITE);
                            preview.setVisibility(View.GONE);
                            ((android.support.constraint.ConstraintLayout)findViewById(R.id.layout_actions_root)).removeView(preview);
                        }
                    });  */
                    tv_x_center.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sb_x.setProgress(x_max/2);
                        }
                    });
                    tv_y_center.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sb_y.setProgress(y_max/2);
                        }
                    });
                    editText.setText(toast);
                    dialog.findViewById(R.id.dialog_toast_preview).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast toast=Toast.makeText(ActionActivity.this,editText.getText().toString(),Toast.LENGTH_SHORT);
                            if(ra_location_custom.isChecked()) toast.setGravity(Gravity.TOP|Gravity.START, sb_x.getProgress(),sb_y.getProgress());
                            toast.show();
                        }
                    });
                    dialog.findViewById(R.id.dialog_toast_confirm).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                            actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_TOAST_LOCALE]=String.valueOf(!cb_enabled.isChecked()?-1:
                                    (ra_location_custom.isChecked()?1:0))+PublicConsts.SEPARATOR_SECOND_LEVEL
                                    +String.valueOf(sb_x.getProgress())+PublicConsts.SEPARATOR_SECOND_LEVEL
                                    +String.valueOf(sb_y.getProgress());
                            toast=editText.getText().toString();
                            refreshActionStatus();
                        }
                    });
                    dialog.findViewById(R.id.dialog_toast_cancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                    LogUtil.putExceptionLog(this,e);
                }
            }
            break;
            case R.id.actions_sms:{
                if(PermissionChecker.checkSelfPermission(this,Manifest.permission.SEND_SMS)!=PermissionChecker.PERMISSION_GRANTED){
                    if(Build.VERSION.SDK_INT>=23){
                        requestPermissions(new String[]{Manifest.permission.SEND_SMS,Manifest.permission.READ_PHONE_STATE},1);
                    }
                    Snackbar snackbar=Snackbar.make(findViewById(R.id.layout_actions_root),getResources().getString(R.string.permission_request_sms_send_message),Snackbar.LENGTH_SHORT);
                    snackbar.setAction(getResources().getString(R.string.permission_grant_action_att), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent appdetail = new Intent();
                            appdetail.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            appdetail.setData(Uri.fromParts("package", getApplication().getPackageName(), null));
                            startActivity(appdetail);
                        }
                    });
                    snackbar.show();
                    return;
                }
                Intent i=new Intent(this,SmsActivity.class);
                i.putExtra(SmsActivity.EXTRA_SMS_VALUES,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SMS_LOCALE]);
                i.putExtra(SmsActivity.EXTRA_SMS_ADDRESS,sms_address);
                i.putExtra(SmsActivity.EXTRA_SMS_MESSAGE,sms_message);
                i.putExtra(EXTRA_TITLE_COLOR,getIntent().getStringExtra(EXTRA_TITLE_COLOR));
                startActivityForResult(i,REQUEST_CODE_SMS_SET);
            }
            break;
            case R.id.actions_enable:{
                if(TimeSwitchService.list==null||TimeSwitchService.list.size()<=0){
                    Snackbar.make(findViewById(R.id.layout_actions_root),getResources().getString(R.string.activity_action_switch_task_null),Snackbar.LENGTH_SHORT).show();
                    return;
                }
               showTaskSelectionDialog(TASK_ENABLE);
            }
            break;
            case R.id.actions_disable:{
                if(TimeSwitchService.list==null||TimeSwitchService.list.size()<=0){
                    Snackbar.make(findViewById(R.id.layout_actions_root),getResources().getString(R.string.activity_action_switch_task_null),Snackbar.LENGTH_SHORT).show();
                    return;
                }
                showTaskSelectionDialog(TASK_DISABLE);
            }
            break;
            case R.id.actions_app_open: case R.id.actions_app_close:{
                final int id=view.getId();
                AlertDialog.Builder builder=new AlertDialog.Builder(this)
                        .setTitle(id==R.id.actions_app_open?getResources().getString(R.string.activity_action_app_open_title):getResources().getString(R.string.activity_action_app_close_title))
                        .setView(LayoutInflater.from(this).inflate(R.layout.layout_dialog_app_select,null))
                        .setPositiveButton(getResources().getString(R.string.dialog_button_positive),null)
                        .setNegativeButton(getResources().getString(R.string.action_deselectall),null);
                if(id==R.id.actions_app_open){
                    builder.setNeutralButton(getResources().getString(R.string.action_test),null);
                }
                dialog_app_oc=builder.show();
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
                        msg.what=id==R.id.actions_app_open?MESSAGE_GET_LIST_OPEN_COMPLETE:MESSAGE_GET_LIST_CLOSE_COMPLETE;
                        sendMessage(msg);
                    }
                }).start();
            }
            break;
            case R.id.actions_app_force_close:{

            }
            break;
            case R.id.actions_autorotation:{
                BottomDialogWith3Selections dialog =new BottomDialogWith3Selections(this,R.drawable.icon_autorotation
                        ,R.drawable.icon_autorotation_off
                        ,Integer.parseInt(actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AUTOROTATION]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AUTOROTATION]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
        }
    }

    private void showTaskSelectionDialog(final int enableOrDisable){
        View dialogView=LayoutInflater.from(this).inflate(R.layout.layout_dialog_with_listview,null);
        final AlertDialog dialog=new AlertDialog.Builder(this)
                .setTitle(enableOrDisable==TASK_ENABLE?getResources().getString(R.string.activity_taskgui_actions_enable):getResources().getString(R.string.activity_taskgui_actions_disable))
                .setView(dialogView)
                .setPositiveButton(getResources().getString(R.string.dialog_button_positive),null)
                .setNegativeButton(getResources().getString(R.string.dialog_button_negative), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
        final TaskAdapter adapter=new TaskAdapter();
        try{
            String [] values=actions[enableOrDisable==TASK_ENABLE? ActionConsts.ActionFirstLevelLocaleConsts.ACTION_ENABLE_TASKS_LOCALE: ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DISABLE_TASKS_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL);
            if(Integer.parseInt(values[0])>=0) adapter.setSelectedItems(values);
        }catch (Exception e){
            e.printStackTrace();
        }
        ListView listView=dialogView.findViewById(R.id.layout_dialog_listview);
        (listView).setAdapter(adapter);
        (listView).setDivider(null);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(TimeSwitchService.list==null||TimeSwitchService.list.size()<=i) return;
                if(TimeSwitchService.list.get(i).id==taskid) {
                    Snackbar.make(view,getResources().getString(R.string.activity_actions_switch_can_not_operate_self),Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TimeSwitchService.list.get(i).trigger_type== TriggerTypeConsts.TRIGGER_TYPE_SINGLE&&TimeSwitchService.list.get(i).time<=System.currentTimeMillis()){
                    Snackbar.make(view,getResources().getString(R.string.activity_actions_switch_outofdate),Snackbar.LENGTH_SHORT).show();
                    return;
                }
                adapter.onItemClicked(i);
            }
        });
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder actionValue=new StringBuilder("");
                boolean isSelected[]=adapter.getIsSelected();
                boolean allUnchecked=true;
                int selectedCount=0;
                for(int j=0;j<isSelected.length;j++){
                    if(isSelected[j]&&TimeSwitchService.list!=null){
                        allUnchecked=false;
                        actionValue.append(TimeSwitchService.list.get(j).id);
                        selectedCount++;
                        if(selectedCount<adapter.getSelectedCount()&&adapter.getSelectedCount()>1)
                            actionValue.append(PublicConsts.SEPARATOR_SECOND_LEVEL);
                    }
                }
                if(allUnchecked) actionValue=new StringBuilder(String.valueOf(-1));
                if(enableOrDisable==TASK_ENABLE)actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_ENABLE_TASKS_LOCALE]=actionValue.toString();
                if(enableOrDisable==TASK_DISABLE)actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DISABLE_TASKS_LOCALE]=actionValue.toString();
                refreshActionStatus();
                dialog.cancel();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            checkAndFinish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void checkAndFinish(){
        if(checkString.equals(toCheckString())){
            setResult(RESULT_CANCELED);
            finish();
        }else {
            long clickedTime=System.currentTimeMillis();
            if(clickedTime-first_clicked_back_time>1000){
                first_clicked_back_time=clickedTime;
                Snackbar.make(findViewById(R.id.layout_actions_root),getResources().getString(R.string.snackbar_changes_not_saved_back),Toast.LENGTH_SHORT).show();
                return;
            }
          /*  new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.dialog_edit_changed_not_saved_title))
                    .setMessage(getResources().getString(R.string.dialog_edit_changed_not_saved_message))
                    .setPositiveButton(getResources().getString(R.string.dialog_button_positive), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.dialog_button_negative), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .show();  */
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private String toCheckString(){
        return Arrays.toString(actions)+uri_ring_notification+uri_ring_call+uri_wallpaper
                +notification_title+notification_message+toast+sms_address+sms_message;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            default:break;
            case android.R.id.home:{
                checkAndFinish();
            }
            break;
            case R.id.action_actions_confirm:{
                Intent i=new Intent();
                i.putExtra(EXTRA_ACTIONS,actions);
                i.putExtra(EXTRA_ACTION_URI_RING_NOTIFICATION,uri_ring_notification);
                i.putExtra(EXTRA_ACTION_URI_RING_CALL,uri_ring_call);
                i.putExtra(EXTRA_ACTION_URI_WALLPAPER_DESKTOP,uri_wallpaper);
                i.putExtra(EXTRA_ACTION_SMS_ADDRESS,sms_address);
                i.putExtra(EXTRA_ACTION_SMS_MESSAGE,sms_message);
                i.putExtra(EXTRA_ACTION_NOTIFICATION_TITLE,notification_title);
                i.putExtra(EXTRA_ACTION_NOTIFICATION_MESSAGE,notification_message);
                i.putExtra(EXTRA_ACTION_TOAST,toast);
                setResult(RESULT_OK,i);
                finish();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            default:break;
            case REQUEST_CODE_RING_CHANGED:{
                if(resultCode==RESULT_OK){
                    if(data==null) return;
                    String ring_selection_values=data.getStringExtra(ActionOfChangingRingtones.EXTRA_RING_VALUES);
                    if(ring_selection_values==null) return;
                    actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_SELECTION_LOCALE]=ring_selection_values;
                    uri_ring_notification=data.getStringExtra(ActionOfChangingRingtones.EXTRA_RING_URI_NOTIFICATION);
                    uri_ring_call=data.getStringExtra(ActionOfChangingRingtones.EXTRA_RING_URI_CALL);
                    //Log.i("TaskGui",ring_selection_values);
                    refreshActionStatus();
                }
            }
            break;
            case REQUEST_CODE_WALLPAPER_CHANGED:{
                if(resultCode==RESULT_OK){
                    if(data==null) return;
                    Uri uri=data.getData();
                    if(uri==null) return;
                    actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SET_WALL_PAPER_LOCALE]=String.valueOf(0);
                    uri_wallpaper= ValueUtils.getRealPathFromUri(this,uri);//uri.toString();
                    refreshActionStatus();
                }
            }
            break;
            case REQUEST_CODE_SMS_SET:{
                if(resultCode==RESULT_OK){
                    if(data==null) return;
                    String values=data.getStringExtra(SmsActivity.EXTRA_SMS_VALUES);
                    if(values==null) return;
                    actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SMS_LOCALE]=values;
                    sms_address=data.getStringExtra(SmsActivity.EXTRA_SMS_ADDRESS);
                    sms_message=data.getStringExtra(SmsActivity.EXTRA_SMS_MESSAGE);
                    refreshActionStatus();
                }
            }
            break;
        }
    }

    private void refreshActionStatus(){
        ((TextView)findViewById(R.id.actions_wifi_status)).setText(ActionDisplayValue.getGeneralDisplayValue(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_WIFI_LOCALE]));
        ((TextView)findViewById(R.id.actions_bluetooth_status)).setText(ActionDisplayValue.getGeneralDisplayValue(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BLUETOOTH_LOCALE]));
        ((TextView)findViewById(R.id.actions_ring_mode_status)).setText(ActionDisplayValue.getRingModeDisplayValue(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_MODE_LOCALE]));
        ((TextView)findViewById(R.id.actions_ring_volume_status)).setText(ActionDisplayValue.getRingVolumeDisplayValue(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_VOLUME_LOCALE]));
        ((TextView)findViewById(R.id.actions_ring_selection_status)).setText(ActionDisplayValue.getRingSelectionDisplayValue(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_SELECTION_LOCALE]));
        ((TextView)findViewById(R.id.actions_brightness_status)).setText(ActionDisplayValue.getBrightnessDisplayValue(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BRIGHTNESS_LOCALE]));
        ((TextView)findViewById(R.id.actions_vibrate_status)).setText(ActionDisplayValue.getVibrateDisplayValue(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_VIBRATE_LOCALE]));
        ((TextView)findViewById(R.id.actions_wallpaper_status)).setText(ActionDisplayValue.getWallpaperDisplayValue(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SET_WALL_PAPER_LOCALE],uri_wallpaper));
        ((TextView)findViewById(R.id.actions_sms_status)).setText(ActionDisplayValue.getSMSDisplayValue(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SMS_LOCALE]));
        ((TextView)findViewById(R.id.actions_notification_status)).setText(ActionDisplayValue.getNotificationDisplayValue(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NOTIFICATION_LOCALE]));
        ((TextView)findViewById(R.id.actions_net_status)).setText(ActionDisplayValue.getGeneralDisplayValue(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NET_LOCALE]));
        ((TextView)findViewById(R.id.actions_gps_status)).setText(ActionDisplayValue.getGeneralDisplayValue(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_GPS_LOCALE]));
        ((TextView)findViewById(R.id.actions_airplane_mode_status)).setText(ActionDisplayValue.getGeneralDisplayValue(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AIRPLANE_MODE_LOCALE]));
        ((TextView)findViewById(R.id.actions_devicecontrol_status)).setText(ActionDisplayValue.getDeviceControlDisplayValue(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DEVICE_CONTROL_LOCALE]));
        ((TextView)findViewById(R.id.actions_toast_status)).setText(ActionDisplayValue.getToastDisplayValue(actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_TOAST_LOCALE],toast));
        ((TextView)findViewById(R.id.actions_enable_status)).setText(ActionDisplayValue.getTaskSwitchDisplayValue(actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_ENABLE_TASKS_LOCALE]));
        ((TextView)findViewById(R.id.actions_disable_status)).setText(ActionDisplayValue.getTaskSwitchDisplayValue(actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DISABLE_TASKS_LOCALE]));
        ((TextView)findViewById(R.id.actions_app_open_status)).setText(ActionDisplayValue.getAppNameDisplayValue(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_LAUNCH_APP_PACKAGES]));
        ((TextView)findViewById(R.id.actions_app_close_status)).setText(ActionDisplayValue.getAppNameDisplayValue(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_STOP_APP_PACKAGES]));
    }

    private void showSnackBarOfSuperuserRequest(){
        Snackbar.make(findViewById(R.id.layout_actions_root),getResources().getString(R.string.activity_taskgui_root_required),Snackbar.LENGTH_SHORT)
                .setAction(getResources().getString(R.string.activity_taskgui_root_required_action), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(ActionActivity.this,getResources().getString(R.string.activity_taskgui_root_toast_attention),Toast.LENGTH_SHORT).show();
                        Intent i=new Intent(ActionActivity.this,com.github.ghmxr.timeswitch.activities.Settings.class);
                        i.putExtra(EXTRA_TITLE_COLOR,getIntent().getStringExtra(EXTRA_TITLE_COLOR));
                        startActivity(i);
                    }
                }).show();
    }

    private class TaskAdapter extends BaseAdapter{
        boolean isSelected[];
        private TaskAdapter(){isSelected=new boolean[TimeSwitchService.list!=null?TimeSwitchService.list.size():1];}
        @Override
        public int getCount() {
            return TimeSwitchService.list!=null?TimeSwitchService.list.size():0;
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
            if(TimeSwitchService.list==null) return null;
            if(i>=TimeSwitchService.list.size()) return null;
            if(view==null){
                view=LayoutInflater.from(ActionActivity.this).inflate(R.layout.item_dialog_task,viewGroup,false);
            }
            int imgRes=R.drawable.ic_launcher;
            TaskItem item=TimeSwitchService.list.get(i);
            switch (TimeSwitchService.list.get(i).trigger_type){
                default:break;
                case TriggerTypeConsts.TRIGGER_TYPE_SINGLE:imgRes=R.drawable.icon_repeat_single;break;
                case TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME:imgRes=R.drawable.icon_repeat_percertaintime;break;
                case TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK:imgRes=R.drawable.icon_repeat_weekloop;break;
                case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE:imgRes=R.drawable.icon_battery_high;break;
                case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE:imgRes=R.drawable.icon_battery_low;break;
                case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE: case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE:
                    imgRes=R.drawable.icon_temperature;break;
                case TriggerTypeConsts.TRIGGER_TYPE_RECEIVED_BROADCAST:imgRes=R.drawable.icon_broadcast;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_ON:imgRes=R.drawable.icon_wifi_on;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF:imgRes=R.drawable.icon_wifi_off;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON:imgRes=R.drawable.icon_bluetooth_on;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF:imgRes=R.drawable.icon_bluetooth_off;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF:imgRes=R.drawable.icon_ring_off;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE:imgRes=R.drawable.icon_ring_vibrate;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL:imgRes=R.drawable.icon_ring_normal;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON:imgRes=R.drawable.icon_airplanemode_on;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF:imgRes=R.drawable.icon_airplanemode_off;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED:imgRes=R.drawable.icon_ap_on;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED:imgRes=R.drawable.icon_ap_off;break;
                case TriggerTypeConsts.TRIGGER_TYPE_NET_ON:imgRes=R.drawable.icon_cellular_on;break;
                case TriggerTypeConsts.TRIGGER_TYPE_NET_OFF:imgRes=R.drawable.icon_cellular_off;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIFI_CONNECTED:imgRes=R.drawable.icon_wifi_connected;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIFI_DISCONNECTED:imgRes=R.drawable.icon_wifi_disconnected;break;
                case TriggerTypeConsts.TRIGGER_TYPE_SCREEN_ON:imgRes=R.drawable.icon_screen_on;break;
                case TriggerTypeConsts.TRIGGER_TYPE_SCREEN_OFF:imgRes=R.drawable.icon_screen_off;break;
                case TriggerTypeConsts.TRIGGER_TYPE_POWER_CONNECTED:imgRes=R.drawable.icon_power_connected;break;
                case TriggerTypeConsts.TRIGGER_TYPE_POWER_DISCONNECTED:imgRes=R.drawable.icon_power_disconnected;break;
                case TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_IN: case TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT:{
                    imgRes=R.drawable.icon_headset;
                }
                break;
                case TriggerTypeConsts.TRIGGER_TYPE_APP_LAUNCHED:imgRes=R.drawable.icon_app_launch;break;
                case TriggerTypeConsts.TRIGGER_TYPE_APP_CLOSED:imgRes=R.drawable.icon_app_stop;break;
            }
            ((ImageView)view.findViewById(R.id.item_dialog_task_img)).setImageResource(imgRes);
            ((TextView)view.findViewById(R.id.item_dialog_task_name)).setText(item.name);
            try{
                ((TextView)view.findViewById(R.id.item_dialog_task_name)).setTextColor(Color.parseColor(item.addition_title_color));
            }catch (Exception e){e.printStackTrace();}
            ((TextView)view.findViewById(R.id.item_dialog_task_name_description)).setText(String.valueOf(item.isenabled?getResources().getString(R.string.opened):
                    getResources().getString(R.string.closed)));
            ((TextView)view.findViewById(R.id.item_dialog_task_name_description)).setTextColor(item.isenabled?getResources().getColor(R.color.color_task_enabled_font):getResources().getColor(R.color.color_task_disabled_font));
            ((CheckBox)view.findViewById(R.id.item_dialog_task_cb)).setChecked(isSelected[i]);
            return view;
        }

        public void onItemClicked(int position){
            if(position<0) return;
            if(TimeSwitchService.list==null) return;
            if(position>=TimeSwitchService.list.size()) return;
            isSelected[position]=!isSelected[position];
            this.notifyDataSetChanged();
        }

        public boolean[] getIsSelected(){return this.isSelected;}

        public int getSelectedCount(){
            int s=0;
            for(boolean b:isSelected){
                if(b)s++;
            }
            return s;
        }

        public void setSelectedItems(String [] selectedIDs){
            if(selectedIDs==null) return;
            for(String id: selectedIDs){
                try{
                    int parsedid=Integer.parseInt(id);
                    if(parsedid<0) continue;
                    int position=ProcessTaskItem.getPosition(parsedid);
                    if(position>=0&&position<isSelected.length) isSelected[position]=true;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

}
