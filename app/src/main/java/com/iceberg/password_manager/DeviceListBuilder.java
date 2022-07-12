package com.iceberg.password_manager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeviceListBuilder extends AppCompatActivity {

    TextView tvLoad;
    ProgressBar pb;
    Button btnProceed;
    byte [] pk;
    private String uid;
    private byte[] ed2b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list_builder);

        Intent oi = getIntent();
        pk = oi.getByteArrayExtra("pk");
        uid = oi.getStringExtra("uid");

        tvLoad = findViewById(R.id.tvLoading);
        pb = findViewById(R.id.progressBar);
        btnProceed = findViewById(R.id.btnProceed);

        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readInDeviceIDs();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                btnProceed.performClick();
            }
        },1000);
    }

    private void readInDeviceIDs() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        Task<QuerySnapshot> tqss = firebaseFirestore.collection("users")
                .document(uid).collection("devices")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            List<String> list = new ArrayList<>();
                            for(QueryDocumentSnapshot qdss : task.getResult()){
                                list.add(qdss.getId());
                            }
                            List<Device> devices = new ArrayList<>();
                            getDevicesFromList(list,0, devices);
                        }else{
                            Toast.makeText(getApplicationContext(),"Error reading data",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void getDevicesFromList(List<String> list, int position, List<Device> devices) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if(list.size()>0 && list!=null){
            Task<DocumentSnapshot> tgss = firebaseFirestore.collection("users")
                    .document(uid).collection("devices")
                    .document(list.get(position)).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                if(document.exists()){
                                    String android_id = list.get(position);
                                    boolean approved = (boolean) document.getData().get("approved");
                                    String make = (String) document.getData().get("make");
                                    String model = (String) document.getData().get("model");
                                    String nickname = (String) document.getData().get("nickname");
                                    String token = (String) document.getData().get("token");
                                    if(!approved){
                                        Blob extraData;
                                        try{
                                            extraData = (Blob) document.getData().get("extraData");
                                        }catch(Exception e){
                                            extraData = null;
                                        }
                                        if(extraData!=null)
                                            ed2b = extraData.toBytes();
                                    }
                                    Device dev;
                                    if(approved){
                                        dev = new Device(android_id,approved,pk,make,model,nickname);
                                    }
                                    else{
                                        dev = new Device(android_id,approved,ed2b,make,model,nickname);
                                    }
                                    devices.add(dev);
                                    if(position<(list.size()-1)){
                                        getDevicesFromList(list,position+1,devices);
                                    }else{
                                        if(writeDeviceListToFile(devices)){
                                            Intent intent = new Intent(DeviceListBuilder.this,Devices.class);
                                            intent.putExtra("uid",uid);
                                            intent.putExtra("pk",pk);
                                            startActivity(intent);
                                            finish();
                                        }else{
                                            Toast.makeText(getApplicationContext(),"Error writing data",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }else{
                                    Toast.makeText(getApplicationContext(),"Error reading a device",Toast.LENGTH_LONG).show();
                                }
                            }else{
                                Toast.makeText(getApplicationContext(),"Error reading device",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private boolean writeDeviceListToFile(List<Device> devices) {
        String filename = "devices.tsv";
        String temp = "";
        for(int x=0;x<devices.size();x++){
            temp = temp + devices.get(x).getAndroid_id()+"\t";
            if(devices.get(x).isApproved()) temp = temp + "true\t";
            else temp = temp + "false\t";
            if(devices.get(x).getExtraData() == null)
                temp = temp + "~\t"+devices.get(x).getMake()+"\t";
            else
                temp = temp + Arrays.toString(devices.get(x).getExtraData()) +"\t"+devices.get(x).getMake()+"\t";
            temp = temp + devices.get(x).getModel()+"\t"+devices.get(x).getNickname()+"\n";
        }
        File path = getApplicationContext().getFilesDir();
        try{
            FileOutputStream fos =new FileOutputStream(new File(path,filename));
            fos.write(temp.getBytes(StandardCharsets.UTF_8));
            fos.close();
            return true;
        }catch(Exception e){
            return false;
        }
    }


}