package com.example.namastey;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.AdRequest;
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
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button Update_acc_setting;
    private EditText user_name,user_status;
    private AdView mAdView;


    private String CurrentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private CircleImageView Profile_image;
    private static final int GalleryPick = 1;
    private StorageReference UserProfileImagesReference;




    private ProgressDialog Loadingbar;
    private Toolbar settingsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth=FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImagesReference = FirebaseStorage.getInstance().getReference().child("Profile Images");

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);







        initializeFields();
        Update_acc_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetrieveUserinfo();

        Profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/");
                startActivityForResult(galleryIntent, GalleryPick);

            }
        });

    }

    private void initializeFields()
    {
        Update_acc_setting = (Button)findViewById(R.id.update_profile);
        user_name = (EditText)findViewById(R.id.set_user_name);
        user_status = (EditText)findViewById(R.id.set_profile_status);
        Profile_image =(CircleImageView)findViewById(R.id.profile_image);

        Loadingbar = new ProgressDialog(this);
        settingsToolbar = (Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Settings");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode== GalleryPick && resultCode ==RESULT_OK && data!=null)
        {

            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK)
            {
                Loadingbar.setTitle("Set Profile Image");
                Loadingbar.setMessage("Please Wait , while updating your Profile Image");
                Loadingbar.setCanceledOnTouchOutside(false);
                Loadingbar.show();
                Uri resultUri = result.getUri();

                final StorageReference Filepath = UserProfileImagesReference.child(CurrentUserID + ".jpg");
                Filepath.putFile(resultUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String downloadUrl = uri.toString();

                                        RootRef.child("Users").child(CurrentUserID).child("image")
                                                .setValue(downloadUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Toast.makeText(SettingsActivity.this, "Image saved in database successfuly", Toast.LENGTH_SHORT).show();
                                                            Loadingbar.dismiss();
                                                        }
                                                        else{
                                                            String message = task.getException().toString();
                                                            Toast.makeText(SettingsActivity.this, "Error: " + message,Toast.LENGTH_SHORT).show();
                                                            Loadingbar.dismiss();

                                                        }

                                                    }
                                                });

                                    }
                                });

                            }
                        });
            }
        }
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
                        if( (dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image"))))
                        {
                            String retriveUserName = dataSnapshot.child("name").getValue().toString();
                            String retriveUserStatus = dataSnapshot.child("status").getValue().toString();
                            final String retriveUserImage = dataSnapshot.child("image").getValue().toString();

                            user_name.setText(retriveUserName);
                            user_status.setText(retriveUserStatus);
                            Picasso.get().load(retriveUserImage).into(Profile_image);

                        }
                        else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
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
