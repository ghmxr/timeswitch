package com.github.ghmxr.timeswitch.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.MySQLiteOpenHelper;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class EditTaskActivity extends TaskGui {

    private String checkString;
    private long first_click_time_back =0;
    private long first_click_time_delete=0;
    @Override
    public void onCreate(Bundle mybundle) {
        super.onCreate(mybundle);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void initialVariables() {
        taskitem=(TaskItem) getIntent().getSerializableExtra(EXTRA_SERIALIZED_TASKITEM);
        if(taskitem.trigger_type!= TriggerTypeConsts.TRIGGER_TYPE_SINGLE&&taskitem.trigger_type!=TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME
        &&taskitem.trigger_type!=TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK){
            taskitem.time=System.currentTimeMillis()+10*60*1000;
        }
        checkString=taskitem.toString();
        isTaskNameEdited=true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            checkIfChangedAndExit();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            checkIfChangedAndExit();
        }

        if(item.getItemId()==R.id.action_edit_save){
            if(saveTaskItem2DB(taskitem.id)==1){
                setResult(RESULT_OK);
                finish();
            }
        }
        if(item.getItemId()==R.id.action_edit_delete){
            long clickedTime=System.currentTimeMillis();
            if(clickedTime-first_click_time_delete>1000){
                first_click_time_delete=clickedTime;
                Snackbar.make(findViewById(R.id.layout_taskgui_root),getResources().getString(R.string.dialog_profile_delete_confirm),Snackbar.LENGTH_SHORT).show();
                return false;
            }

            try{
                MySQLiteOpenHelper.deleteRow(this,MySQLiteOpenHelper.getCurrentTableName(this),taskitem.id);
                setResult(RESULT_OK);
                finish();
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkIfChangedAndExit(){
        boolean isChanged=!taskitem.toString().equals(checkString);
        //Log.e("isChanged",""+isChanged);
        if(isChanged){
            Log.i("checkString",checkString);
            Log.i("taskitem_new",taskitem.toString());
            //showChangesNotSaveDialog();
            long currentTime=System.currentTimeMillis();
            if(currentTime- first_click_time_back >1000){
                first_click_time_back =currentTime;
                Snackbar.make(findViewById(R.id.layout_taskgui_root),getResources().getString(R.string.snackbar_changes_not_saved_back),Snackbar.LENGTH_SHORT).show();
                return;
            }
            setResult(RESULT_CANCELED);
            finish();
        }else{
            setResult(RESULT_CANCELED);
            finish();
        }
    }

}
