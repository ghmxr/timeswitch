package com.github.ghmxr.timeswitch.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.SQLConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.ui.TaskGui;
import com.github.ghmxr.timeswitch.utils.MySQLiteOpenHelper;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class EditTask extends TaskGui {

    private int taskid=0;
    private TaskItem taskitem_read;

    public static final String TAG_EDITTASK_KEY  = "taskkey";
    public static final String TAG_SELECTED_ITEM_POSITION ="position";

    public static final int ACTIVITY_EDIT_RESULT_CANCEL         =   0x00000;
    public static final int ACTIVITY_EDIT_RESULT_SUCCESS        =   0x00001;

    @Override
    public void onCreate(Bundle mybundle) {
        super.onCreate(mybundle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void initialVariables() {
        this.taskid=getIntent().getIntExtra(TAG_EDITTASK_KEY,0);
        int position=getIntent().getIntExtra(TAG_SELECTED_ITEM_POSITION,-1);
        //int position=ProcessTaskItem.getPosition(taskid);
        taskitem_read=TimeSwitchService.list.get(position);
        taskitem=new TaskItem(taskitem_read);
        //if(taskid==0) taskid=taskitem.id;
        Log.i("EDIT","task id is "+taskitem.id);
        if(taskitem_read.trigger_type==PublicConsts.TRIGGER_TYPE_SINGLE||taskitem_read.trigger_type==PublicConsts.TRIGGER_TYPE_LOOP_WEEK) {
            calendar.setTimeInMillis(taskitem.time);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            checkIfChangedAndExit();
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
            taskitem.isenabled=true;
            if(saveTaskItem2DB(taskitem.id)==1){
                setResult(ACTIVITY_EDIT_RESULT_SUCCESS);
                finish();
            }
        }
        if(item.getItemId()==R.id.action_edit_delete){
            SQLiteDatabase database= MySQLiteOpenHelper.getInstance(this).getWritableDatabase();
            if(database.delete(SQLConsts.getCurrentTableName(this),SQLConsts.SQL_TASK_COLUMN_ID +"="+this.taskid,null)==1) {
                setResult(ACTIVITY_EDIT_RESULT_SUCCESS);
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
            showChangesNotSaveDialog();
            return;
        }
        setResult(ACTIVITY_EDIT_RESULT_CANCEL);
        finish();
    }

    private void showChangesNotSaveDialog(){
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
    }
}
