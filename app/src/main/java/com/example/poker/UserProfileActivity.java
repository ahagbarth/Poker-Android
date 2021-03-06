package com.example.poker;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity {

    DatabaseReference mUserDatabase;

    TextView userName, userLevel;

    ImageView profileImage;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Starts activity horizontally and fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_user_profile);



    }

    @Override
    protected void onStart() {
        super.onStart();
        if(user!=null) {
            //User Name
            userName = findViewById(R.id.userName);
            userName.setText(user.getDisplayName().toString());

            //User Level
            userLevel = findViewById(R.id.userLevel);

            //User Profile Picture
            profileImage = findViewById(R.id.profileImage);
            Uri imageUrl = user.getPhotoUrl();

            if(imageUrl != null){
                Picasso.with(this).load(user.getPhotoUrl().toString()).resize(200,200).into(profileImage);
            }

            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());

            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String Level = dataSnapshot.child("userLevel").getValue().toString();
                    userLevel.setText(Level);


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });




        }


    }
}
