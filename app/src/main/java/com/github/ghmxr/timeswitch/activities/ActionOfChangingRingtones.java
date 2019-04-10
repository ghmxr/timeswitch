package com.github.ghmxr.timeswitch.activities;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.ActionConsts;
import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.ui.BottomDialog;
import com.github.ghmxr.timeswitch.utils.LogUtil;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */

public class ActionOfChangingRingtones extends BaseActivity implements View.OnClickListener{
    public static final String EXTRA_RING_VALUES="ring_values";
    public static final String EXTRA_RING_URI_NOTIFICATION="uri_notification";
    public static final String EXTRA_RING_URI_CALL="uri_call";
    //public static final int RESULT_RING_CHANGED=0x00001;
    private static final int REQUEST_CODE_RING_NOTIFICATION_FROM_SYSTEM =0x00010;
    private static final int REQUEST_CODE_RING_NOTIFICATION_FROM_MEDIA_STORE=0x00011;
    private static final int REQUEST_CODE_RING_PHONE_FROM_SYSTEM =0x00020;
    private static final int REQUEST_CODE_RING_PHONE_FROM_MEDIA_STORE=0x00021;
    //String ring_values;
    int selection_notification=-1;
    int selection_phone=-1;
    String value_notification_uri;
    String value_call_uri;
    private String checkString="";
    private long first_exit_time=0;

