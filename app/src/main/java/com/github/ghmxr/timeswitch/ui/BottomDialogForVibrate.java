package com.github.ghmxr.timeswitch.ui;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.utils.LogUtil;

import java.util.Arrays;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class BottomDialogForVibrate implements View.OnClickListener{
    private BottomDialog dialog;
    private Context context;
    private boolean isEnabled=false;
    private int frequency=2;
    private int duration=500;
    private int interval=500;
    private BottomDialogForVibrateConfirmedListener listener;

    public BottomDialogForVibrate(Context context){
        this.context=context;
        this.dialog=new BottomDialog(context);
        dialog.setContentView(R.layout.layout_dialog_vibrate);
    }

    public void setVariables(boolean isEnabled,int frequency,int duration,int interval){
        this.isEnabled=isEnabled;
        if(frequency<=0||duration<=0||interval<=0) return;
        this.frequency=frequency;
        this.duration=duration;
        this.interval=interval;

    }

    public void show(){
        dialog.show();
        CheckBox cb_enabled=dialog.findViewById(R.id.dialog_vibrate_enable_cb);
        cb_enabled.setChecked(isEnabled);
        dialog.findViewById(R.id.dialog_vibrate_frequency).setOnClickListener(this);
        dialog.findViewById(R.id.dialog_vibrate_duration).setOnClickListener(this);
        dialog.findViewById(R.id.dialog_vibrate_interval).setOnClickListener(this);
        onCheckBoxCheckedChanged(isEnabled);
        cb_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onCheckBoxCheckedChanged(b);
            }
        });
        dialog.findViewById(R.id.dialog_action_vibrate_confirm).setOnClickListener(this);
        dialog.findViewById(R.id.dialog_action_vibrate_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            default:break;
            case R.id.dialog_vibrate_frequency:{
                refreshSelectionViews();
                dialog.findViewById(R.id.dialog_vibrate_frequency_arrow).setVisibility(View.VISIBLE);
                dialog.findViewById(R.id.dialog_vibrate_frequency_area).setVisibility(View.VISIBLE);
                WheelView wheelView=dialog.findViewById(R.id.dialog_vibrate_frequency_wheelview);
                String[] frequencies=new String[9];
                for(int i=0;i<frequencies.length;i++) frequencies[i]=String.valueOf(i+1);
                wheelView.setItems(Arrays.asList(frequencies));
                int selection=this.frequency-1;
                if(selection>=0)wheelView.setSeletion(selection);
                wheelView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                    @Override
                    public void onSelected(int selectedIndex, String item) {
                        try{
                            frequency=Integer.parseInt(item);
                            ((TextView)dialog.findViewById(R.id.dialog_vibrate_frequency_value)).setText(frequency+context.getResources().getString(R.string.dialog_actions_vibrate_frequency_measure));
                        }catch (Exception e){
                            e.printStackTrace();
                            LogUtil.putExceptionLog(context,e);
                        }
                    }
                });
            }
            break;
            case R.id.dialog_vibrate_duration:{
                refreshSelectionViews();
                dialog.findViewById(R.id.dialog_vibrate_duration_arrow).setVisibility(View.VISIBLE);
                dialog.findViewById(R.id.dialog_vibrate_duration_area).setVisibility(View.VISIBLE);
                WheelView wheelView=dialog.findViewById(R.id.dialog_vibrate_duration_wheelview);
                String[] durations=new String[30];
                for(int i=0;i<durations.length;i++) durations[i]=String.valueOf((i+1)*100);
                wheelView.setItems(Arrays.asList(durations));
                int selection=(this.duration/100)-1;
                if(selection>=0)wheelView.setSeletion(selection);
                wheelView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                    @Override
                    public void onSelected(int selectedIndex, String item) {
                        try{
                            duration=Integer.parseInt(item);
                            ((TextView)dialog.findViewById(R.id.dialog_vibrate_duration_value)).setText(duration+context.getResources().getString(R.string.dialog_actions_vibrate_duration_measure));
                        }catch (Exception e){
                            e.printStackTrace();
                            LogUtil.putExceptionLog(context,e);
                        }
                    }
                });
            }
            break;
            case R.id.dialog_vibrate_interval:{
                refreshSelectionViews();
                dialog.findViewById(R.id.dialog_vibrate_interval_arrow).setVisibility(View.VISIBLE);
                dialog.findViewById(R.id.dialog_vibrate_interval_area).setVisibility(View.VISIBLE);
                WheelView wheelView=dialog.findViewById(R.id.dialog_vibrate_interval_wheelview);
                final String[] intervals=new String[30];
                for(int i=0;i<intervals.length;i++) intervals[i]=String.valueOf((i+1)*100);
                wheelView.setItems(Arrays.asList(intervals));
                int selection=(this.interval/100)-1;
                if(selection>=0)wheelView.setSeletion(selection);
                wheelView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                    @Override
                    public void onSelected(int selectedIndex, String item) {
                        try{
                            interval=Integer.parseInt(item);
                            ((TextView)dialog.findViewById(R.id.dialog_vibrate_interval_value)).setText(interval+context.getResources().getString(R.string.dialog_actions_vibrate_interval_measure));
                        }catch (Exception e){
                            e.printStackTrace();
                            LogUtil.putExceptionLog(context,e);
                        }
                    }
                });
            }
            break;
            case R.id.dialog_action_vibrate_confirm:{
                dialog.cancel();
                if(listener!=null) {
                   // if(!isEnabled) listener.onBottomDialogForVibrateConfirmed(false,);
                    listener.onBottomDialogForVibrateConfirmed(this.isEnabled,this.frequency,this.duration,this.interval);
                }
            }
            break;
            case R.id.dialog_action_vibrate_cancel:{
                dialog.cancel();
            }
        }
    }

    private void onCheckBoxCheckedChanged(boolean b){
        this.isEnabled=b;
        dialog.findViewById(R.id.dialog_vibrate_frequency).setClickable(b);
        dialog.findViewById(R.id.dialog_vibrate_duration).setClickable(b);
        dialog.findViewById(R.id.dialog_vibrate_interval).setClickable(b);
        ((TextView)dialog.findViewById(R.id.dialog_vibrate_frequency_att)).setTextColor(b?context.getResources().getColor(R.color.color_text_normal):context.getResources().getColor(R.color.color_text_disabled));
        ((TextView)dialog.findViewById(R.id.dialog_vibrate_duration_att)).setTextColor(b?context.getResources().getColor(R.color.color_text_normal):context.getResources().getColor(R.color.color_text_disabled));
        ((TextView)dialog.findViewById(R.id.dialog_vibrate_interval_att)).setTextColor(b?context.getResources().getColor(R.color.color_text_normal):context.getResources().getColor(R.color.color_text_disabled));
        ((TextView)dialog.findViewById(R.id.dialog_vibrate_frequency_value)).setText(b?this.frequency+context.getResources().getString(R.string.dialog_actions_vibrate_frequency_measure):"");
        ((TextView)dialog.findViewById(R.id.dialog_vibrate_duration_value)).setText(b?this.duration+context.getResources().getString(R.string.dialog_actions_vibrate_interval_measure):"");
        ((TextView)dialog.findViewById(R.id.dialog_vibrate_interval_value)).setText(b?this.interval+context.getResources().getString(R.string.dialog_actions_vibrate_interval_measure):"");
        refreshSelectionViews();
    }

    private void refreshSelectionViews(){
        dialog.findViewById(R.id.dialog_vibrate_frequency_area).setVisibility(View.GONE);
        dialog.findViewById(R.id.dialog_vibrate_duration_area).setVisibility(View.GONE);
        dialog.findViewById(R.id.dialog_vibrate_interval_area).setVisibility(View.GONE);
        dialog.findViewById(R.id.dialog_vibrate_frequency_arrow).setVisibility(View.INVISIBLE);
        dialog.findViewById(R.id.dialog_vibrate_duration_arrow).setVisibility(View.INVISIBLE);
        dialog.findViewById(R.id.dialog_vibrate_interval_arrow).setVisibility(View.INVISIBLE);
    }



    public void setOnBottomDialogForVibrateConfirmedListener(BottomDialogForVibrateConfirmedListener listener){
        this.listener=listener;
    }

    public interface BottomDialogForVibrateConfirmedListener{
        void onBottomDialogForVibrateConfirmed(boolean isEnabled,int frequency,int duration,int interval);
    }

}
