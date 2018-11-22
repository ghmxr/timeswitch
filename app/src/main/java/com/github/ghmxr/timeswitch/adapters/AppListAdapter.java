package com.github.ghmxr.timeswitch.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.R;

import java.util.List;

public class AppListAdapter extends BaseAdapter{
    List<AppItemInfo> list;
    Context context;
    boolean[] isSelected;

    public AppListAdapter(Context context,List<AppItemInfo> list, String[] selectedPackageNames){
        this.context=context;
        this.list=list;
        isSelected=new boolean[list.size()];
        if(selectedPackageNames==null||selectedPackageNames.length==0) return;
        for(String name:selectedPackageNames){
            if(name==null) continue;
            for(int i=0;i<list.size();i++){
                if(name.equals(list.get(i).package_name)){
                    isSelected[i]=true;
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
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView==null){
            convertView= LayoutInflater.from(context).inflate(R.layout.item_app_info,parent,false);
            holder=new ViewHolder();
            holder.icon=convertView.findViewById(R.id.item_app_icon);
            holder.tv_name=convertView.findViewById(R.id.item_app_name);
            holder.cb=convertView.findViewById(R.id.item_app_cb);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder) convertView.getTag();
        }
        holder.icon.setImageDrawable(list.get(position).icon);
        holder.tv_name.setText(list.get(position).appname);
        holder.cb.setChecked(isSelected[position]);
        return convertView;
    }

    public void onItemClicked(int position){
        if(position<0||position>=isSelected.length) return;
        isSelected[position]=!isSelected[position];
        notifyDataSetChanged();
    }

    public void deselectAll(){
        for(int i=0;i<isSelected.length;i++){
            isSelected[i]=false;
        }
        notifyDataSetChanged();
    }

    public String[] getSelectedPackageNames(){
        int selectedNum=0;
        for(int i=0;i<isSelected.length;i++){
            if(isSelected[i]) selectedNum++;
        }
        String[] names=new String[selectedNum];
        int j=0;
        for(int i=0;i<isSelected.length;i++){
            if(isSelected[i]) {
                names[j]=list.get(i).package_name;
                j++;
            }
        }
        return names;
    }

    private class ViewHolder{
        ImageView icon;
        TextView tv_name;
        CheckBox cb;
    }

    public static class AppItemInfo{
        public Drawable icon;
        public String appname="";
        public String package_name="";
    }
}

