package com.iceberg.password_manager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ApproveOrDenyDevice extends AppCompatActivity implements View.OnClickListener {

    private static final String PKFN = "infor.pem";
    private Button btnApprove, btnDeny;
    private ImageButton ibCancelADD;
    private TextView tvMessage;
    private byte[] pk;
    private String uid,aid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_or_deny_device);

        tvMessage = findViewById(R.id.tvApproveOrDeny);
        btnApprove = findViewById(R.id.btnApproveDevice);
        btnDeny = findViewById(R.id.btnDenyDevice);
        ibCancelADD = findViewById(R.id.ibCancelADD);

        Intent incoming = getIntent();
        pk = incoming.getByteArrayExtra("pk");
        uid = incoming.getStringExtra("uid");
        aid = incoming.getStringExtra("aid");

        ibCancelADD.setOnClickListener(this);
        btnApprove.setOnClickListener(this);
        btnDeny.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.ibCancelADD:
                moveOn(uid,pk);
                break;
            case R.id.btnApproveDevice:
                Blob blob = Blob.fromBytes(pk);
                FirebaseFirestore.getInstance().collection("users")
                        .document(uid).collection("devices")
                        .document(aid).update("extraData",blob,"approved",true)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(),"Device approved!",Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(ApproveOrDenyDevice.this,DeviceListBuilder.class);
                                    intent.putExtra("uid",uid);
                                    intent.putExtra("pk",pk);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                }else{
                                    Toast.makeText(getApplicationContext(),"Something went wrong.  Device update failed.",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                break;
            case R.id.btnDenyDevice:
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                DocumentReference dr = firebaseFirestore.collection("users")
                        .document(uid).collection("devices")
                        .document(aid);
                dr.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(),"Device denied successfully",Toast.LENGTH_LONG).show();
                            moveOn(uid,pk);
                        }else{
                            Toast.makeText(getApplicationContext(),"There was a problem removing this device",Toast.LENGTH_LONG).show();
                        }
                    }
                });
                break;
        }
    }

    void moveOn(String uid, byte[] pk){
        Intent i = new Intent(ApproveOrDenyDevice.this,DeviceListBuilder.class);
        i.putExtra("uid",uid);
        i.putExtra("pk",pk);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |  Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}