package com.github.ghmxr.timeswitch.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.github.ghmxr.timeswitch.R;

public class DialogForColor extends AlertDialog implements DialogInterface.OnClickListener, View.OnClickListener{

    private String color;
    private OnDialogForColorConfirmedListener listener;
    Context context;

    public DialogForColor(Context context,String colorRgb){
        super(context);
        this.context=context;
        setTitle("Color");
        setView(LayoutInflater.from(context).inflate(R.layout.layout_dialog_color,null));
        setButton(AlertDialog.BUTTON_POSITIVE, context.getResources().getString(R.string.dialog_button_positive), new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        setButton(AlertDialog.BUTTON_NEGATIVE,context.getResources().getString(R.string.dialog_button_negative),this);
        this.color=colorRgb;
    }

    private void addAllPresetColors(){
        findViewById(R.id.dialog_color_e74c3c).setOnClickListener(this);
        findViewById(R.id.dialog_color_2c3e50).setOnClickListener(this);
        findViewById(R.id.dialog_color_8e44ad).setOnClickListener(this);
        findViewById(R.id.dialog_color_16a085).setOnClickListener(this);
        findViewById(R.id.dialog_color_95a5a6).setOnClickListener(this);
        findViewById(R.id.dialog_color_2980b9).setOnClickListener(this);

        findViewById(R.id.dialog_color_bdc3c7).setOnClickListener(this);
        findViewById(R.id.dialog_color_e67e22).setOnClickListener(this);
        findViewById(R.id.dialog_color_f1c40f).setOnClickListener(this);
        findViewById(R.id.dialog_color_3f51b5).setOnClickListener(this);
    }

    @Override
    public void show(){
        super.show();
        try{
            ((EditText)findViewById(R.id.dialog_color_edit)).setText(this.color);
            try{
                (findViewById(R.id.dialog_color_preview)).setBackgroundColor(Color.parseColor(this.color));
            }catch (Exception e){
                e.printStackTrace();
            }
            ((EditText)findViewById(R.id.dialog_color_edit)).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    try{
                        (findViewById(R.id.dialog_color_preview)).setBackgroundColor(Color.parseColor(s.toString()));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        color=((EditText)findViewById(R.id.dialog_color_edit)).getText().toString();
                        Color.parseColor(color);
                        if(listener!=null) listener.onConfirmed(color);
                        cancel();
                    }catch (Exception e){
                        e.printStackTrace();
                        Snackbar.make(findViewById(R.id.dialog_color_root),context.getResources().getString(R.string.snack_dialog_color_invalid),Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
            addAllPresetColors();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setOnDialogConfirmListener(OnDialogForColorConfirmedListener listener){
        this.listener=listener;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which){
            default:break;
            case AlertDialog.BUTTON_NEGATIVE:{
                cancel();
            }
            break;
        }
    }

    @Override
    public void onClick(View v){
        try{
            EditText editText=findViewById(R.id.dialog_color_edit);
            switch (v.getId()){
                default:break;
                case R.id.dialog_color_e74c3c:{
                    editText.setText("#e74c3c");
                }
                break;
                case R.id.dialog_color_2c3e50:{
                    editText.setText("#2c3e50");
                }
                break;
                case R.id.dialog_color_8e44ad:{
                    editText.setText("#8e44ad");
                }
                break;
                case R.id.dialog_color_16a085:{
                    editText.setText("#16a085");
                }
                break;
                case R.id.dialog_color_95a5a6:{
                    editText.setText("#95a5a6");
                }
                break;
                case R.id.dialog_color_2980b9:{
                    editText.setText("#2980b9");
                }
                break;
                case R.id.dialog_color_bdc3c7:{
                    editText.setText("#bdc3c7");
                }
                break;
                case R.id.dialog_color_e67e22:{
                    editText.setText("#e67e22");
                }
                break;
                case R.id.dialog_color_f1c40f:{
                    editText.setText("#f1c40f");
                }
                break;
                case R.id.dialog_color_3f51b5:{
                    editText.setText("#3f51b5");
                }
                break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public interface OnDialogForColorConfirmedListener{
        void onConfirmed(String color);
    }

}
