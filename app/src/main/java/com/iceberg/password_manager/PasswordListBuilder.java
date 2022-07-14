package com.iceberg.password_manager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;

public class PasswordListBuilder extends AppCompatActivity {

    byte[] pk;
    String uid;
    TextView tvLoad;
    ProgressBar pb;
    Button btnProceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_list_builder);

        Intent oi = getIntent();
        uid = oi.getStringExtra("uid");
        pk = oi.getByteArrayExtra("pk");

        tvLoad = findViewById(R.id.tvLoadingPassword);
        pb = findViewById(R.id.progressBarPassword);
        btnProceed = findViewById(R.id.btnProceedPassword);

        btnProceed.setOnClickListener(v -> readInPasswords());

        new Handler().postDelayed(() -> btnProceed.performClick(),1000);
    }

    private void readInPasswords() {
        Task<QuerySnapshot> tqss = FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("words").get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        List<String> list = new ArrayList<>();
                        for(QueryDocumentSnapshot qdss : task.getResult()){
                            list.add(qdss.getId());
                        }
                        List<Password> passwords = new ArrayList<>();
                        getPasswordsFromList(list,0, passwords);
                    }else{
                        Toast.makeText(getApplicationContext(),"Error reading Passwords",Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getPasswordsFromList(List<String> list, int position, List<Password> passwords) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if(list.size()>0){
            Task<DocumentSnapshot> tgss = firebaseFirestore.collection("users")
                    .document(uid).collection("words")
                    .document(list.get(position)).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                if(document.exists()){
                                    String pwID = list.get(position);
                                    String itemName = (String) document.getData().get("itemName");
                                    String encEmail = (String) document.getData().get("email");
                                    String encPassword = (String)document.getData().get("password");
                                    try {
                                        byte[] encItemNameBytes = Base64.decode(itemName,2);
                                        byte[] encEmailBytes = Base64.decode(encEmail,2);
                                        byte[] encPassBytes = Base64.decode(encPassword,2);
                                        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(pk));
                                        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                                        cipher.init(Cipher.DECRYPT_MODE,privateKey);
                                        byte[] decItemName = cipher.doFinal(encItemNameBytes);
                                        byte[] decMsg = cipher.doFinal(encEmailBytes);
                                        itemName = new String(decItemName);
                                        String email = new String(decMsg);
                                        byte[] decPass = cipher.doFinal(encPassBytes);
                                        String password = new String(decPass);
                                        Password pw = new Password(pwID,itemName,email,password);
                                        passwords.add(pw);
                                        if(position<(list.size()-1)){
                                            getPasswordsFromList(list,position+1,passwords);
                                        }else{
                                            if(writePasswordListToFile(passwords)){
                                                Intent intent = new Intent(PasswordListBuilder.this,Passwords.class);
                                                intent.putExtra("pk",pk);
                                                intent.putExtra("uid",uid);
                                                intent.putExtra("size","");
                                                startActivity(intent);
                                                finish();
                                            }else{
                                                Toast.makeText(getApplicationContext(),"Error writing data",Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }else{
                                    Toast.makeText(getApplicationContext(),"Error reading a password",Toast.LENGTH_LONG).show();
                                }
                            }else{
                                Toast.makeText(getApplicationContext(),"Error reading password",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }else{
            Intent intent = new Intent(PasswordListBuilder.this,Passwords.class);
            intent.putExtra("pk",pk);
            intent.putExtra("uid",uid);
            intent.putExtra("size","0");
            startActivity(intent);
            finish();
        }
    }

    private boolean writePasswordListToFile(List<Password> passwords) {
        String filename = "passwords.tsv";
        String temp = "";
        for(int x=0;x<passwords.size();x++){
            temp = temp + passwords.get(x).getpwID()+"\t";
            temp = temp + passwords.get(x).getItemName()+"\t";
            temp = temp + passwords.get(x).getEmail()+"\t";
            temp = temp + passwords.get(x).getPassword()+"\n";
        }
        File path = getApplicationContext().getFilesDir();
        try{
            FileOutputStream fos =new FileOutputStream(new File(path,filename));
            fos.write(temp.getBytes(StandardCharsets.UTF_8));
            fos.close();
            return true;
        }catch(Exception e){
            return false;
        }
    }
}