package com.iceberg.password_manager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import javax.crypto.Cipher;

public class EditViewPassword extends AppCompatActivity implements View.OnClickListener {

    EditText etItemName, etEmail, etPassword;
    ImageButton ibCancelEditViewPass;
    Button btnSavePWonEVPW, btnCopyPass, btnCopyEmail;
    String origItemName,origEmail,origPass,pwID,uid;
    byte[] pk;
    private boolean passwordHidden = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_view_password);

        Intent oi = getIntent();
        pk = oi.getByteArrayExtra("pk");
        uid = oi.getStringExtra("uid");
        pwID = oi.getStringExtra("pwID");
        origItemName = oi.getStringExtra("itemName");
        origEmail = oi.getStringExtra("email");
        origPass = oi.getStringExtra("pass");

        etItemName = findViewById(R.id.etItemName);
        etEmail = findViewById(R.id.etPWEmail);
        btnCopyEmail = findViewById(R.id.btnCopyEmail);
        btnCopyPass = findViewById(R.id.btnCopyPassword);
        etPassword = findViewById(R.id.etEVPasswordPassword);
        ibCancelEditViewPass = findViewById(R.id.ibCancelEVPW);
        btnSavePWonEVPW = findViewById(R.id.btnSavePasswordInfo);

        etItemName.setText(origItemName);
        etEmail.setText(origEmail);
        etPassword.setText(origPass);

        ibCancelEditViewPass.setOnClickListener(this);
        btnSavePWonEVPW.setOnClickListener(this);
        btnCopyPass.setOnClickListener(this);
        btnCopyEmail.setOnClickListener(this);

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
        Intent intent;
        intent = new Intent(EditViewPassword.this,PasswordListBuilder.class);
        intent.putExtra("pk",pk);
        intent.putExtra("uid",uid);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |  Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip;
        switch (v.getId()){
            case R.id.ibCancelEVPW:
                startActivity(intent);
                finish();
                break;
            case R.id.btnSavePasswordInfo:
                String newPass = etPassword.getText().toString().trim();
                String newEmail = etEmail.getText().toString().trim();
                String newItemName = etItemName.getText().toString().trim();
                FirebaseFirestore.getInstance().collection("users")
                        .document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot document = task.getResult();
                                    Blob blob = (Blob) document.getData().get("blob");
                                    byte[] bytes = blob.toBytes();
                                    try {
                                        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
                                        byte[] emailStr2Bytes = newEmail.getBytes();
                                        byte[] passStr2Bytes = newPass.getBytes();
                                        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                                        cipher.init(Cipher.ENCRYPT_MODE,publicKey);
                                        byte[] encEmailByte = cipher.doFinal(emailStr2Bytes);
                                        byte[] encPassByte = cipher.doFinal(passStr2Bytes);
                                        String encEmail = Base64.encodeToString(encEmailByte,2);
                                        String encPass = Base64.encodeToString(encPassByte,2);
                                        FirebaseFirestore.getInstance().collection("users")
                                                .document(uid).collection("words")
                                                .document(pwID).update("itemName",newItemName,"email",encEmail,"password",encPass)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            startActivity(intent);
                                                            finish();
                                                        }else{
                                                            Toast.makeText(getApplicationContext(),"Error updating password record",Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                    } catch (Exception e) {
                                        Toast.makeText(getApplicationContext(),"Key error",Toast.LENGTH_LONG).show();
                                    }
                                }else{
                                    Toast.makeText(getApplicationContext(),"Failed to read",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                break;
            case R.id.btnCopyPassword:
                clip = ClipData.newPlainText("password",etPassword.getText().toString().trim());
                manager.setPrimaryClip(clip);
                break;
            case R.id.btnCopyEmail:
                clip = ClipData.newPlainText("password",etEmail.getText().toString().trim());
                manager.setPrimaryClip(clip);
                break;
        }
    }
}