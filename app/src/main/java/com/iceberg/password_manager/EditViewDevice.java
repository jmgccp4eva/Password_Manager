package com.iceberg.password_manager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditViewDevice extends AppCompatActivity implements View.OnClickListener {

    private FirebaseFirestore firebaseFirestore;
    private String aid, uid, make, model, nickname;
    EditText etMake,etModel,etNickname;
    ImageButton ibCancel;
    Button btnSave;
    byte[] pk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_view_device);

        Intent oi = getIntent();
        uid = oi.getStringExtra("uid");
        aid = oi.getStringExtra("aid");
        make = oi.getStringExtra("make");
        model = oi.getStringExtra("model");
        nickname = oi.getStringExtra("nickname");
        pk = oi.getByteArrayExtra("pk");
        if(nickname.equals("~"))
            nickname = "";

        ibCancel = findViewById(R.id.ibCancelSaveNickname);
        btnSave = findViewById(R.id.btnSaveDeviceNickname);
        etMake = findViewById(R.id.etMake);
        etModel = findViewById(R.id.etModel);
        etNickname = findViewById(R.id.etNickname);

        etMake.setText(make);
        etModel.setText(model);
        etNickname.setText(nickname);
        etModel.setEnabled(false);
        etMake.setEnabled(false);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        ibCancel.setOnClickListener(this);
        btnSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.ibCancelSaveNickname:
                finish();
                break;
            case R.id.btnSaveDeviceNickname:
                String newNickname = etNickname.getText().toString().trim();
                if(newNickname.equals("") || newNickname.equals(" ")){
                    newNickname = "~";
                }
                firebaseFirestore = FirebaseFirestore.getInstance();
                Task<Void> docRef = firebaseFirestore
                        .collection("users")
                        .document(uid)
                        .collection("devices")
                        .document(aid).update("nickname",newNickname)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(),"Nickname successfully updated",Toast.LENGTH_LONG).show();
                                    Intent intent1 = new Intent(EditViewDevice.this,DeviceListBuilder.class);
                                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |  Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent1.putExtra("uid",uid);
                                    startActivity(intent1);
                                    finish();
                                }else{
                                    Toast.makeText(getApplicationContext(),"Failed to update nickname",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                break;
        }
    }
}