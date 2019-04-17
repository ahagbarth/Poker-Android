package com.example.poker;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuickmatchGameActivity extends AppCompatActivity {

    ImageView CurrentUserImage, userImage1, userImage2, userImage3, userImage4;
    ImageView displayCard1, displayCard2, displayCard3, displayCard4, displayCard5;
    ImageView currentUserCard1, currentUserCard2;

    TextView currentUserName, userName1, userName2, userName3, userName4;
    TextView currentUserBalance, userBalance1, userBalance2, userBalance3, userBalance4;
    TextView amountMoneyBet;
    TextView currentUserBet, userMoneyBet1, userMoneyBet2, userMoneyBet3, userMoneyBet4;
    TextView turnDisplay;
    TextView dollarSign1, dollarSign2, dollarSign3, dollarSign4, dollarSign5, dollarSign6, dollarSign7, dollarSign8;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mGamesDatabase;

    String username;

    JSONArray usersList;
    JSONArray waitingList;
    JSONArray tableInitialCards;
    JSONArray userHand;


    String userName, userId;
    int numUsers;
    int userPosition;
    String currentUserMoney;
    int currentMoney;
    String currentGameState;
    int currentState;

    //Whether user is playing, spectating, or offline
    enum UserStatus {
        PLAYING,
        WAITING,
        OFFLINE
    }

    String tableState;

    Boolean myTurn = false;

    Button buttonBet, buttonCall, buttonFold, buttonGameState;

    int currentMaxBet = 0;

    String roomName;

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

        //Firebase Databases
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mGamesDatabase = FirebaseDatabase.getInstance().getReference().child("Games");


        //Connect app to server when this activity is entered
        //mSocket.on(Socket.EVENT_CONNECT,onConnect);
        mSocket.connect();
        mSocket.on(Socket.EVENT_CONNECT, joinLobby);
        mSocket.on(Socket.EVENT_DISCONNECT, disconnectLobby);

        mSocket.on("login", onLogin);
        mSocket.on("game start", onGameStart);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("user left", onUserLeave);
        mSocket.on("hand", onHand);
        mSocket.on("roundOne", onRoundOne);
        mSocket.on("roundTwo", onRoundTwo);
        mSocket.on("roundThree", onRoundThree);
        mSocket.on("finalRound", onFinalRound);
        mSocket.on("passTurn", onTurnPass);
        mSocket.on("betMoney", onMoneyBet);
        mSocket.on("HandCompare", onResults);

        //These are the dollar signs
        dollarSign1 = findViewById(R.id.dollarSign1);
        dollarSign2 = findViewById(R.id.dollarSign2);
        dollarSign3 = findViewById(R.id.dollarSign3);
        dollarSign4 = findViewById(R.id.dollarSign4);
        dollarSign5 = findViewById(R.id.dollarSign5);
        dollarSign6 = findViewById(R.id.dollarSign6);
        dollarSign7 = findViewById(R.id.dollarSign7);
        dollarSign8 = findViewById(R.id.dollarSign8);


        //Profile Images of players
        CurrentUserImage = findViewById(R.id.CurrentUserImage);
        userImage1 = findViewById(R.id.UserImage1);
        userImage2 = findViewById(R.id.UserImage2 );
        userImage3 = findViewById(R.id.UserImage3);
        userImage4 = findViewById(R.id.UserImage4);

        //Cards Displayed on Table
        displayCard1 = findViewById(R.id.displayCard1);
        displayCard2 = findViewById(R.id.displayCard2);
        displayCard3 = findViewById(R.id.displayCard3);
        displayCard4 = findViewById(R.id.displayCard4);
        displayCard5 = findViewById(R.id.displayCard5);

        //User hand cards
        currentUserCard1 = findViewById(R.id.currentUserCard1);
        currentUserCard2 = findViewById(R.id.currentUserCard2);

        //Money of the players
        userBalance1 = findViewById(R.id.userMoney1);
        userBalance2 = findViewById(R.id.userMoney2);
        userBalance3 = findViewById(R.id.userMoney3);
        userBalance4 = findViewById(R.id.userMoney4);
        currentUserBalance = findViewById(R.id.currentUserMoney);

        //User names
        currentUserName = findViewById(R.id.currentUserName);
        userName1 = findViewById(R.id.userName1);
        userName2 = findViewById(R.id.userName2);
        userName3 = findViewById(R.id.userName3);
        userName4 = findViewById(R.id.userName4);

        //Money bet on the table of all users
        amountMoneyBet = findViewById(R.id.amountMoneyBet);

        //Money users bet between rounds
        currentUserBet = findViewById(R.id.currentUserBet);
        userMoneyBet1 = findViewById(R.id.userMoneyBet1);
        userMoneyBet2 = findViewById(R.id.userMoneyBet2);
        userMoneyBet3 = findViewById(R.id.userMoneyBet3);
        userMoneyBet4 = findViewById(R.id.userMoneyBet4);

        //Setting other users profile info to invisible until they join
        userName1.setVisibility(View.GONE);
        userName2.setVisibility(View.GONE);
        userName3.setVisibility(View.GONE);
        userName4.setVisibility(View.GONE);

        userBalance1.setVisibility(View.GONE);
        userBalance2.setVisibility(View.GONE);
        userBalance3.setVisibility(View.GONE);
        userBalance4.setVisibility(View.GONE);

        userImage1.setVisibility(View.GONE);
        userImage2.setVisibility(View.GONE);
        userImage3.setVisibility(View.GONE);
        userImage4.setVisibility(View.GONE);

        userMoneyBet1.setVisibility(View.GONE);
        userMoneyBet2.setVisibility(View.GONE);
        userMoneyBet3.setVisibility(View.GONE);
        userMoneyBet4.setVisibility(View.GONE);

        dollarSign1.setVisibility(View.GONE);
        dollarSign2.setVisibility(View.GONE);
        dollarSign3.setVisibility(View.GONE);
        dollarSign4.setVisibility(View.GONE);
        dollarSign5.setVisibility(View.GONE);
        dollarSign6.setVisibility(View.GONE);
        dollarSign7.setVisibility(View.GONE);
        dollarSign8.setVisibility(View.GONE);


        //Buttons for betting/calling/fold
        buttonBet = findViewById(R.id.buttonBet);
        buttonCall = findViewById(R.id.buttonCall);
        buttonFold = findViewById(R.id.buttonFold);
        buttonGameState = findViewById(R.id.buttonGameState);

        //Display turn signal
        turnDisplay = findViewById(R.id.turnDisplay);
        turnDisplay.setVisibility(View.GONE);



        buttonBet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(myTurn) {


                    //mSocket.emit("change game state", "");
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("username", userId);
                        jsonObject.put("betValue", 10);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    mSocket.emit("betAmount", jsonObject);
                    mSocket.emit("pass_turn", "");

                    currentUserBet.setText("10");


                    mUsersDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            currentUserMoney = dataSnapshot.child(userId).child("userBalance").getValue().toString();
                            String currentUserBetValue = dataSnapshot.child(userId).child("userBet").getValue().toString();
                            currentMoney = Integer.parseInt(currentUserMoney);
                            int finalmoney = currentMoney - 10;



                            DatabaseReference refUser = mUsersDatabase.child(userId);
                            Map<String, Object> updateMoney = new HashMap<>();
                            updateMoney.put("userBalance", finalmoney);

                            updateMoney.put("userBet", 10);





                            refUser.updateChildren(updateMoney);




                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });






                } else {
                    Toast.makeText(QuickmatchGameActivity.this, "Wait for your turn", Toast.LENGTH_SHORT).show();
                }






            }

        });

        buttonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSocket.emit("pass_turn", "");
            }
        });

        buttonFold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
        buttonGameState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mGamesDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        currentGameState = dataSnapshot.child(roomName).child("gameState").getValue().toString();
                        currentState = Integer.parseInt(currentGameState);
                        int finalState = currentState + 1;

                        if (finalState == 5){
                            finalState = 0;
                        }

                        DatabaseReference refUser = mGamesDatabase.child(roomName);
                        Map<String, Object> updateMoney = new HashMap<>();
                        updateMoney.put("gameState", finalState);


                        mSocket.emit("change game state", currentGameState);

                        refUser.updateChildren(updateMoney);




                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });



    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        mSocket.disconnect();


        mSocket.off("user joined", onUserJoined);
        mSocket.off(Socket.EVENT_CONNECT, joinLobby);
        mSocket.off("login", onLogin);
        mSocket.off("game start", onGameStart);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("user left", onUserLeave);
        mSocket.on("hand", onHand);
        mSocket.on("roundOne", onRoundOne);
        mSocket.on("roundTwo", onRoundTwo);
        mSocket.on("roundThree", onRoundThree);
        mSocket.on("finalRound", onFinalRound);
        mSocket.on("passTurn", onTurnPass);
        mSocket.on("betMoney", onMoneyBet);

        mSocket.off(Socket.EVENT_DISCONNECT, disconnectLobby);



    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();


        mSocket.off("user joined", onUserJoined);
        mSocket.off(Socket.EVENT_CONNECT, joinLobby);
        mSocket.off("login", onLogin);
        mSocket.off("game start", onGameStart);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("user left", onUserLeave);
        mSocket.on("hand", onHand);
        mSocket.on("roundOne", onRoundOne);
        mSocket.on("roundTwo", onRoundTwo);
        mSocket.on("roundThree", onRoundThree);
        mSocket.on("finalRound", onFinalRound);
        mSocket.on("passTurn", onTurnPass);
        mSocket.on("betMoney", onMoneyBet);

        mSocket.off(Socket.EVENT_DISCONNECT, disconnectLobby);




    }


    private Emitter.Listener joinLobby = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //Room Name
                    Intent intent = getIntent();
                    roomName = intent.getStringExtra("RoomName");
                    Toast.makeText(QuickmatchGameActivity.this, roomName, Toast.LENGTH_SHORT).show();


                    mSocket.emit("add user", userId);
                    mSocket.emit("room", roomName);


                }
            });
        }
    };

    private Emitter.Listener disconnectLobby = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {



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
                        userPosition = data.getInt("userPosition");


                        //Toast.makeText(QuickmatchGameActivity.this, "" + userPosition, Toast.LENGTH_SHORT).show();
                        Toast.makeText(QuickmatchGameActivity.this, ""+numUsers, Toast.LENGTH_SHORT).show();
                        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

                        mUsersDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String currentUserMoney = dataSnapshot.child(userId).child("userBalance").getValue().toString();
                                String userBet = dataSnapshot.child(userId).child("userBet").getValue().toString();

                                int betUser = Integer.parseInt(userBet);

                                //Toast.makeText(QuickmatchGameActivity.this, "" + currentMaxBet, Toast.LENGTH_SHORT).show();

                                int userNumber = usersList.length();
                                //Toast.makeText(QuickmatchGameActivity.this, "userNumber: " + userNumber + " / numUsers:   " + numUsers + "  / userList: " + usersList, Toast.LENGTH_SHORT).show();
                                currentUserBalance.setText(currentUserMoney);
                                String currentImage = dataSnapshot.child(userId).child("imageURI").getValue().toString();
                                Picasso.with(getApplicationContext()).load(currentImage).resize(200,200).into(CurrentUserImage);

                                switch(numUsers) {

                                    case 1:

                                        mGamesDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                String numberUsers = dataSnapshot.child(roomName).child("numUsers").getValue().toString();
                                                int currentUserNumber = Integer.parseInt(numberUsers);

                                                DatabaseReference referenceUserNumber = mGamesDatabase.child(roomName);
                                                Map<String, Object> updateNumber = new HashMap<>();
                                                updateNumber.put("gameState", 0);


                                                mSocket.emit("change game state", 0);

                                                referenceUserNumber.updateChildren(updateNumber);




                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });


                                        break;
                                    case 2:
                                        try {

                                            userName1.setVisibility(View.VISIBLE);
                                            userImage1.setVisibility(View.VISIBLE);
                                            userBalance1.setVisibility(View.VISIBLE);
                                            userMoneyBet1.setVisibility(View.VISIBLE);
                                            dollarSign1.setVisibility(View.VISIBLE);
                                            dollarSign5.setVisibility(View.VISIBLE);
                                            userMoneyBet1.setText(userBet);

                                            String name1 = dataSnapshot.child(usersList.getString(0)).child("userName").getValue().toString();
                                            userName1.setText(name1);
                                            String userMoney1 = dataSnapshot.child(usersList.getString(0)).child("userBalance").getValue().toString();
                                            userBalance1.setText(userMoney1);

                                            String UserImage = dataSnapshot.child(usersList.getString(0)).child("imageURI").getValue().toString();
                                            Picasso.with(getApplicationContext()).load(UserImage).resize(200,200).into(userImage1);

                                            if(betUser >= currentMaxBet) {
                                                currentMaxBet = betUser;
                                                mSocket.emit("currentBet" ,currentMaxBet);
                                            }


                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case 3:
                                        try {
                                            userName1.setVisibility(View.VISIBLE);
                                            userImage1.setVisibility(View.VISIBLE);
                                            userBalance1.setVisibility(View.VISIBLE);
                                            userMoneyBet1.setVisibility(View.VISIBLE);
                                            dollarSign1.setVisibility(View.VISIBLE);
                                            dollarSign5.setVisibility(View.VISIBLE);
                                            String name1 = dataSnapshot.child(usersList.getString(0)).child("userName").getValue().toString();
                                            userName1.setText(name1);
                                            String userMoney1 = dataSnapshot.child(usersList.getString(0)).child("userBalance").getValue().toString();
                                            userBalance1.setText(userMoney1);
                                            userMoneyBet1.setText(userBet);
                                            String UserImage = dataSnapshot.child(usersList.getString(0)).child("imageURI").getValue().toString();
                                            Picasso.with(getApplicationContext()).load(UserImage).resize(200,200).into(userImage1);
                                            if(betUser > currentMaxBet) {
                                                currentMaxBet = betUser;
                                                mSocket.emit("currentBet" ,currentMaxBet);
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            userName2.setVisibility(View.VISIBLE);
                                            userImage2.setVisibility(View.VISIBLE);
                                            userBalance2.setVisibility(View.VISIBLE);
                                            userMoneyBet2.setVisibility(View.VISIBLE);
                                            dollarSign2.setVisibility(View.VISIBLE);
                                            dollarSign6.setVisibility(View.VISIBLE);
                                            String name2 = dataSnapshot.child(usersList.getString(1)).child("userName").getValue().toString();
                                            userName2.setText(name2);
                                            String userMoney2 = dataSnapshot.child(usersList.getString(1)).child("userBalance").getValue().toString();
                                            userBalance2.setText(userMoney2);
                                            userMoneyBet2.setText(userBet);
                                            String UserImage2 = dataSnapshot.child(usersList.getString(1)).child("imageURI").getValue().toString();
                                            Picasso.with(getApplicationContext()).load(UserImage2).resize(200,200).into(userImage2);
                                            if(betUser > currentMaxBet) {
                                                currentMaxBet = betUser;
                                                mSocket.emit("currentBet" ,currentMaxBet);
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case 4:
                                        try {
                                            userName1.setVisibility(View.VISIBLE);
                                            userImage1.setVisibility(View.VISIBLE);
                                            userBalance1.setVisibility(View.VISIBLE);
                                            userMoneyBet1.setVisibility(View.VISIBLE);
                                            dollarSign1.setVisibility(View.VISIBLE);
                                            dollarSign5.setVisibility(View.VISIBLE);
                                            userMoneyBet1.setText(userBet);
                                            String name1 = dataSnapshot.child(usersList.getString(0)).child("userName").getValue().toString();
                                            userName1.setText(name1);
                                            String userMoney1 = dataSnapshot.child(usersList.getString(0)).child("userBalance").getValue().toString();
                                            userBalance1.setText(userMoney1);
                                            String UserImage1 = dataSnapshot.child(usersList.getString(0)).child("imageURI").getValue().toString();
                                            Picasso.with(getApplicationContext()).load(UserImage1).resize(200,200).into(userImage1);
                                            if(betUser > currentMaxBet) {
                                                currentMaxBet = betUser;
                                                mSocket.emit("currentBet" ,currentMaxBet);
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            userName2.setVisibility(View.VISIBLE);
                                            userImage2.setVisibility(View.VISIBLE);
                                            userBalance2.setVisibility(View.VISIBLE);
                                            userMoneyBet2.setVisibility(View.VISIBLE);
                                            dollarSign2.setVisibility(View.VISIBLE);
                                            dollarSign6.setVisibility(View.VISIBLE);

                                            String name2 = dataSnapshot.child(usersList.getString(1)).child("userName").getValue().toString();
                                            userName2.setText(name2);
                                            String userMoney2 = dataSnapshot.child(usersList.getString(1)).child("userBalance").getValue().toString();
                                            userBalance2.setText(userMoney2);
                                            userMoneyBet2.setText(userBet);
                                            String UserImage2 = dataSnapshot.child(usersList.getString(1)).child("imageURI").getValue().toString();
                                            Picasso.with(getApplicationContext()).load(UserImage2).resize(200,200).into(userImage2);
                                            if(betUser > currentMaxBet) {
                                                currentMaxBet = betUser;
                                                mSocket.emit("currentBet" ,currentMaxBet);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            userName3.setVisibility(View.VISIBLE);
                                            userImage3.setVisibility(View.VISIBLE);
                                            userBalance3.setVisibility(View.VISIBLE);
                                            userMoneyBet3.setVisibility(View.VISIBLE);
                                            dollarSign3.setVisibility(View.VISIBLE);
                                            dollarSign7.setVisibility(View.VISIBLE);

                                            String name3 = dataSnapshot.child(usersList.getString(2)).child("userName").getValue().toString();
                                            userName3.setText(name3);
                                            String userMoney3 = dataSnapshot.child(usersList.getString(2)).child("userBalance").getValue().toString();
                                            userBalance3.setText(userMoney3);
                                            userMoneyBet3.setText(userBet);
                                            String UserImage3 = dataSnapshot.child(usersList.getString(2)).child("imageURI").getValue().toString();
                                            Picasso.with(getApplicationContext()).load(UserImage3).resize(200,200).into(userImage3);
                                            if(betUser > currentMaxBet) {
                                                currentMaxBet = betUser;
                                                mSocket.emit("currentBet" ,currentMaxBet);
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case 5:
                                        try {
                                            userName1.setVisibility(View.VISIBLE);
                                            userImage1.setVisibility(View.VISIBLE);
                                            userBalance1.setVisibility(View.VISIBLE);
                                            userMoneyBet1.setVisibility(View.VISIBLE);
                                            dollarSign1.setVisibility(View.VISIBLE);
                                            dollarSign5.setVisibility(View.VISIBLE);
                                            String name1 = dataSnapshot.child(usersList.getString(0)).child("userName").getValue().toString();
                                            userName1.setText(name1);
                                            String userMoney1 = dataSnapshot.child(usersList.getString(0)).child("userBalance").getValue().toString();
                                            userBalance1.setText(userMoney1);
                                            userMoneyBet1.setText(userBet);
                                            String UserImage1 = dataSnapshot.child(usersList.getString(0)).child("imageURI").getValue().toString();
                                            Picasso.with(getApplicationContext()).load(UserImage1).resize(200,200).into(userImage1);
                                            if(betUser > currentMaxBet) {
                                                currentMaxBet = betUser;
                                                mSocket.emit("currentBet" ,currentMaxBet);
                                            }


                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            userName2.setVisibility(View.VISIBLE);
                                            userImage2.setVisibility(View.VISIBLE);
                                            userBalance2.setVisibility(View.VISIBLE);
                                            userMoneyBet2.setVisibility(View.VISIBLE);
                                            dollarSign2.setVisibility(View.VISIBLE);
                                            dollarSign6.setVisibility(View.VISIBLE);
                                            String name2 = dataSnapshot.child(usersList.getString(1)).child("userName").getValue().toString();
                                            userName2.setText(name2);
                                            String userMoney2 = dataSnapshot.child(usersList.getString(1)).child("userBalance").getValue().toString();
                                            userBalance2.setText(userMoney2);
                                            userMoneyBet2.setText(userBet);
                                            String UserImage2 = dataSnapshot.child(usersList.getString(1)).child("imageURI").getValue().toString();
                                            Picasso.with(getApplicationContext()).load(UserImage2).resize(200,200).into(userImage2);
                                            if(betUser > currentMaxBet) {
                                                currentMaxBet = betUser;
                                                mSocket.emit("currentBet" ,currentMaxBet);
                                            }


                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            userName3.setVisibility(View.VISIBLE);
                                            userImage3.setVisibility(View.VISIBLE);
                                            userBalance3.setVisibility(View.VISIBLE);
                                            userMoneyBet3.setVisibility(View.VISIBLE);
                                            dollarSign3.setVisibility(View.VISIBLE);
                                            dollarSign7.setVisibility(View.VISIBLE);
                                            String name3 = dataSnapshot.child(usersList.getString(2)).child("userName").getValue().toString();
                                            userName3.setText(name3);
                                            String userMoney3 = dataSnapshot.child(usersList.getString(2)).child("userBalance").getValue().toString();
                                            userBalance3.setText(userMoney3);
                                            userMoneyBet3.setText(userBet);
                                            String UserImage3 = dataSnapshot.child(usersList.getString(2)).child("imageURI").getValue().toString();
                                            Picasso.with(getApplicationContext()).load(UserImage3).resize(200,200).into(userImage3);
                                            if(betUser > currentMaxBet) {
                                                currentMaxBet = betUser;
                                                mSocket.emit("currentBet" ,currentMaxBet);
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            userName4.setVisibility(View.VISIBLE);
                                            userImage4.setVisibility(View.VISIBLE);
                                            userBalance4.setVisibility(View.VISIBLE);
                                            userMoneyBet4.setVisibility(View.VISIBLE);
                                            dollarSign4.setVisibility(View.VISIBLE);
                                            dollarSign8.setVisibility(View.VISIBLE);
                                            String name4 = dataSnapshot.child(usersList.getString(3)).child("userName").getValue().toString();
                                            userName4.setText(name4);
                                            String userMoney4 = dataSnapshot.child(usersList.getString(3)).child("userBalance").getValue().toString();
                                            userBalance4.setText(userMoney4);
                                            userMoneyBet4.setText(userBet);
                                            String UserImage4 = dataSnapshot.child(usersList.getString(3)).child("imageURI").getValue().toString();
                                            Picasso.with(getApplicationContext()).load(UserImage4).resize(200,200).into(userImage4);
                                            if(betUser > currentMaxBet) {
                                                currentMaxBet = betUser;
                                                mSocket.emit("currentBet" ,currentMaxBet);
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        break;

                                }



                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });



                        //Toast.makeText(QuickmatchGameActivity.this, tableState + "" , Toast.LENGTH_SHORT).show();

                        //This is to check if table seat is available
                        if(tableState.equals("available")){
                            currentUserName.setText(userName);
                            // Toast.makeText(QuickmatchGameActivity.this, "You have joined a table", Toast.LENGTH_SHORT).show();
                        } else {
                            // Toast.makeText(QuickmatchGameActivity.this, "You are in waiting list " + waitingList, Toast.LENGTH_SHORT).show();

                        }
                        //adds already online people to the chairs



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

                        Toast.makeText(QuickmatchGameActivity.this, "User Joined", Toast.LENGTH_SHORT).show();


                        mUsersDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                username = dataSnapshot.child(userId).child("userName").getValue().toString();
                                String userBalance = dataSnapshot.child(userId).child("userBalance").getValue().toString();
                                String userBet = dataSnapshot.child(userId).child("userBet").getValue().toString();
                                String UserImage = dataSnapshot.child(userId).child("imageURI").getValue().toString();


                                int betUser = Integer.parseInt(userBet);

                                // Toast.makeText(QuickmatchGameActivity.this, "" + currentMaxBet, Toast.LENGTH_SHORT).show();
                                switch(numUsers) {
                                    case 1:
                                        Toast.makeText(QuickmatchGameActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                                        break;
                                    case 2:

                                        userName1.setVisibility(View.VISIBLE);
                                        userImage1.setVisibility(View.VISIBLE);
                                        userBalance1.setVisibility(View.VISIBLE);
                                        userMoneyBet1.setVisibility(View.VISIBLE);
                                        dollarSign1.setVisibility(View.VISIBLE);
                                        dollarSign5.setVisibility(View.VISIBLE);
                                        userName1.setText(username);
                                        userBalance1.setText(userBalance);
                                        userMoneyBet1.setText(userBet);
                                        Picasso.with(getApplicationContext()).load(UserImage).resize(200,200).into(userImage1);

                                        if(betUser > currentMaxBet) {
                                            currentMaxBet = betUser;
                                            mSocket.emit("currentBet" ,currentMaxBet);
                                        }


                                        break;
                                    case 3:
                                        userName2.setVisibility(View.VISIBLE);
                                        userImage2.setVisibility(View.VISIBLE);
                                        userBalance2.setVisibility(View.VISIBLE);
                                        userMoneyBet2.setVisibility(View.VISIBLE);
                                        dollarSign2.setVisibility(View.VISIBLE);
                                        dollarSign6.setVisibility(View.VISIBLE);
                                        userName2.setText(username);
                                        userBalance2.setText(userBalance);
                                        userMoneyBet2.setText(userBet);
                                        Picasso.with(getApplicationContext()).load(UserImage).resize(200,200).into(userImage2);

                                        if(betUser > currentMaxBet) {
                                            currentMaxBet = betUser;
                                            mSocket.emit("currentBet" ,currentMaxBet);
                                        }


                                        break;
                                    case 4:
                                        userName3.setVisibility(View.VISIBLE);
                                        userImage3.setVisibility(View.VISIBLE);
                                        userBalance3.setVisibility(View.VISIBLE);
                                        userMoneyBet3.setVisibility(View.VISIBLE);
                                        dollarSign3.setVisibility(View.VISIBLE);
                                        dollarSign7.setVisibility(View.VISIBLE);
                                        userName3.setText(username);
                                        userBalance3.setText(userBalance);
                                        userMoneyBet3.setText(userBet);
                                        Picasso.with(getApplicationContext()).load(UserImage).resize(200,200).into(userImage3);
                                        if(betUser > currentMaxBet) {
                                            currentMaxBet = betUser;
                                            mSocket.emit("currentBet" ,currentMaxBet);
                                        }

                                        break;
                                    case 5:
                                        userName4.setVisibility(View.VISIBLE);
                                        userImage4.setVisibility(View.VISIBLE);
                                        userBalance4.setVisibility(View.VISIBLE);
                                        userMoneyBet4.setVisibility(View.VISIBLE);
                                        dollarSign4.setVisibility(View.VISIBLE);
                                        dollarSign8.setVisibility(View.VISIBLE);
                                        userName4.setText(username);
                                        userBalance4.setText(userBalance);
                                        userMoneyBet4.setText(userBet);
                                        Picasso.with(getApplicationContext()).load(UserImage).resize(200,200).into(userImage4);

                                        if(betUser > currentMaxBet) {
                                            currentMaxBet = betUser;
                                            mSocket.emit("currentBet" ,currentMaxBet);
                                        }

                                        break;

                                }

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
                                Toast.makeText(QuickmatchGameActivity.this, ""+ numUsers, Toast.LENGTH_SHORT).show();

                                if(userName1.getText().toString() == username){




                                    userName1.setVisibility(View.GONE);
                                    userImage1.setVisibility(View.GONE);
                                    userBalance1.setVisibility(View.GONE);
                                    userMoneyBet1.setVisibility(View.GONE);
                                    dollarSign1.setVisibility(View.GONE);
                                    dollarSign4.setVisibility(View.GONE);



                                }
                                if(userName2.getText().toString() == username){
                                    userName2.setVisibility(View.GONE);
                                    userImage2.setVisibility(View.GONE);
                                    userBalance2.setVisibility(View.GONE);
                                    userMoneyBet2.setVisibility(View.GONE);
                                    dollarSign2.setVisibility(View.GONE);
                                    dollarSign5.setVisibility(View.GONE);
                                }
                                if(userName3.getText().toString() == username){
                                    userName3.setVisibility(View.GONE);
                                    userImage3.setVisibility(View.GONE);
                                    userBalance3.setVisibility(View.GONE);
                                    userMoneyBet3.setVisibility(View.GONE);
                                    dollarSign3.setVisibility(View.GONE);
                                    dollarSign7.setVisibility(View.GONE);
                                }
                                if(userName4.getText().toString() == username){
                                    userName4.setVisibility(View.GONE);
                                    userImage4.setVisibility(View.GONE);
                                    userBalance4.setVisibility(View.GONE);
                                    userMoneyBet4.setVisibility(View.GONE);
                                    dollarSign4.setVisibility(View.GONE);
                                    dollarSign8.setVisibility(View.GONE);
                                }

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
                    mSocket.emit("ReceiveCard", "");

                    displayCard1.setImageResource(R.drawable.cardback);
                    displayCard2.setImageResource(R.drawable.cardback);
                    displayCard3.setImageResource(R.drawable.cardback);
                    displayCard4.setImageResource(R.drawable.cardback);
                    displayCard5.setImageResource(R.drawable.cardback);


                }
            });
        }
    };


    private Emitter.Listener onRoundOne = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    int round;
                    // int tableBet;


                    try {

                        round = data.getInt("gameState");
                        // tableBet = data.getInt("tableBet");

                        tableInitialCards = data.getJSONArray("firstThreeCardsTable");
                        //amountMoneyBet.setText(tableBet);
                        // Toast.makeText(QuickmatchGameActivity.this, "" + tableInitialCards, Toast.LENGTH_SHORT).show();


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


    private Emitter.Listener onRoundTwo = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    JSONArray roundTwo;

                    //int tableBet;
                    try {


                        roundTwo = data.getJSONArray("secondRoundCard");
                        //tableBet = data.getInt("tableBet");

                        String card4 = roundTwo.getString(0);
                        //amountMoneyBet.setText(tableBet);

                        int mFourthCard = getResources().getIdentifier(card4 , "drawable", getPackageName());

                        Drawable fourthCard = getResources().getDrawable(mFourthCard);


                        displayCard4.setImageDrawable(fourthCard);





                    } catch (JSONException e) {

                        return;
                    }

                }
            });
        }
    };


    private Emitter.Listener onRoundThree = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];

                    JSONArray roundThree;
                    //int tableBet;

                    try {
                        roundThree = data.getJSONArray("finalRoundCard");
                        // tableBet = data.getInt("tableBet");

                        //amountMoneyBet.setText(tableBet);

                        String card5 = roundThree.getString(0);

                        int mFifthCard = getResources().getIdentifier(card5 , "drawable", getPackageName());

                        Drawable fifthCard = getResources().getDrawable(mFifthCard);


                        displayCard5.setImageDrawable(fifthCard);



                    } catch (JSONException e) {

                        return;
                    }

                }
            });
        }
    };


    private Emitter.Listener onFinalRound = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];


                    mSocket.emit("EndGameResults");

                    JSONArray round;
                    //int tableBet;

                    try {
                        //tableBet = data.getInt("tableBet");
                        round = data.getJSONArray("finalRoundCard");
                        //Toast.makeText(QuickmatchGameActivity.this, "" + round.getString(0), Toast.LENGTH_SHORT).show();

                        // amountMoneyBet.setText(tableBet);


                    } catch (JSONException e) {

                        return;
                    }

                }
            });
        }
    };


    private Emitter.Listener onResults = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];

                    String myCards;

                    try {
                        myCards = data.getString("userHandCompare");

                        Toast.makeText(QuickmatchGameActivity.this, "" + myCards, Toast.LENGTH_SHORT).show();

                        // amountMoneyBet.setText(tableBet);


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

                        String card1 = userHand.getString(0);
                        String card2 = userHand.getString(1);

                        int mFirstCard = getResources().getIdentifier(card1 , "drawable", getPackageName());
                        int mSecondCard = getResources().getIdentifier(card2 , "drawable", getPackageName());

                        Drawable firstCard = getResources().getDrawable(mFirstCard);
                        Drawable secondCard = getResources().getDrawable(mSecondCard);

                        currentUserCard1.setImageDrawable(firstCard);
                        currentUserCard2.setImageDrawable(secondCard);


                    } catch (JSONException e) {

                        return;
                    }

                }
            });
        }
    };

    private Emitter.Listener onTurnPass = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    int round;


                    try {

                        round = data.getInt("turnState");
                        //Toast.makeText(QuickmatchGameActivity.this, "" + round, Toast.LENGTH_SHORT).show();


                        if(round ==userPosition) {
                            turnDisplay.setVisibility(View.VISIBLE);
                            myTurn = true;
                        } else {
                            turnDisplay.setVisibility(View.GONE);
                            myTurn = false;
                        }


                    } catch (JSONException e) {

                        return;
                    }

                }
            });
        }
    };


    private Emitter.Listener onMoneyBet = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    final int currentBet;
                    final String userBetter;

                    try {

                        currentBet = data.getInt("currentBet");
                        userBetter = data.getString("better");
                        // Toast.makeText(QuickmatchGameActivity.this, "" + currentBet + "   " + userBetter, Toast.LENGTH_SHORT).show();





                        mUsersDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                username = dataSnapshot.child(userBetter).child("userName").getValue().toString();


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






}