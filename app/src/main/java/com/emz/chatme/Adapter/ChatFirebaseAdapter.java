package com.emz.chatme.Adapter;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.emz.chatme.Model.ChatModel;
import com.emz.chatme.Model.UserModel;
import com.emz.chatme.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

import static com.emz.chatme.Util.Util.local;

/**
 * Created by AeMzAKuN on 18/10/2559.
 */

public class ChatFirebaseAdapter extends FirebaseRecyclerAdapter<ChatModel, ChatFirebaseAdapter.MyChatViewHolder> {

    private static final int RIGHT_MSG = 0;
    private static final int LEFT_MSG = 1;
    private static final int RIGHT_MSG_IMG = 2;
    private static final int LEFT_MSG_IMG = 3;

    private ClickListenerChatFirebase mClickListenerChatFirebase;
    private DatabaseReference mFirebaseDatabaseReference;

    private String nameID;

    public ChatFirebaseAdapter(DatabaseReference ref, String nameID, ClickListenerChatFirebase mClickListenerChatFirebase) {
        super(ChatModel.class, R.layout.item_message_left, ChatFirebaseAdapter.MyChatViewHolder.class, ref);
        this.nameID = nameID;
        this.mClickListenerChatFirebase = mClickListenerChatFirebase;
    }

    @Override
    public MyChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType == RIGHT_MSG) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right, parent, false);
            return new MyChatViewHolder(v);
        } else if (viewType == LEFT_MSG) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left, parent, false);
            return new MyChatViewHolder(v);
        } else if (viewType == RIGHT_MSG_IMG) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right_image, parent, false);
            return new MyChatViewHolder(v);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left_image, parent, false);
            return new MyChatViewHolder(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatModel model = getItem(position);
        if (model.getMapModel() != null) {
            if (model.getUser().getId().equals(nameID)) {
                return RIGHT_MSG_IMG;
            } else {
                return LEFT_MSG_IMG;
            }
        } else if (model.getFile() != null) {
            if (model.getFile().getType().equals("img") && model.getUser().getId().equals(nameID)) {
                return RIGHT_MSG_IMG;
            } else {
                return LEFT_MSG_IMG;
            }
        } else if (model.getUser().getId().equals(nameID)) {
            return RIGHT_MSG;
        } else {
            return LEFT_MSG;
        }
    }

    @Override
    protected void populateViewHolder(final MyChatViewHolder viewHolder, final ChatModel model, int position) {
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Query ref = mFirebaseDatabaseReference.child("users").child(model.getUser().getId());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserModel user = dataSnapshot.getValue(UserModel.class);
                viewHolder.setUserImage(user.getProfilePic());
                viewHolder.setTvUserName(user.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        viewHolder.setTxtMessage(model.getMessage());
        viewHolder.setTvTimestamp(model.getTimeStamp());
        viewHolder.tvIsLocation(View.GONE);
        if (model.getFile() != null) {
            viewHolder.tvIsLocation(View.GONE);
            viewHolder.setIvChatPhoto(model.getFile().getFile_url());
        } else if (model.getMapModel() != null) {
            viewHolder.setIvChatPhoto(local(model.getMapModel().getLatitude(), model.getMapModel().getLongitude()));
            viewHolder.tvIsLocation(View.VISIBLE);
        }
    }

    public class MyChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvTimestamp, tvLocation, tvUserName;
        EmojiconTextView txtMessage;
        ImageView userImage, ivChatPhoto;

        public MyChatViewHolder(View itemView) {
            super(itemView);
            tvTimestamp = (TextView) itemView.findViewById(R.id.timestamp);
            txtMessage = (EmojiconTextView) itemView.findViewById(R.id.userMessageTextView);
            tvLocation = (TextView) itemView.findViewById(R.id.tvLocation);
            ivChatPhoto = (ImageView) itemView.findViewById(R.id.img_chat);
            userImage = (ImageView) itemView.findViewById(R.id.userImage);
            tvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            ChatModel model = getItem(position);
            if (model.getMapModel() != null) {
                mClickListenerChatFirebase.clickImageMapChat(v, position, model.getMapModel().getLatitude(), model.getMapModel().getLongitude());
            } else {
                mClickListenerChatFirebase.clickImageChat(v, position, model.getUser().getName(), model.getUser().getProfilePic(), model.getFile().getFile_url());
            }
        }

        public void setTxtMessage(String message) {
            if (txtMessage == null) return;
            txtMessage.setText(message);
        }

        public void setUserImage(String urlPhotoUser) {
            if (urlPhotoUser == null){
                return;
            }else{
                Glide.with(userImage.getContext()).load(urlPhotoUser).into(userImage);
            }
        }

        public void setTvUserName(String name){
            if (tvUserName == null) return;
            tvUserName.setText(name);
        }

        public void setTvTimestamp(String timestamp) {
            if (tvTimestamp == null) return;
            tvTimestamp.setText(converteTimestamp(timestamp));
        }

        public void setIvChatPhoto(String url) {
            if (ivChatPhoto == null) return;
            Glide.with(ivChatPhoto.getContext()).load(url)
                    .override(100, 100)
                    .fitCenter()
                    .into(ivChatPhoto);
            ivChatPhoto.setOnClickListener(this);
        }

        public void tvIsLocation(int visible) {
            if (tvLocation == null) return;
            tvLocation.setVisibility(visible);
        }
    }

    private CharSequence converteTimestamp(String mileSegundos) {
        return DateUtils.getRelativeTimeSpanString(Long.parseLong(mileSegundos), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
    }
}

