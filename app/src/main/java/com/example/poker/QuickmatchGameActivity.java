package com.example.poker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;


import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;

import java.net.URISyntaxException;

public class QuickmatchGameActivity extends AppCompatActivity {

    ImageView CurrentUserImage, UserImage1, UserImage2, UserImage3, UserImage4;
    ImageView displayCard1, displayCard2, displayCard3, displayCard4, displayCard5;


    //declares what server to connect to
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("https://servertest999.herokuapp.com/");
        } catch (URISyntaxException e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Starts activity horizontally and fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_quickmatch_game);

        //Connect app to server when this activity is entered
        //mSocket.on(Socket.EVENT_CONNECT,onConnect);
        mSocket.connect();
        mSocket.emit("add user", "poker");

        //Profile Images of players
        CurrentUserImage = findViewById(R.id.CurrentUserImage);
        UserImage1 = findViewById(R.id.UserImage1);
        UserImage2 = findViewById(R.id.UserImage2 );
        UserImage3 = findViewById(R.id.UserImage3);
        UserImage4 = findViewById(R.id.UserImage4);

        //Cards Displayed on Table
        displayCard1 = findViewById(R.id.displayCard1);
        displayCard2 = findViewById(R.id.displayCard2);
        displayCard3 = findViewById(R.id.displayCard3);
        displayCard4 = findViewById(R.id.displayCard4);
        displayCard5 = findViewById(R.id.displayCard5);







    }
}
