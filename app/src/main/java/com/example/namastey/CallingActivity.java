package com.example.namastey;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.shapes.OvalShape;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
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
    private String callingId = "",ringingId = "";
    private MediaPlayer mediaPlayer;

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
        mediaPlayer = MediaPlayer.create(this,R.raw.ringing);

        getAndsetRecieverProfile();
        cancelCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                checker = "clicked";
                mediaPlayer.stop();

                cancelCallingUser();


            }
        });
        acceptCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                mediaPlayer.stop();
                final HashMap<String, Object> callingPickupMap = new HashMap<>();
                callingPickupMap.put("picked","picked");

                userRef.child(SenderUserId).child("Ringing").
                        updateChildren(callingPickupMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isComplete())
                        {
                            mediaPlayer.stop();
                            Intent intent  = new Intent(CallingActivity.this,VideoChatActivity.class);
                            startActivity(intent);
                            //TODO--added finish here
                            finish();
                        }

                    }
                });

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
                    if(dataSnapshot.child(RecieverUserId).hasChild("image"))
                    {
                        RecieverUserImage = dataSnapshot.child(RecieverUserId).child("image").getValue().toString();
                        RecieverUserName = dataSnapshot.child(RecieverUserId).child("name").getValue().toString();
                        nameContact.setText(RecieverUserName);
                        Picasso.get().load(RecieverUserImage).placeholder(R.drawable.profile_image).into(profileImage);

                    }
                    else
                    {
                        RecieverUserName = dataSnapshot.child(RecieverUserId).child("name").getValue().toString();
                        nameContact.setText(RecieverUserName);
                        Picasso.get().load(R.drawable.profile_image).into(profileImage);

                    }

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
        mediaPlayer.start();
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
                                        ringingInfo.put("ringing",SenderUserId);

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
                if(dataSnapshot.child(RecieverUserId).child("Ringing").hasChild("picked"))
                {
                    mediaPlayer.stop();
                    Intent intent  = new Intent(CallingActivity.this,VideoChatActivity.class);
                    startActivity(intent);
                    finish();

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {


            }
        });





    }

    private void cancelCallingUser()
    {
        //for sender side
        userRef.child(SenderUserId).child("Calling").
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists() && dataSnapshot.hasChild("calling"))
                        {
                            callingId = dataSnapshot.child("calling").getValue().toString();

                            userRef.child(callingId).child("Ringing")
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        userRef.child(SenderUserId).child("Calling")
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                //Chat to home
                                                Intent intent1 = new Intent(CallingActivity.this,HomeActivity.class);
                                                startActivity(intent1);
                                                finish();


                                            }
                                        });
                                    }

                                }
                            });
                        }
                        else
                        {
                            //chat to home
                            Intent intent2 = new Intent(CallingActivity.this,HomeActivity.class);

                            startActivity(intent2);
                            finish();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        //from reciever side

        userRef.child(SenderUserId).child("Ringing").
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists() && dataSnapshot.hasChild("ringing"))
                        {
                            ringingId = dataSnapshot.child("ringing").getValue().toString();

                            userRef.child(ringingId).child("Calling")
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        userRef.child(SenderUserId).child("Ringing")
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                //chat to home
                                                Intent intent = new Intent(CallingActivity.this,HomeActivity.class);

                                                startActivity(intent);
                                                finish();


                                            }
                                        });
                                    }

                                }
                            });
                        }
                        else
                        {
                            //chat to home

                            Intent intent3 = new Intent(CallingActivity.this,HomeActivity.class);

                            startActivity(intent3);
                            finish();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

}
