package com.github.ghmxr.timeswitch.activities;

import android.Manifest;
import android.app.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.Global;
import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.MySQLiteOpenHelper;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.data.v2.SQLConsts;
import com.github.ghmxr.timeswitch.services.TimeSwitchService;
import com.github.ghmxr.timeswitch.ui.DialogForJsonFileSelect;
import com.github.ghmxr.timeswitch.utils.EnvironmentUtils;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.io.File;
import java.util.Calendar;
import java.util.List;

public class ProfileActivity extends BaseActivity implements Runnable{
    RecyclerView recyclerView;
    private static final int MENU_ADD=0;
    private static final int MENU_DELETE=1;
    private static final int MENU_SELECT_ALL=2;
    private static final int MENU_DESELECT_ALL=3;
    private static final int MENU_IMPORT=4;
    private static final int MENU_EXPORT=5;
    private boolean isTableStatusChanged=false;
    private long confirm_time=0;
    private Menu menu;
    private AlertDialog wait_dialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_profile);
        Toolbar toolbar=findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolBarAndStatusBarColor(toolbar,getIntent().getStringExtra(EXTRA_TITLE_COLOR));
        recyclerView=findViewById(R.id.layout_profile_recyclerview);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        wait_dialog=getWaitDialogInstance();
        wait_dialog.show();
        new Thread(this).start();
    }

    @Override
    public void run(){
        final List<MySQLiteOpenHelper.SqlTableItem>list=MySQLiteOpenHelper.getTableListFromDatabase(ProfileActivity.this);
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                recyclerView.setAdapter(new ListAdapter(list));
                if(wait_dialog!=null)wait_dialog.cancel();
            }
        });
    }

    @Override
    public void processMessage(Message msg){}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.profile,menu);
        super.setIconEnable(menu,true);
        this.menu=menu;
        menu.getItem(MENU_ADD).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.getItem(MENU_DELETE).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.getItem(MENU_IMPORT).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return super.onCreateOptionsMenu(menu);
    }

    Toast att;
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final ListAdapter adapter=(ListAdapter) recyclerView.getAdapter();
        switch (item.getItemId()){
            default:break;
            case android.R.id.home:{
                checkIsMultiSelectModeAndFinish();
            }
            break;
            case R.id.action_profile_add:{
                final AlertDialog dialog=new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_profile_new_profile_title))
                        .setView(R.layout.layout_dialog_name)
                        .setPositiveButton(getResources().getString(R.string.dialog_button_positive),null)
                        .setNegativeButton(getResources().getString(R.string.dialog_button_negative), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        }).show();
                final EditText editText=dialog.findViewById(R.id.dialog_edittext_name);
                editText.setText(getResources().getString(R.string.dialog_profile_new_profile_title)+(MySQLiteOpenHelper.getIdForNewTable(this)+1));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(editText.getText().toString().trim().equals("")){
                            if(att!=null) att.cancel();
                            att=Toast.makeText(ProfileActivity.this,getResources().getString(R.string.dialog_profile_new_profile_toast_invalid_name),Toast.LENGTH_SHORT);
                            att.show();
                            return;
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    MySQLiteOpenHelper.addTable(ProfileActivity.this,editText.getText().toString());
                                    new Thread(ProfileActivity.this).start();
                                }catch (Exception e){e.printStackTrace();}
                            }
                        }).start();
                        dialog.cancel();
                    }
                });
            }
            break;
            case R.id.action_profile_delete:{
                if(adapter==null) return false;
                long current=System.currentTimeMillis();
                if(current-confirm_time>1000){
                    confirm_time=current;
                    Snackbar.make(findViewById(android.R.id.content),getResources().getString(R.string.dialog_profile_delete_confirm),Snackbar.LENGTH_SHORT).show();
                    return false;
                }
                recyclerView.setAdapter(null);
                setMenuItemVisibilityOfMultiSelectMode(false);
                wait_dialog=getWaitDialogInstance();
                wait_dialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<MySQLiteOpenHelper.SqlTableItem>list=adapter.getList();
                        boolean[] isSelected=adapter.getIsSelected();
                        for(int i=1;i<list.size();i++){
                            if(isSelected[i]){
                                String table_name=list.get(i).table_name;
                                if(table_name.equals(SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME))continue;
                                MySQLiteOpenHelper.deleteTable(ProfileActivity.this,table_name);
                            }
                        }
                        new Thread(ProfileActivity.this).start();
                    }
                }).start();
            }
            break;
            case R.id.action_profile_select_all:{
                if(adapter!=null) adapter.setAllItemsSelected(true);
            }
            break;
            case R.id.action_profile_deselect_all:{
                if(adapter!=null) adapter.setAllItemsSelected(false);
            }
            break;
            case R.id.action_profile_export:{
                setMenuItemVisibilityOfMultiSelectMode(false);
                final StringBuilder error=new StringBuilder("");
                wait_dialog=getWaitDialogInstance();
                wait_dialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<MySQLiteOpenHelper.SqlTableItem>list=adapter.getList();
                        boolean []isSelected=adapter.getIsSelected();
                        Calendar calendar=Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        String head=calendar.get(Calendar.YEAR)+ValueUtils.format(calendar.get(Calendar.MONTH)+1)+ValueUtils.format(calendar.get(Calendar.DAY_OF_MONTH))
                                +ValueUtils.format(calendar.get(Calendar.HOUR_OF_DAY))+ValueUtils.format(calendar.get(Calendar.MINUTE))+ValueUtils.format(calendar.get(Calendar.SECOND));
                        for(int i=1;i<list.size();i++){
                            if(isSelected[i]){
                                try{
                                    MySQLiteOpenHelper.saveTable2File(ProfileActivity.this,list.get(i).table_name,getExternalFilesDir(null).toString()+"/"+head+i+".json");
                                }catch (Exception e){
                                    e.printStackTrace();
                                    error.append(e.toString());
                                    error.append("\n");
                                }
                            }
                        }
                        new Thread(ProfileActivity.this).start();
                        if(!error.toString().equals("")){
                            Global.handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    new AlertDialog.Builder(ProfileActivity.this).setTitle("ErrorMessages")
                                            .setMessage(error.toString())
                                            .setPositiveButton(getResources().getString(R.string.dialog_button_positive), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {}
                                            }).show();
                                }
                            });
                        }
                        EnvironmentUtils.showToast(ProfileActivity.this,null,getResources().getString(R.string.profile_export_complete)+getExternalFilesDir(null));
                    }
                }).start();
            }
            break;
            case R.id.action_profile_import:{
                if(Build.VERSION.SDK_INT>=23&&PermissionChecker.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PermissionChecker.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
                    EnvironmentUtils.PermissionRequestUtil.showSnackbarWithActionOfAppdetailPage(this
                            ,getResources().getString(R.string.permission_request_read_external_storage)
                            ,getResources().getString(R.string.permission_grant_action_att));
                    return false;
                }
                DialogForJsonFileSelect dialog=new DialogForJsonFileSelect(this);
                dialog.show();
                dialog.setOnDialogConfirmedListener(new DialogForJsonFileSelect.DialogConfirmedListener() {
                    @Override
                    public void onDialogConfirmed(final List<File> selected_files) {
                        wait_dialog=getWaitDialogInstance();
                        wait_dialog.show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final StringBuilder error=new StringBuilder("");
                                for(File file:selected_files){
                                    try{
                                        MySQLiteOpenHelper.readFile2Table(ProfileActivity.this,file);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                        error.append(e.toString());
                                    }

                                }
                                new Thread(ProfileActivity.this).start();
                                if(error.toString().length()>0){
                                    Global.handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            new AlertDialog.Builder(ProfileActivity.this).setTitle("Error")
                                                    .setMessage(error.toString())
                                                    .setPositiveButton(getResources().getString(R.string.dialog_button_positive), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {}
                                                    }).show();
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                });
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkIsMultiSelectModeAndFinish(){
        ListAdapter adapter=(ListAdapter) recyclerView.getAdapter();
        if(adapter==null) {
            finish();
            return ;
        }
        if(adapter.isMultiSelectMode()) adapter.closeMultiSelectMode();
        else finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            checkIsMultiSelectModeAndFinish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void finish(){
        if(isTableStatusChanged){
            setResult(RESULT_OK);
        }
        super.finish();
    }

    private void setMenuItemVisibilityOfMultiSelectMode(boolean isMultiSelectMode){
        menu.getItem(MENU_ADD).setVisible(!isMultiSelectMode);
        menu.getItem(MENU_IMPORT).setVisible(!isMultiSelectMode);
        menu.getItem(MENU_DELETE).setVisible(isMultiSelectMode);
        menu.getItem(MENU_SELECT_ALL).setVisible(isMultiSelectMode);
        menu.getItem(MENU_DESELECT_ALL).setVisible(isMultiSelectMode);
        menu.getItem(MENU_EXPORT).setVisible(isMultiSelectMode);
    }

    private AlertDialog getWaitDialogInstance(){
        if(wait_dialog!=null) wait_dialog.cancel();
        return new AlertDialog.Builder(this).setView(R.layout.layout_dialog_wait)
                .setCancelable(false)
                .create();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder{
        RadioButton ra;
        CheckBox cb;
        TextView tv,tv_att;
        View more;

        public ViewHolder(View itemView) {
            super(itemView);
            ra=itemView.findViewById(R.id.item_profile_ra);
            cb=itemView.findViewById(R.id.item_profile_cb);
            tv=itemView.findViewById(R.id.item_profile_name);
            tv_att=itemView.findViewById(R.id.item_profile_contains);
            more=itemView.findViewById(R.id.item_profile_more);
        }
    }

    private class ListAdapter extends RecyclerView.Adapter<ViewHolder>{
        private List<MySQLiteOpenHelper.SqlTableItem>list;
        private boolean isMultiSelectMode=false;
        private boolean[] isSelected;
        private int selected_position=0;
        private ListAdapter(List<MySQLiteOpenHelper.SqlTableItem> list){
            this.list=list;
            isSelected=new boolean[list.size()];
            for(int i=0;i<list.size();i++){
                if(list.get(i).table_name.equals(getSharedPreferences(PublicConsts.PREFERENCES_NAME,Context.MODE_PRIVATE)
                        .getString(PublicConsts.PREFERENCES_CURRENT_TABLE_NAME,SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME))) selected_position=i;
            }
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ProfileActivity.ViewHolder(LayoutInflater.from(ProfileActivity.this).inflate(R.layout.item_profile,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            final MySQLiteOpenHelper.SqlTableItem item=list.get(position);
            holder.tv.setText(list.get(position).table_display_name);
            holder.tv_att.setText(list.get(position).task_num+getResources().getString(R.string.activity_profile_task_total_mask));
            holder.ra.setChecked(position==selected_position);
            holder.cb.setChecked(isSelected[position]);

            holder.ra.setVisibility(isMultiSelectMode?View.INVISIBLE:View.VISIBLE);
            holder.cb.setVisibility(isMultiSelectMode?View.VISIBLE:View.INVISIBLE);
            holder.more.setVisibility(isMultiSelectMode?View.GONE:View.VISIBLE);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isMultiSelectMode){
                        if(holder.getAdapterPosition()==0){
                            isSelected[0]=false;
                            Snackbar.make(findViewById(android.R.id.content),"The default table can not be selected",Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        isSelected[holder.getAdapterPosition()]=!isSelected[holder.getAdapterPosition()];
                        notifyItemChanged(holder.getAdapterPosition());
                    }else {
                        if(selected_position!=holder.getAdapterPosition()){
                            selected_position=holder.getAdapterPosition();
                            MySQLiteOpenHelper.setCurrentTableName(ProfileActivity.this,item.table_name);
                            isTableStatusChanged=true;
                            TimeSwitchService.sendEmptyMessage(TimeSwitchService.MESSAGE_REQUEST_REFRESH_TASKS);
                            notifyDataSetChanged();
                        }
                    }
                }
            });
            holder.more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(ProfileActivity.this,"more:"+holder.getAdapterPosition(),Toast.LENGTH_SHORT).show();
                    final SharedPreferences settings=getSharedPreferences(PublicConsts.PREFERENCES_NAME, Activity.MODE_PRIVATE);
                    final SharedPreferences.Editor editor=settings.edit();
                    View contentView=LayoutInflater.from(ProfileActivity.this).inflate(R.layout.layout_popup_profile,null);
                    final PopupWindow pw=new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,true);
                    pw.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.color_transparent_popup_window)));
                    pw.setTouchable(true);
                    pw.setOutsideTouchable(true);
                    pw.setContentView(contentView);
                    int[] display_values= ValueUtils.calculatePopWindowPos(v,contentView);
                    pw.showAtLocation(v, Gravity.TOP|Gravity.START,display_values[0],display_values[1]);
                    if(!item.table_name.equals(SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME))
                    contentView.findViewById(R.id.popup_profile_rename).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            pw.dismiss();
                            final View dialogview=LayoutInflater.from(ProfileActivity.this).inflate(R.layout.layout_dialog_name,null);
                            final EditText editText=dialogview.findViewById(R.id.dialog_edittext_name);
                            editText.setText(settings.getString(item.table_name,""));
                            final AlertDialog dialog=new AlertDialog.Builder(ProfileActivity.this)
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
                                        //Snackbar.make(dialogview,getResources().getString(R.string.dialog_profile_new_profile_toast_invalid_name),Snackbar.LENGTH_SHORT).show();
                                        if(att!=null) att.cancel();
                                        att=Toast.makeText(ProfileActivity.this,getResources().getString(R.string.dialog_profile_new_profile_toast_invalid_name),Toast.LENGTH_SHORT);
                                        att.show();
                                        return;
                                    }
                                    editor.putString(item.table_name,name);
                                    editor.apply();
                                    dialog.cancel();
                                    new Thread(ProfileActivity.this).start();
                                }
                            });
                        }
                    });
                    else contentView.findViewById(R.id.popup_profile_rename).setVisibility(View.GONE);
                    if(!item.table_name.equals(SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME))
                        contentView.findViewById(R.id.popup_profile_delete).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                pw.dismiss();
                                new AlertDialog.Builder(ProfileActivity.this)
                                        .setTitle(getResources().getString(R.string.dialog_profile_delete_title))
                                        .setMessage(getResources().getString(R.string.dialog_profile_delete_message)+" ¡°"+item.table_display_name+"¡±?")
                                        .setPositiveButton(getResources().getString(R.string.dialog_button_positive), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if(getSharedPreferences(PublicConsts.PREFERENCES_NAME,Context.MODE_PRIVATE).getString(PublicConsts.PREFERENCES_CURRENT_TABLE_NAME,SQLConsts.SQL_DATABASE_DEFAULT_TABLE_NAME).equals(item.table_name)) isTableStatusChanged=true;
                                                recyclerView.setAdapter(null);
                                                wait_dialog=getWaitDialogInstance();
                                                wait_dialog.show();
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        MySQLiteOpenHelper.deleteTable(ProfileActivity.this,item.table_name);
                                                        new Thread(ProfileActivity.this).start();
                                                    }
                                                }).start();
                                            }
                                        })
                                        .setNegativeButton(getResources().getString(R.string.dialog_button_negative), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        })
                                        .show();
                            }
                        });
                    else contentView.findViewById(R.id.popup_profile_delete).setVisibility(View.GONE);
                    contentView.findViewById(R.id.popup_profile_export).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pw.dismiss();
                            wait_dialog=getWaitDialogInstance();
                            wait_dialog.show();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        Calendar calendar=Calendar.getInstance();
                                        calendar.setTimeInMillis(System.currentTimeMillis());
                                        final String write_path=getExternalFilesDir(null).toString()+"/"
                                                +calendar.get(Calendar.YEAR)+ValueUtils.format(calendar.get(Calendar.MONTH)+1)
                                                +ValueUtils.format(calendar.get(Calendar.DAY_OF_MONTH))+ValueUtils.format(calendar.get(Calendar.HOUR_OF_DAY))
                                                +calendar.get(Calendar.MINUTE)+calendar.get(Calendar.SECOND)+".json";
                                        MySQLiteOpenHelper.saveTable2File(ProfileActivity.this,item.table_name
                                                ,write_path);
                                        Global.handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(wait_dialog!=null) wait_dialog.cancel();
                                                //Snackbar.make(findViewById(android.R.id.content),getResources().getString(R.string.profile_export_complete)+write_path,Snackbar.LENGTH_SHORT).show();
                                                Toast.makeText(ProfileActivity.this,getResources().getString(R.string.profile_export_complete)+write_path,Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }catch (Exception e){
                                        e.printStackTrace();
                                        EnvironmentUtils.showToast(ProfileActivity.this,null,e.toString());
                                    }
                                }
                            }).start();
                        }
                    });
                }
            });
            holder.itemView.setOnLongClickListener(isMultiSelectMode?null:new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(holder.getAdapterPosition()==0) return false;
                    openMultiSelectMode(holder.getAdapterPosition());
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }


        private void openMultiSelectMode(int position){
            isMultiSelectMode=true;
            isSelected=new boolean[list.size()];
            if(position!=0) isSelected[position]=true;
            setMenuItemVisibilityOfMultiSelectMode(true);
            notifyDataSetChanged();
        }
        private void closeMultiSelectMode(){
            isMultiSelectMode=false;
            setMenuItemVisibilityOfMultiSelectMode(false);
            notifyDataSetChanged();
        }
        private void setAllItemsSelected(boolean b){
            for(int i=1;i<isSelected.length;i++){
                isSelected[i]=b;
            }
            notifyDataSetChanged();
        }

        private boolean[] getIsSelected(){
            return isSelected;
        }

        private List<MySQLiteOpenHelper.SqlTableItem> getList(){
            return list;
        }

        private boolean isMultiSelectMode(){
            return isMultiSelectMode;
        }

    }
}
