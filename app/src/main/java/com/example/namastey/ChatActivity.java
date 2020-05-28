package com.example.namastey;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private  String messagerecieverId,messagerecieverName,messageRecieverImage,messagSenderId;
    private DatabaseReference RootRef,UserRef;
    private TextView userName,UserLastSeen;
    private CircleImageView UserImage;
    private Toolbar chatToolbar;
    private ImageButton sendMesssageButton;
    private EditText messageInputText;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private RecyclerView userMessagesList;

    private FirebaseAuth mAuth;

    private InterstitialAd mInterstitialAd;

    private ImageView videoCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mAuth = FirebaseAuth.getInstance();
        messagSenderId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        messagerecieverId = getIntent().getExtras().get("visit_user_id").toString();
        messagerecieverName = getIntent().getExtras().get("visit_user_name").toString();
        messageRecieverImage = getIntent().getExtras().get("visit_user_Image").toString();

        intializeControllers();

        userName.setText(messagerecieverName);
        Picasso.get().load(messageRecieverImage).placeholder(R.drawable.profile_image).into(UserImage);


        sendMesssageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                sendMessage();


            }
        });

        videoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent videoIntent = new Intent(ChatActivity.this,CallingActivity.class);
                videoIntent.putExtra("visit_user_id",messagerecieverId);
                startActivity(videoIntent);
                finish();

            }
        });

        prepareAd();

    }



    private void intializeControllers()
    {
        chatToolbar = (Toolbar)findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        userName = (TextView)findViewById(R.id.custom_profile_name);
        UserImage= (CircleImageView) findViewById(R.id.custom_profile_image);
        sendMesssageButton = (ImageButton)findViewById(R.id.send_message_personal_btn);
        messageInputText = (EditText)findViewById(R.id.input_message);
        messagesAdapter = new MessagesAdapter(messagesList);
        userMessagesList = (RecyclerView)findViewById(R.id.private_message_list);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messagesAdapter);
        videoCall = (ImageView)findViewById(R.id.videoCall);
    }

    @Override
    protected void onStart() {
        super.onStart();



        RootRef.child("Messages").child(messagSenderId).child(messagerecieverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messagesAdapter.notifyDataSetChanged();
                        //for scrolling to last message
                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());


                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {


                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
                    {


                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendMessage()
    {
        String messageText = messageInputText.getText().toString();
        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(ChatActivity.this, "First Write Your Message", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef = "Messages/" + messagSenderId + "/" + messagerecieverId;
            String messageRecieverRef = "Messages/" + messagerecieverId + "/" + messagSenderId;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messagSenderId).child(messagerecieverId).push();

            String messagePushId = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messagSenderId);

            Map messagebodyDetails  = new HashMap();
            messagebodyDetails.put(messageSenderRef + "/" + messagePushId,messageTextBody);
            messagebodyDetails.put(messageRecieverRef + "/" + messagePushId,messageTextBody);

            RootRef.updateChildren(messagebodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Message sent ", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");

                }
            });

        }

    }

    public void prepareAd()
    {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void onBackPressed()
    {
        if(mInterstitialAd.isLoaded())
        {
            mInterstitialAd.show();

            mInterstitialAd.setAdListener(new AdListener(){

                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    finish();
                }
            });
        }
        else
        {
            super.onBackPressed();
        }
    }



}
