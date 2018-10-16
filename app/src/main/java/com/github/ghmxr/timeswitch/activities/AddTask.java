package com.github.ghmxr.timeswitch.activities;

import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.ui.TaskGui;
import com.github.ghmxr.timeswitch.R;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */

public class AddTask extends TaskGui{

    public static final int ACTIVITY_ADD_RESULT_CANCEL  =   0x00000;
    public static final int ACTIVITY_ADD_RESULT_SUCCESS =   0x00001;

    @Override
	public void onCreate(Bundle mybundle){
		super.onCreate(mybundle);
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void initialVariables() {
		// TODO Auto-generated method stub
        if(TimeSwitchService.list==null) return;
        int maxPos=TimeSwitchService.list.size()-1;
        if(maxPos<0) maxPos=0;
        taskitem.name="新建任务"+(TimeSwitchService.list.get(maxPos).id+1);//this.taskname="新建任务";
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add, menu);
        return true;
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
               setResult(ACTIVITY_ADD_RESULT_SUCCESS);
               finish();
               return true;
           }
        }

        if(id==android.R.id.home){
            setResult(ACTIVITY_ADD_RESULT_CANCEL);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


}
