package com.github.ghmxr.timeswitch.ui;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.ActionConsts;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;

public class BottomDialogForToast {
    private BottomDialog dialog;
    private DialogConfirmedCallback callBack;
    public BottomDialogForToast(final Activity activity, String values, String toast){
        dialog=new BottomDialog(activity);
        dialog.setContentView(R.layout.layout_dialog_toast);
        final CheckBox cb_enabled=dialog.findViewById(R.id.dialog_toast_cb);
        final RadioGroup rg=dialog.findViewById(R.id.dialog_toast_rg);
        final RadioButton ra_location_default=dialog.findViewById(R.id.dialog_toast_default_ra);
        final RadioButton ra_location_custom=dialog.findViewById(R.id.dialog_toast_custom_ra);
        final SeekBar sb_x=dialog.findViewById(R.id.dialog_toast_x_sb);
        final SeekBar sb_y=dialog.findViewById(R.id.dialog_toast_y_sb);
        TextView tv_x_center=dialog.findViewById(R.id.dialog_toast_x_center);
        TextView tv_y_center=dialog.findViewById(R.id.dialog_toast_y_center);
        final EditText editText=dialog.findViewById(R.id.dialog_toast_edittext);
        final LinearLayout location_selection_area=dialog.findViewById(R.id.dialog_toast_location);
        final Button button=dialog.findViewById(R.id.dialog_toast_preview);
        try{
            String toast_values[]=values.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            boolean isenabled=Integer.parseInt(toast_values[ActionConsts.ActionSecondLevelLocaleConsts.TOAST_TYPE_LOCALE])>=0;
            int type=Integer.parseInt(toast_values[ActionConsts.ActionSecondLevelLocaleConsts.TOAST_TYPE_LOCALE]);
            cb_enabled.setChecked(isenabled);
            rg.setVisibility(isenabled? View.VISIBLE:View.GONE);
            editText.setText(toast);
            editText.setEnabled(isenabled);
            button.setEnabled(isenabled);
            ra_location_custom.setChecked(type== ActionConsts.ActionValueConsts.TOAST_TYPE_CUSTOM);
            location_selection_area.setVisibility(isenabled?(type== ActionConsts.ActionValueConsts.TOAST_TYPE_CUSTOM?View.VISIBLE:View.GONE):View.GONE);
            cb_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    rg.setVisibility(b?View.VISIBLE:View.GONE);
                    editText.setEnabled(b);
                    button.setEnabled(b);
                    location_selection_area.setVisibility(b?(ra_location_custom.isChecked()?View.VISIBLE:View.GONE):View.GONE);
                }
            });

            ra_location_custom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    ra_location_default.setChecked(!b);
                    location_selection_area.setVisibility(b?View.VISIBLE:View.GONE);
                    sb_x.setEnabled(b);
                    sb_y.setEnabled(b);
                }
            });

            final int x_max=activity.getWindowManager().getDefaultDisplay().getWidth();
            final int y_max=activity.getWindowManager().getDefaultDisplay().getHeight();
            sb_x.setMax(x_max);
            sb_y.setMax(y_max);
            int progress_x=Integer.parseInt(toast_values[ActionConsts.ActionSecondLevelLocaleConsts.TOAST_LOCATION_X_OFFSET_LOCALE]);
            if(progress_x>=0&&progress_x<=x_max) sb_x.setProgress(progress_x);
            int progress_y=Integer.parseInt(toast_values[ActionConsts.ActionSecondLevelLocaleConsts.TOAST_LOCATION_Y_OFFSET_LOCALE]);
            if(progress_y>=0&&progress_y<=y_max) sb_y.setProgress(progress_y);

            tv_x_center.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sb_x.setProgress(x_max/2);
                }
            });
            tv_y_center.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sb_y.setProgress(y_max/2);
                }
            });
            editText.setText(toast);
            dialog.findViewById(R.id.dialog_toast_preview).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast toast=Toast.makeText(activity,editText.getText().toString(),Toast.LENGTH_SHORT);
                    if(ra_location_custom.isChecked()) toast.setGravity(Gravity.TOP|Gravity.START, sb_x.getProgress(),sb_y.getProgress());
                    toast.show();
                }
            });
            dialog.findViewById(R.id.dialog_toast_confirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.cancel();
                    String [] result=new String[2];
                    result[0]=String.valueOf(!cb_enabled.isChecked()?-1:
                            (ra_location_custom.isChecked()?1:0))+PublicConsts.SEPARATOR_SECOND_LEVEL
                            +String.valueOf(sb_x.getProgress())+PublicConsts.SEPARATOR_SECOND_LEVEL
                            +String.valueOf(sb_y.getProgress());
                    result[1]=editText.getText().toString();
                    if(callBack!=null) callBack.onDialogConfirmed(result);
                }
            });
            dialog.findViewById(R.id.dialog_toast_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.cancel();
                }
            });
        }catch (Exception e){e.printStackTrace();}
    }

    public void show(){
        dialog.show();
    }

    public void setOnDialogConfirmedCallback(DialogConfirmedCallback callback){
        this.callBack=callback;
    }

    public interface DialogConfirmedCallback{
        /**
         * @param result result[0]-value , result[1]-toast
         */
        void onDialogConfirmed(String[] result);
    }
}
