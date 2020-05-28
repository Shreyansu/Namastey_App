package com.example.namastey;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {
    private View private_chat_list;
    private DatabaseReference chatsRef,usersRef;
    private RecyclerView chatsList;
    private String currentUserId;
    private FirebaseAuth mAuth;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        private_chat_list = inflater.inflate(R.layout.fragment_chats,container,false);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatsList =(RecyclerView) private_chat_list.findViewById(R.id.chat_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        //added sethasfixed size
        chatsList.setHasFixedSize(true);



        return private_chat_list;
    }

    @Override
    public void onStart()
    {

        super.onStart();
        FirebaseRecyclerOptions<Contact> options = new FirebaseRecyclerOptions.Builder<Contact>()
            .setQuery(chatsRef,Contact.class)
            .build();

        FirebaseRecyclerAdapter<Contact,ChatViewHolder> adapter = new FirebaseRecyclerAdapter<Contact, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatViewHolder holder, int position, @NonNull Contact model)
            {
                final String usersIds = getRef(position).getKey();
                final String[] retImage = {"default_Image"};
                usersRef.child(usersIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {

                        if(dataSnapshot.hasChild("image"))
                        {
                            retImage[0] = dataSnapshot.child("image").getValue().toString();
                            Picasso.get().load(retImage[0]).into(holder.userImage);


                        }
                        final String retName = dataSnapshot.child("name").getValue().toString();
                        final String retStatus = dataSnapshot.child("status").getValue().toString();

                        holder.userName.setText(retName);
                        holder.userStatus.setText(retStatus);


                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                chatIntent.putExtra("visit_user_id",usersIds);
                                chatIntent.putExtra("visit_user_name",retName);
                                chatIntent.putExtra("visit_user_Image",retImage);
                                startActivity(chatIntent);

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_list_layout, parent, false);
                return new ChatViewHolder(view);

            }
        };

        chatsList.setAdapter(adapter);
        adapter.startListening();


    }


    public static class ChatViewHolder extends RecyclerView.ViewHolder
    {
        private TextView userName,userStatus;
        CircleImageView userImage;

        public ChatViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.User_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            userImage = itemView.findViewById(R.id.Users_profile_image);
        }
    }
}
