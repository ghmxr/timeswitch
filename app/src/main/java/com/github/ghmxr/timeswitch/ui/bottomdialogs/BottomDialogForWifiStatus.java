package com.github.ghmxr.timeswitch.ui.bottomdialogs;

import android.content.Context;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.ExceptionConsts;

public class BottomDialogForWifiStatus extends BottomDialogWith3Selections {
    public BottomDialogForWifiStatus(Context context, int head) {
        super(context, R.drawable.icon_wifi_connected, R.drawable.icon_wifi_disconnected, -1);
        ((TextView)findViewById(R.id.selection_area_open_att)).setText(context.getResources().getString(R.string.exception_wifi_status_connected));
        ((TextView)findViewById(R.id.selection_area_close_att)).setText(context.getResources().getString(R.string.exception_wifi_status_disconnected));
        ((RadioButton)findViewById(R.id.selection_area_open_rb)).setChecked(head>=0||head== ExceptionConsts.EXCEPTION_WIFI_VALUE_CONNECTED_TO_RANDOM_SSID);
        ((RadioButton)findViewById(R.id.selection_area_close_rb)).setChecked(head==ExceptionConsts.EXCEPTION_WIFI_VALUE_DISCONNECTED);
        ((RadioButton)findViewById(R.id.selection_area_unselected_rb)).setChecked(head==-1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            default:break;
            case R.id.selection_area_open:{
                cancel();
                if(callBack!=null) callBack.onDialogConfirmed(String.valueOf(ExceptionConsts.EXCEPTION_WIFI_VALUE_CONNECTED_TO_RANDOM_SSID));
            }
            break;
            case R.id.selection_area_close:{
                cancel();
                if(callBack!=null) callBack.onDialogConfirmed(String.valueOf(ExceptionConsts.EXCEPTION_WIFI_VALUE_DISCONNECTED));
            }
            break;
            case R.id.selection_area_unselected:{
                if(callBack!=null) callBack.onDialogConfirmed(String.valueOf(-1));
                cancel();
            }
            break;
        }
    }
}
