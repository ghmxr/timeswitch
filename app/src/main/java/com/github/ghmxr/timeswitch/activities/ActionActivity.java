package com.github.ghmxr.timeswitch.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.adapters.ContentAdapter;
import com.github.ghmxr.timeswitch.data.v2.ActionConsts;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForBrightness;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForDeviceControl;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForFlashlight;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForNotification;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForRingMode;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForToast;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForVibrate;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForVolume;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogWith2Selections;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogWith3Selections;
import com.github.ghmxr.timeswitch.ui.DialogConfirmedCallBack;
import com.github.ghmxr.timeswitch.ui.DialogForAppSelection;
import com.github.ghmxr.timeswitch.ui.DialogForTaskSelection;
import com.github.ghmxr.timeswitch.utils.EnvironmentUtils;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */

public class ActionActivity extends BaseActivity implements View.OnClickListener{
    private TaskItem item;
    private static final int REQUEST_CODE_RING_CHANGED=1;
    private static final int REQUEST_CODE_WALLPAPER_CHANGED=2;
    private static final int REQUEST_CODE_SMS_SET=3;

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
        findViewById(R.id.actions_flashlight).setOnClickListener(this);

        item=(TaskItem)getIntent().getSerializableExtra(EXTRA_SERIALIZED_TASKITEM);
        refreshActionStatus();
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
                        ,Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_WIFI_LOCALE]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_WIFI_LOCALE]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_bluetooth:{
                BottomDialogWith3Selections dialog=new BottomDialogWith3Selections(this,R.drawable.icon_bluetooth_on
                        ,R.drawable.icon_bluetooth_off
                        ,Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BLUETOOTH_LOCALE]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BLUETOOTH_LOCALE]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_ring_mode: {
                if(!EnvironmentUtils.PermissionRequestUtil.checkAndShowNotificationPolicyRequestSnackbar(this,getResources().getString(R.string.permission_request_notification_policy_message)
                        ,getResources().getString(R.string.permission_grant_action_att))) return;

                BottomDialogForRingMode dialog=new BottomDialogForRingMode(this,Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_MODE_LOCALE]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_MODE_LOCALE]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_net:{
                if(!checkAndShowSnackBarOfSuperuserRequest()) return;
                BottomDialogWith3Selections dialog=new BottomDialogWith3Selections(this,R.drawable.icon_cellular_on,R.drawable.icon_cellular_off
                ,Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NET_LOCALE]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NET_LOCALE]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_gps: {
                if(!checkAndShowSnackBarOfSuperuserRequest()) return;
                BottomDialogWith3Selections dialog=new BottomDialogWith3Selections(this,R.drawable.icon_location_on,R.drawable.icon_location_off
                        ,Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_GPS_LOCALE]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_GPS_LOCALE]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_airplane_mode:{
                if(!checkAndShowSnackBarOfSuperuserRequest()) return;
                BottomDialogWith3Selections dialog=new BottomDialogWith3Selections(this,R.drawable.icon_airplanemode_on,R.drawable.icon_airplanemode_off
                        ,Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AIRPLANE_MODE_LOCALE]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AIRPLANE_MODE_LOCALE]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_devicecontrol:{
                if(!checkAndShowSnackBarOfSuperuserRequest()) return;
                BottomDialogForDeviceControl dialog=new BottomDialogForDeviceControl(this,Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DEVICE_CONTROL_LOCALE]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DEVICE_CONTROL_LOCALE]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_brightness:{
                if(!EnvironmentUtils.PermissionRequestUtil.checkAndShowWriteSettingsPermissionRequestSnackbar(this,getResources().getString(R.string.permission_request_write_settings_message)
                        ,getResources().getString(R.string.permission_grant_action_att))) return;
                BottomDialogForBrightness dialog=new BottomDialogForBrightness(this,Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BRIGHTNESS_LOCALE]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BRIGHTNESS_LOCALE]=String.valueOf(result);
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_ring_volume:{
                if(!EnvironmentUtils.PermissionRequestUtil.checkAndShowNotificationPolicyRequestSnackbar(this,getResources().getString(R.string.permission_request_notification_policy_message)
                        ,getResources().getString(R.string.permission_grant_action_att))) return;

                BottomDialogForVolume dialog=new BottomDialogForVolume(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_VOLUME_LOCALE]);
                dialog.show();
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_VOLUME_LOCALE]=result;
                        refreshActionStatus();
                    }
                });

            }
            break;
            case R.id.actions_ring_selection:{
                if(!EnvironmentUtils.PermissionRequestUtil.checkAndShowWriteSettingsPermissionRequestSnackbar(this,getResources().getString(R.string.permission_request_write_settings_message)
                        ,getResources().getString(R.string.permission_grant_action_att))) return;

                Intent intent = new Intent();
                intent.setClass(this,ChangeRingtoneActivity.class);
                intent.putExtra(EXTRA_TITLE_COLOR,getIntent().getStringExtra(EXTRA_TITLE_COLOR));
                intent.putExtra(EXTRA_SERIALIZED_TASKITEM,item);
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
                        ,Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SET_WALL_PAPER_LOCALE]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        if(result.equals("0")){
                            startActivityForResult(new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI),REQUEST_CODE_WALLPAPER_CHANGED);
                            //Toast.makeText(ActionActivity.this,getResources().getString(R.string.dialog_actions_wallpaper_att),Toast.LENGTH_SHORT).show();
                        }
                        else {
                            item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SET_WALL_PAPER_LOCALE]=String.valueOf(-1);
                            refreshActionStatus();
                        }
                    }
                });
                dialog.show();
            }
            break;

            case R.id.actions_vibrate:{
                BottomDialogForVibrate dialog=new BottomDialogForVibrate(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_VIBRATE_LOCALE]);
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_VIBRATE_LOCALE]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_notification:{
                BottomDialogForNotification dialog=new BottomDialogForNotification(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NOTIFICATION_LOCALE],item.notification_title,item.notification_message);
                dialog.show();
                dialog.setOnDialogConfirmedCallback(new BottomDialogForNotification.DialogConfirmedCallback() {
                    @Override
                    public void onDialogConfirmed(String[] result) {
                        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NOTIFICATION_LOCALE]=result[0];
                        item.notification_title=result[1];
                        item.notification_message=result[2];
                        refreshActionStatus();
                    }
                });
            }
            break;
            case R.id.actions_toast:{
                BottomDialogForToast dialog=new BottomDialogForToast(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_TOAST_LOCALE],item.toast);
                dialog.setOnDialogConfirmedCallback(new BottomDialogForToast.DialogConfirmedCallback() {
                    @Override
                    public void onDialogConfirmed(String[] result) {
                        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_TOAST_LOCALE]=result[0];
                        item.toast=result[1];
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
                i.putExtra(EXTRA_SERIALIZED_TASKITEM,item);
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
                        ,TimeSwitchService.list,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_ENABLE_TASKS_LOCALE],null);
                dialog.setOnDialogConfirmedCallback(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_ENABLE_TASKS_LOCALE]=result;
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
                        ,TimeSwitchService.list,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DISABLE_TASKS_LOCALE],"#55e74c3c");
                dialog.setOnDialogConfirmedCallback(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DISABLE_TASKS_LOCALE]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_app_open: {
                DialogForAppSelection dialog=new DialogForAppSelection(this,getResources().getString(R.string.activity_action_app_open_title)
                        ,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_LAUNCH_APP_PACKAGES],null,getResources().getString(R.string.dialog_app_select_long_press_test));
                dialog.show();
                dialog.setOnDialogConfirmedCallBack(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_LAUNCH_APP_PACKAGES]=result;
                        refreshActionStatus();
                    }
                });
            }
            break;

            case R.id.actions_app_close:{
                DialogForAppSelection dialog=new DialogForAppSelection(this,getResources().getString(R.string.activity_action_app_close_title)
                        ,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_STOP_APP_PACKAGES],"#55e74c3c",getResources().getString(R.string.dialog_app_close_att));
                dialog.show();
                dialog.setOnDialogConfirmedCallBack(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_STOP_APP_PACKAGES]=result;
                        refreshActionStatus();
                    }
                });
            }
            break;
            case R.id.actions_app_force_close:{
                if(!checkAndShowSnackBarOfSuperuserRequest()) return;
                DialogForAppSelection dialog=new DialogForAppSelection(this,getResources().getString(R.string.activity_taskgui_actions_app_force_close)
                        ,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FORCE_STOP_APP_PACKAGES],"#55e74c3c",getResources().getString(R.string.dialog_app_force_close_att));
                dialog.show();
                dialog.setOnDialogConfirmedCallBack(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FORCE_STOP_APP_PACKAGES]=result;
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
                        ,Integer.parseInt(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AUTOROTATION]));
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AUTOROTATION]=result;
                        refreshActionStatus();
                    }
                });
                dialog.show();
            }
            break;
            case R.id.actions_flashlight:{
                BottomDialogForFlashlight dialog=new BottomDialogForFlashlight(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FLASHLIGHT]);
                dialog.show();
                dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                    @Override
                    public void onDialogConfirmed(String result) {
                        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FLASHLIGHT]=result;
                        refreshActionStatus();
                    }
                });
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            default:break;
            case android.R.id.home:case R.id.action_actions_confirm:{
                finish();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish(){
        Intent intent=new Intent();
        intent.putExtra(EXTRA_SERIALIZED_TASKITEM,item);
        setResult(RESULT_OK,intent);
        super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data==null)return;
        switch (requestCode){
            default:break;
            case REQUEST_CODE_RING_CHANGED:{
                if(resultCode==RESULT_OK){
                    item=(TaskItem) data.getSerializableExtra(EXTRA_SERIALIZED_TASKITEM);
                    refreshActionStatus();
                }
            }
            break;
            case REQUEST_CODE_WALLPAPER_CHANGED:{
                if(resultCode==RESULT_OK){
                    Uri uri=data.getData();
                    if(uri==null) return;
                    item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SET_WALL_PAPER_LOCALE]=String.valueOf(0);
                    item.uri_wallpaper_desktop= ValueUtils.getRealPathFromUri(this,uri);//uri.toString();
                    refreshActionStatus();
                }
            }
            break;
            case REQUEST_CODE_SMS_SET:{
                if(resultCode==RESULT_OK){
                    item=(TaskItem)data.getSerializableExtra(EXTRA_SERIALIZED_TASKITEM);
                    refreshActionStatus();
                }
            }
            break;
        }
    }

    private void refreshActionStatus(){
        ((TextView)findViewById(R.id.actions_wifi_status)).setText(ContentAdapter.ActionContentAdapter.getGeneralDisplayValue(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_WIFI_LOCALE]));
        ((TextView)findViewById(R.id.actions_bluetooth_status)).setText(ContentAdapter.ActionContentAdapter.getGeneralDisplayValue(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BLUETOOTH_LOCALE]));
        ((TextView)findViewById(R.id.actions_ring_mode_status)).setText(ContentAdapter.ActionContentAdapter.getRingModeDisplayValue(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_MODE_LOCALE]));
        ((TextView)findViewById(R.id.actions_ring_volume_status)).setText(ContentAdapter.ActionContentAdapter.getRingVolumeDisplayValue(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_VOLUME_LOCALE]));
        ((TextView)findViewById(R.id.actions_ring_selection_status)).setText(ContentAdapter.ActionContentAdapter.getRingSelectionDisplayValue(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_SELECTION_LOCALE]));
        ((TextView)findViewById(R.id.actions_brightness_status)).setText(ContentAdapter.ActionContentAdapter.getBrightnessDisplayValue(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BRIGHTNESS_LOCALE]));
        ((TextView)findViewById(R.id.actions_vibrate_status)).setText(ContentAdapter.ActionContentAdapter.getVibrateDisplayValue(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_VIBRATE_LOCALE]));
        ((TextView)findViewById(R.id.actions_wallpaper_status)).setText(ContentAdapter.ActionContentAdapter.getWallpaperDisplayValue(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SET_WALL_PAPER_LOCALE],item.uri_wallpaper_desktop));
        ((TextView)findViewById(R.id.actions_sms_status)).setText(ContentAdapter.ActionContentAdapter.getSMSDisplayValue(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SMS_LOCALE]));
        ((TextView)findViewById(R.id.actions_notification_status)).setText(ContentAdapter.ActionContentAdapter.getNotificationDisplayValue(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NOTIFICATION_LOCALE]));
        ((TextView)findViewById(R.id.actions_net_status)).setText(ContentAdapter.ActionContentAdapter.getGeneralDisplayValue(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NET_LOCALE]));
        ((TextView)findViewById(R.id.actions_gps_status)).setText(ContentAdapter.ActionContentAdapter.getGeneralDisplayValue(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_GPS_LOCALE]));
        ((TextView)findViewById(R.id.actions_airplane_mode_status)).setText(ContentAdapter.ActionContentAdapter.getGeneralDisplayValue(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AIRPLANE_MODE_LOCALE]));
        ((TextView)findViewById(R.id.actions_devicecontrol_status)).setText(ContentAdapter.ActionContentAdapter.getDeviceControlDisplayValue(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DEVICE_CONTROL_LOCALE]));
        ((TextView)findViewById(R.id.actions_toast_status)).setText(ContentAdapter.ActionContentAdapter.getToastDisplayValue(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_TOAST_LOCALE],item.toast));
        ((TextView)findViewById(R.id.actions_enable_status)).setText(ContentAdapter.ActionContentAdapter.getTaskSwitchDisplayValue(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_ENABLE_TASKS_LOCALE]));
        ((TextView)findViewById(R.id.actions_disable_status)).setText(ContentAdapter.ActionContentAdapter.getTaskSwitchDisplayValue(item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DISABLE_TASKS_LOCALE]));
        ((TextView)findViewById(R.id.actions_app_open_status)).setText(ContentAdapter.ActionContentAdapter.getAppNameDisplayValue(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_LAUNCH_APP_PACKAGES]));
        ((TextView)findViewById(R.id.actions_app_close_status)).setText(ContentAdapter.ActionContentAdapter.getAppNameDisplayValue(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_STOP_APP_PACKAGES]));
        ((TextView)findViewById(R.id.actions_autorotation_status)).setText(ContentAdapter.ActionContentAdapter.getGeneralDisplayValue(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AUTOROTATION]));
        ((TextView)findViewById(R.id.actions_flashlight_status)).setText(ContentAdapter.ActionContentAdapter.getFlashlightDisplayValue(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FLASHLIGHT]));
        ((TextView)findViewById(R.id.actions_app_force_close_status)).setText(ContentAdapter.ActionContentAdapter.getAppNameDisplayValue(this,item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FORCE_STOP_APP_PACKAGES]));
    }

    private boolean checkAndShowSnackBarOfSuperuserRequest(){
        if(getSharedPreferences(PublicConsts.PREFERENCES_NAME, Activity.MODE_PRIVATE).getBoolean(PublicConsts.PREFERENCES_IS_SUPERUSER_MODE,PublicConsts.PREFERENCES_IS_SUPERUSER_MODE_DEFAULT)) return true;
        Snackbar.make(findViewById(android.R.id.content),getResources().getString(R.string.activity_taskgui_root_required),Snackbar.LENGTH_SHORT)
                .setAction(getResources().getString(R.string.activity_taskgui_root_required_action), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(ActionActivity.this,getResources().getString(R.string.activity_taskgui_root_toast_attention),Toast.LENGTH_SHORT).show();
                        Intent i=new Intent(ActionActivity.this,SettingsActivity.class);
                        i.putExtra(EXTRA_TITLE_COLOR,getIntent().getStringExtra(EXTRA_TITLE_COLOR));
                        startActivity(i);
                    }
                }).show();
        return false;
    }

}
