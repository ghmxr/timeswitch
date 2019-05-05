package com.github.ghmxr.timeswitch.ui.bottomdialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.TaskItem;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.data.v2.TriggerTypeConsts;

import java.util.List;

public class DialogForTaskSelection implements DialogInterface.OnClickListener{
    private DialogConfirmedCallBack callBack;
    private AlertDialog dialog;
    /**
     * @param ids id:id:id
     */
    public DialogForTaskSelection(Context context,String title,List<TaskItem>list,String ids,@Nullable String selectable_color) {

        View dialogView=LayoutInflater.from(context).inflate(R.layout.layout_dialog_with_recyclerview,null);
        dialog=new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(context.getResources().getString(R.string.dialog_button_positive),this)
                .setNegativeButton(context.getResources().getString(R.string.dialog_button_negative),this)
                .setNeutralButton(context.getResources().getString(R.string.action_deselectall),null)
                .create();

        ((RecyclerView)dialogView.findViewById(R.id.layout_dialog_recyclerview)).setLayoutManager(new GridLayoutManager(context,2));
        ((RecyclerView)dialogView.findViewById(R.id.layout_dialog_recyclerview)).setAdapter(new MiniTaskListAdapter(context,list,ids.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL),selectable_color));

    }

    public void show(){
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MiniTaskListAdapter)((RecyclerView)dialog.findViewById(R.id.layout_dialog_recyclerview)).getAdapter()).deselectAll();
            }
        });
    }

    @Override
    public void onClick(DialogInterface d, int which) {
        switch (which){
            case AlertDialog.BUTTON_POSITIVE:{
                if(callBack!=null) callBack.onDialogConfirmed(((MiniTaskListAdapter)((RecyclerView)dialog.findViewById(R.id.layout_dialog_recyclerview)).getAdapter()).getSelectedIds());
                dialog.cancel();
            }
            break;
            case AlertDialog.BUTTON_NEGATIVE:dialog.cancel();break;
        }
    }

    public void setOnDialogConfirmedCallback(DialogConfirmedCallBack callback){
        this.callBack=callback;
    }

    public static class MiniTaskListAdapter extends RecyclerView.Adapter<MiniTaskListAdapter.ViewHolder> {
        private Context context;
        private List<TaskItem> list;
        private boolean[] isSelected;
        private String selectable_color="#553aaf85";

        public MiniTaskListAdapter(Context context, List<TaskItem> list,String[] selected_ids,@Nullable String selected_color){
            this.context=context;
            this.list=list;
            isSelected=new boolean[list.size()];
            if(selected_color!=null) this.selectable_color=selected_color;
            for(int i=0;i<list.size();i++){
                for(String s:selected_ids) if(Integer.parseInt(s)==list.get(i).id) {isSelected[i]=true;break;}
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_dialog_task,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            TaskItem item=list.get(position);
            int imgRes=R.drawable.ic_launcher;
            Resources resources=context.getResources();
            switch (item.trigger_type){
                default:break;
                case TriggerTypeConsts.TRIGGER_TYPE_SINGLE:imgRes=R.drawable.icon_repeat_single;break;
                case TriggerTypeConsts.TRIGGER_TYPE_LOOP_BY_CERTAIN_TIME:imgRes=R.drawable.icon_repeat_percertaintime;break;
                case TriggerTypeConsts.TRIGGER_TYPE_LOOP_WEEK:imgRes=R.drawable.icon_repeat_weekloop;break;
                case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_MORE_THAN_PERCENTAGE:imgRes=R.drawable.icon_battery_high;break;
                case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LESS_THAN_PERCENTAGE:imgRes=R.drawable.icon_battery_low;break;
                case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_HIGHER_THAN_TEMPERATURE: case TriggerTypeConsts.TRIGGER_TYPE_BATTERY_LOWER_THAN_TEMPERATURE:
                    imgRes=R.drawable.icon_temperature;break;
                case TriggerTypeConsts.TRIGGER_TYPE_RECEIVED_BROADCAST:imgRes=R.drawable.icon_broadcast;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_ON:imgRes=R.drawable.icon_wifi_on;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_WIFI_OFF:imgRes=R.drawable.icon_wifi_off;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_ON:imgRes=R.drawable.icon_bluetooth_on;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_BLUETOOTH_OFF:imgRes=R.drawable.icon_bluetooth_off;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_OFF:imgRes=R.drawable.icon_ring_off;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_MODE_VIBRATE:imgRes=R.drawable.icon_ring_vibrate;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_RING_NORMAL:imgRes=R.drawable.icon_ring_normal;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_ON:imgRes=R.drawable.icon_airplanemode_on;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AIRPLANE_MODE_OFF:imgRes=R.drawable.icon_airplanemode_off;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_ENABLED:imgRes=R.drawable.icon_ap_on;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIDGET_AP_DISABLED:imgRes=R.drawable.icon_ap_off;break;
                case TriggerTypeConsts.TRIGGER_TYPE_NET_ON:imgRes=R.drawable.icon_cellular_on;break;
                case TriggerTypeConsts.TRIGGER_TYPE_NET_OFF:imgRes=R.drawable.icon_cellular_off;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIFI_CONNECTED:imgRes=R.drawable.icon_wifi_connected;break;
                case TriggerTypeConsts.TRIGGER_TYPE_WIFI_DISCONNECTED:imgRes=R.drawable.icon_wifi_disconnected;break;
                case TriggerTypeConsts.TRIGGER_TYPE_SCREEN_ON:imgRes=R.drawable.icon_screen_on;break;
                case TriggerTypeConsts.TRIGGER_TYPE_SCREEN_OFF:imgRes=R.drawable.icon_screen_off;break;
                case TriggerTypeConsts.TRIGGER_TYPE_POWER_CONNECTED:imgRes=R.drawable.icon_power_connected;break;
                case TriggerTypeConsts.TRIGGER_TYPE_POWER_DISCONNECTED:imgRes=R.drawable.icon_power_disconnected;break;
                case TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_IN: case TriggerTypeConsts.TRIGGER_TYPE_HEADSET_PLUG_OUT:{
                    imgRes=R.drawable.icon_headset;
                }
                break;
                case TriggerTypeConsts.TRIGGER_TYPE_APP_LAUNCHED:imgRes=R.drawable.icon_app_launch;break;
                case TriggerTypeConsts.TRIGGER_TYPE_APP_CLOSED:imgRes=R.drawable.icon_app_stop;break;
            }
            holder.icon.setImageResource(imgRes);
            holder.title.setText(item.name);
            holder.title.setTextColor(Color.parseColor(item.addition_title_color));
            holder.description.setText(item.isenabled?resources.getString(R.string.opened):
                    resources.getString(R.string.closed));
            holder.description.setTextColor(item.isenabled?resources.getColor(R.color.color_task_enabled_font):resources.getColor(R.color.color_task_disabled_font));
            //holder.cb.setChecked(isSelected[position]);
            holder.itemView.setBackgroundColor(isSelected[holder.getAdapterPosition()]?Color.parseColor(selectable_color):Color.parseColor("#00000000"));
            holder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isSelected[holder.getAdapterPosition()]=!isSelected[holder.getAdapterPosition()];
                    notifyItemChanged(holder.getAdapterPosition());
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public void deselectAll(){
            isSelected=new boolean[list.size()];
            notifyDataSetChanged();
        }

        /**
         * @return id:id:id,or -1
         */
        public String getSelectedIds(){
            StringBuilder builder=new StringBuilder("");
            for(int i=0;i<isSelected.length;i++){
                if(isSelected[i]) {
                    builder.append(list.get(i).id);
                    builder.append(PublicConsts.SEPARATOR_SECOND_LEVEL);
                }
            }
            if(builder.toString().equals("")) return String.valueOf(-1);
            if(builder.toString().endsWith(PublicConsts.SEPARATOR_SECOND_LEVEL)) builder.deleteCharAt(builder.lastIndexOf(PublicConsts.SEPARATOR_SECOND_LEVEL));
            return builder.toString();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder{
            ImageView icon;
            TextView title,description;
            //CheckBox cb;

            public ViewHolder(View itemView) {
                super(itemView);
                icon=itemView.findViewById(R.id.item_dialog_task_img);
                title=itemView.findViewById(R.id.item_dialog_task_name);
                description=itemView.findViewById(R.id.item_dialog_task_name_description);
                //cb=itemView.findViewById(R.id.item_dialog_task_cb);
            }

            public void setOnClickListener(View.OnClickListener listener){
                itemView.setOnClickListener(listener);
            }
        }
    }
}
