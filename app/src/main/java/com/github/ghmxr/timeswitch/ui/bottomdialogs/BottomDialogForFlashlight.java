package com.github.ghmxr.timeswitch.ui.bottomdialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.Global;
import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.ActionConsts;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.utils.EnvironmentUtils;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

public class BottomDialogForFlashlight extends BottomDialog implements View.OnClickListener {

    private long[] values;
    private EditText editText;
    private RadioButton ra_unselected,ra_hold,ra_custom;
    private TextView unit;
    private Button preview;
    private ViewGroup edit_area;
    private int type=-1;

    private Thread testThread;

    public BottomDialogForFlashlight(Context context,String values) {
        super(context);
        setContentView(R.layout.layout_dialog_flashlight);

        editText=findViewById(R.id.dialog_action_flashlight_edit);
        unit=findViewById(R.id.dialog_action_flashlight_unit);
        ra_unselected=findViewById(R.id.dialog_action_flashlight_unselected);
        ra_hold=findViewById(R.id.dialog_action_flashlight_hold);
        ra_custom=findViewById(R.id.dialog_action_flashlight_custom);
        edit_area=findViewById(R.id.dialog_action_flashlight_edit_area);
        preview=findViewById(R.id.dialog_action_flashlight_test);

        ra_unselected.setOnClickListener(this);
        ra_hold.setOnClickListener(this);
        ra_custom.setOnClickListener(this);
        preview.setOnClickListener(this);
        findViewById(R.id.dialog_action_flashlight_button_confirm).setOnClickListener(this);
        findViewById(R.id.dialog_action_flashlight_button_cancel).setOnClickListener(this);

        long[] a=ValueUtils.string2longArray(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL,values);
        type=(int)a[0];

        this.values=new long[a.length-1];
        System.arraycopy(a,1,this.values,0,this.values.length);

        editText.setText(ValueUtils.longArray2String(",",this.values));
        refreshViews();
        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(testThread!=null)testThread.interrupt();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            default:break;
            case R.id.dialog_action_flashlight_button_confirm:{
                if(callBack!=null) {
                    if(ra_custom.isChecked()){
                        long [] values;
                        try{
                            values=ValueUtils.string2longArray(",",editText.getText().toString().trim());
                        }catch (Exception e){
                            Toast.makeText(getContext(),e.toString(),Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String result=ValueUtils.longArray2String(PublicConsts.SEPARATOR_SECOND_LEVEL,values);
                        int head=-1;
                        if(ra_hold.isChecked()) head= ActionConsts.ActionValueConsts.ACTION_FLASHLIGHT_TYPE_HOLD;
                        if(ra_custom.isChecked()) head= ActionConsts.ActionValueConsts.ACTION_FLASHLIGHT_TYPE_CUSTOM;
                        callBack.onDialogConfirmed(String.valueOf(head)+PublicConsts.SEPARATOR_SECOND_LEVEL+result);
                        cancel();
                    }else if(ra_hold.isChecked()){
                        try{
                            int hold=Integer.parseInt(editText.getText().toString().trim());
                            callBack.onDialogConfirmed(String.valueOf(ActionConsts.ActionValueConsts.ACTION_FLASHLIGHT_TYPE_HOLD)+PublicConsts.SEPARATOR_SECOND_LEVEL+String.valueOf(hold));
                            cancel();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(getContext(),e.toString(),Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        callBack.onDialogConfirmed(String.valueOf(-1));
                        cancel();
                    }

                }

            }
            break;
            case R.id.dialog_action_flashlight_button_cancel:{
                cancel();
            }
            break;
            case R.id.dialog_action_flashlight_unselected: {
                type=-1;
                refreshViews();
            }
            break;
            case R.id.dialog_action_flashlight_custom:{
                type= ActionConsts.ActionValueConsts.ACTION_FLASHLIGHT_TYPE_CUSTOM;
                refreshViews();
            }
            break;
            case R.id.dialog_action_flashlight_hold:{
                type= ActionConsts.ActionValueConsts.ACTION_FLASHLIGHT_TYPE_HOLD;
                refreshViews();
            }
            break;
            case R.id.dialog_action_flashlight_test:{
                preview.setEnabled(false);
                testThread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(type==ActionConsts.ActionValueConsts.ACTION_FLASHLIGHT_TYPE_HOLD){
                            EnvironmentUtils.setTorch(getContext(),Integer.parseInt(editText.getText().toString().trim())*1000);
                        }else if(type== ActionConsts.ActionValueConsts.ACTION_FLASHLIGHT_TYPE_CUSTOM){
                            long[] array=ValueUtils.string2longArray(",",editText.getText().toString().trim());
                            EnvironmentUtils.setTorch(getContext(),array);
                        }
                        Global.handler.post(new Runnable() {
                            @Override
                            public void run() {
                                preview.setEnabled(true);
                            }
                        });
                    }
                });
                testThread.start();
            }
            break;
        }
    }

    private void refreshViews(){
        ra_unselected.setChecked(type==-1);
        ra_hold.setChecked(type==ActionConsts.ActionValueConsts.ACTION_FLASHLIGHT_TYPE_HOLD);
        ra_custom.setChecked(type== ActionConsts.ActionValueConsts.ACTION_FLASHLIGHT_TYPE_CUSTOM);
        if(type==-1){
            edit_area.setVisibility(View.GONE);
        }else if(type== ActionConsts.ActionValueConsts.ACTION_FLASHLIGHT_TYPE_HOLD){
            edit_area.setVisibility(View.VISIBLE);
            unit.setText(getContext().getResources().getString(R.string.unit_second));
            editText.setHint(getContext().getResources().getString(R.string.action_flashlight_hint_hold));
        }else if(type== ActionConsts.ActionValueConsts.ACTION_FLASHLIGHT_TYPE_CUSTOM){
            edit_area.setVisibility(View.VISIBLE);
            unit.setText(getContext().getResources().getString(R.string.unit_millisecond));
            editText.setHint(getContext().getResources().getString(R.string.action_flashlight_hint_custom));
        }
    }
}
