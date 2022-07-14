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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ForgotPassword extends AppCompatActivity implements View.OnClickListener {

    private String email, pass, confPass,uid;
    private Button btnResetPW;
    private ImageButton ibCancel,ibPassVis, ibConfPassVis;
    private EditText etEmail, etFPPass, etFPConfPass;
    private TextView tvError;
    private boolean passwordHidden = true;
    private boolean confPassHidden = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Intent oi = getIntent();
        uid = oi.getStringExtra("uid");

        ibPassVis = findViewById(R.id.ibFPPasswordVis);
        ibConfPassVis=findViewById(R.id.ibFPConfPasswordVis);
        tvError = findViewById(R.id.tvError);
        tvError.setVisibility(View.GONE);
        etEmail = findViewById(R.id.etFPEmail);
        ibCancel = findViewById(R.id.ibCancelFP);
        btnResetPW = findViewById(R.id.btnResetPassword);
        etFPConfPass = findViewById(R.id.etFPConfPass);
        etFPPass = findViewById(R.id.etFPPass);

        ibCancel.setOnClickListener(this);
        btnResetPW.setOnClickListener(this);
        ibPassVis.setOnClickListener(this);
        ibConfPassVis.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnResetPassword:
                hideSoftKeyboard(view);
                tvError.setVisibility(View.GONE);
                email = etEmail.getText().toString().trim();
                pass = etFPPass.getText().toString().trim();
                confPass = etFPConfPass.getText().toString().trim();
                String[] items = new String[]{email,pass,confPass};
                String[] itemNames = new String[]{"Email","New Password","New Confirm Password"};
                String errors = verify(items,itemNames);
                if(errors.equals("No errors")){
                    resetPW(email,pass);
                } else if (errors.equals("Rabbit")) {
                    tvError.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getApplicationContext(), "" + errors, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.ibCancelFP:
                finish();
                break;
            case R.id.ibFPPasswordVis:
                if(passwordHidden){
                    etFPPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ibPassVis.setImageResource(R.drawable.ic_baseline_visibility_24);
                    passwordHidden=false;
                }else{
                    etFPPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ibPassVis.setImageResource(R.drawable.ic_baseline_visibility_off_24);
                    passwordHidden=true;
                }
                break;
            case R.id.ibFPConfPasswordVis:
                if(confPassHidden){
                    etFPConfPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ibConfPassVis.setImageResource(R.drawable.ic_baseline_visibility_24);
                    confPassHidden=false;
                }else{
                    etFPConfPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ibConfPassVis.setImageResource(R.drawable.ic_baseline_visibility_off_24);
                    confPassHidden=true;
                }
                break;
        }
    }

    private void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

    private void resetPW(String email, String pass) {
        if(uid.length()<1){
            Toast.makeText(getApplicationContext(),"Please contact support",Toast.LENGTH_LONG).show();
        }else{
            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                            if(isNewUser){
                                Toast.makeText(getApplicationContext(),"No user found.  Please confirm the email entered.",Toast.LENGTH_LONG).show();
                            }else {
                                String origEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                                if(origEmail.equals(email)){
                                    FirebaseAuth.getInstance().getCurrentUser().updatePassword(pass)
                                            .addOnCompleteListener(task1 -> {
                                                if(task1.isSuccessful()){
                                                    finish();
                                                }else{
                                                    Toast.makeText(getApplicationContext(),"Failed to update password",Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }else{
                                    Toast.makeText(getApplicationContext(),"Cannot confirm your identity",Toast.LENGTH_LONG).show();
                                }
                            }
                        }else{
                            Toast.makeText(getApplicationContext(),"Failed to process",Toast.LENGTH_LONG).show();
                        }
                    });
        }
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
            if(items[2].equals(items[1])){
                Pattern pattern = Pattern.compile(emlRegEx);
                if(pattern.matcher(items[0]).matches()){
                    Pattern pattern1 = Pattern.compile(passRegEx);
                    if(pattern1.matcher(items[1]).matches()){
                        e = new StringBuilder("No errors");
                    }else{
                        e = new StringBuilder("Rabbit");
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