package com.emz.chatme.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.emz.chatme.Model.UserModel;
import com.emz.chatme.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by AeMzAKuN on 20/10/2559.
 */

public class MyFriendViewHolder extends RecyclerView.ViewHolder {
    public TextView tvUserName;
    public TextView tvLastMessage;
    public CircleImageView userImage;

    public MyFriendViewHolder(View itemView) {
        super(itemView);

        tvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
        tvLastMessage = (TextView) itemView.findViewById(R.id.tvLastMessage);
        userImage = (CircleImageView) itemView.findViewById(R.id.userImage);
    }

    public void bindToItem(UserModel user) {
        tvUserName.setText(user.getName());
        Glide.with(userImage.getContext()).load(user.getProfilePic().toString()).into(userImage);
    }
}
