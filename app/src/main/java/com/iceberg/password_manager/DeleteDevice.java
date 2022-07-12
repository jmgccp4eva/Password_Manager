package com.iceberg.password_manager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.google.common.io.Files;

public class DeleteDevice extends AppCompatActivity implements View.OnClickListener {

    private static final String PKFN = "infor.pem";
    private boolean lastDevice, sameDevice;
    private String android_id,make,model,uid,nickname, ddmsg, ddsdmsg, ddldmsg;
    private byte[] pk;
    private TextView tvSameDevice, tvLastDevice, tvMessage;
    private Button btnCancelDeleteDevice, btnDeleteDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_device);

        Intent oi = getIntent();
        android_id = oi.getStringExtra("aid");
        make = oi.getStringExtra("make");
        model = oi.getStringExtra("model");
        uid = oi.getStringExtra("uid");
        nickname = oi.getStringExtra("nickname");
        lastDevice = oi.getBooleanExtra("lastDevice",false);
        sameDevice = oi.getBooleanExtra("sameDevice",false);
        pk = oi.getByteArrayExtra("pk");

        if(nickname.equals("") || nickname.equals("~")){
            ddmsg = "You are about to delete "+make+" "+model+".\n\nAre you sure you want to delete this devices?";
        }else{
            ddmsg = "You are about to delete "+nickname+".\n\nAre you sure you want to delete this devices?";
        }

        tvLastDevice = findViewById(R.id.tvLastDevice);
        tvSameDevice = findViewById(R.id.tvSameDevice);
        tvMessage = findViewById(R.id.tvMessage);
        btnCancelDeleteDevice = findViewById(R.id.btnCancelDeleteDevice);
        btnDeleteDevice = findViewById(R.id.btnDeleteDevice);

        tvMessage.setText(ddmsg);

        if(sameDevice){
            ddsdmsg = "Warning: Deleting this device will remove the device and log you out of the system!";
            tvSameDevice.setText(ddsdmsg);
            tvSameDevice.setVisibility(View.VISIBLE);
        }else{
            ddsdmsg = "";
            tvSameDevice.setText(ddsdmsg);
            tvSameDevice.setVisibility(View.GONE);
        }

        if(lastDevice){
            ddldmsg = "Warning: This is the only device you have registered.  Removing it will remove your account.";
            tvLastDevice.setText(ddldmsg);
            tvLastDevice.setVisibility(View.VISIBLE);
        }else{
            ddldmsg = "";
            tvLastDevice.setText(ddldmsg);
            tvLastDevice.setVisibility(View.GONE);
        }

        btnDeleteDevice.setOnClickListener(this);
        btnCancelDeleteDevice.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnCancelDeleteDevice:
                Intent intent = new Intent(DeleteDevice.this,DeviceListBuilder.class);
                intent.putExtra("pk",pk);
                intent.putExtra("uid",uid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |  Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
            case R.id.btnDeleteDevice:
                if(sameDevice){
                    if(lastDevice){
                        // last Device
                        // delete PKFN
                        // delete entry in FF for this user
                        // delete entry in auth for this user
                        // log out user & return to Sign
                        deleteSameAndLast();
                    }else{
                        // NOT last Device
                        // delete PKFN
                        // delete entry in FF for this device
                        // log out user & return to Sign In
                        deleteSameButNotLast();
                    }
                }else{
                    // Different device, therefore NOT last device
                    // delete entry in FF for this device
                    // Go to DeviceListBuilder, clearing history
                    deleteDifferentDevice();
                }
                break;
        }
    }

    private void deleteSameAndLast() {
        File file = new File(getFilesDir(),PKFN);
        if(file.delete()){
            FirebaseFirestore.getInstance().collection("users")
                    .document(uid).collection("devices")
                    .document(android_id).delete().addOnCompleteListener(task -> {
                        // Get list of 'words' for this user
                        // iterate through list and delete them all
                        // then do the rest
                        if(task.isSuccessful()){
                            FirebaseFirestore.getInstance().collection("users")
                                    .document(uid).delete().addOnCompleteListener(task1 -> {
                                        if(task1.isSuccessful()){
                                            FirebaseAuth.getInstance().getCurrentUser().delete()
                                                    .addOnCompleteListener(task11 -> {
                                                        if(task11.isSuccessful()){
                                                            FirebaseAuth.getInstance().signOut();
                                                            Intent intent = new Intent(DeleteDevice.this,SignIn.class);
                                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |  Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(intent);
                                                            finish();
                                                        }else{
                                                            try {
                                                                Files.write(pk, new File(getFilesDir(),PKFN));
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }
                                                            byte[] bytes = new byte[0];
                                                            Blob blob = Blob.fromBytes(bytes);
                                                            boolean approved = false;
                                                            boolean original = true;
                                                            Map<String,Object> nested = new HashMap<>();
                                                            nested.put("android_id",android_id);
                                                            nested.put("approved",approved);
                                                            nested.put("make",make);
                                                            nested.put("extraData",blob);
                                                            nested.put("model",model);
                                                            nested.put("nickname",nickname);
                                                            nested.put("original",original);
                                                            Toast.makeText(getApplicationContext(),"Failed to delete this device",Toast.LENGTH_LONG).show();
                                                            FirebaseFirestore.getInstance().collection("users")
                                                                    .document(uid).collection("devices")
                                                                    .document(android_id).set(nested)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task11) {
                                                                            if(task11.isSuccessful()){
                                                                                Intent intent = new Intent(DeleteDevice.this,DeviceListBuilder.class);
                                                                                intent.putExtra("pk",pk);
                                                                                intent.putExtra("uid",uid);
                                                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |  Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                startActivity(intent);
                                                                                finish();
                                                                            }else{
                                                                                Toast.makeText(getApplicationContext(),"Deleting Device Error: Please confact support",Toast.LENGTH_LONG).show();
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    });
                                        }else{
                                            Toast.makeText(getApplicationContext(),"Failed to delete this device",Toast.LENGTH_LONG).show();
                                            try {
                                                Files.write(pk,new File(getFilesDir(),PKFN));
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                        }else{
                            Toast.makeText(getApplicationContext(),"Failed to delete this device",Toast.LENGTH_LONG).show();
                        }
                    });

        }else{
            Toast.makeText(getApplicationContext(),"Failed to delete this device",Toast.LENGTH_LONG).show();
        }
    }

    private void deleteSameButNotLast() {
        File file = new File(getFilesDir(),PKFN);
        if(file.delete()){
            FirebaseFirestore.getInstance().collection("users")
                    .document(uid).collection("devices")
                    .document(android_id).delete()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(DeleteDevice.this,SignIn.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |  Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(),"Failed to delete this device",Toast.LENGTH_LONG).show();
                            try {
                                Files.write(pk, new File(getFilesDir(),PKFN));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }else{
            Toast.makeText(getApplicationContext(),"Failed to delete this device",Toast.LENGTH_LONG).show();
        }
    }

    private void deleteDifferentDevice() {
        FirebaseFirestore.getInstance().collection("users")
                .document(uid).collection("devices")
                .document(android_id).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Intent intent = new Intent(DeleteDevice.this,DeviceListBuilder.class);
                            intent.putExtra("uid",uid);
                            intent.putExtra("pk",pk);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |  Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(),"Failed to remove device",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}