package com.github.ghmxr.timeswitch.ui.bottomdialogs;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.ActionConsts;

/**
 * 三个选项的底部Dialog
 */
public class BottomDialogWith3Selections extends BottomDialog implements View.OnClickListener{

    /**
     *打开、关闭、不选择此项三项
     * @param context 显示此dialog的activity
     * @param icon_open_res open行为的图标
     * @param icon_close_res close 行为的图标
     * @param selection 这个dialog的初始选项,值参考ActionConsts
     */
    public BottomDialogWith3Selections(Context context,int icon_open_res,int icon_close_res,int selection){
        this(context,null,selection,icon_open_res,icon_close_res,R.drawable.icon_unselected,context.getResources().getString(R.string.open)
                ,context.getResources().getString(R.string.close),context.getResources().getString(R.string.unselected));
    }

    /**
     * 自定义三项底部Dialog
     * @param context 显示此dialog的context
     * @param selection 选项。-1为第三项，0为第二项，1为第一项。参考{@link ActionConsts}
     * @param icon_1 第一个图标res值
     * @param icon_2 第二个图标res值
     * @param icon_3 第三个图标res值
     * @param action_1 第一个行为描述
     * @param action_2 第二个行为描述
     * @param action_3 第三个行为描述
     */
    public BottomDialogWith3Selections (Context context, @Nullable String title, int selection, int icon_1, int icon_2, int icon_3, String action_1, String action_2, String action_3){
        super(context);
        setContentView(R.layout.layout_dialog_actions_3selections);
        ((ImageView)findViewById(R.id.selection_area_open_icon)).setImageResource(icon_1);
        ((ImageView)findViewById(R.id.selection_area_close_icon)).setImageResource(icon_2);
        ((ImageView)findViewById(R.id.selection_area_unselected_icon)).setImageResource(icon_3);
        ((TextView)findViewById(R.id.selection_area_open_att)).setText(action_1);
        ((TextView)findViewById(R.id.selection_area_close_att)).setText(action_2);
        ((TextView)findViewById(R.id.selection_area_unselected_att)).setText(action_3);
        findViewById(R.id.selection_area_open).setOnClickListener(this);
        findViewById(R.id.selection_area_close).setOnClickListener(this);
        findViewById(R.id.selection_area_unselected).setOnClickListener(this);
        if(title!=null){
            TextView dialog_title=findViewById(R.id.selection_title);
            dialog_title.setVisibility(View.VISIBLE);
            dialog_title.setText(title);
        }
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
