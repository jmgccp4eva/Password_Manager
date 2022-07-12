package com.iceberg.password_manager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Devices extends AppCompatActivity {

    private String uid;
    List<Device> devices;
    RecyclerView recyclerView;
    DeviceAdapter adapter;
    Button btnGo2Pass;
    byte[] pk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        pk = intent.getByteArrayExtra("pk");

        btnGo2Pass = findViewById(R.id.btnGo2Pass);
        devices = convertToDevices(readInData());
        deleteFile("devices.tsv");
        recyclerView = findViewById(R.id.listOf);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeviceAdapter(this,devices,Devices.this, pk);
        recyclerView.setAdapter(adapter);

        btnGo2Pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // WE ARE HERE NOW
                Intent intent1 = new Intent(Devices.this,PasswordListBuilder.class);
                intent1.putExtra("uid",uid);
                intent1.putExtra("pk",pk);
                startActivity(intent1);
                finish();
            }
        });
    }

    private List<Device> convertToDevices(String temp) {
        List<Device> devices = new ArrayList<>();
        String[] lines = temp.split("\n");
        for(int x=0;x< lines.length;x++){
            String[] items = lines[x].split("\t");
            Device d = null;
            if(items[1].equals("true")) {
                d = new Device(items[0],true,items[2].getBytes(),items[3],items[4],items[5]);
            }else{
                d = new Device(items[0],false,items[2].getBytes(),items[3],items[4],items[5]);
            }
            devices.add(d);
        }
        return devices;
    }

    private String readInData() {
        String filename = "devices.tsv";
        File path = getApplicationContext().getFilesDir();
        File file = new File(path,filename);
        StringBuilder text = new StringBuilder();
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while((line= bufferedReader.readLine())!=null){
                text.append(line);
                text.append("\n");
            }
            bufferedReader.close();
            return text.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public void nextInstance(String aid, String whichOne, String make, String model, String nickname){
        Intent intent = null;
        if(whichOne.equals("createApproveOrDeny")){
            intent = new Intent(this,ApproveOrDenyDevice.class);
        }else if(whichOne.equals("editViewDevice")){
            intent = new Intent(this,EditViewDevice.class);
            intent.putExtra("make",make);
            intent.putExtra("model",model);
            intent.putExtra("nickname",nickname);
        }
        intent.putExtra("aid",aid);
        intent.putExtra("uid",uid);
        intent.putExtra("pk",pk);
        startActivity(intent);
    }
}