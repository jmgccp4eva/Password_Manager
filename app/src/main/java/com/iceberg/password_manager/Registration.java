package com.iceberg.password_manager;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;

public class Registration extends AppCompatActivity implements View.OnClickListener {

    private String modNum = "";
    private String token = "";
    private EditText etName, etEmail, etPass, etConfPass;
    private Button btnReg;
    private FirebaseAuth auth;
    private boolean passwordVis = false;
    private boolean confPassVis = false;
    private FirebaseFirestore firebaseFirestore;
    private String android_id = "";
    private File path;
    private static final String PKFN = "infor.pem";
    private ImageButton ibPass, ibConfPass;
    private boolean passwordHidden = true;
    private boolean confPassHidden = true;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        etName = findViewById(R.id.etRegName);
        etEmail = findViewById(R.id.etRegEmail);
        etPass = findViewById(R.id.etRegPass);
        etConfPass = findViewById(R.id.etRegConfPass);
        btnReg = findViewById(R.id.btnRegister);
        ibConfPass = findViewById(R.id.ibConfPasswordVis);
        ibPass = findViewById(R.id.ibPasswordVis);

        Intent intent = getIntent();
        android_id = intent.getStringExtra("android_id");

        String make = Build.MANUFACTURER;
        modNum = Build.MODEL;

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        auth = FirebaseAuth.getInstance();

        btnReg.setOnClickListener(this);
        ibPass.setOnClickListener(this);
        ibConfPass.setOnClickListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.btnRegister:

                // START HERE.


                break;
            case R.id.ibConfPasswordVis:
                if(confPassHidden){
                    etConfPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ibConfPass.setImageResource(R.drawable.ic_baseline_visibility_24);
                    confPassHidden=false;
                }else{
                    etConfPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ibConfPass.setImageResource(R.drawable.ic_baseline_visibility_off_24);
                    confPassHidden=true;
                }
                break;
            case R.id.ibPasswordVis:
                if(passwordHidden){
                    etPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ibPass.setImageResource(R.drawable.ic_baseline_visibility_24);
                    passwordHidden=false;
                }else{
                    etPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ibPass.setImageResource(R.drawable.ic_baseline_visibility_off_24);
                    passwordHidden=true;
                }
                break;
        }
    }
}