package com.iceberg.password_manager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
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
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.io.Files;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SignIn extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private ProgressBar pb;
    private boolean original = false;
    FirebaseAnalytics mFirebaseAnalytics;
    private byte[] pk, pk2;
    private EditText etEmail, etPass;
    private Button btnSI, btnReg, btnFP;
    private FirebaseAuth auth;
    private boolean isFile = false;
    private boolean passwordVis = false;
    private String android_id = "";
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
        try{
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }catch (Exception e){
            uid = "";
        }


        pb = findViewById(R.id.pbSignIn);
        pb.setVisibility(View.GONE);
        etEmail = findViewById(R.id.etSIEmail);
        etPass = findViewById(R.id.etSIPass);
        btnSI = findViewById(R.id.btnSignIn);
        btnReg = findViewById(R.id.btnSIReg);
        btnFP = findViewById(R.id.btnFP);
//        try {
//            pk = Files.toByteArray(new File(getFilesDir(),PKFN));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        pk2 = pk;
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
                pb.setVisibility(View.VISIBLE);
                hideSoftKeyboard(v);
                String email = etEmail.getText().toString().trim();
                String pass = etPass.getText().toString().trim();
                etEmail.setText("");
                etPass.setText("");
                verify(email,pass);
                break;
            case R.id.btnFP:
                etEmail.setText("");
                etPass.setText("");
                Intent intent1 = new Intent(SignIn.this,ForgotPassword.class);
                intent1.putExtra("uid",uid);
                startActivity(intent1);
                break;
            case R.id.btnSIReg:
                etEmail.setText("");
                etPass.setText("");
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
            pb.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(),"Email is required",Toast.LENGTH_LONG).show();
        }else if(email.length()>0){
            pb.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(),"Password is required",Toast.LENGTH_LONG).show();
        }else{
            pb.setVisibility(View.GONE);
            Toast.makeText(SignIn.this, "Email and Passward are required", Toast.LENGTH_LONG).show();
        }
    }

    private void signInUser(String email, String pass) {
        FirebaseAuth.getInstance().signOut();
        auth.signInWithEmailAndPassword(email,pass)
            .addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    FirebaseFirestore.getInstance().collection("users")
                        .document(uid).collection("devices")
                        .document(android_id).get()
                        .addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful()){
                                DocumentSnapshot dss = task1.getResult();
                                if(dss.exists()){
                                    approved = (boolean) dss.getData().get("approved");
                                    original = (boolean) dss.getData().get("original");
                                    if(auth.getCurrentUser().isEmailVerified()){
                                        if(original){
                                            if(!approved){
                                                FirebaseFirestore.getInstance().collection("users")
                                                        .document(uid).collection("devices")
                                                        .document(android_id).update("approved",true)
                                                        .addOnCompleteListener(task11 -> {
                                                            if(!task11.isSuccessful()){
                                                                pb.setVisibility(View.GONE);
                                                                Toast.makeText(getApplicationContext(),"Error updating device record",Toast.LENGTH_LONG).show();
                                                            }else{
                                                                Intent intent = new Intent(SignIn.this,MainMenu.class);
                                                                intent.putExtra("uid",uid);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        });
                                            }else{
                                                Intent intent = new Intent(SignIn.this,MainMenu.class);
                                                intent.putExtra("uid",uid);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }else{
                                            if(!approved){
                                                Intent intent= new Intent(SignIn.this,NotApprovedYet.class);
                                                auth.signOut();
                                                startActivity(intent);
                                                finish();
                                            }else{
                                                if(!fileExists(PKFN)){
                                                    FirebaseFirestore.getInstance().collection("users")
                                                            .document(uid).collection("devices")
                                                            .document(android_id).get().addOnCompleteListener(task112 -> {
                                                                if(task112.isSuccessful()){
                                                                    Blob blob = (Blob) task112.getResult().getData().get("extraData");
                                                                    pk = blob.toBytes();
                                                                    File file = new File(getFilesDir(),PKFN);
                                                                    try {
                                                                        com.google.common.io.Files.write(pk,file);
                                                                    } catch (Exception e) {
                                                                        pb.setVisibility(View.GONE);
                                                                        Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_LONG).show();
                                                                    }
                                                                    byte[] temp = new byte[0];
                                                                    blob = Blob.fromBytes(temp);
                                                                    FirebaseFirestore.getInstance().collection("users")
                                                                            .document(uid).collection("devices")
                                                                            .document(android_id).update("extraData",blob)
                                                                            .addOnCompleteListener(task2 -> {
                                                                                if(task2.isSuccessful()){
                                                                                    Intent intent = new Intent(SignIn.this,MainMenu.class);
                                                                                    intent.putExtra("uid",uid);
                                                                                    startActivity(intent);
                                                                                    finish();
                                                                                }else{
                                                                                    pb.setVisibility(View.GONE);
                                                                                    Toast.makeText(getApplicationContext(),"Failed to update data",Toast.LENGTH_LONG).show();
                                                                                }
                                                                            });
                                                                }
                                                            });
                                                }
                                                else
                                                {
                                                    Intent intent = new Intent(SignIn.this, MainMenu.class);
                                                    intent.putExtra("uid", uid);
                                                    startActivity(intent);
                                                }
                                            }
                                        }
                                    }else{
                                        pb.setVisibility(View.GONE);
                                        etPass.setText("");
                                        etEmail.setText("");
                                        Toast.makeText(getApplicationContext(),"Awaiting email verification",Toast.LENGTH_LONG).show();
                                    }
                                }else{
                                    if(fileExists(PKFN)){
                                        File file = new File(getFilesDir(),PKFN);
                                        file.delete();
                                    }
                                    byte[] bytes = new byte[0];
                                    Blob blob = Blob.fromBytes(bytes);
                                    boolean approved = false;
                                    boolean original = false;
                                    String make = Build.MANUFACTURER;
                                    String model = Build.MODEL;
                                    Map<String,Object> nested = new HashMap<>();
                                    nested.put("android_id",android_id);
                                    nested.put("approved",approved);
                                    nested.put("make",make);
                                    nested.put("extraData",blob);
                                    nested.put("model",model);
                                    nested.put("nickname","~");
                                    nested.put("original",original);
                                    FirebaseFirestore.getInstance().collection("users")
                                            .document(uid).collection("devices")
                                            .document(android_id).set(nested)
                                            .addOnCompleteListener(task113 -> {
                                                if(task113.isSuccessful()){
                                                    Intent intent = new Intent(SignIn.this,NotApprovedYet.class);
                                                    startActivity(intent);
                                                    finish();
                                                }else{
                                                    pb.setVisibility(View.GONE);
                                                    Toast.makeText(getApplicationContext(),"Failed adding device",Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }
                            }else{
                                pb.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(),"Failed to recognize device",Toast.LENGTH_LONG).show();
                            }
                        });
                }else{
                    etEmail.setText("");
                    etPass.setText("");
                    pb.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),"Failed to sign in.  Check your registered email and password are incorrect.",Toast.LENGTH_LONG).show();
                }
            });
    }

    private boolean fileExists(String filename) {
        File path = getFilesDir();
        File file = new File(path,filename);
        return file.exists();
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