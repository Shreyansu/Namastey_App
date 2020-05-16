package com.example.namastey;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {
    private static String API_Key = "46741992";
    private static String SESSION_ID = "1_MX40Njc0MTk5Mn5-MTU4OTYyNTYzMjkxOH51T3k2WWdsYnhrNmlXQTdKeGg1c1ZlS3B-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00Njc0MTk5MiZzaWc9Y2ViMzIwZmRhMTc0MTI1MzE4N2JkNjRjODMyOThkYzdjZDhjYmYzMjpzZXNzaW9uX2lkPTFfTVg0ME5qYzBNVGs1TW41LU1UVTRPVFl5TlRZek1qa3hPSDUxVDNrMldXZHNZbmhyTm1sWFFUZEtlR2cxYzFabFMzQi1mZyZjcmVhdGVfdGltZT0xNTg5NjI1NjkwJm5vbmNlPTAuNzcwNDg1OTIwMjAxMzg5JnJvbGU9cHVibGlzaGVyJmV4cGlyZV90aW1lPTE1OTIyMTc2OTAmaW5pdGlhbF9sYXlvdXRfY2xhc3NfbGlzdD0=";
    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;

    private ImageView closeVideoChatButtton;
    private DatabaseReference usersRef;
    private String UserId  = "";
    private FrameLayout mPublisherViewController, mSubscriberViewController;


    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);


        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        closeVideoChatButtton = findViewById(R.id.close_video_chat_btn);
        closeVideoChatButtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {

                        if(dataSnapshot.child(UserId).hasChild("Ringing"))
                        {
                            usersRef.child(UserId).child("Ringing").removeValue();
                            if(mPublisher!=null)
                            {
                                mPublisher.destroy();
                            }
                            if(mSubscriber!=null)
                            {
                                mSubscriber.destroy();
                            }
                            Intent newIntent = new Intent(VideoChatActivity.this,HomeActivity.class);
                            startActivity(newIntent);
                            finish();
                        }

                        if(dataSnapshot.child(UserId).hasChild("Calling"))
                        {
                            if(mPublisher!=null)
                            {
                                mPublisher.destroy();
                            }
                            if(mSubscriber!=null)
                            {
                                mSubscriber.destroy();
                            }


                            Intent newIntent = new Intent(VideoChatActivity.this,HomeActivity.class);
                            startActivity(newIntent);
                            finish();
                        }
                        else
                        {
                            if(mPublisher!=null)
                            {
                                mPublisher.destroy();
                            }
                            if(mSubscriber!=null)
                            {
                                mSubscriber.destroy();
                            }

                            Intent newIntent = new Intent(VideoChatActivity.this,HomeActivity.class);
                            startActivity(newIntent);
                            finish();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {


                    }
                });


            }
        });
        requestPermisssions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,VideoChatActivity.this);
    }


    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermisssions()
    {
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};

        if(EasyPermissions.hasPermissions(this,perms))
        {
            mPublisherViewController = findViewById(R.id.publisher_container);
            mSubscriberViewController = findViewById(R.id.subscriber_cntainer);
            //intilliaze and connect to the session
            mSession = new Session.Builder(this,API_Key,SESSION_ID).build();
            mSession.setSessionListener(VideoChatActivity.this);
            mSession.connect(TOKEN);
        }
        else
        {
            EasyPermissions.requestPermissions(this,"Hey,this app needs Mic and Camera, Please allow",RC_VIDEO_APP_PERM,perms);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream)
    {


    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream)
    {


    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError)
    {


    }

    @Override
    public void onConnected(Session session)
    {
        Log.i(LOG_TAG,"Session Connected");
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChatActivity.this);

        mPublisherViewController.addView(mPublisher.getView());

        if(mPublisher.getView() instanceof GLSurfaceView)
        {
             ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mPublisher);



    }

    @Override
    public void onDisconnected(Session session)
    {
        Log.i(LOG_TAG,"Session Disconnected");


    }

    @Override
    public void onStreamReceived(Session session, Stream stream)
    {
        //subscribing/recieving to the stream
        Log.i(LOG_TAG,"Session Recieved");
        if(mSubscriber ==null)
        {
            mSubscriber = new Subscriber.Builder(this,stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewController.addView(mSubscriber.getView());
        }


    }

    @Override
    public void onStreamDropped(Session session, Stream stream)
    {
        Log.i(LOG_TAG,"Session Dropped");
        if(mSubscriber!=null)
        {
            mSubscriber = null;
            mSubscriberViewController.removeAllViews();
        }


    }

    @Override
    public void onError(Session session, OpentokError opentokError)
    {
        Log.i(LOG_TAG,"Session Error");


    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture)
    {


    }
}
