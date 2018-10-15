package com.github.ghmxr.timeswitch.ui;

import android.content.Context;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import com.github.ghmxr.timeswitch.R;

import java.util.Arrays;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class BottomDialogForInterval implements CustomTimePicker.OnTimeChangedListener, View.OnClickListener,WheelView.OnWheelViewListener{
    private Context context;
    private BottomDialog bottomDialog;
    private CustomTimePicker timePicker;
    private WheelView wheelView;
    private OnDialogConfirmedListener mListener;
    private long millis=1000*60*60;
    private int day=0,hour=1,minute=0;
    public BottomDialogForInterval(Context context){
        this.context=context;
        this.bottomDialog=new BottomDialog(this.context);
        bottomDialog.setContentView(R.layout.layout_dialog_setinterval);
        timePicker=bottomDialog.findViewById(R.id.dialog_setinterval_timepicker);
        timePicker.setIs24HourView(true);
        wheelView=bottomDialog.findViewById(R.id.dialog_setinterval_day);
        String [] items=new String[100];
        for(int i=0;i<items.length;i++) items[i]=""+i;
        wheelView.setItems(Arrays.asList(items));
    }

    public void setVariables(int day,int hour,int minute){
        this.day=day;
        this.hour=hour;
        this.minute=minute;
        refreshMillis();
    }

    public void setTitle(String title){
        TextView tv=bottomDialog.findViewById(R.id.dialog_setinterval_title);
        tv.setText(title);
    }

    public void show(){
        wheelView.setSeletion(this.day);
        if(Build.VERSION.SDK_INT>=23){
            timePicker.setHour(this.hour);
            timePicker.setMinute(this.minute);
        }
        else{
            timePicker.setCurrentHour(this.hour);
            timePicker.setCurrentMinute(this.minute);
        }
        timePicker.setOnTimeChangedListener(this);
        wheelView.setOnWheelViewListener(this);
        this.bottomDialog.findViewById(R.id.dialog_setinterval_button_cancel).setOnClickListener(this);
        this.bottomDialog.findViewById(R.id.dialog_setinterval_button_confirm).setOnClickListener(this);
        refreshMillis();
        refreshAtt();
        bottomDialog.show();
    }

    @Override
    public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
        this.hour=hourOfDay;
        this.minute=minute;
        refreshMillis();
        refreshAtt();
    }

    @Override
    public void onSelected(int selectedIndex, String item) {
       try{
           this.day=Integer.parseInt(item);
       }catch (Exception e){
           e.printStackTrace();
       }
       refreshMillis();
       refreshAtt();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            default:break;
            case R.id.dialog_setinterval_button_cancel:{
                this.bottomDialog.cancel();
            }
            break;
            case R.id.dialog_setinterval_button_confirm:{
                if(this.millis<=0){
                    Snackbar.make(view,context.getResources().getString(R.string.dialog_trigger_type_per_certain_time_more_than_zero),Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(mListener!=null){
                    mListener.onDialogConfirmed(this.millis);
                }
                this.bottomDialog.cancel();
            }
            break;
        }
    }

    public void setOnDialogConfirmedListener(OnDialogConfirmedListener listener){
        mListener=listener;
    }

    private void refreshMillis(){
        //Log.e("dialog","day="+day+" hour="+hour+" min="+minute);
        //Log.e("millis",""+millis);
        this.millis=(long)this.day*24*60*60*1000+(long)this.hour*60*60*1000+(long)this.minute*60*1000;
    }

    private void refreshAtt(){
        TextView tv=bottomDialog.findViewById(R.id.dialog_setinterval_att);
        StringBuilder value=new StringBuilder("");
        if(this.day!=0) {
            value.append(this.day
                +context.getResources().getString(R.string.day)
                +this.hour+context.getResources().getString(R.string.hour)
                +this.minute+context.getResources().getString(R.string.minute));
        }
        else if(this.hour!=0){
            value.append(this.hour+context.getResources().getString(R.string.hour)
            +this.minute+context.getResources().getString(R.string.minute));
        }else{
            value.append(this.minute+context.getResources().getString(R.string.minute));
        }
        tv.setText(value.toString());
    }

    public interface OnDialogConfirmedListener{
        void onDialogConfirmed(long millis);
    }
}
