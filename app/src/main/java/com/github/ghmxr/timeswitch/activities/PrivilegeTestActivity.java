package com.github.ghmxr.timeswitch.activities;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.PermissionChecker;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.Global;
import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.utils.EnvironmentUtils;

public class PrivilegeTestActivity extends BaseActivity implements View.OnClickListener{
    Toast toast;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_privilege);
        setToolBarAndStatusBarColor(null,getIntent().getStringExtra(EXTRA_TITLE_COLOR));
        try{getSupportActionBar().setDisplayHomeAsUpEnabled(true);}catch (Exception e){e.printStackTrace();}
        findViewById(R.id.privilege_wifi).setOnClickListener(this);
        findViewById(R.id.privilege_bluetooth).setOnClickListener(this);
        findViewById(R.id.privilege_flashlight).setOnClickListener(this);
        findViewById(R.id.privilege_notification_policy).setOnClickListener(this);
        findViewById(R.id.privilege_read_phone_state).setOnClickListener(this);
        findViewById(R.id.privilege_read_external_storage).setOnClickListener(this);
        findViewById(R.id.privilege_send_sms).setOnClickListener(this);
        findViewById(R.id.privilege_write_settings).setOnClickListener(this);
        findViewById(R.id.privilege_notification_policy_page).setOnClickListener(this);
        findViewById(R.id.privilege_usage_page).setOnClickListener(this);
        findViewById(R.id.privilege_write_settings_page).setOnClickListener(this);
        findViewById(R.id.privilege_app_detail).setOnClickListener(this);
    }

    @Override
    public void processMessage(Message msg) {}

    @Override
    public void onClick(View v){
        //Resources resources=getResources();
        switch (v.getId()){
            default:break;
            case R.id.privilege_wifi:{
                try{
                    showToast(EnvironmentUtils.setWifiEnabled(this,true));
                }catch (Exception e){
                    showFailedToastWithErrorInfo(e.toString());
                }
            }
            break;
            case R.id.privilege_bluetooth:{
                try{
                    showToast(EnvironmentUtils.setBluetoothEnabled(true));
                }catch (Exception e){
                    showFailedToastWithErrorInfo(e.toString());
                }
            }
            break;
            case R.id.privilege_flashlight:{
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            EnvironmentUtils.setTorch(PrivilegeTestActivity.this,1000);
                        }catch (final Exception e){
                            e.printStackTrace();
                            Global.handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    showFailedToastWithErrorInfo(e.toString());
                                }
                            });
                        }
                    }
                }).start();
            }
            break;
            case R.id.privilege_notification_policy:{
                try{
                    boolean is_granted=EnvironmentUtils.SpecialPermissionCheckUtil.isNotificationPolicyGranted(this);
                    if(!is_granted&&Build.VERSION.SDK_INT>=23){
                        requestPermissions(new String[]{Manifest.permission.ACCESS_NOTIFICATION_POLICY},0);
                        showToast(false);
                    }else {
                        showToast(is_granted);
                    }
                }catch (Exception e){
                    showFailedToastWithErrorInfo(e.toString());
                }
            }
            break;
            case R.id.privilege_read_phone_state:{
                try{
                    boolean is_granted= PermissionChecker.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)==PermissionChecker.PERMISSION_GRANTED;
                    if(!is_granted&&Build.VERSION.SDK_INT>=23) {
                        requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},0);
                        showToast(false);
                    }else {
                        showToast(true);
                    }
                }catch (Exception e){
                    showFailedToastWithErrorInfo(e.toString());
                }
            }
            break;
            case R.id.privilege_send_sms:{
                try{
                    boolean is_granted= PermissionChecker.checkSelfPermission(this,Manifest.permission.SEND_SMS)==PermissionChecker.PERMISSION_GRANTED;
                    if(!is_granted&&Build.VERSION.SDK_INT>=23) {
                        requestPermissions(new String[]{Manifest.permission.SEND_SMS},0);
                        showToast(false);
                    }else {
                        showToast(true);
                    }
                }catch (Exception e){
                    showFailedToastWithErrorInfo(e.toString());
                }
            }
            break;
            case R.id.privilege_read_external_storage:{
                try{
                    boolean is_granted= PermissionChecker.checkSelfPermission(this,"android.permission.READ_EXTERNAL_STORAGE")==PermissionChecker.PERMISSION_GRANTED;
                    if(!is_granted&&Build.VERSION.SDK_INT>=23) {
                        requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"},0);
                        showToast(false);
                    }else {
                        showToast(true);
                    }
                }catch (Exception e){
                    showFailedToastWithErrorInfo(e.toString());
                }
            }
            break;
            case R.id.privilege_write_settings:{
                try{
                    boolean is_granted=EnvironmentUtils.SpecialPermissionCheckUtil.isWriteSettingsPermissionGranted(this);
                    if(!is_granted&&Build.VERSION.SDK_INT>=23){
                        requestPermissions(new String[]{Manifest.permission.WRITE_SETTINGS},0);
                        showToast(false);
                    }else {
                        showToast(is_granted);
                    }
                }catch (Exception e){
                    showFailedToastWithErrorInfo(e.toString());
                }
            }
            break;
            case R.id.privilege_notification_policy_page:{
                if(Build.VERSION.SDK_INT>=23)startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
            }
            break;
            case R.id.privilege_usage_page:{
                if(Build.VERSION.SDK_INT>=21)startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            }
            break;
            case R.id.privilege_write_settings_page:{
                if(Build.VERSION.SDK_INT>=23){
                    Intent i=new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    i.setData(Uri.parse("package:"+getPackageName()));
                    startActivity(i);
                }
            }
            break;
            case R.id.privilege_app_detail:{
                EnvironmentUtils.PermissionRequestUtil.showAppDetailPageOfThisApplication(this);
            }
            break;
        }
    }

    private void showToast(boolean if_success){
        if(toast!=null) toast.cancel();
        toast=Toast.makeText(this,if_success?getResources().getString(R.string.privilege_result_success):getResources().getString(R.string.privilege_result_failed),Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showFailedToastWithErrorInfo(String info){
        if(toast!=null) toast.cancel();
        toast=Toast.makeText(this,getResources().getString(R.string.privilege_result_failed)+":"+info,Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            default:break;
            case android.R.id.home:{
                finish();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }
}
