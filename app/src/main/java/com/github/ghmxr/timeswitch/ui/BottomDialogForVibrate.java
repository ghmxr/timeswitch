package com.github.ghmxr.timeswitch.ui;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.ExceptionConsts;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;

import java.util.Arrays;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class BottomDialogForVibrate extends BottomDialog implements View.OnClickListener{

    private boolean isEnabled=false;
    private int frequency=1;
    private int duration=500;
    private int interval=500;
    private BottomDialogForVibrateConfirmedListener listener;

    /**
     * @deprecated
     */
    public BottomDialogForVibrate(Context context){
        this(context,"2:500:500");
    }

    /**
     * @param values frequency:duration:interval
     */
    public BottomDialogForVibrate(Context context,String values){
        super(context);
        setContentView(R.layout.layout_dialog_vibrate);
        try{
            String[] v=values.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
            int frequency=Integer.parseInt(v[0]);
            int duration=Integer.parseInt(v[1]);
            int interval=Integer.parseInt(v[2]);
            if(frequency>0) {
                this.frequency=frequency;
                this.duration=duration;
                this.interval=interval;
                isEnabled=true;
            }
        }catch (Exception e){e.printStackTrace();}

    }

    /**
     * @deprecated
     */
    public void setVariables(boolean isEnabled,int frequency,int duration,int interval){
        this.isEnabled=isEnabled;
        if(frequency<=0||duration<=0||interval<=0) return;
        this.frequency=frequency;
        this.duration=duration;
        this.interval=interval;

    }

    @Override
    public void show(){
        super.show();
        CheckBox cb_enabled=findViewById(R.id.dialog_vibrate_enable_cb);
        cb_enabled.setChecked(isEnabled);
        findViewById(R.id.dialog_vibrate_frequency).setOnClickListener(this);
        findViewById(R.id.dialog_vibrate_duration).setOnClickListener(this);
        findViewById(R.id.dialog_vibrate_interval).setOnClickListener(this);
        onCheckBoxCheckedChanged(isEnabled);
        cb_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onCheckBoxCheckedChanged(b);
            }
        });
        findViewById(R.id.dialog_action_vibrate_confirm).setOnClickListener(this);
        findViewById(R.id.dialog_action_vibrate_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            default:break;
            case R.id.dialog_vibrate_frequency:{
                refreshSelectionViews();
                findViewById(R.id.dialog_vibrate_frequency_arrow).setVisibility(View.VISIBLE);
                findViewById(R.id.dialog_vibrate_frequency_area).setVisibility(View.VISIBLE);
                WheelView wheelView=findViewById(R.id.dialog_vibrate_frequency_wheelview);
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
                            ((TextView)findViewById(R.id.dialog_vibrate_frequency_value)).setText(frequency+getContext().getResources().getString(R.string.dialog_actions_vibrate_frequency_measure));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
            break;
            case R.id.dialog_vibrate_duration:{
                refreshSelectionViews();
                findViewById(R.id.dialog_vibrate_duration_arrow).setVisibility(View.VISIBLE);
                findViewById(R.id.dialog_vibrate_duration_area).setVisibility(View.VISIBLE);
                WheelView wheelView=findViewById(R.id.dialog_vibrate_duration_wheelview);
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
                            ((TextView)findViewById(R.id.dialog_vibrate_duration_value)).setText(duration+getContext().getResources().getString(R.string.dialog_actions_vibrate_duration_measure));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
            break;
            case R.id.dialog_vibrate_interval:{
                refreshSelectionViews();
                findViewById(R.id.dialog_vibrate_interval_arrow).setVisibility(View.VISIBLE);
                findViewById(R.id.dialog_vibrate_interval_area).setVisibility(View.VISIBLE);
                WheelView wheelView=findViewById(R.id.dialog_vibrate_interval_wheelview);
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
                            ((TextView)findViewById(R.id.dialog_vibrate_interval_value)).setText(interval+getContext().getResources().getString(R.string.dialog_actions_vibrate_interval_measure));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
            break;
            case R.id.dialog_action_vibrate_confirm:{
                cancel();
                if(listener!=null) {
                   // if(!isEnabled) listener.onBottomDialogForVibrateConfirmed(false,);
                    listener.onBottomDialogForVibrateConfirmed(this.isEnabled,this.frequency,this.duration,this.interval);
                }
                if(callBack!=null) {
                    if(!isEnabled) callBack.onDialogConfirmed("-1:-1:-1");
                    else callBack.onDialogConfirmed(String.valueOf(frequency)+":"+String.valueOf(duration)+":"+String.valueOf(interval));
                }
            }
            break;
            case R.id.dialog_action_vibrate_cancel:{
                cancel();
            }
        }
    }

    private void onCheckBoxCheckedChanged(boolean b){
        this.isEnabled=b;
        findViewById(R.id.dialog_vibrate_frequency).setClickable(b);
        findViewById(R.id.dialog_vibrate_duration).setClickable(b);
        findViewById(R.id.dialog_vibrate_interval).setClickable(b);
        ((TextView)findViewById(R.id.dialog_vibrate_frequency_att)).setTextColor(b?getContext().getResources().getColor(R.color.color_text_normal):getContext().getResources().getColor(R.color.color_text_disabled));
        ((TextView)findViewById(R.id.dialog_vibrate_duration_att)).setTextColor(b?getContext().getResources().getColor(R.color.color_text_normal):getContext().getResources().getColor(R.color.color_text_disabled));
        ((TextView)findViewById(R.id.dialog_vibrate_interval_att)).setTextColor(b?getContext().getResources().getColor(R.color.color_text_normal):getContext().getResources().getColor(R.color.color_text_disabled));
        ((TextView)findViewById(R.id.dialog_vibrate_frequency_value)).setText(b?this.frequency+getContext().getResources().getString(R.string.dialog_actions_vibrate_frequency_measure):"");
        ((TextView)findViewById(R.id.dialog_vibrate_duration_value)).setText(b?this.duration+getContext().getResources().getString(R.string.dialog_actions_vibrate_interval_measure):"");
        ((TextView)findViewById(R.id.dialog_vibrate_interval_value)).setText(b?this.interval+getContext().getResources().getString(R.string.dialog_actions_vibrate_interval_measure):"");
        refreshSelectionViews();
    }

    private void refreshSelectionViews(){
        findViewById(R.id.dialog_vibrate_frequency_area).setVisibility(View.GONE);
        findViewById(R.id.dialog_vibrate_duration_area).setVisibility(View.GONE);
        findViewById(R.id.dialog_vibrate_interval_area).setVisibility(View.GONE);
        findViewById(R.id.dialog_vibrate_frequency_arrow).setVisibility(View.INVISIBLE);
        findViewById(R.id.dialog_vibrate_duration_arrow).setVisibility(View.INVISIBLE);
        findViewById(R.id.dialog_vibrate_interval_arrow).setVisibility(View.INVISIBLE);
    }

    /**
     * @deprecated
     */
    public void setOnBottomDialogForVibrateConfirmedListener(BottomDialogForVibrateConfirmedListener listener){
        this.listener=listener;
    }

    /**
     * @deprecated
     */
    public interface BottomDialogForVibrateConfirmedListener{
        void onBottomDialogForVibrateConfirmed(boolean isEnabled,int frequency,int duration,int interval);
    }

}
