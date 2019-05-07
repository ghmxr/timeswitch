package com.github.ghmxr.timeswitch.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.ActionConsts;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.ui.ActionDisplayValue;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForBrightness;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForDeviceControl;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForNotification;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForRingMode;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForToast;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForVibrate;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForVolume;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogWith2Selections;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogWith3Selections;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.DialogConfirmedCallBack;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.DialogForAppSelection;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.DialogForTaskSelection;
import com.github.ghmxr.timeswitch.utils.EnvironmentUtils;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.util.Arrays;

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
    //private int taskid=-1;
    //private static final int TASK_ENABLE=0;
    //private static final int TASK_DISABLE=1;
    //private static final int MESSAGE_GET_LIST_OPEN_COMPLETE=101;
    //private static final int MESSAGE_GET_LIST_CLOSE_COMPLETE=102;

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
            //taskid=data.getIntExtra(EXTRA_TASK_ID,-1);
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
    public void processMessage(Message msg) {}

    @Override
    public void onClick(View view) {
        switch (view.getId()){
           default:break;
            case R.id.actions_wifi:{
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
                if(!EnvironmentUtils.PermissionRequestUtil.checkAndShowNotificationPolicyRequestSnackbar(this,getResources().getString(R.string.permission_request_notification_policy_message)
                        ,getResources().getString(R.string.permission_grant_action_att))) return;

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
                if(!checkAndShowSnackBarOfSuperuserRequest()) return;
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
                if(!checkAndShowSnackBarOfSuperuserRequest()) return;
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
                if(!checkAndShowSnackBarOfSuperuserRequest()) return;
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
                if(!checkAndShowSnackBarOfSuperuserRequest()) return;
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
                if(!EnvironmentUtils.PermissionRequestUtil.checkAndShowWriteSettingsPermissionRequestSnackbar(this,getResources().getString(R.string.permission_request_write_settings_message)
                        ,getResources().getString(R.string.permission_grant_action_att))) return;
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
                if(!EnvironmentUtils.PermissionRequestUtil.checkAndShowNotificationPolicyRequestSnackbar(this,getResources().getString(R.string.permission_request_notification_policy_message)
                        ,getResources().getString(R.string.permission_grant_action_att))) return;

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
                if(!EnvironmentUtils.PermissionRequestUtil.checkAndShowWriteSettingsPermissionRequestSnackbar(this,getResources().getString(R.string.permission_request_write_settings_message)
                        ,getResources().getString(R.string.permission_grant_action_att))) return;

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
                    EnvironmentUtils.PermissionRequestUtil.showSnackbarWithActionOfAppdetailPage(this,getResources().getString(R.string.permission_request_read_external_storage)
                    ,getResources().getString(R.string.permission_grant_action_att));
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
                BottomDialogForNotification dialog=new BottomDialogForNotification(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NOTIFICATION_LOCALE],notification_title,notification_message);
                dialog.show();
                dialog.setOnDialogConfirmedCallback(new BottomDialogForNotification.DialogConfirmedCallback() {
                    @Override
                    public void onDialogConfirmed(String[] result) {
                        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NOTIFICATION_LOCALE]=result[0];
                        notification_title=result[1];
                        notification_message=result[2];
                        refreshActionStatus();
                    }
                });
            }
            break;
            case R.id.actions_toast:{
                BottomDialogForToast dialog=new BottomDialogForToast(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_TOAST_LOCALE],toast);
                dialog.setOnDialogConfirmedCallback(new BottomDialogForToast.DialogConfirmedCallback() {
                    @Override
                    public void onDialogConfirmed(String[] result) {
                        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_TOAST_LOCALE]=result[0];
                        toast=result[1];
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_sms:{
                if(PermissionChecker.checkSelfPermission(this,Manifest.permission.SEND_SMS)!=PermissionChecker.PERMISSION_GRANTED&&Build.VERSION.SDK_INT>=23){
                    requestPermissions(new String[]{Manifest.permission.SEND_SMS,Manifest.permission.READ_PHONE_STATE},1);
                    EnvironmentUtils.PermissionRequestUtil.showSnackbarWithActionOfAppdetailPage(this,getResources().getString(R.string.permission_request_sms_send_message)
                            ,getResources().getString(R.string.permission_grant_action_att));
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
                if(TimeSwitchService.list==null||TimeSwitchService.list.size()==0){
                    Snackbar.make(findViewById(R.id.layout_actions_root),getResources().getString(R.string.activity_action_switch_task_null),Snackbar.LENGTH_SHORT).show();
                    return;
                }

                DialogForTaskSelection dialog=new DialogForTaskSelection(this
                        ,getResources().getString(R.string.activity_taskgui_actions_enable)
                        ,TimeSwitchService.list,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_ENABLE_TASKS_LOCALE],null);
                dialog.setOnDialogConfirmedCallback(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_ENABLE_TASKS_LOCALE]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_disable:{
                if(TimeSwitchService.list==null||TimeSwitchService.list.size()==0){
                    Snackbar.make(findViewById(R.id.layout_actions_root),getResources().getString(R.string.activity_action_switch_task_null),Snackbar.LENGTH_SHORT).show();
                    return;
                }
                DialogForTaskSelection dialog=new DialogForTaskSelection(this
                        ,getResources().getString(R.string.activity_taskgui_actions_disable)
                        ,TimeSwitchService.list,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DISABLE_TASKS_LOCALE],"#55e74c3c");
                dialog.setOnDialogConfirmedCallback(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DISABLE_TASKS_LOCALE]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_app_open: {
                DialogForAppSelection dialog=new DialogForAppSelection(this,getResources().getString(R.string.activity_action_app_open_title)
                        ,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_LAUNCH_APP_PACKAGES],null,getResources().getString(R.string.dialog_app_select_long_press_test));
                dialog.show();
                dialog.setOnDialogConfirmedCallBack(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_LAUNCH_APP_PACKAGES]=result;
                        refreshActionStatus();
                    }
                });
            }
            break;

            case R.id.actions_app_close:{
                DialogForAppSelection dialog=new DialogForAppSelection(this,getResources().getString(R.string.activity_action_app_close_title)
                        ,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_STOP_APP_PACKAGES],"#55e74c3c",getResources().getString(R.string.dialog_app_close_att));
                dialog.show();
                dialog.setOnDialogConfirmedCallBack(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_STOP_APP_PACKAGES]=result;
                        refreshActionStatus();
                    }
                });
            }
            break;
            case R.id.actions_app_force_close:{
                if(!checkAndShowSnackBarOfSuperuserRequest()) return;
                DialogForAppSelection dialog=new DialogForAppSelection(this,getResources().getString(R.string.activity_taskgui_actions_app_force_close)
                        ,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FORCE_STOP_APP_PACKAGES],"#55e74c3c",getResources().getString(R.string.dialog_app_force_close_att));
                dialog.show();
                dialog.setOnDialogConfirmedCallBack(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FORCE_STOP_APP_PACKAGES]=result;
                        refreshActionStatus();
                    }
                });
            }
            break;
            case R.id.actions_autorotation:{
                if(!EnvironmentUtils.PermissionRequestUtil.checkAndShowWriteSettingsPermissionRequestSnackbar(this
                ,getResources().getString(R.string.permission_request_write_settings_message)
                ,getResources().getString(R.string.permission_grant_action_att))) return;

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
                Snackbar.make(findViewById(android.R.id.content),getResources().getString(R.string.snackbar_changes_not_saved_back),Toast.LENGTH_SHORT).show();
                return;
            }
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
        ((TextView)findViewById(R.id.actions_autorotation_status)).setText(ActionDisplayValue.getGeneralDisplayValue(this,actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AUTOROTATION]));
    }

    private boolean checkAndShowSnackBarOfSuperuserRequest(){
        if(getSharedPreferences(PublicConsts.PREFERENCES_NAME, Activity.MODE_PRIVATE).getBoolean(PublicConsts.PREFERENCES_IS_SUPERUSER_MODE,PublicConsts.PREFERENCES_IS_SUPERUSER_MODE_DEFAULT)) return true;
        Snackbar.make(findViewById(android.R.id.content),getResources().getString(R.string.activity_taskgui_root_required),Snackbar.LENGTH_SHORT)
                .setAction(getResources().getString(R.string.activity_taskgui_root_required_action), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(ActionActivity.this,getResources().getString(R.string.activity_taskgui_root_toast_attention),Toast.LENGTH_SHORT).show();
                        Intent i=new Intent(ActionActivity.this,com.github.ghmxr.timeswitch.activities.Settings.class);
                        i.putExtra(EXTRA_TITLE_COLOR,getIntent().getStringExtra(EXTRA_TITLE_COLOR));
                        startActivity(i);
                    }
                }).show();
        return false;
    }

}
