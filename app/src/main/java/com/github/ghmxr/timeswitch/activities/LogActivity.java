package com.github.ghmxr.timeswitch.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.utils.EnvironmentUtils;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class LogActivity extends BaseActivity {
    ListView mListView;
    SwipeRefreshLayout swr;

    private final List<LogItem> loglist=new ArrayList<>();

    public static final int MESSAGE_REQUEST_REFRESH     =0x20001;

    @Override
    public void onCreate(Bundle myBundle){
        super.onCreate(myBundle);
        setContentView(R.layout.layout_log);
        Toolbar toolbar=findViewById(R.id.log_toolbar);
        setSupportActionBar(toolbar);
        try{getSupportActionBar().setDisplayHomeAsUpEnabled(true);}catch (Exception e){e.printStackTrace();}
        setToolBarAndStatusBarColor(toolbar,getIntent().getStringExtra(EXTRA_TITLE_COLOR));
        swr=findViewById(R.id.log_swipe);
        swr.setColorSchemeResources(R.color.colorPrimary);
        swr.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLogs();
            }
        });
        mListView=findViewById(R.id.log_list);
        refreshLogs();
    }

    private void refreshLogs(){
        swr.setRefreshing(true);
        mListView.setAdapter(null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (loglist){
                    loglist.clear();
                    try{
                        SharedPreferences log=getSharedPreferences(PublicConsts.PREFERENCES_LOGS_NAME, Activity.MODE_PRIVATE);
                        Map<String,?> log_hashmap=log.getAll();
                        List <Object> keylist=Arrays.asList(log_hashmap.keySet().toArray());
                        for(int i=0;i<keylist.size();i++){
                            LogItem item=new LogItem();
                            try{
                                item.log_time=Long.parseLong(keylist.get(i).toString());
                                item.log_value=log_hashmap.get(keylist.get(i).toString()).toString();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            loglist.add(item);
                        }
                        Collections.sort(loglist);
                        myHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                swr.setRefreshing(false);
                                mListView.setAdapter(new LogListAdapter());
                                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        View contentview =LayoutInflater.from(LogActivity.this).inflate(R.layout.layout_log_popup,null);
                                        ((TextView)contentview.findViewById(R.id.log_popup_value)).setText(ValueUtils.getFormatDateTime(loglist.get(i).log_time)+"\n"+loglist.get(i).log_value);
                                        PopupWindow pw=new PopupWindow(contentview, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,true);
                                        pw.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.color_transparent_popup_window)));
                                        pw.setTouchable(true);
                                        pw.setOutsideTouchable(true);
                                        pw.setContentView(contentview);
                                        int[] display_values=ValueUtils.calculatePopWindowPos(view,contentview);
                                        pw.showAtLocation(view, Gravity.TOP|Gravity.START,display_values[0],display_values[1]);
                                    }
                                });
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                        EnvironmentUtils.showToast(LogActivity.this,null,e.toString());
                    }

                }
            }
        }).start();
    }

    public void processMessage(Message msg){
        switch (msg.what){
            default:break;
            case MESSAGE_REQUEST_REFRESH:{
                refreshLogs();
            }
            break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.log,menu);
        setIconEnable(menu,true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            default:break;
            case R.id.actions_log_ascend:{
                LogItem.sortConfig= LogItem.ASCENDING;
                refreshLogs();
            }
            break;
            case R.id.actions_log_descend:{
                LogItem.sortConfig= LogItem.DESCENDING;
                refreshLogs();
            }
            break;
            case R.id.actions_log_clear:{
                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.dialog_log_clear_title))
                        .setMessage(getResources().getString(R.string.dialog_log_clear_message))
                        .setIcon(android.R.drawable.ic_menu_delete)
                        .setPositiveButton(getResources().getString(R.string.dialog_button_positive), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                swr.setRefreshing(true);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        getSharedPreferences(PublicConsts.PREFERENCES_LOGS_NAME,Activity.MODE_PRIVATE).edit().clear().apply();
                                        sendEmptyMessage(MESSAGE_REQUEST_REFRESH);
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
            break;
            case android.R.id.home:{
                finish();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class LogListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return loglist.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(LogActivity.this).inflate(R.layout.item_log,parent,false);
            }
            TextView time=convertView.findViewById(R.id.item_log_time);
            TextView value=convertView.findViewById(R.id.item_log_value);
            time.setText(ValueUtils.getFormatDateTime(loglist.get(position).log_time));
            value.setText(loglist.get(position).log_value);
            return convertView;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

    }

    private static class LogItem implements Comparable<LogItem>{
        private static final int DESCENDING=-1;
        private static final int ASCENDING=1;
        long log_time=0;
        String log_value="";
        private static int sortConfig=-1;
        @Override
        public int compareTo(@NonNull LogItem o) {
            if(sortConfig==ASCENDING){
                if(this.log_time>o.log_time) return 1;
                if(this.log_time<o.log_time) return -1;
                //return (int)(this.log_time-o.log_time);
            }
            else{
                if(this.log_time<o.log_time) return 1;
                if(this.log_time>o.log_time) return -1;
                //return (int)(o.log_time-this.log_time);
            }
            return 0;
        }
    }

}
