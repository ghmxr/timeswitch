package com.github.ghmxr.timeswitch.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.PublicConsts;
import com.github.ghmxr.timeswitch.data.SQLConsts;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.utils.MySQLiteOpenHelper;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class Profile extends BaseActivity {

    List<TableItem> tablelist=new ArrayList<>();
    ListView listview;
    TableListAdapter adapter;
    private boolean isMultiSelectMode=false;
    private Menu menu;
    private AlertDialog waitdiag;

    Thread thread;
    RefreshTaskTablesFromDatabase runnable;

    List<File> jsonFiles=new ArrayList<>();
    AlertDialog file_dialog;
    Thread thread_getalljsonfiles;
    GetAllJSONFiles getAllJSONFiles;
    long delete_firstclicked=0;

    //public static final int RESULT_NOTHING_CHANGED=0x000000;
    public static final int RESULT_PROFILE_CHANGED=0x000001;

    private static final int MESSAGE_REQUIRE_UPDATE_LIST=0x30000;
    private static final int MESSAGE_EXPORT_TABLES_COMPLETE=0x30001;
    private static final int MESSAGE_IMPORT_TABLES_COMPLETE=0x30002;
    private static final int MESSAGE_GET_JSON_FILES_COMPLETE=0x30003;
    public static final int MESSAGE_REFRESH_TABLES=0x30010;

    AdapterView.OnItemClickListener onItemClicked_Normal=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(i!=getSelectedPosition()){
                SharedPreferences settings =Profile.this.getSharedPreferences(PublicConsts.PREFERENCES_NAME,Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor=settings.edit();
                editor.putString(PublicConsts.PREFERENCES_CURRENT_TABLE_NAME,tablelist.get(i).table_name);
                editor.apply();
                MySQLiteOpenHelper.clearCurrentInstance();
                showWaitDialog();
                //isChanged=true;
                setResult(RESULT_PROFILE_CHANGED);
                if(TimeSwitchService.service_queue.size()>0) TimeSwitchService.service_queue.getLast().refreshTaskItems();
                else startService(new Intent(Profile.this, TimeSwitchService.class));
            }
        }
    };

    AdapterView.OnItemLongClickListener onItemLongClicked_Normal=new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(tablelist.size()<=1) return false;
            openMultiSelectMode(i);
            listview.setOnItemLongClickListener(null);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    adapter.onMultiSelectModeItemClicked(i);
                }
            });
            return true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_profile);
        Toolbar toolbar=findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        try{getSupportActionBar().setDisplayHomeAsUpEnabled(true);}catch (Exception e){e.printStackTrace();}
        listview=findViewById(R.id.layout_profile_list);
        listview.setDivider(null);
        refreshTables();
    }

    private void refreshTables(){
        if(runnable!=null){
            runnable.isInterrupted=true;
            runnable=null;
        }
        runnable=new RefreshTaskTablesFromDatabase();
        thread=new Thread(runnable);
        thread.start();
    }

    public void processMessage(Message msg){
        switch(msg.what){
            default:break;
            case MESSAGE_REQUIRE_UPDATE_LIST:{
                if(adapter==null){
                    adapter=new TableListAdapter();
                    listview.setAdapter(adapter);
                }else{
                    adapter.onDataSetChanged();
                }

                listview.setOnItemClickListener(onItemClicked_Normal);

                listview.setOnItemLongClickListener(onItemLongClicked_Normal);

                if(waitdiag!=null) {
                    waitdiag.cancel();
                    waitdiag=null;
                }
            }
            break;
            case MESSAGE_REFRESH_TABLES:{
                refreshTables();
            }
            break;
            case MESSAGE_EXPORT_TABLES_COMPLETE:{
                if(waitdiag!=null) {
                    waitdiag.cancel();
                    waitdiag = null;
                }
                if(isMultiSelectMode) closeMultiSelectMode();
                //if(msg.obj instanceof Boolean && !((Boolean) msg.obj)) Toast.makeText(Profile.this,getResources().getString(R.string.activity_profile_toast_export_complete),Toast.LENGTH_SHORT).show();
            }
            break;
            case MESSAGE_IMPORT_TABLES_COMPLETE:{
                if(waitdiag!=null){
                    waitdiag.cancel();
                    waitdiag=null;
                }
                sendEmptyMessage(MESSAGE_REFRESH_TABLES);
            }
            break;
            case MESSAGE_GET_JSON_FILES_COMPLETE:{
                if(file_dialog!=null){
                    //file_dialog.show();
                    ListView listView=file_dialog.findViewById(R.id.layout_filelist_listview);
                    file_dialog.findViewById(R.id.layout_filelist_progressbar).setVisibility(View.GONE);
                    if(jsonFiles.size()>0) {
                        listView.setVisibility(View.VISIBLE);
                        listView.setDivider(null);
                        final JSONFileAdapter adapter=new JSONFileAdapter();
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                adapter.onItemClicked(i);
                            }
                        });
                        file_dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                List<File> files=new ArrayList<>();
                                boolean [] isSelected=adapter.getIsSelected();
                                for(int i=0;i<isSelected.length;i++){
                                    if(isSelected[i]) files.add(jsonFiles.get(i));
                                }
                                if(files.size()<=0){
                                    Snackbar.make(file_dialog.findViewById(R.id.layout_filelist_listview),getResources().getString(R.string.dialog_profile_attention),Snackbar.LENGTH_SHORT).show();
                                    return;
                                }
                                file_dialog.cancel();
                                file_dialog=null;
                                showWaitDialog();
                                new Thread(new ReadFilesAndSave2Tables(files)).start();
                            }
                        });
                        file_dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                long clickedTime=System.currentTimeMillis();
                                boolean[] isSelected=adapter.getIsSelected();
                                List<File> files=new ArrayList<>();
                                for(int i=0;i<isSelected.length;i++){
                                    if(isSelected[i]) files.add(jsonFiles.get(i));
                                }
                                if(files.size()<=0){
                                    Snackbar.make(view,getResources().getString(R.string.dialog_profile_delete_no_selection),Snackbar.LENGTH_SHORT).show();
                                    return;
                                }
                                if((clickedTime-delete_firstclicked)>1000){
                                    delete_firstclicked=clickedTime;
                                    Toast.makeText(Profile.this,getResources().getString(R.string.dialog_profile_delete_confirm),Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                for(int i=0;i<isSelected.length;i++){
                                    if(isSelected[i]){
                                        try{jsonFiles.get(i).delete();}catch (Exception e){e.printStackTrace();}
                                    }
                                }
                                if(getAllJSONFiles!=null){
                                    getAllJSONFiles.isInterrupted=true;
                                    getAllJSONFiles=null;
                                }
                                if(thread_getalljsonfiles!=null){
                                    thread_getalljsonfiles.interrupt();
                                    thread_getalljsonfiles=null;
                                }
                                getAllJSONFiles=new GetAllJSONFiles();
                                thread_getalljsonfiles=new Thread(getAllJSONFiles);
                                thread_getalljsonfiles.start();

                            }
                        });
                    }else{
                        /*file_dialog.findViewById(R.id.layout_filelist_attention).setVisibility(View.VISIBLE);
                        file_dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                file_dialog.cancel();
                                file_dialog=null;
                            }
                        });  */
                        file_dialog.cancel();
                        new AlertDialog.Builder(this)
                                .setTitle(getResources().getString(R.string.dialog_profile_import_no_file_title))
                                .setMessage(getResources().getString(R.string.dialog_profile_import_no_file_att1)+PublicConsts.PACKAGE_NAME+getResources().getString(R.string.dialog_profile_import_no_file_att2))
                                .setPositiveButton(getResources().getString(R.string.dialog_button_positive), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .show();
                    }

                }
            }
            break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(isMultiSelectMode) closeMultiSelectMode();
            else finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void finish(){
        //if(isChanged) setResult(RESULT_PROFILE_CHANGED);
       // else setResult(RESULT_NOTHING_CHANGED);
        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       new MenuInflater(this).inflate(R.menu.profile,menu);
       super.setIconEnable(menu,true);
       this.menu=menu;

        this.menu.getItem(0).setVisible(true);//add
        this.menu.getItem(1).setVisible(false);//delete
        this.menu.getItem(2).setVisible(false);//select all
        this.menu.getItem(3).setVisible(false);//deselect all
        this.menu.getItem(4).setVisible(true);//import
        this.menu.getItem(5).setVisible(false);//export

        this.menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        this.menu.getItem(4).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
       return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            default:break;
            case R.id.action_profile_add:{
                View view=LayoutInflater.from(this).inflate(R.layout.layout_dialog_name,null);
                final EditText editText=view.findViewById(R.id.dialog_edittext_name);
                editText.setText("New Profile List "+(getNotUsedMinimalId()+1));
                final AlertDialog dialog=new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_profile_new_profile_title))
                        .setView(view)
                        .setPositiveButton(getResources().getString(R.string.dialog_button_positive), null)
                        .setNegativeButton(getResources().getString(R.string.dialog_button_negative), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(editText.getText().toString().trim().equals("")){
                            Snackbar.make(view,getResources().getString(R.string.dialog_profile_new_profile_toast_invalid_name),Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        addNewSQLList(editText.getText().toString().trim());
                        dialog.cancel();
                    }
                });

            }
            break;
            case R.id.action_profile_delete:{
                if(isMultiSelectMode){
                    final List<String> table_names=new ArrayList<>();
                    for(int i=0;i<adapter.getIsSelected().length;i++){
                        if(adapter.getIsSelected()[i]) table_names.add(tablelist.get(i).table_name);
                    }
                    if(table_names.size()<=0){
                        return false;
                    }
                    final AlertDialog dialog=new AlertDialog.Builder(this)
                            .setTitle(getResources().getString(R.string.dialog_profile_delete_title))
                            .setMessage(getResources().getString(R.string.dialog_profile_delete_multi_message))
                            .setPositiveButton(getResources().getString(R.string.dialog_button_positive),null)
                            .setNegativeButton(getResources().getString(R.string.dialog_button_negative), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                            deleteTables(table_names);
                            closeMultiSelectMode();
                        }
                    });
                }
            }
            break;
            case R.id.action_profile_select_all:{
                adapter.selectAll();
            }
            break;
            case R.id.action_profile_deselect_all:{
                adapter.deselectAll();
            }
            break;
            case R.id.action_profile_export:{
                if(isMultiSelectMode){
                    List<String> selected_table_names=new ArrayList<>();
                    boolean[] isSelected=adapter.getIsSelected();
                    for(int i=1;i<isSelected.length;i++){
                        if(isSelected[i]) selected_table_names.add(tablelist.get(i).table_name);
                    }
                    File externalFile=getExternalFilesDir(null);
                    if(externalFile!=null) {
                        showWaitDialog();
                        new Thread(new SaveTables2Files(selected_table_names, externalFile.getPath())).start();
                    }
                    else{
                        showStorageErrorDialog();
                    }
                }
            }
            break;
            case R.id.action_profile_import:{
                View dialog_view=LayoutInflater.from(this).inflate(R.layout.layout_filelist,null);
                if(file_dialog!=null){
                    file_dialog.cancel();
                    file_dialog=null;
                }
                file_dialog=new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_profile_import_title))
                        .setView(dialog_view)
                        .setPositiveButton(getResources().getString(R.string.word_import),null)
                        .setNegativeButton(getResources().getString(R.string.dialog_button_negative), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                file_dialog.cancel();
                                file_dialog=null;
                            }
                        })
                        .setNeutralButton(getResources().getString(R.string.action_delete), null)
                        .show();
                if(getAllJSONFiles!=null){
                    getAllJSONFiles.isInterrupted=true;
                    getAllJSONFiles=null;
                }
                if(thread_getalljsonfiles!=null){
                    thread_getalljsonfiles.interrupt();
                    thread_getalljsonfiles=null;
                }
                getAllJSONFiles=new GetAllJSONFiles();
                thread_getalljsonfiles=new Thread(getAllJSONFiles);
                thread_getalljsonfiles.start();
                file_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        if(getAllJSONFiles!=null){
                            getAllJSONFiles.isInterrupted=true;
                            getAllJSONFiles=null;
                        }
                        if(thread_getalljsonfiles!=null){
                            thread_getalljsonfiles.interrupt();
                            thread_getalljsonfiles=null;
                        }
                        file_dialog=null;
                    }
                });
            }
            break;
            case android.R.id.home:{
                if(isMultiSelectMode) closeMultiSelectMode();
                else finish();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeTableList(){
        tablelist.clear();
        TableItem item=new TableItem();
        item.table_diaplay_name=getResources().getString(R.string.activity_profile_default_item);
        item.table_name=SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME;
        SQLiteDatabase database=MySQLiteOpenHelper.getInstance(this).getWritableDatabase();
        Cursor insideCursor=database.rawQuery("select * from "+item.table_name+"",null);
        int count=0;
        while (insideCursor.moveToNext()){
            count++;
        }
        insideCursor.close();
        item.tasknum=count;
        tablelist.add(item);
    }

    private void showWaitDialog(){
        waitdiag=new AlertDialog.Builder(this)
                .setView(LayoutInflater.from(this).inflate(R.layout.layout_dialog_wait,null))
                .setCancelable(false)
                .create();
        waitdiag.show();
    }

    private void showStorageErrorDialog(){
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.activity_profile_storage_error_title))
                .setMessage(getResources().getString(R.string.activity_profile_storage_error_message))
                .setPositiveButton(getResources().getString(R.string.dialog_button_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    private void deleteTable(final String table_name){
        SharedPreferences settings=getSharedPreferences(PublicConsts.PREFERENCES_NAME,Activity.MODE_PRIVATE);
        final SharedPreferences.Editor editor=settings.edit();
        if(settings.getString(PublicConsts.PREFERENCES_CURRENT_TABLE_NAME,"").equals(table_name)){
            editor.putString(PublicConsts.PREFERENCES_CURRENT_TABLE_NAME,SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME);
            editor.apply();
        }
        showWaitDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase database=MySQLiteOpenHelper.getInstance(Profile.this).getWritableDatabase();
                try{
                    if(!table_name.equals(SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME)) {
                        database.execSQL("drop table "+table_name+" ;");
                        editor.remove(table_name);
                    }
                    editor.apply();
                    MySQLiteOpenHelper.clearCurrentInstance();
                    setResult(RESULT_PROFILE_CHANGED);
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(TimeSwitchService.service_queue.size()>0) TimeSwitchService.service_queue.getLast().refreshTaskItems();
                else startService(new Intent(Profile.this,TimeSwitchService.class));
            }
        }).start();
    }

    private void deleteTables(final List<String> table_names){
        showWaitDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences settings=getSharedPreferences(PublicConsts.PREFERENCES_NAME,Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor=settings.edit();
                SQLiteDatabase database=MySQLiteOpenHelper.getInstance(Profile.this).getWritableDatabase();
                try{
                    for(int i=0;i<table_names.size();i++){
                        if(!table_names.get(i).equals(SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME)) {
                            if(settings.getString(PublicConsts.PREFERENCES_CURRENT_TABLE_NAME,"").equals(table_names.get(i))){
                                editor.putString(PublicConsts.PREFERENCES_CURRENT_TABLE_NAME,SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME);
                                editor.apply();
                            }
                            database.execSQL("drop table "+table_names.get(i)+" ;");
                            editor.remove(table_names.get(i));
                        }
                    }
                    editor.apply();
                    MySQLiteOpenHelper.clearCurrentInstance();
                    setResult(RESULT_PROFILE_CHANGED);
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(TimeSwitchService.service_queue.size()>0) TimeSwitchService.service_queue.getLast().refreshTaskItems();
                else startService(new Intent(Profile.this,TimeSwitchService.class));
            }
        }).start();
    }

    private int getSelectedPosition(){
        SharedPreferences settings=getSharedPreferences(PublicConsts.PREFERENCES_NAME,Activity.MODE_PRIVATE);
        for(int i=0;i<tablelist.size();i++){
            if(tablelist.get(i).table_name.equals(settings.getString(PublicConsts.PREFERENCES_CURRENT_TABLE_NAME,""))) return i;
        }
        return -1;
    }

    private void addNewSQLList(final String displayname){
        showWaitDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String table_name=SQLConsts.SQL_DATABASE_TABLE_NAME_FONT+getNotUsedMinimalId();
                SQLiteDatabase database=MySQLiteOpenHelper.getInstance(Profile.this).getWritableDatabase();
                database.execSQL(MySQLiteOpenHelper.getCreateTableSQLCommand(table_name));
                SharedPreferences settings=getSharedPreferences(PublicConsts.PREFERENCES_NAME,Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor=settings.edit();
                editor.putString(table_name,displayname);
                editor.apply();
                sendEmptyMessage(MESSAGE_REFRESH_TABLES);
            }
        }).start();
    }

    private int getNotUsedMinimalId(){
        int selectedID;
        for (selectedID=0;selectedID<tablelist.size();selectedID++){
            boolean ifcontains=false;
            for(int h=0;h<tablelist.size();h++){
                if(tablelist.get(h).table_name.equals(SQLConsts.SQL_DATABASE_TABLE_NAME_FONT+selectedID)){
                    ifcontains=true;
                    break;
                }
            }
            if(!ifcontains) break;
        }
        return selectedID;
    }

    private void openMultiSelectMode(int position){
        isMultiSelectMode=true;
        adapter.onItemLongClicked(position);
        this.menu.getItem(0).setVisible(false);//add
        this.menu.getItem(1).setVisible(true);//delete;
        this.menu.getItem(2).setVisible(true);//select all
        this.menu.getItem(3).setVisible(true);//deselect all
        this.menu.getItem(4).setVisible(false);//import
        this.menu.getItem(5).setVisible(true);//export

        this.menu.getItem(1).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        this.menu.getItem(2).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        this.menu.getItem(3).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        this.menu.getItem(5).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    private void closeMultiSelectMode(){
        isMultiSelectMode=false;
        adapter.closeMultiSelectMode();
        listview.setOnItemClickListener(onItemClicked_Normal);
        listview.setOnItemLongClickListener(onItemLongClicked_Normal);
        this.menu.getItem(0).setVisible(true);//add
        this.menu.getItem(1).setVisible(false);//delete
        this.menu.getItem(2).setVisible(false);//select all
        this.menu.getItem(3).setVisible(false);//deselect all
        this.menu.getItem(4).setVisible(true);//import
        this.menu.getItem(5).setVisible(false);//export

        this.menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        this.menu.getItem(4).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    private class RefreshTaskTablesFromDatabase implements Runnable{
    boolean isInterrupted=false;
        @Override
        public void run(){
            initializeTableList();
            SQLiteDatabase database= MySQLiteOpenHelper.getInstance(Profile.this).getWritableDatabase();
            SharedPreferences settings=getSharedPreferences(PublicConsts.PREFERENCES_NAME, Activity.MODE_PRIVATE);
            final String sql="select name from "+ "sqlite_master"+" where type='table' order by name";
            Cursor cursor=database.rawQuery(sql,null);
            while (cursor.moveToNext()){
                if(isInterrupted) {
                    initializeTableList();
                    break;
                }
                TableItem item=new TableItem();
                item.table_name=cursor.getString(0);
                item.table_diaplay_name=settings.getString(item.table_name,"");
                if((item.table_name.contains(SQLConsts.SQL_DATABASE_TABLE_NAME_FONT))&&!item.table_name.equals(SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME)) {
                    Cursor insideCursor=database.rawQuery("select * from "+item.table_name+"",null);
                    int count=0;
                    while (insideCursor.moveToNext()){
                        count++;
                    }
                    insideCursor.close();
                    item.tasknum=count;
                    tablelist.add(item);
                }
            }
            cursor.close();
            if(!isInterrupted) sendEmptyMessage(MESSAGE_REQUIRE_UPDATE_LIST);
        }
    }

    private class TableListAdapter extends BaseAdapter{
        private boolean isMultiSelectMode=false;
        boolean isSelected[] ;
        private TableListAdapter(){
            isSelected=new boolean[tablelist.size()];
        }

        @Override
        public int getCount() {
            return tablelist.size();
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
        public View getView(final int i, View view, ViewGroup viewGroup) {
           //final SharedPreferences settings=Profile.this.getSharedPreferences(PublicConsts.PREFERENCES_NAME,Activity.MODE_PRIVATE);
            if(view==null){
                view= LayoutInflater.from(Profile.this).inflate(R.layout.item_profile,viewGroup,false);
            }
            String display_filename=tablelist.get(i).table_diaplay_name;
            if(display_filename.length()>26) display_filename=display_filename.substring(0,26)+"...";
            ((TextView)view.findViewById(R.id.item_profile_name)).setText(display_filename);
            ((TextView)view.findViewById(R.id.item_profile_contains)).setText(tablelist.get(i).tasknum+getResources().getString(R.string.activity_profile_task_total_mask));
            ((RadioButton)view.findViewById(R.id.item_profile_ra)).setChecked(SQLConsts.getCurrentTableName(Profile.this).equals(tablelist.get(i).table_name));
            view.findViewById(R.id.item_profile_ra).setVisibility(isMultiSelectMode?View.GONE:View.VISIBLE);
            view.findViewById(R.id.item_profile_more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemMenuClicked(view,tablelist.get(i).table_name);
                }
            });
            ((CheckBox)view.findViewById(R.id.item_profile_cb)).setChecked(isSelected[i]);
            if(i!=0){
                view.findViewById(R.id.item_profile_cb).setVisibility(isMultiSelectMode?View.VISIBLE:View.GONE);
            }
            else{
                view.findViewById(R.id.item_profile_cb).setVisibility(View.INVISIBLE);
                //view.findViewById(R.id.item_profile_more).setVisibility(View.GONE);
            }
            view.findViewById(R.id.item_profile_more).setVisibility(isMultiSelectMode?View.GONE:View.VISIBLE);
            return view;
        }

        private void onDataSetChanged(){
            isSelected=new boolean[tablelist.size()];
            notifyDataSetChanged();
        }

        private void onItemLongClicked(int position){
            isSelected=new boolean[tablelist.size()];
            if(position>=0&&position<isSelected.length){
                isMultiSelectMode=true;
                if(position!=0) isSelected[position]=true;
                notifyDataSetChanged();
            }
        }

        private void onMultiSelectModeItemClicked(int position){
            if(position>=0&&position<isSelected.length){
                if(position!=0){
                    isSelected[position]=!isSelected[position];
                }
                notifyDataSetChanged();
            }
        }

        private void closeMultiSelectMode(){
            isMultiSelectMode=false;
            notifyDataSetChanged();
        }

        private void selectAll(){
            if(isMultiSelectMode){
                for(int i=1;i<isSelected.length;i++){
                    isSelected[i]=true;
                }
            }
            notifyDataSetChanged();
        }

        private void deselectAll(){
            if(isMultiSelectMode){
                for(int i=1;i<isSelected.length;i++){
                    isSelected[i]=false;
                }
            }
            notifyDataSetChanged();
        }

        private boolean[] getIsSelected(){
            return this.isSelected;
        }

        private void onItemMenuClicked(View view, final String table_name){
            final SharedPreferences settings=getSharedPreferences(PublicConsts.PREFERENCES_NAME,Activity.MODE_PRIVATE);
            final SharedPreferences.Editor editor=settings.edit();
            View contentView=LayoutInflater.from(Profile.this).inflate(R.layout.layout_popup_profile,null);
            final PopupWindow pw=new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,true);
            pw.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.color_transparent_popup_window)));
            pw.setTouchable(true);
            pw.setOutsideTouchable(true);
            pw.setContentView(contentView);
            int[] display_values= ValueUtils.calculatePopWindowPos(view,contentView);
            pw.showAtLocation(view, Gravity.TOP|Gravity.START,display_values[0],display_values[1]);

            if(!table_name.equals(SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME))
            contentView.findViewById(R.id.popup_profile_rename).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pw.dismiss();
                    final View dialogview=LayoutInflater.from(Profile.this).inflate(R.layout.layout_dialog_name,null);
                    final EditText editText=dialogview.findViewById(R.id.dialog_edittext_name);
                    editText.setText(settings.getString(table_name,""));
                    final AlertDialog dialog=new AlertDialog.Builder(Profile.this)
                            .setTitle(getResources().getString(R.string.dialog_profile_rename_title))
                            .setView(dialogview)
                            .setPositiveButton(getResources().getString(R.string.dialog_button_positive), null)
                            .setNegativeButton(getResources().getString(R.string.dialog_button_negative), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String name=editText.getText().toString().trim();
                            if(name.equals("")||name.length()==0) {
                                Snackbar.make(dialogview,getResources().getString(R.string.dialog_profile_new_profile_toast_invalid_name),Snackbar.LENGTH_SHORT).show();
                                return;
                            }
                            editor.putString(table_name,name);
                            editor.apply();
                            dialog.cancel();
                            sendEmptyMessage(MESSAGE_REFRESH_TABLES);
                        }
                    });
                }
            });
            else contentView.findViewById(R.id.popup_profile_rename).setVisibility(View.GONE);

            if(!table_name.equals(SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME))
            contentView.findViewById(R.id.popup_profile_delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pw.dismiss();
                    SharedPreferences settings=getSharedPreferences(PublicConsts.PREFERENCES_NAME,Activity.MODE_PRIVATE);
                    final String table_display_name=settings.getString(table_name,"");
                    if(!table_name.equals(SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME)){
                        new AlertDialog.Builder(Profile.this)
                                .setTitle(getResources().getString(R.string.dialog_profile_delete_title))
                                .setMessage(getResources().getString(R.string.dialog_profile_delete_message)+" ¡°"+table_display_name+"¡±?")
                                .setPositiveButton(getResources().getString(R.string.dialog_button_positive), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        deleteTable(table_name);
                                    }
                                })
                                .setNegativeButton(getResources().getString(R.string.dialog_button_negative), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .show();
                    }
                    else{
                        Toast.makeText(Profile.this,"default table can not delete",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            else contentView.findViewById(R.id.popup_profile_delete).setVisibility(View.GONE);

            contentView.findViewById(R.id.popup_profile_export).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pw.dismiss();
                    showWaitDialog();
                    String[] table_name_array=new String[1];
                    table_name_array[0]=table_name;
                    File externalDir=getExternalFilesDir(null);
                    if(externalDir==null){
                        showStorageErrorDialog();
                        return;
                    }
                    new Thread(new SaveTables2Files(Arrays.asList(table_name_array),externalDir.getPath())).start();
                }
            });
        }
    }

    private static class TableItem{
        public String table_name="";
        public String table_diaplay_name="";
        public int tasknum=0;
        private TableItem(){}
        private TableItem(String table_name,String table_diaplay_name,int tasknum){
            this.table_name=table_name;
            this.table_diaplay_name=table_diaplay_name;
            this.tasknum=tasknum;
        }
    }

    private class SaveTables2Files implements Runnable{
        private List<String> table_names;
        String file_path;
        //long current_time=System.currentTimeMillis();
        String filename_mask;
        public SaveTables2Files(List<String> table_names,String path){
            this.table_names=table_names;
            this.file_path=path;
            Calendar calendar=Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            filename_mask=""+calendar.get(Calendar.YEAR)+"-"+ValueUtils.format(calendar.get(Calendar.MONTH)+1)+"-"+ValueUtils.format(calendar.get(Calendar.DAY_OF_MONTH))+"-"+ValueUtils.format(calendar.get(Calendar.HOUR_OF_DAY))+"-"+ValueUtils.format(calendar.get(Calendar.MINUTE))+"-"+ValueUtils.format(calendar.get(Calendar.SECOND));
        }

        @Override
        public void run(){
            SQLiteDatabase database=MySQLiteOpenHelper.getInstance(Profile.this).getWritableDatabase();
            boolean ifHasException=false;
            final List<String> exceptions=new ArrayList<>();
            for(int i=0;i<table_names.size();i++){
                try{
                    JSONArray jsonArray=new JSONArray();
                    Cursor cursor=database.rawQuery("select * from "+table_names.get(i)+" ;",null);
                    while (cursor.moveToNext()){
                        JSONObject jsonObject=new JSONObject();
                        jsonObject.put(SQLConsts.SQL_TASK_COLUMN_ID,cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ID)));
                        jsonObject.put(SQLConsts.SQL_TASK_COLUMN_NAME,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_NAME)));
                        jsonObject.put(SQLConsts.SQL_TASK_COLUMN_ENABLED,cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ENABLED)));
                        jsonObject.put(SQLConsts.SQL_TASK_COLUMN_TYPE,cursor.getInt(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_TYPE)));
                        jsonObject.put(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES)));
                        jsonObject.put(SQLConsts.SQL_TASK_COLUMN_EXCEPTIONS,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_EXCEPTIONS)));
                        jsonObject.put(SQLConsts.SQL_TASK_COLUMN_ACTIONS,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ACTIONS)));
                        jsonObject.put(SQLConsts.SQL_TASK_COLUMN_URI_RING_NOTIFICATION,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_URI_RING_NOTIFICATION)));
                        jsonObject.put(SQLConsts.SQL_TASK_COLUMN_URI_RING_CALL,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_URI_RING_CALL)));
                        jsonObject.put(SQLConsts.SQL_TASK_COLUMN_URI_WALLPAPER_DESKTOP,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_URI_WALLPAPER_DESKTOP)));
                        jsonObject.put(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_TITLE,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_TITLE)));
                        jsonObject.put(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_MESSAGE,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_MESSAGE)));
                        jsonObject.put(SQLConsts.SQL_TASK_COLUMN_TOAST,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_TOAST)));
                        jsonObject.put(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_PHONE_NUMBERS,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_PHONE_NUMBERS)));
                        jsonObject.put(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_MESSAGE,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_MESSAGE)));
                        jsonObject.put(SQLConsts.SQL_TASK_COLUMN_ADDITIONS,cursor.getString(cursor.getColumnIndex(SQLConsts.SQL_TASK_COLUMN_ADDITIONS)));
                        jsonArray.put(jsonObject);
                    }
                    cursor.close();
                    OutputStream out=new FileOutputStream(new File(file_path+"/"+filename_mask+"-"+(i+1)+".json"));
                    Writer writer=new OutputStreamWriter(out);
                    writer.write(jsonArray.toString());
                    writer.flush();
                    writer.close();
                }catch (Exception e){
                    ifHasException=true;
                    exceptions.add(e.toString());
                    e.printStackTrace();
                }
            }
            Message message=new Message();
            message.what=MESSAGE_EXPORT_TABLES_COMPLETE;
            message.obj=ifHasException;
            sendMessage(message);
            if(!ifHasException){
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Profile.this,getResources().getString(R.string.activity_profile_toast_export_complete)+PublicConsts.PACKAGE_NAME
                                +getResources().getString(R.string.activity_profile_toast_export_complete2),Toast.LENGTH_SHORT).show();
                    }
                });
            }
            if(exceptions.size()>0) {
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder builder=new StringBuilder("");
                        for(String string:exceptions){
                            builder.append(string);
                            builder.append("\n");
                        }
                        new AlertDialog.Builder(Profile.this)
                                .setTitle("Exception")
                                .setMessage(builder.toString())
                                .setPositiveButton(getResources().getString(R.string.dialog_button_positive), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .show();
                    }
                });

            }
        }
    }

    private class ReadFilesAndSave2Tables implements Runnable{
        private List<File> files;
        private List<String> exceptions=new ArrayList<>();
        public ReadFilesAndSave2Tables(List<File> files) {
            this.files=files;
        }
        @Override
        public void run(){
            if(files==null) return;
            SQLiteDatabase database=MySQLiteOpenHelper.getInstance(Profile.this).getWritableDatabase();
            SharedPreferences settings=getSharedPreferences(PublicConsts.PREFERENCES_NAME,Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor=settings.edit();
            for(int i=0;i<files.size();i++){
                try{
                    StringBuilder builder=new StringBuilder("");
                    InputStream in=new FileInputStream(files.get(i));
                    BufferedReader reader=new BufferedReader(new InputStreamReader(in));
                    String line;
                    while((line=reader.readLine())!=null){
                        builder.append(line);
                    }
                    JSONArray jsonarray = (JSONArray) new JSONTokener(builder.toString()).nextValue();
                    String newTableName=SQLConsts.SQL_DATABASE_TABLE_NAME_FONT+getNotUsedMinimalId();
                    database.execSQL(MySQLiteOpenHelper.getCreateTableSQLCommand(newTableName));
                    String display_name=files.get(i).getName().substring(0,files.get(i).getName().lastIndexOf("."));
                    editor.putString(newTableName,display_name);
                    editor.apply();
                    int count=0;
                    for(int j=0;j<jsonarray.length();j++){
                        JSONObject jsonObject=(JSONObject)jsonarray.get(j);
                        ContentValues contentValues=new ContentValues();
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_ID,jsonObject.getInt(SQLConsts.SQL_TASK_COLUMN_ID));
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_NAME,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_NAME));
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_ENABLED,jsonObject.getInt(SQLConsts.SQL_TASK_COLUMN_ENABLED));
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_TYPE,jsonObject.getInt(SQLConsts.SQL_TASK_COLUMN_TYPE));
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_TRIGGER_VALUES));
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_EXCEPTIONS,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_EXCEPTIONS));
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_ACTIONS,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_ACTIONS));
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_URI_RING_NOTIFICATION,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_URI_RING_NOTIFICATION));
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_URI_RING_CALL,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_URI_RING_CALL));
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_URI_WALLPAPER_DESKTOP,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_URI_WALLPAPER_DESKTOP));
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_TITLE,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_TITLE));
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_MESSAGE,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_NOTIFICATION_MESSAGE));
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_TOAST,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_TOAST));
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_PHONE_NUMBERS,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_PHONE_NUMBERS));
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_MESSAGE,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_SMS_SEND_MESSAGE));
                        contentValues.put(SQLConsts.SQL_TASK_COLUMN_ADDITIONS,jsonObject.getString(SQLConsts.SQL_TASK_COLUMN_ADDITIONS));
                        database.insert(newTableName,null,contentValues);
                        count++;
                    }
                    tablelist.add(new TableItem(newTableName,display_name,count));
                }catch (Exception e){
                    exceptions.add(e.toString());
                    e.printStackTrace();
                }
            }
            sendEmptyMessage(MESSAGE_IMPORT_TABLES_COMPLETE);
            if(exceptions.size()>0){
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder builder=new StringBuilder("");
                        for(int i=0;i<exceptions.size();i++){
                            builder.append(exceptions.get(i));
                            builder.append("\n");
                        }
                        new AlertDialog.Builder(Profile.this)
                        .setTitle("Exception")
                        .setMessage(builder.toString())
                        .setPositiveButton(getResources().getString(R.string.dialog_button_positive), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
                    }
                });
            }else{
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                      Toast.makeText(Profile.this,getResources().getString(R.string.activity_profile_toast_import_complete),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }

    private class GetAllJSONFiles implements Runnable{
        boolean isInterrupted=false;
        @Override
        public void run(){
            File externalFile=getExternalFilesDir(null);
            if(externalFile==null) return;
            jsonFiles.clear();
            try{
                File[] files=externalFile.listFiles();
                for(File file:files){
                    if(isInterrupted){
                        jsonFiles.clear();
                        break;
                    }
                    if(!file.isDirectory()&&(file.getName().substring(file.getName().lastIndexOf(".")).equals(".json"))) jsonFiles.add(file);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            if(!isInterrupted)sendEmptyMessage(MESSAGE_GET_JSON_FILES_COMPLETE);
        }
    }

    private class JSONFileAdapter extends BaseAdapter{
        private boolean[] isSelected;
        private JSONFileAdapter(){
            isSelected=new boolean[jsonFiles.size()];
        }
        @Override
        public int getCount() {
            return jsonFiles.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        public void onItemClicked(int position){
            isSelected[position]=!isSelected[position];
            notifyDataSetChanged();
        }

        public boolean[] getIsSelected(){
            return this.isSelected;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view==null){
                view=LayoutInflater.from(Profile.this).inflate(R.layout.item_json_file,viewGroup,false);
            }
            String filename=jsonFiles.get(i).getName();
            if(filename.length()>28) filename=filename.substring(0,28)+"...";
            ((TextView)view.findViewById(R.id.item_json_file_name)).setText(filename);
            ((CheckBox)view.findViewById(R.id.item_json_file_checkbox)).setChecked(isSelected[i]);
            return view;
        }
    }

}
