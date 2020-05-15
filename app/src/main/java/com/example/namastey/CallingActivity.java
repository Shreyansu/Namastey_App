package com.example.namastey;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CallingActivity extends AppCompatActivity {

    private TextView nameContact;
    private ImageView profileImage,cancelCallButton,acceptCallButton;
    private String RecieverUserId="",RecieverUserName="",RecieverUserImage="";
    private String SenderUserId="",SenderUserName="",SenderUserImage="",checker="";
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);
        mAuth = FirebaseAuth.getInstance();

        SenderUserId= mAuth.getCurrentUser().getUid();


        RecieverUserId = getIntent().getExtras().get("visit_user_id").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        nameContact = findViewById(R.id.user_name_calling);
        profileImage = findViewById(R.id.profile_image_calling);
        cancelCallButton = findViewById(R.id.cancel_call);
        acceptCallButton = findViewById(R.id.make_call);

        getAndsetRecieverProfile();
        cancelCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                checker = "clicked";


            }
        });

    }

    private void getAndsetRecieverProfile()
    {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child(RecieverUserId).exists())
                {
                    RecieverUserImage = dataSnapshot.child(RecieverUserId).child("image").getValue().toString();
                    RecieverUserName = dataSnapshot.child(RecieverUserId).child("name").getValue().toString();
                    nameContact.setText(RecieverUserName);
                    Picasso.get().load(RecieverUserImage).placeholder(R.drawable.profile_image).into(profileImage);

                }

                if(dataSnapshot.child(SenderUserId).exists())
                {
                    SenderUserImage = dataSnapshot.child(SenderUserId).child("image").getValue().toString();
                    SenderUserName = dataSnapshot.child(SenderUserId).child("name").getValue().toString();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {


            }
        });
    }

    @Override
    protected void onStart()
    {

        super.onStart();
        userRef.child(RecieverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(!checker.equals("clicked") && !dataSnapshot.hasChild("Calling") && !dataSnapshot.hasChild("Ringing"))
                {
                    final HashMap<String, Object> callingInfo = new HashMap<>();

                    callingInfo.put("calling",RecieverUserId);

                    userRef.child(SenderUserId).child("Calling").updateChildren(callingInfo).
                            addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        final HashMap<String, Object> ringingInfo = new HashMap<>();
                                        callingInfo.put("ringing",SenderUserId);

                                        userRef.child(RecieverUserId).child("Ringing").updateChildren(ringingInfo);

                                    }


                                }
                            });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child(SenderUserId).hasChild("Ringing") && !dataSnapshot.child(SenderUserId).hasChild("Calling"))
                {
                    acceptCallButton.setVisibility(View.VISIBLE);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {


            }
        });





    }
}
