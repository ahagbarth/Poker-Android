package com.example.poker;

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

import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class QuickmatchGameActivity extends AppCompatActivity {

    ImageView CurrentUserImage, UserImage1, UserImage2, UserImage3, UserImage4;
    ImageView displayCard1, displayCard2, displayCard3, displayCard4, displayCard5;
    ImageView currentUserCard1, currentUserCard2;

    TextView currentUserName, userName1, userName2, userName3, userName4;
    TextView currentUserBalance, userBalance1, userBalance2, userBalance3, userBalance4;
    TextView amountMoneyBet;
    TextView currentUserBet, userMoneyBet1, userMoneyBet2, userMoneyBet3, userMoneyBet4;
    TextView turnDisplay;

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

    Boolean myTurn = false;

    Button buttonBet, buttonCall, buttonFold;





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
        mSocket.on("roundOne", onRoundOne);
        mSocket.on("roundTwo", onRoundTwo);
        mSocket.on("roundThree", onRoundThree);
        mSocket.on("finalRound", onFinalRound);
        mSocket.on("passTurn", onTurnPass);
        mSocket.on("betMoney", onMoneyBet);



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




        //Buttons for betting/calling/fold
        buttonBet = findViewById(R.id.buttonBet);
        buttonCall = findViewById(R.id.buttonCall);
        buttonFold = findViewById(R.id.buttonFold);

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
                mSocket.emit("change game state", "");

            }
        });





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
                        userPosition = data.getInt("userPosition");

                        Toast.makeText(QuickmatchGameActivity.this, "" + userPosition, Toast.LENGTH_SHORT).show();

                        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

                        mUsersDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String currentUserMoney = dataSnapshot.child(userId).child("userBalance").getValue().toString();

                                int userNumber = usersList.length();

                                currentUserBalance.setText(currentUserMoney);

                                switch(numUsers) {

                                    case 1:

                                        break;
                                    case 2:
                                        try {
                                            String name1 = dataSnapshot.child(usersList.getString(0)).child("userName").getValue().toString();
                                            userName1.setText(name1);
                                            String userMoney1 = dataSnapshot.child(usersList.getString(0)).child("userBalance").getValue().toString();
                                            userBalance1.setText(userMoney1);

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case 3:
                                        try {
                                            String name1 = dataSnapshot.child(usersList.getString(0)).child("userName").getValue().toString();
                                            userName1.setText(name1);
                                            String userMoney1 = dataSnapshot.child(usersList.getString(0)).child("userBalance").getValue().toString();
                                            userBalance1.setText(userMoney1);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            String name2 = dataSnapshot.child(usersList.getString(1)).child("userName").getValue().toString();
                                            userName2.setText(name2);
                                            String userMoney2 = dataSnapshot.child(usersList.getString(1)).child("userBalance").getValue().toString();
                                            userBalance2.setText(userMoney2);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case 4:
                                        try {
                                            String name1 = dataSnapshot.child(usersList.getString(0)).child("userName").getValue().toString();
                                            userName1.setText(name1);
                                            String userMoney1 = dataSnapshot.child(usersList.getString(0)).child("userBalance").getValue().toString();
                                            userBalance1.setText(userMoney1);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            String name2 = dataSnapshot.child(usersList.getString(1)).child("userName").getValue().toString();
                                            userName2.setText(name2);
                                            String userMoney2 = dataSnapshot.child(usersList.getString(1)).child("userBalance").getValue().toString();
                                            userBalance2.setText(userMoney2);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            String name3 = dataSnapshot.child(usersList.getString(2)).child("userName").getValue().toString();
                                            userName3.setText(name3);
                                            String userMoney3 = dataSnapshot.child(usersList.getString(2)).child("userBalance").getValue().toString();
                                            userBalance3.setText(userMoney3);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case 5:
                                        try {
                                            String name1 = dataSnapshot.child(usersList.getString(0)).child("userName").getValue().toString();
                                            userName1.setText(name1);
                                            String userMoney1 = dataSnapshot.child(usersList.getString(0)).child("userBalance").getValue().toString();
                                            userBalance1.setText(userMoney1);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            String name2 = dataSnapshot.child(usersList.getString(1)).child("userName").getValue().toString();
                                            userName2.setText(name2);
                                            String userMoney2 = dataSnapshot.child(usersList.getString(1)).child("userBalance").getValue().toString();
                                            userBalance2.setText(userMoney2);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            String name3 = dataSnapshot.child(usersList.getString(2)).child("userName").getValue().toString();
                                            userName3.setText(name3);
                                            String userMoney3 = dataSnapshot.child(usersList.getString(2)).child("userBalance").getValue().toString();
                                            userBalance3.setText(userMoney3);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            String name4 = dataSnapshot.child(usersList.getString(3)).child("userName").getValue().toString();
                                            userName4.setText(name4);
                                            String userMoney4 = dataSnapshot.child(usersList.getString(3)).child("userBalance").getValue().toString();
                                            userBalance4.setText(userMoney4);
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


                        Toast.makeText(QuickmatchGameActivity.this, "" + tableInitialCards, Toast.LENGTH_SHORT).show();
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

                    JSONArray round;
                    //int tableBet;

                    try {
                        //tableBet = data.getInt("tableBet");
                        round = data.getJSONArray("finalRoundCard");
                        Toast.makeText(QuickmatchGameActivity.this, "" + round.getString(0), Toast.LENGTH_SHORT).show();

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
                        Toast.makeText(QuickmatchGameActivity.this, "" + round, Toast.LENGTH_SHORT).show();


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
                        //Toast.makeText(QuickmatchGameActivity.this, "" + currentBet + "   " + userBetter, Toast.LENGTH_SHORT).show();


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
