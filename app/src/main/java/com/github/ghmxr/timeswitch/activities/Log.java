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
import com.github.ghmxr.timeswitch.utils.ValueUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class Log extends BaseActivity {
    //public static LinkedList<Log> log_queue=new LinkedList<>();
    //public static MyHandler handler;
    ListView mListView;
    LogListAdapter adapter=new LogListAdapter();
    SwipeRefreshLayout swr;

    List<LogItem> loglist=new ArrayList<>();
    int sortConfig=-1;

    Thread refreshThread;
    RefreshLogItems runnable;

    public static final int MESSAGE_REQUEST_UPDATE=0x20000;
    public static final int MESSAGE_REQUEST_REFRESH     =0x20001;

    public static final int ASCENDING=1;
    public static final int DESCENDING=-1;

    @Override
    public void onCreate(Bundle myBundle){
        super.onCreate(myBundle);
        //if(!log_queue.contains(this)) log_queue.add(this);
        //handler=new MyHandler();
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
        mListView.setAdapter(adapter);
        refreshLogs();
    }

    private synchronized void refreshLogs(){
        if(runnable!=null){
            runnable.isInterrupted=true;
            runnable=null;
        }
        runnable=new RefreshLogItems();
        refreshThread=new Thread(runnable);
        refreshThread.start();
        swr.setRefreshing(true);
    }

    /*public static void sendMessage(Message msg){
        if(handler!=null) handler.sendMessage(msg);
    }

    public static void sendEmptyMessage(int what){
        if(handler!=null) handler.sendEmptyMessage(what);
    }  */

    public void processMessage(Message msg){
        switch (msg.what){
            default:break;
            case MESSAGE_REQUEST_UPDATE:{
                swr.setRefreshing(false);
                adapter.notifyDataSetChanged();
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        View contentview =LayoutInflater.from(Log.this).inflate(R.layout.layout_log_popup,null);
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
            break;
            case MESSAGE_REQUEST_REFRESH:{
                refreshLogs();
            }
            break;
        }
    }

    @Override
    public void finish(){
        super.finish();
        //if(log_queue.contains(this)) log_queue.remove(this);
        //handler=null;
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
                sortConfig=ASCENDING;
                sendEmptyMessage(MESSAGE_REQUEST_REFRESH);
            }
            break;
            case R.id.actions_log_descend:{
                sortConfig=DESCENDING;
                sendEmptyMessage(MESSAGE_REQUEST_REFRESH);
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
                                new Thread(new ClearLog()).start();
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
            if(loglist==null) return 0;
            return loglist.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(Log.this).inflate(R.layout.item_log,parent,false);
            }
            TextView time=convertView.findViewById(R.id.item_log_time);
            TextView value=convertView.findViewById(R.id.item_log_value);
            time.setText(ValueUtils.getFormatDateTime(loglist.get(position).log_time));
            String display_value=loglist.get(position).log_value;
            if(display_value.length()>50) display_value=display_value.substring(0,50)+"...";
            value.setText(display_value);
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

    private class LogItem implements Comparable<LogItem>{
        long log_time=0;
        String log_value="";

        @Override
        public int compareTo(@NonNull LogItem o) {
            if(sortConfig>=0){
                return (int)(this.log_time-o.log_time);
            }
            else{
                return (int)(o.log_time-this.log_time);
            }
        }
    }

    private class RefreshLogItems implements Runnable{
        boolean isInterrupted=false;
        @Override
        public void run(){
            List<LogItem> cachelist=new ArrayList<>();
            SharedPreferences log=getSharedPreferences(PublicConsts.PREFERENCES_LOGS_NAME, Activity.MODE_PRIVATE);
            Map<String,?> log_hashmap=log.getAll();
            List <Object> keylist=Arrays.asList(log_hashmap.keySet().toArray());
            for(int i=0;i<keylist.size();i++){
                if(isInterrupted) break;
                LogItem item=new LogItem();
                try{
                    item.log_time=Long.parseLong(keylist.get(i).toString());
                    item.log_value=log_hashmap.get(keylist.get(i).toString()).toString();
                }catch (Exception e){
                    e.printStackTrace();
                }
                cachelist.add(item);
            }
            Collections.sort(cachelist);
            if(!isInterrupted) {
                loglist=cachelist;
                sendEmptyMessage(MESSAGE_REQUEST_UPDATE);
            }
        }
    }

    private class ClearLog implements Runnable{
        //boolean isInterrupted=false;
        @Override
        public void run(){
            SharedPreferences log=getSharedPreferences(PublicConsts.PREFERENCES_LOGS_NAME,Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor=log.edit();
            editor.clear();
            editor.apply();
            sendEmptyMessage(MESSAGE_REQUEST_REFRESH);
        }
    }

    /*private static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try{
                if(log_queue.size()>0) log_queue.getLast().processMessage(msg);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }  */

}
