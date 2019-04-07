package com.example.poker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateGameActivity extends AppCompatActivity {

    EditText roomName;
    Button buttonCreateRoom;

    String RoomName;

    private DatabaseReference mGamesDatabases;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Starts activity horizontally and fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_create_game);

        roomName = findViewById(R.id.roomName);
        buttonCreateRoom = findViewById(R.id.createRoom);

        //Database Reference
        mGamesDatabases = FirebaseDatabase.getInstance().getReference().child("Games");



        buttonCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mGamesDatabases.child(roomName.getText().toString()).child("gameName").setValue(roomName.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        mGamesDatabases.child(roomName.getText().toString()).child("gameState").setValue(0).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(CreateGameActivity.this, "Game Created", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                Intent intent = new Intent(CreateGameActivity.this, QuickmatchGameActivity.class);
                intent.putExtra("RoomName", roomName.getText().toString());
                startActivity(intent);

            }
        });



    }
}
