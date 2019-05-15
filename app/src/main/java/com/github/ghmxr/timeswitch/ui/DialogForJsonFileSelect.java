package com.github.ghmxr.timeswitch.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ghmxr.timeswitch.Global;
import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.utils.StorageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DialogForJsonFileSelect{
    private Context context;
    private AlertDialog  dialog;
    private final List<File> list=new ArrayList<>();
    private File file=new File(StorageUtil.getExternalStorageDirectory());
    private String current_root_path=StorageUtil.getExternalStorageDirectory();

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private DialogConfirmedListener listener;

    public DialogForJsonFileSelect(Context context){
        dialog=new AlertDialog.Builder(context).setTitle(context.getResources().getString(R.string.dialog_profile_import_title))
        .setView(R.layout.layout_filelist).setPositiveButton(context.getResources().getString(R.string.dialog_button_positive),null)
        .setNegativeButton(context.getResources().getString(R.string.dialog_button_negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        }).create();
        this.context=context;
    }
    Toast att;
    public void show(){
        dialog.show();
        recyclerView=dialog.findViewById(R.id.layout_filelist_recyclerview);
        progressBar=dialog.findViewById(R.id.layout_filelist_progressbar);
        LinearLayoutManager manager=new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        refreshFileListElements();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListAdapter adapter=(ListAdapter) recyclerView.getAdapter();
                if(adapter==null) return;

                boolean[]isSelected=adapter.getIsSelected();
                List<File> files=new ArrayList<>();

                for(int i=0;i<list.size();i++) {
                    File file=list.get(i);
                    if(isSelected[i]&&file.isFile()&&file.getName().endsWith(".json")) files.add(file);
                }

                if(files.size()==0) {
                    if(att!=null) att.cancel();
                    att=Toast.makeText(context,context.getResources().getString(R.string.dialog_profile_attention),Toast.LENGTH_SHORT);
                    att.show();
                    return;
                }
                if(listener!=null) listener.onDialogConfirmed(files);
                dialog.cancel();
            }
        });
    }

    public void setOnDialogConfirmedListener(DialogConfirmedListener listener){
        this.listener=listener;
    }


    private void refreshFileListElements(){
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (list){
                    list.clear();
                    try{
                        if(file.getAbsolutePath().length()<current_root_path.length()) {
                            list.clear();
                            current_root_path=null;
                            List<String> storages=StorageUtil.getAvailableStoragePaths();
                            for(String s:storages)list.add(new File(s));
                        }else{
                            list.add(file.getParentFile());
                            File[]files=file.listFiles();
                            for(File f:files){
                                if(f.isDirectory()||f.getName().endsWith(".json")) list.add(f);
                            }
                        }
                    }catch (Exception e){e.printStackTrace();}

                    Global.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            recyclerView.setAdapter(new ListAdapter());
                        }
                    });
                }
            }
        }).start();


    }

    private class ListAdapter extends RecyclerView.Adapter<ViewHolder>{
        private boolean[] isSelected;
        ListAdapter(){isSelected=new boolean[list.size()];}

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_json_file,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            final File file=list.get(holder.getAdapterPosition());
            if(holder.getAdapterPosition()==0) holder.icon.setImageResource(R.drawable.icon_folder);
            else if(file.isFile()&&file.getName().endsWith(".json")){
                holder.icon.setImageResource(R.drawable.icon_json);
            }else if(file.isFile())holder.icon.setImageResource(R.drawable.icon_file);
            else if(file.isDirectory()) holder.icon.setImageResource(R.drawable.icon_folder);
            holder.file_name.setText(holder.getAdapterPosition()==0&&current_root_path!=null?"..":file.getName());
            holder.arrow.setVisibility(file.isDirectory()?View.VISIBLE:View.GONE);
            holder.cb.setVisibility(file.isDirectory()?View.GONE:View.VISIBLE);
            holder.cb.setChecked(isSelected[holder.getAdapterPosition()]);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(file.isDirectory()){
                        DialogForJsonFileSelect.this.file=file;
                        if(current_root_path==null)current_root_path=file.getAbsolutePath();
                        refreshFileListElements();
                    }else{
                        isSelected[holder.getAdapterPosition()]=!isSelected[holder.getAdapterPosition()];
                        notifyItemChanged(holder.getAdapterPosition());
                    }

                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        private boolean[] getIsSelected(){
            return isSelected;
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView icon;
        TextView file_name;
        CheckBox cb;
        ImageView arrow;

        public ViewHolder(View itemView) {
            super(itemView);
            icon=itemView.findViewById(R.id.item_json_file_img);
            file_name=itemView.findViewById(R.id.item_json_file_name);
            cb=itemView.findViewById(R.id.item_json_file_checkbox);
            arrow=itemView.findViewById(R.id.item_json_file_arrow);
        }
    }

    //private R

    public interface DialogConfirmedListener{
        void onDialogConfirmed(List<File>selected_files);
    }

}
