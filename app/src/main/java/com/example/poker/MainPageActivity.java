package com.example.poker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainPageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    DatabaseReference mUserDatabase;


    TextView userName, userBalance;
    ImageButton buttonProfile, buttonFriends, buttonSettings, buttonLeaderboard;
    Button buttonJoinTable, buttonCreateTable, buttonQuickMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Starts activity horizontally and fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main_page);

        buttonProfile = findViewById(R.id.buttonProfile);
        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent profile = new Intent(MainPageActivity.this, UserProfileActivity.class);
                startActivity(profile);

            }
        });

        buttonFriends = findViewById(R.id.buttonFriends);
        buttonFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent friends = new Intent(MainPageActivity.this, FriendsActivity.class);
                startActivity(friends);
            }
        });

        buttonSettings = findViewById(R.id.buttonSettings);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        buttonLeaderboard = findViewById(R.id.buttonLeaderboard);
        buttonLeaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        buttonJoinTable = findViewById(R.id.buttonJoinTable);
        buttonJoinTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainPageActivity.this, JoinGameActivity.class);
                startActivity(intent);

            }
        });

        buttonCreateTable = findViewById(R.id.buttonCreateTable);
        buttonCreateTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainPageActivity.this, CreateGameActivity.class);
                startActivity(intent);

            }
        });

        buttonQuickMatch = findViewById(R.id.buttonQuickMatch);
        buttonQuickMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainPageActivity.this, QuickmatchGameActivity.class);
                startActivity(intent);
            }
        });

    }



    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            String name = user.getDisplayName();
            userName = findViewById(R.id.userName);
            userName.setText(name);

            userBalance = findViewById(R.id.userBalance);

            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());

            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String Level = dataSnapshot.child("userBalance").getValue().toString();
                    userBalance.setText(Level);


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



        }

    }
}
