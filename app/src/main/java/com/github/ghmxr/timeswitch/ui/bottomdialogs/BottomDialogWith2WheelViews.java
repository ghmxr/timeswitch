package com.github.ghmxr.timeswitch.ui.bottomdialogs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.ui.WheelView;

import java.util.List;

public abstract class BottomDialogWith2WheelViews extends BottomDialog implements View.OnClickListener{
    CheckBox checkbox_enable;
    WheelView wheelview_first,wheelview_second;

    public BottomDialogWith2WheelViews(@NonNull Context context, @NonNull String title,String description_of_end,boolean isEnabled) {
        super(context);
        setContentView(R.layout.layout_dialog_exceptions_battery);

        TextView textview_cancel=findViewById(R.id.bottom_dialog_button_cancel);
        TextView textview_confirm=findViewById(R.id.bottom_dialog_button_confirm);
        TextView textview_title=findViewById(R.id.bottom_dialog_title);
        checkbox_enable=findViewById(R.id.bottom_dialog_checkbox);
        //TextView textview_first_description=findViewById(R.id.bottom_dialog_wheelview_first_selection_description);
        TextView textview_second_description=findViewById(R.id.bottom_dialog_wheelview_second_selection_description);
        wheelview_first=findViewById(R.id.bottom_dialog_wheelview_first_selection);
        wheelview_second=findViewById(R.id.bottom_dialog_wheelview_second_selection);

        textview_cancel.setOnClickListener(this);
        textview_confirm.setOnClickListener(this);

        textview_title.setText(title);
        textview_second_description.setText(description_of_end);
        wheelview_first.setItems(getFirstWheelViewSelectionItems(context));
        wheelview_second.setItems(getSecondWheelViewSelectionItems());

        checkbox_enable.setChecked(isEnabled);
        wheelview_first.setEnabled(isEnabled);
        wheelview_second.setEnabled(isEnabled);

        checkbox_enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                wheelview_first.setEnabled(b);
                wheelview_second.setEnabled(b);
            }
        });

    }

    public abstract @NonNull List<String> getFirstWheelViewSelectionItems(Context context);

    public abstract @NonNull List<String> getSecondWheelViewSelectionItems();

    public abstract @NonNull Object getFirstSelectionValue();

    public abstract @NonNull Object getSecondSelectionValue();

    @Override
    public void onClick(View v){
        switch (v.getId()){
            default:break;
            case R.id.bottom_dialog_button_cancel:cancel();break;
            case R.id.bottom_dialog_button_confirm:{
                if(callBack!=null){
                    callBack.onDialogConfirmed(checkbox_enable.isChecked()? getFirstSelectionValue()+","+ getSecondSelectionValue():"-1");
                }
                cancel();
            }
            break;
        }
    }
}
