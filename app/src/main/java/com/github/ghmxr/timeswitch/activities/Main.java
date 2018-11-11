package com.github.ghmxr.timeswitch.activities;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.adapters.MainListAdapter;
import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.SQLConsts;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.utils.MySQLiteOpenHelper;
import com.github.ghmxr.timeswitch.utils.ProcessTaskItem;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class Main extends BaseActivity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener{
	ListView listview;
    FloatingActionButton fab;
    //boolean isFabVisible=true;
	public MainListAdapter adapter;
	SwipeRefreshLayout swrlayout;
	//public List<TaskItem> list=new ArrayList<>();
	//private List<TaskItem> list_private;
	Thread  thread_delete;
	boolean isMultiSelectMode=false;
	Menu menu;
	private boolean ifRefresh=true;
	//private boolean ifRefresh_has_certain_time=false;
    public static final int MESSAGE_START_SERVICE       =  0x00000;
	public static final int MESSAGE_GETLIST_COMPLETE   =   0x00001;
	public static final int MESSAGE_BATTERY_CHANGED    =   0x00002;
	public static final int MESSAGE_SHOW_INDICATOR      =   0x00003;
	public static final int MESSAGE_HIDE_INDICATOR      =   0x00004;
	public static final int MESSAGE_REQUEST_UPDATE_LIST =0x00005;

	private static final int MESSAGE_DELETE_SELECTED_ITEMS_COMPLETE  =0x00010;

	private static final int REQUEST_CODE_ACTIVITY_ADD=0x00100;
	private static final int REQUEST_CODE_ACTIVITY_EDIT=0x00101;
	private static final int REQUEST_CODE_ACTIVITY_SETTINGS=0x00102;
	private static final int REQUEST_CODE_ACTIVITY_PROFILE=0x00103;

	private BroadcastReceiver batteryReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent==null) return;
            final String ACTION=intent.getAction();
            if(ACTION==null) return;
            if(ACTION.equals(Intent.ACTION_BATTERY_CHANGED)){
                Message msg=new Message();
                msg.what=Main.MESSAGE_BATTERY_CHANGED;
                msg.obj=intent;
                sendMessage(msg);
            }
        }
    };

	private boolean isBatteryReceiverRegistered=false;
    private long first_click_delete=0;
   // public static LinkedList<Main> queue=new LinkedList<>();
  /*  public static Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            if(queue.size()>0) queue.getLast().processMessage(msg);
        }
    };  */

    //public static MyHandler mhandler;


    public void onCreate(Bundle mybundle){
		super.onCreate(mybundle);
		//if(!queue.contains(this)) queue.add(this);
		//mhandler=new MyHandler();
		setContentView(R.layout.layout_main);

        SharedPreferences settings=getSharedPreferences(PublicConsts.PREFERENCES_NAME,Activity.MODE_PRIVATE);

        Toolbar toolbar =findViewById(R.id.toolbar);
        listview=findViewById(R.id.main_listview);
        swrlayout=findViewById(R.id.main_swrlayout);


        findViewById(R.id.main_indicator).setVisibility(settings.getBoolean(PublicConsts.PREFERENCES_MAINPAGE_INDICATOR,PublicConsts.PREFERENCES_MAINPAGE_INDICATOR_DEFAULT)?View.VISIBLE:View.GONE);
        listview.setDivider(null);
		setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        //.setAction("Action", null).show();

                if(isMultiSelectMode) closeMultiSelectMode();
                //Main.this.startActivity(new Intent(Main.this,AddTask.class));
                startActivityForResult(new Intent(Main.this,AddTask.class),REQUEST_CODE_ACTIVITY_ADD);
            }
        });

        this.swrlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               startService2Refresh();
            }
        });
        startRefreshingIndicator();
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                if(ifRefresh){
                    myHandler.postDelayed(this,500);
                    if(adapter!=null) adapter.refreshAllCertainTimeTaskItems();
                }

            }
        });

        checkIfHasServiceInstanceAndRun();
	}

	@Override
    public void onResume(){
        super.onResume();
        //startService2Refresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE_ACTIVITY_ADD){
            if(resultCode==AddTask.ACTIVITY_ADD_RESULT_SUCCESS){
                startService2Refresh();
                //sendEmptyMessage(MESSAGE_GETLIST_COMPLETE);
            }
        }else if(requestCode==REQUEST_CODE_ACTIVITY_EDIT){
            if(resultCode==EditTask.ACTIVITY_EDIT_RESULT_SUCCESS){
                startService2Refresh();
                //sendEmptyMessage(MESSAGE_GETLIST_COMPLETE);
            }
        }else if(requestCode==REQUEST_CODE_ACTIVITY_PROFILE){
            if(resultCode==Profile.RESULT_PROFILE_CHANGED){
                adapter=null;
                listview.setAdapter(null);
                startService2Refresh();
            }
        }else if(requestCode==REQUEST_CODE_ACTIVITY_SETTINGS){
            if(resultCode==Settings.RESULT_CHAGED_INDICATOR_STATE){
                SharedPreferences settings=getSharedPreferences(PublicConsts.PREFERENCES_NAME,Activity.MODE_PRIVATE);
                findViewById(R.id.main_indicator).setVisibility(settings.getBoolean(PublicConsts.PREFERENCES_MAINPAGE_INDICATOR,PublicConsts.PREFERENCES_MAINPAGE_INDICATOR_DEFAULT)?View.VISIBLE:View.GONE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //if(isMultiSelectMode) closeMultiSelectMode();
    }

    private void checkIfHasServiceInstanceAndRun(){
        if(TimeSwitchService.service_queue.size()==0){
            startService2Refresh();
        }else{
            sendEmptyMessage(MESSAGE_GETLIST_COMPLETE);
        }
    }

    public void startService2Refresh(){
        this.swrlayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        this.swrlayout.setRefreshing(true);
        listview.setOnItemLongClickListener(null);
        this.startService(new Intent(this, TimeSwitchService.class));
    }


    /**
     * 主界面任务列表 listview 的Item点击回调
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(!isMultiSelectMode) {
            editTask(i);
        }else{
            adapter.onMultiSelectModeItemClicked(i);
            boolean ifHasSelectedItem=false;
            boolean isSelected [] =adapter.getIsSelected();
            for(int j=0;j<isSelected.length;j++){
                if(isSelected[j]) ifHasSelectedItem=true;
            }
            this.menu.getItem(0).setEnabled(ifHasSelectedItem);
        }
    }

    private void editTask(int position){
        //int taskkey = list.get(i).id;
        Intent intent = new Intent();
        //intent.putExtra(EditTask.TAG_EDITTASK_KEY, taskkey);
        intent.putExtra(EditTask.TAG_SELECTED_ITEM_POSITION,position);
        intent.setClass(this, EditTask.class);
        startActivityForResult(intent,REQUEST_CODE_ACTIVITY_EDIT);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        openMultiSelectMode(i);
        return true;
    }


    public void processMessage(Message msg) {
        switch (msg.what){
            case MESSAGE_REQUEST_UPDATE_LIST:{
                if(adapter!=null) {
                    adapter.onDataSetChanged(TimeSwitchService.list);
                    refreshAttVisibility();
                }
            }
            break;
            case MESSAGE_GETLIST_COMPLETE:{
                //this.list=TimeSwitchService.list;
                if(adapter==null){
                    adapter=new MainListAdapter(this,TimeSwitchService.list);
                }
                else{
                    adapter.onDataSetChanged(TimeSwitchService.list);
                }
                this.listview.setAdapter(adapter);
                this.swrlayout.setRefreshing(false);
                adapter.setOnSwitchChangedListener(new MainListAdapter.SwitchChangedListener() {
                    @Override
                    public void onCheckedChanged(final int position,boolean b) {
                        if(TimeSwitchService.list==null||position>=TimeSwitchService.list.size()||position<0) return;
                        if(b&&TimeSwitchService.list.get(position).trigger_type == PublicConsts.TRIGGER_TYPE_SINGLE&&(TimeSwitchService.list.get(position).time<System.currentTimeMillis())){
                            Snackbar.make(fab,getResources().getString(R.string.activity_main_toast_task_invalid),Snackbar.LENGTH_SHORT)
                                    .setAction(getResources().getString(R.string.activity_main_toast_task_invalid_action), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            editTask(position);
                                        }
                                    }).show();
                            TimeSwitchService.list.get(position).isenabled=false;
                            adapter.notifyDataSetChanged();
                            return;
                        }
                        try{
                            ProcessTaskItem.setTaskEnabled(TimeSwitchService.service_queue.getLast(),TimeSwitchService.list.get(position).id,b);
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                });
                listview.setOnItemClickListener(this);
                listview.setOnItemLongClickListener(this);
                //setListViewScrollListener();
                refreshAttVisibility();
            }
            break;
            case MESSAGE_DELETE_SELECTED_ITEMS_COMPLETE:startService2Refresh();break;
            case MESSAGE_BATTERY_CHANGED:{
                Intent intent=(Intent)msg.obj;
                boolean isCharging=false;
                int level=intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
                int temperature=intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-1);
                float voltage=(float)intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,-1000)/1000;
                int status=intent.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
                if(status==BatteryManager.BATTERY_STATUS_CHARGING) isCharging=true;
                refreshIndicatorOfBatteryInfo(level,temperature,voltage,isCharging);
            }
            break;
            case MESSAGE_HIDE_INDICATOR:{
                findViewById(R.id.main_indicator).setVisibility(View.GONE);
            }
            break;
            case MESSAGE_SHOW_INDICATOR:{
                findViewById(R.id.main_indicator).setVisibility(View.VISIBLE);
            }
            break;
        }
    }

    private void setFabVisibility(boolean isVisible){
        if(fab==null) return;
        fab.setAnimation(isVisible?AnimationUtils.loadAnimation(Main.this,R.anim.anim_fab_enter):AnimationUtils.loadAnimation(Main.this,R.anim.anim_fab_exit));
        fab.setVisibility(isVisible?View.VISIBLE:View.GONE);
        //isFabVisible=isVisible;
    }

    /*private void setListViewScrollListener(){
        if(listview==null) return;
        listview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Log.d("listview","scrolled");
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem+visibleItemCount==totalItemCount){
                    //Log.d("listview","来到底部");
                    View lastItemView=listview.getChildAt(listview.getChildCount()-1);
                    if(lastItemView==null) return;
                    if(listview.getBottom()==lastItemView.getBottom()){
                       // Log.d("listview","滑动到底部");
                        if(isFabVisible) setFabVisibility(false);
                        return;
                    }
                }
                if(!isFabVisible) setFabVisibility(true);
            }
        });
    }  */

    private void refreshAttVisibility(){
        RelativeLayout area=findViewById(R.id.main_no_task_att);
        if(area==null) return;
        if(TimeSwitchService.list==null) return;
        area.setVisibility(TimeSwitchService.list.size()>0?View.GONE:View.VISIBLE);
    }

    /*public static void sendMessage(Message msg){
        if(mhandler!=null) mhandler.sendMessage(msg);
    }

    public static void sendEmptyMessage(int what){
        if(mhandler!=null) mhandler.sendEmptyMessage(what);
    }   */

    private void openMultiSelectMode(int longclickposition){
        this.isMultiSelectMode=true;
        this.swrlayout.setEnabled(false);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        adapter.openMultiSelecteMode(longclickposition);
        listview.setOnItemLongClickListener(null);
        this.menu.getItem(0).setVisible(true);
        this.menu.getItem(0).setEnabled(true);
        this.menu.getItem(1).setVisible(true);
        this.menu.getItem(2).setVisible(true);
        this.menu.getItem(3).setVisible(false);
        this.menu.getItem(4).setVisible(false);
        listview.setOnScrollListener(null);
        setFabVisibility(false);
    }

    private void closeMultiSelectMode(){
        this.isMultiSelectMode=false;
        this.swrlayout.setEnabled(true);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        adapter.closeMultiSelectMode();
        listview.setOnItemLongClickListener(this);
        this.menu.getItem(0).setVisible(false);
        this.menu.getItem(1).setVisible(false);
        this.menu.getItem(2).setVisible(false);
        this.menu.getItem(3).setVisible(true);
        this.menu.getItem(4).setVisible(true);
        //setListViewScrollListener();
        setFabVisibility(true);
    }

    private void startRefreshingIndicator(){
        this.ifRefresh=true;
        if(!isBatteryReceiverRegistered){
            registerReceiver(batteryReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            isBatteryReceiverRegistered=true;
        }

       myHandler.post(new Runnable() {
           @Override
           public void run() {
               if(Build.VERSION.SDK_INT>=21&&ifRefresh){
                   myHandler.postDelayed(this,1000);
                   BatteryManager batteryManager=(BatteryManager) getSystemService(Activity.BATTERY_SERVICE);
                   try{
                       refreshIndicatorOfBatteryCurrent(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW));
                   }catch (java.lang.NullPointerException ne){
                        refreshIndicatorOfBatteryCurrent(0);
                   }catch (Exception e){
                       e.printStackTrace();
                   }
               }
           }
       });

        myHandler.post(new Runnable() {
            @Override
            public void run() {
                if (ifRefresh){
                    myHandler.postDelayed(this,500);
                     Calendar calendar=Calendar.getInstance();
                     calendar.setTimeInMillis(System.currentTimeMillis());
                     int month=calendar.get(Calendar.MONTH)+1;
                    // BatteryManager batteryManager=(BatteryManager) getSystemService(Activity.BATTERY_SERVICE);
                     TextView tv_time=findViewById(R.id.main_indicator_time_value);
                     tv_time.setText(ValueUtils.format(calendar.get(Calendar.YEAR))
                             +"/"+ ValueUtils.format(month)
                             +"/"+ ValueUtils.format(calendar.get(Calendar.DAY_OF_MONTH))
                             +"("+ValueUtils.getDayOfWeek(calendar.getTimeInMillis())
                             +")/" +ValueUtils.format(calendar.get(Calendar.HOUR_OF_DAY))
                             +":"+ValueUtils.format(calendar.get(Calendar.MINUTE))
                            +":"+ValueUtils.format(calendar.get(Calendar.SECOND)));

                    //Log.e("mhandler","activated!!!");
                }
            }
        });
    }

    private void refreshIndicatorOfBatteryInfo(int level,int temperature,float voltage,boolean isCharging){
        ImageView img_level=findViewById(R.id.main_indicator_battery_percentage_img);
        TextView tv_percentage=findViewById(R.id.main_indicator_battery_percentage_value);
        TextView tv_temperature=findViewById(R.id.main_indicator_battery_temperature_value);
        TextView tv_voltage=findViewById(R.id.main_indicator_battery_voltage_value);
        TextView tv_current=findViewById(R.id.main_indicator_battery_current_value);
        if(isCharging) {
            tv_current.setTextColor(getResources().getColor(R.color.color_current_charge));
            img_level.setImageResource(R.drawable.icon_battery_charging);
        }
        else {
            tv_current.setTextColor(getResources().getColor(R.color.color_current_discharge));

            if(level>=90) img_level.setImageResource(R.drawable.icon_battery_full);
            else if(level>70) img_level.setImageResource(R.drawable.icon_battery_high);
            else if(level>45) img_level.setImageResource(R.drawable.icon_battery_medium);
            else if(level>30) img_level.setImageResource(R.drawable.icon_battery_low);
            else if(level>=0)img_level.setImageResource(R.drawable.icon_battery_low_alarm);
        }

        if(level>0) tv_percentage.setText(level+"%");
        if(temperature!=-1)tv_temperature.setText((double)temperature/10+"℃");
        if(voltage!=-1)tv_voltage.setText(voltage+"V");
    }

    private void refreshIndicatorOfBatteryCurrent(int current){
        TextView tv_current=findViewById(R.id.main_indicator_battery_current_value);
        if(current<0) current=0-current;
        tv_current.setText(current/1000+"mA");
    }

    @Override
    public void finish(){
        super.finish();
        this.ifRefresh=false;
        if(isBatteryReceiverRegistered) {
            unregisterReceiver(batteryReceiver);
            isBatteryReceiverRegistered=false;
        }
        //adapter=null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(isMultiSelectMode) closeMultiSelectMode();
            else finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        this.menu=menu;
        setIconEnable(menu,true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId()==android.R.id.home){
            if(isMultiSelectMode){
                closeMultiSelectMode();
            }
        }
        if(item.getItemId()==R.id.action_selectall){
            adapter.selectAll();
            this.menu.getItem(0).setEnabled(true);
        }
        if(item.getItemId()==R.id.action_deselectall){
            adapter.deselectAll();
            this.menu.getItem(0).setEnabled(false);
        }
        if(item.getItemId()==R.id.action_delete_selected){
            long clickedTime=System.currentTimeMillis();
            if(clickedTime-first_click_delete>1000){
                first_click_delete=clickedTime;
                Snackbar.make(fab,getResources().getString(R.string.dialog_profile_delete_confirm),Snackbar.LENGTH_SHORT).show();
                return false;
            }
            closeMultiSelectMode();
            swrlayout.setRefreshing(true);
            this.thread_delete=new Thread(new Runnable() {
                @Override
                public void run() {
                    SQLiteDatabase database=MySQLiteOpenHelper.getInstance(Main.this).getWritableDatabase();
                    boolean isSelected[]=Main.this.adapter.getIsSelected();
                    for(int i=0;i<isSelected.length;i++){
                        if(isSelected[i]){
                           int key=TimeSwitchService.list.get(i).id;
                           TimeSwitchService.list.get(i).cancelTrigger();
                           //list.remove(i);
                           database.delete(SQLConsts.getCurrentTableName(Main.this),SQLConsts.SQL_TASK_COLUMN_ID +"="+key,null);
                        }
                    }
                    sendEmptyMessage(MESSAGE_DELETE_SELECTED_ITEMS_COMPLETE);
                }
            });
            this.thread_delete.start();
        }
        if(item.getItemId()==R.id.action_settings){
            startActivityForResult(new Intent(this,Settings.class),REQUEST_CODE_ACTIVITY_SETTINGS);
        }
        if(item.getItemId()==R.id.action_profile){
            //startActivity(new Intent(this,Profile.class));
            startActivityForResult(new Intent(this,Profile.class),REQUEST_CODE_ACTIVITY_PROFILE);

        }
        return super.onOptionsItemSelected(item);
    }

   /* private  static class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //Log.i("MyHandler","Received a message and the msg.what is"+msg.what);
            try{
                if(queue.size()>0) queue.getLast().processMessage(msg);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }   */
	
}
