package com.iceberg.password_manager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

public class AddPassword extends AppCompatActivity implements View.OnClickListener {

    private ImageButton ibCancelAP, ibAPPasswordVis;
    private Button btnAPSavePasswordInfo;
    private EditText etAPItemName, etAPEmail, etAPPassword;
    private String email,pass,itemName, uid;
    private byte[] pk;
    private boolean passwordHidden = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_password);

        Intent oi = getIntent();
        pk = oi.getByteArrayExtra("pk");
        uid = oi.getStringExtra("uid");

        ibCancelAP=findViewById(R.id.ibCancelAP);
        ibAPPasswordVis=findViewById(R.id.ibAPPasswordVis);
        btnAPSavePasswordInfo=findViewById(R.id.btnAPSavePasswordInfo);
        etAPItemName=findViewById(R.id.etAPItemName);
        etAPEmail=findViewById(R.id.etAPEmail);
        etAPPassword=findViewById(R.id.etAPPassword);

        ibCancelAP.setOnClickListener(this);
        ibAPPasswordVis.setOnClickListener(this);
        btnAPSavePasswordInfo.setOnClickListener(this);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        hideSoftKeyboard(v);
        switch(v.getId()){
            case R.id.ibAPPasswordVis:
                if(passwordHidden){
                    etAPPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ibAPPasswordVis.setImageResource(R.drawable.ic_baseline_visibility_24);
                    passwordHidden=false;
                }else{
                    etAPPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ibAPPasswordVis.setImageResource(R.drawable.ic_baseline_visibility_off_24);
                    passwordHidden=true;
                }
                break;
            case R.id.ibCancelAP:
                finish();
                break;
            case R.id.btnAPSavePasswordInfo:
                email = etAPEmail.getText().toString().trim();
                pass = etAPPassword.getText().toString().trim();
                itemName = etAPItemName.getText().toString().trim();
                FirebaseFirestore.getInstance().collection("users")
                        .document(uid).get().addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                if(document.exists()){
                                    Blob blob = (Blob) document.getData().get("blob");
                                    byte[] bytes = blob.toBytes();
                                    try {
                                        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
                                        byte[] email2Bytes = email.getBytes();
                                        byte[] pass2Bytes = pass.getBytes();
                                        byte[] item2Bytes = itemName.getBytes();
                                        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                                        cipher.init(Cipher.ENCRYPT_MODE,publicKey);
                                        byte[] encEmailBytes = cipher.doFinal(email2Bytes);
                                        byte[] encPassBytes = cipher.doFinal(pass2Bytes);
                                        byte[] encItemNameBytes = cipher.doFinal(item2Bytes);
                                        String encEmail = Base64.encodeToString(encEmailBytes,2);
                                        String encPass = Base64.encodeToString(encPassBytes,2);
                                        String encItemName = Base64.encodeToString(encItemNameBytes,2);
                                        Map<String,Object> nested = new HashMap<>();
                                        nested.put("password",encPass);
                                        nested.put("email",encEmail);
                                        nested.put("itemName",encItemName);
                                        FirebaseFirestore.getInstance().collection("users")
                                                .document(uid).collection("words")
                                                .document(itemName).set(nested)
                                                .addOnCompleteListener(task1 -> {
                                                    if(task1.isSuccessful()){
                                                        Intent intent = new Intent(AddPassword.this,PasswordListBuilder.class);
                                                        intent.putExtra("uid",uid);
                                                        intent.putExtra("pk",pk);
                                                        startActivity(intent);
                                                        finish();
                                                    }else{
                                                        Toast.makeText(getApplicationContext(),"Error saving record",Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }else{
                                    Toast.makeText(getApplicationContext(),"Missing data",Toast.LENGTH_LONG).show();
                                }
                            }else{
                                Toast.makeText(getApplicationContext(),"Error reading record",Toast.LENGTH_LONG).show();
                            }
                        });
                break;
        }
    }
}