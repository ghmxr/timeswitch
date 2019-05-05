package com.github.ghmxr.timeswitch.ui.bottomdialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;

import java.util.ArrayList;
import java.util.List;


public class DialogForAppSelection implements DialogInterface.OnClickListener{

    private AlertDialog dialog;
    private Handler handler=new Handler(Looper.getMainLooper());
    private Context context;
    private DialogConfirmedCallBack callBack;
    private final String []selected_package_names;
    private final String selectable_color;
    private final String att;
    /**
     * @param selected_package_names package:package:package or -1
     */
    public DialogForAppSelection(final Context context,String title, String selected_package_names,String selectable_color,String att){
        this(context,title,selected_package_names.split(PublicConsts.SPLIT_SEPARATOR_SECOND_LEVEL),selectable_color,att);
    }

    public DialogForAppSelection(final Context context,String title,String[] selected_package_names,String selectable_color,String att){
        dialog=new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(LayoutInflater.from(context).inflate(R.layout.layout_dialog_app_select,null))
                .setPositiveButton(context.getResources().getString(R.string.dialog_button_positive),null)
                .setNegativeButton(context.getResources().getString(R.string.dialog_button_negative),this)
                .setNeutralButton(context.getResources().getString(R.string.action_deselectall),null)
                .create();
        this.context=context;
        this.selected_package_names=selected_package_names;
        this.selectable_color=selectable_color;
        this.att=att;
    }

    public void show(){
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<AppItemInfo> list=new ArrayList<>();
                PackageManager manager=context.getPackageManager();
                List<PackageInfo> list1=manager.getInstalledPackages(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
                for(PackageInfo info:list1){
                    AppItemInfo itemInfo=new AppItemInfo();
                    itemInfo.drawable=manager.getApplicationIcon(info.applicationInfo);
                    itemInfo.package_name=info.applicationInfo.packageName;
                    itemInfo.title=manager.getApplicationLabel(info.applicationInfo).toString();
                    list.add(itemInfo);
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.findViewById(R.id.dialog_app_wait).setVisibility(View.GONE);
                        ((RecyclerView)dialog.findViewById(R.id.dialog_app_recyclerview)).setLayoutManager(new GridLayoutManager(context,2));
                        ((RecyclerView)dialog.findViewById(R.id.dialog_app_recyclerview)).setAdapter(new AppListAdapter(context,list,selected_package_names,selectable_color));
                        dialog.findViewById(R.id.dialog_app_list_area).setVisibility(View.VISIBLE);
                        ((TextView)dialog.findViewById(R.id.dialog_app_att)).setText(att);
                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((AppListAdapter)((RecyclerView)dialog.findViewById(R.id.dialog_app_recyclerview)).getAdapter()).deselectall();
                            }
                        });
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(callBack!=null) {
                                    AppListAdapter adapter=(AppListAdapter)((RecyclerView)dialog.findViewById(R.id.dialog_app_recyclerview)).getAdapter();
                                    if(adapter==null) return;
                                    callBack.onDialogConfirmed(adapter.getSelectedPackageNames());
                                    dialog.cancel();
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }

    @Override
    public void onClick(DialogInterface d, int which) {}

    public void setOnDialogConfirmedCallBack(DialogConfirmedCallBack callBack){
        this.callBack=callBack;
    }

    public static class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder>{
        private List<AppItemInfo> list;
        private Context context;
        private boolean isSelected[];
        private String select_color="#553aaf85";

        public AppListAdapter(Context context, List<AppItemInfo> list,String[] selected_package_names,@Nullable String selectable_color){
            this.list=list;
            this.context=context;
            isSelected=new boolean[list.size()];
            for(String s:selected_package_names){
                for(int i=0;i<isSelected.length;i++) if (list.get(i).package_name.equals(s)) {isSelected[i]=true;break;}
            }
            if(selectable_color!=null) select_color=selectable_color;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_app_info,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            holder.icon.setImageDrawable(list.get(holder.getAdapterPosition()).drawable);
            holder.tv_name.setText(list.get(holder.getAdapterPosition()).title);
            holder.itemView.setBackgroundColor(isSelected[holder.getAdapterPosition()]? Color.parseColor(select_color):Color.parseColor("#00000000"));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isSelected[holder.getAdapterPosition()]=!isSelected[holder.getAdapterPosition()];
                    notifyItemChanged(holder.getAdapterPosition());
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    try{
                        Intent intent=context.getPackageManager().getLaunchIntentForPackage(list.get(holder.getAdapterPosition()).package_name);
                        context.startActivity(intent);
                        return true;
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public void deselectall(){
            isSelected=new boolean[list.size()];
            notifyDataSetChanged();
        }

        public String getSelectedPackageNames(){
            StringBuilder builder=new StringBuilder("");
            for(int i=0;i<isSelected.length;i++){
                if(isSelected[i]) {
                    builder.append(list.get(i).package_name);
                    builder.append(":");
                }
            }
            if(builder.toString().equals("")) return String.valueOf(-1);
            if(builder.toString().endsWith(":")) builder.deleteCharAt(builder.lastIndexOf(":"));
            return builder.toString();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder{
            public ImageView icon;
            public TextView tv_name;
            public ViewHolder(View itemView) {
                super(itemView);
                icon=itemView.findViewById(R.id.item_app_icon);
                tv_name=itemView.findViewById(R.id.item_app_name);
            }
        }
    }

    public static class AppItemInfo{
        Drawable drawable;
        String package_name,title;
    }

}
