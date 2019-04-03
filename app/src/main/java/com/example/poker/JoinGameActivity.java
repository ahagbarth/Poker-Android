package com.example.poker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class JoinGameActivity extends AppCompatActivity {

    private RecyclerView mGamesList;
    private DatabaseReference mGamesDatabases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Starts activity horizontally and fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_join_game);

        mGamesDatabases = FirebaseDatabase.getInstance().getReference().child("Games");

        mGamesList = findViewById(R.id.gamesList);
        mGamesList.setHasFixedSize(true);
        mGamesList.setLayoutManager(new LinearLayoutManager(this));



    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Games, GamesViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Games, GamesViewHolder>(

                Games.class,
                R.layout.gamecard,
                GamesViewHolder.class,
                mGamesDatabases.orderByChild("gameName")

        ) {
            @Override
            protected void populateViewHolder(GamesViewHolder viewHolder, Games model, int position) {
                viewHolder.setName(model.getGameName());


                final String tableName =  getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        Intent profileIntent = new Intent(JoinGameActivity.this, QuickmatchGameActivity.class);
                        profileIntent.putExtra("RoomName", tableName);
                        startActivity(profileIntent);

                    }
                });

            }


        };

        mGamesList.setAdapter(firebaseRecyclerAdapter);



    }

    public static class GamesViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public GamesViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
        }
        public void setName(String name){
            TextView userNameView = mView.findViewById(R.id.gameName);
            userNameView.setText(name);



        }





    }


}
