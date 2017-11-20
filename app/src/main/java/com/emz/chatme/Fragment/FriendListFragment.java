package com.emz.chatme.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.emz.chatme.Model.UserModel;
import com.emz.chatme.R;
import com.emz.chatme.ViewHolder.MyFriendViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class FriendListFragment extends Fragment {
    private static final String TAG = "FriendListFragment";

    private DatabaseReference mFirebaseDatabaseReference;

    private FirebaseRecyclerAdapter<UserModel, MyFriendViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    public FriendListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_friend, container, false);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mRecycler = (RecyclerView) rootView.findViewById(R.id.friend_list);
        mRecycler.setHasFixedSize(true);

        mManager = new LinearLayoutManager(getActivity());
        mRecycler.setLayoutManager(mManager);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Query ref = mFirebaseDatabaseReference.child("users");
        mAdapter = new FirebaseRecyclerAdapter<UserModel, MyFriendViewHolder>(UserModel.class, R.layout.item_friend, MyFriendViewHolder.class, ref){
            @Override
            protected void populateViewHolder(MyFriendViewHolder viewHolder, UserModel model, int position) {
                viewHolder.bindToItem(model);
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }
}
