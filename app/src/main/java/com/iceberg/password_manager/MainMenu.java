package com.iceberg.password_manager;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import java.nio.file.Files;

public class MainMenu extends AppCompatActivity implements View.OnClickListener {

    private Button btnDev, btnPass;
    private String uid;
    private static final String PKFN = "infor.pem";
    private static final String TFILE = "infodv.dat";
    private String token = "";
    private byte[] pk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        btnDev = findViewById(R.id.btnDevices);
        btnPass = findViewById(R.id.btnPasswords);
        Intent oi = getIntent();
        uid = oi.getStringExtra("uid");

        btnDev.setOnClickListener(this);
        btnPass.setOnClickListener(this);
    }

    @SuppressLint({"NewApi", "NonConstantResourceId"})
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnDevices:
                Intent intent = new Intent(MainMenu.this,DeviceListBuilder.class);
                intent.putExtra("uid",uid);
                startActivity(intent);
                break;
            case R.id.btnPasswords:
                File file = new File(getFilesDir(),PKFN);
                try{
                    pk = Files.readAllBytes(file.toPath());
                }catch(Exception e){
                    Toast.makeText(getApplicationContext(),"Failed to read in data",Toast.LENGTH_LONG).show();
                }
                if(pk.length>0){
                    Intent intent1 = new Intent(MainMenu.this,PasswordListBuilder.class);
                    intent1.putExtra("uid",uid);
                    intent1.putExtra("pk",pk);
                    startActivity(intent1);
                }else{
                    Toast.makeText(getApplicationContext(),"Failed to read in data",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}