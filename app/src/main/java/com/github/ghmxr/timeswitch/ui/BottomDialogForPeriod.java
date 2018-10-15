package com.github.ghmxr.timeswitch.ui;

import android.content.Context;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class BottomDialogForPeriod implements View.OnClickListener,CompoundButton.OnCheckedChangeListener,TimePicker.OnTimeChangedListener{
    private BottomDialog bottomDialog;
    private CustomTimePicker timePicker;
    private Context context;
    private OnDialogConfirmedListener mConfirmedListener;

    private boolean isEnabled;

    private static final int STATE_START_TIME=0x0001;
    private static final int STATE_END_TIME=0x0002;

    private int current_state;

    private int start_hour=12;
    private int start_minute=0;
    private int end_hour=18;
    private int end_minute=0;

    public BottomDialogForPeriod(Context context){
        this.context=context;
        this.bottomDialog=new BottomDialog(this.context);
        bottomDialog.setContentView(R.layout.layout_dialog_exceptions_period);
        timePicker=bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_timepicker);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(this);
        bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_start_time).setOnClickListener(this);
        bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_button_confirm).setOnClickListener(this);
        bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_button_cancel).setOnClickListener(this);
        bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_end_time).setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(compoundButton.getId()==R.id.bottom_dialog_exceptions_period_enabled){
            this.isEnabled=b;
            setClickAreaEnabled(b);
        }
    }

    @Override
    public void onClick(View view) {
       switch (view.getId()){
           default:break;
           case R.id.bottom_dialog_exceptions_period_start_time: {
                onClickStartTimeArea();
           }
           break;
           case R.id.bottom_dialog_exceptions_period_end_time:{
                onClickEndTimeArea();
           }
           break;
           case R.id.bottom_dialog_exceptions_period_button_confirm:{
               if(this.start_hour==this.end_hour&&this.start_minute==this.end_minute){
                   Snackbar.make(view,context.getResources().getString(R.string.dialog_exceptions_period_same_time),Snackbar.LENGTH_SHORT).show();
                   return;
               }
               if(mConfirmedListener!=null){
                   mConfirmedListener.onConfirmed(this.isEnabled,this.start_hour,this.start_minute,this.end_hour,this.end_minute);
               }
               bottomDialog.cancel();
           }
           break;
           case R.id.bottom_dialog_exceptions_period_button_cancel:{
                bottomDialog.cancel();
           }
           break;
       }

    }

    @Override
    public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
        switch (current_state){
            default:break;
            case STATE_START_TIME:{
                this.start_hour=hourOfDay;
                this.start_minute=minute;
                TextView tv_value_start=bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_start_time_value);
                tv_value_start.setText( ValueUtils.format(this.start_hour)+":"+ ValueUtils.format(this.start_minute));
            }
            break;
            case STATE_END_TIME:{
                this.end_hour=hourOfDay;
                this.end_minute=minute;
                TextView tv_value_end=bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_end_time_value);
                tv_value_end.setText( ValueUtils.format(this.end_hour)+":"+ ValueUtils.format(this.end_minute));
            }
            break;
        }
        //Log.e("OnTimeChanged!!","startHour:"+this.start_hour+" startMin:"+this.start_minute+" endHour"+this.end_hour+" endMin:"+this.end_minute);
    }

    public void show(){
        CheckBox cb_enabled=bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_enabled);
        cb_enabled.setChecked(this.isEnabled);
        cb_enabled.setOnCheckedChangeListener(this);

        TextView tv_value_start=bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_start_time_value);
        TextView tv_value_end=bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_end_time_value);

        tv_value_start.setText( ValueUtils.format(this.start_hour)+":"+ ValueUtils.format(this.start_minute));
        tv_value_end.setText( ValueUtils.format(this.end_hour)+":"+ ValueUtils.format(this.end_minute));

        setClickAreaEnabled(this.isEnabled);

        this.bottomDialog.show();
    }

    public void cancel(){
        this.bottomDialog.cancel();
    }

    public void setTitle(String title){
        TextView tv_title=bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_title);
        tv_title.setText(title);
    }

    public void setOnDialogConfirmedListener(OnDialogConfirmedListener listener){
        mConfirmedListener=listener;
    }

    public void setVariables(boolean isEnabled,int start_hour,int start_minute,int end_hour,int end_minute){
        this.isEnabled=isEnabled;
        this.start_hour=start_hour;
        this.start_minute=start_minute;
        this.end_hour=end_hour;
        this.end_minute=end_minute;
    }

    private void onClickStartTimeArea(){
        current_state=STATE_START_TIME;
        bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_start_time_arrow).setVisibility(View.VISIBLE);
        bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_end_time_arrow).setVisibility(View.INVISIBLE);

        TextView tv_att_start=bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_start_time_att);
        TextView tv_att_end=bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_end_time_att);

        tv_att_start.setTextColor(context.getResources().getColor(R.color.color_item_selected));
        tv_att_end.setTextColor(context.getResources().getColor(R.color.color_text_normal));

        timePicker.setOnTimeChangedListener(null);
        if(Build.VERSION.SDK_INT<23){
            timePicker.setCurrentHour(this.start_hour);
            timePicker.setCurrentMinute(this.start_minute);
        }else{
            timePicker.setHour(this.start_hour);
            timePicker.setMinute(this.start_minute);
        }
        timePicker.setVisibility(View.VISIBLE);
        timePicker.setOnTimeChangedListener(this);
        //Log.e("Area_start","startHour:"+this.start_hour+" startMin:"+this.start_minute+" endHour"+this.end_hour+" endMin:"+this.end_minute);
    }

    private void onClickEndTimeArea(){
        current_state=STATE_END_TIME;
        bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_start_time_arrow).setVisibility(View.INVISIBLE);
        bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_end_time_arrow).setVisibility(View.VISIBLE);

        TextView tv_att_start=bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_start_time_att);
        TextView tv_att_end=bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_end_time_att);

        tv_att_start.setTextColor(context.getResources().getColor(R.color.color_text_normal));
        tv_att_end.setTextColor(context.getResources().getColor(R.color.color_item_selected));

        timePicker.setOnTimeChangedListener(null);
        if (Build.VERSION.SDK_INT < 23) {
            timePicker.setCurrentHour(this.end_hour);
            timePicker.setCurrentMinute(this.end_minute);
        }else{
            timePicker.setHour(this.end_hour);
            timePicker.setMinute(this.end_minute);
        }
        timePicker.setOnTimeChangedListener(this);
        timePicker.setVisibility(View.VISIBLE);
        //Log.e("Area_end","startHour:"+this.start_hour+" startMin:"+this.start_minute+" endHour"+this.end_hour+" endMin:"+this.end_minute);
    }

    private void setClickAreaEnabled(boolean isEnabled){
        bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_start_time).setClickable(isEnabled);
        bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_end_time).setClickable(isEnabled);
        if(!isEnabled) {
            bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_start_time_arrow).setVisibility(View.INVISIBLE);
            bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_end_time_arrow).setVisibility(View.INVISIBLE);
            timePicker.setVisibility(View.GONE);
        }

        TextView tv_att_start=bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_start_time_att);
        TextView tv_value_start=bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_start_time_value);
        TextView tv_att_end=bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_end_time_att);
        TextView tv_value_end=bottomDialog.findViewById(R.id.bottom_dialog_exceptions_period_end_time_value);

        tv_att_start.setTextColor(isEnabled? context.getResources().getColor(R.color.color_text_normal):context.getResources().getColor(R.color.color_text_disabled));
        tv_value_start.setVisibility(isEnabled? View.VISIBLE:View.GONE);
        tv_att_end.setTextColor(isEnabled? context.getResources().getColor(R.color.color_text_normal):context.getResources().getColor(R.color.color_text_disabled));
        tv_value_end.setVisibility(isEnabled?View.VISIBLE:View.GONE);
    }

    public interface OnDialogConfirmedListener{
        void onConfirmed(boolean isEnabled,int start_hour,int start_minute,int end_hour,int end_minute);
    }

}
