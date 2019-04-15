package com.example.poker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RecyclerAdapter extends Fragment {

    private RecyclerView mFriendsList;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private String mCurrent_user_id;
    private FirebaseAuth mAuth;
    private View mMainView;

    public RecyclerAdapter(){

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.friendsfragment,container,false);

       // mFriendsList = mMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        //mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();



        FirebaseRecyclerAdapter<Users, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Users, FriendsViewHolder>(
                Users.class,
                R.layout.card,
                FriendsViewHolder.class,
                mUsersDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Users model, int position) {



                String list_user_id = mUsersDatabase.getRef().getKey();
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String userName = dataSnapshot.child("userName").getValue().toString();

                        viewHolder.setName(userName);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        mFriendsList.setAdapter(friendsRecyclerViewAdapter);

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String userName) {
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(userName);
        }

    }
}
