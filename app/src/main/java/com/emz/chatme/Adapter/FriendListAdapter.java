//package com.emz.chatme.Adapter;
//
//import android.content.Context;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import com.bumptech.glide.Glide;
//import com.emz.chatme.Model.UserModel;
//import com.emz.chatme.R;
//import com.emz.chatme.ViewHolder.MyFriendViewHolder;
//
//import java.util.List;
//
//import de.hdodenhof.circleimageview.CircleImageView;
//
///**
// * Created by AeMzAKuN on 23/10/2559.
// */
//
//public class FriendListAdapter extends RecyclerView.Adapter<MyFriendViewHolder> {
//
//    private List<UserModel> friendsList;
//    private Context context;
//
//    public FriendListAdapter(Context context, List<UserModel> friendsList) {
//        this.friendsList = friendsList;
//        this.context = context;
//    }
//
//    @Override
//    public int getItemCount() {
//        return friendsList.size();
//    }
//
//    public Context getContext() {
//        return context;
//    }
//
//    @Override
//    public MyFriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        Context context = parent.getContext();
//        LayoutInflater inflater = LayoutInflater.from(context);
//
//        View view = inflater.inflate(R.layout.item_friend, parent, false);
//
//        MyFriendViewHolder viewHolder = new MyFriendViewHolder(view);
//        return viewHolder;
//    }
//
//    @Override
//    public void onBindViewHolder(MyFriendViewHolder holder, int position) {
//        UserModel users = friendsList.get(position);
//
//        TextView tvUserName = holder.tvUserName;
//        CircleImageView userImage = holder.userImage;
//        tvUserName.setText(users.getName());
//        Glide.with(userImage.getContext()).load(users.getProfilePic()).into(userImage);
//    }
//}
