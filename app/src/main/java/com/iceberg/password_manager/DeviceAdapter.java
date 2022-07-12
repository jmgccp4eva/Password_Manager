package com.iceberg.password_manager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private String android_id,make,model,nickname,uid;
    LayoutInflater layoutInflater;
    List<Device> list;
    Activity mActivity;
    private Context mContext;
    private byte[] pk;

    public DeviceAdapter(Context context,List<Device> list, Activity mActivity, byte[] pk){
        this.layoutInflater =LayoutInflater.from(context);
        this.mContext = context;
        this.list = list;
        this.mActivity = mActivity;
        this.pk = pk;
    }

    @NonNull
    @Override
    public DeviceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =layoutInflater.inflate(R.layout.custom_list_view,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() { return list.size(); }

    @Override
    public void onBindViewHolder(@NonNull DeviceAdapter.ViewHolder holder, int position) {
        if(list!=null && list.size()>0){
            String title;
            if(list.get(position).getNickname().equals("~")){
                title = list.get(position).getMake()+" "+list.get(position).getModel();
            }else{
                title = list.get(position).getNickname();
            }
            holder.myApprovedTitle.setText(title);
            if(list.get(position).isApproved()){
                holder.myApprovedTitle.setTextColor(Color.BLACK);
            }else{
                holder.myApprovedTitle.setTextColor(Color.RED);
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView myApprovedTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

        }
    }
}
