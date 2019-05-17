package com.github.ghmxr.timeswitch.ui.bottomdialogs;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;

public class BottomDialogForTriggerBrightness extends BottomDialog implements View.OnClickListener,SensorEventListener{
    private Context context;
    private int selection=TriggerTypeConsts.TRIGGER_TYPE_LIGHT_SENSOR_HIGHER_THAN;
    //private int brightness_set;
    private SensorManager manager;

    public BottomDialogForTriggerBrightness(Context context,int selection,int value) {
        super(context);
        this.context=context;
        if(selection==TriggerTypeConsts.TRIGGER_TYPE_LIGHT_SENSOR_HIGHER_THAN||selection==TriggerTypeConsts.TRIGGER_TYPE_LIGHT_SENSOR_LOWER_THAN)this.selection=selection;
        setTitle(context.getResources().getString(R.string.activity_trigger_brightness));
        setContentView(R.layout.layout_trigger_brightness);
        findViewById(R.id.dialog_trigger_brightness_button_cancel).setOnClickListener(this);
        findViewById(R.id.dialog_trigger_brightness_button_confirm).setOnClickListener(this);
        findViewById(R.id.brightness_trigger_higher_than).setOnClickListener(this);
        findViewById(R.id.brightness_trigger_lower_than).setOnClickListener(this);
        ((EditText)findViewById(R.id.brightness_trigger_edit)).setText(String.valueOf(value));
        refreshView();

        try{
            manager=(SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
            manager.registerListener(this,manager.getDefaultSensor(Sensor.TYPE_LIGHT),SensorManager.SENSOR_DELAY_NORMAL);
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            default:break;
            case R.id.dialog_trigger_brightness_button_cancel:cancel();break;
            case R.id.dialog_trigger_brightness_button_confirm:{
                if(callBack!=null) callBack.onDialogConfirmed(String.valueOf(selection)+","+((EditText)findViewById(R.id.brightness_trigger_edit)).getText().toString());
                cancel();
            }
            break;
            case R.id.brightness_trigger_higher_than:{
                selection=TriggerTypeConsts.TRIGGER_TYPE_LIGHT_SENSOR_HIGHER_THAN;
                refreshView();
            }
            break;
            case R.id.brightness_trigger_lower_than:{
                selection=TriggerTypeConsts.TRIGGER_TYPE_LIGHT_SENSOR_LOWER_THAN;
                refreshView();
            }
            break;
        }
    }

    @Override
    public void cancel(){
        super.cancel();
        try{
            manager.unregisterListener(this);
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int value=(int)event.values[0];
        ((TextView)findViewById(R.id.brightness_trigger_current)).setText(context.getResources().getString(R.string.trigger_brightness_current)+
                value+context.getResources().getString(R.string.trigger_brightness_unit));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public int getSelection(){
        return selection;
    }

    public int getBrightness_set(){
        return Integer.parseInt(((EditText)findViewById(R.id.brightness_trigger_edit)).getText().toString());
    }

    private void refreshView(){
        ((RadioButton)findViewById(R.id.brightness_trigger_higher_than)).setChecked(selection== TriggerTypeConsts.TRIGGER_TYPE_LIGHT_SENSOR_HIGHER_THAN);
        ((RadioButton)findViewById(R.id.brightness_trigger_lower_than)).setChecked(selection==TriggerTypeConsts.TRIGGER_TYPE_LIGHT_SENSOR_LOWER_THAN);
    }
}
