package com.example.poker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class UsernameActivity extends AppCompatActivity {

    //Databases
    DatabaseReference databaseUsers;
    //DatabaseReference databaseStats;



    //String profileImageUrl;
    FirebaseAuth mAuth;

    Button buttonSave;
    EditText editTextDisplayName;
    ImageView profilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Starts activity horizontally and fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_username);

        //Database instances
        databaseUsers = FirebaseDatabase.getInstance().getReference("Users");
       // databaseStats = FirebaseDatabase.getInstance().getReference("UserStats");

        //Button to save the profile
        buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveProfile();

            }
        });

        //Set Profile image
        profilePicture = findViewById(R.id.profilePicture);
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    private void saveProfile() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        editTextDisplayName = findViewById(R.id.editTextDisplayName);

        final String displayName = editTextDisplayName.getText().toString();
/*
                if(displayName.isEmpty()) {
                    editTextDisplayName.setError("Name required");
                    editTextDisplayName.requestFocus();
                    return;
                }
*/
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                //.setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            //Add user in database
                            String id =  user.getUid();
                            Users user = new Users(id, displayName);

                            databaseUsers.child(id).setValue(user);

                            //Set starting stats to the user



                            //Log.d(TAG, "User profile updated.");
                            Toast.makeText(UsernameActivity.this, "Username Set!", Toast.LENGTH_SHORT).show();
                            Intent profileUpdated = new Intent(UsernameActivity.this, MainPageActivity.class);
                            startActivity(profileUpdated);
                        }

                    }
                });
    }


}
