package com.example.namastey;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 1000;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run()
            {
                FirebaseUser currentUser = mAuth.getCurrentUser();

                if(currentUser == null)
                {
                    SendUserToLoginActivity();
                }
                else
                {
                    VerifyUserExistence();
                }

            }
        },SPLASH_TIME_OUT);
    }

    private void VerifyUserExistence()
    {
        String currentUserID = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child("name").exists())
                {

                    SendUserToHomeActivity();
                }
                else
                {
                    SendUserToLoginActivity();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToHomeActivity()
    {

        Intent SettingsIntent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(SettingsIntent);

    }

    private void SendUserToLoginActivity()
    {

        Intent SettingsIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(SettingsIntent);

    }

    private void SendUserToSettingsActivity()
    {

        Intent SettingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(SettingsIntent);

    }



}
