package com.example.poker;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class QuickmatchGameActivity extends AppCompatActivity {

    ImageView CurrentUserImage, UserImage1, UserImage2, UserImage3, UserImage4;
    ImageView displayCard1, displayCard2, displayCard3, displayCard4, displayCard5;

    TextView currentUserName, userName1, userName2, userName3, userName4;
    TextView currentUserMoney, userMoney1, userMoney2, userMoney3, userMoney4;
    TextView amountMoneyBet;

    private DatabaseReference mUsersDatabase;

    String username;

    JSONArray usersList;
    JSONArray waitingList;
    JSONArray tableInitialCards;
    JSONArray userHand;


    String userName, userId;
    int numUsers;
    int userPosition;

    //Whether user is playing, spectating, or offline
    enum UserStatus {
        PLAYING,
        WAITING,
        OFFLINE
    }

    String tableState;





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


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            userName = user.getDisplayName();
            userId = user.getUid();
        }

        //Firebase Database for users
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


        //Connect app to server when this activity is entered
        //mSocket.on(Socket.EVENT_CONNECT,onConnect);
        mSocket.connect();
        mSocket.on(Socket.EVENT_CONNECT, joinLobby);
        mSocket.on("login", onLogin);
        mSocket.on("game start", onGameStart);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("user left", onUserLeave);
        mSocket.on("hand", onHand);


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

        //Money of the players
        userMoney1 = findViewById(R.id.userMoney1);
        userMoney2 = findViewById(R.id.userMoney2);
        userMoney3 = findViewById(R.id.userMoney3);
        userMoney4 = findViewById(R.id.userMoney4);
        currentUserMoney = findViewById(R.id.currentUserMoney);

        //User names
        currentUserName = findViewById(R.id.currentUserName);
        userName1 = findViewById(R.id.userName1);
        userName2 = findViewById(R.id.userName2);
        userName3 = findViewById(R.id.userName3);
        userName4 = findViewById(R.id.userName4);

        //Money bet on the table of all users
        amountMoneyBet = findViewById(R.id.amountMoneyBet);





    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();


        mSocket.off("user joined", onUserJoined);

    }


    private Emitter.Listener joinLobby = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mSocket.emit("add user", userId);

                }
            });
        }
    };

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    JSONObject data = (JSONObject) args[0];

                    try {



                        numUsers = data.getInt("numUsers");
                        tableState = data.getString("tableState");
                        waitingList = data.getJSONArray("waitingList");
                        usersList = data.getJSONArray("users");






                        //Toast.makeText(QuickmatchGameActivity.this, tableState + "" , Toast.LENGTH_SHORT).show();

                        //This is to check if table seat is available
                        if(tableState.equals("available")){
                            currentUserName.setText(userName);
                           // Toast.makeText(QuickmatchGameActivity.this, "You have joined a table", Toast.LENGTH_SHORT).show();
                        } else {
                           // Toast.makeText(QuickmatchGameActivity.this, "You are in waiting list " + waitingList, Toast.LENGTH_SHORT).show();

                        }
                        //adds already online people to the chairs
                        switch(numUsers) {

                            case 1:

                                break;
                            case 2:
                                userName1.setText(usersList.getString(0));
                                break;
                            case 3:
                                userName1.setText(usersList.getString(0));
                                userName2.setText(usersList.getString(1));
                                break;
                            case 4:
                                userName1.setText(usersList.getString(0));
                                userName2.setText(usersList.getString(1));
                                userName3.setText(usersList.getString(2));
                                break;
                            case 5:
                                userName1.setText(usersList.getString(0));
                                userName2.setText(usersList.getString(1));
                                userName3.setText(usersList.getString(2));
                                userName4.setText(usersList.getString(3));
                                break;

                        }


                    } catch (JSONException e) {

                        return;
                    }

                }
            });
        }
    };



    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    final String userId;


                    try {
                        userId = data.getString("username");
                        numUsers = data.getInt("numUsers");
                        usersList = data.getJSONArray("users");




                        mUsersDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                username = dataSnapshot.child(userId).child("userName").getValue().toString();

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        switch(numUsers) {
                            case 1:
                                Toast.makeText(QuickmatchGameActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                                break;
                            case 2:

                                if(usersList.getString(1) != userId) {
                                    userName1.setText(username);
                                } else {
                                    Toast.makeText(QuickmatchGameActivity.this, "It is you", Toast.LENGTH_SHORT).show();
                                }

                                break;
                            case 3:
                                userName2.setText(username);
                                break;
                            case 4:
                                userName3.setText(username);
                                break;
                            case 5:
                                userName4.setText(username);
                                break;

                        }




                    } catch (JSONException e) {

                        return;
                    }

                }
            });
        }
    };

    private Emitter.Listener onUserLeave = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    final String userId;


                    try {
                        userId = data.getString("username");
                        numUsers = data.getInt("numUsers");
                        usersList = data.getJSONArray("users");

                       // Toast.makeText(QuickmatchGameActivity.this, "" + usersList, Toast.LENGTH_SHORT).show();
                        mUsersDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                username = dataSnapshot.child(userId).child("userName").getValue().toString();
                                userMoney1.setText(username);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });




                    } catch (JSONException e) {

                        return;
                    }

                }
            });
        }
    };

    private Emitter.Listener onGameStart = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];



                    try {



                        tableInitialCards = data.getJSONArray("firstThreeCardsTable");



                        String mCardName1 = tableInitialCards.getString(0);
                        String mCardName2 = tableInitialCards.getString(1);
                        String mCardName3 = tableInitialCards.getString(2);

                        int mFirstCard = getResources().getIdentifier(mCardName1 , "drawable", getPackageName());
                        int mSecondCard = getResources().getIdentifier(mCardName2 , "drawable", getPackageName());
                        int mThirdCard = getResources().getIdentifier(mCardName3 , "drawable", getPackageName());

                        Drawable firstCard = getResources().getDrawable(mFirstCard);
                        Drawable secondCard = getResources().getDrawable(mSecondCard);
                        Drawable thirdCard = getResources().getDrawable(mThirdCard);

                        displayCard1.setImageDrawable(firstCard);
                        displayCard2.setImageDrawable(secondCard);
                        displayCard3.setImageDrawable(thirdCard);




                    } catch (JSONException e) {

                        return;
                    }

                }
            });
        }
    };


    private Emitter.Listener onHand = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];

                    try {
                        userHand = data.getJSONArray("userHand");
                        Toast.makeText(QuickmatchGameActivity.this, "" + userHand, Toast.LENGTH_SHORT).show();


                    } catch (JSONException e) {

                        return;
                    }

                }
            });
        }
    };


}