    private static final int TYPE_NOTIFICATION=0;
    private static final int TYPE_PHONE=1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_ring_selection);
        Toolbar toolbar=findViewById(R.id.ring_selection_toolbar);
        setSupportActionBar(toolbar);
        setToolBarAndStatusBarColor(toolbar,getIntent().getStringExtra(EXTRA_TITLE_COLOR));
        try{getSupportActionBar().setDisplayHomeAsUpEnabled(true);}catch (Exception e){e.printStackTrace();}
        try{
            String ring_values[]=getIntent().getStringExtra(EXTRA_RING_VALUES).split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            selection_notification=Integer.parseInt(ring_values[ActionConsts.ActionSecondLevelLocaleConsts.RING_SELECTION_NOTIFICATION_TYPE_LOCALE]);
            selection_phone=Integer.parseInt(ring_values[ActionConsts.ActionSecondLevelLocaleConsts.RING_SELECTION_CALL_TYPE_LOCALE]);
            //value_notification_uri =ring_values[PublicConsts.RING_SELECTION_NOTIFICATION_VALUE_LOCALE];
            //value_call_uri =ring_values[PublicConsts.RING_SELECTION_PHONE_VALUE_LOCALE];
            value_notification_uri=getIntent().getStringExtra(EXTRA_RING_URI_NOTIFICATION);
            value_call_uri =getIntent().getStringExtra(EXTRA_RING_URI_CALL);
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.putExceptionLog(this,e);
        }
        findViewById(R.id.ring_selection_notification).setOnClickListener(this);
        findViewById(R.id.ring_selection_phone).setOnClickListener(this);
        try{
            ((TextView)findViewById(R.id.ring_selection_notification_value)).setText(
                    selection_notification== ActionConsts.ActionValueConsts.RING_TYPE_FROM_SYSTEM ?RingtoneManager.getRingtone(this,Uri.parse(value_notification_uri)).getTitle(this):
                            (selection_notification== ActionConsts.ActionValueConsts.RING_TYPE_FROM_MEDIA ?RingtoneManager.getRingtone(this,Uri.parse(value_notification_uri)).getTitle(this):getResources().getString(R.string.unselected))
            );
            ((TextView)findViewById(R.id.ring_selection_phone_value)).setText(
                    selection_phone== ActionConsts.ActionValueConsts.RING_TYPE_FROM_SYSTEM ?RingtoneManager.getRingtone(this,Uri.parse(value_call_uri)).getTitle(this):
                            (selection_phone== ActionConsts.ActionValueConsts.RING_TYPE_FROM_MEDIA ?RingtoneManager.getRingtone(this,Uri.parse(value_call_uri)).getTitle(this):getResources().getString(R.string.unselected))
            );
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.putExceptionLog(this,e);
        }
        checkString=toCheckString();
    }

    private String toCheckString(){
        return String.valueOf(selection_phone)+"/"+String.valueOf(value_call_uri)+String.valueOf(selection_notification)+"/"+String.valueOf(value_notification_uri);
    }

    @Override
    public void processMessage(Message msg) {}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            default:break;
            case android.R.id.home:{
                checkAndExit();
            }
            break;
            case R.id.action_ring_selection_confirm:{
                Intent i=new Intent();
                String returnValue=String.valueOf(selection_notification)
                        +PublicConsts.SEPARATOR_SECOND_LEVEL
                        +String.valueOf(selection_phone);
                i.putExtra(EXTRA_RING_VALUES,returnValue);
                i.putExtra(EXTRA_RING_URI_NOTIFICATION,value_notification_uri);
                i.putExtra(EXTRA_RING_URI_CALL,value_call_uri);
                setResult(RESULT_OK,i);
                finish();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK){
            checkAndExit();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.ringselection,menu);
        menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            default:break;
            case R.id.ring_selection_notification:{
                startShowingSelectionDialogs(TYPE_NOTIFICATION);
            }
            break;
            case R.id.ring_selection_phone:{
                startShowingSelectionDialogs(TYPE_PHONE);
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            default:break;
            case REQUEST_CODE_RING_NOTIFICATION_FROM_SYSTEM:{
                if(resultCode==RESULT_OK) onNotificationRingtoneSetFromSystem(data);
            }
            break;
            case REQUEST_CODE_RING_NOTIFICATION_FROM_MEDIA_STORE:{
                if(resultCode==RESULT_OK) onNotificationRingtoneSetFromMediaStore(data);
            }
            break;
            case REQUEST_CODE_RING_PHONE_FROM_SYSTEM:{
                if(resultCode==RESULT_OK) onPhoneRingtoneSetFromSystem(data);
            }
            break;
            case REQUEST_CODE_RING_PHONE_FROM_MEDIA_STORE:{
                if(resultCode==RESULT_OK) onPhoneRingtoneSetFromMediaStore(data);
            }
            break;
        }
    }

    private void checkAndExit(){
        long clicked_time=System.currentTimeMillis();
        if(toCheckString().equals(checkString)){
            setResult(RESULT_CANCELED);
            finish();
        }else{
            if(clicked_time-first_exit_time>1000){
                first_exit_time=clicked_time;
                Snackbar.make(findViewById(R.id.ring_selection_root),getResources().getString(R.string.snackbar_changes_not_saved_back),Snackbar.LENGTH_SHORT).show();
                return;
            }
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void onNotificationRingtoneSetFromSystem(Intent data){
        if(data==null) return;
        Uri uri=data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
        if(uri==null) return;
        selection_notification= ActionConsts.ActionValueConsts.RING_TYPE_FROM_SYSTEM;
        value_notification_uri =uri.toString();
        ((TextView)findViewById(R.id.ring_selection_notification_value)).setText(RingtoneManager.getRingtone(this,uri).getTitle(this));
    }

    private void onNotificationRingtoneSetFromMediaStore(Intent data){
        if(data==null) return;
        Uri uri=data.getData();
        if(uri==null) return;
        selection_notification= ActionConsts.ActionValueConsts.RING_TYPE_FROM_MEDIA;
        value_notification_uri =uri.toString();
        ((TextView)findViewById(R.id.ring_selection_notification_value)).setText(RingtoneManager.getRingtone(this,uri).getTitle(this));//new File(ValueUtils.getRealPathFromUri(this,uri)).getName());
    }

    private void onPhoneRingtoneSetFromSystem(Intent data){
        if(data==null) return;
        Uri uri=data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
        if(uri==null) return;
        selection_phone= ActionConsts.ActionValueConsts.RING_TYPE_FROM_SYSTEM;
        value_call_uri =uri.toString();
        ((TextView)findViewById(R.id.ring_selection_phone_value)).setText(RingtoneManager.getRingtone(this,uri).getTitle(this));
    }

    private void onPhoneRingtoneSetFromMediaStore(Intent data){
        if(data==null) return;
        Uri uri=data.getData();
        if(uri==null) return;
        selection_phone= ActionConsts.ActionValueConsts.RING_TYPE_FROM_MEDIA;
        value_call_uri =uri.toString();
        ((TextView)findViewById(R.id.ring_selection_phone_value)).setText(RingtoneManager.getRingtone(this,uri).getTitle(this));
    }

    /**
     * show selection dialog
     * @param type 0 or 1
     */
    private void startShowingSelectionDialogs(final int type){
        final BottomDialog dialog=new BottomDialog(this);
        dialog.setContentView(R.layout.layout_dialog_ring_selection);
        dialog.show();
        if(type==TYPE_NOTIFICATION){
            ((RadioButton)dialog.findViewById(R.id.dialog_ring_unselected_rb)).setChecked(selection_notification==-1);
            ((RadioButton)dialog.findViewById(R.id.dialog_ring_system_rb)).setChecked(selection_notification== ActionConsts.ActionValueConsts.RING_TYPE_FROM_SYSTEM);
            ((RadioButton)dialog.findViewById(R.id.dialog_ring_media_rb)).setChecked(selection_notification== ActionConsts.ActionValueConsts.RING_TYPE_FROM_MEDIA);
            //((RadioButton)dialog.findViewById(R.id.dialog_ring_path_rb)).setChecked(selection_notification==PublicConsts.RING_TYPE_PATH);
        }else if(type==TYPE_PHONE){
            ((RadioButton)dialog.findViewById(R.id.dialog_ring_unselected_rb)).setChecked(selection_phone==-1);
            ((RadioButton)dialog.findViewById(R.id.dialog_ring_system_rb)).setChecked(selection_phone== ActionConsts.ActionValueConsts.RING_TYPE_FROM_SYSTEM);
            ((RadioButton)dialog.findViewById(R.id.dialog_ring_media_rb)).setChecked(selection_phone== ActionConsts.ActionValueConsts.RING_TYPE_FROM_MEDIA);
            //((RadioButton)dialog.findViewById(R.id.dialog_ring_path_rb)).setChecked(selection_phone==PublicConsts.RING_TYPE_PATH);
        }

        dialog.findViewById(R.id.dialog_ring_unselected).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                if(type==TYPE_NOTIFICATION) {
                    selection_notification=-1;
                    value_notification_uri =String.valueOf("");
                    ((TextView)findViewById(R.id.ring_selection_notification_value)).setText(getResources().getString(R.string.unselected));
                }
                else if(type==TYPE_PHONE) {
                    selection_phone=-1;
                    value_call_uri =String.valueOf("");
                    ((TextView)findViewById(R.id.ring_selection_phone_value)).setText(getResources().getString(R.string.unselected));
                }
            }
        });
        dialog.findViewById(R.id.dialog_ring_system).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                Intent i=new Intent();
                i.setAction(RingtoneManager.ACTION_RINGTONE_PICKER);
                i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,false);
                i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,false);
                if(type==TYPE_NOTIFICATION)i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_NOTIFICATION);
                else if(type==TYPE_PHONE)i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_RINGTONE);
                try{
                    switch (type){
                        default:break;
                        case TYPE_NOTIFICATION:{
                            if(value_notification_uri !=null&&!value_notification_uri.trim().equals("")&&selection_notification== ActionConsts.ActionValueConsts.RING_TYPE_FROM_SYSTEM)
                                i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,Uri.parse(value_notification_uri));
                        }
                        break;
                        case TYPE_PHONE:{
                            if(value_call_uri !=null&&!value_call_uri.trim().equals("")&&selection_phone== ActionConsts.ActionValueConsts.RING_TYPE_FROM_SYSTEM)
                                i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,Uri.parse(value_call_uri));
                        }
                        break;
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    LogUtil.putExceptionLog(ActionOfChangingRingtones.this,e);
                }
                if(type==TYPE_NOTIFICATION) startActivityForResult(i, REQUEST_CODE_RING_NOTIFICATION_FROM_SYSTEM);
                else if(type==TYPE_PHONE) startActivityForResult(i, REQUEST_CODE_RING_PHONE_FROM_SYSTEM);
            }
        });
        dialog.findViewById(R.id.dialog_ring_media).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                Intent i=new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                if(type==TYPE_NOTIFICATION)startActivityForResult(i, REQUEST_CODE_RING_NOTIFICATION_FROM_MEDIA_STORE);
                else if(type==TYPE_PHONE) startActivityForResult(i, REQUEST_CODE_RING_PHONE_FROM_MEDIA_STORE);
            }
        });
    }

}
