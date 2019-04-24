package com.github.ghmxr.timeswitch.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.R;

import java.util.ArrayList;
import java.util.List;


public class DialogForAppSelection implements DialogInterface.OnClickListener{

    private AlertDialog dialog;
    private Handler handler=new Handler(Looper.getMainLooper());
    private Context context;
    /**
     * @param selected_package_names package:package:package or -1
     */
    public DialogForAppSelection(final Context context,String title, String selected_package_names){
        dialog=new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(LayoutInflater.from(context).inflate(R.layout.layout_dialog_app_select,null))
                .setPositiveButton(context.getResources().getString(R.string.dialog_button_positive),this)
                .setNegativeButton(context.getResources().getString(R.string.dialog_button_negative),this)
                .create();
        this.context=context;

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
                        //dialog.cancel();
                        View dialogview=LayoutInflater.from(context).inflate(R.layout.layout_dialog_with_recyclerview,null);
                        ((RecyclerView)dialogview.findViewById(R.id.layout_dialog_recyclerview)).setLayoutManager(new GridLayoutManager(context,3));
                        ((RecyclerView)dialogview.findViewById(R.id.layout_dialog_recyclerview)).setAdapter(new AppListAdapter(context,list,new String[1]));
                        /*dialog=new AlertDialog.Builder(context)
                                .setTitle("title")
                        .setView(dialogview)
                                .setPositiveButton(context.getResources().getString(R.string.dialog_button_positive),DialogForAppSelection.this)
                                .setNegativeButton(context.getResources().getString(R.string.dialog_button_negative),DialogForAppSelection.this)
                                .show();*/
                        dialog.findViewById(R.id.dialog_app_wait).setVisibility(View.GONE);
                        ((RecyclerView)dialog.findViewById(R.id.dialog_app_recyclerview)).setLayoutManager(new GridLayoutManager(context,2));
                        ((RecyclerView)dialog.findViewById(R.id.dialog_app_recyclerview)).setAdapter(new AppListAdapter(context,list,new String[1]));
                        dialog.findViewById(R.id.dialog_app_list_area).setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }

    public static class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder>{
        private List<AppItemInfo> list;
        private Context context;
        private boolean isSelected[];
        private String select_color="#553aaf85";

        public AppListAdapter(Context context, List<AppItemInfo> list,String[] selected_package_names){
            this.list=list;
            this.context=context;
            isSelected=new boolean[list.size()];
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
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public void deselectall(){
            isSelected=new boolean[list.size()];
            notifyDataSetChanged();
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
