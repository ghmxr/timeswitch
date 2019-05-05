package com.github.ghmxr.timeswitch.ui.bottomdialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.provider.Settings;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.github.ghmxr.timeswitch.R;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class BottomDialogForBrightness extends BottomDialog implements View.OnClickListener{

    private RelativeLayout manual_area;
    private static final int SELECTION_UNSELECTED= -1;
    private static final int SELECTION_AUTO= 256;
    private static final int BRIGHTNESS_MAX= 255;
    private int brightness;
    private SeekBar seekBar;
    private CheckBox preview_cb;
    private boolean ifPreview=false;
    private final int screen_mode_original;
    private final int screen_brightness_original;

    /**
     * @param brightness 对话框亮度初始值，0~255，256表示自动,-1为未选择
     */
    public BottomDialogForBrightness(Context context, int brightness){
        super(context);
        this.brightness=brightness;
        setContentView(R.layout.layout_dialog_actions_selection_brightness);
        manual_area=findViewById(R.id.bottom_dialog_action_selection_brightness_manual_panel);
        preview_cb=findViewById(R.id.bottom_dialog_action_selection_brightness_manual_preview_cb);
        seekBar=findViewById(R.id.bottom_dialog_action_selection_brightness_manual_seekbar);
        seekBar.setMax(BRIGHTNESS_MAX);
        screen_mode_original=getSystemScreenMode();
        screen_brightness_original=getSystemBrightness();
        preview_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ifPreview=b;
                if(!b) recoverDefaultBrightness();
                else if(BottomDialogForBrightness.this.brightness >=0&& BottomDialogForBrightness.this.brightness <=BRIGHTNESS_MAX)  setWindowBrightness(BottomDialogForBrightness.this.brightness);
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                BottomDialogForBrightness.this.brightness =i;
                if(ifPreview){
                    setWindowBrightness(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        findViewById(R.id.bottom_dialog_action_selection_brightness_button_cancel).setOnClickListener(this);
        findViewById(R.id.bottom_dialog_action_selection_brightness_button_confirm).setOnClickListener(this);
        findViewById(R.id.bottom_dialog_action_selection_brightness_unselected).setOnClickListener(this);
        findViewById(R.id.bottom_dialog_action_selection_brightness_auto).setOnClickListener(this);
        findViewById(R.id.bottom_dialog_action_selection_brightness_manual).setOnClickListener(this);

        if(this.brightness == -1) onUnselectedAreaClicked();
        else if(this.brightness == 256) onAutoAreaClicked();
        else onManualAreaClicked();
    }


    public void show(){
        super.show();
        setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if(preview_cb.isChecked()) recoverDefaultBrightness();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            default:break;
            case R.id.bottom_dialog_action_selection_brightness_button_cancel:{
                cancel();
            }
            break;
            case R.id.bottom_dialog_action_selection_brightness_button_confirm:{
                if(callBack!=null){
                    callBack.onDialogConfirmed(String.valueOf(brightness));
                }
                cancel();
            }
            break;
            case R.id.bottom_dialog_action_selection_brightness_unselected:{
                onUnselectedAreaClicked();
            }
            break;
            case R.id.bottom_dialog_action_selection_brightness_auto:{
                onAutoAreaClicked();
            }
            break;
            case R.id.bottom_dialog_action_selection_brightness_manual:{
                onManualAreaClicked();
            }
            break;
        }

    }

    private void onUnselectedAreaClicked(){
        refreshRadioButton(0);
        this.brightness =SELECTION_UNSELECTED;
        manual_area.setVisibility(View.GONE);
        if(preview_cb.isChecked()) recoverDefaultBrightness();
    }

    private void onAutoAreaClicked(){
        refreshRadioButton(1);
        this.brightness =SELECTION_AUTO;
        manual_area.setVisibility(View.GONE);
        if(preview_cb.isChecked()) recoverDefaultBrightness();
    }

    private void onManualAreaClicked(){
        refreshRadioButton(2);
        if(brightness>=0&&brightness<=255) seekBar.setProgress(brightness);
        else {
            brightness=getSystemBrightness();
            seekBar.setProgress(brightness);
        }
        manual_area.setVisibility(View.VISIBLE);
        if(preview_cb.isChecked()) setWindowBrightness(brightness);
    }

    /**
     * @param position 0-unselected,1-auto,2-manual
     */
    private void refreshRadioButton(int position){
        RadioButton ra_unselected=findViewById(R.id.bottom_dialog_action_selection_brightness_unselected_rb);
        RadioButton ra_auto=findViewById(R.id.bottom_dialog_action_selection_brightness_auto_rb);
        RadioButton ra_manual=findViewById(R.id.bottom_dialog_action_selection_brightness_manual_rb);
        ra_unselected.setChecked(position==0);
        ra_auto.setChecked(position==1);
        ra_manual.setChecked(position==2);
    }

    private void setWindowBrightness(int brightness) {
        try{
            Settings.System.putInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            Settings.System.putInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void recoverDefaultBrightness(){
        try{
            Settings.System.putInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, screen_brightness_original);
            Settings.System.putInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, screen_mode_original);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private int getSystemBrightness() {
        return Settings.System.getInt(getContext().getContentResolver(),Settings.System.SCREEN_BRIGHTNESS, 125);
    }

    private int getSystemScreenMode(){
        return Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,0);
    }

}
