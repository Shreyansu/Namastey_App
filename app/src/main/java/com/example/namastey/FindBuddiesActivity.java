package com.example.namastey;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindBuddiesActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private RecyclerView FindBuddiesRecyclerList;
    private DatabaseReference UserRef;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_buddies);

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        FindBuddiesRecyclerList = (RecyclerView) findViewById(R.id.find_buddies_recycler_list);
        FindBuddiesRecyclerList.setLayoutManager(new LinearLayoutManager(this));



        mToolbar = (Toolbar)findViewById(R.id.find_buddies_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Buddies");

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        prepareAd();


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contact> options = new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(UserRef,Contact.class)
                .build();


        FirebaseRecyclerAdapter<Contact,FindFriendViewHolder> adapter = new FirebaseRecyclerAdapter<Contact, FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contact model)
            {
                holder.userName.setText(model.getName());
                holder.userStatus.setText(model.getStatus());
                Picasso.get().load(model.getImage()).into(holder.profileImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        String visit_user_id = getRef(position).getKey();
                        Intent profileIntent = new Intent(FindBuddiesActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("visit_user_id",visit_user_id);
                        startActivity(profileIntent);

                    }
                });


            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_list_layout, parent,false);
                FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);
                return viewHolder;
            }
        };

        FindBuddiesRecyclerList.setAdapter(adapter);
        adapter.startListening();


    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;

        public FindFriendViewHolder(@NonNull View itemView)
        {

            super(itemView);
            userName=itemView.findViewById(R.id.User_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.Users_profile_image);
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
