package com.github.ghmxr.timeswitch.triggers.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.triggers.Trigger;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

public class LightSensor implements Trigger,SensorEventListener{

    private static SensorManager sensor_manager;
    private TaskItem item;
    private Context context;
    private boolean mLock=true;


    public LightSensor(Context context, TaskItem item){
        this.item=item;
        this.context=context;
        if(sensor_manager==null){
            synchronized (LightSensor.class){
                if(sensor_manager==null) sensor_manager=(SensorManager)context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
            }
        }
    }

    @Override
    public void activate() {
        try{
            sensor_manager.registerListener(this,sensor_manager.getDefaultSensor(Sensor.TYPE_LIGHT),SensorManager.SENSOR_DELAY_NORMAL);
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public void cancel() {
        try{
            sensor_manager.unregisterListener(this);
        }catch (Exception e){e.printStackTrace();}
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        int value=(int)event.values[0];  //光线亮度，单位勒克斯
        if(item.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_LIGHT_SENSOR_HIGHER_THAN){
            if(value>item.light_brightness) runProcessTaskItem();
            else mLock=false;
            return;
        }

        if(item.trigger_type==TriggerTypeConsts.TRIGGER_TYPE_LIGHT_SENSOR_LOWER_THAN){
            if(value<item.light_brightness) runProcessTaskItem();
            else mLock=false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void runProcessTaskItem(){
        if(!mLock){
            mLock=true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        new ProcessTaskItem(context,item).checkExceptionsAndRunActions();
                    }catch (Exception e){e.printStackTrace();}
                }
            }).start();
        }
    }

}
