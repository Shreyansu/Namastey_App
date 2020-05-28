package com.example.namastey;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
public class RequestFragment extends Fragment {
    private View RequestFragmentView;
    private RecyclerView myRequestList;
    private DatabaseReference chatRequestRef,UsersRef,contactRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RequestFragmentView=inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        myRequestList = (RecyclerView) RequestFragmentView.findViewById(R.id.chat_request_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));


        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requsets");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        return RequestFragmentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        FirebaseRecyclerOptions<Contact> options = new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(chatRequestRef.child(currentUserId),Contact.class)
                .build();

        FirebaseRecyclerAdapter<Contact,RequestViewHolder> adapter = new FirebaseRecyclerAdapter<Contact, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contact model)
            {
                holder.itemView.findViewById(R.id.request_accept).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel).setVisibility(View.VISIBLE);

                final String list_user_id = getRef(position).getKey();

                DatabaseReference getTypeRef = getRef(position).child("Request Type").getRef();
                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            String type = dataSnapshot.getValue().toString();

                            if(type.equals("recieved"))
                            {
                                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        if(dataSnapshot.hasChild("image"))
                                        {
                                            final String requestuserImage = dataSnapshot.child("image").getValue().toString();
                                            Picasso.get().load(requestuserImage).into(holder.ProfileImage);

                                        }
                                        final String requestuserName = dataSnapshot.child("name").getValue().toString();
                                        final String requestuserStatus = dataSnapshot.child("status").getValue().toString();


                                        holder.userName.setText(requestuserName);
                                        holder.userStatus.setText("Wants to connect with you");

                                      holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                CharSequence[] options = new CharSequence[]
                                                        {
                                                                "Accept",
                                                                "Cancel"
                                                        };

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(requestuserName + "Chat Request");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which)
                                                    {
                                                        if(which == 0)
                                                        {
                                                            contactRef.child(currentUserId).child(list_user_id).child("contacts")
                                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if(task.isSuccessful())
                                                                    {
                                                                        contactRef.child(list_user_id).child(currentUserId).child("contacts")
                                                                                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                            {
                                                                                if(task.isSuccessful())
                                                                                {
                                                                                    chatRequestRef.child(currentUserId).child(list_user_id)
                                                                                            .removeValue()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                                {
                                                                                                    if(task.isSuccessful())
                                                                                                    {
                                                                                                        chatRequestRef.child(list_user_id).child(currentUserId)
                                                                                                                .removeValue()
                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                                                    {
                                                                                                                        if(task.isSuccessful())
                                                                                                                        {
                                                                                                                            Toast.makeText(getContext(),"Contact Saved",Toast.LENGTH_SHORT).show();
                                                                                                                        }


                                                                                                                    }
                                                                                                                });
                                                                                                    }


                                                                                                }
                                                                                            });

                                                                                }

                                                                            }
                                                                        });

                                                                    }

                                                                }
                                                            });

                                                        }
                                                        if(which==1)
                                                        {
                                                            chatRequestRef.child(currentUserId).child(list_user_id)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                chatRequestRef.child(list_user_id).child(currentUserId)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                            {
                                                                                                if(task.isSuccessful())
                                                                                                {
                                                                                                    Toast.makeText(getContext(),"Contact Deleted",Toast.LENGTH_SHORT).show();
                                                                                                }


                                                                                            }
                                                                                        });
                                                                            }


                                                                        }
                                                                    });

                                                        }

                                                    }
                                                });
                                                builder.show();

                                            }
                                      });

                                    }


                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                            else if(type.equals("sent"))
                            {
                                Button request_sent_btn = holder.itemView.findViewById(R.id.request_accept);
                                request_sent_btn.setText("Req Sent");

                                holder.itemView.findViewById(R.id.request_cancel).setVisibility(View.INVISIBLE);


                                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        if(dataSnapshot.hasChild("image"))
                                        {
                                            final String requestuserImage = dataSnapshot.child("image").getValue().toString();
                                            Picasso.get().load(requestuserImage).into(holder.ProfileImage);

                                        }
                                        final String requestuserName = dataSnapshot.child("name").getValue().toString();
                                        final String requestuserStatus = dataSnapshot.child("status").getValue().toString();


                                        holder.userName.setText(requestuserName);
                                        holder.userStatus.setText("You have sent Friend Request");

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                CharSequence[] options = new CharSequence[]
                                                        {

                                                                "Cancel Chat Request"
                                                        };

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle( "Already sent request");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which)
                                                    {
                                                        if(which==0)
                                                        {
                                                            chatRequestRef.child(currentUserId).child(list_user_id)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                chatRequestRef.child(list_user_id).child(currentUserId)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                            {
                                                                                                if(task.isSuccessful())
                                                                                                {
                                                                                                    Toast.makeText(getContext(),"You Have cancelled the Chat request",Toast.LENGTH_SHORT).show();
                                                                                                }


                                                                                            }
                                                                                        });
                                                                            }


                                                                        }
                                                                    });

                                                        }

                                                    }
                                                });
                                                builder.show();



                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_list_layout, parent, false);
                RequestViewHolder Holder = new RequestViewHolder(view);
                return Holder;
            }
        };
        myRequestList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView ProfileImage;
        Button acceptBtn,cancelBtn;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.User_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            ProfileImage = itemView.findViewById(R.id.Users_profile_image);
            acceptBtn = itemView.findViewById(R.id.request_accept);
            cancelBtn = itemView.findViewById(R.id.request_cancel);
        }
    }
}
