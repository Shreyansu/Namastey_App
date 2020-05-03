package com.example.namastey;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private Button Update_acc_setting;
    private EditText user_name,user_status;


    private String CurrentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;


    private ProgressDialog Loadingbar;
    private Toolbar settingsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth=FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();



        initializeFields();
        Update_acc_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetrieveUserinfo();

    }

    private void initializeFields()
    {
        Update_acc_setting = (Button)findViewById(R.id.update_profile);
        user_name = (EditText)findViewById(R.id.set_user_name);
        user_status = (EditText)findViewById(R.id.set_profile_status);

        Loadingbar = new ProgressDialog(this);
        settingsToolbar = (Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Settings");

    }

    private void UpdateSettings()
    {
        String SetUserName =  user_name.getText().toString();
        String SetStatus = user_status.getText().toString();

        if(TextUtils.isEmpty(SetUserName))
        {
            Toast.makeText(SettingsActivity.this, "Enter Your UserName", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(SetStatus))
        {
            Toast.makeText(SettingsActivity.this, "Set Status", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid",CurrentUserID);
            profileMap.put("name",SetUserName);
            profileMap.put("status",SetStatus);

            RootRef.child("Users").child(CurrentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                sendUserToHomeActivity();
                                Toast.makeText(SettingsActivity.this,"Profile updated successfully",Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this,"Error:" + message,Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }

    }

    private void RetrieveUserinfo()
    {
        RootRef.child("Users").child(CurrentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {
                            String retriveUserName = dataSnapshot.child("name").getValue().toString();
                            String retriveUserStatus = dataSnapshot.child("status").getValue().toString();

                            user_name.setText(retriveUserName);
                            user_status.setText(retriveUserStatus);
                        }
                        else
                        {
                            Toast.makeText(SettingsActivity.this,"Please Set Your Profile Details",Toast.LENGTH_SHORT).show();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendUserToHomeActivity() {
        Intent MainIntent = new Intent(SettingsActivity.this, HomeActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }




}
