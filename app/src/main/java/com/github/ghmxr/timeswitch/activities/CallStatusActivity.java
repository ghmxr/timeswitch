package com.github.ghmxr.timeswitch.activities;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.ActionConsts;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.utils.EnvironmentUtils;

public class CallStatusActivity extends BaseActivity implements View.OnClickListener {
    private TaskItem item;
    RadioButton ra_incoming,ra_connected,ra_finished;
    EditText editText;
    ImageView contact;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_call_status);
        try{getSupportActionBar().setDisplayHomeAsUpEnabled(true);}catch (Exception e){e.printStackTrace();}
        setToolBarAndStatusBarColor(null,getIntent().getStringExtra(EXTRA_TITLE_COLOR));
        item=(TaskItem) getIntent().getSerializableExtra(EXTRA_SERIALIZED_TASKITEM);
        ra_incoming=findViewById(R.id.call_status_incoming);
        ra_connected=findViewById(R.id.call_status_connected);
        ra_finished=findViewById(R.id.call_status_finished);
        editText=findViewById(R.id.call_status_edit);
        contact=findViewById(R.id.call_status_address);

        ra_incoming.setOnClickListener(this);
        ra_connected.setOnClickListener(this);
        ra_finished.setOnClickListener(this);
        contact.setOnClickListener(this);
        if(item.trigger_type!=TriggerTypeConsts.TRIGGER_TYPE_CALL_STATE_FINISHED&&item.trigger_type!=TriggerTypeConsts.TRIGGER_TYPE_CALL_STATE_CONNECTED
        &&item.trigger_type!=TriggerTypeConsts.TRIGGER_TYPE_CALL_STATE_INCOMING){
            item.trigger_type=TriggerTypeConsts.TRIGGER_TYPE_CALL_STATE_INCOMING;
        }

        StringBuilder builder=new StringBuilder();
        for(String s:item.call_state_numbers){
            builder.append(s);
            builder.append(",");
        }
        if(builder.toString().endsWith(","))builder.deleteCharAt(builder.lastIndexOf(","));
        editText.setText(builder.toString());

        refreshSelection();
    }


    @Override
    public void processMessage(Message msg){}

    @Override
    public void onClick(View v){
        switch (v.getId()){
            default:break;
            case R.id.call_status_incoming:{
                item.trigger_type= TriggerTypeConsts.TRIGGER_TYPE_CALL_STATE_INCOMING;
                refreshSelection();
            }
            break;
            case R.id.call_status_connected:{
                item.trigger_type=TriggerTypeConsts.TRIGGER_TYPE_CALL_STATE_CONNECTED;
                refreshSelection();
            }
            break;
            case R.id.call_status_finished:{
                item.trigger_type=TriggerTypeConsts.TRIGGER_TYPE_CALL_STATE_FINISHED;
                refreshSelection();
            }
            break;
            case R.id.call_status_address:{
                if(Build.VERSION.SDK_INT>=23&& PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)!=PermissionChecker.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},1);

                    EnvironmentUtils.PermissionRequestUtil.showSnackbarWithActionOfAppdetailPage(this,getResources().getString(R.string.permission_request_read_contacts)
                            ,getResources().getString(R.string.permission_grant_action_att));
                    return;
                }
                startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), 0);
            }
            break;
        }
    }

    private void refreshSelection(){
        ra_incoming.setChecked(item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_CALL_STATE_INCOMING);
        ra_connected.setChecked(item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_CALL_STATE_CONNECTED);
        ra_finished.setChecked(item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_CALL_STATE_FINISHED);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            default:break;
            case android.R.id.home: case R.id.action_menu_single_confirm:{
                finish();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.menu_single_confirm,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            default:break;
            case 0:{
                if(resultCode==RESULT_OK){
                    if(data==null) return;
                    Uri uri=data.getData();
                    if(uri==null) return;
                    try{
                        Cursor cursor = getContentResolver().query(uri,
                                new String[] { ContactsContract.Contacts._ID,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME },
                                null, null, null);
                        if(cursor==null) {
                            Log.e("Cursor ","cursor is null");
                            return;
                        }
                        while (cursor.moveToNext()) {
                            String id = cursor.getString(0);
                            Cursor phone=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,null,null);
                            if(phone==null) continue;
                            while (phone.moveToNext()){
                                String number=phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                String edit_read=editText.getText().toString();
                                if(edit_read.length()>0){
                                    if(!editText.getText().toString().endsWith(",")) editText.setText(edit_read+"," +number);
                                    else editText.setText(edit_read+number);
                                }
                                else editText.setText(number);
                            }
                            phone.close();
                            //String name = cursor.getString(1);
                        }
                        cursor.close();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void finish(){
        String number_value=editText.getText().toString().trim();
        if(!number_value.equals("")){
            try{
                item.call_state_numbers=number_value.split(",");
            }catch (Exception e){
                e.printStackTrace();
                item.call_state_numbers=new String[0];
            }
        }else {
            item.call_state_numbers=new String[0];
        }
        Intent data=new Intent();
        data.putExtra(EXTRA_SERIALIZED_TASKITEM,item);
        setResult(RESULT_OK,data);
        super.finish();
    }
}
