package com.github.ghmxr.timeswitch.ui;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.ActionConsts;

/**
 * �򿪡��رա���ѡ�����ĵײ�Dialog
 */
public class BottomDialogWith3Selections extends BottomDialog implements View.OnClickListener{

    /**
     *
     * @param context ��ʾ��dialog��activity
     * @param icon_open_res open��Ϊ��ͼ��
     * @param icon_close_res close ��Ϊ��ͼ��
     * @param selection ���dialog�ĳ�ʼѡ��,ֵ�ο�ActionConsts
     */
    public BottomDialogWith3Selections(Context context,int icon_open_res,int icon_close_res,int selection){
        super(context);
        setContentView(R.layout.layout_dialog_actions_3selections);
        ((ImageView)findViewById(R.id.selection_area_open_icon)).setImageResource(icon_open_res);
        ((ImageView)findViewById(R.id.selection_area_close_icon)).setImageResource(icon_close_res);
        findViewById(R.id.selection_area_open).setOnClickListener(this);
        findViewById(R.id.selection_area_close).setOnClickListener(this);
        findViewById(R.id.selection_area_unselected).setOnClickListener(this);
        switch (selection){
            default:break;
            case ActionConsts.ActionValueConsts.ACTION_OPEN:{
                ((RadioButton)findViewById(R.id.selection_area_open_rb)).setChecked(true);
            }
            break;
            case ActionConsts.ActionValueConsts.ACTION_CLOSE:{
                ((RadioButton)findViewById(R.id.selection_area_close_rb)).setChecked(true);
            }
            break;
            case ActionConsts.ActionValueConsts.ACTION_UNSELECTED:{
                ((RadioButton)findViewById(R.id.selection_area_unselected_rb)).setChecked(true);
            }
            break;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            default:break;
            case R.id.selection_area_open:{
                cancel();
                if(callBack!=null) callBack.onDialogConfirmed(String.valueOf(ActionConsts.ActionValueConsts.ACTION_OPEN));
            }
            break;
            case R.id.selection_area_close:{
                cancel();
                if(callBack!=null) callBack.onDialogConfirmed(String.valueOf(ActionConsts.ActionValueConsts.ACTION_CLOSE));
            }
            break;
            case R.id.selection_area_unselected:{
                if(callBack!=null) callBack.onDialogConfirmed(String.valueOf(ActionConsts.ActionValueConsts.ACTION_UNSELECTED));
                cancel();
            }
            break;
        }
    }


}
