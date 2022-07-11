package com.iceberg.password_manager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;

public class SignIn extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private boolean original = false;
    private FirebaseMessaging firebaseMessaging;
    private String token = "";
    private byte[] pk, pk2;
    private FirebaseAnalytics mFirebaseAnalytics;
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

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
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

    public void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnSignIn:
                hideSoftKeyboard(v);
                String email = etEmail.getText().toString().trim();
                String pass = etPass.getText().toString().trim();
                verify(email,pass);
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

    public void verify(String email, String pass){
        if(email.length()>0 && pass.length()>0){
            signInUser(email,pass);
        }else if(email.length()<1 && pass.length()>0){
            Toast.makeText(getApplicationContext(),"Email is required",Toast.LENGTH_LONG).show();
        }else if(email.length()>0 && pass.length()<1){
            Toast.makeText(getApplicationContext(),"Password is required",Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(SignIn.this, "Email and Passward are required", Toast.LENGTH_LONG).show();
        }
    }

    private void signInUser(String email, String pass) {
        auth.signInWithEmailAndPassword(email,pass)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        uid = auth.getCurrentUser().getUid();
                        FirebaseFirestore.getInstance().collection("users")
                            .document(uid).collection("devices")
                            .document(android_id).get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        DocumentSnapshot dss = task.getResult();
                                        if(dss.exists()){
                                            if(auth.getCurrentUser().isEmailVerified()){
                                                Toast.makeText(getApplicationContext(),"Already verified",Toast.LENGTH_LONG).show();
                                            }else{
                                                Toast.makeText(getApplicationContext(),"NEED TO BE VERIFIED",Toast.LENGTH_LONG).show();
                                            }
                                        }else{
                                            Toast.makeText(getApplicationContext(),"Not in system",Toast.LENGTH_LONG).show();
                                        }
                                    }else{
                                        Toast.makeText(getApplicationContext(),"Failed to recognize device",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                    }else{
                        etEmail.setText("");
                        etPass.setText("");
                        Toast.makeText(getApplicationContext(),"Failed to sign in.  Either your email or password are incorrect.  Or you need to register.",Toast.LENGTH_LONG).show();
                    }
                }
            });


//        auth.signInWithEmailAndPassword(email,pass)
//                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if(task.isSuccessful()){
//                            FirebaseFirestore.getInstance().collection("users")
//                                    .document(uid).collection("devices")
//                                    .document(android_id).get()
//                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                            if(task.isSuccessful()){
//                                                DocumentSnapshot dss = task.getResult();
//                                                if(dss.exists()){
//                                                    approved = (boolean) dss.getData().get("approved");
//                                                    original = (boolean) dss.getData().get("original");
//                                                    if(approved){
//                                                        if(!fileExists(PKFN)){
//                                                            Blob blob = (Blob) document.getData().get("extraData");
//                                                                                byte[] bytes = blob.toBytes();
//                                                                                // write the file
//                                                                                File file = new File(getFilesDir(),PKFN);
//                                                                                try {
//                                                                                    Files.write(file.toPath(),bytes);
//                                                                                } catch (Exception e) {
//                                                                                    Toast.makeText(getApplicationContext(),"Error writing file",Toast.LENGTH_LONG).show();
//                                                                                }
//                                                                            }else{
//                                                                                Toast.makeText(getApplicationContext(),"Error with device specs",Toast.LENGTH_LONG).show();
//                                                                            }
//                                                                        }
//                                                                    });
//                                                        }
//                                                        Intent intent = new Intent(SignIn.this,MainMenu.class);
//                                                        intent.putExtra("uid",uid);
//                                                        startActivity(intent);
//                                                        finish();
//                                                    }else{
//                                                        if(original){
//                                                            if(auth.getCurrentUser().isEmailVerified()){
//                                                                FirebaseFirestore.getInstance().collection("users")
//                                                                        .document(uid).collection("devices")
//                                                                        .document(android_id).update("approved",true)
//                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                                            @Override
//                                                                            public void onComplete(@NonNull Task<Void> task) {
//                                                                                if(task.isSuccessful()){
//                                                                                    Intent intent = new Intent(SignIn.this,MainMenu.class);
//                                                                                    intent.putExtra("uid",uid);
//                                                                                    startActivity(intent);
//                                                                                    finish();
//                                                                                }else{
//                                                                                    Toast.makeText(getApplicationContext(),"Couldn't update original user",Toast.LENGTH_LONG).show();
//                                                                                    auth.signOut();
//                                                                                }
//                                                                            }
//                                                                        });
//                                                            }else{
//                                                                Toast.makeText(getApplicationContext(),"Please verify your email",Toast.LENGTH_LONG).show();
//                                                                auth.signOut();
//                                                            }
//                                                        }else{
//                                                            startActivity(new Intent(SignIn.this,NotApprovedYet.class));
//                                                            auth.signOut();
//                                                            finish();
//                                                        }
//                                                    }
//                                                }else{
//                                                    Toast.makeText(getApplicationContext(),"Failed to locate",Toast.LENGTH_LONG).show();
//                                                }
//                                            }else{
//                                                Toast.makeText(getApplicationContext(),"Error retrieving record",Toast.LENGTH_LONG).show();
//                                            }
//                                        }
//                                    });
//
//                        }else{
//                            Toast.makeText(getApplicationContext(),"Failed to sign in user",Toast.LENGTH_LONG).show();
//                        }
//                    }
//                });
    }

    private boolean fileExists(String filename) {
        File path = getFilesDir();
        File file = new File(path,filename);
        if(file.exists()){
            return true;
        }
        return false;
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