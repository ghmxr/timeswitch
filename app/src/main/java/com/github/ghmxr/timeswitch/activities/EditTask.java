package com.github.ghmxr.timeswitch.activities;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.SQLConsts;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.ui.TaskGui;
import com.github.ghmxr.timeswitch.utils.MySQLiteOpenHelper;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class EditTask extends TaskGui {

    //private int taskid=0;
    private TaskItem taskitem_read;

    //public static final String TAG_EDITTASK_KEY  = "taskkey";
    public static final String TAG_SELECTED_ITEM_POSITION ="position";

    //public static final int ACTIVITY_EDIT_RESULT_CANCEL         =   0x00000;
    //public static final int ACTIVITY_EDIT_RESULT_SUCCESS        =   0x00001;

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
        //this.taskid=getIntent().getIntExtra(TAG_EDITTASK_KEY,0);
        int position=getIntent().getIntExtra(TAG_SELECTED_ITEM_POSITION,-1);
        //int position=ProcessTaskItem.getPosition(taskid);
        if(position<0) {
            Toast.makeText(this,"Can not get the task position",Toast.LENGTH_SHORT).show();
            return;
        }
        try{
            taskitem_read=TimeSwitchService.list.get(position);
            taskitem=new TaskItem(taskitem_read);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
            finish();
        }
        //if(taskid==0) taskid=taskitem.id;
        Log.i("EDIT","task id is "+taskitem.id);
        //if(taskitem_read.trigger_type==PublicConsts.TRIGGER_TYPE_SINGLE||taskitem_read.trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_WEEK) {
            //calendar.setTimeInMillis(taskitem.time);
       // }
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
            SQLiteDatabase database= MySQLiteOpenHelper.getInstance(this).getWritableDatabase();
            if(database.delete(MySQLiteOpenHelper.getCurrentTableName(this),SQLConsts.SQL_TASK_COLUMN_ID +"="+taskitem.id,null)==1) {
                setResult(RESULT_OK);
                this.finish();
            }
            else {
                Toast.makeText(this, "Task does not exist", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkIfChangedAndExit(){
        boolean isChanged=!taskitem_read.toString().equals(taskitem.toString());
        //Log.e("isChanged",""+isChanged);
        if(isChanged){
            Log.i("taskitem_old",taskitem_read.toString());
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

    /*private void showChangesNotSaveDialog(){
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.dialog_edit_changed_not_saved_title))
                .setIcon(R.drawable.icon_warn)
                .setMessage(getResources().getString(R.string.dialog_edit_changed_not_saved_message))
                .setPositiveButton(getResources().getString(R.string.dialog_button_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setResult(ACTIVITY_EDIT_RESULT_CANCEL);
                        finish();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.dialog_button_negative), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                })
                .show();
    }  */
}
