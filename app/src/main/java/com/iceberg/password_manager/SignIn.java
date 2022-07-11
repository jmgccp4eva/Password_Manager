package com.iceberg.password_manager;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class SignIn extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private FirebaseMessaging firebaseMessaging;
    private String token = "";
    private byte[] pk, pk2;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseFirestore firebaseFirestore;
    private EditText etEmail, etPass;
    private Button btnSI, btnReg, btnFP;
    private FirebaseAuth auth;
    private boolean isFile = false;
    private boolean passwordVis = false;
    private String android_id = "";
    private String make = "";
    private String model = "";
    private String uid = "";
    private boolean approved = false;
    private static final String PKFN = "infor.pem";

    @SuppressLint({"ClickableViewAccessibility", "HardwareIds"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        firebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        auth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etSIEmail);
        etPass = findViewById(R.id.etSIPass);
        btnSI = findViewById(R.id.btnSignIn);
        btnReg = findViewById(R.id.btnSIReg);
        btnFP = findViewById(R.id.btnFP);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        btnSI.setOnClickListener(this);
        btnFP.setOnClickListener(this);
        btnReg.setOnClickListener(this);

        etPass.setOnTouchListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnSignIn:
                Toast.makeText(getApplicationContext(),"Sign in clicked",Toast.LENGTH_LONG).show();
                break;
            case R.id.btnFP:

                break;
            case R.id.btnSIReg:
                Intent intent = new Intent(SignIn.this, Registration.class);
                intent.putExtra("android_id",android_id);
                startActivity(intent);
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int Right=2;
        if(event.getAction()==MotionEvent.ACTION_UP){
            if(event.getRawX()>=etPass.getRight()-etPass.getCompoundDrawables()[Right].getBounds().width()){
                int selection = etPass.getSelectionEnd();
                if(passwordVis){
                    etPass.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_baseline_visibility_off_24,0);
                    etPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passwordVis=false;
                }else{
                    etPass.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_baseline_visibility_24,0);
                    etPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordVis=true;
                }
                etPass.setSelection(selection);
                return true;
            }
        }
        return false;
    }
}