package com.github.ghmxr.timeswitch.ui;

import android.content.Context;
import android.view.View;
import android.widget.RadioButton;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.ActionConsts;

public class BottomDialogForRingMode extends BottomDialog implements View.OnClickListener{

    /**
     * 铃声模式选项dialog
     * @param context 打开此dialog的activity
     * @param selection 初选项
     */
    public BottomDialogForRingMode(Context context, int selection){
        super(context);
        setContentView(R.layout.layout_dialog_actions_selection_ring_mode);
        findViewById(R.id.selection_ring_area_vibrate).setOnClickListener(this);
        findViewById(R.id.selection_ring_area_off).setOnClickListener(this);
        findViewById(R.id.selection_ring_area_normal).setOnClickListener(this);
        findViewById(R.id.selection_ring_area_unselected).setOnClickListener(this);
        switch (selection){
            default:break;
            case ActionConsts.ActionValueConsts.ACTION_RING_VIBRATE:{
                ((RadioButton)findViewById(R.id.selection_ring_area_vibrate_rb)).setChecked(true);
            }
            break;
            case ActionConsts.ActionValueConsts.ACTION_RING_OFF:{
                ((RadioButton)findViewById(R.id.selection_ring_area_off_rb)).setChecked(true);
            }
            break;
            case ActionConsts.ActionValueConsts.ACTION_RING_NORMAL:{
                ((RadioButton)findViewById(R.id.selection_ring_area_normal_rb)).setChecked(true);
            }
            break;
            case ActionConsts.ActionValueConsts.ACTION_RING_UNSELECTED:{
                ((RadioButton)findViewById(R.id.selection_ring_area_unselected_rb)).setChecked(true);
            }
            break;
        }
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            default:break;
            case R.id.selection_ring_area_vibrate:{
                cancel();
                if(callBack!=null) callBack.onDialogConfirmed(String.valueOf(ActionConsts.ActionValueConsts.ACTION_RING_VIBRATE));
            }
            break;
            case R.id.selection_ring_area_off:{
                cancel();
                if(callBack!=null) callBack.onDialogConfirmed(String.valueOf(ActionConsts.ActionValueConsts.ACTION_RING_OFF));
            }
            break;
            case R.id.selection_ring_area_normal:{
                cancel();
                if(callBack!=null) callBack.onDialogConfirmed(String.valueOf(ActionConsts.ActionValueConsts.ACTION_RING_NORMAL));
            }
            break;
            case R.id.selection_ring_area_unselected:{
                cancel();
                if(callBack!=null) callBack.onDialogConfirmed(String.valueOf(ActionConsts.ActionValueConsts.ACTION_RING_UNSELECTED));
            }
            break;
        }
    }

}
