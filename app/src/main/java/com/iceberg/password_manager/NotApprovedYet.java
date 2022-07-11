package com.iceberg.password_manager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class NotApprovedYet extends AppCompatActivity {

    private TextView tvMessage;
    private String uid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_approved_yet);
        tvMessage = findViewById(R.id.tvNAM);
    }
}