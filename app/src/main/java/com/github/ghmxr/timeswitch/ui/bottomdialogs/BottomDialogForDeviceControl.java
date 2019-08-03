package com.github.ghmxr.timeswitch.ui.bottomdialogs;

import android.content.Context;
import android.view.View;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.ActionConsts;

public class BottomDialogForDeviceControl extends BottomDialogWith3Selections {
    /**
     * @param selection -1-unselected , 0-restart , 1 - power off
     */
    public BottomDialogForDeviceControl(Context context,int selection) {
        super(context,context.getResources().getString(R.string.activity_taskgui_actions_device_control)
                ,selection,R.drawable.icon_power,R.drawable.icon_reboot
                ,R.drawable.icon_unselected,context.getResources().getString(R.string.shut_down)
                ,context.getResources().getString(R.string.reboot),context.getResources().getString(R.string.unselected));

    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            default:break;
            case R.id.selection_area_close:{
                cancel();
                if(callBack!=null) callBack.onDialogConfirmed(String.valueOf(ActionConsts.ActionValueConsts.ACTION_DEVICE_CONTROL_REBOOT));
            }
            break;
            case R.id.selection_area_open:{
                cancel();
                if(callBack!=null) callBack.onDialogConfirmed(String.valueOf(ActionConsts.ActionValueConsts.ACTION_DEVICE_CONTROL_SHUTDOWN));
            }
            break;
            case R.id.selection_area_unselected:{
                if(callBack!=null) callBack.onDialogConfirmed(String.valueOf(ActionConsts.ActionValueConsts.ACTION_UNSELECTED));
                cancel();
            }
            break;
        }
    }
}
