package com.github.ghmxr.timeswitch.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.Global;
import com.github.ghmxr.timeswitch.R;

import java.util.ArrayList;
import java.util.List;

public class DialogForWifiInfoSelection {

    private Context context;
    private AlertDialog dialog;
    private ListAdapter adapter;
    private List<WifiConfiguration>list=new ArrayList<>();
    private DialogConfirmedListener listener;
    public interface DialogConfirmedListener {
        void onDialogConfirmed(int [] ids);
    }


    public DialogForWifiInfoSelection(Context context,@NonNull int[]selectedIDs){
        this.context=context;
        list.addAll(Global.NetworkReceiver.wifiList2);
        this.dialog=new AlertDialog.Builder(context).setTitle(context.getResources().getString(R.string.activity_trigger_wifi_dialog_att))
                .setView(LayoutInflater.from(context).inflate(R.layout.layout_dialog_with_listview,null))
                .setPositiveButton(context.getResources().getString(R.string.dialog_button_positive),null)
                .setNegativeButton(context.getResources().getString(R.string.dialog_button_negative), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                })
                .create();
        adapter=new ListAdapter(selectedIDs);
    }

    public void show(){
        dialog.show();
        ListView listView=dialog.findViewById(R.id.layout_dialog_listview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.onItemClicked(position);
            }
        });
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener==null)return;
                List<Integer> list=new ArrayList<>();
                boolean [] isSelected=adapter.isSelected;
                for(int i=0;i<isSelected.length;i++){
                    if(isSelected[i]){
                        list.add(DialogForWifiInfoSelection.this.list.get(i).networkId);
                    }
                }
                int []a=new int[list.size()];
                for(int i=0;i<a.length;i++){
                    a[i]=list.get(i);
                }
                listener.onDialogConfirmed(a);
                dialog.cancel();
            }
        });
    }

    public void setOnDialogConfirmedListener(DialogConfirmedListener listener){
        this.listener=listener;
    }

    private class ListAdapter extends BaseAdapter{
        private boolean [] isSelected;

        ListAdapter(@NonNull int[] selectedIDs){
            isSelected=new boolean[list.size()];
            for(int i=0;i<list.size();i++){
                for(int id:selectedIDs){
                    if(list.get(i).networkId==id){
                        isSelected[i]=true;
                        break;
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if(view==null){
                view= LayoutInflater.from(context).inflate(R.layout.item_wifiinfo,parent,false);
            }

            ((TextView)view.findViewById(R.id.item_wifiinfo_ssid)).setText(list.get(position).SSID);
            ((CheckBox)view.findViewById(R.id.item_wifiinfo_cb)).setChecked(isSelected[position]);

            return view;
        }

        void onItemClicked(int position){
            if(position<0||position>=list.size())return;
            isSelected[position]=!isSelected[position];
            notifyDataSetChanged();
        }
    }
}
