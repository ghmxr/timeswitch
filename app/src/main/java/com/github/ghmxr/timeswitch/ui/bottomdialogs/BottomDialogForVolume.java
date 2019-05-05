package com.github.ghmxr.timeswitch.ui.bottomdialogs;

import android.content.Context;
import android.media.AudioManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.ActionConsts;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;

public class BottomDialogForVolume extends BottomDialog implements View.OnClickListener ,CompoundButton.OnCheckedChangeListener{

    private final CheckBox cb_ring,cb_media,cb_notification,cb_alarmclock;
    private final SeekBar sb_ring,sb_media,sb_notification,sb_alarmclock;

    public BottomDialogForVolume(Context context,String volume_values) {
        super(context);
        setContentView(R.layout.layout_dialog_volume);
        cb_ring=findViewById(R.id.dialog_volume_ring_cb);
        cb_media=findViewById(R.id.dialog_volume_media_cb);
        cb_notification=findViewById(R.id.dialog_volume_notification_cb);
        cb_alarmclock=findViewById(R.id.dialog_volume_alarmclock_cb);
        sb_ring=findViewById(R.id.dialog_volume_ring_seekbar);
        sb_media= findViewById(R.id.dialog_volume_media_seekbar);
        sb_notification=findViewById(R.id.dialog_volume_notification_seekbar);
        sb_alarmclock=findViewById(R.id.dialog_volume_alarmclock_seekbar);
        cb_ring.setOnCheckedChangeListener(this);
        cb_media.setOnCheckedChangeListener(this);
        cb_notification.setOnCheckedChangeListener(this);
        cb_alarmclock.setOnCheckedChangeListener(this);
        findViewById(R.id.dialog_volume_button_confirm).setOnClickListener(this);
        findViewById(R.id.dialog_volume_button_cancel).setOnClickListener(this);
        try{
            AudioManager audioManager=(AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            String[] values=volume_values.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);

            sb_ring.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
            sb_media.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            sb_notification.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
            sb_alarmclock.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
            int volume_ring=Integer.parseInt(values[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_RING_LOCALE]);
            int volume_media=Integer.parseInt(values[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_MEDIA_LOCALE]);
            int volume_notification=Integer.parseInt(values[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_NOTIFICATION_LOCALE]);
            int volume_alarmclock=Integer.parseInt(values[ActionConsts.ActionSecondLevelLocaleConsts.VOLUME_ALARM_LOCALE]);
            cb_ring.setChecked(volume_ring>=0);
            cb_media.setChecked(volume_media>=0);
            cb_notification.setChecked(volume_notification>=0);
            cb_alarmclock.setChecked(volume_alarmclock>=0);
            sb_ring.setProgress(cb_ring.isChecked()?(volume_ring>=0?volume_ring:0):audioManager.getStreamVolume(AudioManager.STREAM_RING));
            sb_media.setProgress(cb_media.isChecked()?(volume_media>=0?volume_media:0):audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            sb_notification.setProgress(cb_notification.isChecked()?(volume_notification>=0?volume_notification:0):audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
            sb_alarmclock.setProgress(cb_alarmclock.isChecked()?(volume_alarmclock>=0?volume_alarmclock:0):audioManager.getStreamVolume(AudioManager.STREAM_ALARM));
            sb_ring.setEnabled(cb_ring.isChecked());
            sb_media.setEnabled(cb_media.isChecked());
            sb_notification.setEnabled(cb_notification.isChecked());
            sb_alarmclock.setEnabled(cb_alarmclock.isChecked());
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            default:break;
            case R.id.dialog_volume_button_confirm:{
                cancel();
                if(callBack!=null){
                    int volume_ring=-1;
                    int volume_media=-1;
                    int volume_notification=-1;
                    int volume_alarmclock=-1;
                    if(cb_ring.isChecked()) volume_ring=sb_ring.getProgress();
                    if(cb_media.isChecked()) volume_media=sb_media.getProgress();
                    if(cb_notification.isChecked()) volume_notification=sb_notification.getProgress();
                    if(cb_alarmclock.isChecked()) volume_alarmclock=sb_alarmclock.getProgress();
                    String volume_values=String.valueOf(volume_ring)+PublicConsts.SEPARATOR_SECOND_LEVEL
                            +String.valueOf(volume_media)+PublicConsts.SEPARATOR_SECOND_LEVEL
                            +String.valueOf(volume_notification)+PublicConsts.SEPARATOR_SECOND_LEVEL
                            +String.valueOf(volume_alarmclock);
                    callBack.onDialogConfirmed(volume_values);
                }
            }
            break;
            case R.id.dialog_volume_button_cancel:cancel();break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            default:break;
            case R.id.dialog_volume_ring_cb:{
                sb_ring.setEnabled(isChecked);
            }
            break;
            case R.id.dialog_volume_media_cb:{
                sb_media.setEnabled(isChecked);
            }
            break;
            case R.id.dialog_volume_notification_cb:{
                sb_notification.setEnabled(isChecked);
            }
            break;
            case R.id.dialog_volume_alarmclock_cb:{
                sb_alarmclock.setEnabled(isChecked);
            }
            break;
        }
    }
}
