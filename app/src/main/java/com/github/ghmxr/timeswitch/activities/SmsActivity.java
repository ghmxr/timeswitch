package com.github.ghmxr.timeswitch.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.ActionConsts;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.utils.EnvironmentUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class SmsActivity extends BaseActivity {
    private TaskItem item;
    boolean enabled=false;
    private EditText edit_addresses;
    private EditText edit_message;
    private android.support.v7.widget.AppCompatSpinner spinner;

    private static final int REQUEST_CODE_SELECT_CONTACTS=0;
    private int subid=-1; //for the widget can not save the subscription id;
    CheckBox cb_receipt_toast;
    //public static final String SPLIT_RECEIVERS=",";
    /**
     * @deprecated
     */
    private String checkString="";
    private long first_click_back_time=0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_sms);
        Toolbar toolbar=findViewById(R.id.sms_toolbar);
        setSupportActionBar(toolbar);
        try{getSupportActionBar().setDisplayHomeAsUpEnabled(true);}catch (Exception e){e.printStackTrace();}
        setToolBarAndStatusBarColor(toolbar,getIntent().getStringExtra(EXTRA_TITLE_COLOR));
        edit_addresses=findViewById(R.id.layout_sms_edit_address);
        edit_message=findViewById(R.id.layout_sms_edit_message);
        spinner=findViewById(R.id.layout_sms_spinner);
        cb_receipt_toast=findViewById(R.id.layout_sms_toast_cb);
        try{
            item=(TaskItem) getIntent().getSerializableExtra(EXTRA_SERIALIZED_TASKITEM);
            String sms_values[]=item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SMS_LOCALE].split(PublicConsts.SEPARATOR_SECOND_LEVEL);
            enabled=Integer.parseInt(sms_values[ActionConsts.ActionSecondLevelLocaleConsts.SMS_ENABLED_LOCALE])>=0;
            subid=Integer.parseInt(sms_values[ActionConsts.ActionSecondLevelLocaleConsts.SMS_SUBINFO_LOCALE]);
            edit_addresses.setText(item.sms_address);
            edit_message.setText(item.sms_message);
            edit_addresses.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    item.sms_address=s.toString();
                }
            });
            edit_message.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    item.sms_message=s.toString();
                    Log.d("sms_ms",item.sms_message);
                }
            });

            if(Build.VERSION.SDK_INT>=22){
                final List<SubscriptionInfo> list_subinfo=EnvironmentUtils.getAvailableSubscribtionInfos(this);
                List<String> displayNames=new ArrayList<>();
                int selection=0;
                if(list_subinfo!=null&&list_subinfo.size()>0){
                    boolean isSelected=false;
                    for(int i=0;i<list_subinfo.size();i++){
                        displayNames.add("Sim "+(i+1)+":"+list_subinfo.get(i).getDisplayName().toString());
                        if(subid==list_subinfo.get(i).getSubscriptionId()){
                            selection=i;
                            isSelected=true;
                        }
                    }
                    if(!isSelected) subid=list_subinfo.get(selection).getSubscriptionId();
                }
                else{
                    displayNames.add(getResources().getString(R.string.subinfo_no_sim_found));
                    selection=0;
                    if(PermissionChecker.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)!=PermissionChecker.PERMISSION_GRANTED){
                        Toast.makeText(this,getResources().getString(R.string.activity_sms_toast_permission_read_phone_state_att),Toast.LENGTH_SHORT).show();
                    }
                }
                spinner.setAdapter(new ArrayAdapter<>(this,R.layout.item_subinfo,R.id.item_subinfo_textview,displayNames));
                spinner.setSelection(selection);
                spinner.setEnabled(list_subinfo!=null&&list_subinfo.size()>0);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    @TargetApi(22)
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if(list_subinfo!=null&&list_subinfo.size()>0) subid=list_subinfo.get(i).getSubscriptionId();
                        //item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SMS_LOCALE]
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }else{
                spinner.setAdapter(new ArrayAdapter<>(this,R.layout.item_subinfo,R.id.item_subinfo_textview,new String[]{getResources().getString(R.string.subinfo_default_low_api)}));
                spinner.setSelection(0);
                spinner.setEnabled(false);
            }
            cb_receipt_toast.setChecked(Integer.parseInt(sms_values[ActionConsts.ActionSecondLevelLocaleConsts.SMS_RESULT_TOAST_LOCALE])>=0);

        }catch (Exception e){
            e.printStackTrace();
        }
        final SwitchCompat switchCompat=(findViewById(R.id.layout_sms_switch));
        switchCompat.setChecked(enabled);
        setSmsAreaVisible(switchCompat.isChecked());
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setSmsAreaVisible(b);
                enabled=b;
            }
        });
        findViewById(R.id.layout_sms_switch_area).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchCompat.toggle();
            }
        });
        findViewById(R.id.layout_sms_address_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT>=23&&PermissionChecker.checkSelfPermission(SmsActivity.this, Manifest.permission.READ_CONTACTS)!=PermissionChecker.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},1);
                    /*Snackbar snackbar=Snackbar.make(findViewById(R.id.layout_sms_root),getResources().getString(R.string.permission_request_read_contacts),Snackbar.LENGTH_SHORT);
                    snackbar.setAction(getResources().getString(R.string.permission_grant_action_att), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent appdetail = new Intent();
                            appdetail.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            appdetail.setData(Uri.fromParts("package", getApplication().getPackageName(), null));
                            startActivity(appdetail);
                        }
                    });
                    snackbar.show();*/
                    EnvironmentUtils.PermissionRequestUtil.showSnackbarWithActionOfAppdetailPage(SmsActivity.this,getResources().getString(R.string.permission_request_read_contacts)
                            ,getResources().getString(R.string.permission_grant_action_att));
                    return;
                }
                startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_SELECT_CONTACTS);
            }
        });
    }

    private void setSmsAreaVisible(boolean b){
        findViewById(R.id.layout_sms_area).setVisibility(b? View.VISIBLE:View.GONE);
    }

    @Override
    public void processMessage(Message msg) {}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            default:break;
            case REQUEST_CODE_SELECT_CONTACTS:{
                if(resultCode==RESULT_OK){
                    if(data==null) return;
                    Uri uri=data.getData();
                    if(uri==null) return;
                   // Log.e("uri!!!",uri.toString());
                    try{
                       // Log.e("uri!!!","uri is quering");
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
                                String edit_read=edit_addresses.getText().toString();
                                if(edit_read.length()>0){
                                    if(!edit_read.substring(edit_read.length()-1).equals(PublicConsts.SEPARATOR_SMS_RECEIVERS)) edit_addresses.setText(edit_read+PublicConsts.SEPARATOR_SMS_RECEIVERS +number);
                                    else edit_addresses.setText(edit_read+number);
                                }
                                else edit_addresses.setText(number);
                                //else  edit_addresses.setText(edit_read+number);
                                Log.i("GOT NUMBER",number);
                            }
                            phone.close();
                            String name = cursor.getString(1);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sms,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            default:break;
            case android.R.id.home:{
                checkAndFinish();
            }
            break;
            case R.id.action_sms_confirm:{
                checkAndFinish();
            }
        }
        return super.onOptionsItemSelected(item);
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
        String addresses=edit_addresses.getText().toString().trim();
        if(addresses.trim().equals("")&&enabled){
            long time=System.currentTimeMillis();
            if(time-first_click_back_time>1000){
                first_click_back_time=time;
                Snackbar.make(findViewById(android.R.id.content),"没有收件人号码，再按一次舍弃修改并退出",Snackbar.LENGTH_SHORT).show();
                return;
            }else{
                setResult(RESULT_CANCELED);
                finish();
            }
        }
        String sms_values=String.valueOf(enabled?0:-1)+PublicConsts.SEPARATOR_SECOND_LEVEL
                +String.valueOf(subid)+PublicConsts.SEPARATOR_SECOND_LEVEL
                +String.valueOf(cb_receipt_toast.isChecked()?0:-1);
        item.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SMS_LOCALE]=sms_values;
        Intent intent=new Intent();
        intent.putExtra(EXTRA_SERIALIZED_TASKITEM,item);
        setResult(RESULT_OK,intent);
        finish();
    }
}
