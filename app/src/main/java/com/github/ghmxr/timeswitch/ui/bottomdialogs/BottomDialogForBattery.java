package com.github.ghmxr.timeswitch.ui.bottomdialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.ui.WheelView;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class BottomDialogForBattery {
    public BottomDialog bottomDialog;
    public TextView textview_cancel,textview_confirm,textview_first_description,textview_second_description,textview_title;
    public CheckBox checkbox_enable;
    public WheelView wheelview_first,wheelview_second;
    public BottomDialogForBattery(Context context) {
        bottomDialog=new BottomDialog(context);
        View dialogview= LayoutInflater.from(context).inflate(R.layout.layout_dialog_exceptions_battery,null);
        textview_cancel=dialogview.findViewById(R.id.bottom_dialog_button_cancel);
        textview_confirm=dialogview.findViewById(R.id.bottom_dialog_button_confirm);
        textview_title=dialogview.findViewById(R.id.bottom_dialog_title);
        checkbox_enable=dialogview.findViewById(R.id.bottom_dialog_checkbox);
        textview_first_description=dialogview.findViewById(R.id.bottom_dialog_wheelview_first_selection_description);
        textview_second_description=dialogview.findViewById(R.id.bottom_dialog_wheelview_second_selection_description);
        wheelview_first=dialogview.findViewById(R.id.bottom_dialog_wheelview_first_selection);
        wheelview_second=dialogview.findViewById(R.id.bottom_dialog_wheelview_second_selection);

        //checkbox_enable.setChecked(true);

        bottomDialog.setContentView(dialogview);
        this.textview_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomDialogForBattery.this.bottomDialog.cancel();
            }
        });
    }

    public void show(){
        /*checkbox_enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                wheelview_first.setEnabled(b);
                wheelview_second.setEnabled(b);
            }
        });
        wheelview_first.setEnabled(checkbox_enable.isChecked());
        wheelview_second.setEnabled(checkbox_enable.isChecked());  */
        this.bottomDialog.show();
    }

    public void cancel(){
        this.bottomDialog.cancel();
    }
}
