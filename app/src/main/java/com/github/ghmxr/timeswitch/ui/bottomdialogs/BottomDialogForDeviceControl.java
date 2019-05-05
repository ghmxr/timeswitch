package com.github.ghmxr.timeswitch.ui.bottomdialogs;

import android.content.Context;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.R;

public class BottomDialogForDeviceControl extends BottomDialogWith3Selections {
    /**
     * @param selection -1-unselected , 0-restart , 1 - power off
     */
    public BottomDialogForDeviceControl(Context context,int selection) {
        super(context,R.drawable.icon_reboot,R.drawable.icon_power,selection);
        ((TextView)findViewById(R.id.selection_area_open_att)).setText(getContext().getResources().getString(R.string.reboot));
        ((TextView)findViewById(R.id.selection_area_close_att)).setText(getContext().getResources().getString(R.string.shut_down));
    }
}
