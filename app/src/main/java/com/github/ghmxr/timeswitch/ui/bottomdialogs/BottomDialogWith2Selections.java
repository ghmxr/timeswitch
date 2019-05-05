package com.github.ghmxr.timeswitch.ui.bottomdialogs;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.R;

public class BottomDialogWith2Selections extends BottomDialog implements View.OnClickListener{

    /**
     * @param selection -1 for unselected
     */
    public BottomDialogWith2Selections(Context context,int icon_selected_res,String selected_att ,int selection) {
        super(context);
        setContentView(R.layout.layout_dialog_actions_2selections);
        ((RadioButton)findViewById(R.id.dialog_2selections_unselected_rb)).setChecked(selection==-1);
        ((RadioButton)findViewById(R.id.dialog_2selections_select_rb)).setChecked(selection>=0);
        findViewById(R.id.dialog_2selections_unselected).setOnClickListener(this);
        findViewById(R.id.dialog_2selections_select).setOnClickListener(this);
        ((ImageView)findViewById(R.id.dialog_2selections_select_icon)).setImageResource(icon_selected_res);
        ((TextView)findViewById(R.id.dialog_2selections_select_att)).setText(selected_att);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            default:break;
            case R.id.dialog_2selections_unselected:{
                if(callBack!=null) callBack.onDialogConfirmed(String.valueOf(-1));
                cancel();
            }
            break;
            case R.id.dialog_2selections_select:{
                if(callBack!=null) callBack.onDialogConfirmed(String.valueOf(0));
                cancel();
            }
        }
    }
}
