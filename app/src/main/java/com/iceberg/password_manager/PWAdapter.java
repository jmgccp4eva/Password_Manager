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

public class PWAdapter extends RecyclerView.Adapter<PWAdapter.ViewHolder> {

    LayoutInflater layoutInflater;
    List<Password> list;
    Activity mActivity;
    private Context mContext;
    private byte[] pk;

    public PWAdapter(Context context,List<Password> list, Activity mActivity, byte[] pk) {
        this.list = list;
        this.mActivity = mActivity;
        this.mContext = mContext;
        this.pk = pk;
    }

    @NonNull
    @Override
    public PWAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view =layoutInflater.inflate(R.layout.custom_list_view,parent,false);
        return new PWAdapter.ViewHolder(view);
    }

    @Override
    public int getItemCount() { return list.size(); }

    @Override
    public void onBindViewHolder(@NonNull PWAdapter.ViewHolder holder, int position) {
        if(list!=null && list.size()>0){
            String title;
            title = list.get(position).getItemName();
            holder.myApprovedTitle.setText(title);
            holder.myApprovedTitle.setTextColor(Color.BLACK);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView myApprovedTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            myApprovedTitle = itemView.findViewById(R.id.tvAlreadyApproved);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = list.get(getAdapterPosition()).getEmail();
                    String password = list.get(getAdapterPosition()).getPassword();
                    String itemName = list.get(getAdapterPosition()).getItemName();
                    ((Passwords)mContext).nextInstance(list.get(getAdapterPosition()).getpwID(),itemName,email,password,pk);
                }
            });
        }
    }
}
