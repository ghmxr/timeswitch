package com.github.ghmxr.timeswitch.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.TaskItem;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.ui.ActionDisplayValue;
import com.github.ghmxr.timeswitch.ui.BottomDialog;
import com.github.ghmxr.timeswitch.ui.BottomDialogForBrightness;
import com.github.ghmxr.timeswitch.ui.BottomDialogForVibrate;
import com.github.ghmxr.timeswitch.utils.LogUtil;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;

import java.util.Arrays;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */

public class Actions extends BaseActivity implements View.OnClickListener{
    public static final String EXTRA_TASK_ID="taskid";
    public static final String EXTRA_ACTIONS="actions";
    public static final String EXTRA_ACTION_URI_RING_NOTIFICATION="uri_ring_notification";
    public static final String EXTRA_ACTION_URI_RING_CALL="uri_ring_call";
    public static final String EXTRA_ACTION_URI_WALLPAPER_DESKTOP ="uri_wallpaper";
    public static final String EXTRA_ACTION_NOTIFICATION_TITLE="notification_title";
    public static final String EXTRA_ACTION_NOTIFICATION_MESSAGE="notification_message";
    public static final String EXTRA_ACTION_TOAST="toast";
    public static final String EXTRA_ACTION_SMS_ADDRESS="sms_address";
    public static final String EXTRA_ACTION_SMS_MESSAGE="sms_message";
    private static final int REQUEST_CODE_RING_CHANGED=1;
    private static final int REQUEST_CODE_WALLPAPER_CHANGED=2;
    private static final int REQUEST_CODE_SMS_SET=3;
    //boolean isItemClicked=false;
    String [] actions=new String[PublicConsts.ACTION_LENGTH];
    String uri_ring_notification="",uri_ring_call="",
            uri_wallpaper="",
            notification_title="",notification_message="",toast="",
            sms_address="",sms_message="";
    String checkString ="";
    private long first_clicked_back_time=0;
    private int taskid=-1;
    private static final int TASK_ENABLE=0;
    private static final int TASK_DISABLE=1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_actions);
        Toolbar toolbar=findViewById(R.id.actions_toolbar);
        setSupportActionBar(toolbar);
        try{getSupportActionBar().setDisplayHomeAsUpEnabled(true);}
        catch (Exception e){e.printStackTrace();}
        findViewById(R.id.actions_wifi).setOnClickListener(this);
        findViewById(R.id.actions_bluetooth).setOnClickListener(this);
        findViewById(R.id.actions_ring_mode).setOnClickListener(this);
        findViewById(R.id.actions_ring_volume).setOnClickListener(this);
        findViewById(R.id.actions_ring_selection).setOnClickListener(this);
        findViewById(R.id.actions_brightness).setOnClickListener(this);
        findViewById(R.id.actions_wallpaper).setOnClickListener(this);
        findViewById(R.id.actions_vibrate).setOnClickListener(this);
        findViewById(R.id.actions_sms).setOnClickListener(this);
        findViewById(R.id.actions_notification).setOnClickListener(this);
        findViewById(R.id.actions_toast).setOnClickListener(this);
        findViewById(R.id.actions_net).setOnClickListener(this);
        findViewById(R.id.actions_gps).setOnClickListener(this);
        findViewById(R.id.actions_airplane_mode).setOnClickListener(this);
        findViewById(R.id.actions_devicecontrol).setOnClickListener(this);
        findViewById(R.id.actions_enable).setOnClickListener(this);
        findViewById(R.id.actions_disable).setOnClickListener(this);
        try{
            Intent data=getIntent();
            actions=data.getStringArrayExtra(EXTRA_ACTIONS);
            taskid=data.getIntExtra(EXTRA_TASK_ID,-1);
            uri_ring_notification=data.getStringExtra(EXTRA_ACTION_URI_RING_NOTIFICATION);
            uri_ring_call=data.getStringExtra(EXTRA_ACTION_URI_RING_CALL);
            uri_wallpaper=data.getStringExtra(EXTRA_ACTION_URI_WALLPAPER_DESKTOP);
            notification_title=data.getStringExtra(EXTRA_ACTION_NOTIFICATION_TITLE);
            notification_message=data.getStringExtra(EXTRA_ACTION_NOTIFICATION_MESSAGE);
            toast=data.getStringExtra(EXTRA_ACTION_TOAST);
            sms_address=data.getStringExtra(EXTRA_ACTION_SMS_ADDRESS);
            sms_message=data.getStringExtra(EXTRA_ACTION_SMS_MESSAGE);
            checkString = toCheckString();
            refreshActionStatus();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void processMessage(Message msg) {}

    @Override
    public void onClick(View view) {
        //isItemClicked=true;
        switch (view.getId()){
           default:break;
            case R.id.actions_wifi: case R.id.actions_bluetooth: case R.id.actions_ring_mode: {
                showNormalBottomDialog(view.getId());
            }
            break;
            case R.id.actions_net: case R.id.actions_gps: case R.id.actions_airplane_mode:
            case R.id.actions_devicecontrol:{
                boolean isRoot=getSharedPreferences(PublicConsts.PREFERENCES_NAME, Activity.MODE_PRIVATE).getBoolean(PublicConsts.PREFERENCES_IS_SUPERUSER_MODE,PublicConsts.PREFERENCES_IS_SUPERUSER_MODE_DEFAULT);
                if(!isRoot){
                    showSnackBarOfSuperuserRequest();
                    return;
                }
                showNormalBottomDialog(view.getId());
            }
            break;
            case R.id.actions_brightness:{
                BottomDialogForBrightness dialog=new BottomDialogForBrightness(this);
                try{
                    dialog.setVariables(Integer.parseInt(actions[PublicConsts.ACTION_BRIGHTNESS_LOCALE]));
                }catch (NumberFormatException ne){
                    ne.printStackTrace();
                    LogUtil.putExceptionLog(this,ne);
                }
                dialog.show();
                dialog.setOnDialogConfirmedListener(new BottomDialogForBrightness.OnDialogConfirmedListener() {
                    @Override
                    public void onConfirmed(int value) {
                        actions[PublicConsts.ACTION_BRIGHTNESS_LOCALE]=String.valueOf(value);
                        refreshActionStatus();
                    }
                });
            }
            break;
            case R.id.actions_ring_volume:{
                AudioManager audioManager=(AudioManager) getSystemService(Context.AUDIO_SERVICE);
                String volumeValues;
                String[] volumeArray;
                int volume_ring=-1;
                int volume_media=-1;
                int volume_notification=-1;
                int volume_alarmclock=-1;

                View dialogview= LayoutInflater.from(this).inflate(R.layout.layout_dialog_volume,null);
                final BottomDialog dialog=new BottomDialog(this);
                dialog.setContentView(dialogview);
                final CheckBox cb_ring=dialogview.findViewById(R.id.dialog_volume_ring_cb);
                final CheckBox cb_media=dialogview.findViewById(R.id.dialog_volume_media_cb);
                final CheckBox cb_notification=dialogview.findViewById(R.id.dialog_volume_notification_cb);
                final CheckBox cb_alarmclock=dialogview.findViewById(R.id.dialog_volume_alarmclock_cb);
                final SeekBar sb_ring=dialogview.findViewById(R.id.dialog_volume_ring_seekbar);
                final SeekBar sb_media= dialogview.findViewById(R.id.dialog_volume_media_seekbar);
                final SeekBar sb_notification=dialogview.findViewById(R.id.dialog_volume_notification_seekbar);
                final SeekBar sb_alarmclock=dialogview.findViewById(R.id.dialog_volume_alarmclock_seekbar);

                try{
                    volumeValues=actions[PublicConsts.ACTION_RING_VOLUME_LOCALE];
                    volumeArray=volumeValues.split(PublicConsts.SPLIT_SEPERATOR_SECOND_LEVEL);
                    sb_ring.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
                    sb_media.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                    sb_notification.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
                    sb_alarmclock.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
                    volume_ring=Integer.parseInt(volumeArray[PublicConsts.VOLUME_RING_LOCALE]);
                    volume_media=Integer.parseInt(volumeArray[PublicConsts.VOLUME_MEDIA_LOCALE]);
                    volume_notification=Integer.parseInt(volumeArray[PublicConsts.VOLUME_NOTIFICATION_LOCALE]);
                    volume_alarmclock=Integer.parseInt(volumeArray[PublicConsts.VOLUME_ALARM_LOCALE]);
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
                    LogUtil.putExceptionLog(this,e);
                }

                cb_ring.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        sb_ring.setEnabled(b);
                    }
                });

                cb_media.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        sb_media.setEnabled(b);
                    }
                });

                cb_notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        sb_notification.setEnabled(isChecked);
                    }
                });

                cb_alarmclock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        sb_alarmclock.setEnabled(b);
                    }
                });

                dialog.show();

                dialogview.findViewById(R.id.dialog_volume_button_confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int volume_ring=-1;
                        int volume_media=-1;
                        int volume_notification=-1;
                        int volume_alarmclock=-1;
                        if(cb_ring.isChecked()) volume_ring=sb_ring.getProgress();
                        if(cb_media.isChecked()) volume_media=sb_media.getProgress();
                        if(cb_notification.isChecked()) volume_notification=sb_notification.getProgress();
                        if(cb_alarmclock.isChecked()) volume_alarmclock=sb_alarmclock.getProgress();
                        String volume_values=String.valueOf(volume_ring)+PublicConsts.SEPERATOR_SECOND_LEVEL
                                +String.valueOf(volume_media)+PublicConsts.SEPERATOR_SECOND_LEVEL
                                +String.valueOf(volume_notification)+PublicConsts.SEPERATOR_SECOND_LEVEL
                                +String.valueOf(volume_alarmclock);
                        actions[PublicConsts.ACTION_RING_VOLUME_LOCALE]=volume_values;
                        dialog.cancel();
                        refreshActionStatus();
                    }
                });

                dialogview.findViewById(R.id.dialog_volume_button_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });
            }
            break;
            case R.id.actions_ring_selection:{
                Intent intent = new Intent();
                intent.setClass(this,ActionOfChangingRingtones.class);
                intent.putExtra(ActionOfChangingRingtones.EXTRA_RING_VALUES,actions[PublicConsts.ACTION_RING_SELECTION_LOCALE]);
                intent.putExtra(ActionOfChangingRingtones.EXTRA_RING_URI_NOTIFICATION,uri_ring_notification);
                intent.putExtra(ActionOfChangingRingtones.EXTRA_RING_URI_CALL,uri_ring_call);
                startActivityForResult(intent,REQUEST_CODE_RING_CHANGED);
            }
            break;

            case R.id.actions_wallpaper:{
                final BottomDialog dialog=new BottomDialog(this);
                dialog.setContentView(R.layout.layout_dialog_actions_selection_wallpaper);
                try{
                    //String[] wallpaper_values=taskitem.actions[PublicConsts.ACTION_SET_WALL_PAPER_LOCALE].split(PublicConsts.SPLIT_SEPERATOR_SECOND_LEVEL);
                    ((RadioButton)dialog.findViewById(R.id.dialog_wallpaper_unselected_rb)).setChecked(Integer.parseInt(actions[PublicConsts.ACTION_SET_WALL_PAPER_LOCALE])==PublicConsts.ACTION_UNSELECTED);
                    ((RadioButton)dialog.findViewById(R.id.dialog_wallpaper_select_rb)).setChecked(Integer.parseInt(actions[PublicConsts.ACTION_SET_WALL_PAPER_LOCALE])>=0);
                }catch (Exception e){
                    e.printStackTrace();
                    LogUtil.putExceptionLog(this,e);
                }
                dialog.show();
                dialog.findViewById(R.id.dialog_wallpaper_unselected).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                        actions[PublicConsts.ACTION_SET_WALL_PAPER_LOCALE]=String.valueOf(-1);//+PublicConsts.SEPERATOR_SECOND_LEVEL+String.valueOf(" ");
                        refreshActionStatus();
                    }
                });
                dialog.findViewById(R.id.dialog_wallpaper_select).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                        startActivityForResult(new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI),REQUEST_CODE_WALLPAPER_CHANGED);
                        Toast.makeText(Actions.this,getResources().getString(R.string.dialog_actions_wallpaper_att),Toast.LENGTH_SHORT).show();
                    }
                });
            }
            break;
            case R.id.actions_vibrate:{
                try{
                    final BottomDialogForVibrate dialog=new BottomDialogForVibrate(this);
                    String[] vibrate_values=actions[PublicConsts.ACTION_VIBRATE_LOCALE].split(PublicConsts.SPLIT_SEPERATOR_SECOND_LEVEL);
                    dialog.setVariables(Integer.parseInt(vibrate_values[PublicConsts.VIBRATE_FREQUENCY_LOCALE])>0,
                            Integer.parseInt(vibrate_values[PublicConsts.VIBRATE_FREQUENCY_LOCALE]),
                            Integer.parseInt(vibrate_values[PublicConsts.VIBRATE_DURATION_LOCALE]),
                            Integer.parseInt(vibrate_values[PublicConsts.VIBRATE_INTERVAL_LOCALE]));
                    dialog.show();
                    dialog.setOnBottomDialogForVibrateConfirmedListener(new BottomDialogForVibrate.BottomDialogForVibrateConfirmedListener() {
                        @Override
                        public void onBottomDialogForVibrateConfirmed(boolean isEnabled, int frequency, int duration, int interval) {
                            if(isEnabled){
                                actions[PublicConsts.ACTION_VIBRATE_LOCALE]=String.valueOf(frequency)+PublicConsts.SEPERATOR_SECOND_LEVEL
                                        +String.valueOf(duration)+PublicConsts.SEPERATOR_SECOND_LEVEL
                                        +String.valueOf(interval);
                            }
                            else actions[PublicConsts.ACTION_VIBRATE_LOCALE]=String.valueOf(-1)+PublicConsts.SEPERATOR_SECOND_LEVEL
                                    +String.valueOf(-1)+PublicConsts.SEPERATOR_SECOND_LEVEL
                                    +String.valueOf(-1);
                            refreshActionStatus();
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                    LogUtil.putExceptionLog(this,e);
                }
            }
            break;
            case R.id.actions_notification:{
                final BottomDialog dialog=new BottomDialog(this);
                dialog.setContentView(R.layout.layout_dialog_notification);
                dialog.show();
                try{
                    String[] notification_values=actions[PublicConsts.ACTION_NOTIFICATION_LOCALE].split(PublicConsts.SPLIT_SEPERATOR_SECOND_LEVEL);
                    int type=Integer.parseInt(notification_values[PublicConsts.NOTIFICATION_TYPE_LOCALE]);
                    int type_custom=Integer.parseInt(notification_values[PublicConsts.NOTIFICATION_TYPE_IF_CUSTOM_LOCALE]);
                    final String title=notification_title;//notification_values[PublicConsts.NOTIFICATION_TITLE_LOCALE];
                    final String message=notification_message;//notification_values[PublicConsts.NOTIFICATION_MESSAGE_LOCALE];
                    final RadioButton ra_unselected=dialog.findViewById(R.id.dialog_notification_selection_unselected_ra);
                    final RadioButton ra_with_vibrate=dialog.findViewById(R.id.dialog_notification_selection_with_vibrate_ra);
                    final RadioButton ra_without_vibrate=dialog.findViewById(R.id.dialog_notification_selection_without_vibrate_ra);
                    final RadioButton ra_default=dialog.findViewById(R.id.dialog_notification_operation_default_ra);
                    final RadioButton ra_custom=dialog.findViewById(R.id.dialog_notification_operation_custom_ra);
                    final RelativeLayout operation_area=dialog.findViewById(R.id.dialog_notification_operation_area);
                    final EditText edit_title=dialog.findViewById(R.id.dialog_notification_operation_custom_edit_title);
                    final EditText edit_message=dialog.findViewById(R.id.dialog_notification_operation_custom_edit_message);
                    edit_title.setText(title.trim());
                    edit_message.setText(message.trim());
                    (ra_unselected).setChecked(type==PublicConsts.NOTIFICATION_TYPE_UNSELECTED);
                    (ra_with_vibrate).setChecked(type==PublicConsts.NOTIFICATION_TYPE_VIBRATE);
                    (ra_without_vibrate).setChecked(type==PublicConsts.NOTIFICATION_TYPE_NO_VIBRATE);
                    (operation_area).setVisibility(type==PublicConsts.NOTIFICATION_TYPE_UNSELECTED?View.GONE:View.VISIBLE);
                    (dialog.findViewById(R.id.dialog_notification_selection_unselected)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            (ra_unselected).setChecked(true);
                            ra_with_vibrate.setChecked(false);
                            ra_without_vibrate.setChecked(false);
                            (operation_area).setVisibility(View.GONE);
                        }
                    });

                    (dialog.findViewById(R.id.dialog_notification_selection_without_vibrate)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            (ra_unselected).setChecked(false);
                            ra_with_vibrate.setChecked(false);
                            ra_without_vibrate.setChecked(true);
                            (operation_area).setVisibility(View.VISIBLE);

                        }
                    });

                    ra_default.setChecked(type_custom==PublicConsts.NOTIFICATION_TYPE_DEFAULT);
                    ra_custom.setChecked(type_custom==PublicConsts.NOTIFICATION_TYPE_CUSTOM);
                    edit_title.setEnabled(ra_custom.isChecked());
                    edit_message.setEnabled(ra_custom.isChecked());
                    ra_custom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            edit_title.setEnabled(b);
                            edit_message.setEnabled(b);
                        }
                    });
                    (dialog.findViewById(R.id.dialog_notification_button_confirm)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                            int type=ra_unselected.isChecked()?PublicConsts.NOTIFICATION_TYPE_UNSELECTED
                                    :(ra_with_vibrate.isChecked()?PublicConsts.NOTIFICATION_TYPE_VIBRATE:
                                    (ra_without_vibrate.isChecked()?PublicConsts.NOTIFICATION_TYPE_NO_VIBRATE:PublicConsts.NOTIFICATION_TYPE_UNSELECTED)
                            );
                            int type_if_custom=ra_custom.isChecked()?PublicConsts.NOTIFICATION_TYPE_CUSTOM:PublicConsts.NOTIFICATION_TYPE_DEFAULT;
                            String custom_title=edit_title.getText().toString();
                            String custom_message=edit_message.getText().toString();
                            actions[PublicConsts.ACTION_NOTIFICATION_LOCALE]=String.valueOf(type)+PublicConsts.SEPERATOR_SECOND_LEVEL
                                    +String.valueOf(type_if_custom);
                            notification_title=custom_title;
                            notification_message=custom_message;
                            refreshActionStatus();
                        }
                    });
                    (dialog.findViewById(R.id.dialog_notification_button_cancel)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                    LogUtil.putExceptionLog(this,e);
                }
            }
            break;
            case R.id.actions_toast:{
                /*final ImageView preview=new ImageView(this);
                final ViewGroup.LayoutParams layoutParams=new ViewGroup.LayoutParams(DisplayDensity.dip2px(this,25),DisplayDensity.dip2px(this,25));
                preview.setLayoutParams(layoutParams);
                preview.setVisibility(View.GONE);
                preview.setBackgroundColor(getResources().getColor(R.color.colorAccent));  */

                final BottomDialog dialog=new BottomDialog(this);
                dialog.setContentView(R.layout.layout_dialog_toast);
                dialog.show();
                final CheckBox cb_enabled=dialog.findViewById(R.id.dialog_toast_cb);
                final RadioGroup rg=dialog.findViewById(R.id.dialog_toast_rg);
                final RadioButton ra_location_default=dialog.findViewById(R.id.dialog_toast_default_ra);
                final RadioButton ra_location_custom=dialog.findViewById(R.id.dialog_toast_custom_ra);
                final SeekBar sb_x=dialog.findViewById(R.id.dialog_toast_x_sb);
                final SeekBar sb_y=dialog.findViewById(R.id.dialog_toast_y_sb);
                TextView tv_x_center=dialog.findViewById(R.id.dialog_toast_x_center);
                TextView tv_y_center=dialog.findViewById(R.id.dialog_toast_y_center);
                final EditText editText=dialog.findViewById(R.id.dialog_toast_edittext);
                final LinearLayout location_selection_area=dialog.findViewById(R.id.dialog_toast_location);
                final Button button=dialog.findViewById(R.id.dialog_toast_preview);
                try{
                    String toast_values[]=actions[PublicConsts.ACTION_TOAST_LOCALE].split(PublicConsts.SPLIT_SEPERATOR_SECOND_LEVEL);
                    boolean isenabled=Integer.parseInt(toast_values[PublicConsts.TOAST_TYPE_LOCALE])>=0;
                    int type=Integer.parseInt(toast_values[PublicConsts.TOAST_TYPE_LOCALE]);
                    cb_enabled.setChecked(isenabled);
                    rg.setVisibility(isenabled?View.VISIBLE:View.GONE);
                    editText.setText(toast);
                    editText.setEnabled(isenabled);
                    button.setEnabled(isenabled);
                    ra_location_custom.setChecked(type==PublicConsts.TOAST_TYPE_CUSTOM);
                    location_selection_area.setVisibility(isenabled?(type==PublicConsts.TOAST_TYPE_CUSTOM?View.VISIBLE:View.GONE):View.GONE);
                    cb_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            rg.setVisibility(b?View.VISIBLE:View.GONE);
                            editText.setEnabled(b);
                            button.setEnabled(b);
                            location_selection_area.setVisibility(b?(ra_location_custom.isChecked()?View.VISIBLE:View.GONE):View.GONE);
                        }
                    });

                    ra_location_custom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            ra_location_default.setChecked(!b);
                            location_selection_area.setVisibility(b?View.VISIBLE:View.GONE);
                            sb_x.setEnabled(b);
                            sb_y.setEnabled(b);
                        }
                    });

                    final int x_max=getWindowManager().getDefaultDisplay().getWidth();
                    final int y_max=getWindowManager().getDefaultDisplay().getHeight();
                    sb_x.setMax(x_max);
                    sb_y.setMax(y_max);
                    int progress_x=Integer.parseInt(toast_values[PublicConsts.TOAST_LOCATION_X_OFFSET_LOCALE]);
                    if(progress_x>=0&&progress_x<=x_max) sb_x.setProgress(progress_x);
                    int progress_y=Integer.parseInt(toast_values[PublicConsts.TOAST_LOCATION_Y_OFFSET_LOCALE]);
                    if(progress_y>=0&&progress_y<=y_max) sb_y.setProgress(progress_y);
                    /*sb_x.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            preview.setX(i);
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                            dialog.setBackgroundColor("#00ffffff");
                            ((android.support.constraint.ConstraintLayout)findViewById(R.id.layout_actions_root)).addView(preview);
                            preview.setVisibility(View.VISIBLE);
                            preview.setX(seekBar.getProgress());
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            dialog.setBackgroundColor(BottomDialog.BOTTOM_DIALOG_COLOR_WHITE);
                            preview.setVisibility(View.GONE);
                            ((android.support.constraint.ConstraintLayout)findViewById(R.id.layout_actions_root)).removeView(preview);
                        }
                    });  */

                    /*sb_y.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            preview.setY(i);
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                            dialog.setBackgroundColor("#00ffffff");
                            ((android.support.constraint.ConstraintLayout)findViewById(R.id.layout_actions_root)).addView(preview);
                            preview.setVisibility(View.VISIBLE);
                            preview.setY(seekBar.getProgress());
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            dialog.setBackgroundColor(BottomDialog.BOTTOM_DIALOG_COLOR_WHITE);
                            preview.setVisibility(View.GONE);
                            ((android.support.constraint.ConstraintLayout)findViewById(R.id.layout_actions_root)).removeView(preview);
                        }
                    });  */
                    tv_x_center.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sb_x.setProgress(x_max/2);
                        }
                    });
                    tv_y_center.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sb_y.setProgress(y_max/2);
                        }
                    });
                    editText.setText(toast);
                    dialog.findViewById(R.id.dialog_toast_preview).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast toast=Toast.makeText(Actions.this,editText.getText().toString(),Toast.LENGTH_SHORT);
                            if(ra_location_custom.isChecked()) toast.setGravity(Gravity.TOP|Gravity.START, sb_x.getProgress(),sb_y.getProgress());
                            toast.show();
                        }
                    });
                    dialog.findViewById(R.id.dialog_toast_confirm).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                            actions[PublicConsts.ACTION_TOAST_LOCALE]=String.valueOf(!cb_enabled.isChecked()?-1:
                                    (ra_location_custom.isChecked()?1:0))+PublicConsts.SEPERATOR_SECOND_LEVEL
                                    +String.valueOf(sb_x.getProgress())+PublicConsts.SEPERATOR_SECOND_LEVEL
                                    +String.valueOf(sb_y.getProgress());
                            toast=editText.getText().toString();
                            refreshActionStatus();
                        }
                    });
                    dialog.findViewById(R.id.dialog_toast_cancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                    LogUtil.putExceptionLog(this,e);
                }
            }
            break;
            case R.id.actions_sms:{
                Intent i=new Intent(this,SmsActivity.class);
                i.putExtra(SmsActivity.EXTRA_SMS_VALUES,actions[PublicConsts.ACTION_SMS_LOCALE]);
                i.putExtra(SmsActivity.EXTRA_SMS_ADDRESS,sms_address);
                i.putExtra(SmsActivity.EXTRA_SMS_MESSAGE,sms_message);
                startActivityForResult(i,REQUEST_CODE_SMS_SET);
            }
            break;
            case R.id.actions_enable:{
                if(TimeSwitchService.list==null||TimeSwitchService.list.size()<=0){
                    Snackbar.make(findViewById(R.id.layout_actions_root),getResources().getString(R.string.activity_action_switch_task_null),Snackbar.LENGTH_SHORT).show();
                    return;
                }
               showTaskSelectionDialog(TASK_ENABLE);
            }
            break;
            case R.id.actions_disable:{
                if(TimeSwitchService.list==null||TimeSwitchService.list.size()<=0){
                    Snackbar.make(findViewById(R.id.layout_actions_root),getResources().getString(R.string.activity_action_switch_task_null),Snackbar.LENGTH_SHORT).show();
                    return;
                }
                showTaskSelectionDialog(TASK_DISABLE);
            }
            break;
        }
    }

    private void showTaskSelectionDialog(final int enableOrDisable){
        View dialogView=LayoutInflater.from(this).inflate(R.layout.layout_dialog_with_listview,null);
        final AlertDialog dialog=new AlertDialog.Builder(this)
                .setTitle(enableOrDisable==TASK_ENABLE?getResources().getString(R.string.activity_taskgui_actions_enable):getResources().getString(R.string.activity_taskgui_actions_disable))
                .setView(dialogView)
                .setPositiveButton(getResources().getString(R.string.dialog_button_positive),null)
                .setNegativeButton(getResources().getString(R.string.dialog_button_negative), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
        final TaskAdapter adapter=new TaskAdapter();
        try{
            String [] values=actions[enableOrDisable==TASK_ENABLE?PublicConsts.ACTION_ENABLE_TASKS_LOCALE:PublicConsts.ACTION_DISABLE_TASKS_LOCALE].split(PublicConsts.SEPERATOR_SECOND_LEVEL);
            if(Integer.parseInt(values[0])>=0) adapter.setSelectedItems(values);
        }catch (Exception e){
            e.printStackTrace();
        }
        ListView listView=dialogView.findViewById(R.id.layout_dialog_listview);
        (listView).setAdapter(adapter);
        (listView).setDivider(null);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(TimeSwitchService.list==null||TimeSwitchService.list.size()<=i) return;
                if(TimeSwitchService.list.get(i).id==taskid) {
                    Snackbar.make(view,getResources().getString(R.string.activity_actions_switch_can_not_operate_self),Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TimeSwitchService.list.get(i).trigger_type==PublicConsts.TRIGGER_TYPE_SINGLE&&TimeSwitchService.list.get(i).time<=System.currentTimeMillis()){
                    Snackbar.make(view,getResources().getString(R.string.activity_actions_switch_outofdate),Snackbar.LENGTH_SHORT).show();
                    return;
                }
                adapter.onItemClicked(i);
            }
        });
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder actionValue=new StringBuilder("");
                boolean isSelected[]=adapter.getIsSelected();
                boolean allUnchecked=true;
                int selectedCount=0;
                for(int j=0;j<isSelected.length;j++){
                    if(isSelected[j]&&TimeSwitchService.list!=null){
                        allUnchecked=false;
                        actionValue.append(TimeSwitchService.list.get(j).id);
                        selectedCount++;
                        if(selectedCount<adapter.getSelectedCount()&&adapter.getSelectedCount()>1)
                            actionValue.append(PublicConsts.SEPERATOR_SECOND_LEVEL);
                    }
                }
                if(allUnchecked) actionValue=new StringBuilder(String.valueOf(-1));
                if(enableOrDisable==TASK_ENABLE)actions[PublicConsts.ACTION_ENABLE_TASKS_LOCALE]=actionValue.toString();
                if(enableOrDisable==TASK_DISABLE)actions[PublicConsts.ACTION_DISABLE_TASKS_LOCALE]=actionValue.toString();
                refreshActionStatus();
                dialog.cancel();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            checkAndFinish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void checkAndFinish(){
        if(checkString.equals(toCheckString())){
            setResult(RESULT_CANCELED);
            finish();
        }else {
            long clickedTime=System.currentTimeMillis();
            if(clickedTime-first_clicked_back_time>1000){
                first_clicked_back_time=clickedTime;
                Snackbar.make(findViewById(R.id.layout_actions_root),getResources().getString(R.string.snackbar_changes_not_saved_back),Toast.LENGTH_SHORT).show();
                return;
            }
          /*  new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.dialog_edit_changed_not_saved_title))
                    .setMessage(getResources().getString(R.string.dialog_edit_changed_not_saved_message))
                    .setPositiveButton(getResources().getString(R.string.dialog_button_positive), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.dialog_button_negative), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .show();  */
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private String toCheckString(){
        return Arrays.toString(actions)+uri_ring_notification+uri_ring_call+uri_wallpaper
                +notification_title+notification_message+toast+sms_address+sms_message;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            default:break;
            case android.R.id.home:{
                checkAndFinish();
            }
            break;
            case R.id.action_actions_confirm:{
                Intent i=new Intent();
                i.putExtra(EXTRA_ACTIONS,actions);
                i.putExtra(EXTRA_ACTION_URI_RING_NOTIFICATION,uri_ring_notification);
                i.putExtra(EXTRA_ACTION_URI_RING_CALL,uri_ring_call);
                i.putExtra(EXTRA_ACTION_URI_WALLPAPER_DESKTOP,uri_wallpaper);
                i.putExtra(EXTRA_ACTION_SMS_ADDRESS,sms_address);
                i.putExtra(EXTRA_ACTION_SMS_MESSAGE,sms_message);
                i.putExtra(EXTRA_ACTION_NOTIFICATION_TITLE,notification_title);
                i.putExtra(EXTRA_ACTION_NOTIFICATION_MESSAGE,notification_message);
                i.putExtra(EXTRA_ACTION_TOAST,toast);
                setResult(RESULT_OK,i);
                finish();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            default:break;
            case REQUEST_CODE_RING_CHANGED:{
                if(resultCode==RESULT_OK){
                    if(data==null) return;
                    String ring_selection_values=data.getStringExtra(ActionOfChangingRingtones.EXTRA_RING_VALUES);
                    if(ring_selection_values==null) return;
                    actions[PublicConsts.ACTION_RING_SELECTION_LOCALE]=ring_selection_values;
                    uri_ring_notification=data.getStringExtra(ActionOfChangingRingtones.EXTRA_RING_URI_NOTIFICATION);
                    uri_ring_call=data.getStringExtra(ActionOfChangingRingtones.EXTRA_RING_URI_CALL);
                    //Log.i("TaskGui",ring_selection_values);
                    refreshActionStatus();
                }
            }
            break;
            case REQUEST_CODE_WALLPAPER_CHANGED:{
                if(resultCode==RESULT_OK){
                    if(data==null) return;
                    Uri uri=data.getData();
                    if(uri==null) return;
                    actions[PublicConsts.ACTION_SET_WALL_PAPER_LOCALE]=String.valueOf(0);
                    uri_wallpaper=uri.toString();
                    refreshActionStatus();
                }
            }
            break;
            case REQUEST_CODE_SMS_SET:{
                if(resultCode==RESULT_OK){
                    if(data==null) return;
                    String values=data.getStringExtra(SmsActivity.EXTRA_SMS_VALUES);
                    if(values==null) return;
                    actions[PublicConsts.ACTION_SMS_LOCALE]=values;
                    sms_address=data.getStringExtra(SmsActivity.EXTRA_SMS_ADDRESS);
                    sms_message=data.getStringExtra(SmsActivity.EXTRA_SMS_MESSAGE);
                    refreshActionStatus();
                }
            }
            break;
        }
    }

    private void refreshActionStatus(){
        ((TextView)findViewById(R.id.actions_wifi_status)).setText(ActionDisplayValue.getGeneralDisplayValue(this,actions[PublicConsts.ACTION_WIFI_LOCALE]));
        ((TextView)findViewById(R.id.actions_bluetooth_status)).setText(ActionDisplayValue.getGeneralDisplayValue(this,actions[PublicConsts.ACTION_BLUETOOTH_LOCALE]));
        ((TextView)findViewById(R.id.actions_ring_mode_status)).setText(ActionDisplayValue.getRingModeDisplayValue(this,actions[PublicConsts.ACTION_RING_MODE_LOCALE]));
        ((TextView)findViewById(R.id.actions_ring_volume_status)).setText(ActionDisplayValue.getRingVolumeDisplayValue(this,actions[PublicConsts.ACTION_RING_VOLUME_LOCALE]));
        ((TextView)findViewById(R.id.actions_ring_selection_status)).setText(ActionDisplayValue.getRingSelectionDisplayValue(this,actions[PublicConsts.ACTION_RING_SELECTION_LOCALE]));
        ((TextView)findViewById(R.id.actions_brightness_status)).setText(ActionDisplayValue.getBrightnessDisplayValue(this,actions[PublicConsts.ACTION_BRIGHTNESS_LOCALE]));
        ((TextView)findViewById(R.id.actions_vibrate_status)).setText(ActionDisplayValue.getVibrateDisplayValue(this,actions[PublicConsts.ACTION_VIBRATE_LOCALE]));
        ((TextView)findViewById(R.id.actions_wallpaper_status)).setText(ActionDisplayValue.getWallpaperDisplayValue(this,actions[PublicConsts.ACTION_SET_WALL_PAPER_LOCALE],uri_wallpaper));
        ((TextView)findViewById(R.id.actions_sms_status)).setText(ActionDisplayValue.getSMSDisplayValue(this,actions[PublicConsts.ACTION_SMS_LOCALE]));
        ((TextView)findViewById(R.id.actions_notification_status)).setText(ActionDisplayValue.getNotificationDisplayValue(this,actions[PublicConsts.ACTION_NOTIFICATION_LOCALE]));
        ((TextView)findViewById(R.id.actions_net_status)).setText(ActionDisplayValue.getGeneralDisplayValue(this,actions[PublicConsts.ACTION_NET_LOCALE]));
        ((TextView)findViewById(R.id.actions_gps_status)).setText(ActionDisplayValue.getGeneralDisplayValue(this,actions[PublicConsts.ACTION_GPS_LOCALE]));
        ((TextView)findViewById(R.id.actions_airplane_mode_status)).setText(ActionDisplayValue.getGeneralDisplayValue(this,actions[PublicConsts.ACTION_AIRPLANE_MODE_LOCALE]));
        ((TextView)findViewById(R.id.actions_devicecontrol_status)).setText(ActionDisplayValue.getDeviceControlDisplayValue(this,actions[PublicConsts.ACTION_DEVICECONTROL_LOCALE]));
        ((TextView)findViewById(R.id.actions_toast_status)).setText(ActionDisplayValue.getToastDisplayValue(actions[PublicConsts.ACTION_TOAST_LOCALE],toast));
        ((TextView)findViewById(R.id.actions_enable_status)).setText(ActionDisplayValue.getTaskSwitchDisplayValue(actions[PublicConsts.ACTION_ENABLE_TASKS_LOCALE]));
        ((TextView)findViewById(R.id.actions_disable_status)).setText(ActionDisplayValue.getTaskSwitchDisplayValue(actions[PublicConsts.ACTION_DISABLE_TASKS_LOCALE]));
    }

    public void showNormalBottomDialog(int id) {
        final BottomDialog bdialog=new BottomDialog(this);
        bdialog.setContentView(R.layout.layout_dialog_actions_selection);
        if(id==R.id.actions_ring_mode){
            bdialog.setContentView(R.layout.layout_dialog_actions_selection_ring_mode);
        }
        if(id==R.id.actions_devicecontrol){
            bdialog.setContentView(R.layout.layout_dialog_actions_selection_devicecontrol);
        }

        ImageView icon_on=bdialog.findViewById(R.id.selection_area_open_icon);
        ImageView icon_off=bdialog.findViewById(R.id.selection_area_close_icon);
        RelativeLayout rl_open=bdialog.findViewById(R.id.selection_area_open);
        RelativeLayout rl_close=bdialog.findViewById(R.id.selection_area_close);
        RelativeLayout rl_unselected=bdialog.findViewById(R.id.selection_area_unselected);
        RadioButton rb_open=bdialog.findViewById(R.id.selection_area_open_rb);
        RadioButton rb_close=bdialog.findViewById(R.id.selection_area_close_rb);
        RadioButton rb_unselected=bdialog.findViewById(R.id.selection_area_unselected_rb);


        RelativeLayout rl_ring_vibrate=bdialog.findViewById(R.id.selection_ring_area_vibrate);
        RelativeLayout rl_ring_off=bdialog.findViewById(R.id.selection_ring_area_off);
        RelativeLayout rl_ring_normal=bdialog.findViewById(R.id.selection_ring_area_normal);
        RelativeLayout rl_ring_unselected=bdialog.findViewById(R.id.selection_ring_area_unselected);
        RadioButton rb_ring_normal=bdialog.findViewById(R.id.selection_ring_area_normal_rb);
        RadioButton rb_ring_off=bdialog.findViewById(R.id.selection_ring_area_off_rb);
        RadioButton rb_ring_vibrate=bdialog.findViewById(R.id.selection_ring_area_vibrate_rb);
        RadioButton rb_ring_unselected=bdialog.findViewById(R.id.selection_ring_area_unselected_rb);

        RelativeLayout rl_reboot=bdialog.findViewById(R.id.selection_area_reboot);
        RelativeLayout rl_shutdown=bdialog.findViewById(R.id.selection_area_shutdown);
        RelativeLayout rl_unselected_devicecontrol=bdialog.findViewById(R.id.selection_area_unselected_devicecontrol);

        RadioButton rb_devicecontrol_reboot=bdialog.findViewById(R.id.selection_area_reboot_rb);
        RadioButton rb_devicecontrol_shutdown=bdialog.findViewById(R.id.selection_area_shutdown_rb);
        RadioButton rb_devicecontrol_unselected=bdialog.findViewById(R.id.selection_area_unselected_devicecontrol_rb);

        int locale=-1;
        switch(id){
            default:break;
            case R.id.actions_wifi:icon_on.setImageResource(R.drawable.icon_wifi_on);icon_off.setImageResource(R.drawable.icon_wifi_off);locale=PublicConsts.ACTION_WIFI_LOCALE;break;
            case R.id.actions_bluetooth:icon_on.setImageResource(R.drawable.icon_bluetooth_on);icon_off.setImageResource(R.drawable.icon_bluetooth_off);locale=PublicConsts.ACTION_BLUETOOTH_LOCALE;break;
            case R.id.actions_net:icon_on.setImageResource(R.drawable.icon_cellular_on);icon_off.setImageResource(R.drawable.icon_cellular_off);locale=PublicConsts.ACTION_NET_LOCALE;break;
            case R.id.actions_ring_mode:locale=PublicConsts.ACTION_RING_MODE_LOCALE;break;
            case R.id.actions_devicecontrol:locale=PublicConsts.ACTION_DEVICECONTROL_LOCALE;break;
            case R.id.actions_gps:icon_on.setImageResource(R.drawable.icon_location_on);icon_off.setImageResource(R.drawable.icon_location_off);locale=PublicConsts.ACTION_GPS_LOCALE;break;
            case R.id.actions_airplane_mode:icon_on.setImageResource(R.drawable.icon_airplanemode_on);icon_off.setImageResource(R.drawable.icon_airplanemode_off);locale=PublicConsts.ACTION_AIRPLANE_MODE_LOCALE;break;
        }

        if(id==R.id.actions_ring_mode){
            int action_ring=-1;
            try{
                action_ring=Integer.parseInt(actions[PublicConsts.ACTION_RING_MODE_LOCALE]);
            }catch (NumberFormatException ne){
                ne.printStackTrace();
                LogUtil.putExceptionLog(this,ne);
            }

            if(action_ring==PublicConsts.ACTION_RING_NORMAL) rb_ring_normal.setChecked(true);
            if(action_ring==PublicConsts.ACTION_RING_VIBRATE) rb_ring_vibrate.setChecked(true);
            if(action_ring==PublicConsts.ACTION_RING_OFF) rb_ring_off.setChecked(true);
            if(action_ring==PublicConsts.ACTION_RING_UNSELECTED) rb_ring_unselected.setChecked(true);

            rl_ring_vibrate.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    actions[PublicConsts.ACTION_RING_MODE_LOCALE]=String.valueOf(PublicConsts.ACTION_RING_VIBRATE);//TaskGui.this.actions[PublicConsts.ACTION_RING_MODE_LOCALE]=PublicConsts.ACTION_RING_VIBRATE;
                    bdialog.cancel();
                    refreshActionStatus();
                }
            });

            rl_ring_off.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    actions[PublicConsts.ACTION_RING_MODE_LOCALE]=String.valueOf(PublicConsts.ACTION_RING_OFF);//TaskGui.this.actions[PublicConsts.ACTION_RING_MODE_LOCALE]=PublicConsts.ACTION_RING_OFF;
                    bdialog.cancel();
                    refreshActionStatus();
                }
            });

            rl_ring_normal.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    actions[PublicConsts.ACTION_RING_MODE_LOCALE]=String.valueOf(PublicConsts.ACTION_RING_NORMAL);//TaskGui.this.actions[PublicConsts.ACTION_RING_MODE_LOCALE]=PublicConsts.ACTION_RING_NORMAL;
                    bdialog.cancel();
                    refreshActionStatus();
                }
            });

            rl_ring_unselected.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    actions[PublicConsts.ACTION_RING_MODE_LOCALE]=String.valueOf(PublicConsts.ACTION_RING_UNSELECTED);//TaskGui.this.actions[PublicConsts.ACTION_RING_MODE_LOCALE]=PublicConsts.ACTION_RING_UNSELECTED;
                    bdialog.cancel();
                    refreshActionStatus();
                }
            });

        }else if(id==R.id.actions_devicecontrol){
            int action_device=-1;
            try{
                action_device=Integer.parseInt(actions[PublicConsts.ACTION_DEVICECONTROL_LOCALE]);
            }catch (NumberFormatException ne){
                ne.printStackTrace();
                LogUtil.putExceptionLog(this,ne);
            }
            if(action_device==PublicConsts.ACTION_DEVICECONTROL_REBOOT) rb_devicecontrol_reboot.setChecked(true);//if(actions[PublicConsts.ACTION_DEVICECONTROL_LOCALE]==PublicConsts.ACTION_DEVICECONTROL_REBOOT) rb_devicecontrol_reboot.setChecked(true);
            if(action_device==PublicConsts.ACTION_DEVICECONTROL_SHUTDOWN) rb_devicecontrol_shutdown.setChecked(true);//if(actions[PublicConsts.ACTION_DEVICECONTROL_LOCALE]==PublicConsts.ACTION_DEVICECONTROL_SHUTDOWN) rb_devicecontrol_shutdown.setChecked(true);
            if(action_device==PublicConsts.ACTION_DEVICECONSTROL_NONE) rb_devicecontrol_unselected.setChecked(true);//if(actions[PublicConsts.ACTION_DEVICECONTROL_LOCALE]==PublicConsts.ACTION_DEVICECONSTROL_NONE) rb_devicecontrol_unselected.setChecked(true);

            rl_reboot.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    actions[PublicConsts.ACTION_DEVICECONTROL_LOCALE]=String.valueOf(PublicConsts.ACTION_DEVICECONTROL_REBOOT);//TaskGui.this.actions[PublicConsts.ACTION_DEVICECONTROL_LOCALE]=PublicConsts.ACTION_DEVICECONTROL_REBOOT;
                    bdialog.cancel();
                    refreshActionStatus();
                }
            });

            rl_shutdown.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    actions[PublicConsts.ACTION_DEVICECONTROL_LOCALE]=String.valueOf(PublicConsts.ACTION_DEVICECONTROL_SHUTDOWN);//TaskGui.this.actions[PublicConsts.ACTION_DEVICECONTROL_LOCALE]=PublicConsts.ACTION_DEVICECONTROL_SHUTDOWN;
                    bdialog.cancel();
                    refreshActionStatus();
                }
            });

            rl_unselected_devicecontrol.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    actions[PublicConsts.ACTION_DEVICECONTROL_LOCALE]=String.valueOf(PublicConsts.ACTION_DEVICECONSTROL_NONE);//TaskGui.this.actions[PublicConsts.ACTION_DEVICECONTROL_LOCALE]=PublicConsts.ACTION_DEVICECONSTROL_NONE;
                    bdialog.cancel();
                    refreshActionStatus();
                }
            });
        }else{

            int action_value=-1;
            try{
                action_value=Integer.parseInt(actions[locale]);
            }catch (NumberFormatException ne){
                ne.printStackTrace();
                LogUtil.putExceptionLog(this,ne);
            }

            if(action_value==PublicConsts.ACTION_OPEN) rb_open.setChecked(true);//if(actions[locale]==PublicConsts.ACTION_OPEN) rb_open.setChecked(true);
            if(action_value==PublicConsts.ACTION_CLOSE) rb_close.setChecked(true);//if(actions[locale]==PublicConsts.ACTION_CLOSE) rb_close.setChecked(true);
            if(action_value==PublicConsts.ACTION_UNSELECTED) rb_unselected.setChecked(true);//if(actions[locale]==PublicConsts.ACTION_UNSELECTED) rb_unselected.setChecked(true);
            final int locale_listener=locale;
            rl_open.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    actions[locale_listener]=String.valueOf(PublicConsts.ACTION_OPEN);//TaskGui.this.actions[locale_listener]=PublicConsts.ACTION_OPEN;
                    bdialog.cancel();
                    refreshActionStatus();
                }
            });

            rl_close.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    actions[locale_listener]=String.valueOf(PublicConsts.ACTION_CLOSE);//TaskGui.this.actions[locale_listener]=PublicConsts.ACTION_CLOSE;
                    bdialog.cancel();
                    refreshActionStatus();
                }
            });

            rl_unselected.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    actions[locale_listener]=String.valueOf(PublicConsts.ACTION_UNSELECTED);//TaskGui.this.actions[locale_listener]=PublicConsts.ACTION_UNSELECTED;
                    bdialog.cancel();
                    refreshActionStatus();
                }
            });
        }

        bdialog.show();
    }

    private void showSnackBarOfSuperuserRequest(){
        Snackbar.make(findViewById(R.id.layout_actions_root),getResources().getString(R.string.activity_taskgui_root_required),Snackbar.LENGTH_SHORT)
                .setAction(getResources().getString(R.string.activity_taskgui_root_required_action), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(Actions.this,getResources().getString(R.string.activity_taskgui_root_toast_attention),Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Actions.this, com.github.ghmxr.timeswitch.activities.Settings.class));
                    }
                }).show();
    }

    private class TaskAdapter extends BaseAdapter{
        boolean isSelected[];
        private TaskAdapter(){isSelected=new boolean[TimeSwitchService.list!=null?TimeSwitchService.list.size():1];}
        @Override
        public int getCount() {
            return TimeSwitchService.list!=null?TimeSwitchService.list.size():0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(TimeSwitchService.list==null) return null;
            if(i>=TimeSwitchService.list.size()) return null;
            if(view==null){
                view=LayoutInflater.from(Actions.this).inflate(R.layout.item_dialog_task,viewGroup,false);
            }
            int imgRes=R.drawable.ic_launcher;
            TaskItem item=TimeSwitchService.list.get(i);
            switch (TimeSwitchService.list.get(i).trigger_type){
                default:break;
                case PublicConsts.TRIGGER_TYPE_SINGLE:imgRes=R.drawable.icon_repeat_single;break;
                case PublicConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME:imgRes=R.drawable.icon_repeat_percertaintime;break;
                case PublicConsts.TRIGGER_TYPE_LOOP_WEEK:imgRes=R.drawable.icon_repeat_weekloop;break;
                case PublicConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE:imgRes=R.drawable.icon_battery_high;break;
                case PublicConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE:imgRes=R.drawable.icon_battery_low;break;
                case PublicConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE: case PublicConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE:
                    imgRes=R.drawable.icon_temperature;break;
                case PublicConsts.TRIGGER_TYPE_RECEIVED_BROADTCAST:imgRes=R.drawable.icon_broadcast;break;
            }
            ((ImageView)view.findViewById(R.id.item_dialog_task_img)).setImageResource(imgRes);
            ((TextView)view.findViewById(R.id.item_dialog_task_name)).setText(item.name);
            ((TextView)view.findViewById(R.id.item_dialog_task_name_description)).setText(String.valueOf(item.isenabled?getResources().getString(R.string.opened):
                    getResources().getString(R.string.closed)));
            ((CheckBox)view.findViewById(R.id.item_dialog_task_cb)).setChecked(isSelected[i]);
            return view;
        }

        public void onItemClicked(int position){
            if(position<0) return;
            if(TimeSwitchService.list==null) return;
            if(position>=TimeSwitchService.list.size()) return;
            isSelected[position]=!isSelected[position];
            this.notifyDataSetChanged();
        }

        public boolean[] getIsSelected(){return this.isSelected;}

        public int getSelectedCount(){
            int s=0;
            for(boolean b:isSelected){
                if(b)s++;
            }
            return s;
        }

        public void setSelectedItems(String [] selectedIDs){
            if(selectedIDs==null) return;
            for(String id: selectedIDs){
                try{
                    int parsedid=Integer.parseInt(id);
                    if(parsedid<0) continue;
                    int position=ProcessTaskItem.getPosition(parsedid);
                    if(position>=0&&position<isSelected.length) isSelected[position]=true;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

}
