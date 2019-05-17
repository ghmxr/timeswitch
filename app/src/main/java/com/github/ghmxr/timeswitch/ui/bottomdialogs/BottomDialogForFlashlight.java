package com.github.ghmxr.timeswitch.ui.bottomdialogs;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.utils.EnvironmentUtils;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.util.Arrays;

public class BottomDialogForFlashlight extends BottomDialog implements View.OnClickListener {

    private long[] values;
    private EditText editText;
    private RadioButton ra_unselected,ra_custom;
    private TextView unit;
    private Button preview;
    private ViewGroup edit_area;

    public BottomDialogForFlashlight(Context context,String values) {
        super(context);
        setContentView(R.layout.layout_dialog_flashlight);
        editText=findViewById(R.id.dialog_action_flashlight_edit);
        ra_unselected=findViewById(R.id.dialog_action_flashlight_unselected);
        ra_custom=findViewById(R.id.dialog_action_flashlight_custom);
        edit_area=findViewById(R.id.dialog_action_flashlight_edit_area);
        preview=findViewById(R.id.dialog_action_flashlight_test);

        ra_unselected.setOnClickListener(this);
        ra_custom.setOnClickListener(this);
        preview.setOnClickListener(this);
        editText.setText(values.equals("-1")?String.valueOf(100):values);
        this.values=ValueUtils.string2longArray(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL,values);
        refreshViews();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            default:break;
            case R.id.dialog_action_flashlight_button_confirm:{
                if(callBack!=null) {
                    if(ra_custom.isChecked()){
                        String s=editText.getText().toString().trim();
                        long [] values;
                        try{
                            values=string2longArray(s);
                        }catch (Exception e){
                            Toast.makeText(getContext(),e.toString(),Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String result=ValueUtils.longArray2String(PublicConsts.SEPARATOR_SECOND_LEVEL,values);
                        callBack.onDialogConfirmed(result);
                    }else {
                        callBack.onDialogConfirmed(String.valueOf(-1));
                    }

                }
                cancel();
            }
            break;
            case R.id.dialog_action_flashlight_button_cancel:{
                cancel();
            }
            break;
            case R.id.dialog_action_flashlight_unselected: case R.id.dialog_action_flashlight_custom:{
                refreshViews();
            }
            break;
            case R.id.dialog_action_flashlight_test:{
                try{
                    long[] array=string2longArray(editText.getText().toString().trim());
                    EnvironmentUtils.setTorch(getContext(),array);
                }catch (Exception e){
                    Toast.makeText(getContext(),e.toString(),Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }

    private long[] string2longArray(String values){
        String [] values_array=values.split(",");
        long values_long_array[]=new long[values_array.length];
        for(int i=0;i<values_array.length;i++){
            values_long_array[i]=Long.parseLong(values_array[i]);
        }
        return values_long_array;
    }

    private void refreshViews(){
        ra_unselected.setChecked(values[0]<0);
        ra_custom.setChecked(values[0]>=0);
       // edit_area.setVisibility(ra_custom.isChecked()?View.VISIBLE:View.GONE);
    }
}
