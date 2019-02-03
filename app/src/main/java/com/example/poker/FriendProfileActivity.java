package com.example.poker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FriendProfileActivity extends AppCompatActivity {

    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

    //Databases
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendsDatabase;

    private String friendState;

    String userName;

    private TextView mProfileName;
    private Button mProfileSendReqBtn;
    private Button mProfileDecline;
    private Button mAcceptRequest;
    private TextView mUserLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Starts activity horizontally and fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_friend_profile);

        //Declarations
        mProfileName = findViewById(R.id.userName);
        mProfileSendReqBtn = findViewById(R.id.buttonFriendRequest);
        mProfileDecline = findViewById(R.id.ProfileDeclinebtn);
        mAcceptRequest = findViewById(R.id.buttonAcceptRequest);
        mUserLevel = findViewById(R.id.userLevel);

        friendState = "notFriends";

        //Receive intent
        Intent intent = getIntent();
        final String id = intent.getStringExtra("user_id");

        //Database Reference
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");


        //Receiving user data
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //This is to retrieve name and level info from user
                userName = dataSnapshot.child("userName").getValue().toString();
                String userLevel = dataSnapshot.child("userLevel").getValue().toString();

                mProfileName.setText(userName);
                mUserLevel.setText(userLevel);

                //This is to retrieve friend status
                mFriendRequestDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child("Sender").child(mUser.getUid()).hasChild(id)) {
                            Toast.makeText(FriendProfileActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();

                        }
                        if(dataSnapshot.child("Receiver").child(mUser.getUid()).hasChild(id)) {
                            Toast.makeText(FriendProfileActivity.this, "Request Received", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*
        mFriendRequestDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean test = dataSnapshot.child("Sender").child(mUser.getUid()).child(id).toString().isEmpty();

                if(test ==true) {
                    Toast.makeText(FriendProfileActivity.this, "Friend request sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FriendProfileActivity.this, "Not Sent", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


*/


        //Adding functionality to the buttons
        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(friendState == "notFriends") {
                    mFriendRequestDatabase.child("Sender").child(mUser.getUid()).child(id).setValue("Sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mFriendRequestDatabase.child("Receiver").child(id).child(mUser.getUid()).setValue("Received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    Toast.makeText(FriendProfileActivity.this, "Friend Request Sent", Toast.LENGTH_SHORT).show();
                                    friendState = "requestSent";
                                    mProfileSendReqBtn.setVisibility(View.GONE);

                                }
                            });
                        }
                    });

                }

            }
        });


        mProfileDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

             if(friendState == "requestSent"){
                 mFriendRequestDatabase.child("Receiver").child(id).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                     @Override
                     public void onComplete(@NonNull Task<Void> task) {
                      mFriendRequestDatabase.child("Sender").child(mUser.getUid()).child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                          @Override
                          public void onComplete(@NonNull Task<Void> task) {
                              Toast.makeText(FriendProfileActivity.this, "Request Cancelled", Toast.LENGTH_SHORT).show();
                          }
                      });
                     }
                 });
             }

            }
        });

        mAcceptRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


    }

}
