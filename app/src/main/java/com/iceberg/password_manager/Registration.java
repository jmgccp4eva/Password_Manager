package com.iceberg.password_manager;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.io.Files;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class Registration extends AppCompatActivity implements View.OnClickListener {

    private String make = "";
    private String modNum = "";
    private String token = "";
    private EditText etName, etEmail, etPass, etConfPass;
    private Button btnReg;
    private ProgressBar pbRegBtnPressed;
    private TextView tvEmailSent;
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
        tvEmailSent = findViewById(R.id.tvEmailSent);
        tvEmailSent.setVisibility(View.GONE);
        pbRegBtnPressed = findViewById(R.id.pbRegBtnPressed);
        pbRegBtnPressed.setVisibility(View.GONE);

        Intent intent = getIntent();
        android_id = intent.getStringExtra("android_id");

        make = Build.MANUFACTURER;
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
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String pass = etPass.getText().toString().trim();
                String confPass = etConfPass.getText().toString().trim();

                // Build arrays to be passed in for error checking
                String[] items = new String[]{name,email,pass,confPass};
                String[] itemNames = new String[]{"Name","Email","Password","Confirm Password"};

                hideSoftKeyboard(v);
                pbRegBtnPressed.setVisibility(View.VISIBLE);
                String error = verify(items,itemNames);

                if(error.equals("No errors")){
                    registerUser(name,email,pass,token,android_id,make,modNum);
                }else{
                    Toast.makeText(Registration.this,""+error,Toast.LENGTH_LONG).show();
                }

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

    private void registerUser(String name, String email, String pass, String token, String android_id, String make, String modNum) {
        auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(
                task ->{
                    if(task.isSuccessful()){
                        // Need to add user to database
                        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                        storeUserData(name,email,uid,token,android_id,make,modNum);
                    }else{
                        Toast.makeText(getApplicationContext(),"Failed to register user.\n\nYou may already be registered.",Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    @SuppressLint("NewApi")
    private void storeUserData(String name, String email, String uid, String token, String android_id, String make, String modNum) {
        KeyPair pair = createKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        byte[] pubData = publicKey.getEncoded();
        byte[] privData = privateKey.getEncoded();

        Blob blob = Blob.fromBytes(pubData);
        File file = new File(getFilesDir(),PKFN);
        try {
            Files.write(privData,file);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_LONG).show();
        }

        insertDataIntoUserColl(uid,name,email,blob,android_id,make,"~",modNum);
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

    private void insertDataIntoDevColl(String uid,String android_id,String make,String model,String nickname){
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

        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .collection("devices")
                .document(android_id)
                .set(nested)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        sendVerificationEmail();
                    }else{
                        Toast.makeText(Registration.this,"Failed to add Device",Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendVerificationEmail() {
        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(getApplicationContext(),"User successfully registered.  Please verify your email.",Toast.LENGTH_LONG).show();
                        etEmail.setText("");
                        etName.setText("");
                        etPass.setText("");
                        etConfPass.setText("");
                        pbRegBtnPressed.setVisibility(View.GONE);
                        etEmail.setVisibility(View.GONE);
                        etName.setVisibility(View.GONE);
                        etPass.setVisibility(View.GONE);
                        btnReg.setVisibility(View.GONE);
                        etConfPass.setVisibility(View.GONE);
                        ibConfPass.setVisibility(View.GONE);
                        ibPass.setVisibility(View.GONE);
                        tvEmailSent.setVisibility(View.VISIBLE);
                    }else{
                        Toast.makeText(getApplicationContext(),"Failed to register",Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void insertDataIntoUserColl(String uid, String name, String email, Blob blob, String android_id, String make, String nickname, String modNum) {
        Map<String,Object> data =new HashMap<>();
        data.put("id",uid);
        data.put("name",name);
        data.put("email",email);
        data.put("blob",blob);

        FirebaseFirestore.getInstance().collection("users").document(uid)
                .set(data).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        insertDataIntoDevColl(uid,android_id,make,modNum,nickname);
                    }else{
                        Toast.makeText(Registration.this,"Failed to add User",Toast.LENGTH_LONG).show();
                    }
                });
    }

    private KeyPair createKeyPair() {
        KeyPair pair = null;
        try {
            KeyPairGenerator keyPairGenerator=KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            pair = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            Log.d("Reg","Failed to make keys\n"+e);
        }
        return pair;
    }

    private String verify(String[] items, String[] itemNames) {
        int occur;
        StringBuilder e= new StringBuilder();
        String emlRegEx = "^[\\w-_]{1,20}@\\w{2,20}\\.\\w{2,3}$";
        String passRegEx = "^(?=.{12,}$)(?=.*?[a-z])(?=.*?[A-Z])(?=.*?[0-9])(?=.*?\\W).*$";
        for(int x=0;x<items.length;x++){
            if(items[x].length()<1) {
                if (e.length() > 0)
                    e.append(", ");
                e.append(itemNames[x]);
            }
        }
        if(e.length()>0){
            occur = getCharOccur(e.toString(),',');
            if(occur>0){    // More than one missing
                e.append(" are required.");
            }else{
                e.append(" is required");
            }
        }else{
            if(items[2].equals(items[3])){
                Pattern pattern = Pattern.compile(emlRegEx);
                if(pattern.matcher(items[1]).matches()){
                    Pattern pattern1 = Pattern.compile(passRegEx);
                    if(pattern1.matcher(items[2]).matches()){
                        e = new StringBuilder("No errors");
                    }else{
                        e = new StringBuilder("Password invalid.  It must contain:\nAt least 1 lowercase letter\nAt least 1 uppercase letter\n" +
                                "At least 1 digit\nAt least 1 Special Character\nAnd must be at least 12 characters long.");
                    }

                }else{
                    e = new StringBuilder("Email is invalid");
                }
            }else{
                return "Passwords do not match";
            }
        }
        return e.toString();
    }

    private int getCharOccur(String e, char c) {
        int count=0;
        for(int x=0;x<e.length();x++){
            if(e.charAt(x)==c)
                count+=1;
        }
        return count;
    }
}