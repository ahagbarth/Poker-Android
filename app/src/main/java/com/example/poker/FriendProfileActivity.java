package com.example.poker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FriendProfileActivity extends AppCompatActivity {

    FirebaseUser mUser;

    private DatabaseReference mUsersDatabase;

    private TextView mProfileName;
    private Button mProfileSendReqBtn;
    private Button mProfileDecline;

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



        //Receive intent
        Intent intent = getIntent();
        String id = intent.getStringExtra("user_id");

        //Database Reference
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(id);

        //Receiving user data
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.child("userName").getValue().toString();

                mProfileName.setText(userName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





    }

}
