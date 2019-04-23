package com.github.ghmxr.timeswitch.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.ActionConsts;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;

public class BottomDialogForNotification {
    private BottomDialog dialog;
    private int type=-1,if_custom=0;
    //private String title,message;
    private DialogConfirmedCallback callback;
    /**
     * @param values type:if_custom , default is -1:-1
     */
    public BottomDialogForNotification(Context context, String values, @NonNull String title, @NonNull String message) {
        dialog=new BottomDialog(context);
        //int type=-1;
        //int if_custom=0;
        try{
            String[] read=values.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            type=Integer.parseInt(read[0]);
            if_custom=Integer.parseInt(read[1]);
        }catch (Exception e){e.printStackTrace();}
        dialog.setContentView(R.layout.layout_dialog_notification);

        final RadioButton ra_unselected=dialog.findViewById(R.id.dialog_notification_selection_unselected_ra);
        final RadioButton ra_not_override=dialog.findViewById(R.id.dialog_notification_selection_not_override_ra);
        final RadioButton ra_override_last=dialog.findViewById(R.id.dialog_notification_selection_override_last_ra);
        final RadioButton ra_default=dialog.findViewById(R.id.dialog_notification_operation_default_ra);
        final RadioButton ra_custom=dialog.findViewById(R.id.dialog_notification_operation_custom_ra);
        final RelativeLayout operation_area=dialog.findViewById(R.id.dialog_notification_operation_area);
        final EditText edit_title=dialog.findViewById(R.id.dialog_notification_operation_custom_edit_title);
        final EditText edit_message=dialog.findViewById(R.id.dialog_notification_operation_custom_edit_message);
        edit_title.setText(title.trim());
        edit_message.setText(message.trim());
        (ra_unselected).setChecked(type== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_UNSELECTED);
        (ra_not_override).setChecked(type== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_NOT_OVERRIDE);
        (ra_override_last).setChecked(type== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_OVERRIDE_LAST);
        (operation_area).setVisibility(type== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_UNSELECTED? View.GONE:View.VISIBLE);


        (dialog.findViewById(R.id.dialog_notification_selection_unselected)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomDialogForNotification.this.type=-1;
                (ra_unselected).setChecked(true);
                ra_not_override.setChecked(false);
                ra_override_last.setChecked(false);
                (operation_area).setVisibility(View.GONE);
            }
        });

        (dialog.findViewById(R.id.dialog_notification_selection_override_last)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomDialogForNotification.this.type=ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_OVERRIDE_LAST;
                (ra_unselected).setChecked(false);
                ra_not_override.setChecked(false);
                ra_override_last.setChecked(true);
                (operation_area).setVisibility(View.VISIBLE);

            }
        });

        dialog.findViewById(R.id.dialog_notification_selection_not_override).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomDialogForNotification.this.type=ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_NOT_OVERRIDE;
                (ra_unselected).setChecked(false);
                ra_not_override.setChecked(true);
                ra_override_last.setChecked(false);
                (operation_area).setVisibility(View.VISIBLE);
            }
        });

        ra_default.setChecked(if_custom== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_CONTENT_DEFAULT);
        ra_custom.setChecked(if_custom== ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_CONTENT_CUSTOM);
        edit_title.setEnabled(ra_custom.isChecked());
        edit_message.setEnabled(ra_custom.isChecked());
        ra_custom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                edit_title.setEnabled(b);
                edit_message.setEnabled(b);
            }
        });
        (dialog.findViewById(R.id.dialog_notification_button_confirm)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                //int type=-1;
                //if(ra_unselected.isChecked()) type= ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_UNSELECTED;
                //if(ra_override_last.isChecked()) type= ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_OVERRIDE_LAST;
                //if(ra_not_override.isChecked()) type= ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_NOT_OVERRIDE;
                if_custom=ra_custom.isChecked()? ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_CONTENT_CUSTOM : ActionConsts.ActionValueConsts.NOTIFICATION_TYPE_CONTENT_DEFAULT;

                if(callback!=null){
                    String[] result=new String[3];
                    result[0]=type+":"+if_custom;
                    result[1]=edit_title.getText().toString();
                    result[2]=edit_message.getText().toString();
                    callback.onDialogConfirmed(result);
                }
            }
        });
        (dialog.findViewById(R.id.dialog_notification_button_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

    }


    public void show(){
        dialog.show();
    }

    public void setOnDialogConfirmedCallback(DialogConfirmedCallback callback){
        this.callback=callback;
    }

    public interface DialogConfirmedCallback{
        /**
         * @param result result[0]-values   result[1]-title  result[2]-message
         */
         void onDialogConfirmed(String[] result);
    }
}
