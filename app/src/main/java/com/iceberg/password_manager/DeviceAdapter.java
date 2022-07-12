package com.iceberg.password_manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

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
            myApprovedTitle = itemView.findViewById(R.id.tvAlreadyApproved);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    android_id = list.get(getAdapterPosition()).getAndroid_id();
                    make = list.get(getAdapterPosition()).getMake();
                    model = list.get(getAdapterPosition()).getModel();
                    nickname = list.get(getAdapterPosition()).getNickname();
                    uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Intent intent =new Intent(mActivity.getApplicationContext(),DeleteDevice.class);
                    intent.putExtra("make",make);
                    intent.putExtra("model",model);
                    intent.putExtra("nickname",nickname);
                    intent.putExtra("aid",android_id);
                    intent.putExtra("uid",uid);
                    intent.putExtra("pk",pk);
                    mContext.startActivity(intent);
                    return true;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(list.get(getAdapterPosition()).isApproved()){
                        // Item is approved
                        // Show pop up for editing
                        String make = list.get(getAdapterPosition()).getMake();
                        String model = list.get(getAdapterPosition()).getModel();
                        String nickname = list.get(getAdapterPosition()).getNickname();
                        ((Devices)mContext).nextInstance(list.get(getAdapterPosition()).getAndroid_id(),"editViewDevice",make,model,nickname);
                    }else{
                        // Item not approved yet
                        // Show pop up for approving
                        if(mContext instanceof Devices){
                            ((Devices)mContext).nextInstance(list.get(getAdapterPosition()).getAndroid_id(),"createApproveOrDeny","","","");
                        }
                    }
                }
            });
        }
    }
}
