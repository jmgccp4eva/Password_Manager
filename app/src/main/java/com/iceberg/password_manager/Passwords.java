package com.iceberg.password_manager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Passwords extends AppCompatActivity {

    private String uid;
    List<Password> passwords;
    RecyclerView recyclerView;
    PWAdapter adapter;
    Button btnGo2Dev;
    ImageButton ibAddPW;
    byte[] pk;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwords);

        Intent oi = getIntent();
        uid = oi.getStringExtra("uid");
        pk = oi.getByteArrayExtra("pk");

        ibAddPW = findViewById(R.id.ibAddPW);
        btnGo2Dev = findViewById(R.id.btnGo2Dev);
        passwords = convertToPasswords(readInData());
        deleteFile("passwords.tsv");
        recyclerView = findViewById(R.id.listOfPasswords);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PWAdapter(getApplicationContext(),passwords,Passwords.this,pk);
        recyclerView.setAdapter(adapter);

        btnGo2Dev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Passwords.this,DeviceListBuilder.class);
                intent1.putExtra("uid",uid);
                intent1.putExtra("pk",pk);
                startActivity(intent1);
                finish();
            }
        });

        ibAddPW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private List<Password> convertToPasswords(String temp) {
        List<Password> passwords = new ArrayList<>();
        String[] lines = temp.split("\n");
        for(int x=0;x<lines.length;x++){
            String[] items = lines[x].split("\t");
            Password p = new Password(items[0],items[1],items[2],items[3]);
            passwords.add(p);
        }
        return passwords;
    }

    private String readInData() {
        String filename = "passwords.tsv";
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

    public void nextInstance(String pwID, String itemName, String email, String password, byte[] pk){
        Intent intent = new Intent(this,EditViewPassword.class);
        intent.putExtra("pwID",pwID);
        intent.putExtra("itemName",itemName);
        intent.putExtra("email",email);
        intent.putExtra("pass",password);
        intent.putExtra("pk",pk);
        intent.putExtra("uid",uid);
        startActivity(intent);
    }
}