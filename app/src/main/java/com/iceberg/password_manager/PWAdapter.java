package com.iceberg.password_manager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class PWAdapter extends RecyclerView.Adapter<PWAdapter.PWViewHolder> {

    LayoutInflater layoutInflater;
    List<Password> list;
    Activity mActivity;
    private Context mContext;
    private byte[] pk;

    public PWAdapter(Context context,List<Password> list, Activity mActivity, byte[] pk) {
        this.layoutInflater =LayoutInflater.from(context);
        this.list = list;
        this.mActivity = mActivity;
        this.mContext = context;
        this.pk = pk;
    }

    @NonNull
    @Override
    public PWAdapter.PWViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view =layoutInflater.inflate(R.layout.custom_list_view,parent,false);
        return new PWAdapter.PWViewHolder(view);
    }

    @Override
    public int getItemCount() { return list.size(); }

    @Override
    public void onBindViewHolder(@NonNull PWAdapter.PWViewHolder holder, int position) {
        if(list!=null && list.size()>0){
            String title = list.get(position).getpwID();
            holder.myApprovedTitle.setText(title);
            holder.myApprovedTitle.setTextColor(Color.BLACK);
        }
    }

    public class PWViewHolder extends RecyclerView.ViewHolder{

        TextView myApprovedTitle;

        public PWViewHolder(@NonNull View itemView) {
            super(itemView);
            myApprovedTitle = itemView.findViewById(R.id.tvAlreadyApproved);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });

            itemView.setOnClickListener(v -> {
                String email = list.get(getAdapterPosition()).getEmail();
                String password = list.get(getAdapterPosition()).getPassword();
                String itemName = list.get(getAdapterPosition()).getItemName();
                ((Passwords)mContext).nextInstance(list.get(getAdapterPosition()).getpwID(),itemName,email,password,pk);
            });
        }
    }
}
