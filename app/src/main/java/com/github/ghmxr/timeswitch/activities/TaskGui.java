package com.github.ghmxr.timeswitch.activities;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.Global;
import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.adapters.ContentAdapter;
import com.github.ghmxr.timeswitch.data.v2.ActionConsts;
import com.github.ghmxr.timeswitch.data.v2.ExceptionConsts;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.ui.DialogForWifiInfoSelection;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialog;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForBatteryPercentageWithEnabledSelection;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForBatteryTemperatureWithEnabledSelection;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForBrightness;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForDeviceControl;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForFlashlight;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForNotification;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForPeriod;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForRingMode;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForToast;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForVibrate;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForVolume;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogForWifiStatus;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogWith2Selections;
import com.github.ghmxr.timeswitch.ui.bottomdialogs.BottomDialogWith3Selections;
import com.github.ghmxr.timeswitch.ui.DialogConfirmedCallBack;
import com.github.ghmxr.timeswitch.ui.DialogForAppSelection;
import com.github.ghmxr.timeswitch.ui.DialogForColor;
import com.github.ghmxr.timeswitch.ui.DialogForTaskSelection;
import com.github.ghmxr.timeswitch.utils.DisplayDensity;
import com.github.ghmxr.timeswitch.data.v2.MySQLiteOpenHelper;
import com.github.ghmxr.timeswitch.utils.EnvironmentUtils;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public abstract class TaskGui extends BaseActivity implements View.OnClickListener{

	public TaskItem taskitem=new TaskItem();
	private static final int REQUEST_CODE_TRIGGERS=0;
	private static final int REQUEST_CODE_EXCEPTIONS=1;
	private static final int REQUEST_CODE_ACTIONS=2;
	private static final int REQUEST_CODE_ACTION_RINGTONE=3;
	private static final int REQUEST_CODE_SET_WALLPAPER=4;
	private static final int REQUEST_CODE_SMS=5;
	private static final int REQUEST_CODE_PLAY_FROM_SYSTEM=6;
	private static final int REQUEST_CODE_PLAY_FROM_MEDIA=7;

	boolean isTaskNameEdited=false;

	private final View.OnClickListener listener_on_exception_item_clicked=new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent=new Intent();
			intent.setClass(TaskGui.this,ExceptionActivity.class);
			intent.putExtra(EXTRA_SERIALIZED_TASKITEM,taskitem);
			intent.putExtra(EXTRA_TITLE_COLOR,taskitem.addition_title_color);
			startActivityForResult(intent,REQUEST_CODE_EXCEPTIONS);
		}
	};

	public void onCreate(Bundle mybundle){
		super.onCreate(mybundle);
		setContentView(R.layout.layout_taskgui);
		Toolbar toolbar =findViewById(R.id.taskgui_toolbar);
		setSupportActionBar(toolbar);

		Calendar calendar=Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis()+10*60*1000);
		calendar.set(Calendar.SECOND,0);

		taskitem.time=calendar.getTimeInMillis();

		findViewById(R.id.layout_taskgui_area_name).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_enable).setOnClickListener(this);

		findViewById(R.id.layout_taskgui_trigger).setOnClickListener(this);

		findViewById(R.id.layout_taskgui_area_additional_notify).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_additional_autodelete).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_additional_autoclose).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_area_additional_delayed).setOnClickListener(this);
		findViewById(R.id.layout_taskgui_additional_titlecolor).setOnClickListener(this);

		findViewById(R.id.layout_taskgui_area_exception_additem).setOnClickListener(listener_on_exception_item_clicked);
		findViewById(R.id.layout_taskgui_area_action_additem).setOnClickListener(this);

		taskitem.addition_title_color=(getSharedPreferences(PublicConsts.PREFERENCES_NAME, Activity.MODE_PRIVATE).getString(PublicConsts.PREFERENCES_THEME_COLOR,PublicConsts.PREFERENCES_THEME_COLOR_DEFAULT));

		initialVariables();

		//do set the views of the variables.
		((TextView)findViewById(R.id.layout_taskgui_area_name_text)).setText(taskitem.name);
		((CheckBox)findViewById(R.id.layout_taskgui_area_additional_autoclose_cb)).setChecked(taskitem.autoclose);
		((CheckBox)findViewById(R.id.layout_taskgui_area_additional_autodelete_cb)).setChecked(taskitem.autodelete);
		((CheckBox)findViewById(R.id.layout_taskgui_area_additional_delayed_cb)).setChecked(taskitem.delayed);
		//activateTriggerType(taskitem.trigger_type);
		try{
			(findViewById(R.id.layout_taskgui_additional_titlecolor_img)).setBackgroundColor(Color.parseColor(taskitem.addition_title_color));
		}catch (Exception e){
			e.printStackTrace();
		}

		if(taskitem.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_SINGLE) setAutoCloseAreaEnabled(false);
		else {
			setAutoCloseAreaEnabled(!((CheckBox)findViewById(R.id.layout_taskgui_area_additional_autodelete_cb)).isChecked());
		}

		((SwitchCompat)findViewById(R.id.layout_taskgui_enable_sw)).setChecked(taskitem.isenabled);
		refreshTriggerDisplayValue();
		refreshExceptionViews();
		refreshActionStatus();
		setTaskThemeColor(taskitem.addition_title_color);
	}

	public abstract void initialVariables();

	private void setTaskThemeColor(String color){
		try{ setToolBarAndStatusBarColor(findViewById(R.id.taskgui_toolbar),color); } catch (Exception e){ e.printStackTrace(); }
	}

	@Override
	public void processMessage(Message msg){}

	private void refreshActionStatus(){
		final Resources resources=getResources();
		ViewGroup group=findViewById(R.id.layout_taskgui_area_action);
		group.removeAllViews();
		//checkAndPlayTransitionAnimation();
		final int action_wifi=Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_WIFI_LOCALE]);
		if(action_wifi>=0){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_wifi_on,
					resources.getString(R.string.activity_taskgui_actions_wifi),
					ContentAdapter.ActionContentAdapter.getGeneralDisplayValue(this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_WIFI_LOCALE]));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//Toast.makeText(TaskGui.this,"clicked",Toast.LENGTH_SHORT).show();
					BottomDialogWith3Selections dialog=new BottomDialogWith3Selections(TaskGui.this,R.drawable.icon_wifi_on,R.drawable.icon_wifi_off,action_wifi);
					dialog.show();
					dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
						@Override
						public void onDialogConfirmed(String result) {
							taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_WIFI_LOCALE]=String.valueOf(result);
							refreshActionStatus();
						}
					});
				}
			});
			group.addView(view);
		}

		final int action_bluetooth=Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BLUETOOTH_LOCALE]);
		if(action_bluetooth>=0){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_bluetooth_on,
					resources.getString(R.string.activity_taskgui_actions_bluetooth),
					ContentAdapter.ActionContentAdapter.getGeneralDisplayValue(this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BLUETOOTH_LOCALE]));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//Toast.makeText(TaskGui.this,"clicked",Toast.LENGTH_SHORT).show();
					BottomDialogWith3Selections dialog=new BottomDialogWith3Selections(TaskGui.this,R.drawable.icon_bluetooth_on,R.drawable.icon_bluetooth_off,action_bluetooth);
					dialog.show();
					dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
						@Override
						public void onDialogConfirmed(String result) {
							taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BLUETOOTH_LOCALE]=String.valueOf(result);
							refreshActionStatus();
						}
					});
				}
			});
			group.addView(view);
		}

        final int action_ring_mode=Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_MODE_LOCALE]);
		if(action_ring_mode>=0){
            View view=getActionItemViewForViewGroup(group,R.drawable.icon_ring_normal,
                    resources.getString(R.string.activity_taskgui_actions_ring_mode),
                    ContentAdapter.ActionContentAdapter.getRingModeDisplayValue(this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_MODE_LOCALE]));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(TaskGui.this,"clicked",Toast.LENGTH_SHORT).show();
                    BottomDialogForRingMode dialog=new BottomDialogForRingMode(TaskGui.this,action_ring_mode);
                    dialog.show();
                    dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                        @Override
                        public void onDialogConfirmed(String result) {
                            taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_MODE_LOCALE]=String.valueOf(result);
                            refreshActionStatus();
                        }
                    });
                }
            });
            group.addView(view);
        }

        if(!taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_VOLUME_LOCALE].equals("-1:-1:-1:-1")){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_ring_volume,
					resources.getString(R.string.activity_taskgui_actions_ring_volume),
					ContentAdapter.ActionContentAdapter.getRingVolumeDisplayValue(this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_VOLUME_LOCALE]));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//Toast.makeText(TaskGui.this,"clicked",Toast.LENGTH_SHORT).show();
					BottomDialogForVolume dialog=new BottomDialogForVolume(TaskGui.this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_VOLUME_LOCALE]);
					dialog.show();
					dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
						@Override
						public void onDialogConfirmed(String result) {
							taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_VOLUME_LOCALE]=String.valueOf(result);
							refreshActionStatus();
						}
					});
				}
			});
			group.addView(view);
		}

		if(!taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_SELECTION_LOCALE].equals("-1:-1")){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_music
			,resources.getString(R.string.activity_taskgui_actions_ring_selection),
					ContentAdapter.ActionContentAdapter.getRingSelectionDisplayValue(this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_RING_SELECTION_LOCALE]));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i=new Intent();
					i.setClass(TaskGui.this,ChangeRingtoneActivity.class);
					i.putExtra(EXTRA_TITLE_COLOR,taskitem.addition_title_color);
					i.putExtra(EXTRA_SERIALIZED_TASKITEM,taskitem);
					startActivityForResult(i,REQUEST_CODE_ACTION_RINGTONE);
				}
			});
			group.addView(view);
		}

		final int action_brightness=Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BRIGHTNESS_LOCALE]);
		if(action_brightness>=0){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_brightness
			,resources.getString(R.string.activity_taskgui_actions_brightness)
			, ContentAdapter.ActionContentAdapter.getBrightnessDisplayValue(this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BRIGHTNESS_LOCALE]));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					BottomDialogForBrightness dialog=new BottomDialogForBrightness(TaskGui.this,action_brightness);
					dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
						@Override
						public void onDialogConfirmed(String result) {
							taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_BRIGHTNESS_LOCALE]=result;
							refreshActionStatus();
						}
					});
					dialog.show();
				}
			});
			group.addView(view);
		}

		if(!"-1:-1:-1".equals(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_VIBRATE_LOCALE])){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_ring_vibrate,resources.getString(R.string.activity_taskgui_actions_vibrate)
			, ContentAdapter.ActionContentAdapter.getVibrateDisplayValue(this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_VIBRATE_LOCALE]));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					BottomDialogForVibrate dialog=new BottomDialogForVibrate(TaskGui.this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_VIBRATE_LOCALE]);
					dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
						@Override
						public void onDialogConfirmed(String result) {
							taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_VIBRATE_LOCALE]=result;
							refreshActionStatus();
						}
					});
					dialog.show();
				}
			});
			group.addView(view);
		}

		final int action_autorotation=Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AUTOROTATION]);
		if(action_autorotation>=0){
		    View view=getActionItemViewForViewGroup(group,R.drawable.icon_autorotation,resources.getString(R.string.activity_taskgui_actions_autorotation)
            , ContentAdapter.ActionContentAdapter.getGeneralDisplayValue(this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AUTOROTATION]));
		    view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BottomDialogWith3Selections dialog=new BottomDialogWith3Selections(TaskGui.this,R.drawable.icon_autorotation,R.drawable.icon_autorotation_off,action_autorotation);
                    dialog.show();
                    dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                        @Override
                        public void onDialogConfirmed(String result) {
                            taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AUTOROTATION]=String.valueOf(result);
                            refreshActionStatus();
                        }
                    });
                }
            });
		    group.addView(view);
        }

        final int action_wallpaper=Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SET_WALL_PAPER_LOCALE]);
		if(action_wallpaper>=0){
		    View view =getActionItemViewForViewGroup(group,R.drawable.icon_wallpaper
                    ,resources.getString(R.string.action_set_wallpaper)
            , ContentAdapter.ActionContentAdapter.getWallpaperDisplayValue(this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SET_WALL_PAPER_LOCALE],taskitem.uri_wallpaper_desktop));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BottomDialogWith2Selections dialog=new BottomDialogWith2Selections(TaskGui.this,R.drawable.icon_wallpaper,resources.getString(R.string.dialog_actions_wallpaper_select),action_wallpaper);
                    dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
                        @Override
                        public void onDialogConfirmed(String result) {
                            if(result.equals("0")){
                                startActivityForResult(new Intent(Intent.ACTION_PICK,
                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI),REQUEST_CODE_SET_WALLPAPER);
                                //Toast.makeText(ActionActivity.this,getResources().getString(R.string.dialog_actions_wallpaper_att),Toast.LENGTH_SHORT).show();
                            }
                            else {
                                taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SET_WALL_PAPER_LOCALE]=String.valueOf(-1);
                                refreshActionStatus();
                            }
                        }
                    });
                    dialog.show();
                }
            });
            group.addView(view);
        }

        final int action_flashlight=Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FLASHLIGHT].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[0]);
		if(action_flashlight>=0){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_flashlight
			,resources.getString(R.string.action_flashlight)
			,ContentAdapter.ActionContentAdapter.getFlashlightDisplayValue(this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FLASHLIGHT]));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					BottomDialogForFlashlight dialog=new BottomDialogForFlashlight(TaskGui.this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FLASHLIGHT]);
					dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
						@Override
						public void onDialogConfirmed(String result) {
							taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FLASHLIGHT]=result;
							refreshActionStatus();
						}
					});
					dialog.show();
				}
			});
			group.addView(view);
		}

        if(Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SMS_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[ActionConsts.ActionSecondLevelLocaleConsts.SMS_ENABLED_LOCALE])>=0){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_sms,resources.getString(R.string.activity_taskgui_actions_sms)
					, ContentAdapter.ActionContentAdapter.getSMSDisplayValue(this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SMS_LOCALE]));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i=new Intent(TaskGui.this,SmsActivity.class);
					i.putExtra(EXTRA_SERIALIZED_TASKITEM,taskitem);
					i.putExtra(EXTRA_TITLE_COLOR,taskitem.addition_title_color);
					startActivityForResult(i,REQUEST_CODE_SMS);
				}
			});
			group.addView(view);
		}

		if(Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NOTIFICATION_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[0])>=0){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_notification,resources.getString(R.string.activity_taskgui_actions_notification)
			, ContentAdapter.ActionContentAdapter.getNotificationDisplayValue(this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NOTIFICATION_LOCALE]));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					BottomDialogForNotification dialog=new BottomDialogForNotification(TaskGui.this
							,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NOTIFICATION_LOCALE]
							,taskitem.notification_title,taskitem.notification_message);
					dialog.setOnDialogConfirmedCallback(new BottomDialogForNotification.DialogConfirmedCallback() {
						@Override
						public void onDialogConfirmed(String[] result) {
							taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NOTIFICATION_LOCALE]=result[0];
							taskitem.notification_title=result[1];
							taskitem.notification_message=result[2];
							refreshActionStatus();
						}
					});
					dialog.show();
				}
			});
			group.addView(view);
		}

		if(Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_TOAST_LOCALE].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[0])>=0){
		    View view=getActionItemViewForViewGroup(group,R.drawable.icon_toast,resources.getString(R.string.activity_taskgui_actions_toast)
                    , ContentAdapter.ActionContentAdapter.getToastDisplayValue(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_TOAST_LOCALE],taskitem.toast));
		    view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BottomDialogForToast dialog=new BottomDialogForToast(TaskGui.this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_TOAST_LOCALE],taskitem.toast);
                    dialog.setOnDialogConfirmedCallback(new BottomDialogForToast.DialogConfirmedCallback() {
                        @Override
                        public void onDialogConfirmed(String[] result) {
                            taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_TOAST_LOCALE]=result[0];
                            taskitem.toast=result[1];
                            refreshActionStatus();
                        }
                    });
                    dialog.show();
                }
            });
		    group.addView(view);
        }

		if(Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_PLAY_AUDIO])>=0){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_play,getResources().getString(R.string.activity_taskgui_actions_play)
			,ContentAdapter.ActionContentAdapter.getPlayDisplayValue(TaskGui.this,taskitem));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Toast.makeText(TaskGui.this,getResources().getString(R.string.activity_taskgui_action_play_att),Toast.LENGTH_SHORT).show();
					final int selection=Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_PLAY_AUDIO]);
					final BottomDialog dialog=new BottomDialog(TaskGui.this);
					dialog.setContentView(R.layout.layout_dialog_ring_selection);
					dialog.show();
					((RadioButton)dialog.findViewById(R.id.dialog_ring_unselected_rb)).setChecked(taskitem.uri_play==null||taskitem.uri_play.equals("")||selection==-1);
					((RadioButton)dialog.findViewById(R.id.dialog_ring_system_rb)).setChecked(selection== ActionConsts.ActionValueConsts.RING_TYPE_FROM_SYSTEM);
					((RadioButton)dialog.findViewById(R.id.dialog_ring_media_rb)).setChecked(selection== ActionConsts.ActionValueConsts.RING_TYPE_FROM_MEDIA);
					dialog.findViewById(R.id.dialog_ring_unselected).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_PLAY_AUDIO]=String.valueOf(-1);
							refreshActionStatus();
							dialog.cancel();
						}
					});
					dialog.findViewById(R.id.dialog_ring_system).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							dialog.cancel();
							Intent intent=new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
							intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,false);
							intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,false);
							intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_ALL);
							if(taskitem.uri_play!=null&&selection== ActionConsts.ActionValueConsts.RING_TYPE_FROM_SYSTEM)intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,Uri.parse(taskitem.uri_play));
							startActivityForResult(intent,REQUEST_CODE_PLAY_FROM_SYSTEM);
						}
					});
					dialog.findViewById(R.id.dialog_ring_media).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							dialog.cancel();
							Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
							startActivityForResult(intent,REQUEST_CODE_PLAY_FROM_MEDIA);
						}
					});
					dialog.show();
				}
			});
			group.addView(view);
		}

		int action_notification=0;
		try{
			action_notification=Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_CLEAN_NOTIFICATION]);
		}catch (Exception e){}
		if(action_notification>=0){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_clean,
					getResources().getString(R.string.activity_taskgui_action_clean_notification)
			,ContentAdapter.ActionContentAdapter.getCleaningNotificationValue(this,taskitem));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if(!EnvironmentUtils.PermissionRequestUtil.checkAndShowNotificationReadingRequestSnackbar(TaskGui.this,
							getResources().getString(R.string.activity_taskgui_action_clean_notification_permission)
							,getResources().getString(R.string.permission_grant_action_att))) return;
					int selection=0;
					//String [] package_names=null;
					final String value=taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_CLEAN_NOTIFICATION];
					try{
						selection=Integer.parseInt(value);
					}catch (NumberFormatException ne){
						try{
							//package_names=value.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
						}catch (Exception e){}
					}

					final BottomDialogWith3Selections dialog=new BottomDialogWith3Selections(TaskGui.this,getResources().getString(R.string.activity_taskgui_action_clean_notification),selection,R.drawable.icon_notification_clear_all
							,R.drawable.icon_robot,R.drawable.icon_unselected,getResources().getString(R.string.activity_taskgui_action_clean_notfication_all)
							,getResources().getString(R.string.activity_taskgui_action_clean_notification_package)
							,getResources().getString(R.string.unselected));

					dialog.show();
					dialog.findViewById(R.id.selection_area_unselected).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_CLEAN_NOTIFICATION]=String.valueOf(-1);
							refreshActionStatus();
							dialog.cancel();
						}
					});
					dialog.findViewById(R.id.selection_area_open).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_CLEAN_NOTIFICATION]=String.valueOf(ActionConsts.ActionValueConsts.ACTION_CLEAN_NOTIFICATION_ALL);
							refreshActionStatus();
							dialog.cancel();
						}
					});
					dialog.findViewById(R.id.selection_area_close).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							dialog.cancel();
							String[]package_names=null;
							try{
								package_names=value.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
							}catch (Exception e){}
							DialogForAppSelection dialog1=new DialogForAppSelection(TaskGui.this
									,getResources().getString(R.string.activity_taskgui_action_clean_notification_package)
									,package_names==null?new String[0]:package_names,null,"");
							dialog1.show();
							dialog1.setOnDialogConfirmedCallBack(new DialogConfirmedCallBack() {
								@Override
								public void onDialogConfirmed(String result) {
									if(result.equals(String.valueOf(-1)))return;
									//result.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL);
									taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_CLEAN_NOTIFICATION]=result;
									refreshActionStatus();
								}
							});
						}
					});
				}
			});
			group.addView(view);
		}

        int action_net=Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NET_LOCALE]);
		if(action_net>=0){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_cellular_on,resources.getString(R.string.activity_taskgui_actions_net)
			, ContentAdapter.ActionContentAdapter.getGeneralDisplayValue(this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NET_LOCALE]));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					BottomDialogWith3Selections dialog=new BottomDialogWith3Selections(TaskGui.this,R.drawable.icon_cellular_on,R.drawable.icon_cellular_off
							,Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NET_LOCALE]));
					dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
						@Override
						public void onDialogConfirmed(String result) {
							taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_NET_LOCALE]=result;
							refreshActionStatus();
						}
					});
					dialog.show();
				}
			});
			group.addView(view);
		}

		int action_gps=Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_GPS_LOCALE]);
		if(action_gps>=0){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_location_on,resources.getString(R.string.activity_taskgui_actions_gps)
			, ContentAdapter.ActionContentAdapter.getGeneralDisplayValue(this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_GPS_LOCALE]));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					BottomDialogWith3Selections dialog=new BottomDialogWith3Selections(TaskGui.this,R.drawable.icon_location_on,R.drawable.icon_location_off
							,Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_GPS_LOCALE]));
					dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
						@Override
						public void onDialogConfirmed(String result) {
							taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_GPS_LOCALE]=result;
							refreshActionStatus();
						}
					});
					dialog.show();
				}
			});
			group.addView(view);
		}

		int action_airplane_mode=Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AIRPLANE_MODE_LOCALE]);
		if(action_airplane_mode>=0){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_airplanemode_on,resources.getString(R.string.activity_taskgui_actions_airplane_mode)
			, ContentAdapter.ActionContentAdapter.getGeneralDisplayValue(this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AIRPLANE_MODE_LOCALE]));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					BottomDialogWith3Selections dialog=new BottomDialogWith3Selections(TaskGui.this,R.drawable.icon_airplanemode_on,R.drawable.icon_airplanemode_off
							,Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AIRPLANE_MODE_LOCALE]));
					dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
						@Override
						public void onDialogConfirmed(String result) {
							taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_AIRPLANE_MODE_LOCALE]=result;
							refreshActionStatus();
						}
					});
					dialog.show();
				}
			});
			group.addView(view);
		}

		int action_device_control=Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DEVICE_CONTROL_LOCALE]);
		if(action_device_control>=0){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_screen_on,resources.getString(R.string.activity_taskgui_actions_device_control)
			, ContentAdapter.ActionContentAdapter.getDeviceControlDisplayValue(this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DEVICE_CONTROL_LOCALE]));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					BottomDialogForDeviceControl dialog=new BottomDialogForDeviceControl(TaskGui.this,Integer.parseInt(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DEVICE_CONTROL_LOCALE]));
					dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
						@Override
						public void onDialogConfirmed(String result) {
							taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DEVICE_CONTROL_LOCALE]=result;
							refreshActionStatus();
						}
					});
					dialog.show();
				}
			});
			group.addView(view);
		}

		if(!taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_ENABLE_TASKS_LOCALE].equals("-1")){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_mark,resources.getString(R.string.adapter_action_task_enable)
			, ContentAdapter.ActionContentAdapter.getTaskSwitchDisplayValue(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_ENABLE_TASKS_LOCALE]));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					DialogForTaskSelection dialog=new DialogForTaskSelection(TaskGui.this
							,getResources().getString(R.string.activity_taskgui_actions_enable)
							, TimeSwitchService.list,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_ENABLE_TASKS_LOCALE],null);
					dialog.setOnDialogConfirmedCallback(new DialogConfirmedCallBack() {
						@Override
						public void onDialogConfirmed(String result) {
							taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_ENABLE_TASKS_LOCALE]=result;
							refreshActionStatus();
						}
					});
					dialog.show();
				}
			});
			group.addView(view);
		}

		if(!taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DISABLE_TASKS_LOCALE].equals("-1")){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_cross,resources.getString(R.string.adapter_action_task_disable)
			, ContentAdapter.ActionContentAdapter.getTaskSwitchDisplayValue(taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DISABLE_TASKS_LOCALE]));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					DialogForTaskSelection dialog=new DialogForTaskSelection(TaskGui.this,getResources().getString(R.string.activity_taskgui_actions_disable)
					,TimeSwitchService.list,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DISABLE_TASKS_LOCALE],"#55e74c3c");
					dialog.setOnDialogConfirmedCallback(new DialogConfirmedCallBack() {
						@Override
						public void onDialogConfirmed(String result) {
							taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_DISABLE_TASKS_LOCALE]=result;
							refreshActionStatus();
						}
					});
					dialog.show();
				}
			});
			group.addView(view);
		}

		if(!taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_LAUNCH_APP_PACKAGES].equals("-1")){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_app_launch,resources.getString(R.string.activity_action_app_open_title),
					ContentAdapter.ActionContentAdapter.getAppNameDisplayValue(this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_LAUNCH_APP_PACKAGES]));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					DialogForAppSelection dialog=new DialogForAppSelection(TaskGui.this,resources.getString(R.string.activity_action_app_open_title)
					,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_LAUNCH_APP_PACKAGES],null,resources.getString(R.string.dialog_app_select_long_press_test));
					dialog.setOnDialogConfirmedCallBack(new DialogConfirmedCallBack() {
						@Override
						public void onDialogConfirmed(String result) {
							taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_LAUNCH_APP_PACKAGES]=result;
							refreshActionStatus();
						}
					});
					dialog.show();
				}
			});
			group.addView(view);
		}
		if(!taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_STOP_APP_PACKAGES].equals("-1")){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_app_stop,resources.getString(R.string.activity_action_app_close_title),
					ContentAdapter.ActionContentAdapter.getAppNameDisplayValue(this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_STOP_APP_PACKAGES]));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					DialogForAppSelection dialog=new DialogForAppSelection(TaskGui.this,resources.getString(R.string.activity_action_app_close_title)
							,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_STOP_APP_PACKAGES],"#55e74c3c",resources.getString(R.string.dialog_app_close_att));
					dialog.setOnDialogConfirmedCallBack(new DialogConfirmedCallBack() {
						@Override
						public void onDialogConfirmed(String result) {
							taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_STOP_APP_PACKAGES]=result;
							refreshActionStatus();
						}
					});
					dialog.show();
				}
			});
			group.addView(view);
		}

		if(!taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FORCE_STOP_APP_PACKAGES].equals("-1")){
			View view=getActionItemViewForViewGroup(group,R.drawable.icon_app_force_stop,resources.getString(R.string.activity_taskgui_actions_app_force_close)
			,ContentAdapter.ActionContentAdapter.getAppNameDisplayValue(this,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FORCE_STOP_APP_PACKAGES]));
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					DialogForAppSelection dialog=new DialogForAppSelection(TaskGui.this,resources.getString(R.string.activity_taskgui_actions_app_force_close)
							,taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FORCE_STOP_APP_PACKAGES]
							,"#55e74c3c"
							, resources.getString(R.string.dialog_app_force_close_att));
					dialog.setOnDialogConfirmedCallBack(new DialogConfirmedCallBack() {
						@Override
						public void onDialogConfirmed(String result) {
							taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_FORCE_STOP_APP_PACKAGES]=result;
							refreshActionStatus();
						}
					});
					dialog.show();
				}
			});
			group.addView(view);
		}
    }

    private void refreshTriggerDisplayValue(){
		ImageView icon=findViewById(R.id.layout_taskgui_trigger_icon);
		TextView att=findViewById(R.id.layout_taskgui_trigger_att);
		TextView value=findViewById(R.id.layout_taskgui_trigger_value);
		try{
			Integer icon_res_id=(Integer)ContentAdapter.TriggerContentAdapter.getContentForTriggerType(this, ContentAdapter.TriggerContentAdapter.CONTENT_TYPE_ICON_RESOURCE_DRAWABLE_ID,taskitem);
			if(icon_res_id!=null) icon.setImageResource(icon_res_id);
			att.setText((String) ContentAdapter.TriggerContentAdapter.getContentForTriggerType(this,ContentAdapter.TriggerContentAdapter.CONTENT_TYPE_DISPLAY_STRING_TITLE,taskitem));
			value.setText((String)ContentAdapter.TriggerContentAdapter.getContentForTriggerType(this,ContentAdapter.TriggerContentAdapter.CONTENT_TYPE_DISPLAY_STRING_CONTENT,taskitem));
		}catch (Exception e){e.printStackTrace();}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
			default:break;
			case R.id.layout_taskgui_area_name:{
				View dialogview =LayoutInflater.from(this).inflate(R.layout.layout_dialog_name,null);
				final EditText edittext =dialogview.findViewById(R.id.dialog_edittext_name);
				edittext.setText(TaskGui.this.taskitem.name);//edittext.setText(TaskGui.this.taskname);
				final AlertDialog dialog=new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.dialog_task_name_title))
						.setView(dialogview)
						.setPositiveButton(getResources().getString(R.string.dialog_button_positive), null).create();
				dialog.show();
				dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String name=edittext.getText().toString().trim();
						if(name.length()<=0||name.equals("")){
							Toast.makeText(TaskGui.this,getResources().getString(R.string.dialog_task_name_invalid),Toast.LENGTH_SHORT).show();
							//Snackbar.make(v,getResources().getString(R.string.dialog_task_name_invalid),Snackbar.LENGTH_SHORT).show();
						}
						else{
							taskitem.name=name;
							dialog.cancel();
							isTaskNameEdited=true;
							((TextView)findViewById(R.id.layout_taskgui_area_name_text)).setText(taskitem.name);
						}
					}
				});
			}
			break;
			case R.id.layout_taskgui_area_enable:{
				SwitchCompat sw=((SwitchCompat)findViewById(R.id.layout_taskgui_enable_sw));
				sw.toggle();
				taskitem.isenabled=sw.isChecked();
			}
			break;
			case R.id.layout_taskgui_trigger:{
				Intent i=new Intent();
				i.setClass(this,TriggerActivity.class);
				i.putExtra(EXTRA_TITLE_COLOR,taskitem.addition_title_color);
				i.putExtra(BaseActivity.EXTRA_SERIALIZED_TASKITEM,taskitem);
				startActivityForResult(i,REQUEST_CODE_TRIGGERS);
			}
			break;

			case R.id.layout_taskgui_area_action_additem:{
				Intent i=new Intent(this,ActionActivity.class);
				i.putExtra(EXTRA_SERIALIZED_TASKITEM,taskitem);
				i.putExtra(EXTRA_TITLE_COLOR,taskitem.addition_title_color);
				startActivityForResult(i,REQUEST_CODE_ACTIONS);
			}
			break;

			case R.id.layout_taskgui_area_additional_autodelete:{
				CheckBox cb_autodelete=findViewById(R.id.layout_taskgui_area_additional_autodelete_cb);
				cb_autodelete.toggle();
				if(taskitem.trigger_type!= TriggerTypeConsts.TRIGGER_TYPE_SINGLE) setAutoCloseAreaEnabled(!cb_autodelete.isChecked());
				taskitem.autodelete=cb_autodelete.isChecked();
			}
			break;
			case R.id.layout_taskgui_area_additional_autoclose:{
				CheckBox cb_autoclose=findViewById(R.id.layout_taskgui_area_additional_autoclose_cb);
				if(cb_autoclose.isEnabled()) {
					cb_autoclose.toggle();
					taskitem.autoclose=cb_autoclose.isChecked();
				}
			}
			break;
            case R.id.layout_taskgui_area_additional_delayed:{
                CheckBox cb_delayed=findViewById(R.id.layout_taskgui_area_additional_delayed_cb);
                cb_delayed.toggle();
                taskitem.delayed=cb_delayed.isChecked();
            }
            break;
			case R.id.layout_taskgui_additional_titlecolor:{
				DialogForColor dialog=new DialogForColor(this,taskitem.addition_title_color);
				dialog.setTitle(getResources().getString(R.string.activity_taskgui_additional_titlecolor_att));
				dialog.show();
				dialog.setOnDialogConfirmListener(new DialogForColor.OnDialogForColorConfirmedListener() {
					@Override
					public void onConfirmed(String color) {
						taskitem.addition_title_color=color;
						try{
							findViewById(R.id.layout_taskgui_additional_titlecolor_img).setBackgroundColor(Color.parseColor(taskitem.addition_title_color));
							setTaskThemeColor(taskitem.addition_title_color);
						}catch (Exception e){
							e.printStackTrace();
						}
					}
				});
			}
			break;
		}
		
	}

	private void setAutoCloseAreaEnabled(boolean b){
		RelativeLayout rl_autoclose=findViewById(R.id.layout_taskgui_area_additional_autoclose);
		CheckBox cb_autoclose=findViewById(R.id.layout_taskgui_area_additional_autoclose_cb);
		TextView tv_autoclose=findViewById(R.id.layout_taskgui_additional_autoclose_att);
		rl_autoclose.setClickable(b);
		cb_autoclose.setEnabled(b);
		tv_autoclose.setTextColor(b?getResources().getColor(R.color.color_text_normal):getResources().getColor(R.color.color_text_disabled));
		if(!b) {
			cb_autoclose.setChecked(true);
			taskitem.autoclose=true;
		}else{
			cb_autoclose.setChecked(taskitem.autoclose);
		}
	}

	public void refreshExceptionViews(){
		final ViewGroup group=((ViewGroup)findViewById(R.id.layout_taskgui_area_exception));
		group.removeAllViews();
		checkAndPlayTransitionAnimation();
		Resources resources=getResources();
		if(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_LOCKEDSCREEN])==1){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_screen_off,resources.getString(R.string.activity_taskgui_exception_screen_locked),null,listener_on_exception_item_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_LOCKEDSCREEN]=String.valueOf(0);
					//refreshExceptionViews();
					checkAndPlayTransitionAnimation();
					group.removeView(view);
				}
			});
			group.addView(view);
		}
		if(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_UNLOCKEDSCREEN])==1){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_screen_on,resources.getString(R.string.activity_taskgui_exception_screen_unlocked),null,listener_on_exception_item_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_UNLOCKEDSCREEN]=String.valueOf(0);
					//refreshExceptionViews();
                    checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}
		if(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_RING_VIBRATE])==1){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_ring_vibrate,resources.getString(R.string.activity_taskgui_exception_ring_vibrate),null,listener_on_exception_item_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_RING_VIBRATE]=String.valueOf(0);
					//refreshExceptionViews();
                    checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}
		if(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_RING_OFF])==1){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_ring_off,resources.getString(R.string.activity_taskgui_exception_ring_off),null,listener_on_exception_item_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_RING_OFF]=String.valueOf(0);
					//refreshExceptionViews();
                    checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}
		if(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_RING_NORMAL])==1){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_ring_normal,resources.getString(R.string.activity_taskgui_exception_ring_normal),null,listener_on_exception_item_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_RING_NORMAL]=String.valueOf(0);
					//refreshExceptionViews();
                    checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}
		if(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_WIFI_ENABLED])==1){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_wifi_on,resources.getString(R.string.activity_taskgui_exception_wifi_enabled),null,listener_on_exception_item_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_WIFI_ENABLED]=String.valueOf(0);
					//refreshExceptionViews();
                    checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}
		if(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_WIFI_DISABLED])==1){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_wifi_off,resources.getString(R.string.activity_taskgui_exception_wifi_disabled),null,listener_on_exception_item_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_WIFI_DISABLED]=String.valueOf(0);
					checkAndPlayTransitionAnimation();
					//refreshExceptionViews();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}
		if(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_BLUETOOTH_ENABLED])==1){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_bluetooth_on,resources.getString(R.string.activity_taskgui_exception_bluetooth_enabled),null,listener_on_exception_item_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_BLUETOOTH_ENABLED]=String.valueOf(0);
					//refreshExceptionViews();
                    checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}
		if(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_BLUETOOTH_DISABLED])==1){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_bluetooth_off,resources.getString(R.string.activity_taskgui_exception_bluetooth_disabled),null,listener_on_exception_item_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_BLUETOOTH_DISABLED]=String.valueOf(0);
					//refreshExceptionViews();
                    checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}
		if(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_NET_ENABLED])==1){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_cellular_on,resources.getString(R.string.activity_taskgui_exception_net_enabled),null,listener_on_exception_item_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_NET_ENABLED]=String.valueOf(0);
					//refreshExceptionViews();
                    checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}
		if(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_NET_DISABLED])==1){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_cellular_off,resources.getString(R.string.activity_taskgui_exception_net_disabled),null,listener_on_exception_item_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_NET_DISABLED]=String.valueOf(0);
					//refreshExceptionViews();
                    checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}
		if(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_GPS_ENABLED])==1){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_location_on,resources.getString(R.string.activity_taskgui_exception_gps_on),null,listener_on_exception_item_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_GPS_ENABLED]=String.valueOf(0);
					//refreshExceptionViews();
                    checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}
		if(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_GPS_DISABLED])==1){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_location_off,resources.getString(R.string.activity_taskgui_exception_gps_off),null,listener_on_exception_item_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_GPS_DISABLED]=String.valueOf(0);
					//refreshExceptionViews();
                    checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}
		if(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_AIRPLANE_MODE_ENABLED])==1){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_airplanemode_on,resources.getString(R.string.activity_taskgui_exception_airplanemode_on),null,listener_on_exception_item_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_AIRPLANE_MODE_ENABLED]=String.valueOf(0);
					//refreshExceptionViews();
                    checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}
		if(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_AIRPLANE_MODE_DISABLED])==1){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_airplanemode_off,resources.getString(R.string.activity_taskgui_exception_airplanemode_off),null,listener_on_exception_item_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_AIRPLANE_MODE_DISABLED]=String.valueOf(0);
					//refreshExceptionViews();
                    checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}
		if(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_IS_IN_CALL_COMING_STATE])==1){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_call,resources.getString(R.string.activity_taskgui_exception_incall_ring),null,listener_on_exception_item_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_IS_IN_CALL_COMING_STATE]=String.valueOf(0);
					//refreshExceptionViews();
					checkAndPlayTransitionAnimation();
					group.removeView(view);
				}
			});
			group.addView(view);
		}
		if(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_IS_IN_CALL_CONNECTED_STATE])==1){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_call_incall,resources.getString(R.string.activity_taskgui_exception_incall_connected),null,listener_on_exception_item_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_IS_IN_CALL_CONNECTED_STATE]=String.valueOf(0);
					//refreshExceptionViews();
					checkAndPlayTransitionAnimation();
					group.removeView(view);
				}
			});
			group.addView(view);
		}
		if(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_IS_NOT_IN_CALL_STATE])==1){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_call_end,resources.getString(R.string.activity_taskgui_exception_incall_disconnected),null,listener_on_exception_item_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_IS_NOT_IN_CALL_STATE]=String.valueOf(0);
					//refreshExceptionViews();
					checkAndPlayTransitionAnimation();
					group.removeView(view);
				}
			});
			group.addView(view);
		}
		int headset_status=Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_HEADSET_STATUS]);
		final View.OnClickListener listener_headset_clicked=new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					int headset_selection=Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_HEADSET_STATUS]);
					final AlertDialog dialog=new AlertDialog.Builder(TaskGui.this)
							.setTitle(getResources().getString(R.string.activity_taskgui_exception_headset))
							.setIcon(R.drawable.icon_headset)
							.setView(LayoutInflater.from(TaskGui.this).inflate(R.layout.layout_dialog_with_three_single_choices,null))
							.show();
					RadioButton button_unselected=dialog.findViewById(R.id.dialog3_choice_first);
					RadioButton button_plugged=dialog.findViewById(R.id.dialog3_choice_second);
					RadioButton button_unplugged=dialog.findViewById(R.id.dialog3_choice_third);
					button_unselected.setText(getResources().getString(R.string.word_unselected));
					button_plugged.setText(getResources().getString(R.string.activity_taskgui_exception_headset_in));
					button_unplugged.setText(getResources().getString(R.string.activity_taskgui_exception_headset_out));
					button_unselected.setChecked(headset_selection==0);
					button_plugged.setChecked(headset_selection== ExceptionConsts.EXCEPTION_HEADSET_PLUG_IN);
					button_unplugged.setChecked(headset_selection== ExceptionConsts.EXCEPTION_HEADSET_PLUG_OUT);
					button_unselected.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							taskitem.exceptions[ExceptionConsts.EXCEPTION_HEADSET_STATUS]=String.valueOf(0);
							dialog.cancel();
							refreshExceptionViews();
						}
					});
					button_unplugged.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							taskitem.exceptions[ExceptionConsts.EXCEPTION_HEADSET_STATUS]=String.valueOf(ExceptionConsts.EXCEPTION_HEADSET_PLUG_OUT);
							dialog.cancel();
							refreshExceptionViews();
						}
					});
					button_plugged.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							taskitem.exceptions[ExceptionConsts.EXCEPTION_HEADSET_STATUS]=String.valueOf(ExceptionConsts.EXCEPTION_HEADSET_PLUG_IN);
							dialog.cancel();
							refreshExceptionViews();
						}
					});
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		};
		if(headset_status==ExceptionConsts.EXCEPTION_HEADSET_PLUG_IN){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_headset,resources.getString(R.string.activity_taskgui_exception_headset),resources.getString(R.string.activity_taskgui_exception_headset_in),listener_headset_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_HEADSET_STATUS]=String.valueOf(0);
					//refreshExceptionViews();
                    checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}
		if(headset_status==ExceptionConsts.EXCEPTION_HEADSET_PLUG_OUT){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_headset,resources.getString(R.string.activity_taskgui_exception_headset),resources.getString(R.string.activity_taskgui_exception_headset_out),listener_headset_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_HEADSET_STATUS]=String.valueOf(0);
					//refreshExceptionViews();
                    checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}
		final View.OnClickListener listener_on_battery_percentage_clicked=new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int ex_more_than=Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]);
				int ex_less_than=Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]);
				int selection_first=0;
				if(ex_less_than>=0) selection_first=1;
				int selection_second=50;
				if(ex_more_than>=0) selection_second=ex_more_than;
				else if(ex_less_than>=0) selection_second=ex_less_than;
				final BottomDialogForBatteryPercentageWithEnabledSelection dialog=new BottomDialogForBatteryPercentageWithEnabledSelection(TaskGui.this,(ex_more_than>=0||ex_less_than>=0),selection_first,selection_second);
				dialog.show();
				dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
					@Override
					public void onDialogConfirmed(String result) {
						if(result.equals("-1")){
							taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]=String.valueOf(-1);
							taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]=String.valueOf(-1);
						}else{
							//String[]results=result.split(",");
							//int selection=Integer.parseInt(results[0]);
							taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]=dialog.getFirstSelectionValue()==0?String.valueOf(dialog.getSecondSelectionValue()):String.valueOf(-1);
							taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]=dialog.getFirstSelectionValue()==1?String.valueOf(dialog.getSecondSelectionValue()):String.valueOf(-1);
						}
						refreshExceptionViews();
					}
				});
			}
		};
		int battery_more_than_percentage=Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]);
		if(battery_more_than_percentage>=0){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_battery,resources.getString(R.string.activity_taskgui_exceptions_battery_percentage),resources.getString(R.string.dialog_battery_compare_more_than)+battery_more_than_percentage+"%",listener_on_battery_percentage_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]=String.valueOf(-1);
					checkAndPlayTransitionAnimation();
					//refreshExceptionViews();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}

		int battery_less_than_percentage=Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]);
		if(battery_less_than_percentage>=0){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_battery_low,resources.getString(R.string.activity_taskgui_exceptions_battery_percentage),resources.getString(R.string.dialog_battery_compare_less_than)+battery_less_than_percentage+"%",listener_on_battery_percentage_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]=String.valueOf(-1);
					//refreshExceptionViews();
                    checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}

		final View.OnClickListener listener_on_battery_temperature_clicked=new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int ex_higher_than=Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]);
				int ex_lower_than=Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]);
				int selection_first=0;
				if(ex_lower_than>=0) selection_first=1;
				int selection_second=45;
				if(ex_higher_than>=0) selection_second=ex_higher_than;
				else if(ex_lower_than>=0) selection_second=ex_lower_than;
				final BottomDialogForBatteryTemperatureWithEnabledSelection dialog=new BottomDialogForBatteryTemperatureWithEnabledSelection(TaskGui.this,(ex_lower_than>=0||ex_higher_than>=0),selection_first,selection_second);
				dialog.show();
				dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
					@Override
					public void onDialogConfirmed(String result) {
						if(result.equals("-1")){
							taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]=String.valueOf(-1);
							taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]=String.valueOf(-1);
						}else {
							//String[]results=result.split(",");
							//int selection=Integer.parseInt(results[0]);
							taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]=dialog.getFirstSelectionValue()==0?String.valueOf(dialog.getSecondSelectionValue()):String.valueOf(-1);
							taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]=dialog.getFirstSelectionValue()==1?String.valueOf(dialog.getSecondSelectionValue()):String.valueOf(-1);
						}
						refreshExceptionViews();
					}
				});
			}
		};

		int battery_higher_than_temperature=Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]);
		if(battery_higher_than_temperature>=0){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_temperature,resources.getString(R.string.activity_taskgui_exceptions_battery_temperature),resources.getString(R.string.dialog_battery_compare_higher_than)+battery_higher_than_temperature+"℃",listener_on_battery_temperature_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]=String.valueOf(-1);
					//refreshExceptionViews();
                    checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}

		int battery_lower_than_temperature=Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]);
		if(battery_lower_than_temperature>=0){
			final View view= getExceptionItemViewForViewGroup(group,R.drawable.icon_temperature,resources.getString(R.string.activity_taskgui_exceptions_battery_temperature),resources.getString(R.string.dialog_battery_compare_lower_than)+battery_lower_than_temperature+"℃",listener_on_battery_temperature_clicked);
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]=String.valueOf(-1);
					//refreshExceptionViews();
                    checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}

		StringBuilder value=new StringBuilder("");
		value.append(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_MONDAY])==1?getResources().getString(R.string.monday)+" ":"");
		value.append(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_TUESDAY])==1?getResources().getString(R.string.tuesday)+" ":"");
		value.append(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_WEDNESDAY])==1?getResources().getString(R.string.wednesday)+" ":"");
		value.append(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_THURSDAY])==1?getResources().getString(R.string.thursday)+" ":"");
		value.append(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_FRIDAY])==1?getResources().getString(R.string.friday)+" ":"");
		value.append(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_SATURDAY])==1?getResources().getString(R.string.saturday)+' ':"");
		value.append(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_SUNDAY])==1?getResources().getString(R.string.sunday)+" ":"");
		if(!value.toString().equals("")){
			final View view= getExceptionItemViewForViewGroup(group, R.drawable.icon_repeat_weekloop, resources.getString(R.string.activity_taskgui_exceptions_day_of_week), value.toString(), new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					View dialogview=LayoutInflater.from(TaskGui.this).inflate(R.layout.layout_dialog_weekloop,null);
					final CheckBox cb_mon=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_mon);
					final CheckBox cb_tue=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_tue);
					final CheckBox cb_wed=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_wed);
					final CheckBox cb_thu=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_thu);
					final CheckBox cb_fri=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_fri);
					final CheckBox cb_sat=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_sat);
					final CheckBox cb_sun=dialogview.findViewById(R.id.layout_dialog_weekloop_cb_sun);

					try{
						cb_mon.setChecked(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_MONDAY])==1);
						cb_tue.setChecked(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_TUESDAY])==1);
						cb_wed.setChecked(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_WEDNESDAY])==1);
						cb_thu.setChecked(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_THURSDAY])==1);
						cb_fri.setChecked(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_FRIDAY])==1);
						cb_sat.setChecked(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_SATURDAY])==1);
						cb_sun.setChecked(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_SUNDAY])==1);
					}catch (NumberFormatException ne){
						ne.printStackTrace();
					}

					final AlertDialog dialog=new AlertDialog.Builder(TaskGui.this)
							.setTitle(getResources().getString(R.string.dialog_exceptions_day_of_week_title))
							.setView(dialogview)
							.setPositiveButton(getResources().getString(R.string.dialog_button_positive), null)
							.setNegativeButton(getResources().getString(R.string.dialog_button_negative), null).create();
					dialog.show();
					dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							if(!cb_mon.isChecked()||!cb_tue.isChecked()||!cb_wed.isChecked()
									||!cb_thu.isChecked()||!cb_fri.isChecked()||!cb_sat.isChecked()
									||!cb_sun.isChecked()){
								taskitem.exceptions[ExceptionConsts.EXCEPTION_MONDAY]=cb_mon.isChecked()?String.valueOf(1):String.valueOf(0);
								taskitem.exceptions[ExceptionConsts.EXCEPTION_TUESDAY]=cb_tue.isChecked()?String.valueOf(1):String.valueOf(0);
								taskitem.exceptions[ExceptionConsts.EXCEPTION_WEDNESDAY]=cb_wed.isChecked()?String.valueOf(1):String.valueOf(0);
								taskitem.exceptions[ExceptionConsts.EXCEPTION_THURSDAY]=cb_thu.isChecked()?String.valueOf(1):String.valueOf(0);
								taskitem.exceptions[ExceptionConsts.EXCEPTION_FRIDAY]=cb_fri.isChecked()?String.valueOf(1):String.valueOf(0);
								taskitem.exceptions[ExceptionConsts.EXCEPTION_SATURDAY]=cb_sat.isChecked()?String.valueOf(1):String.valueOf(0);
								taskitem.exceptions[ExceptionConsts.EXCEPTION_SUNDAY]=cb_sun.isChecked()?String.valueOf(1):String.valueOf(0);
								dialog.cancel();
								refreshExceptionViews();
							}
							else{
								Snackbar.make(view,getResources().getString(R.string.dialog_exceptions_day_of_week_all_selected),Snackbar.LENGTH_SHORT).show();
								return;
							}

						}
					});
					dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							dialog.cancel();
						}
					});
				}
			});
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_MONDAY]=String.valueOf(0);
					taskitem.exceptions[ExceptionConsts.EXCEPTION_TUESDAY]=String.valueOf(0);
					taskitem.exceptions[ExceptionConsts.EXCEPTION_WEDNESDAY]=String.valueOf(0);
					taskitem.exceptions[ExceptionConsts.EXCEPTION_THURSDAY]=String.valueOf(0);
					taskitem.exceptions[ExceptionConsts.EXCEPTION_FRIDAY]=String.valueOf(0);
					taskitem.exceptions[ExceptionConsts.EXCEPTION_SATURDAY]=String.valueOf(0);
					taskitem.exceptions[ExceptionConsts.EXCEPTION_SUNDAY]=String.valueOf(0);
					//refreshExceptionViews();
					checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}

		int startTime=Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_START_TIME]);
		int endTime=Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_END_TIME]);
		if(startTime>=0&&endTime>=0){
			String display= ValueUtils.format(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_START_TIME])/60)+":"
							+ ValueUtils.format(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_START_TIME])%60)
							+"~"+ ValueUtils.format(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_END_TIME])/60)+":"
							+ ValueUtils.format(Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_END_TIME])%60);
			final View view= getExceptionItemViewForViewGroup(group, R.drawable.icon_repeat_percertaintime, resources.getString(R.string.activity_taskgui_exceptions_period), display, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					BottomDialogForPeriod dialog=new BottomDialogForPeriod(TaskGui.this);
					boolean isEnabled=false;
					int startHour=0;
					int startMin=0;
					int endHour=0;
					int endMin=0;
					try{
						isEnabled=Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_START_TIME])!=-1&&Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_END_TIME])!=-1;
						startHour=isEnabled?Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_START_TIME])/60:18;
						startMin=isEnabled?Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_START_TIME])%60:0;
						endHour=isEnabled?Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_END_TIME])/60:8;
						endMin=isEnabled?Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_END_TIME])%60:0;
					}catch (NumberFormatException ne){
						ne.printStackTrace();
					}
					dialog.setVariables(isEnabled,startHour,startMin,endHour,endMin);
					dialog.setTitle(getResources().getString(R.string.dialog_exceptions_period_title));
					dialog.show();
					dialog.setOnDialogConfirmedListener(new BottomDialogForPeriod.OnDialogConfirmedListener() {
						@Override
						public void onConfirmed(boolean isEnabled, int start_hour, int start_minute, int end_hour, int end_minute) {
							taskitem.exceptions[ExceptionConsts.EXCEPTION_START_TIME]=isEnabled?String.valueOf(start_hour*60+start_minute):String.valueOf(-1);
							taskitem.exceptions[ExceptionConsts.EXCEPTION_END_TIME]=isEnabled?String.valueOf(end_hour*60+end_minute):String.valueOf(-1);
							refreshExceptionViews();
						}
					});
				}
			});
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_START_TIME]=String.valueOf(-1);
					taskitem.exceptions[ExceptionConsts.EXCEPTION_END_TIME]=String.valueOf(-1);
					//refreshExceptionViews();
                    checkAndPlayTransitionAnimation();
                    group.removeView(view);
				}
			});
			group.addView(view);
		}

		int wifi_status_head=Integer.parseInt(taskitem.exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS].split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL)[0]);
		if(wifi_status_head!=-1){
			final View view=getExceptionItemViewForViewGroup(group, R.drawable.icon_wifi_connected, getResources().getString(R.string.exception_wifi_status)
					, ContentAdapter.ExceptionContentAdapter.getExceptionValueOfWifiStatus(this, taskitem.exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS])
					, new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							final int []values=ValueUtils.string2intArray(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL,taskitem.exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS]);
							BottomDialogForWifiStatus dialog=new BottomDialogForWifiStatus(TaskGui.this,values.length>0?values[0]:-1);
							dialog.setOnDialogConfirmedListener(new DialogConfirmedCallBack() {
								@Override
								public void onDialogConfirmed(String result) {
									int value=Integer.parseInt(result);
									switch (value){
										default:break;
										case -1:{
											taskitem.exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS]=String.valueOf(-1);
											refreshExceptionViews();
										}
										break;
										case ExceptionConsts.EXCEPTION_WIFI_VALUE_DISCONNECTED:{
											taskitem.exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS]=String.valueOf(ExceptionConsts.EXCEPTION_WIFI_VALUE_DISCONNECTED);
											refreshExceptionViews();
										}
										break;
										case ExceptionConsts.EXCEPTION_WIFI_VALUE_CONNECTED_TO_RANDOM_SSID:{
											taskitem.exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS]=String.valueOf(ExceptionConsts.EXCEPTION_WIFI_VALUE_CONNECTED_TO_RANDOM_SSID);
											refreshExceptionViews();
											if(Global.NetworkReceiver.wifiList2.size()==0){
												Snackbar.make(findViewById(android.R.id.content),getResources().getString(R.string.activity_trigger_wifi_open_att),Snackbar.LENGTH_SHORT).show();
												return;
											}
											DialogForWifiInfoSelection dialog1=new DialogForWifiInfoSelection(TaskGui.this,values);
											dialog1.setOnDialogConfirmedListener(new DialogForWifiInfoSelection.DialogConfirmedListener() {
												@Override
												public void onDialogConfirmed(int[] ids) {
													if(ids==null||ids.length==0) {
														taskitem.exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS]=String.valueOf(ExceptionConsts.EXCEPTION_WIFI_VALUE_CONNECTED_TO_RANDOM_SSID);
													}else taskitem.exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS]=ValueUtils.intArray2String(PublicConsts.SEPARATOR_SECOND_LEVEL,ids);
													refreshExceptionViews();
												}
											});
											dialog1.show();
										}
										break;
									}
								}
							});
							dialog.show();
						}
					});
			view.findViewById(R.id.layout_taskgui_exception_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskitem.exceptions[ExceptionConsts.EXCEPTION_WIFI_STATUS]=String.valueOf(-1);
					checkAndPlayTransitionAnimation();
					group.removeView(view);
				}
			});
			group.addView(view);
		}

		((TextView)findViewById(R.id.layout_taskgui_area_exception_att)).setText(Integer.parseInt(taskitem.addition_exception_connector)==0?
				getResources().getString(R.string.activity_taskgui_att_exception_and)
				:getResources().getString(R.string.activity_taskgui_att_exception_or));
	}

	/**
	 * 将当前实例中的TaskItem插入到数据库中
	 * @param id 任务的ID，为空时则在数据库新建一行，不为空时则更新指定ID的行
	 * @return 插入或者更新数据的结果,插入失败时返回-1，更新失败时返回0
	 */
	public long saveTaskItem2DB(@Nullable Integer id){
		try{
			return MySQLiteOpenHelper.insertOrUpdateRow(this,this.taskitem,id);
		}catch (Exception e){
			e.printStackTrace();
			Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
		}
		return -1;
	}

	/**
	 * 将dp值转换为px
	 */
	public int dp2px(int dp){
		return DisplayDensity.dip2px(this, dp);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(data==null)return;
		switch(requestCode){
			default:break;
			case REQUEST_CODE_TRIGGERS:{
				if(resultCode==RESULT_OK){
					taskitem=(TaskItem) data.getSerializableExtra(EXTRA_SERIALIZED_TASKITEM);
					refreshTriggerDisplayValue();
					if(!isTaskNameEdited)taskitem.name=(String)ContentAdapter.TriggerContentAdapter.getContentForTriggerType(this,ContentAdapter.TriggerContentAdapter.CONTENT_TYPE_DISPLAY_STRING_TITLE,taskitem)+" "+
							(String)ContentAdapter.TriggerContentAdapter.getContentForTriggerType(this, ContentAdapter.TriggerContentAdapter.CONTENT_TYPE_DISPLAY_STRING_CONTENT,taskitem);
					((TextView)findViewById(R.id.layout_taskgui_area_name_text)).setText(taskitem.name);
					if(taskitem.trigger_type== TriggerTypeConsts.TRIGGER_TYPE_SINGLE) setAutoCloseAreaEnabled(false);
					else {
						setAutoCloseAreaEnabled(!((CheckBox)findViewById(R.id.layout_taskgui_area_additional_autodelete_cb)).isChecked());
					}
				}
			}
			break;
			case REQUEST_CODE_EXCEPTIONS:{
				if(resultCode==RESULT_OK) {
					taskitem=(TaskItem) data.getSerializableExtra(EXTRA_SERIALIZED_TASKITEM);
					refreshExceptionViews();
				}
			}
			break;
			case REQUEST_CODE_ACTIONS:{
				if(resultCode==RESULT_OK){
					taskitem=(TaskItem)data.getSerializableExtra(EXTRA_SERIALIZED_TASKITEM);
					refreshActionStatus();
				}
			}
			break;
			case REQUEST_CODE_ACTION_RINGTONE:{
				if(resultCode==RESULT_OK){
					taskitem=(TaskItem)data.getSerializableExtra(EXTRA_SERIALIZED_TASKITEM);
					refreshActionStatus();
				}
			}
			break;
			case REQUEST_CODE_SET_WALLPAPER:{
				if(resultCode==RESULT_OK){
					Uri uri=data.getData();
					if(uri==null) return;
					taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_SET_WALL_PAPER_LOCALE]=String.valueOf(0);
					taskitem.uri_wallpaper_desktop= ValueUtils.getRealPathFromUri(this,uri);//uri.toString();
					refreshActionStatus();
				}
			}
			break;
			case REQUEST_CODE_SMS:{
				if(resultCode==RESULT_OK){
					taskitem=(TaskItem)data.getSerializableExtra(EXTRA_SERIALIZED_TASKITEM);
					refreshActionStatus();
				}
			}
			break;
			case REQUEST_CODE_PLAY_FROM_SYSTEM:{
				if(resultCode==RESULT_OK){
					taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_PLAY_AUDIO]=String.valueOf(ActionConsts.ActionValueConsts.RING_TYPE_FROM_SYSTEM);
					Uri uri=data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
					if(uri!=null)taskitem.uri_play=uri.toString();
					refreshActionStatus();
				}
			}
			break;
			case REQUEST_CODE_PLAY_FROM_MEDIA:{
				if(resultCode==RESULT_OK){
					taskitem.actions[ActionConsts.ActionFirstLevelLocaleConsts.ACTION_PLAY_AUDIO]=String.valueOf(ActionConsts.ActionValueConsts.RING_TYPE_FROM_MEDIA);
					Uri uri=data.getData();
					if(uri!=null)taskitem.uri_play=uri.toString();
					refreshActionStatus();
				}
			}
			break;
		}
	}

	private void clearExceptionsOfTimeType(){
        taskitem.exceptions[ExceptionConsts.EXCEPTION_START_TIME]=String.valueOf(-1);
        taskitem.exceptions[ExceptionConsts.EXCEPTION_END_TIME]=String.valueOf(-1);
        taskitem.exceptions[ExceptionConsts.EXCEPTION_MONDAY]=String.valueOf(0);
        taskitem.exceptions[ExceptionConsts.EXCEPTION_TUESDAY]=String.valueOf(0);
        taskitem.exceptions[ExceptionConsts.EXCEPTION_WEDNESDAY]=String.valueOf(0);
        taskitem.exceptions[ExceptionConsts.EXCEPTION_THURSDAY]=String.valueOf(0);
        taskitem.exceptions[ExceptionConsts.EXCEPTION_FRIDAY]=String.valueOf(0);
        taskitem.exceptions[ExceptionConsts.EXCEPTION_SATURDAY]=String.valueOf(0);
        taskitem.exceptions[ExceptionConsts.EXCEPTION_SUNDAY]=String.valueOf(0);
        refreshExceptionViews();
    }

    private void clearExceptionsOfBatteryPercentage(){
	    taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_MORE_THAN_PERCENTAGE]=String.valueOf(-1);
	    taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LESS_THAN_PERCENTAGE]=String.valueOf(-1);
	    refreshExceptionViews();
    }

    private void clearExceptionsOfBatteryTemperature(){
	    taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_HIGHER_THAN_TEMPERATURE]=String.valueOf(-1);
	    taskitem.exceptions[ExceptionConsts.EXCEPTION_BATTERY_LOWER_THAN_TEMPERATURE]=String.valueOf(-1);
	    refreshExceptionViews();
    }

	/**
	 * 为ViewGroup获取一个ExceptionView实例
	 * @param group 要添加view实例的group
	 * @param icon_res 图标资源值
	 * @param title 标题，不可为空
	 * @param description 描述，可为空
	 * @return  添加的view 实例
	 */
    public View getExceptionItemViewForViewGroup(ViewGroup group, int icon_res, @NonNull String title , @Nullable String description,View.OnClickListener listener){
		View view=LayoutInflater.from(this).inflate(R.layout.layout_taskgui_item_exception,group,false);
		((ImageView)view.findViewById(R.id.layout_taskgui_area_exception_icon)).setImageResource(icon_res);
		((TextView)view.findViewById(R.id.layout_taskgui_area_exception_att)).setText(title);
		TextView tv_description=view.findViewById(R.id.layout_taskgui_area_exception_value);
		if(description==null) {
			tv_description.setVisibility(View.GONE);
		}else{
			tv_description.setVisibility(View.VISIBLE);
			tv_description.setText(description);
		}
		view.findViewById(R.id.layout_exception_item).setOnClickListener(listener);
		return view;
	}
	/**
	 * 为ViewGroup获取一个ActionView实例
	 * @param group 要添加view实例的group
	 * @param icon_res 图标资源值
	 * @param title 标题
	 * @param value 描述
	 * @return  添加的view 实例
	 */
	public View getActionItemViewForViewGroup(ViewGroup group, int icon_res, @NonNull String title, @NonNull String value){
    	View view=LayoutInflater.from(this).inflate(R.layout.layout_taskgui_item_action,group,false);
    	view.setClickable(true);
		((ImageView)view.findViewById(R.id.taskgui_action_item_icon)).setImageResource(icon_res);
		((TextView)view.findViewById(R.id.taskgui_action_item_action)).setText(title);
		((TextView)view.findViewById(R.id.taskgui_action_item_status)).setText(value);
    	return view;
	}

}