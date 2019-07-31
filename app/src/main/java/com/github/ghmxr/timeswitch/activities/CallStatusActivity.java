package com.github.ghmxr.timeswitch.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
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
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;

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
