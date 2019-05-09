package com.github.ghmxr.timeswitch.activities;

import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.R;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */

public class AddTask extends TaskGui{
    private String checkString;
    //public static final int ACTIVITY_ADD_RESULT_CANCEL  =   0x00000;
    //public static final int ACTIVITY_ADD_RESULT_SUCCESS =   0x00001;
    private long first_clicked_back=0;

    @Override
	public void onCreate(Bundle mybundle){
		super.onCreate(mybundle);
		try{this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);}catch (Exception e){e.printStackTrace();}
	}

	@Override
	public void initialVariables() {
		// TODO Auto-generated method stub
        if(TimeSwitchService.list==null){
            taskitem.name=getResources().getString(R.string.activity_add_name_mask)+1;
            return;
        }
        taskitem.name=getResources().getString(R.string.activity_add_name_mask)+(TimeSwitchService.list.size()+1);
        checkString=taskitem.toString();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add, menu);
        return true;
    }

    private void checkAndExit(){
        long current_time=System.currentTimeMillis();
        if(!taskitem.toString().equals(checkString)){
            if(current_time-first_clicked_back>1000){
                first_clicked_back=current_time;
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK){
            checkAndExit();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_save) {
           if(saveTaskItem2DB(null)!=-1){
               setResult(RESULT_OK);
               finish();
               return true;
           }
        }

        if(id==android.R.id.home){
            checkAndExit();
        }
        return super.onOptionsItemSelected(item);
    }


}
