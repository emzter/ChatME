package com.emz.chatme.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.emz.chatme.Adapter.ChatFirebaseAdapter;
import com.emz.chatme.Adapter.ClickListenerChatFirebase;
import com.emz.chatme.Model.ChatModel;
import com.emz.chatme.Model.FileModel;
import com.emz.chatme.Model.Location;
import com.emz.chatme.Model.UserModel;
import com.emz.chatme.R;
import com.emz.chatme.SignInActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

import static android.app.Activity.RESULT_OK;
import static com.emz.chatme.Util.UIUpdateClass.createSnackbar;
import static com.emz.chatme.Util.Util.FOLDER_STORAGE_IMG;
import static com.emz.chatme.Util.Util.URL_STORAGE_REFERENCE;
import static com.emz.chatme.Util.Util.convertString;
import static com.emz.chatme.Util.Util.verifyConnectivity;

/**
 * Created by AeMzAKuN on 22/11/2559.
 */

public class ChatRoomFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, ClickListenerChatFirebase {
    private static final String TAG = "ChatRoomFragment";
    public static final String MESSAGES_CHILD = "messages";
    private static final int IMAGE_GALLERY_REQUEST = 1;
    private static final int IMAGE_CAMERA_REQUEST = 2;
    private static final int PLACE_PICKER_REQUEST = 3;

    private Button sendButton;
    private ProgressBar progressBar;
    private RecyclerView messageRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private EmojiconEditText messageEditText;
    private EmojIconActions emojIcon;
    private View rootView;

    private UserModel user;
    private File filePathImageCamera;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mFirebaseDatabaseReference;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    public ChatRoomFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_chat_room, container, false);

        if (!verifyConnectivity(getContext())) {
            createSnackbar(rootView, "Please connect the internet");
        } else {
            bindView(rootView);
            authCheck();
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .enableAutoManage(getActivity(), this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API)
                    .build();
        }

        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sendButton:
                sendMessageFirebase();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        StorageReference storageRef = storage.getReferenceFromUrl(URL_STORAGE_REFERENCE).child(FOLDER_STORAGE_IMG);

        if (requestCode == IMAGE_GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri selectImageUri = data.getData();
                if (selectImageUri != null) {
                    sendFileFirebase(storageRef, selectImageUri);
                }
            }
        } else if (requestCode == IMAGE_CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (filePathImageCamera != null) {
                    StorageReference imageCameraRef = storageRef.child(filePathImageCamera.getName() + "_camera");
                    sendFileFirebase(imageCameraRef, filePathImageCamera);
                }
            }
        } else if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(getContext(), data);
                if (place != null) {
                    LatLng latLng = place.getLatLng();
                    Location location = new Location(latLng.latitude + "", latLng.longitude + "");
                    ChatModel chat = new ChatModel(user, Calendar.getInstance().getTime().getTime() + "", location);
                    mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(chat);
                }
            }
        }
    }

    private void sendMessageFirebase() {
        ChatModel chat = new ChatModel(user, convertString(messageEditText), Calendar.getInstance().getTime().getTime() + "", null);
        mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(chat);
        messageEditText.setText(null);
    }

    public void authCheck() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            toSignInActivity();
        } else {
            user = new UserModel(mFirebaseUser.getUid());
            getMessageFirebase();
        }
    }

    private void toSignInActivity() {
        startActivity(new Intent(getActivity(), SignInActivity.class));
    }

    private void sendFileFirebase(StorageReference storageRef, final Uri file) {
        if (storageRef != null) {
            final String name = DateFormat.format("dd-MM-yyyy_hhmmss", new Date()).toString();
            StorageReference imageRef = storageRef.child(name + "_gallery");
            UploadTask uploadTask = imageRef.putFile(file);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i(TAG, "onSuccess sendFileFirebase");
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    FileModel fileTarget = new FileModel("img", downloadUri.toString(), name, "");
                    ChatModel chat = new ChatModel(user, "", Calendar.getInstance().getTime().getTime() + "", fileTarget);
                    mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(chat);
                }
            });
        }
    }

    private void sendFileFirebase(StorageReference storageRef, final File file) {
        if(storageRef != null){
            UploadTask uploadTask = storageRef.putFile(Uri.fromFile(file));
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i(TAG, "onSuccess sendFileFirebase");
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    FileModel fileTarget = new FileModel("img", downloadUrl.toString(), file.getName(), file.length() + "");
                    ChatModel chat = new ChatModel(user, "", Calendar.getInstance().getTime().getTime() + "", fileTarget);
                    mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(chat);
                }
            });
        }
    }

    private void getMessageFirebase() {
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        final ChatFirebaseAdapter firebaseAdapter = new ChatFirebaseAdapter(mFirebaseDatabaseReference.child(MESSAGES_CHILD), user.getId(), this);
        firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver(){
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = firebaseAdapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    messageRecyclerView.scrollToPosition(positionStart);
                }
                progressBar.setVisibility(progressBar.INVISIBLE);
            }
        });
        messageRecyclerView.setLayoutManager(linearLayoutManager);
        messageRecyclerView.setAdapter(firebaseAdapter);
    }

    private void bindView(View view) {
        sendButton = (Button) view.findViewById(R.id.sendButton);
        sendButton.setEnabled(false);
        sendButton.setOnClickListener(this);
        messageEditText = (EmojiconEditText) view.findViewById(R.id.messageEditText);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        messageRecyclerView = (RecyclerView) view.findViewById(R.id.messageRecyclerView);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(linearLayoutManager);
        rootView = view.findViewById(R.id.chat_room_root_view);
    }

    @Override
    public void clickImageChat(View view, int position, String nameUser, String urlPhotoUser, String urlPhotoClick) {

    }

    @Override
    public void clickImageMapChat(View view, int position, String latitude, String longitude) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
}
