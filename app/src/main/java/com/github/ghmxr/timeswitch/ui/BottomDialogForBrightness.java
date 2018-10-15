package com.github.ghmxr.timeswitch.ui;

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
import com.github.ghmxr.timeswitch.data.PublicConsts;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class BottomDialogForBrightness implements View.OnClickListener{
    private BottomDialog bottomDialog;
    private Context context;
    private RelativeLayout manual_area;
    private static final int SELECTION_UNSELECTED= PublicConsts.ACTION_BRIGHTNESS_UNSELECTED;
    private static final int SELECTION_AUTO=PublicConsts.ACTION_BRIGHTNESS_AUTO;
    private static final int BRIGHTNESS_MAX=PublicConsts.BRIGHTNESS_MAX;
    private int selection;
    private SeekBar seekBar;
    private CheckBox preview_cb;
    private boolean ifPreview=false;
    private OnDialogConfirmedListener mListener;
    private final int screen_mode_original;
    private final int screen_brightness_original;
    public BottomDialogForBrightness(Context context){
        this.context=context;
        this.bottomDialog=new BottomDialog(context);
        this.bottomDialog.setContentView(R.layout.layout_dialog_actions_selection_brightness);
        manual_area=bottomDialog.findViewById(R.id.bottom_dialog_action_selection_brightness_manual_panel);
        preview_cb=bottomDialog.findViewById(R.id.bottom_dialog_action_selection_brightness_manual_preview_cb);
        seekBar=bottomDialog.findViewById(R.id.bottom_dialog_action_selection_brightness_manual_seekbar);
        seekBar.setMax(BRIGHTNESS_MAX);
        screen_mode_original=getSystemScreenMode();
        screen_brightness_original=getSystemBrightness();
    }

    public void setVariables(int selection){
        this.selection=selection;
    }

    public void show(){

        preview_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ifPreview=b;
                if(!b) recoverDefaultBrightness();
                else if(selection>=0&&selection<=BRIGHTNESS_MAX)  setWindowBrightness(selection);
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                selection=i;
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
        bottomDialog.findViewById(R.id.bottom_dialog_action_selection_brightness_button_cancel).setOnClickListener(this);
        bottomDialog.findViewById(R.id.bottom_dialog_action_selection_brightness_button_confirm).setOnClickListener(this);
        bottomDialog.findViewById(R.id.bottom_dialog_action_selection_brightness_unselected).setOnClickListener(this);
        bottomDialog.findViewById(R.id.bottom_dialog_action_selection_brightness_auto).setOnClickListener(this);
        bottomDialog.findViewById(R.id.bottom_dialog_action_selection_brightness_manual).setOnClickListener(this);

        if(selection==PublicConsts.ACTION_BRIGHTNESS_UNSELECTED) onUnselectedAreaClicked();
        else if(selection==PublicConsts.ACTION_BRIGHTNESS_AUTO) onAutoAreaClicked();
        else onManualAreaClicked();

        bottomDialog.show();
        bottomDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
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
                bottomDialog.cancel();
            }
            break;
            case R.id.bottom_dialog_action_selection_brightness_button_confirm:{
                if(mListener!=null){
                    mListener.onConfirmed(selection);
                }
                bottomDialog.cancel();
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

    public void setOnDialogConfirmedListener(OnDialogConfirmedListener listener){
        mListener=listener;
    }

    private void onUnselectedAreaClicked(){
        refreshRadioButton(0);
        this.selection=SELECTION_UNSELECTED;
        manual_area.setVisibility(View.GONE);
        if(preview_cb.isChecked()) recoverDefaultBrightness();
    }

    private void onAutoAreaClicked(){
        refreshRadioButton(1);
        this.selection=SELECTION_AUTO;
        manual_area.setVisibility(View.GONE);
        if(preview_cb.isChecked()) recoverDefaultBrightness();
    }

    private void onManualAreaClicked(){
        refreshRadioButton(2);
        seekBar.setProgress((selection>BRIGHTNESS_MAX||selection==SELECTION_UNSELECTED)?125:(selection>=0?selection:125));
        this.selection=seekBar.getProgress();
        manual_area.setVisibility(View.VISIBLE);
        if(preview_cb.isChecked()) setWindowBrightness(selection);
    }

    /**
     * @param position 0-unselected,1-auto,2-manual
     */
    private void refreshRadioButton(int position){
        RadioButton ra_unselected=bottomDialog.findViewById(R.id.bottom_dialog_action_selection_brightness_unselected_rb);
        RadioButton ra_auto=bottomDialog.findViewById(R.id.bottom_dialog_action_selection_brightness_auto_rb);
        RadioButton ra_manual=bottomDialog.findViewById(R.id.bottom_dialog_action_selection_brightness_manual_rb);
        ra_unselected.setChecked(position==0);
        ra_auto.setChecked(position==1);
        ra_manual.setChecked(position==2);
    }

    private void setWindowBrightness(int brightness) {
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
    }

    private void recoverDefaultBrightness(){
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, screen_brightness_original);
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, screen_mode_original);
    }

    private int getSystemBrightness() {
        return Settings.System.getInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS, 125);
    }

    private int getSystemScreenMode(){
        return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,0);
    }


    public interface OnDialogConfirmedListener{
        void onConfirmed(int value);
    }
}
