package com.example.poker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateGameActivity extends AppCompatActivity {

    EditText roomName;
    Button buttonCreateRoom;

    String RoomName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Starts activity horizontally and fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_create_game);

        roomName = findViewById(R.id.roomName);
        buttonCreateRoom = findViewById(R.id.createRoom);





        buttonCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(CreateGameActivity.this, QuickmatchGameActivity.class);
                intent.putExtra("RoomName", roomName.getText().toString());
                startActivity(intent);

            }
        });



    }
}
