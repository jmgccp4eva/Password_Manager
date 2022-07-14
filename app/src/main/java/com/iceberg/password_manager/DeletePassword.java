package com.iceberg.password_manager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DeletePassword extends AppCompatActivity implements View.OnClickListener {

    private String pwID,uid,itemName,password,email,dpwmsg;
    private byte[] pk;
    private TextView tvMsg;
    private Button btnDel;
    private ImageButton ibCancelDPW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_password);

        Intent oi = getIntent();
        pk = oi.getByteArrayExtra("pk");
        pwID = oi.getStringExtra("pwID");
        uid = oi.getStringExtra("uid");
        itemName = oi.getStringExtra("itemName");
        password = oi.getStringExtra("password");
        email = oi.getStringExtra("email");

        dpwmsg = "You are about to delete the record for " + pwID + ".\n\nAre you sure you want to delete this record?";

        btnDel = findViewById(R.id.btnDelPW);
        tvMsg = findViewById(R.id.tvMsgDD);
        ibCancelDPW = findViewById(R.id.ibCancelDPW);

        tvMsg.setText(dpwmsg);

        btnDel.setOnClickListener(view -> FirebaseFirestore.getInstance().collection("users")
            .document(uid).collection("words")
            .document(pwID).delete()
            .addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Record successfully deleted",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(DeletePassword.this,PasswordListBuilder.class);
                    intent.putExtra("uid",uid);
                    intent.putExtra("pk",pk);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |  Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(),"Failed to delete record",Toast.LENGTH_LONG).show();
                }
            }));
        ibCancelDPW.setOnClickListener(view -> finish());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.ibCancelDPW:
                finish();
                break;
            case R.id.btnDelPW:
//                FirebaseFirestore.getInstance().collection("users")
//                        .document(uid).collection("words")
//                        .document(pwID).delete()
//                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if(task.isSuccessful()){
//                                    Toast.makeText(getApplicationContext(),"Successfully deleted record",Toast.LENGTH_LONG).show();
//                                    Intent intent = new Intent(DeletePassword.this,PasswordListBuilder.class);
//                                    intent.putExtra("uid",uid);
//                                    intent.putExtra("pk",pk);
//                                    startActivity(intent);
//                                    finish();
//                                }else{
//                                    Toast.makeText(getApplicationContext(),"Failed to delete record",Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        });
                Toast.makeText(getApplicationContext(),uid,Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(),pwID,Toast.LENGTH_LONG).show();
                break;
        }
    }
}